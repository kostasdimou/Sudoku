import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

class Sudoku {
    private static final int MAX = 9;
    private static final int TOTAL = MAX * MAX;
    private static final int MIN = MAX / 3;
    private static final int FACE = 0;
    private static final int MISSING = 0;
    private static final int NOT_FOUND = -1;
    private static final int QUIET = -1;
    private static final char DASH = '-';
    private static final char PIPE = '|';
    private static final char PLUS = '+';
    private static final char SPACE = ' ';
    private static final char ZERO = '0';
    private static final char ALPHA = 'A';
    private static final String EMPTY = "";

	private static boolean analyze = false;
	private static boolean debug = false;
	private static boolean interactive = false;
	private static boolean solve = false;
	private static boolean view = false;

    private int[][][] s = new int[MAX][MAX][1 + MAX]; // [row][column][FACE, number]
    private int passages = 0;
    private long startTime = 0;
    private long endTime = 0;
	private Coordinate coordinate = Coordinate.JAVA;
	private List<Method> methodList = new ArrayList<Method>();
	private List<String> goList = new ArrayList<String>();
	private List<int> allAllowed = Stream.iterate(1, n -> n + 1).limit(MAX).collect(Collectors.toList());
    private Scanner in = new Scanner(System.in);

	public enum Area {
		ALL, // y * x * FACE (9 x 9)
		HORIZONTAL, // 1 * x * FACE (1 * 9)
		VERTICAL, // y * 1 * FACE (9 * 1)
		BLOCK, // y * x * FACE (3 * 3)
		MINI_HORIZONTAL, // 1 * x * FACE (1 * 3)
		MINI_VERTICAL, // y * 1 * FACE (3 * 1)
		NUMBER //  1 * 1 * n (1 * 1)
	}

	public enum Method {
		NAKED_SINGLE,
		HIDDEN_SINGLE,
		MISSING,
		NAKED_PAIR
	};

	public enum Coordinate {
		CHESS, // Y=9-1, X=A-I
		JAVA, // Y=0-9, X=0-9 (default)
		SUDOKU // Y=A-I, X=1-9
	};

	class Position {
		int y;
		int x;

		Position() {
			y = NOT_FOUND;
			x = NOT_FOUND;
		}

		Position(int row, int column) {
			y = row;
			x = column;
		}

		Position(Position position) {
			y = position.y;
			x = position.x;
		}

		void print(int depth) {
			System.out.print(margin(depth) + "[" + coordinateY(y) + "," + coordinateX(x) + "]");
		}

		void println(int depth) {
			print(depth);
			System.out.println();
		}

		boolean equals(Position position) {
			if(y == position.y)
				return (x == position.x);
			return false;
		}
	}

	class Cell implements Comparable<Cell> {
		Position position;
		int number;
		List<int> allowed = allAllowed;

		Cell() {
			number = MISSING;
		}

		Cell(int number) {
			this.number = number;
		}

		Cell(Position position, int number) {
			this.position = position;
			this.number = number;
		}

		void print(int depth) {
			System.out.print(margin(depth) + "Cell");
			position.print(0);
			System.out.print(EMPTY + SPACE + number);
		}

		void println(int depth) {
			print(depth);
			System.out.println();
		}

		boolean equals(Cell cell) {
			if(position.equals(cell.position))
				return (number == cell.number);
			return false;
		}

		// Removes allowed numbers
		void remove(int notAllowed) {
			allowed.remove(allowed.indexOf(notAllowed));
		}
	}

	class Pair implements Comparable<Pair> {
		List<Cell> cellList = new ArrayList<Cell>();

		boolean equals(Pair pair) {
			switch(cellList.size()) {
				case 0:
					return true;
				case 1:
					return cellList.get(0).equals(pair.cellList.get(0));
				default:
					if(cellList.get(0).equals(pair.cellList.get(0)))
						return cellList.get(1).equals(pair.cellList.get(1));
			}
			return false;
		}

		int value() {
			switch(cellList.size()) {
				case 0:
					return 0;
				case 1:
					return MAX * cellList.get(0).number;
				default:
					return MAX * cellList.get(0).number + cellList.get(1).number;
			}
			return 0;
		}
	}

