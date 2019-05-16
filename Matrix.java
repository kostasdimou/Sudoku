import java.util.ArrayList;

class Matrix {
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

	// NUMBER //

	// Sets the number and removes the equivalent candidates
	// from the block, the horizontal and vertical lines.
	int setNumber(Position p, int n, int depth) {
		cellList[p.getY()][p.getX()].setNumber(n);
		removeCandidate(Area.HORIZONTAL, p, n, depth);
		removeCandidate(Area.VERTICAL, p, n, depth);
		removeCandidate(Area.BLOCK, p, n, depth);
		return 1;
	}

	// Count the non-empty cells omitting the exception
    int count(Area a, int row, int column, int exception, int depth) {
		Debug.log("count(area = " + a + ", row = " + row + ", column = " + column + ", exception = " + exception + ")", depth++);
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
		Debug.var("counter", counter, depth);
        return counter;
    }

	// Counts the non-empty cells without exception
    int count(Area a, int y, int x, int depth) {
		return count(a, y, x, Cell.MISSING, depth);
	}

	// Counts the non-empty cells without exception
    int count(int y, int x) {
		return cellList[y][x].count();
	}

	// Checks if a cell is empty (has not a number yet).
    boolean isEmpty(int y, int x) {
		return cellList[y][x].isEmpty();
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
    	Debug.log("exists(area = " + a + ", row = " + row + ", column = " + column + ", number = " + number + ")", depth++);
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
		Debug.var("exists", exists, depth);
        return exists;
    }

	// Checks if a number exists in the current block, horizontal and vertical lines.
    boolean exists(int y, int x, int number, int depth) {
		boolean exists =
			(exists(Area.HORIZONTAL, y, x, number, depth) > Position.NOT_FOUND) ||
			(exists(Area.VERTICAL, y, x, number, depth) > Position.NOT_FOUND) ||
			(exists(Area.BLOCK, y, x, number, depth) > Position.NOT_FOUND);
		Debug.var("exists", exists, depth);
		return exists;
	}

	// Checks if a cell is not empty (has a number assigned).
    boolean exists(int y, int x) {
		return cellList[y][x].exists();
	}
	
	// CANDIDATES //

	// Returns the all candidates of a cell.
    ArrayList<Integer> getCandidates(int y, int x) {
		return cellList[y][x].getCandidates();
	}

	// Returns the candidate of a cell from the given index.
    int getCandidateAt(int y, int x, int index) {
		return cellList[y][x].getCandidateAt(0);
	}

	// Removes the candidates from the given area.
	Position findCandidate(Area a, Position p, ArrayList<Integer> sample, ArrayList<Position> exceptions, int depth) {
		Position b = p.base();
		int max = p.getMax();
		int min = p.getMin();
		switch(a) {
			case BLOCK:
				for(int i = 0, y = b.getY(); i < min; i++, y++)
					for(int j = 0, x = b.getX(); j < min; j++, x++)
						for(Position e: exceptions)
							if(!e.equals(y, x))
								if(cellList[y][x].equalsCandidates(sample))
									return new Position(y, x);
				break;
			case HORIZONTAL:
				for(int x = 0; x < max; x++)
					for(Position e: exceptions)
						if(!e.equals(p.getY(), x))
							if(cellList[p.getY()][x].equalsCandidates(sample))
								return new Position(p.getY(), x);
				break;
			case VERTICAL:
				for(int y = 0; y < max; y++)
					for(Position e: exceptions)
						if(!e.equals(y, p.getX()))
							if(cellList[y][p.getX()].equalsCandidates(sample))
								return new Position(y, p.getX());
				break;
			case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						for(Position e: exceptions)
							if(!e.equals(y, x))
								if(cellList[y][x].equalsCandidates(sample))
									return new Position(y, x);
				break;
		}
		return null;
	}

