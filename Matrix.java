import java.util.ArrayList;
import java.io.PrintStream;

class Matrix {
	private static boolean ANALYZE;

	private Cell[][] cells;
	
	public Matrix(int max) {
		Position.setMax(max);
		ANALYZE = false;
		cells = new Cell[max][max]; // rows x columns
		for(Position position = new Position(0, 0); position != null; position = position.forward(Area.ALL))
			cells[position.getY()][position.getX()] = new Cell(position);
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
		// Debug.log("getCell(position = " + position + ")", depth);
		// depth++;
		if(position == null)
			return null;
		int max = position.getMax();
		return cells[position.getY()][position.getX()];
	}

	// Return the cells of the given area defined by the position omitting the exceptions.
	ArrayList<Cell> getCells(Area area, Position position, ArrayList<Position> exceptions, int depth) {
		// Debug.log("getCells(area = " + area + ", position = " + position + ", exceptions = " + exceptions + ")", depth);
		depth++;
		if(position == null)
			return null;
		Position target = null;
		switch(area) {
			case BUCKET_HORIZONTAL: target = new Position(position.getY(), position.base().getX()); break;
			case BUCKET_VERTICAL: target = new Position(position.base().getY(), position.getX()); break;
			case DIAGONAL: target = new Position(0, 0); break;
			case HORIZONTAL: target = new Position(position.getY(), 0); break;
			case VERTICAL: target = new Position(0, position.getX()); break;
			case BLOCK: target = new Position(position.base().getY(), position.base().getX()); break;
			case ALL: target = new Position(0, 0); break;
		}
		ArrayList<Cell> areaCells = new ArrayList<Cell>();
		for(;target != null; target = target.forward(area)) {
			if((exceptions != null) && exceptions.contains(target))
				continue;
			areaCells.add(getCell(target, depth));
		}
		// Debug.var("areaCells", areaCells, depth);
		return areaCells;
	}

	// NUMBER //

	// Sets the number and removes the equivalent candidates
	// from the block, the horizontal and vertical lines.
	void setNumber(Position position, int number, int depth) {
		Debug.log("setNumber(position = " + position + ", number = " + number + ")", depth);
		depth++;
		if(position != null) {
			cells[position.getY()][position.getX()].setNumber(number);
			if(number != Cell.MISSING) {
				ArrayList<Position> exceptions = new ArrayList<Position>();
				exceptions.add(position);
				removeCandidateIf(Area.HORIZONTAL, position, number, exceptions, depth);
				removeCandidateIf(Area.VERTICAL, position, number, exceptions, depth);
				removeCandidateIf(Area.BLOCK, position, number, exceptions, depth);
			}
		}
	}

