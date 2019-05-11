import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

class Sudoku {
    private static final int MAX = 9;
    private static final int TOTAL = MAX * MAX;
    private static final int FACE = 0;
    private static final int QUIET = -1;
    private static final char DASH = '-';
    private static final char PIPE = '|';
    private static final char PLUS = '+';
    private static final char ZERO = '0';
    private static final char ALPHA = 'A';

	private static boolean analyze = false;
	private static boolean debug = false;
	private static boolean interactive = false;
	private static boolean solve = false;
	private static boolean view = false;

    private int passages = 0;
    private long startTime = 0;
    private long endTime = 0;
	private static final Coordinate coordinate = Coordinate.getInstance();
	private List<Method> methodList = new ArrayList<Method>();
	private List<String> goList = new ArrayList<String>();
    private Scanner in = new Scanner(System.in);

	public enum Method {
		NAKED_SINGLE,
		HIDDEN_SINGLE,
		MISSING,
		NAKED_PAIR
	};

	private Matrix matrix = new Matrix(MAX);

	// Activates the analysis mode.
    void setAnalyze() {
		this.analyze = true;
	}

	// Activates the debug mode.
    void setDebug() {
		this.debug = true;
	}

	// Stores the points and numbers combinations we want to examine during solution.
    boolean setGo(String go) {
		String message = "Incompatible coordinates: " + go;
		StringBuilder goUpper = new StringBuilder(go.toUpperCase());
		char y = goUpper.charAt(0);
		if((y >= ALPHA) && (coordinate.getFormat() != Coordinate.Format.SUDOKU)) {
			System.out.println(message);
			return false;
		}
		char x = goUpper.charAt(1);
		if((x >= ALPHA) && (coordinate.getFormat() != Coordinate.Format.CHESS)) {
			System.out.println(message);
			return false;
		}
		if(((y >= ZERO + MAX) || (x >= ZERO + MAX)) && (coordinate.getFormat() == Coordinate.Format.JAVA)) {
			System.out.println(message);
			return false;
		}
		goUpper.setCharAt(0, readY(y));
		goUpper.setCharAt(1, readX(x));
		goList.add(goUpper.toString());
		return true;
	}