	class Matrix {
		private Cell[][] cellList = new Cell[MAX][MAX]; // [row][column]
	}

    void setAnalyze() {
		this.analyze = true;
	}

    boolean setCoordinate(String coordinateName) {
		boolean found = false;
		for(Coordinate coordinate: Coordinate.values())
			if(coordinateName.equals(coordinate.name())) {
				this.coordinate = coordinate;
				found = true;
			}
		if(!found)
			System.out.println("Unsupported coordinate: " + coordinateName);
		return found;
	}

    void setDebug() {
		this.debug = true;
	}

	// Stores the specific cell coordinates and number to work with
    boolean setGo(String go) {
		String message = "Incompatible coordinates: " + go;
		StringBuilder goUpper = new StringBuilder(go.toUpperCase());
		char y = goUpper.charAt(0);
		if((y >= ALPHA) && (coordinate != Coordinate.SUDOKU)) {
			System.out.println(message);
			return false;
		}
		char x = goUpper.charAt(1);
		if((x >= ALPHA) && (coordinate != Coordinate.CHESS)) {
			System.out.println(message);
			return false;
		}
		if(((y >= ZERO + MAX) || (x >= ZERO + MAX)) && (coordinate == Coordinate.JAVA)) {
			System.out.println(message);
			return false;
		}
		goUpper.setCharAt(0, readY(y));
		goUpper.setCharAt(1, readX(x));
		goList.add(goUpper.toString());
		return true;
	}

    void setInteractive() {
		this.interactive = true;
	}

	// Stores the methods we want activated for the solution
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

    void setSolve() {
		this.solve = true;
	}

    void setView() {
		this.view = true;
	}

	public static String margin(int depth) {
		int width = depth * 2;
		String result = EMPTY;
		for(int i = 0; i < width; i++)
			result += SPACE;
		return result;
	}

	public static void log(int depth, String message) {
		if(debug)
			System.out.println(margin(depth) + message);
	}

	public static void var(int depth, String name, Object value) {
		String message = name + " = " + value;
		log(depth, message);
	}

	// Converts JAVA y index to coordinate system
	char coordinateY(Integer y) {
		char map = SPACE;
		switch(coordinate) {
			case JAVA: // Y=0-9, X=0-9
				map = y.toString().charAt(0);
				break;
			case SUDOKU: // Y=A-I, X=1-9
				map = (char)(ALPHA + y);
				break;
			case CHESS: // Y=9-1, X=A-I
				y = MAX - y;
				map = y.toString().charAt(0);
				break;
		}
		return map;
	}

	// Converts JAVA x index to coordinate system
	char coordinateX(Integer x) {
		char map = SPACE;
		switch(coordinate) {
			case JAVA: // Y=0-9, X=0-9
				map = x.toString().charAt(0);
				break;
			case SUDOKU: // Y=A-I, X=1-9
				x++;
				map = x.toString().charAt(0);
				break;
			case CHESS: // Y=9-1, X=A-I
				map = (char)(ALPHA + x);
				break;
		}
		return map;
	}

	// Converts JAVA y, x indexes to coordinate system
	String coordinates(int y, int x) {
		return EMPTY + coordinateY(y) + coordinateX(x);
	}

	// Converts the coordinate system y to JAVA index
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

	// Converts the coordinate system x to JAVA index
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

    int blockBase(int i) {
		int base = NOT_FOUND;
		if(i != NOT_FOUND)
			base = (i / MIN) * MIN;
		return base;
	}

	int nextException(int i, int exception) {
		int base = blockBase(i);
		for(int other = base; other < base + MIN; other++) {
			if(other == i)
				continue;
			if(other == exception)
				continue;
			return other;
		}
		return NOT_FOUND;
	}

	int next(int i) {
		return nextException(i, NOT_FOUND);
	}

