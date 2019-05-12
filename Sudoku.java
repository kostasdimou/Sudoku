import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

class Sudoku extends Print {
    private static final int MAX = 9;
    private static final int TOTAL = MAX * MAX;

	private static boolean interactive = false;
	private static boolean solution = false;
	private static boolean verbose = false;

    private int passages = 0;
    private long startTime = 0;
    private long endTime = 0;
	private static final Coordinate coordinate = Coordinate.getInstance();
	private ArrayList<Method> methodList = new ArrayList<Method>();
	private ArrayList<Position> goList = new ArrayList<Position>();
    private Scanner in = new Scanner(System.in);

	public enum Method {
		FULL_HOUSE,
		NAKED_SINGLE,
		HIDDEN_SINGLE,
		NAKED_PAIR
	};

	private Matrix matrix = new Matrix(MAX);

	// Stores the points and numbers combinations we want to examine during solution.
    boolean setGo(String go) {
		go = go.toUpperCase();
		int length = go.length();
		int middle = length / 2;
		String y = go.substring(0, middle);
		String x = go.substring(middle, length);
		int max = Position.getMax();
		if(!coordinate.validY(y, max) || !coordinate.validX(x, max)) {
			System.out.println("Incompatible coordinates: " + go);
			return false;
		}
		Position p = new Position(coordinate.readY(y, max), coordinate.readX(x));
		goList.add(p);
		return true;
	}

	// Activates the interactive mode.
	// The user is prompt to provide the numbers and blanks for populating the matrix.
    void setInteractive() {
		interactive = true;
	}

	// Stores the methods we want to utilize for the solution.
    boolean setActive(String methodName) {
		boolean found = false;
		for(Method method: Method.values())
			if(methodName.equals(method.name())) {
				if(!methodList.contains(method))
					methodList.add(method);
				found = true;
			}
		if(!found)
			System.out.println("Unsupported method: " + methodName);
		return found;
	}

	// Activates the solution mode.
    void setSolution() {
		solution = true;
	}

	// Activates the verbose mode.
	// Displays the actions and the matrix for each solution assage.
    void setVerbose() {
		verbose = true;
	}