	// Activates the interactive mode.
	// The user is prompt to provide the numbers and blanks for populating the matrix.
    void setInteractive() {
		this.interactive = true;
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
    void setSolve() {
		this.solve = true;
	}

	// Activates the view mode.
	// Displays the actions and the matrix for each solution assage.
    void setView() {
		this.view = true;
	}

	// Calculates the left margin spaces for displying the debug information.
	public static String margin(int depth) {
		int width = depth * 2;
		String result = EMPTY;
		for(int i = 0; i < width; i++)
			result += SPACE;
		return result;
	}

	// Displays debugging information.
	// Mostly used for the method calls.
	public static void log(int depth, String message) {
		if(debug)
			System.out.println(margin(depth) + message);
	}

	// Displays debugging information.
	// Used only for displaying the variables.
	public static void var(int depth, String name, Object value) {
		String message = name + " = " + value;
		log(depth, message);
	}

	// Returns a formated string with the coordinates.
	String coordinates(int y, int x) {
		return "[" + coordinateY(y) + "," + coordinateX(x) + "]";
	}

	// Converts the coordinate system row to the JAVA system.
	char readY(char y) {
		char map = SPACE;
		switch(coordinate) {
			case JAVA: // Y=0-9, X=0-9
				map = y;
				break;
			case SUDOKU: // Y=A-I, X=1-9
				map = y;
				map -= ALPHA - ZERO;
				break;
			case CHESS: // Y=9-1, X=A-I
				map = ZERO;
				map += MAX;
				map -= y;
				map += ZERO;
				break;
		}
		return map;
	}

	// Converts the coordinate system column to the JAVA system.
	char readX(char x) {
		char map = SPACE;
		switch(coordinate) {
			case JAVA: // Y=0-9, X=0-9
				map = x;
				break;
			case SUDOKU: // Y=A-I, X=1-9
				map = x;
				map--;
				break;
			case CHESS: // Y=9-1, X=A-I
				map = x;
				map -= ALPHA - ZERO;
				break;
		}
		return map;
	}

	int blockNextException(int i, int exception) {
		int base = blockBase(i);
		for(int next = 0; next < MAX; next += Position.getMin()) {
			if(next == base)
				continue;
			if(next == exception)
				continue;
			return next;
		}
		return NOT_FOUND;
	}

	int blockNext(int i) {
		return blockNextException(i, NOT_FOUND);
	}

	// Count the existing numbers omitting the exception
    int countAreaException(Area area, int row, int column, int exception, int depth) {
		if(depth != QUIET)
			log(depth++, "countAreaException(area = " + area + ", row = " + row + ", column = " + column + ", exception = " + exception + ")");
        int counter = 0;
        switch(area) {
            case ALL:
				for(int y = 0; y < Position.getMax(); y++)
					for(int x = 0; x < Position.getMax(); x++)
						if((s[y][x][FACE] != MISSING) && (s[y][x][FACE] != exception))
							counter++;
				break;
            case HORIZONTAL:
                for(int x = 0; x < Position.getMax(); x++)
                    if((s[row][x][FACE] != MISSING) && (s[row][x][FACE] != exception))
						counter++;
                break;
            case VERTICAL:
                for(int y = 0; y < Position.getMax(); y++)
                    if((s[y][column][FACE] != MISSING) && (s[y][column][FACE] != exception))
						counter++;
                break;
            case BLOCK:
                for(int i = 0, y = blockBase(row); i < Position.getMin(); i++, y++)
                    for(int j = 0, x = blockBase(column); j < Position.getMin(); j++, x++)
                        if((s[y][x][FACE] != MISSING) &&
                           (s[y][x][FACE] != exception))
							counter++;
				break;
            case BLOCK_HORIZONTAL:
				for(int j = 0, x = blockBase(column); j < Position.getMin(); j++, x++)
					if((s[row][x][FACE] != MISSING) &&
					   (s[row][x][FACE] != exception))
						counter++;
				break;
            case BLOCK_VERTICAL:
                for(int i = 0, y = blockBase(row); i < Position.getMin(); i++, y++)
					if((s[y][column][FACE] != MISSING) &&
					   (s[y][column][FACE] != exception))
						counter++;
				break;
            case NUMBER:
                for(int n = 1; n <= Position.getMax(); n++)
                    if((s[row][column][n] != MISSING) &&
                       (s[row][column][n] != exception))
						counter++;
                break;
		}
		if(depth != QUIET)
			var(depth, "counter", counter);
        return counter;
    }

	// Count without exception
    int countArea(Area area, int row, int column, int depth) {
		return countAreaException(area, row, column, MISSING, depth);
	}

	// Count all cells
    int count(Area area, int depth) {
		return countArea(area, 0, 0, depth);
	}

    int existsArea(Area area, int row, int column, int number, int depth) {
    	log(depth++, "existsArea(area = " + area + ", row = " + row + ", column = " + column + ", number = " + number + ")");
		int exists = NOT_FOUND;
		if(number != 0)
			switch(area) {
				case ALL:
					for(int y = 0; y < Position.getMax(); y++)
						for(int x = 0; x < Position.getMax(); x++)
							if(s[y][x][FACE] == number)
								exists = y;
					break;
				case BLOCK:
					for(int i = 0, y = blockBase(row); i < Position.getMin(); i++, y++)
						for(int j = 0, x = blockBase(column); j < Position.getMin(); j++, x++)
							if(s[y][x][FACE] == number)
								exists = y;
					break;
				case HORIZONTAL:
					for(int x = 0; x < Position.getMax(); x++)
						if(s[row][x][FACE] == number)
							exists = x;
					break;
				case VERTICAL:
					for(int y = 0; y < Position.getMax(); y++)
						if(s[y][column][FACE] == number)
							exists = y;
					break;
			}
		var(depth, "exists", exists);
        return exists;
    }

    boolean existsException(int row, int column, int number, int depth) {
    	log(depth++, "existsException(row = " + row + ", column = " + column + ", number = " + number + ")");
		boolean exists = false;
		if(number != MISSING)
			exists =
				(existsArea(Area.HORIZONTAL, row, column, number, depth) > NOT_FOUND) ||
				(existsArea(Area.VERTICAL, row, column, number, depth) > NOT_FOUND) ||
				(existsArea(Area.BLOCK, row, column, number, depth) > NOT_FOUND);
		var(depth, "exists", exists);
		return exists;
	}

    void printDash(char character) {
        System.out.print(EMPTY + DASH + character);
	}

    void printSpace(char character) {
        System.out.print(EMPTY + SPACE + character);
	}

    void printSpace(int number) {
        System.out.print(EMPTY + SPACE + number);
	}

    void printHorizontalLine(char prefix, boolean showCounter) {
        System.out.print(prefix);
        printSpace(PLUS);
        for(int y = 0; y < Position.getMin(); y++) {
            for(int x = 0; x < Position.getMin(); x++)
                printDash(DASH);
            printDash(PLUS);
        }
        if(showCounter)
            System.out.print(" (" + count(Area.ALL, QUIET) + ")");
        System.out.println();
    }

    void print() {
		System.out.print(SPACE);
		for(int x = 0; x < Position.getMax(); x++) {
			if(x % Position.getMin() == 0)
				printSpace(SPACE);
			printSpace(coordinateX(x));
		}
		System.out.println();
        for(int y = 0; y < Position.getMax(); y++) {
            if((y % Position.getMin()) == 0)
				printHorizontalLine(SPACE, false);
			System.out.print(coordinateY(y));
            for(int x = 0; x < Position.getMax(); x++) {
                if((x % Position.getMin()) == 0)
                    printSpace(PIPE);
                if(s[y][x][FACE] != MISSING)
                    printSpace(s[y][x][FACE]);
                else
                     printSpace(SPACE);
            }
            printSpace(PIPE);
			System.out.println();
        }
        printHorizontalLine(SPACE,true);
		if(analyze)
			analysis();
    }

	void populateMissingNumbers() {
		for(int y = 0; y < Position.getMax(); y++)
			for(int x = 0; x < Position.getMax(); x++) {
				for(int number = 1; number <= Position.getMax(); number++)
					s[y][x][number] = number;
			}
	}

    void run() {
		System.out.println();
		System.out.println(printSpace("SUDOKU"));
		System.out.println();
		populateMissingNumbers();
        String row;
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
            for(int x = 0; x < Position.getMax(); x++)
                if(x < length)
                    if(row.charAt(x) == SPACE)
                        setNumber(y, x, 0);
                    else
                        setNumber(y, x, row.charAt(x) - ZERO);
                else
					setNumber(y, x, 0);
        }
		print();
		if(solve)
			solution();
    }