	int blockNextException(int i, int exception) {
		int base = blockBase(i);
		for(int next = 0; next < MAX; next += MIN) {
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
				for(int y = 0; y < MAX; y++)
					for(int x = 0; x < MAX; x++)
						if((s[y][x][FACE] != MISSING) && (s[y][x][FACE] != exception))
							counter++;
				break;
            case HORIZONTAL:
                for(int x = 0; x < MAX; x++)
                    if((s[row][x][FACE] != MISSING) && (s[row][x][FACE] != exception))
						counter++;
                break;
            case VERTICAL:
                for(int y = 0; y < MAX; y++)
                    if((s[y][column][FACE] != MISSING) && (s[y][column][FACE] != exception))
						counter++;
                break;
            case BLOCK:
                for(int i = 0, y = blockBase(row); i < MIN; i++, y++)
                    for(int j = 0, x = blockBase(column); j < MIN; j++, x++)
                        if((s[y][x][FACE] != MISSING) &&
                           (s[y][x][FACE] != exception))
							counter++;
				break;
            case MINI_HORIZONTAL:
				for(int j = 0, x = blockBase(column); j < MIN; j++, x++)
					if((s[row][x][FACE] != MISSING) &&
					   (s[row][x][FACE] != exception))
						counter++;
				break;
            case MINI_VERTICAL:
                for(int i = 0, y = blockBase(row); i < MIN; i++, y++)
					if((s[y][column][FACE] != MISSING) &&
					   (s[y][column][FACE] != exception))
						counter++;
				break;
            case NUMBER:
                for(int n = 1; n <= MAX; n++)
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
					for(int y = 0; y < MAX; y++)
						for(int x = 0; x < MAX; x++)
							if(s[y][x][FACE] == number)
								exists = y;
					break;
				case BLOCK:
					for(int i = 0, y = blockBase(row); i < MIN; i++, y++)
						for(int j = 0, x = blockBase(column); j < MIN; j++, x++)
							if(s[y][x][FACE] == number)
								exists = y;
					break;
				case HORIZONTAL:
					for(int x = 0; x < MAX; x++)
						if(s[row][x][FACE] == number)
							exists = x;
					break;
				case VERTICAL:
					for(int y = 0; y < MAX; y++)
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
        for(int y = 0; y < MIN; y++) {
            for(int x = 0; x < MIN; x++)
                printDash(DASH);
            printDash(PLUS);
        }
        if(showCounter)
            System.out.print(" (" + count(Area.ALL, QUIET) + ")");
        System.out.println();
    }

    void print() {
		System.out.print(SPACE);
		for(int x = 0; x < MAX; x++) {
			if(x % MIN == 0)
				printSpace(SPACE);
			printSpace(coordinateX(x));
		}
		System.out.println();
        for(int y = 0; y < MAX; y++) {
            if((y % MIN) == 0)
				printHorizontalLine(SPACE, false);
			System.out.print(coordinateY(y));
            for(int x = 0; x < MAX; x++) {
                if((x % MIN) == 0)
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

    void printMissing(int row, int column) {
		if(debug) {
			System.out.print("Missing[" + coordinateY(row) + "," + coordinateX(column) + "]");
			int counter = 0;
			for(int i = 1; i <= MAX; i++) {
				System.out.print(SPACE);
				if(s[row][column][i] != MISSING) {
					System.out.print(i);
					counter++;
				}
				else
					System.out.print(SPACE);
			}
			switch(counter) {
				case 1:
					System.out.print(" << SINGLE >>");
					break;
				case 2:
					System.out.print(" << PAIR >>");
					break;
			}
			System.out.println();
		}
	}

	void populateMissingNumbers() {
		for(int y = 0; y < MAX; y++)
			for(int x = 0; x < MAX; x++) {
				for(int number = 1; number <= MAX; number++)
					s[y][x][number] = number;
			}
	}

    void run() {
		System.out.println();
		System.out.println(dilute("SUDOKU"));
		System.out.println();
		populateMissingNumbers();
        String row;
        for(int y = 0; y < MAX; y++) {
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
            for(int x = 0; x < MAX; x++)
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
					for(int y = 0; y < MAX; y++)
						for(int x = 0; x < MAX; x++)
							if(s[y][x][number] != MISSING)
								s[y][x][number] = MISSING;
					break;
				case BLOCK:
					for(int i = 0, y = blockBase(row); i < MIN; i++, y++)
						for(int j = 0, x = blockBase(column); j < MIN; j++, x++)
							if(s[y][x][number] != MISSING)
								s[y][x][number] = MISSING;
					break;
				case HORIZONTAL:
					for(int x = 0; x < MAX; x++)
						if(s[row][x][number] != MISSING)
							s[row][x][number] = MISSING;
					break;
				case VERTICAL:
					for(int y = 0; y < MAX; y++)
						if(s[y][column][number] != MISSING)
							s[y][column][number] = MISSING;
					break;
				case NUMBER:
					for(int n = 1; n <= MAX; n++)
						if(s[row][column][n] != MISSING)
							s[row][column][n] = MISSING;
					break;
			}
	}

    void unset(int row, int column, int number) {
		if(number != MISSING) {
			unsetArea(Area.BLOCK, row, column, number);
			unsetArea(Area.HORIZONTAL, row, column, number);
			unsetArea(Area.VERTICAL, row, column, number);
			unsetArea(Area.NUMBER, row, column, number);
		}
	}

    void unset(int number, List<Cell> cellList) {
		if(number != MISSING)
			for(Cell cell: cellList)
				cell.remove(number);
	}

    int setNumber(int row, int column, int number) {
		s[row][column][FACE] = number;
		if(number != MISSING)
			unset(row, column, number);
		return 1; // added
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
		if((countAreaException(Area.BLOCK, row, column, number, depth) == MAX - 1) || // last one in-block
		   (countAreaException(Area.HORIZONTAL, row, column, number, depth) == MAX - 1) || // last one horizontally
		   (countAreaException(Area.VERTICAL, row, column, number, depth) == MAX - 1) || // last one vertically
		   (countAreaException(Area.NUMBER, row, column, number, depth) == 0)) // all others are not allowed here
			added += setNumber(row, column, number);
		var(depth, "added", added);
		if(view && (added > 0))
			System.out.println(Method.NAKED_SINGLE.name() + " >> " + number + " @ " + coordinates(row, column));
		return added;
    }

    List<Pair> pairsArea(Area area, int row, int column, int depth) {
		log(depth++, "pairsArea(area = " + area + ", row = " + row + ", column = " + column + ")");
		List<Pair> pairList = new ArrayList<Pair>();
		switch(area) {
			case ALL:
				for(int y = 0; y < MAX; y++)
					for(int x = 0; x < MAX; x++) {
						Position position = new Position(y, x);
						if(countArea(Area.NUMBER, y, x, depth) == 2) {
							Pair pair = new Pair();
							for(int number = 1; number <= MAX; number++)
								if(s[y][x][number] == number) {
									Cell cell = new Cell(position, number);
									pair.cellList.add(cell);
								}
							pairList.add(pair);
						}
					}
				break;
			case BLOCK:
				for(int i = 0, y = blockBase(row); i < MIN; i++, y++)
					for(int j = 0, x = blockBase(column); j < MIN; j++, x++) {
						Position position = new Position(y, x);
						if(countArea(Area.NUMBER, y, x, depth) == 2) {
							Pair pair = new Pair();
							for(int number = 1; number <= MAX; number++)
								if(s[y][x][number] == number) {
									Cell cell = new Cell(position, number);
									pair.cellList.add(cell);
								}
							pairList.add(pair);
						}
					}
				break;
			case HORIZONTAL:
				for(int x = 0; x < MAX; x++) {
					Position position = new Position(row, x);
					if(countArea(Area.NUMBER, row, x, depth) == 2) {
						Pair pair = new Pair();
						for(int number = 1; number <= MAX; number++)
							if(s[row][x][number] == number) {
								Cell cell = new Cell(position, number);
								pair.cellList.add(cell);
							}
						pairList.add(pair);
					}
				}
				break;
			case VERTICAL:
				for(int y = 0; y < MAX; y++) {
					Position position = new Position(y, column);
					if(countArea(Area.NUMBER, y, column, depth) == 2) {
						Pair pair = new Pair();
						for(int number = 1; number <= MAX; number++)
							if(s[y][column][number] == number) {
								Cell cell = new Cell(position, number);
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

    // Check for multiple naked pairs per area and unset the allowed numbers.
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
					cellList.remove(matrix.cells(first.position));
					cellList.remove(matrix.cells(second.position));
					matrix.unset(int number, cellList);
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
		Area blockArea = (area == Area.HORIZONTAL)? Area.MINI_HORIZONTAL: Area.MINI_VERTICAL;
		int jFirst = (area == Area.HORIZONTAL)? blockNext(j): blockNext(i);
		var(depth, "jFirst", jFirst);
		int jSecond = (area == Area.HORIZONTAL)? blockNextException(j, jFirst): blockNextException(i, jFirst);
		var(depth, "jSecond", jSecond);
		int count_jFirst = (area == Area.HORIZONTAL)? countArea(blockArea, i, jFirst, depth): countArea(blockArea, j, jFirst, depth); // 1 x 3
		var(depth, "count_jFirst", count_jFirst);
		int count_jSecond = (area == Area.HORIZONTAL)? countArea(blockArea, i, jSecond, depth): countArea(blockArea, j, jSecond, depth); // 1 x 3
		var(depth, "count_jSecond", count_jSecond);
		if((count_jFirst == MIN) && (count_jSecond == MIN))
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
				  (((exists_iSecond == jFirst) && (count_jSecond == MIN) && (count_iSecond_jSecond == MIN)) ||
				   ((exists_iSecond == jSecond) && (count_jFirst == MIN) && (count_iSecond_jFirst == MIN))))
					allowed = true;
				else if((exists_iSecond == NOT_FOUND) &&
				  (((exists_iFirst == jFirst) && (count_jSecond == MIN) && (count_iFirst_jSecond == MIN)) ||
				   ((exists_iFirst == jSecond) && (count_jFirst == MIN) && (count_iFirst_jFirst == MIN))))
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
					for(int i = 0, y = blockBase(row); i < MIN; i++, y++) {
						for(int j = 0, x = blockBase(column); j < MIN; j++, x++) {
							if(((x != column) || (y != row)) &&
							   ((s[y][x][FACE] != MISSING) ||
								(existsArea(Area.HORIZONTAL, y, x, number, depth) != NOT_FOUND) ||
								(existsArea(Area.VERTICAL, y, x, number, depth) != NOT_FOUND)))
								notAllowed++;
						}
					}
					break;
				case HORIZONTAL:
					for(int x = 0; x < MIN; x++)
						if((x != column) &&
						   ((s[row][x][FACE] != MISSING) ||
							(existsArea(Area.VERTICAL, row, x, number, depth) != NOT_FOUND)))
							notAllowed++;
					break;
				case VERTICAL:
					for(int y = 0; y < MIN; y++)
						if((y != row) &&
						   ((s[y][column][FACE] != MISSING) ||
							(existsArea(Area.HORIZONTAL, y, column, number, depth) != NOT_FOUND)))
							notAllowed++;
					break;
			}
			if(notAllowed == MAX - 1) {
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
            for(int y = 0; y < MAX; y++) {
				if(go.length() > 0)
					y = go.charAt(0) - ZERO;
                for(int x = 0; x < MAX; x++) {
					if(go.length() > 1)
						x = go.charAt(1) - ZERO;
					printMissing(y, x);
					if(s[y][x][FACE] == MISSING) { // empty?
						for(int number = 1; number <= MAX; number++) {
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

	String dilute(String dense) {
		String thin = EMPTY;
		for(int i = 0; i < dense.length(); i++) {
			if(i != 0)
				thin += SPACE;
			thin += dense.charAt(i);
		}
		return thin;
	}

	String dilute(Area area) {
		return dilute(area.name());
	}

    void statistics() {
		System.out.println();
		System.out.println(dilute("STATISTICS"));
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
		System.out.println();
		System.out.println(dilute(Area.HORIZONTAL));
        for(int y = 0; y < MAX; y++) {
			System.out.println();
            for(int x = 0; x < MAX; x++)
                if(s[y][x][FACE] == MISSING)
                    printMissing(y, x);
		}
		System.out.println();
		System.out.println(dilute(Area.VERTICAL));
		for(int x = 0; x < MAX; x++) {
			System.out.println();
			for(int y = 0; y < MAX; y++)
                if(s[y][x][FACE] == MISSING)
                    printMissing(y, x);
		}
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
						if(!sudoku.setCoordinate(argument))
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
