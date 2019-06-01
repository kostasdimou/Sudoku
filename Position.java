import java.util.stream.IntStream;
import java.lang.Math;
import java.lang.IllegalArgumentException;

class Position {
	public static final int NOT_FOUND = -1;

	private static int MAX = 9;
	private static int MIN = (int)Math.sqrt(MAX);
	private static int TOTAL = MAX * MAX;

	private int y;
	private int x;

	Position() {
		y = NOT_FOUND;
		x = NOT_FOUND;
	}

	Position(int y, int x) {
		setY(y);
		setX(x);
	}

	Position(Position position) {
		setY(position.y);
		setX(position.x);
	}

	public static void setMax(int max) {
		MAX = max;
		MIN = (int)Math.sqrt(MAX);
		TOTAL = MAX * MAX;
	}

	public static int getMax() {
		return MAX;
	}

	public static int getMin() {
		return MIN;
	}

	public static int getTotal() {
		return TOTAL;
	}

	// Checks the range of the coordinate.
	boolean valid(int i) {
		if((i != NOT_FOUND) && ((i < 0) || (i >= MAX)))
			return false;
		return true;
	}

	// Checks the range of the coordinates.
	boolean valid(int y, int x) {
		if(valid(y))
			return valid(x);
		return false;
	}

	public void setY(int y) throws IllegalArgumentException {
		if(!valid(y))
			throw new IllegalArgumentException();
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) throws IllegalArgumentException {
		if(!valid(x))
			throw new IllegalArgumentException();
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

	// Position comparison.
	boolean equals(int y, int x) {
		if(y == this.y)
			return (x == this.x);
		return false;
	}

	// Overriding Object.equals.
	boolean equals(Position x) {
		if(x == null)
			return false;
		if(x == this)
			return true;
		return equals(x.y, x.x);
	}

	// Returns the position of the container block
	Position base() {
		Position position = new Position(y / MIN * MIN, x / MIN * MIN);
		return position;
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
		Position position = base();
		switch(a) {
			case HORIZONTAL:
			case VERTICAL:
				for(int i = 0; i < MIN * step; i++, position.forward(a, step)) {
					if(position.equals(this))
						continue;
					else if(position.equals(exception))
						continue;
					return position;
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