    void unsetArea(Area area, int row, int column, int number) {
		if(number != MISSING)
			switch(area) {
				case ALL:
					for(int y = 0; y < Position.getMax(); y++)
						for(int x = 0; x < Position.getMax(); x++)
							if(s[y][x][number] != MISSING)
								s[y][x][number] = MISSING;
					break;
				case BLOCK:
					for(int i = 0, y = blockBase(row); i < Position.getMin(); i++, y++)
						for(int j = 0, x = blockBase(column); j < Position.getMin(); j++, x++)
							if(s[y][x][number] != MISSING)
								s[y][x][number] = MISSING;
					break;
				case HORIZONTAL:
					for(int x = 0; x < Position.getMax(); x++)
						if(s[row][x][number] != MISSING)
							s[row][x][number] = MISSING;
					break;
				case VERTICAL:
					for(int y = 0; y < Position.getMax(); y++)
						if(s[y][column][number] != MISSING)
							s[y][column][number] = MISSING;
					break;
				case NUMBER:
					for(int n = 1; n <= Position.getMax(); n++)
						if(s[row][column][n] != MISSING)
							s[row][column][n] = MISSING;
					break;
			}
	}

    // Set the last missing number when all other are present
	// 
	//     B  B  B                                                      V
	//   +--+--+--|--+--+--|--+--+--+   +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// B | ?| X| X|  |  |  |  |  |  | H | ?| X| X| X| X| X| X| X| X|  | ?|  |  |  |  |  |  |  |  |
	//   +--+--+--|--+--+--|--+--+--+   +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// B | X| X| X|  |  |  |  |  |  |   |  |  |  |  |  |  |  |  |  |  | X|  |  |  |  |  |  |  |  |
	//   +--+--+--|--+--+--|--+--+--+   +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// B | X| X| X|  |  |  |  |  |  |   |  |  |  |  |  |  |  |  |  |  | X|  |  |  |  |  |  |  |  |
	//   +--+--+--|--+--+--|--+--+--+   +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	//    Example Block                  Example Horizontal            Example Vertical
    int setNakedSingle(int row, int column, int number, int depth) {
		log(depth++, "setNakedSingle(row = " + row + ", column = " + column + ", number = " + number + ")");
		int added = 0;
		if((countAreaException(Area.BLOCK, row, column, number, depth) == Position.getMax() - 1) || // last one in-block
		   (countAreaException(Area.HORIZONTAL, row, column, number, depth) == Position.getMax() - 1) || // last one horizontally
		   (countAreaException(Area.VERTICAL, row, column, number, depth) == Position.getMax() - 1) || // last one vertically
		   (countAreaException(Area.NUMBER, row, column, number, depth) == 0)) // all others are not allowed here
			added += setNumber(row, column, number);
		var(depth, "added", added);
		if(view && (added > 0))
			System.out.println(Method.NAKED_SINGLE.name() + " >> " + number + " @ " + coordinates(row, column));
		return added;
    }

