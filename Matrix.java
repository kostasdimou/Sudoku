class Matrix extends Print {
	private static final Coordinate coordinate = Coordinate.getInstance();

	private static boolean analysis;

	private Cell[][] cellList;
	
	public Matrix(int max) {
		Position.setMax(max);
		analysis = false;
		cellList = new Cell[max][max]; // rows x columns
		Position p = new Position(0, 0);
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, p.forward(Area.HORIZONTAL))
				cellList[y][x] = new Cell(p);
	}

	// Activates the analysis mode.
	static void setAnalysis(boolean a) {
		analysis = a;
	}

	// Returns the analysis flag.
	boolean getAnalysis() {
		return analysis;
	}

	// Return the cell of the given position.
	Cell cell(Position p) {
		return cellList[p.getY()][p.getX()];
	}

	// Return the cell of the given coordinates.
	Cell cell(int y, int x) {
		int max = Position.getMax();
		if((y < 0) || (y >= max) || (x < 0) || (x >= max))
			return null;
		return cellList[y][x];
	}

	// Sets the number and removes the equivalent candidates
	// from the block, the horizontal and vertical lines.
	int setNumber(Position p, int n) {
		cellList[p.getY()][p.getX()].setNumber(n);
		removeCandidate(p, Area.HORIZONTAL, n);
		removeCandidate(p, Area.VERTICAL, n);
		removeCandidate(p, Area.BLOCK, n);
		return 1;
	}

	// Removes the candidates from the given area.
	void removeCandidate(Position p, Area a, int c) {
		Position b = p.base();
		int max = p.getMax();
		int min = p.getMin();
		switch(a) {
			case POINT:
				cellList[p.getY()][p.getX()].removeCandidate(c);
				break;
			case BUCKET_HORIZONTAL:
				for(int j = 0, x = b.getX(); j < min; j++, x++)
					cellList[p.getY()][x].removeCandidate(c);
				break;
			case BUCKET_VERTICAL:
				for(int i = 0, y = b.getY(); i < min; i++, y++)
					cellList[y][p.getX()].removeCandidate(c);
				break;
			case BLOCK:
				for(int i = 0, y = b.getY(); i < min; i++, y++)
					for(int j = 0, x = b.getX(); j < min; j++, x++)
						cellList[y][x].removeCandidate(c);
				break;
			case HORIZONTAL:
				for(int x = 0; x < max; x++)
					cellList[p.getY()][x].removeCandidate(c);
				break;
			case VERTICAL:
				for(int y = 0; y < max; y++)
					cellList[y][p.getX()].removeCandidate(c);
				break;
			case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						cellList[y][x].removeCandidate(c);
				break;
		}
	}

	// Counts the candidates of a cell.
    int candidateCount(int row, int column) {
		return cellList[row][column].candidateCount();
	}
	
	// Returns the first candidate of a cell.
    int getFirstCandidate(int row, int column) {
		return cellList[row][column].getFirstCandidate();
	}

	// Count the non-empty cells omitting the exception
    int count(Area a, int row, int column, int exception, int depth) {
		log("count(area = " + a + ", row = " + row + ", column = " + column + ", exception = " + exception + ")", depth++);
		Position p = new Position(row, column);
		int max = Position.getMax();
		int min = Position.getMin();
        int counter = 0;
        switch(a) {
            case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						counter += cellList[y][x].count(exception);
				break;
            case HORIZONTAL:
                for(int x = 0; x < max; x++)
					counter += cellList[row][x].count(exception);
                break;
            case VERTICAL:
                for(int y = 0; y < max; y++)
					counter += cellList[y][column].count(exception);
                break;
            case BLOCK:
                for(int i = 0, y = p.base().getY(); i < min; i++, y++)
                    for(int j = 0, x = p.base().getX(); j < min; j++, x++)
						counter += cellList[y][x].count(exception);
				break;
            case BUCKET_HORIZONTAL:
				for(int j = 0, x = p.base().getX(); j < min; j++, x++)
					counter += cellList[row][x].count(exception);
				break;
            case BUCKET_VERTICAL:
                for(int i = 0, y = p.base().getY(); i < min; i++, y++)
					counter += cellList[y][column].count(exception);
				break;
		}
		var("counter", counter, depth);
        return counter;
    }

	// Counts the non-empty cells without exception
    int count(Area a, int row, int column, int depth) {
		return count(a, row, column, Cell.MISSING, depth);
	}

	// Counts the non-empty cells without exception
    int count(int row, int column) {
		return cellList[row][column].count();
	}

	// Checks if a cell is empty (has not a number yet).
    boolean empty(int row, int column) {
		return cellList[row][column].empty();
	}

	// Count all cells
    int countAll() {
		int max = Position.getMax();
        int counter = 0;
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++)
				counter += cellList[y][x].count();
        return counter;
	}

	// Checks if a number exists in the given area
    int exists(Area a, int row, int column, int number, int depth) {
    	log("exists(area = " + a + ", row = " + row + ", column = " + column + ", number = " + number + ")", depth++);
		int max = Position.getMax();
		int exists = Position.NOT_FOUND;
		if(number != 0)
			switch(a) {
				case ALL:
					for(int y = 0; y < Position.getMax(); y++)
						for(int x = 0; x < Position.getMax(); x++)
							if(cellList[y][x].equals(number))
								exists = y;
					break;
				case BLOCK:
					Position p = new Position(row, column);
					int min = Position.getMin();
					for(int i = 0, y = p.base().getY(); i < Position.getMin(); i++, y++)
						for(int j = 0, x = p.base().getX(); j < Position.getMin(); j++, x++)
							if(cellList[y][x].equals(number))
								exists = y;
					break;
				case HORIZONTAL:
					for(int x = 0; x < Position.getMax(); x++)
						if(cellList[row][x].equals(number))
							exists = x;
					break;
				case VERTICAL:
					for(int y = 0; y < Position.getMax(); y++)
						if(cellList[y][column].equals(number))
							exists = y;
					break;
			}
		var("exists", exists, depth);
        return exists;
    }

	// Checks if a number exists in the current block, horizontal and vertical lines.
    boolean exists(int row, int column, int number, int depth) {
    	// log("exists(row = " + row + ", column = " + column + ", number = " + number + ")", depth++);
		boolean exists =
			(exists(Area.HORIZONTAL, row, column, number, depth) > Position.NOT_FOUND) ||
			(exists(Area.VERTICAL, row, column, number, depth) > Position.NOT_FOUND) ||
			(exists(Area.BLOCK, row, column, number, depth) > Position.NOT_FOUND);
		var("exists", exists, depth);
		return exists;
	}

	// Checks if a cell is not empty (has a number assigned).
    boolean exists(int row, int column, int depth) {
    	// log("exists(row = " + row + ", column = " + column + ")", depth++);
		return cellList[row][column].exists();
	}

	// Checks if the given number ia a possible candidate for these coordinates.
	boolean candidateExists(int row, int column, int number, int depth) {
    	// log("candidateExists(row = " + row + ", column = " + column + ", number = " + number + ")", depth++);
		return cellList[row][column].candidateExists(number);
	}

	// Count the non-empty cells omitting the exception
    int candidateCountIf(Area a, int row, int column, int candidate, int depth) {
		log("candidateCountIf(area = " + a + ", row = " + row + ", column = " + column + ", candidate = " + candidate + ")", depth++);
		Position p = new Position(row, column);
		int max = Position.getMax();
		int min = Position.getMin();
        int counter = 0;
        switch(a) {
            case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						if(cellList[y][x].candidateExists(candidate))
							counter++;
				break;
            case HORIZONTAL:
                for(int x = 0; x < max; x++)
					if(cellList[row][x].candidateExists(candidate))
						counter++;
                break;
            case VERTICAL:
                for(int y = 0; y < max; y++)
					if(cellList[y][column].candidateExists(candidate))
						counter++;
                break;
            case BLOCK:
                for(int i = 0, y = p.base().getY(); i < min; i++, y++)
                    for(int j = 0, x = p.base().getX(); j < min; j++, x++)
						if(cellList[y][x].candidateExists(candidate))
							counter++;
				break;
            case BUCKET_HORIZONTAL:
				for(int j = 0, x = p.base().getX(); j < min; j++, x++)
					if(cellList[row][x].candidateExists(candidate))
						counter++;
				break;
            case BUCKET_VERTICAL:
                for(int i = 0, y = p.base().getY(); i < min; i++, y++)
					if(cellList[y][column].candidateExists(candidate))
						counter++;
				break;
		}
		var("counter", counter, depth);
        return counter;
    }

	// Prints the candidate numbers of the given area.
	void printCells(Area a, int depth) {
		// log("printCells(area = " + a + "exception = " + exception + ")", depth);
		Position p = new Position(0, 0);
		int max = Position.getMax();
		int min = Position.getMin();
		Cell c = null;
		System.out.println();
		System.out.println(diluted(a));
		switch(a) {
			case BLOCK:
				for(int blockY = 0; blockY < min; blockY++)
					for(int blockX = 0; blockX < min; blockX++) {
						System.out.println();
						for(int y = 0; y < min; y++)
							for(int x = 0; x < min; x++, p.forward(Area.HORIZONTAL)) {
								c = cellList[blockY * min + y][blockX * min + x];
								if(c.empty())
									c.print(depth);
							}
					}
				break;
			case HORIZONTAL:
				for(int y = 0; y < max; y++) {
					System.out.println();
					for(int x = 0; x < max; x++, p.forward(a)) {
						c = cellList[y][x];
						if(c.empty())
							c.print(depth);
					}
				}
				break;
			case VERTICAL:
				for(int x = 0; x < max; x++) {
					System.out.println();
					for(int y = 0; y < max; y++, p.forward(a)) {
						c = cellList[y][x];
						if(c.empty())
							c.print(depth);
					}
				}
				break;
		}
		System.out.println();
	}

	// Auxiliary method
	// Prints the horizontal lines in the whole matrix print.
    void printHorizontalLine(char prefix, boolean showCounter) {
        System.out.print(prefix);
        System.out.print(SPACE);
		if(coordinate.getFormat() == Coordinate.Format.ROWCOL)
			System.out.print(SPACE);
        System.out.print(PLUS);
        for(int y = 0; y < Position.getMin(); y++) {
            for(int x = 0; x < Position.getMin(); x++) {
				System.out.print(DASH);
				System.out.print(DASH);
				if(coordinate.getFormat() == Coordinate.Format.ROWCOL)
					System.out.print(DASH);
			}
			System.out.print(DASH);
			System.out.print(PLUS);
        }
        if(showCounter)
            System.out.print(" (" + countAll() + ")");
        System.out.println();
    }

	// Prints the whole matrix.
    void print() {
		System.out.println();
		Position xAxis = new Position(0, 0);
		System.out.print(SPACE);
		if(coordinate.getFormat() == Coordinate.Format.ROWCOL)
			System.out.print(SPACE);
		for(int x = 0; x < Position.getMax(); x++, xAxis.forward(Area.HORIZONTAL)) {
			if(x % Position.getMin() == 0) {
				System.out.print(SPACE);
				System.out.print(SPACE);
			}
			System.out.print(SPACE);
			System.out.print(xAxis.X()); // X coordinate
		}
		System.out.println();
		Position yAxis = new Position(0, 0);
        for(int y = 0; y < Position.getMax(); y++, yAxis.forward(Area.VERTICAL)) {
            if((y % Position.getMin()) == 0)
				printHorizontalLine(SPACE, false);
			System.out.print(yAxis.Y()); // Y coordinate
            for(int x = 0; x < Position.getMax(); x++) {
                if((x % Position.getMin()) == 0) {
                    System.out.print(SPACE);
                    System.out.print(PIPE);
				}
				System.out.print(SPACE);
				if(coordinate.getFormat() == Coordinate.Format.ROWCOL)
					System.out.print(SPACE);
                if(cellList[y][x].exists()) {
                    System.out.print(cellList[y][x].getNumber()); // Number
				} else {
					System.out.print(SPACE); // Empty
				}
            }
            System.out.print(SPACE);
            System.out.print(PIPE);
			System.out.println();
        }
        printHorizontalLine(SPACE, true);
		if(analysis)
			analyze();
    }

	// Prints the candidates grouped by areas (block, horizontal, vertical).
    void analyze() {
		boolean memory = Print.getDebug();
		Print.setDebug(true);
		int depth = 0;
		System.out.println();
		System.out.println(diluted("ANALYSIS"));
		printCells(Area.BLOCK, depth);
		printCells(Area.HORIZONTAL, depth);
		printCells(Area.VERTICAL, depth);
		Print.setDebug(memory);
		System.out.println();
    }
}
