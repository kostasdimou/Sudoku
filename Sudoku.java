import java.util.Scanner;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.Math;

class Sudoku {

	// ATTRIBUTES //

	private static boolean INTERACTIVE = false;
	private static boolean SOLVE = false;
	private static boolean VERBOSE = false;
	private long startTime = 0;
	private long endTime = 0;
	private ArrayList<InputStream> inputList = new ArrayList<InputStream>();
	private ArrayList<Method> methodList = new ArrayList<Method>();
	private ArrayList<Position> goList = new ArrayList<Position>();
	private Matrix matrix = null;

	// METHOD //

	public enum Method {
		// name     code  active
		FULL_HOUSE(    0, true),
		NAKED(        10, false), // dummy entry for grouping
		NAKED_SINGLE( 11, true),
		NAKED_PAIR(   12, true),
		NAKED_TRIPLE( 13, true),
		NAKED_QUAD(   14, true),
		NAKED_QUINT(  15, true),
		HIDDEN(       20, false), // dummy entry for grouping
		HIDDEN_SINGLE(21, true),
		HIDDEN_PAIR(  22, true),
		HIDDEN_TRIPLE(23, true),
		HIDDEN_QUAD(  24, true),
		HIDDEN_QUINT( 25, true);

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

	// Defines the methods we want to use for the solution.
	boolean setMethod(String name) {
		boolean found = false;
		for(Method method: Method.values())
			if(name.equals(method.name())) {
				if(!method.active) {
					System.out.println("Inactive method: " + name);
					return false;
				}
				if(!methodList.contains(method))
					methodList.add(method);
				found = true;
			}
		if(!found)
			System.out.println("Unsupported method: " + name);
		return found;
	}

	// POSITIONS //

	// Stores the points and numbers combinations we want to examine during solution.
	boolean setPosition(String yx) {
		int max = Position.getMax();
		int widthY = Coordinate.widthY(max);
		int widthX = Coordinate.widthX(max);
		if(yx.length() < widthY + widthX) {
			System.out.println("Too short coordinates (" + (widthY + widthX) + "): " + yx.length());
			return false;
		}
		yx = yx.toUpperCase();
		String y = yx.substring(0, widthY);
		yx = yx.substring(widthY);
		String x = yx.substring(0, widthX);
		if(!Coordinate.validY(y, max) || !Coordinate.validX(x, max)) {
			System.out.println("Incompatible coordinates: " + yx);
			return false;
		}
		Position position = new Position(Coordinate.readY(y, max), Coordinate.readX(x));
		goList.add(position);
		return true;
	}

	// INTERACTIVE //

	// Activates the INTERACTIVE mode.
	// The user is prompt to provide the numbers and blanks for populating the matrix.
	void setInteractive() {
		INTERACTIVE = true;
	}

	// INPUT_LIST //

	// Defines the files we want to solve.
	boolean setFile(String name) {
		File file = new File(name);
		if(!file.exists()) {
			System.out.println("File does not exist: " + name);
			return false;
		}
		try {
			InputStream input = new FileInputStream(file);
			inputList.add(input);
		} catch(FileNotFoundException e) {
			System.out.println(e);
			return false;
		}
		return true;
	}

	// MAX //

	// Defines the MAX width of the Sudoku matrix.
	boolean setMax(String width) {
		int max = 0;
		try {
			max = Integer.parseInt(width);
		} catch(NumberFormatException e) {
			System.out.println(e);
			return false;
		}
		double root = Math.sqrt(max);
		if((int)root != root) {
			System.out.println("Invalid width: " + width);
			return false;
		}
		Position.setMax(max);
		return true;
	}

	// VERBOSE //

	// Activates the VERBOSE mode.
	// VERBOSE mode displays the actions and the matrix for each solution passage.
	void setVerbose() {
		VERBOSE = true;
	}

	// PAUSE //

	void pause() {
		Scanner in = new Scanner(System.in);
		String x = in.nextLine();
	}

	// SOLVE //

	// Activates the SOLVE mode.
	void setSolve() {
		SOLVE = true;
	}

