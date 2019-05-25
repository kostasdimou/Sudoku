import java.util.Scanner;
import java.util.ArrayList;
import java.util.NoSuchElementException;

class Sudoku {
	private static final int MAX = 9;
	private static final int TOTAL = MAX * MAX;

	private static boolean INTERACTIVE = false;
	private static boolean SOLVE = false;
	private static boolean VERBOSE = false;

	private int passages = 0;
	private long startTime = 0;
	private long endTime = 0;
	private ArrayList<Method> methodList = new ArrayList<Method>();
	private ArrayList<Position> goList = new ArrayList<Position>();
	private Scanner in = new Scanner(System.in);
	private Matrix matrix = new Matrix(MAX);

	public enum Method {
		FULL_HOUSE(0, true),
		NAKED_SINGLE(1, true),
		NAKED_PAIR(2, true),
		NAKED_TRIPLE(3, true),
		NAKED_QUAD(4, true),
		NAKED_QUINT(5, true),
		HIDDEN_SINGLE(11, true),
		HIDDEN_PAIR(12, false),
		HIDDEN_TRIPLE(13, false),
		HIDDEN_QUAD(14, false),
		HIDDEN_QUINT(15, false);

		public final int code;
		public final boolean active;

		private Method(int code, boolean active) {
			this.code = code;
			this.active = active;
		}

		public static Method valueOfCode(int code) {
			for (Method method: values())
				if (method.code == code)
					return method;
			return null;
		}
	}

	// Stores the points and numbers combinations we want to examine during solution.
	boolean setGo(String go) {
		go = go.toUpperCase();
		int length = go.length();
		int middle = length / 2;
		String y = go.substring(0, middle);
		String x = go.substring(middle, length);
		int max = Position.getMax();
		if(!Coordinate.validY(y, max) || !Coordinate.validX(x, max)) {
			System.out.println("Incompatible coordinates: " + go);
			return false;
		}
		Position position = new Position(Coordinate.readY(y, max), Coordinate.readX(x));
		goList.add(position);
		return true;
	}

	// Activates the INTERACTIVE mode.
	// The user is prompt to provide the numbers and blanks for populating the matrix.
	void setInteractive() {
		INTERACTIVE = true;
	}

	// Stores the methods we want to utilize for the solution.
	boolean setActive(String methodName) {
		boolean found = false;
		for(Method method: Method.values())
			if(methodName.equals(method.name())) {
				if(!method.active) {
					System.out.println("Inactive method: " + methodName);
					return false;
				}
				if(!methodList.contains(method))
					methodList.add(method);
				found = true;
			}
		if(!found)
			System.out.println("Unsupported method: " + methodName);
		return found;
	}

	// Activates the SOLVE mode.
	void setSolve() {
		SOLVE = true;
	}

	// Activates the VERBOSE mode.
	// Displays the actions and the matrix for each solution passage.
	void setVerbose() {
		VERBOSE = true;
	}

	void read(int depth) {
		Debug.log("read()", depth++);
		String row = null;
		Position position = new Position(0, 0);
		for(int y = 0; y < Position.getMax(); y++) {
			if(INTERACTIVE)
				System.out.print("ROW[" + y + "] = ");
			try {
				row = in.nextLine();
			} catch(NoSuchElementException e) {
				row = null; // empty line
			} catch(Exception e) {
				System.out.println(e);
				return;
			}
			int length = (row != null)? row.length(): 0;
			for(int x = 0; x < Position.getMax(); x++, position.forward(Area.HORIZONTAL))
				if(x < length)
					if(row.charAt(x) == ' ')
						matrix.setNumber(position, 0, depth);
					else
						matrix.setNumber(position, row.charAt(x) - Coordinate.ZERO, depth);
				else
					matrix.setNumber(position, 0, depth);
		}
	}

	void run() {
		int depth = 0;
		System.out.println();
		System.out.println(matrix.diluted("SUDOKU"));
		if(INTERACTIVE)
			System.out.println();
		read(depth);
		matrix.print();
		if(SOLVE)
			solve(depth);
	}