    void run() {
		System.out.println();
		System.out.println(diluted("SUDOKU"));
		if(interactive)
			System.out.println();
        String row = null;
		Position p = new Position(0, 0);
        for(int y = 0; y < Position.getMax(); y++) {
            if(interactive)
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
            for(int x = 0; x < Position.getMax(); x++, p.forward(Area.HORIZONTAL))
                if(x < length)
                    if(row.charAt(x) == SPACE)
                        matrix.setNumber(p, 0);
                    else
                        matrix.setNumber(p, row.charAt(x) - Coordinate.ZERO);
                else
					matrix.setNumber(p, 0);
        }
		matrix.print();
		if(solution)
			solve();
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
    int setFullHouse(int y, int x, int depth) {
		log("setFullHouse(y = " + y + ", x = " + x + ")", depth++);
		int added = 0;
		if(matrix.empty(y, x)) {
			Position p = new Position(y, x);
			if(verbose)
				System.out.println(matrix.cell(p));
			int max = Position.getMax();
			for(int missing = 1; missing <= max; missing++) {
				if((matrix.count(Area.BLOCK, y, x, missing, depth) == max - 1) ||
				  (matrix.count(Area.HORIZONTAL, y, x, missing, depth) == max - 1) ||
				  (matrix.count(Area.VERTICAL, y, x, missing, depth) == max - 1)) {
					added += matrix.setNumber(p, missing);
					if(verbose)
						System.out.println(" >> " + Method.FULL_HOUSE.name() + " >> " + missing);
					break;
				}
			}
		}
		return added;
    }

    int setAllFullHouses(int depth) {
		log("setAllFullHouses()", depth++);
		int added = 0;
		Position p = new Position(0, 0);
		int max = Position.getMax();
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, p.forward(Area.HORIZONTAL))
				added += setFullHouse(y, x, depth);
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
    int setNakedSingle(int y, int x, int depth) {
		log("setNakedSingle(y = " + y + ", x = " + x + ")", depth++);
		int added = 0;
		if(matrix.empty(y, x)) {
			Position p = new Position(y, x);
			if(verbose)
				System.out.println(matrix.cell(p));
			if(matrix.candidateCount(y, x) == 1) {
				int single = matrix.getFirstCandidate(y, x);
				added += matrix.setNumber(p, single);
				if(verbose)
					System.out.println(" >> " + Method.NAKED_SINGLE.name() + " >> " + single);
			}
		}
		return added;
    }

    int setAllNakedSingles(int depth) {
		log("setAllNakedSingles()", depth++);
		int added = 0;
		Position p = new Position(0, 0);
		int max = Position.getMax();
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, p.forward(Area.HORIZONTAL))
				added += setNakedSingle(y, x, depth);
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
	int setHiddenSingle(int y, int x, int depth) {
		log("setHiddenSingle(y = " + y + ", x = " + x + ")", depth++);
		int added = 0;
		if(matrix.empty(y, x)) {
			Position p = new Position(y, x);
			if(verbose)
				System.out.println(matrix.cell(p));
			int max = Position.getMax();
			for(int hidden = 1; hidden <= max; hidden++) {
				if((matrix.candidateCountIf(Area.BLOCK, y, x, hidden, depth) == 1) ||
				  (matrix.candidateCountIf(Area.HORIZONTAL, y, x, hidden, depth) == 1) ||
				  (matrix.candidateCountIf(Area.VERTICAL, y, x, hidden, depth) == 1)) {
					added += matrix.setNumber(p, hidden);
					if(verbose)
						System.out.println(" >> " + Method.HIDDEN_SINGLE.name() + " >> " + hidden);
					break;
				}
			}
		}
		return added;
	}

    int setAllHiddenSingles(int depth) {
		log("setAllHiddenSingles()", depth++);
		int added = 0;
		Position p = new Position(0, 0);
		int max = Position.getMax();
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, p.forward(Area.HORIZONTAL))
				added += setHiddenSingle(y, x, depth);
		return added;
    }

