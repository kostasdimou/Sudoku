import java.util.ArrayList;

class Matrix {
	private static boolean ANALYZE;

	private Cell[][] cells;
	
	public Matrix(int max) {
		Position.setMax(max);
		ANALYZE = false;
		cells = new Cell[max][max]; // rows x columns
		Position position = new Position(0, 0);
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++, position.forward(Area.HORIZONTAL))
				cells[y][x] = new Cell(position);
	}

	// Activates the ANALYZE mode.
	static void setAnalyze(boolean analyze) {
		ANALYZE = analyze;
	}

	// Returns the ANALYZE flag.
	boolean getAnalyze() {
		return ANALYZE;
	}

	// CELL //

	// Return the cell of the given coordinates.
	Cell getCell(Position position, int depth) {
		Debug.log("getCell(position = " + position + ")", depth++);
		if(position == null)
			return null;
		int max = position.getMax();
		return cells[position.getY()][position.getX()];
	}

	// Return the cells of the given area defined by the position.
	ArrayList<Cell> getCells(Area area, Position position, int depth) {
		Debug.log("getCells(area = " + area + ", position = " + position + ")", depth++);
		if(position == null)
			return null;
		ArrayList<Cell> areaCells = new ArrayList<Cell>();
		Position block = position.base();
		int max = position.getMax();
		int min = position.getMin();
		int minY = 0;
		int maxY = max;
		int minX = 0;
		int maxX = max;
		switch(area) {
			case BUCKET_HORIZONTAL:
				minY = position.getY();
				maxY = minY + 1;
				minX = block.getX();
				maxX = minX + min;
				break;
			case BUCKET_VERTICAL:
				minY = block.getY();
				maxY = minY + min;
				minX = position.getX();
				maxX = minX + 1;
				break;
			case HORIZONTAL:
				minY = position.getY();
				maxY = minY + 1;
				break;
			case VERTICAL:
				minX = position.getX();
				maxX = minX + 1;
				break;
			case BLOCK:
				minY = block.getY();
				maxY = minY + min;
				minX = block.getX();
				maxX = minX + min;
				break;
			case ALL:
				break;
		}
		for(int y = minY; y < maxY; y++)
			for(int x = minX; x < maxX; x++)
				areaCells.add(cells[y][x]);
		return areaCells;
	}

	// NUMBER //

	// Sets the number and removes the equivalent candidates
	// from the block, the horizontal and vertical lines.
	int setNumber(Position position, int number, int depth) {
		Debug.log("setNumber(position = " + position + ", number = " + number + ")", depth++);
		if(position == null)
			return 0;
		cells[position.getY()][position.getX()].setNumber(number);
		if(number != Cell.MISSING) {
			ArrayList<Position> exceptions = new ArrayList<Position>();
			exceptions.add(position);
			removeCandidateIf(Area.HORIZONTAL, position, number, exceptions, depth);
			removeCandidateIf(Area.VERTICAL, position, number, exceptions, depth);
			removeCandidateIf(Area.BLOCK, position, number, exceptions, depth);
		}
		return 1;
	}

	// Count the non-empty cells omitting the exception.
	int count(Area area, Position position, int exception, int depth) {
		Debug.log("count(area = " + area + ", position = " + position + ", exception = " + exception + ")", depth++);
		int max = Position.getMax();
		int min = Position.getMin();
		int counter = 0;
		switch(area) {
			case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						counter += cells[y][x].count(exception);
				break;
			case HORIZONTAL:
				for(int x = 0; x < max; x++)
					counter += cells[position.getY()][x].count(exception);
				break;
			case VERTICAL:
				for(int y = 0; y < max; y++)
					counter += cells[y][position.getX()].count(exception);
				break;
			case BLOCK:
				for(int i = 0, y = position.base().getY(); i < min; i++, y++)
					for(int j = 0, x = position.base().getX(); j < min; j++, x++)
						counter += cells[y][x].count(exception);
				break;
			case BUCKET_HORIZONTAL:
				for(int j = 0, x = position.base().getX(); j < min; j++, x++)
					counter += cells[position.getY()][x].count(exception);
				break;
			case BUCKET_VERTICAL:
				for(int i = 0, y = position.base().getY(); i < min; i++, y++)
					counter += cells[y][position.getX()].count(exception);
				break;
		}
		Debug.var("counter", counter, depth);
		return counter;
	}

	// Counts the non-empty cells without exception
	int count(Area area, Position position, int depth) {
		return count(area, position, Cell.MISSING, depth);
	}

	// Counts the non-empty cells without exception
	int count(Position position, int depth) {
		return getCell(position, depth).count();
	}

	// Checks if a cell is empty (has not a number yet).
	boolean isEmpty(Position position, int depth) {
		Debug.log("isEmpty(position = " + position + ")", depth++);
		return getCell(position, depth).isEmpty();
	}

	// Count all cells
	int countAll() {
		int max = Position.getMax();
		int counter = 0;
		for(int y = 0; y < max; y++)
			for(int x = 0; x < max; x++)
				counter += cells[y][x].count();
		return counter;
	}

	// Checks if a number exists in the given area
	int exists(Area area, Position position, int number, int depth) {
		Debug.log("exists(area = " + area + ", position = " + position + ", number = " + number + ")", depth++);
		int max = position.getMax();
		int exists = position.NOT_FOUND;
		if(number != 0)
			switch(area) {
				case ALL:
					for(int y = 0; y < Position.getMax(); y++)
						for(int x = 0; x < Position.getMax(); x++)
							if(cells[y][x].equals(number))
								exists = y;
					break;
				case BLOCK:
					int min = Position.getMin();
					for(int i = 0, y = position.base().getY(); i < Position.getMin(); i++, y++)
						for(int j = 0, x = position.base().getX(); j < Position.getMin(); j++, x++)
							if(cells[y][x].equals(number))
								exists = y;
					break;
				case HORIZONTAL:
					for(int x = 0; x < Position.getMax(); x++)
						if(cells[position.getY()][x].equals(number))
							exists = x;
					break;
				case VERTICAL:
					for(int y = 0; y < Position.getMax(); y++)
						if(cells[y][position.getX()].equals(number))
							exists = y;
					break;
			}
		return exists;
	}

	// Checks if a number exists in the current block, horizontal and vertical lines.
	boolean exists(Position position, int number, int depth) {
		Debug.log("exists(positon = " + position + ", number = " + number + ")", depth++);
		return
			(exists(Area.HORIZONTAL, position, number, depth) > Position.NOT_FOUND) ||
			(exists(Area.VERTICAL, position, number, depth) > Position.NOT_FOUND) ||
			(exists(Area.BLOCK, position, number, depth) > Position.NOT_FOUND);
	}

	// Checks if a cell is not empty (has a number assigned).
	boolean exists(Position position, int depth) {
		Debug.log("exists(position = " + position + ")", depth++);
		return getCell(position, depth).exists();
	}
	
	// CANDIDATES //

	// Returns the all candidates of a cell.
	ArrayList<Integer> getCandidates(Position position, int depth) {
		Debug.log("getCandidates(position = " + position + ")", depth++);
		return getCell(position, depth).getCandidates();
	}

	// Returns the candidate of a cell from the given index.
	int getCandidateAt(Position position, int index, int depth) {
		Debug.log("getCandidateAt(position = " + position + ", index = " + index + ")", depth++);
		return getCell(position, depth).getCandidateAt(0);
	}

	// Locates the cells which have atleast minimum same candidates and none different in the given area.
	ArrayList<Position> cleanMatchCandidates(Area area, Position position, ArrayList<Integer> numbers, int minimum, int depth) {
		Debug.log("cleanMatchCandidates(area = " + area + ", position = " + position + ", numbers = " + numbers + ", minimum = " + minimum + ")", depth++);
		ArrayList<Cell> areaCells = getCells(area, position, depth);
		ArrayList<Position> matches = new ArrayList<Position>();
		for(Cell cell: areaCells) {
			Debug.log("--> cell: " + cell, depth);
			Position target = cell.getPosition();
			if(cell.cleanSubset(numbers, minimum)) {
				Debug.log("--> clean subset: TRUE", depth);
				matches.add(target);
			}
		}
		return matches;
	}

	// Locates the cells which have atleast minimum same candidates and maybe some different in the given area.
	ArrayList<Position> dirtyMatchCandidates(Area area, Position position, ArrayList<Integer> numbers, int minimum, int depth) {
		Debug.log("dirtyMatchCandidates(area = " + area + ", position = " + position + ", numbers = " + numbers + ", minimum = " + minimum + ")", depth++);
		ArrayList<Cell> areaCells = getCells(area, position, depth);
		ArrayList<Position> matches = new ArrayList<Position>();
		for(Cell cell: areaCells) {
			Debug.log("--> cell: " + cell, depth);
			Position target = cell.getPosition();
			if(cell.dirtySubset(numbers, minimum)) {
				Debug.log("--> dirty subset: TRUE", depth);
				matches.add(target);
			}
		}
		return matches;
	}

	// Removes the candidates from the given area.
	void removeCandidateIf(Area area, Position position, int number, ArrayList<Position> exceptions, int depth) {
		Debug.log("removeCandidateIf(area = " + area + ", position = " + position + ", number = " + number + ", exceptions = " + exceptions + ")", depth++);
		ArrayList<Cell> areaCells = getCells(area, position, depth);
		for(Cell cell: areaCells) {
			Position target = cell.getPosition();
			if(target.equals(position))
				continue;
			if(exceptions.contains(target))
				continue;
			cell.removeCandidate(number);
		}
	}

	// Counts the candidates of a cell.
	int countCandidates(Position position, int depth) {
		Debug.log("countCandidate(position = " + position + ")", depth++);
		return getCell(position, depth).countCandidates();
	}
	
	// Checks if the given number ia a possible candidate for these coordinates.
	boolean existsCandidate(Position position, int number, int depth) {
		Debug.log("existsCandidate(position = " + position + ", number = " + number + ")", depth++);
		return getCell(position, depth).existsCandidate(number);
	}

	// Count the non-empty cells omitting the exception
	int countCandidateIf(Area area, Position position, int candidate, int depth) {
		Debug.log("countCandidateIf(area = " + area + ", position = " + position + ", candidate = " + candidate + ")", depth++);
		int max = Position.getMax();
		int min = Position.getMin();
		int counter = 0;
		switch(area) {
			case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						if(cells[y][x].existsCandidate(candidate))
							counter++;
				break;
			case HORIZONTAL:
				for(int x = 0; x < max; x++)
					if(cells[position.getY()][x].existsCandidate(candidate))
						counter++;
				break;
			case VERTICAL:
				for(int y = 0; y < max; y++)
					if(cells[y][position.getX()].existsCandidate(candidate))
						counter++;
				break;
			case BLOCK:
				for(int i = 0, y = position.base().getY(); i < min; i++, y++)
					for(int j = 0, x = position.base().getX(); j < min; j++, x++)
						if(cells[y][x].existsCandidate(candidate))
							counter++;
				break;
			case BUCKET_HORIZONTAL:
				for(int j = 0, x = position.base().getX(); j < min; j++, x++)
					if(cells[position.getY()][x].existsCandidate(candidate))
						counter++;
				break;
			case BUCKET_VERTICAL:
				for(int i = 0, y = position.base().getY(); i < min; i++, y++)
					if(cells[y][position.getX()].existsCandidate(candidate))
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
	void printCells(Area area, int depth) {
		// Debug.log("printCells(area = " + area + "exception = " + exception + ")", depth);
		Position position = new Position(0, 0);
		int max = Position.getMax();
		int min = Position.getMin();
		Cell cell = null;
		System.out.println();
		System.out.println(diluted(area));
		switch(area) {
			case BLOCK:
				for(int blockY = 0; blockY < min; blockY++)
					for(int blockX = 0; blockX < min; blockX++) {
						System.out.println();
						for(int y = 0; y < min; y++)
							for(int x = 0; x < min; x++, position.forward(Area.HORIZONTAL)) {
								cell = cells[blockY * min + y][blockX * min + x];
								if(cell.isEmpty())
									cell.println();
							}
					}
				break;
			case HORIZONTAL:
				for(int y = 0; y < max; y++) {
					System.out.println();
					for(int x = 0; x < max; x++, position.forward(area)) {
						cell = cells[y][x];
						if(cell.isEmpty())
							cell.println();
					}
				}
				break;
			case VERTICAL:
				for(int x = 0; x < max; x++) {
					System.out.println();
					for(int y = 0; y < max; y++, position.forward(area)) {
						cell = cells[y][x];
						if(cell.isEmpty())
							cell.println();
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
				if(cells[y][x].exists())
					System.out.print(cells[y][x].getNumber()); // Number
				else
					System.out.print(" "); // Empty
			}
			System.out.println(" |");
		}
		printHorizontalLine(" ", true);
		if(ANALYZE)
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