	// Removes the candidates from the given area.
	void removeCandidate(Area a, Position p, int c, ArrayList<Position> exceptions, int depth) {
		Position b = p.base();
		int max = p.getMax();
		int min = p.getMin();
		switch(a) {
			case POINT:
				cellList[p.getY()][p.getX()].removeCandidate(c);
				break;
			case BUCKET_HORIZONTAL:
				for(int j = 0, x = b.getX(); j < min; j++, x++)
					for(Position e: exceptions)
						if(!e.equals(p.getY(), x))
							cellList[p.getY()][x].removeCandidate(c);
				break;
			case BUCKET_VERTICAL:
				for(int i = 0, y = b.getY(); i < min; i++, y++)
					for(Position e: exceptions)
						if(!e.equals(y, p.getX()))
							cellList[y][p.getX()].removeCandidate(c);
				break;
			case BLOCK:
				for(int i = 0, y = b.getY(); i < min; i++, y++)
					for(int j = 0, x = b.getX(); j < min; j++, x++)
						for(Position e: exceptions)
							if(!e.equals(y, x))
								cellList[y][x].removeCandidate(c);
				break;
			case HORIZONTAL:
				for(int x = 0; x < max; x++)
					for(Position e: exceptions)
						if(!e.equals(p.getY(), x))
							cellList[p.getY()][x].removeCandidate(c);
				break;
			case VERTICAL:
				for(int y = 0; y < max; y++)
					for(Position e: exceptions)
						if(!e.equals(y, p.getX()))
							cellList[y][p.getX()].removeCandidate(c);
				break;
			case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						for(Position e: exceptions)
							if(!e.equals(y, x))
								cellList[y][x].removeCandidate(c);
				break;
		}
	}

	// Removes the candidates from the given area.
	void removeCandidate(Area a, Position p, int c, int depth) {
		Position none = new Position();
		ArrayList<Position> noExceptions = new ArrayList<Position>();
		noExceptions.add(none);
		removeCandidate(a, p, c, noExceptions, depth);
	}

	// Removes the candidates from the given area.
	void removeCandidate(Area a, int y, int x, int c, int depth) {
		Position p = new Position(y, x);
		removeCandidate(a, p, c, depth);
	}

	// Counts the candidates of a cell.
    int countCandidates(int y, int x) {
		return cellList[y][x].countCandidates();
	}
	
	// Checks if the given number ia a possible candidate for these coordinates.
	boolean existsCandidate(int y, int x, int number) {
		return cellList[y][x].existsCandidate(number);
	}

	// Count the non-empty cells omitting the exception
    int countCandidateIf(Area a, int row, int column, int candidate, int depth) {
		Debug.log("countCandidateIf(area = " + a + ", row = " + row + ", column = " + column + ", candidate = " + candidate + ")", depth++);
		Position p = new Position(row, column);
		int max = Position.getMax();
		int min = Position.getMin();
        int counter = 0;
        switch(a) {
            case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						if(cellList[y][x].existsCandidate(candidate))
							counter++;
				break;
            case HORIZONTAL:
                for(int x = 0; x < max; x++)
					if(cellList[row][x].existsCandidate(candidate))
						counter++;
                break;
            case VERTICAL:
                for(int y = 0; y < max; y++)
					if(cellList[y][column].existsCandidate(candidate))
						counter++;
                break;
            case BLOCK:
                for(int i = 0, y = p.base().getY(); i < min; i++, y++)
                    for(int j = 0, x = p.base().getX(); j < min; j++, x++)
						if(cellList[y][x].existsCandidate(candidate))
							counter++;
				break;
            case BUCKET_HORIZONTAL:
				for(int j = 0, x = p.base().getX(); j < min; j++, x++)
					if(cellList[row][x].existsCandidate(candidate))
						counter++;
				break;
            case BUCKET_VERTICAL:
                for(int i = 0, y = p.base().getY(); i < min; i++, y++)
					if(cellList[y][column].existsCandidate(candidate))
						counter++;
				break;
		}
		Debug.var("counter", counter, depth);
        return counter;
    }