	/*
    ArrayList<Pair> pairsArea(Area a, int row, int column, int depth) {
		log("pairsArea(area = " + a + ", row = " + row + ", column = " + column + ")", depth++);
		ArrayList<Pair> pairList = new ArrayList<Pair>();
		switch(a) {
			case ALL:
				for(int y = 0; y < Position.getMax(); y++)
					for(int x = 0; x < Position.getMax(); x++) {
						Position point = new Position(y, x);
						if(countArea(Area.NUMBER, y, x, depth) == 2) {
							Pair pair = new Pair();
							for(int number = 1; number <= Position.getMax(); number++)
								if(s[y][x][number] == number) {
									Cell cell = new Cell(point, number);
									pair.cellList.add(cell);
								}
							pairList.add(pair);
						}
					}
				break;
			case BLOCK:
				for(int i = 0, y = blockBase(row); i < Position.getMin(); i++, y++)
					for(int j = 0, x = blockBase(column); j < Position.getMin(); j++, x++) {
						Position point = new Position(y, x);
						if(countArea(Area.NUMBER, y, x, depth) == 2) {
							Pair pair = new Pair();
							for(int number = 1; number <= Position.getMax(); number++)
								if(s[y][x][number] == number) {
									Cell cell = new Cell(point, number);
									pair.cellList.add(cell);
								}
							pairList.add(pair);
						}
					}
				break;
			case HORIZONTAL:
				for(int x = 0; x < Position.getMax(); x++) {
					Position point = new Position(row, x);
					if(countArea(Area.NUMBER, row, x, depth) == 2) {
						Pair pair = new Pair();
						for(int number = 1; number <= Position.getMax(); number++)
							if(s[row][x][number] == number) {
								Cell cell = new Cell(point, number);
								pair.cellList.add(cell);
							}
						pairList.add(pair);
					}
				}
				break;
			case VERTICAL:
				for(int y = 0; y < Position.getMax(); y++) {
					Position point = new Position(y, column);
					if(countArea(Area.NUMBER, y, column, depth) == 2) {
						Pair pair = new Pair();
						for(int number = 1; number <= Position.getMax(); number++)
							if(s[y][column][number] == number) {
								Cell cell = new Cell(point, number);
								pair.cellList.add(cell);
							}
						pairList.add(pair);
					}
				}
				break;
		}
		return pairList;
	}

	int twinPairs(ArrayList<Pair> pairList) {
	}

    // Check for multiple naked pairs per area and unset the candidates.
	// 
	//     B  B  B                                                      V
	//   +--+--+--|--+--+--|--+--+--+   +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// B |13| 6|12|  |  |  |  |  |  | H | 6|12| 7| 8|12|13|95|24|49|  |13|  |  |  |  |  |  |  |  |
	//   +--+--+--|--+--+--|--+--+--+   +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// B |94|12| 7|  |  |  |  |  |  |   |  |  |  |  |  |  |  |  |  |  |12|  |  |  |  |  |  |  |  |
	//   +--+--+--|--+--+--|--+--+--+   +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// B |49| 8|24|  |  |  |  |  |  |   |  |  |  |  |  |  |  |  |  |  |12|  |  |  |  |  |  |  |  |
	//   +--+--+--|--+--+--|--+--+--+   +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	//    Example Block                  Example Horizontal            Example Vertical
    int setNakedPair(int row, int column, int number, int depth) {
		log("setNakedPair(row = " + row + ", column = " + column + ", number = " + number + ")", depth++);
		int added = 0;
		ArrayList<Pair> pairList = pairsArea(Area.BLOCK, row, column, depth);
		int pairs = pairList.size();
		var("pairs", pairs, depth);
		for(int p = 0; p < pairs; p++) {
			int first = pairList.get(p).value();
			for(int q = p; q < pairs; q++) {
				int second = pairList.get(q).value();
				if(first == second) {
					ArrayList<Cell> cellList = matrix.horizontal(row);
					cellList.removeCandidate(matrix.cells(first.point));
					cellList.removeCandidate(matrix.cells(second.point));
					matrix.removeCandidate(int number, cellList);
				}
			}
		}
		pairList = pairsArea(Area.HORIZONTAL, row, column, depth);
		var("pairs", pairList.size(, depth));
		pairList = pairsArea(Area.VERTICAL, row, column, depth);
		var("pairs", pairList.size(), depth);
		// added += setNumber(row, column, number);
		var("added", added, depth);
		if(verbose && (added > 0))
			System.out.println(" >> " + Method.XXXXXXXXXXXX.name() + " >> " + number);
		return added;
    }
	*/