	// Full House
	// A Full House is a row, column or box with a single unsolved cell.
	// 
	//    C1  C2  C3  C4  C5  C6  C7  C8  C9
	//   |===========|===========|===========|
	// R1|   |   |   |   |-1 |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R2|   |   |   |   | 3 |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R3|   |   |   |   | 4 |   |   |   |   |
	//   |===========|===========|===========|
	// R4| 4 | 5 | 6 | 1 | 2 | 3 | 7 | 8 |-9 |
	//   |---+---+---|---+---+---|---+---+---|
	// R5|   |   |   | 4 | 5 | 6 |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R6|   |   |   |-7 | 8 | 9 |   |   |   |
	//   |===========|===========|===========|
	// R7|   |   |   |   | 6 |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R8|   |   |   |   | 7 |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R9|   |   |   |   | 9 |   |   |   |   |
	//   |===========|===========|===========|
	int setFullHouse(Position position, int depth) {
		Debug.log("setFullHouse(position = " + position + ")", depth++);
		int added = 0;
		Cell cell = matrix.getCell(position, depth);
		if(cell.isEmpty()) {
			if(VERBOSE)
				cell.println();
			int max = Position.getMax();
			for(int missing = 1; missing <= max; missing++) {
				if((matrix.count(Area.BLOCK, position, missing, depth) == max - 1) ||
				  (matrix.count(Area.HORIZONTAL, position, missing, depth) == max - 1) ||
				  (matrix.count(Area.VERTICAL, position, missing, depth) == max - 1)) {
					added += matrix.setNumber(position, missing, depth);
					if(VERBOSE)
						System.out.println(Method.FULL_HOUSE.name() + ":" +
							" missing Number(" + missing +")" +
							" at Position" + position);
					break;
				}
			}
		}
		return added;
	}

