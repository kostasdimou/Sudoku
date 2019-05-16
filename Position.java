import java.util.stream.IntStream;
import java.lang.Math;

class Position {
    public static final int NOT_FOUND = -1;

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
		return Coordinate.Y(y, MAX);
	}

	// Returns the x coordinate according to the internal coordinate format
	public String X() {
		return Coordinate.X(x);
	}

	public String toString() {
		return "[" + Y() + "," + X() + "]";
	}

	// Prints the coordinates of the position according to the internal coordinate format
	void println() {
		System.out.println(toString());
	}

	boolean equals(int row, int column) {
		if(row == y)
			return (column == x);
		return false;
	}

	boolean equals(Position p) {
		if(p == null)
			return false;
		return equals(p.y, p.x);
	}

	// Returns the position of the container block
	Position base() {
		Position p = new Position(y / MIN * MIN, x / MIN * MIN);
		return p;
	}

	// Increments the position according to the given area
	void forward(Area a, int step) {
		switch(a) {
			case DIAGONAL:
				if((y + step < MAX) && (x + step < MAX)) {
					y += step;
					x += step;
				}
				break;
			case HORIZONTAL:
				if(x + step < MAX)
					x += step;
				else
					if(y + step < MAX) {
						x = 0;
						y += step;
					}
				break;
			case VERTICAL:
				if(y + step < MAX)
					y += step;
				else
					if(x + step < MAX) {
						y = 0;
						x += step;
					}
				break;
		}
	}

	void forward(Area a) {
		forward(a, 1);
	}

	// Decrements the position according to the given area
	void backward(Area a, int step) {
		switch(a) {
			case DIAGONAL:
				if((y - step >= 0) && (x - step >= 0)) {
					y -= step;
					x -= step;
				}
				break;
			case HORIZONTAL:
				if(x - step >= 0)
					x -= step;
				else
					if(y - step >= 0) {
						x = MAX;
						y -= step;
					}
				break;
			case VERTICAL:
				if(y - step >= 0)
					y -= step;
				else
					if(x - step >= 0) {
						y = MAX;
						x -= step;
					}
				break;
		}
	}

	void backward(Area a) {
		backward(a, 1);
	}

	// Returns the adjacent position according to the given area, excluding the exception
	Position next(Area a, int step, Position exception) {
		Position p = base();
		switch(a) {
			case HORIZONTAL:
			case VERTICAL:
				for(int i = 0; i < MIN * step; i++, p.forward(a, step)) {
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

	Position next(Area a, int step) {
		return next(a, step, null);
	}

	Position next(Area a) {
		return next(a, 1, null);
	}

	Position blockNext(Area a, Position blockException) {
		return next(a, Position.getMin(), blockException);
	}

	Position blockNext(Area a) {
		return next(a, Position.getMin(), null);
	}
}