    void solve() {
		int depth = 0;
		log("solve()", depth++);
        startTime = System.nanoTime();
		if(methodList.size() == 0)
			for(Method method: Method.values())
				methodList.add(method);
        int added = TOTAL;
		if(goList.size() > 0)
			for(Position p: goList) {
				added = 0;
				if(methodList.contains(Method.FULL_HOUSE))
					added += setFullHouse(p.getY(), p.getX(), depth);
				if(methodList.contains(Method.NAKED_SINGLE))
					added += setNakedSingle(p.getY(), p.getX(), depth);
				if(methodList.contains(Method.HIDDEN_SINGLE))
					added += setHiddenSingle(p.getY(), p.getX(), depth);
				/*
				if(methodList.contains(Method.NAKED_PAIR))
					added += setNakedPair(p.getY(), p.getX(), number, depth);
				*/
				var("added", added, depth);
				if((added > 0) && verbose)
					matrix.print();
			}
		else {
			int counter = matrix.countAll();
			while((counter < TOTAL) && (added > 0)) {
				passages++;
				added = 0;
				if(methodList.contains(Method.FULL_HOUSE))
					added += setAllFullHouses(depth);
				if(methodList.contains(Method.NAKED_SINGLE))
					added += setAllNakedSingles(depth);
				if(methodList.contains(Method.HIDDEN_SINGLE))
					added += setAllHiddenSingles(depth);
				/*
				if(methodList.contains(Method.NAKED_PAIR))
					added += setAllNakedPair(y, x, number, depth);
				*/
				var("added", added, depth);
				if((added > 0) && verbose)
					matrix.print();
				if(interactive) {
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
        if(!verbose)
            matrix.print();
        statistics();
    }

    void statistics() {
		System.out.println();
		System.out.println(diluted("STATISTICS"));
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
		System.out.println("    Sudoku [--analyze] [--coordinates SYSTEM] [--debug] [--go YXN] [--help] \\");
		System.out.println("           [--interactive] [--method METHOD] [--solve] [--verbose]");
		System.out.println();
		System.out.println("Where:");
		System.out.println("    -a or --analyze:");
		System.out.println("        Displays the posible positions for each number.");
		System.out.println("    -c or --coordinates:");
		System.out.println("        Selects the coordinates system for the Sudoku matrix.");
		System.out.println("        Available systems:");
		System.out.println("            CHESS:  Y=9-1, X=A-I");
		System.out.println("            JAVA:   Y=0-9, X=0-9 (default)");
		System.out.println("            SUDOKU: Y=A-I, X=1-9");
		System.out.println("    -d or --debug:");
		System.out.println("        Activates the debugging mode.");
		System.out.println("    -g or --go:");
		System.out.println("        Check a specific coordinate.");
		System.out.println("        Example:");
		System.out.println("            -c CHESS  -g 5A: row = 4, column = 1.");
		System.out.println("            -c JAVA   -g 40: row = 4, column = 1.");
		System.out.println("            -c SUDOKU -g D1: row = 4, column = 1.");
		System.out.println("    -h or --help:");
		System.out.println("        Displays this help message and exits.");
		System.out.println("    -i or --interactive:");
		System.out.println("        Displays a prompt for each imput row and pauses on each passage.");
		System.out.println("        For each missing number you can provide a zero (0) or a space ( ).");
		System.out.println("        Example:");
		System.out.println("            ROW[0] = 57 9  1");
		System.out.println("            ROW[1] = 01030005");
		System.out.println("            ROW[3] =  2070 0 9");
		System.out.println("    -m or --method:");
		System.out.println("        Calls the equivalent method for solving the Sudoku.");
		System.out.println("        By default all methods are called.");
		System.out.println("        Available methods:");
		System.out.println("            FULL_HOUSE");
		System.out.println("            HIDDEN_SINGLE");
		System.out.println("            NAKED_PAIR");
		System.out.println("            NAKED_SINGLE");
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
		System.out.println("    java Sudoku -s -m NAKED_SINGLE -m NAKED_PAIR < Sudoku.0001");
	}

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
        String value = null;
        if(args.length > 0)
			for(String argument: args) {
				if(value == null) {
					if(argument.equals("-a") || argument.equals("--analyze"))
						Matrix.setAnalysis(true);
					else if(argument.equals("-c") || argument.equals("--coordinate"))
						value = "-c";
					else if(argument.equals("-d") || argument.equals("--debug"))
						Print.setDebug(true);
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
						sudoku.setSolution();
					else if(argument.equals("-v") || argument.equals("--verbose"))
						sudoku.setVerbose();
					else {
						System.out.println("Unsupported flag: " + argument);
						return;
					}
				} else
					if(value.equals("-c")) {
						if(!coordinate.setFormat(argument))
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
