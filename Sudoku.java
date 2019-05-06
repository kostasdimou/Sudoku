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
    private static final char DASH = '-';
    private static final char PIPE = '|';
    private static final char PLUS = '+';
    private static final char SPACE = ' ';
    private static final String TAB = "  ";
    private static final String[] methods = {"setSingle", "setMissing", "setAllowed"};

	private static boolean analyze = false;
	private static boolean debug = false;
	private static boolean interactive = false;
	private static boolean solve = false;
	private static boolean view = false;

    private int[][][] s = new int[MAX][MAX][1 + MAX]; // [row][column][FACE, number]
    private int passages = 0;
    private long startTime = 0;
    private long endTime = 0;
	private List<String> isActive = new ArrayList<String>(Arrays.asList(methods));
	private List<String> goes = new ArrayList<String>();
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

    void setAnalyze() {
		this.analyze = true;
	}

    void setDebug() {
		this.debug = true;
	}

    void setGo(String go) {
		goes.add(go);
	}

    void setInteractive() {
		this.interactive = true;
	}

    void setSolve() {
		this.solve = true;
	}

    void setView() {
		this.view = true;
	}

	public static String margin(int depth) {
		String result = "";
		for(int i = 0; i < depth; i++)
			result += TAB;
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

    int blockBase(int i) {
		int base = NOT_FOUND;
		if(i != NOT_FOUND)
			base = (i / MIN) * MIN;
		return base;
	}

	int pairException(int i, int exception) {
		int base = blockBase(i);
		for(int pair = base; pair < base + MIN; pair++) {
			if(pair == i)
				continue;
			if(pair == exception)
				continue;
			return pair;
		}
		return NOT_FOUND;
	}

	int pairException(int i) {
		return pairException(i, NOT_FOUND);
	}

	int blockPairException(int i, int exception) {
		int base = blockBase(i);
		int blockPair = NOT_FOUND;
		for(int pair = 0; pair < MAX; pair += MIN) {
			if(pair == base)
				continue;
			if(pair == exception)
				continue;
			blockPair = pair;
			break;
		}
		return blockPair;
	}

	int blockPairException(int i) {
		return blockPairException(i, NOT_FOUND);
	}

	// Count the existing numbers omitting the exception
    int countAreaException(Area area, int row, int column, int exception) {
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
        return counter;
    }

	// Count without exception
    int countArea(Area area, int row, int column) {
		return countAreaException(area, row, column, MISSING);
	}

	// Count all cells
    int count(Area area) {
		return countArea(area, 0, 0);
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

    void printHorizontalLine(boolean showCounter) {
        System.out.print(PLUS);
        for(int y = 0; y < MIN; y++) {
            for(int x = 0; x < MIN; x++)
                System.out.print(DASH);
            System.out.print(PLUS);
        }
        if(showCounter)
            System.out.print(" (" + count(Area.ALL) + ")");
        System.out.println();
    }

    void print() {
        for(int y = 0; y < MAX; y++) {
            if((y % MIN) == 0)
                printHorizontalLine(false);
            for(int x = 0; x < MAX; x++) {
                if((x % MIN) == 0)
                    System.out.print(PIPE);
                if(s[y][x][FACE] != MISSING)
                    System.out.print(s[y][x][FACE]);
                else
                     System.out.print(SPACE);
            }
            System.out.println(PIPE);
        }
        printHorizontalLine(true);
		if(analyze)
			analysis();
    }

    void printMissing(int row, int column) {
		if(debug) {
			System.out.print("M["+row + ","+column+"]");
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
			if(counter == 1)
				System.out.println(" << SINGLE >>");
			else
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
                        setNumber(y, x, row.charAt(x) - '0');
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
							s[y][x][number] = MISSING;
					break;
				case BLOCK:
					for(int i = 0, y = blockBase(row); i < MIN; i++, y++)
						for(int j = 0, x = blockBase(column); j < MIN; j++, x++)
							s[y][x][number] = MISSING;
					break;
				case HORIZONTAL:
					for(int x = 0; x < MAX; x++)
						s[row][x][number] = MISSING;
					break;
				case VERTICAL:
					for(int y = 0; y < MAX; y++)
						s[y][column][number] = MISSING ;
					break;
			}
	}

    void unsetMissing(int row, int column, int number) {
		if(number != MISSING) {
			unsetArea(Area.BLOCK, row, column, number);
			unsetArea(Area.HORIZONTAL, row, column, number);
			unsetArea(Area.VERTICAL, row, column, number);
		}
	}

    int setNumber(int row, int column, int number) {
		s[row][column][FACE] = number;
		if(number != MISSING)
			unsetMissing(row, column, number);
		return 1; // added
	}

    // Set the last missing number when all other are present
	// 
	//      column
	//     +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// row |1?|X |X |  |  |  |  |  |  |  |1?|X |X |X |X |X |X |X |X |  |1?|  |  |  |  |  |  |  |  |
	//     +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	//     |X |X |X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |X |  |  |  |  |  |  |  |  |
	//     +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	//     |X |X |X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |X |  |  |  |  |  |  |  |  |
	//     +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	//      Example Block                 Example Horizontal            Example Vertical
    int setSingle(int row, int column, int number, int depth) {
		log(depth++, "setSingle(row = " + row + ", column = " + column + ", number = " + number + ")");
		int added = 0;
        if((s[row][column][FACE] == MISSING) && // empty?
           (s[row][column][number] != MISSING)) { // possible position?
			if((countAreaException(Area.BLOCK, row, column, number) == MAX - 1) || // in-block
			   (countAreaException(Area.HORIZONTAL, row, column, number) == MAX - 1) || // horizontally
			   (countAreaException(Area.VERTICAL, row, column, number) == MAX - 1) || // vertically
			   (countAreaException(Area.NUMBER, row, column, number) == 0)) // in-depth
				added += setNumber(row, column, number);
		}
		var(depth, "added", added);
		return added;
    }

	// Check if the missing number is allowed to be put in the cell.
	// The method should be called twice in order to verify the position.
	//        j         jNext     jPair 
	//       +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// i     |1?|  |  |  |  |  |  |  |  |  |1?|  |  |  |  |  |  |  |  |  |1?|  |  |  |  |  |  |  |  |
	//       +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// iNext |  |  |  |  |  |  |  |  |  |  |  |  |  |1 |  |  |  |  |  |  |  |  |  |1 |  |  |  |  |  |
	//       +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	// iPair |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |1 |  |  |  |X |X |X |  |  |  |X |X |X |
	//       +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+  +--+--+--|--+--+--|--+--+--+
	//                                      Example 1                     Example 2
    boolean missing(Area area, int i, int j, int number, int depth) {
		log(depth++, "missing(area = " + area + ", i = " + i + ", j = " + j + ", number = " + number + ")");
		boolean allowed = false;
		Area blockArea = (area == Area.HORIZONTAL)? Area.MINI_HORIZONTAL: Area.MINI_VERTICAL;
		int jNext = (area == Area.HORIZONTAL)? blockPairException(j): blockPairException(i);
		var(depth, "jNext", jNext);
		int jPair = (area == Area.HORIZONTAL)? blockPairException(j, jNext): blockPairException(i, jNext);
		var(depth, "jPair", jPair);
		int count_jNext = (area == Area.HORIZONTAL)? countArea(blockArea, i, jNext): countArea(blockArea, j, jNext); // 1 x 3
		var(depth, "count_jNext", count_jNext);
		int count_jPair = (area == Area.HORIZONTAL)? countArea(blockArea, i, jPair): countArea(blockArea, j, jPair); // 1 x 3
		var(depth, "count_jPair", count_jPair);
		if((count_jNext == MIN) && (count_jPair == MIN))
			allowed = true;
		else {
			int iNext = (area == Area.HORIZONTAL)? pairException(i): pairException(j);
			var(depth, "iNext", iNext);
			int iPair = (area == Area.HORIZONTAL)? pairException(i, iNext): pairException(j, iNext);
			var(depth, "iPair", iPair);
			int exists_iNext = (area == Area.HORIZONTAL)? blockBase(existsArea(area, iNext, j, number, depth)):
				blockBase(existsArea(area, i, iNext, number, depth)); // 1 x 9
			var(depth, "exists_iNext", exists_iNext);
			int exists_iPair = (area == Area.HORIZONTAL)? blockBase(existsArea(area, iPair, j, number, depth)):
				blockBase(existsArea(area, i, iPair, number, depth)); // 1 x 9
			var(depth, "exists_iPair", exists_iPair);
			if(((exists_iNext == jNext) && (exists_iPair == jPair)) ||
			   ((exists_iNext == jPair) && (exists_iPair == jNext)))
				allowed = true;
			else {
				int count_iNext_jNext = countArea(blockArea, iNext, jNext); // 1 x 3
				var(depth, "count_iNext_jNext", count_iNext_jNext);
				int count_iNext_jPair = countArea(blockArea, iNext, jPair); // 1 x 3
				var(depth, "count_iNext_jPair", count_iNext_jPair);
				int count_iPair_jNext = countArea(blockArea, iPair, jNext); // 1 x 3
				var(depth, "count_iPair_jNext", count_iPair_jNext);
				int count_iPair_jPair = countArea(blockArea, iPair, jPair); // 1 x 3
				var(depth, "count_iPair_jPair", count_iPair_jPair);
				if((exists_iNext == NOT_FOUND) &&
				  (((exists_iPair == jNext) && (count_jPair == MIN) && (count_iPair_jPair == MIN)) ||
				   ((exists_iPair == jPair) && (count_jNext == MIN) && (count_iPair_jNext == MIN))))
					allowed = true;
				else if((exists_iPair == NOT_FOUND) &&
				  (((exists_iNext == jNext) && (count_jPair == MIN) && (count_iNext_jPair == MIN)) ||
				   ((exists_iNext == jPair) && (count_jNext == MIN) && (count_iNext_jNext == MIN))))
					allowed = true;
			}
		}
		var(depth, "allowed", allowed);
		return allowed;
	}

    // Set the missing number when
	// 1. all surrounding same numbers are present
	// 2. all surrounding 3 cell areas are blocked
    int setMissing(int row, int column, int number, int depth) {
		log(depth++, "setMissing(row = " + row + ", column = " + column + ", number = " + number + ")");
		int added = 0;
        if((s[row][column][FACE] == MISSING) && // empty?
          (s[row][column][number] == number) && // possible position?
		  (existsArea(Area.BLOCK, row, column, number, depth) == NOT_FOUND) && // 3 x 3
          (existsArea(Area.HORIZONTAL, row, column, number, depth) == NOT_FOUND) && // 1 x 9
          (existsArea(Area.VERTICAL, row, column, number, depth) == NOT_FOUND) && // 9 x 1
		  missing(Area.HORIZONTAL, row, column, number, depth) && // 2 x 9
		  missing(Area.VERTICAL, row, column, number, depth)) // 9 x 2
			added += setNumber(row, column, number);
		var(depth, "added", added);
		return added;
    }

	// Set the missing number if the cell is the only allowed position
	int setAllowed(int row, int column, int number, int depth) {
		log(depth++, "setAllowed(row = " + row + ", column = " + column + ", number = " + number + ")");
		int added = 0;
        if((s[row][column][FACE] == MISSING) && // empty?
          (s[row][column][number] == number)) { // possible position?
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
		}
		var(depth, "added", added);
		return added;
	}

    void solution() {
		int depth = 0;
		log(depth++, "solution()");
        startTime = System.nanoTime();
		String go = "";
		int counter = count(Area.ALL);
        int added = 0;
        while((counter < TOTAL) || (added > 0) || (goes.size() > 0)) {
            passages++;
            added = 0;
			if(goes.size() > 0) {
				go = goes.get(0);
				goes.remove(0);
			}
            for(int y = 0; y < MAX; y++) {
				if(go.length() > 0)
					y = go.charAt(0) - '0';
                for(int x = 0; x < MAX; x++) {
					if(go.length() > 1)
						x = go.charAt(1) - '0';
					printMissing(y, x);
					if(s[y][x][FACE] == MISSING) { // empty?
						for(int number = 1; number <= MAX; number++) {
							if(go.length() > 2)
								number = go.charAt(2) - '0';
							if(s[y][x][number] == number) { // possible position?
								if(isActive.contains("setSingle"))
									added += setSingle(y, x, number, depth);
								if(isActive.contains("setMissing"))
								   added += setMissing(y, x, number, depth);
								if(isActive.contains("setAllowed"))
									added += setAllowed(y, x, number, depth);
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
			if((counter == TOTAL) || (goes.size() == 0))
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
		String thin = "";
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
		boolean debugMemory = debug;
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
		debug = debugMemory;
    }

    void activate(String method) {
		if(isActive.size() == methods.length)
			isActive.clear();
		if(!isActive.contains(method))
			isActive.add(method);
	}

	public static void help() {
		System.out.println("Sudoku.java by Kostas Dimou @ 2019");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("    Sudoku [--analyze] [--debug] [--go YXN] [--help] [--interactive] \\");
		System.out.println("           [--method METHOD] [--solve] [--view]");
		System.out.println();
		System.out.println("Where:");
		System.out.println("    -a or --analyze:");
		System.out.println("        Displays the posible positions for each number.");
		System.out.println("    -d or --debug:");
		System.out.println("        Activates the debugging mode.");
		System.out.println("    -g or --go:");
		System.out.println("        Try a specific coordinate and/or a specific number.");
		System.out.println("        Example: --go 419: row = 4, column = 1, number = 9.");
		System.out.println("    -h or --help:");
		System.out.println("        Displays this help message and exits.");
		System.out.println("    -i or --interactive:");
		System.out.println("        Displays a prompt for each imput row and pauses on each passage.");
		System.out.println("        For each missing number you can provide a zero (0) or a space ( ).");
		System.out.println("        Example: ROW[0] = 57 9  1");
		System.out.println("        Example: ROW[1] = 01030005");
		System.out.println("        Example: ROW[3] =  2070 0 9");
		System.out.println("    -m or --method:");
		System.out.println("        Calls the equivalent method for solving the Sudoku.");
		System.out.println("        By default all methods are called.");
		System.out.println("        Available methods:");
		System.out.println("        1. setSingle");
		System.out.println("        2. setMissing");
		System.out.println("        3. setAllowed");
		System.out.println("    -s or --solve:");
		System.out.println("        Solves the Sudoku by using all possible methods.");
		System.out.println("    -v or --view:");
		System.out.println("        Displays the Sudoku matrix on every passage.");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("    java Sudoku --help");
		System.out.println("    java Sudoku --interactive");
		System.out.println("    java Sudoku < Sudoku.0002");
		System.out.println("    java Sudoku --analyze < Sudoku.0002");
		System.out.println("    java Sudoku --solve < Sudoku.0002");
		System.out.println("    java Sudoku -s --view < Sudoku.0002");
		System.out.println("    java Sudoku -s --go 419 --debug < Sudoku.0006");
		System.out.println("    java Sudoku -s --method setSingle < Sudoku.0000");
		System.out.println("    java Sudoku -s -m setSingle -m setMissing < Sudoku.0001");
	}

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
        String next = "";
        if(args.length > 0)
			for(String argument:args) {
				if(next.length() == 0) {
					if(argument.equals("-a") || argument.equals("--analyze"))
						sudoku.setAnalyze();
					if(argument.equals("-d") || argument.equals("--debug"))
						sudoku.setDebug();
					else if(argument.equals("-s") || argument.equals("--solve"))
						sudoku.setSolve();
					else if(argument.equals("-g") || argument.equals("--go"))
						next = argument;
					else if(argument.equals("-h") || argument.equals("--help")) {
						sudoku.help();
						return;
					}
					else if(argument.equals("-i") || argument.equals("--interactive"))
						sudoku.setInteractive();
					else if(argument.equals("-m") || argument.equals("--method"))
						next = argument;
					else if(argument.equals("-v") || argument.equals("--view"))
						sudoku.setView();
				} else
					if(next.equals("-g") || next.equals("--go")) {
						sudoku.setGo(argument);
						next = "";
					} else if(next.equals("-m") || next.equals("--method")) {
						sudoku.activate(argument);
						next = "";
					}
			}
        sudoku.run();
	}
}