	// STRATEGIES //

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
	void setFullHouse(Position position, int depth) {
		Debug.log("setFullHouse(position = " + position + ")", depth);
		depth++;
		Cell cell = matrix.getCell(position, depth);
		if(!cell.isEmpty())
			return;
		if(VERBOSE)
			cell.println();
		int max = Position.getMax();
		for(int missing = 1; missing <= max; missing++) {
			if((matrix.count(Area.BLOCK, position, missing, depth) == max - 1) ||
			  (matrix.count(Area.HORIZONTAL, position, missing, depth) == max - 1) ||
			  (matrix.count(Area.VERTICAL, position, missing, depth) == max - 1)) {
				matrix.setNumber(position, missing, depth);
				if(VERBOSE)
					System.out.println(Method.FULL_HOUSE.name() + ":" +
						" missing Number(" + missing +")" +
						" at Position" + position);
				break;
			}
		}
	}

	void setAllFullHouses(int depth) {
		Debug.log("setAllFullHouses()", depth);
		depth++;
		for(Position position = new Position(0, 0); position != null; position = position.forward(Area.ALL))
			setFullHouse(position, depth);
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
	void setNakedSingle(Position position, int depth) {
		Debug.log("setNakedSingle(position = " + position + ")", depth);
		depth++;
		Cell cell = matrix.getCell(position, depth);
		if(!cell.isEmpty())
			return;
		if(VERBOSE)
			cell.println();
		if(cell.countCandidates() == 1) {
			int single = cell.getCandidates().get(0);
			matrix.setNumber(position, single, depth);
			if(VERBOSE)
				System.out.println(Method.NAKED_SINGLE.name() + ":" +
					" single Number(" + single +")" +
					" at Position" + position);
		}
	}

	void setAllNakedSingles(int depth) {
		Debug.log("setAllNakedSingles()", depth);
		depth++;
		for(Position position = new Position(0, 0); position != null; position = position.forward(Area.ALL))
			setNakedSingle(position, depth);
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
	void setNakedSubset(Position position, int limit, int depth) {
		Debug.log("setNakedSubset(position = " + position + ", limit = " + limit + ")", depth);
		depth++;
		Cell cell = matrix.getCell(position, depth);
		if(!cell.isEmpty())
			return;
		if(VERBOSE)
			cell.println();
		ArrayList<Integer> candidates = new ArrayList<>(cell.getCandidates());
		if(candidates.size() != limit)
			return;
		for(Area area: new ArrayList<Area>() {{add(Area.HORIZONTAL); add(Area.VERTICAL); add(Area.BLOCK);}}) {
			ArrayList<Position> matches = matrix.cleanMatchCandidates(area, position, candidates, 2, depth);
			if(matches.size() != limit)
				continue;
			if(VERBOSE)
				System.out.println(Method.valueOfCode(Method.NAKED.code + limit).name() + ":" +
					" found Numbers" + candidates + " at Positions" + matches);
			for(Integer candidate: candidates) {
				ArrayList<Position> removed = matrix.removeCandidateIf(area, position, candidate, matches, depth);
				if((removed.size() > 0) && VERBOSE)
					System.out.println("  Removed Candidate[" + candidate + "] from Positions" + removed);
			}
		}
	}

	void setAllNakedSubsets(int limit, int depth) {
		Debug.log("setAllNakedSubsets(limit = " + limit + ")", depth);
		depth++;
		for(Position position = new Position(0, 0); position != null; position = position.forward(Area.ALL))
			setNakedSubset(position, limit, depth);
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
	void setHiddenSingle(Position position, int depth) {
		Debug.log("setHiddenSingle(position = " + position + ")", depth);
		depth++;
		Cell cell = matrix.getCell(position, depth);
		if(!cell.isEmpty())
			return;
		if(VERBOSE)
			cell.println();
		int max = Position.getMax();
		ArrayList<Position> exceptions = new ArrayList<Position>();
		for(int hidden = 1; hidden <= max; hidden++)
			if(matrix.existsCandidate(position, hidden, depth))
				for(Area area: new ArrayList<Area>() {{add(Area.HORIZONTAL); add(Area.VERTICAL); add(Area.BLOCK);}})
			  		if(matrix.countCandidateIf(area, position, hidden, exceptions, depth) == 1) {
						matrix.setNumber(position, hidden, depth);
						if(VERBOSE)
							System.out.println(Method.HIDDEN_SINGLE.name() + ":" +
								" single Number(" + hidden +")" +
								" at Position" + position);
						break;
					}
	}

	void setAllHiddenSingles(int depth) {
		Debug.log("setAllHiddenSingles()", depth);
		depth++;
		for(Position position = new Position(0, 0); position != null; position = position.forward(Area.ALL))
			setHiddenSingle(position, depth);
	}

	// Hidden Pair aka Hidden Twins
	// If you can find two cells within a house such as that two candidates appear
	// nowhere outside those cells in that house, those two candidates must be placed
	// in the two cells. All other candidates can therefore be eliminated.
	//
	void setHiddenSubset(Position position, int limit, int depth) {
		Debug.log("setHiddenSubset(position = " + position + ", limit = " + limit + ")", depth);
		depth++;
		Cell cell = matrix.getCell(position, depth);
		if(!cell.isEmpty())
			return;
		if(VERBOSE)
			cell.println();
		ArrayList<Integer> candidates = new ArrayList<>(cell.getCandidates());
		if(candidates.size() < limit)
			return;
		ArrayList<Position> exceptions = new ArrayList<Position>();
		for(Area area: new ArrayList<Area>() {{add(Area.HORIZONTAL); add(Area.VERTICAL); add(Area.BLOCK);}}) {
			ArrayList<Position> matches = matrix.dirtyMatchCandidates(area, position, candidates, 2, exceptions, depth);
			if(matches.size() == limit) {
				ArrayList<Integer> hidden = new ArrayList<Integer>();
				for(Integer candidate: candidates)
					if(matrix.countCandidateIf(area, position, candidate, matches, depth) == 0)
						hidden.add(candidate);
				if(hidden.size() != limit)
					continue;
				if(VERBOSE)
					System.out.println(Method.valueOfCode(Method.HIDDEN.code + limit).name() + ":" +
						" found Numbers" + hidden + " at Positions" + matches);
				for(Integer candidate: cell.getCandidates()) {
					ArrayList<Position> removed = new ArrayList<Position>();
					for(Position match: matches) {
						cell = matrix.getCell(match, depth);
						if(!hidden.contains(candidate)) {
							cell.removeCandidate(candidate);
							removed.add(match);
						}
					}
					if((removed.size() > 0) && VERBOSE)
						System.out.println("    Removed Candidate[" + candidate + "] from Positions" + removed);
				}
			}
		}
	}

	void setAllHiddenSubsets(int limit, int depth) {
		Debug.log("setAllHiddenSubsets(limit = " + limit + ")", depth);
		depth++;
		for(Position position = new Position(0, 0); position != null; position = position.forward(Area.ALL))
			setHiddenSubset(position, limit, depth);
	}

	void strategies(int depth) {
		Debug.log("strategies()", depth);
		depth++;
		startTime = System.nanoTime();
		// If none method is defined then define all active ones
		if(methodList.size() == 0)
			for(Method method: Method.values())
				if(method.active)
					methodList.add(method);
		int passages = 0;
		if(goList.size() > 0) {
			int numbers = matrix.countAllNumbers();
			int candidates = matrix.countAllCandidates();
			for(Position position: goList) {
				// REMOVERS
				for(Method method: new ArrayList<Method>()
					{{add(Method.NAKED_PAIR); add(Method.NAKED_TRIPLE); add(Method.NAKED_QUAD); add(Method.NAKED_QUINT);}})
					if(methodList.contains(method))
						setNakedSubset(position, method.code - Method.NAKED.code, depth);
				for(Method method: new ArrayList<Method>()
					{{add(Method.NAKED_PAIR); add(Method.NAKED_TRIPLE); add(Method.NAKED_QUAD); add(Method.NAKED_QUINT);}})
					if(methodList.contains(method))
						setHiddenSubset(position, method.code - Method.HIDDEN.code, depth);
				// SETTERS
				if(methodList.contains(Method.FULL_HOUSE))
					setFullHouse(position, depth);
				if(methodList.contains(Method.NAKED_SINGLE))
					setNakedSingle(position, depth);
				if(methodList.contains(Method.HIDDEN_SINGLE))
					setHiddenSingle(position, depth);
				if(((numbers < matrix.countAllNumbers()) || (candidates > matrix.countAllCandidates())) && VERBOSE)
					matrix.print();
			}
		} else {
			int numbers = 0;
			int candidates = 0;
			int totalNumbers = Position.getTotalNumbers();
			do {
				passages++;
				if(VERBOSE)
					System.out.println("PASSAGE = " + passages);
				numbers = matrix.countAllNumbers();
				candidates = matrix.countAllCandidates();
				// REMOVERS
				for(Method method: new ArrayList<Method>()
					{{add(Method.NAKED_PAIR); add(Method.NAKED_TRIPLE); add(Method.NAKED_QUAD); add(Method.NAKED_QUINT);}})
					if(methodList.contains(method))
						setAllNakedSubsets(method.code - Method.NAKED.code, depth);
				for(Method method: new ArrayList<Method>()
					{{add(Method.NAKED_PAIR); add(Method.NAKED_TRIPLE); add(Method.NAKED_QUAD); add(Method.NAKED_QUINT);}})
					if(methodList.contains(method))
						setAllHiddenSubsets(method.code - Method.HIDDEN.code, depth);
				// SETTERS
				if(methodList.contains(Method.FULL_HOUSE))
					setAllFullHouses(depth);
				if(methodList.contains(Method.NAKED_SINGLE))
					setAllNakedSingles(depth);
				if(methodList.contains(Method.HIDDEN_SINGLE))
					setAllHiddenSingles(depth);
				if(((numbers < matrix.countAllNumbers()) || (candidates > matrix.countAllCandidates())) && VERBOSE) {
					matrix.print();
					matrix.verifyAll(depth);
				}
				if(INTERACTIVE) {
					try {
						pause();
					} catch(Exception e) {
						System.out.println(e);
					}
				}
			} while((totalNumbers > matrix.countAllNumbers()) &&
				((numbers < matrix.countAllNumbers()) || (candidates > matrix.countAllCandidates())));
		}
		endTime = System.nanoTime();
		if(!VERBOSE) {
			matrix.print();
			matrix.verifyAll(depth);
		}
		statistics(passages);
	}

	// STATISTICS //

	void statistics(int passages) {
		System.out.println();
		System.out.println(matrix.diluted("STATISTICS"));
		System.out.println();
		System.out.println("passages   = " + passages);
		System.out.println("numbers    = " + matrix.countAllNumbers());
		System.out.println("candidates = " + matrix.countAllCandidates());
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
		System.out.println("duration   = " + String.format(decimal, duration) + " " + unit);
	}

	// HELP //

	void help() {
		System.out.println("Sudoku.java by Kostas Dimou @ 2019");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("    Sudoku [--analyze] [--coordinates FORMAT] [--debug] [--file INPUT_FILE] \\");
		System.out.println("    Sudoku [--go YX] [--help] [--interactive] [--method METHOD] [--solve] \\");
		System.out.println("           [--verbose] [--width WIDTH]");
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
		System.out.println("    -f or --file:");
		System.out.println("        Defines an external file for the initial population.");
		System.out.println("        Example for reading file Sudoku.9x9.0002:");
		System.out.println("            -f Sudoku.9x9.0002");
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
		System.out.println("        Example for file Sudoku.9x9.0002:");
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
		System.out.println("        Activates a method for solving the Sudoku.");
		System.out.println("        By default all methods are active.");
		System.out.println("        Available methods:");
		for(Method method: Method.values())
			if(method.active)
				System.out.println("            " + method);
		System.out.println("        Example for activating the method NAKED_SINGLE:");
		System.out.println("            -m NAKED_SINGLE");
		System.out.println("    -s or --solve:");
		System.out.println("        Solves the Sudoku by using all possible methods.");
		System.out.println("    -v or --verbose:");
		System.out.println("        Displays the Sudoku matrix on every passage.");
		System.out.println("    -w or --width:");
		System.out.println("        Defines the width of the Sudoku matrix.");
		System.out.println("        The accepted numbers should have an integer root (e.g. 4, 9, 16, ...).");
		System.out.println("        The default width is 9.");
		System.out.println("        Example for setting the width equal to 16:");
		System.out.println("            -w 16");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("    java Sudoku --help");
		System.out.println("    java Sudoku --interactive");
		System.out.println("    java Sudoku --coordinate SUDOKU < Sudoku.9x9.0002");
		System.out.println("    java Sudoku --analyze < Sudoku.9x9.0002");
		System.out.println("    java Sudoku --solve < Sudoku.9x9.0002");
		System.out.println("    java Sudoku -s --verbose < Sudoku.9x9.0002");
		System.out.println("    java Sudoku -s --go 41 --debug < Sudoku.9x9.0006");
		System.out.println("    java Sudoku -s --method NAKED_SINGLE < Sudoku.9x9.0000");
		System.out.println("    java Sudoku -s -m FULL_HOUSE -m NAKED_SINGLE < Sudoku.9x9.0001");
	}

	// READ_NUMBERS //

	boolean readNumbers(InputStream input, int depth) {
		Debug.log("readNumbers()", depth);
		depth++;
		Scanner scan = new Scanner(input);
		String row = null;
		int max = Position.getMax();
		int width = Coordinate.digitsMax(max);
		for(int y = 0; y < max; y++) {
			if(INTERACTIVE)
				System.out.print("ROW[" + y + "] = ");
			try {
				row = scan.nextLine();
			} catch(NoSuchElementException e) {
				row = null; // empty line
			} catch(Exception e) {
				System.out.println(e);
				return false;
			}
			for(Position position = new Position(y, 0); position != null; position = position.forward(Area.HORIZONTAL))
				if(row == null)
					matrix.setNumber(position, 0, depth);
				else
					if(row.length() < width)
						matrix.setNumber(position, 0, depth);
					else {
						String cell = row.substring(0, width).trim();
						if(cell.equals(""))
							matrix.setNumber(position, 0, depth);
						else {
							int number = 0;
							try {
								number = Integer.parseInt(cell);
							} catch(Exception e) {
								System.out.println(e);
								return false;
							}
							if(number > max) {
								System.out.println("Number out of range (1.." + max + ") at position " + position + ": " + number);
								return false;
							}
							matrix.setNumber(position, number, depth);
						}
						row = row.substring(width);
					}
		}
		matrix.print();
		return true;
	}

	// ARGUMENTS //

	boolean validateArguments() {
		if(inputList.size() == 0)
			inputList.add(System.in);
		if(matrix == null)
			matrix = new Matrix(Position.getMax());
		if(matrix == null)
			return false;
		return true;
	}

	boolean readArguments(String[] arguments) {
		String next = null;
		if(arguments.length > 0)
			for(String argument: arguments) {
				if(next == null) {
					if(argument.equals("-a") || argument.equals("--analyze"))
						Matrix.setAnalyze(true);
					else if(argument.equals("-c") || argument.equals("--coordinate"))
						next = "-c";
					else if(argument.equals("-d") || argument.equals("--debug"))
						Debug.set(true);
					else if(argument.equals("-f") || argument.equals("--file"))
						next = "-f";
					else if(argument.equals("-g") || argument.equals("--go"))
						next = "-g";
					else if(argument.equals("-h") || argument.equals("--help")) {
						help();
						return false;
					}
					else if(argument.equals("-i") || argument.equals("--interactive"))
						setInteractive();
					else if(argument.equals("-m") || argument.equals("--method"))
						next = "-m";
					else if(argument.equals("-s") || argument.equals("--solve"))
						setSolve();
					else if(argument.equals("-v") || argument.equals("--verbose"))
						setVerbose();
					else if(argument.equals("-w") || argument.equals("--width"))
						next = "-w";
					else {
						System.out.println("Unsupported flag: " + argument);
						return false;
					}
				} else
					if(next.equals("-c")) {
						if(!Coordinate.setFormat(argument))
							return false;
						next = null;
					} else if(next.equals("-f")) {
						if(!setFile(argument))
							return false;
						next = null;
					} else if(next.equals("-g")) {
						if(!setPosition(argument))
							return false;
						next = null;
					} else if(next.equals("-m")) {
						if(!setMethod(argument))
							return false;
						next = null;
					} else if(next.equals("-w")) {
						if(!setMax(argument))
							return false;
						next = null;
					}
			}
		return true;
	}

	// SOLVE //

	void solve() {
		System.out.println();
		System.out.println(Matrix.diluted("SUDOKU"));
		if(INTERACTIVE)
			System.out.println();
		int depth = 0;
		for(InputStream input: inputList)
			if(readNumbers(input, depth))
				if(matrix.verifyAll(depth) && SOLVE)
					strategies(depth);
	}

	// MAIN //

	public static void main(String[] arguments) {
		Sudoku sudoku = new Sudoku();
		if(sudoku.readArguments(arguments))
			if(sudoku.validateArguments())
				sudoku.solve();
	}
}