	// Count the non-empty cells omitting the exception.
	int count(Area area, Position position, int exception, int depth) {
		Debug.log("count(area = " + area + ", position = " + position + ", exception = " + exception + ")", depth);
		depth++;
		int max = Position.getMax();
		int min = Position.getMin();
		int counter = 0;
		switch(area) {
			case BUCKET_HORIZONTAL:
				for(int j = 0, x = position.base().getX(); j < min; j++, x++)
					counter += cells[position.getY()][x].count(exception);
				break;
			case BUCKET_VERTICAL:
				for(int i = 0, y = position.base().getY(); i < min; i++, y++)
					counter += cells[y][position.getX()].count(exception);
				break;
			case DIAGONAL:
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
			case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						counter += cells[y][x].count(exception);
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
		Debug.log("isEmpty(position = " + position + ")", depth);
		depth++;
		return getCell(position, depth).isEmpty();
	}

	// Count the resolved numbers from all cells
	int countAllNumbers() {
		int counter = 0;
		for(Position position = new Position(0, 0); position != null; position = position.forward(Area.ALL))
			counter += getCell(position, Debug.NO_DEPTH).count();
		return counter;
	}

	// Checks if a number exists in the given area omitting the exceptions
	boolean existsIf(Area area, Position position, int number, ArrayList<Position> exceptions, int depth) {
		Debug.log("exists(area = " + area + ", position = " + position + ", number = " + number + ", exceptions = " + exceptions + ")", depth);
		depth++;
		if(number == 0)
			return false;
		for(Cell cell: getCells(area, position, exceptions, depth))
			if(cell.getNumber() == number)
				return true;
		return false;
	}

	// Checks if a number exists in the given area
	boolean exists(Area area, Position position, int number, int depth) {
		Debug.log("exists(area = " + area + ", position = " + position + ", number = " + number + ")", depth);
		depth++;
		return existsIf(area, position, number, new ArrayList<Position>(), depth);
	}

	// Checks if a number exists in the current block, horizontal and vertical lines.
	boolean exists(Position position, int number, int depth) {
		Debug.log("exists(positon = " + position + ", number = " + number + ")", depth);
		depth++;
		for(Area area: new ArrayList<Area>() {{add(Area.HORIZONTAL); add(Area.VERTICAL); add(Area.BLOCK);}})
			if(exists(area, position, number, depth))
				return true;
		return false;
	}

	// Checks if a cell is not empty (has a number assigned).
	boolean exists(Position position, int depth) {
		Debug.log("exists(position = " + position + ")", depth);
		depth++;
		return getCell(position, depth).exists();
	}
	
	// CANDIDATES //

	// Count the number of candidates of all cells
	int countAllCandidates() {
		int counter = 0;
		for(Position position = new Position(0, 0); position != null; position = position.forward(Area.ALL))
			counter += getCell(position, Debug.NO_DEPTH).getCandidates().size();
		return counter;
	}

	// Locates the cells which have atleast minimum same candidates and none different in the given area.
	ArrayList<Position> cleanMatchCandidates(Area area, Position position, ArrayList<Integer> candidates, int minimum, int depth) {
		Debug.log("cleanMatchCandidates(area = " + area + ", position = " + position + ", candidates = " + candidates + ", minimum = " + minimum + ")", depth);
		depth++;
		ArrayList<Position> matches = new ArrayList<Position>();
		for(Cell cell: getCells(area, position, new ArrayList<Position>(), depth)) {
			Position target = cell.getPosition();
			if(cell.cleanSubset(candidates, minimum))
				matches.add(target);
		}
		return matches;
	}

	// Locates the cells which have atleast minimum same candidates and maybe some different in the given area.
	ArrayList<Position> dirtyMatchCandidates(Area area, Position position, ArrayList<Integer> candidates, int minimum, ArrayList<Position> exceptions, int depth) {
		Debug.log("dirtyMatchCandidates(area = " + area + ", position = " + position + ", candidates = " + candidates + ", minimum = " + minimum + ", exceptions = " + exceptions + ")", depth);
		depth++;
		ArrayList<Cell> areaCells = getCells(area, position, exceptions, depth);
		ArrayList<Position> matches = new ArrayList<Position>();
		for(Cell cell: areaCells) {
			Position target = cell.getPosition();
			if((exceptions != null) && exceptions.contains(target))
				continue;
			if(cell.dirtySubset(candidates, minimum))
				matches.add(target);
		}
		return matches;
	}

	// Removes the candidates from the given area.
	ArrayList<Position> removeCandidateIf(Area area, Position position, int candidate, ArrayList<Position> exceptions, int depth) {
		Debug.log("removeCandidateIf(area = " + area + ", position = " + position + ", candidate = " + candidate + ", exceptions = " + exceptions + ")", depth);
		depth++;
		ArrayList<Cell> areaCells = getCells(area, position, exceptions, depth);
		ArrayList<Position> removed = new ArrayList<Position>();
		for(Cell cell: areaCells) {
			Position target = cell.getPosition();
			if(target.equals(position))
				continue;
			if((exceptions != null) && exceptions.contains(target))
				continue;
			if(cell.removeCandidate(candidate))
				removed.add(target);
		}
		return removed;
	}

	// Checks if the given number is a possible candidate for these coordinates.
	boolean existsCandidate(Position position, int candidate, int depth) {
		// Debug.log("existsCandidate(position = " + position + ", candidate = " + candidate + ")", depth);
		depth++;
		return getCell(position, depth).existsCandidate(candidate);
	}

	// Count the appearances of the given candidate in the area omitting the exceptions
	int countCandidateIf(Area area, Position position, int candidate, ArrayList<Position> exceptions, int depth) {
		Debug.log("countCandidateIf(area = " + area + ", position = " + position + ", candidate = " + candidate + ", exceptions = " + exceptions + ")", depth);
		depth++;
		int counter = 0;
		ArrayList<Cell> areaCells = getCells(area, position, exceptions, depth);
		for(Cell cell: areaCells)
			if(cell.existsCandidate(candidate))
				counter++;
		if(counter > 0)
			Debug.var("counter", counter, depth);
		return counter;
	}

	// PRINT //

	// Dilutes the given string (characters separated by spaces)
	public static String diluted(String dense) {
		String thin = "";
		for(int i = 0; i < dense.length(); i++) {
			if(i != 0)
				thin += " ";
			thin += dense.charAt(i);
		}
		return thin;
	}

	// Returns the name of the given area diluted.
	public static String diluted(Area area) {
		return diluted(area.name());
	}

	// Prints the cell information horizontally of the given area.
	void printCells(Area area, int depth) {
		// Debug.log("printCells(area = " + area + "exception = " + exception + ")", depth);
		ArrayList<Position> exceptions = new ArrayList<Position>();
		int max = Position.getMax();
		int min = Position.getMin();
		System.out.println();
		System.out.println(diluted(area));
		switch(area) {
			case BLOCK:
				for(int blockY = 0; blockY < min; blockY++)
					for(int blockX = 0; blockX < min; blockX++) {
						System.out.println();
						Position position = new Position(blockY * min, blockX * min);
						ArrayList<Cell> areaCells = getCells(area, position, exceptions, depth);
						for(Cell cell: areaCells)
							if(cell.isEmpty())
								cell.println();
					}
				break;
			case HORIZONTAL:
				for(int y = 0; y < max; y++) {
					System.out.println();
					Position position = new Position(y, 0);
					ArrayList<Cell> areaCells = getCells(area, position, exceptions, depth);
					for(Cell cell: areaCells)
						if(cell.isEmpty())
							cell.println();
				}
				break;
			case VERTICAL:
				for(int x = 0; x < max; x++) {
					System.out.println();
					Position position = new Position(0, x);
					ArrayList<Cell> areaCells = getCells(area, position, exceptions, depth);
					for(Cell cell: areaCells)
						if(cell.isEmpty())
							cell.println();
				}
				break;
		}
		System.out.println();
	}

	// Returns width characters.
	String repeat(String pattern, int width) {
		String format = "";
		for(int i = 0; i < width; i++)
			format += pattern;
		return format;
	}

	// Prints the top coordinates line.
	void printTopCoordinatesLine() {
		Position xAxis = new Position(0, 0);
		int max = xAxis.getMax();
		String line = repeat(" ", Coordinate.widthY(max) + 1);
		int min = xAxis.getMin();
		for(int x = 0; x < max; x++, xAxis.forward(Area.HORIZONTAL)) {
			if(x % min == 0)
				line += "  ";
			line += Coordinate.formattedX(xAxis.X(), max) + " "; // x cordinate
		}
		System.out.println(line);
	}

	// Prints the horizontal delimiter line.
	void printHorizontalDelimiter(String suffix) {
		Position xAxis = new Position(0, 0);
		int max = xAxis.getMax();
		String line = repeat(" ", Coordinate.widthY(max) + 1);
		int min = xAxis.getMin();
		for(int y = 0; y < min; y++)
			for(int x = 0; x < min; x++) {
				if(x % min == 0)
					line += "+-";
				line += repeat("-", Coordinate.widthX(max) + 1);
			}
		line += "+";
		System.out.println(line + suffix);
	}

	// Prints the whole matrix.
	void print() {
		System.out.println(); // blank line
		printTopCoordinatesLine();
		Position yAxis = new Position(0, 0);
		int max = yAxis.getMax();
		int min = yAxis.getMin();
		for(int y = 0; y < max; y++, yAxis.forward(Area.VERTICAL)) {
			if((y % min) == 0)
				printHorizontalDelimiter("");
			String line = Coordinate.formattedY(yAxis.Y(), max) + " "; // y coordinate
			for(int x = 0; x < max; x++) {
				if((x % min) == 0)
					line += "| ";
				line += Coordinate.formattedY(cells[y][x].formattedNumber(), max) + " "; // number
			}
			line += "|";
			System.out.println(line);
		}
		printHorizontalDelimiter(" (N=" + countAllNumbers() + ", C=" + countAllCandidates() + ")");
		System.out.println(); // blank line
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

	boolean verify(Position position, int depth) {
		Debug.log("verify(position = " + position + ")", depth);
		depth++;
		Cell cell = getCell(position, depth);
		ArrayList<Position> exceptions = new ArrayList<Position>() {{add(position);}};
		for(Area area: new ArrayList<Area>() {{add(Area.HORIZONTAL); add(Area.VERTICAL); add(Area.BLOCK);}})
			if(existsIf(area, position, cell.getNumber(), exceptions, depth)) {
				System.out.println("Number " + cell.getNumber() + " in position " + position + " is duplicate");
				return false;
			}
		return true;
	}

	// Verify that all cells have a valid mumber.
	boolean verifyAll(int depth) {
		Debug.log("verifyAll()", depth);
		depth++;
		boolean valid = true;
		for(Position position = new Position(0, 0); valid && (position != null); position = position.forward(Area.ALL))
			valid = verify(position, depth);
		System.out.println("VERIFIED");
		return valid;
	}
}