	int setAllFullHouses(int depth) {
		Debug.log("setAllFullHouses()", depth++);
		int added = 0;
		Position position = new Position(0, 0);
		int max = Position.getMax();
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, position.forward(Area.HORIZONTAL))
				added += setFullHouse(new Position(y, x), depth);
		return added;
	}

	// Naked Single aka Forced Digit aka Sole Candidate
	// A naked single is the last remaining candidate in a cell.
	//
	//    C1  C2  C3  C4  C5  C6  C7  C8  C9
	//   |===========|===========|===========|
	// R1|   |   |   | 1 |   | 2 |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R2|   | 8 |   |   |-3 |   | 4 | 5 |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R3|   |   |   |   |   | 9 |   |   |   |
	//   |===========|===========|===========|
	// R4|   |   |   |   | 6 |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R5|   |   |   |   |   |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R6|   |   |   |   | 7 |   |   |   |   |
	//   |===+===+===|===+===+===|===+===+===|
	// R7|   |   |   |   |   |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R8|   |   |   |   |   |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R9|   |   |   |   |   |   |   |   |   |
	//   |===========|===========|===========|
	int setNakedSingle(Position position, int depth) {
		Debug.log("setNakedSingle(position = " + position + ")", depth++);
		int added = 0;
		Cell cell = matrix.getCell(position, depth);
		if(cell.isEmpty()) {
			if(VERBOSE)
				cell.println();
			if(cell.countCandidates() == 1) {
				int single = matrix.getCandidateAt(position, 0, depth);
				added += matrix.setNumber(position, single, depth);
				if(VERBOSE)
					System.out.println(Method.NAKED_SINGLE.name() + ":" +
						" single Number(" + single +")" +
						" at Position" + position);
			}
		}
		return added;
	}

	int setAllNakedSingles(int depth) {
		Debug.log("setAllNakedSingles()", depth++);
		int added = 0;
		Position position = new Position(0, 0);
		int max = Position.getMax();
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, position.forward(Area.HORIZONTAL))
				added += setNakedSingle(position, depth);
		return added;
	}

	// Naked Pair aka Conjugate Pair
	// The "naked pair" solving technique is an intermediate solving technique.
	// In this technique the Sudoku is scanned for a pair of cells in a row, column or box
	// containing only the same two candidates.
	// Since these candidates must go in these cells, they can therefore be removed from the
	// candidate lists of all other unsolved cells in that row, column or box.
	//
	// Below: +35 the naked pair, -3 the excluding candidates.
	// 
	//    C1  C2  C3  C4  C5  C6  C7  C8  C9
	//   |===========|===========|===========|
	// R1|   |   |   | 7 |   | 6 | 8 |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R2|   |   |   |   |   | 5 |   | 7 |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R3|   |   | 7 |   | 9 | 1 | 6 |   |   |
	//   |===========|===========|===========|
	// R4| 9 | 7 |   |   |   |   |   | 4 |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R5| 8 | 1 |   |   | 7 |   |   | 5 |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R6|+35| 4 |+35| 6 | 2 |-3 |   |-3 |-3 |
	//   |===========|===========|===========|
	// R7|   |   |   |   |   |   | 3 |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R8| 4 | 2 |   |   | 6 |   |   |   | 9 |
	//   |---+---+---|---+---+---|---+---+---|
	// R9| 7 |   |   |   | 8 |   |   |   |   |
	//   |===========|===========|===========|
	//
	// Below: +25 the naked pair, -2 the excluding candidates.
	// 
	//    C1  C2  C3  C4  C5  C6  C7  C8  C9
	//   |===========|===========|===========|
	// R1|   | 5 |   | 1 | 3 | 4 | 6 |   |-2 |
	//   |---+---+---|---+---+---|---+---+---|
	// R2|   | 9 |   | 6 | 5 | 2 | 1 | 3 | 8 |
	//   |---+---+---|---+---+---|---+---+---|
	// R3|   | 3 |-2 | 8 | 7 | 9 |+25| 4 |+25|
	//   |===========|===========|===========|
	// R4| 2 | 1 | 5 |   |   | 3 |   |   | 6 |
	//   |---+---+---|---+---+---|---+---+---|
	// R5|   | 8 |   | 2 | 6 | 1 | 3 | 5 |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R6| 3 | 6 |   |   | 8 | 5 | 9 | 2 | 1 |
	//   |===========|===========|===========|
	// R7|   | 4 |   |   | 2 | 7 |   | 1 | 3 |
	//   |---+---+---|---+---+---|---+---+---|
	// R8|   | 7 | 3 |   |   | 6 |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R9|   | 2 |   | 3 |   | 8 | 7 | 6 |   |
	//   |===========|===========|===========|
	int setNakedSubset(Position position, int amount, int depth) {
		Debug.log("setNakedSubset(position = " + position + ", amount = " + amount + ")", depth++);
		Cell cell = matrix.getCell(position, depth);
		if(cell.isEmpty()) {
			if(VERBOSE)
				cell.println();
			if(cell.countCandidates() == amount) {
				ArrayList<Integer> numbers = cell.getCandidates();
				for(Area area: Area.values()) {
					switch(area) {
						case  BLOCK:
						case  HORIZONTAL:
						case  VERTICAL:
							ArrayList<Position> matches = matrix.matchCandidates(area, position, numbers, 2, depth);
							if(matches.size() == amount) {
								for(Integer number: numbers)
									matrix.removeCandidateIf(area, position, number, matches, depth);
								if(VERBOSE)
									System.out.println(Method.valueOfCode(amount).name() + ":" +
										" found Numbers" + matrix.getCandidates(position, depth) +
										" at Positions" + matches);
							}
							break;
					}
				}
			}
		}
		return 0;
	}

	int setAllNakedSubsets(int amount, int depth) {
		Debug.log("setAllNakedSubsets(amount = " + amount + ")", depth++);
		int added = 0;
		Position position = new Position(0, 0);
		int max = Position.getMax();
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, position.forward(Area.HORIZONTAL))
				added += setNakedSubset(position, amount, depth);
		return added;
	}

	// Hidden Single aka Pinned Digit
	// A Hidden Single is a single candidate remaining for a specific digit in a row, column or box.
	// In Sudoku variants, additional constraints can also produce Hidden Singles.
	//
	//    C1  C2  C3  C4  C5  C6  C7  C8  C9
	//   |===========|===========|===========|
	// R1|   |   |   |   |   |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R2|   |   |   |   |   |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R3|   |   |   |   |   | 4 |   |   |   |
	//   |===========|===========|===========|
	// R4|   |   |   |   |   |   |   |   | 4 |
	//   |---+---+---|---+---+---|---+---+---|
	// R5| 4 |   |   |   | 2 |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R6|   |   |   | 3 |-4 |   |   |   |   |
	//   |===========|===========|===========|
	// R7|   |   |   |   |   |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R8|   |   |   |   |   |   |   |   |   |
	//   |---+---+---|---+---+---|---+---+---|
	// R9|   |   |   |   |   |   |   |   |   |
	//   |===========|===========|===========|
	int setHiddenSingle(Position position, int depth) {
		Debug.log("setHiddenSingle(position = " + position + ")", depth++);
		int added = 0;
		Cell cell = matrix.getCell(position, depth);
		if(cell.isEmpty()) {
			if(VERBOSE)
				cell.println();
			int max = Position.getMax();
			for(int hidden = 1; hidden <= max; hidden++) {
				if((matrix.existsCandidate(position, hidden, depth)) &&
				  (matrix.countCandidateIf(Area.BLOCK, position, hidden, depth) == 1) ||
				  (matrix.countCandidateIf(Area.HORIZONTAL, position, hidden, depth) == 1) ||
				  (matrix.countCandidateIf(Area.VERTICAL, position, hidden, depth) == 1)) {
					added += matrix.setNumber(position, hidden, depth);
					if(VERBOSE)
						System.out.println(Method.HIDDEN_SINGLE.name() + ":" +
							" single Number(" + hidden +")" +
							" at Position" + position);
					break;
				}
			}
		}
		return added;
	}

	int setAllHiddenSingles(int depth) {
		Debug.log("setAllHiddenSingles()", depth++);
		int added = 0;
		Position position = new Position(0, 0);
		int max = Position.getMax();
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, position.forward(Area.HORIZONTAL))
				added += setHiddenSingle(position, depth);
		return added;
	}

	void solve(int depth) {
		Debug.log("solve()", depth++);
		startTime = System.nanoTime();
		if(methodList.size() == 0)
			for(Method method: Method.values())
				methodList.add(method);
		int added = TOTAL;
		if(goList.size() > 0)
			for(Position position: goList) {
				// REMOVERS
				for(Method method: Method.values())
					switch(method) {
						case NAKED_PAIR:
						case NAKED_TRIPLE:
						case NAKED_QUAD:
							if(methodList.contains(method))
								setNakedSubset(position, method.code, depth);
							break;
					}
				// SETTERS
				added = 0;
				if(methodList.contains(Method.FULL_HOUSE))
					added += setFullHouse(position, depth);
				if(methodList.contains(Method.NAKED_SINGLE))
					added += setNakedSingle(position, depth);
				if(methodList.contains(Method.HIDDEN_SINGLE))
					added += setHiddenSingle(position, depth);
				Debug.var("added", added, depth);
				if((added > 0) && VERBOSE)
					matrix.print();
			}
		else {
			int counter = matrix.countAll();
			while((counter < TOTAL) && (added > 0)) {
				passages++;
				// REMOVERS
				for(Method method: Method.values())
					switch(method) {
						case NAKED_PAIR:
						case NAKED_TRIPLE:
						case NAKED_QUAD:
							if(methodList.contains(method))
								setAllNakedSubsets(method.code, depth);
							break;
					}
				// SETTERS
				added = 0;
				if(methodList.contains(Method.FULL_HOUSE))
					added += setAllFullHouses(depth);
				if(methodList.contains(Method.NAKED_SINGLE))
					added += setAllNakedSingles(depth);
				if(methodList.contains(Method.HIDDEN_SINGLE))
					added += setAllHiddenSingles(depth);
				Debug.var("added", added, depth);
				if((added > 0) && VERBOSE)
					matrix.print();
				if(INTERACTIVE) {
					String pause = null;
					try {
						pause = in.nextLine();
					} catch(Exception e) {
						System.out.println(e);
					}
				}
				counter += added;
			}
		}
		endTime = System.nanoTime();
		if(!VERBOSE)
			matrix.print();
		statistics();
	}

	void statistics() {
		System.out.println();
		System.out.println(matrix.diluted("STATISTICS"));
		System.out.println();
		System.out.println("passages " + passages);
		String unit = "ns";
		double duration = (double)endTime - startTime;
		if(duration > 1000) {
			duration /= 1000;
			unit = "µs";
		}
		if(duration > 1000) {
			duration /= 1000;
			unit = "ms";
		}
		if(duration > 1000) {
			duration /= 1000;
			unit = "s";
		}
		String decimal;
		if(duration > 10)
			decimal = new String("%.1f");
		else
			decimal = new String("%.2f");
		System.out.println("duration " + String.format(decimal, duration) + " " + unit);
	}

	public static void help() {
		System.out.println("Sudoku.java by Kostas Dimou @ 2019");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("    Sudoku [--analyze] [--coordinates FORMAT] [--debug] [--go YX] [--help] \\");
		System.out.println("           [--interactive] [--method METHOD] [--solve] [--verbose]");
		System.out.println();
		System.out.println("Where:");
		System.out.println("    -a or --analyze:");
		System.out.println("        Displays the posible positions for each number.");
		System.out.println("    -c or --coordinates:");
		System.out.println("        Selects the coordinates format for the Sudoku matrix.");
		System.out.println("        Available formats:");
		System.out.println("            CHESS:  Y=9..1,   X=A..I");
		System.out.println("            JAVA:   Y=0..9,   X=0..9 (default)");
		System.out.println("            ROWCOL: Y=R1..R9, X=C1..C9");
		System.out.println("            SUDOKU: Y=A..I,   X=1..9");
		System.out.println("    -d or --debug:");
		System.out.println("        Activates the debugging mode.");
		System.out.println("    -g or --go:");
		System.out.println("        Check a specific coordinate.");
		System.out.println("        Example for row = 4, column = 0:");
		System.out.println("            -c CHESS  -g 5A");
		System.out.println("            -c JAVA   -g 40");
		System.out.println("            -c ROWCOL -g R5C1");
		System.out.println("            -c SUDOKU -g D1");
		System.out.println("    -h or --help:");
		System.out.println("        Displays this help message and exits.");
		System.out.println("    -i or --interactive:");
		System.out.println("        Displays a prompt for each imput row and pauses on each passage.");
		System.out.println("        For each missing number you can provide a zero (0) or a space ( ).");
		System.out.println("        Example for file Sudoku.0002:");
		System.out.println("            ROW[0] = 53  7");
		System.out.println("            ROW[1] = 6  195");
		System.out.println("            ROW[2] =  98    6");
		System.out.println("            ROW[3] = 8   6   3");
		System.out.println("            ROW[4] = 4  8 3  1");
		System.out.println("            ROW[5] = 7   2   6");
		System.out.println("            ROW[6] =  6    28");
		System.out.println("            ROW[7] =    419  5");
		System.out.println("            ROW[8] =     8  79");
		System.out.println("    -m or --method:");
		System.out.println("        Calls the equivalent method for solving the Sudoku.");
		System.out.println("        By default all methods are called.");
		System.out.println("        Available methods:");
		for(Method method: Method.values())
			if(method.active)
				System.out.println("            " + method);
		System.out.println("    -s or --solve:");
		System.out.println("        Solves the Sudoku by using all possible methods.");
		System.out.println("    -v or --verbose:");
		System.out.println("        Displays the Sudoku matrix on every passage.");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("    java Sudoku --help");
		System.out.println("    java Sudoku --interactive");
		System.out.println("    java Sudoku --coordinate SUDOKU < Sudoku.0002");
		System.out.println("    java Sudoku --analyze < Sudoku.0002");
		System.out.println("    java Sudoku --solve < Sudoku.0002");
		System.out.println("    java Sudoku -s --verbose < Sudoku.0002");
		System.out.println("    java Sudoku -s --go 41 --debug < Sudoku.0006");
		System.out.println("    java Sudoku -s --method NAKED_SINGLE < Sudoku.0000");
		System.out.println("    java Sudoku -s -m FULL_HOUSE -m NAKED_SINGLE < Sudoku.0001");
	}

	public static void main(String[] args) {
		Sudoku sudoku = new Sudoku();
		String value = null;
		if(args.length > 0)
			for(String argument: args) {
				if(value == null) {
					if(argument.equals("-a") || argument.equals("--analyze"))
						Matrix.setAnalyze(true);
					else if(argument.equals("-c") || argument.equals("--coordinate"))
						value = "-c";
					else if(argument.equals("-d") || argument.equals("--debug"))
						Debug.set(true);
					else if(argument.equals("-g") || argument.equals("--go"))
						value = "-g";
					else if(argument.equals("-h") || argument.equals("--help")) {
						sudoku.help();
						return;
					}
					else if(argument.equals("-i") || argument.equals("--interactive"))
						sudoku.setInteractive();
					else if(argument.equals("-m") || argument.equals("--method"))
						value = "-m";
					else if(argument.equals("-s") || argument.equals("--solve"))
						sudoku.setSolve();
					else if(argument.equals("-v") || argument.equals("--verbose"))
						sudoku.setVerbose();
					else {
						System.out.println("Unsupported flag: " + argument);
						return;
					}
				} else
					if(value.equals("-c")) {
						if(!Coordinate.setFormat(argument))
							return;
						value = null;
					} else if(value.equals("-g")) {
						if(!sudoku.setGo(argument))
							return;
						value = null;
					} else if(value.equals("-m")) {
						if(!sudoku.setActive(argument))
							return;
						value = null;
					}
			}
		sudoku.run();
	}
}