	/*
    List<Pair> pairsArea(Area area, int row, int column, int depth) {
		log(depth++, "pairsArea(area = " + area + ", row = " + row + ", column = " + column + ")");
		List<Pair> pairList = new ArrayList<Pair>();
		switch(area) {
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

	int twinPairs(List<Pair> pairList) {
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
		log(depth++, "setNakedPair(row = " + row + ", column = " + column + ", number = " + number + ")");
		int added = 0;
		List<Pair> pairList = pairsArea(Area.BLOCK, row, column, depth);
		int pairs = pairList.size();
		var(depth, "pairs", pairs);
		for(int p = 0; p < pairs; p++) {
			int first = pairList.get(p).value();
			for(int q = p; q < pairs; q++) {
				int second = pairList.get(q).value();
				if(first == second) {
					List<Cell> cellList = matrix.horizontal(row);
					cellList.removeCandidate(matrix.cells(first.point));
					cellList.removeCandidate(matrix.cells(second.point));
					matrix.removeCandidate(int number, cellList);
				}
			}
		}
		pairList = pairsArea(Area.HORIZONTAL, row, column, depth);
		var(depth, "pairs", pairList.size());
		pairList = pairsArea(Area.VERTICAL, row, column, depth);
		var(depth, "pairs", pairList.size());
		// added += setNumber(row, column, number);
		var(depth, "added", added);
		if(view && (added > 0))
			System.out.println(Method.NAKED_SINGLE.name() + " >> " + number + " @ " + coordinates(row, column));
		return added;
    }
	*/