	// PRINT //

	// Dilutes the given string (characters separated by spaces)
	String diluted(String dense) {
		String thin = "";
		for(int i = 0; i < dense.length(); i++) {
			if(i != 0)
				thin += " ";
			thin += dense.charAt(i);
		}
		return thin;
	}

	// Returns the name of the given area diluted.
	String diluted(Area area) {
		return diluted(area.name());
	}

	// Prints the cell information horizontally of the given area.
	void printCells(Area a, int depth) {
		// Debug.log("printCells(area = " + a + "exception = " + exception + ")", depth);
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
								if(c.isEmpty())
									c.println();
							}
					}
				break;
			case HORIZONTAL:
				for(int y = 0; y < max; y++) {
					System.out.println();
					for(int x = 0; x < max; x++, p.forward(a)) {
						c = cellList[y][x];
						if(c.isEmpty())
							c.println();
					}
				}
				break;
			case VERTICAL:
				for(int x = 0; x < max; x++) {
					System.out.println();
					for(int y = 0; y < max; y++, p.forward(a)) {
						c = cellList[y][x];
						if(c.isEmpty())
							c.println();
					}
				}
				break;
		}
		System.out.println();
	}

	// Auxiliary method
	// Prints the horizontal lines in the whole matrix print.
    void printHorizontalLine(String prefix, boolean showCounter) {
        System.out.print(prefix + " ");
		if(Coordinate.getFormat() == Coordinate.Format.ROWCOL)
			System.out.print(" ");
        System.out.print("+");
        for(int y = 0; y < Position.getMin(); y++) {
            for(int x = 0; x < Position.getMin(); x++) {
				System.out.print("--");
				if(Coordinate.getFormat() == Coordinate.Format.ROWCOL)
					System.out.print("-");
			}
			System.out.print("-+");
        }
        if(showCounter)
            System.out.print(" (" + countAll() + ")");
        System.out.println();
    }

	// Prints the whole matrix.
    void print() {
		System.out.println();
		Position xAxis = new Position(0, 0);
		System.out.print(" ");
		if(Coordinate.getFormat() == Coordinate.Format.ROWCOL)
			System.out.print(" ");
		for(int x = 0; x < Position.getMax(); x++, xAxis.forward(Area.HORIZONTAL)) {
			if(x % Position.getMin() == 0)
				System.out.print("  ");
			System.out.print(" " + xAxis.X()); // X coordinate
		}
		System.out.println();
		Position yAxis = new Position(0, 0);
        for(int y = 0; y < Position.getMax(); y++, yAxis.forward(Area.VERTICAL)) {
            if((y % Position.getMin()) == 0)
				printHorizontalLine(" ", false);
			System.out.print(yAxis.Y()); // Y coordinate
            for(int x = 0; x < Position.getMax(); x++) {
                if((x % Position.getMin()) == 0)
                    System.out.print(" |");
				System.out.print(" ");
				if(Coordinate.getFormat() == Coordinate.Format.ROWCOL)
					System.out.print(" ");
                if(cellList[y][x].exists())
                    System.out.print(cellList[y][x].getNumber()); // Number
				else
					System.out.print(" "); // Empty
            }
			System.out.println(" |");
        }
        printHorizontalLine(" ", true);
		if(analysis)
			analyze();
    }

	// Prints the candidates grouped by areas (block, horizontal, vertical).
    void analyze() {
		boolean memory = Debug.get();
		Debug.set(true);
		int depth = 0;
		System.out.println();
		System.out.println(diluted("ANALYSIS"));
		printCells(Area.BLOCK, depth);
		printCells(Area.HORIZONTAL, depth);
		printCells(Area.VERTICAL, depth);
		Debug.set(memory);
		System.out.println();
    }
}
