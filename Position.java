import java.util.stream.IntStream;
import java.lang.Math;

class Position extends Print {
	private static final char ALPHA = 'A';
    private static final int NOT_FOUND = -1;
	private static final Coordinate coordinate = Coordinate.getInstance();

	private static int MAX = 9;
	private static int MIN = (int)Math.sqrt(MAX);

	private int y;
	private int x;

	Position() {
		y = NOT_FOUND;
		x = NOT_FOUND;
	}

	Position(int y, int x) {
		this.y = y;
		this.x = x;
	}

	Position(Position p) {
		y = p.y;
		x = p.x;
	}

	public static void setMax(int max) {
		MAX = max;
		MIN = (int)Math.sqrt(MAX);
	}

	public static int getMax() {
		return MAX;
	}

	public static int getMin() {
		return MIN;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	// Returns the y coordinate according to the internal coordinate format
	public String Y() {
		Integer i = y;
		switch(coordinate.getFormat()) {
			case JAVA: // Y=0-9, X=0-9
				return i.toString();
			case SUDOKU: // Y=A-I, X=1-9
				char c = ALPHA;
				c += y;
				return Character.toString(c);
			case CHESS: // Y=9-1, X=A-I
				i = MAX - i;
				return i.toString();
		}
		return EMPTY;
	}

	// Returns the x coordinate according to the internal coordinate format
	public String X() {
		Integer i = x;
		switch(coordinate.getFormat()) {
			case JAVA: // Y=0-9, X=0-9
				return i.toString();
			case SUDOKU: // Y=A-I, X=1-9
				return (++i).toString();
			case CHESS: // Y=9-1, X=A-I
				char c = ALPHA;
				c += x;
				return Character.toString(c);
		}
		return EMPTY;
	}

	// Prints the coordinates of the position according to the internal coordinate format
	void print(int depth) {
		margin(depth);
		System.out.print("[" + Y() + "," + X() + "]");
	}

	void println(int depth) {
		print(depth);
		System.out.println();
	}

	boolean equals(Position p) {
		if(p == null)
			return false;
		if(y == p.y)
			return (x == p.x);
		return false;
	}

	// Returns the position of the container block
	Position base() {
		Position p = new Position(y / MIN * MIN, x / MIN * MIN);
		return p;
	}

	// Increments the position according to the given area
	void plus(Area area) {
		Position p = null;
		switch(area) {
			case DIAGONAL:
				if((y < MAX) && (x < MAX)) {
					y++;
					x++;
				}
				break;
			case HORIZONTAL:
				if(x < MAX)
					x++;
				else
					if(y < MAX) {
						x = 0;
						y++;
					}
				break;
			case VERTICAL:
				if(y < MAX)
					y++;
				else
					if(x < MAX) {
						y = 0;
						x++;
					}
				break;
		}
	}

	// Decrements the position according to the given area
	void minus(Area area) {
		switch(area) {
			case DIAGONAL:
				if((y > 0) && (x > 0)) {
					y--;
					x--;
				}
				break;
			case HORIZONTAL:
				if(x > 0)
					x--;
				else
					if(y > 0) {
						x = MAX;
						y--;
					}
				break;
			case VERTICAL:
				if(y > 0)
					y--;
				else
					if(x > 0) {
						y = MAX;
						x--;
					}
				break;
		}
	}

	// Returns the adjacent position according to the given area, excluding the exception
	Position next(Area area, Position exception) {
		Position p = base();
		switch(area) {
			case HORIZONTAL:
			case VERTICAL:
				for(int i = 0; i < MIN; i++, p.plus(area)) {
					if(p.equals(this))
						continue;
					else if(p.equals(exception))
						continue;
					return p;
				}
				break;
		}
		return null;
	}

	Position next(Area area) {
		return next(area, null);
	}
}