	// Check if the missing number is allowed to be put in the cell.
	// The method should be called twice in order to verify the position.
	//           j        jFirst   jSecond 
	//         +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// i       | ?|  |  |  |  |  |  |  |  |  | ?|  |  |  |  |  |  |  |  |  | ?|  |  |  |  |  |  |  |  |
	//         +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// iFirst  |  |  |  |  |  |  |  |  |  |  |  |  |  | 1|  |  |  |  |  |  |  |  |  | 1|  |  |  |  |  |
	//         +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// iSecond |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | 1|  |  |  | X| X| X|  |  |  | X| X| X|
	//         +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	//                                        Example 1                     Example 2
    boolean missingArea(Area area, int i, int j, int number, int depth) {
		log(depth++, "missingArea(area = " + area + ", i = " + i + ", j = " + j + ", number = " + number + ")");
		boolean allowed = false;
		Area blockArea = (area == Area.HORIZONTAL)? Area.BLOCK_HORIZONTAL: Area.BLOCK_VERTICAL;
		int jFirst = (area == Area.HORIZONTAL)? blockNext(j): blockNext(i);
		var(depth, "jFirst", jFirst);
		int jSecond = (area == Area.HORIZONTAL)? blockNextException(j, jFirst): blockNextException(i, jFirst);
		var(depth, "jSecond", jSecond);
		int count_jFirst = (area == Area.HORIZONTAL)? countArea(blockArea, i, jFirst, depth): countArea(blockArea, j, jFirst, depth); // 1 x 3
		var(depth, "count_jFirst", count_jFirst);
		int count_jSecond = (area == Area.HORIZONTAL)? countArea(blockArea, i, jSecond, depth): countArea(blockArea, j, jSecond, depth); // 1 x 3
		var(depth, "count_jSecond", count_jSecond);
		if((count_jFirst == Position.getMin()) && (count_jSecond == Position.getMin()))
			allowed = true;
		else {
			int iFirst = (area == Area.HORIZONTAL)? next(i): next(j);
			var(depth, "iFirst", iFirst);
			int iSecond = (area == Area.HORIZONTAL)? nextException(i, iFirst): nextException(j, iFirst);
			var(depth, "iSecond", iSecond);
			int exists_iFirst = (area == Area.HORIZONTAL)? blockBase(existsArea(area, iFirst, j, number, depth)):
				blockBase(existsArea(area, i, iFirst, number, depth)); // 1 x 9
			var(depth, "exists_iFirst", exists_iFirst);
			int exists_iSecond = (area == Area.HORIZONTAL)? blockBase(existsArea(area, iSecond, j, number, depth)):
				blockBase(existsArea(area, i, iSecond, number, depth)); // 1 x 9
			var(depth, "exists_iSecond", exists_iSecond);
			if(((exists_iFirst == jFirst) && (exists_iSecond == jSecond)) ||
			   ((exists_iFirst == jSecond) && (exists_iSecond == jFirst)))
				allowed = true;
			else {
				int count_iFirst_jFirst = countArea(blockArea, iFirst, jFirst, depth); // 1 x 3
				var(depth, "count_iFirst_jFirst", count_iFirst_jFirst);
				int count_iFirst_jSecond = countArea(blockArea, iFirst, jSecond, depth); // 1 x 3
				var(depth, "count_iFirst_jSecond", count_iFirst_jSecond);
				int count_iSecond_jFirst = countArea(blockArea, iSecond, jFirst, depth); // 1 x 3
				var(depth, "count_iSecond_jFirst", count_iSecond_jFirst);
				int count_iSecond_jSecond = countArea(blockArea, iSecond, jSecond, depth); // 1 x 3
				var(depth, "count_iSecond_jSecond", count_iSecond_jSecond);
				if((exists_iFirst == NOT_FOUND) &&
				  (((exists_iSecond == jFirst) && (count_jSecond == Position.getMin()) && (count_iSecond_jSecond == Position.getMin())) ||
				   ((exists_iSecond == jSecond) && (count_jFirst == Position.getMin()) && (count_iSecond_jFirst == Position.getMin()))))
					allowed = true;
				else if((exists_iSecond == NOT_FOUND) &&
				  (((exists_iFirst == jFirst) && (count_jSecond == Position.getMin()) && (count_iFirst_jSecond == Position.getMin())) ||
				   ((exists_iFirst == jSecond) && (count_jFirst == Position.getMin()) && (count_iFirst_jFirst == Position.getMin()))))
					allowed = true;
			}
		}
		var(depth, "allowed", allowed);
		return allowed;
	}

