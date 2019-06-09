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
		set(y, x);
	}

	Position(Position position) {
		set(position.y, position.x);
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

	public static int getTotalNumbers() {
		return TOTAL;
	}

	public static int getTotalCandidates() {
		return TOTAL * TOTAL;
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

	public void set(int y, int x) {
		setY(y);
		setX(x);
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

	// Position coordinates comparison.
	boolean equals(int y, int x) {
		if(y == this.y)
			return (x == this.x);
		return false;
	}

	boolean equals(Position position) {
		if(position == null)
			return false;
		if(position == this)
			return true;
		return equals(position.y, position.x);
	}

	@Override
	public boolean equals(Object object) {
		return equals((Position)object);
	}

	// Returns the position of the container block
	Position base() {
		Position position = new Position(y / MIN * MIN, x / MIN * MIN);
		return position;
	}

	// Increments the position according to the given area
	Position forward(Area a, int step) {
		switch(a) {
			case BUCKET_HORIZONTAL:
				if(x + step < (x / MIN + 1) * MIN)
					x += step;
				else
					return null;
				break;
			case BUCKET_VERTICAL:
				if(y + step < (y / MIN + 1) * MIN)
					y += step;
				else
					return null;
				break;
			case DIAGONAL:
				if((y + step < MAX) && (x + step < MAX)) {
					y += step;
					x += step;
				} else
					return null;
				break;
			case HORIZONTAL:
				if(x + step < MAX)
					x += step;
				else
					return null;
				break;
			case VERTICAL:
				if(y + step < MAX)
					y += step;
				else
					return null;
				break;
			case BLOCK:
				if(x + step < (x / MIN + 1) * MIN)
					x += step;
				else
					if(y + step < (y / MIN + 1) * MIN) {
						x = x / MIN * MIN;
						y += step;
					} else
						return null;
				break;
			case ALL:
				if(x + step < MAX)
					x += step;
				else
					if(y + step < MAX) {
						x = 0;
						y += step;
					} else
						return null;
				break;
		}
		return this;
	}

	Position forward(Area a) {
		return forward(a, 1);
	}

	// Decrements the position according to the given area
	Position backward(Area a, int step) {
		switch(a) {
			case BUCKET_HORIZONTAL:
				if(x - step >= x / MIN * MIN)
					x -= step;
				else
					return null;
				break;
			case BUCKET_VERTICAL:
				if(y - step >= y / MIN * MIN)
					y -= step;
				else
					return null;
				break;
			case DIAGONAL:
				if((y - step >= 0) && (x - step >= 0)) {
					y -= step;
					x -= step;
				} else
					return null;
				break;
			case HORIZONTAL:
				if(x - step >= 0)
					x -= step;
				else
					return null;
				break;
			case VERTICAL:
				if(y - step >= 0)
					y -= step;
				else
					return null;
				break;
			case BLOCK:
				if(x - step >= x / MIN * MIN)
					x -= step;
				else
					if(y - step >= y / MIN * MIN) {
						x = (x / MIN) * MIN - step;
						y -= step;
					} else
						return null;

				break;
			case ALL:
				if(x - step >= 0)
					x -= step;
				else
					if(y - step >= 0) {
						x = MAX - step;
						y -= step;
					} else
						return null;
				break;
		}
		return this;
	}

	Position backward(Area a) {
		return backward(a, 1);
	}

	// Returns the adjacent position according to the given area, excluding the exception
	Position adjacent(Area a, int step, Position exception) {
		switch(a) {
			case HORIZONTAL:
			case VERTICAL:
				for(Position position = base(); position != null; position = position.forward(a, step)) {
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

	Position adjacent(Area a, int step) {
		return adjacent(a, step, null);
	}

	Position adjacent(Area a) {
		return adjacent(a, 1, null);
	}

	Position blockAdjacent(Area a, Position blockException) {
		return adjacent(a, Position.getMin(), blockException);
	}

	Position blockAdjacent(Area a) {
		return adjacent(a, Position.getMin(), null);
	}
}