    // Set the hidden single number when
	// 1. all surrounding same numbers are present
	// 2. all surrounding 3 cell areas are blocked
    int setMissing(int row, int column, int number, int depth) {
		log(depth++, "setMissing(row = " + row + ", column = " + column + ", number = " + number + ")");
		int added = 0;
		if((existsArea(Area.BLOCK, row, column, number, depth) == NOT_FOUND) && // 3 x 3
          (existsArea(Area.HORIZONTAL, row, column, number, depth) == NOT_FOUND) && // 1 x 9
          (existsArea(Area.VERTICAL, row, column, number, depth) == NOT_FOUND) && // 9 x 1
		  missingArea(Area.HORIZONTAL, row, column, number, depth) && // 2 x 9
		  missingArea(Area.VERTICAL, row, column, number, depth)) // 9 x 2
			added += setNumber(row, column, number);
		var(depth, "added", added);
		if(view && (added > 0))
			System.out.println(Method.MISSING.name() + " >> " + number + " @ " + coordinates(row, column));
		return added;
    }

	// Set the missing number if the cell is the only allowed position
	int setHiddenSingle(int row, int column, int number, int depth) {
		log(depth++, "setHiddenSingle(row = " + row + ", column = " + column + ", number = " + number + ")");
		int added = 0;
		for(Area area: Area.values()) {
			int notAllowed = 0;
			switch(area) {
				case BLOCK:
					for(int i = 0, y = blockBase(row); i < Position.getMin(); i++, y++) {
						for(int j = 0, x = blockBase(column); j < Position.getMin(); j++, x++) {
							if(((x != column) || (y != row)) &&
							   ((s[y][x][FACE] != MISSING) ||
								(existsArea(Area.HORIZONTAL, y, x, number, depth) != NOT_FOUND) ||
								(existsArea(Area.VERTICAL, y, x, number, depth) != NOT_FOUND)))
								notAllowed++;
						}
					}
					break;
				case HORIZONTAL:
					for(int x = 0; x < Position.getMin(); x++)
						if((x != column) &&
						   ((s[row][x][FACE] != MISSING) ||
							(existsArea(Area.VERTICAL, row, x, number, depth) != NOT_FOUND)))
							notAllowed++;
					break;
				case VERTICAL:
					for(int y = 0; y < Position.getMin(); y++)
						if((y != row) &&
						   ((s[y][column][FACE] != MISSING) ||
							(existsArea(Area.HORIZONTAL, y, column, number, depth) != NOT_FOUND)))
							notAllowed++;
					break;
			}
			if(notAllowed == Position.getMax() - 1) {
				added += setNumber(row, column, number);
				break;
			}
		}
		var(depth, "added", added);
		if(view && (added > 0))
			System.out.println(Method.HIDDEN_SINGLE.name() + " >> " + number + " @ " + coordinates(row, column));
		return added;
	}

    void solution() {
		int depth = 0;
		log(depth++, "solution()");
        startTime = System.nanoTime();
		if(methodList.size() == 0)
			for(Method method: Method.values())
				methodList.add(method);
		String go = EMPTY;
		int counter = count(Area.ALL, depth);
        int added = TOTAL;
        while((added > 0) || (goList.size() > 0)) {
            passages++;
            added = 0;
			if(goList.size() > 0) {
				go = goList.get(0);
				goList.remove(0);
			}
            for(int y = 0; y < Position.getMax(); y++) {
				if(go.length() > 0)
					y = go.charAt(0) - ZERO;
                for(int x = 0; x < Position.getMax(); x++) {
					if(go.length() > 1)
						x = go.charAt(1) - ZERO;
					printMissing(y, x);
					if(s[y][x][FACE] == MISSING) { // empty?
						for(int number = 1; number <= Position.getMax(); number++) {
							if(go.length() > 2)
								number = go.charAt(2) - ZERO;
							if(s[y][x][number] == number) { // possible position?
								if(methodList.contains(Method.NAKED_SINGLE))
									added += setNakedSingle(y, x, number, depth);
								if(methodList.contains(Method.MISSING))
								   added += setMissing(y, x, number, depth);
								if(methodList.contains(Method.HIDDEN_SINGLE))
									added += setHiddenSingle(y, x, number, depth);
								if(methodList.contains(Method.NAKED_PAIR))
									added += setNakedPair(y, x, number, depth);
							}
							if(go.length() > 0)
								break;
						}
					}
					if(go.length() > 0)
						break;
				}
				if(go.length() > 0)
					break;
			}
			var(depth, "added", added);
            if((added > 0) && view)
                print();
			if(interactive) {
				String pause = null;
				try {
					pause = in.nextLine();
				} catch(Exception e) {
					System.out.println(e);
				}
			}
			counter += added;
			if((counter == TOTAL) || ((go.length() > 0) && (goList.size() == 0)))
				break;
        }
        endTime = System.nanoTime();
        if(!view)
            print();
        statistics();
		if(analyze)
			analysis();
    }

    void statistics() {
		System.out.println();
		System.out.println(printSpace("STATISTICS"));
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

    void analysis() {
		boolean memory = debug;
		debug = true;
		matrix.printMissing(Area.BLOCK);
		matrix.printMissing(Area.HORIZONTAL);
		matrix.printMissing(Area.VERTICAL);
		debug = memory;
    }

	public static void help() {
		System.out.println("Sudoku.java by Kostas Dimou @ 2019");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("    Sudoku [--analyze] [--coordinates SYSTEM] [--debug] [--go YXN] [--help] \\");
		System.out.println("           [--interactive] [--method METHOD] [--solve] [--view]");
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
		System.out.println("        Try a specific coordinate and/or a specific number.");
		System.out.println("        Example:");
		System.out.println("            --go 419: row = 4, column = 1, number = 9.");
		System.out.println("            --chess --go 5A9: row = 4, column = 1, number = 9.");
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
		System.out.println("            HIDDEN_SINGLE");
		System.out.println("            MISSING");
		System.out.println("            NAKED_PAIR");
		System.out.println("            NAKED_SINGLE");
		System.out.println("    -s or --solve:");
		System.out.println("        Solves the Sudoku by using all possible methods.");
		System.out.println("    -v or --view:");
		System.out.println("        Displays the Sudoku matrix on every passage.");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("    java Sudoku --help");
		System.out.println("    java Sudoku --interactive");
		System.out.println("    java Sudoku --coordinate sudoku < Sudoku.0002");
		System.out.println("    java Sudoku --analyze < Sudoku.0002");
		System.out.println("    java Sudoku --solve < Sudoku.0002");
		System.out.println("    java Sudoku -s --view < Sudoku.0002");
		System.out.println("    java Sudoku -s --go 419 --debug < Sudoku.0006");
		System.out.println("    java Sudoku -s --method NAKED_SINGLE < Sudoku.0000");
		System.out.println("    java Sudoku -s -m NAKED_SINGLE -m NAKED_PAIR < Sudoku.0001");
	}

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
        String value = EMPTY;
        if(args.length > 0)
			for(String argument: args) {
				if(value.length() == 0) {
					if(argument.equals("-a") || argument.equals("--analyze"))
						sudoku.setAnalyze();
					else if(argument.equals("-c") || argument.equals("--coordinate"))
						value = "-c";
					else if(argument.equals("-d") || argument.equals("--debug"))
						sudoku.setDebug();
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
					else if(argument.equals("-v") || argument.equals("--view"))
						sudoku.setView();
					else {
						System.out.println("Unsupported flag: " + argument);
						return;
					}
				} else
					if(value.equals("-c")) {
						if(!coordinate.setSystem(argument))
							return;
						value = EMPTY;
					} else if(value.equals("-g")) {
						if(!sudoku.setGo(argument))
							return;
						value = EMPTY;
					} else if(value.equals("-m")) {
						if(!sudoku.setActive(argument))
							return;
						value = EMPTY;
					}
			}
        sudoku.run();
	}
}
