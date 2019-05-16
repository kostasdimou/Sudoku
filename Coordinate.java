public class Coordinate {
    private static final char ALPHA = 'A';

    public static final char ZERO = '0';

	public enum Format {
		CHESS, // Y=9-1, X=A-I
		JAVA, // Y=0-9, X=0-9 (default)
		ROWCOL, // Y=R1-R9, X=C1-C9
		SUDOKU; // Y=A-I, X=1-9
	}

	private static Format format = Format.JAVA;

    private Coordinate() {
	}

	public static boolean setFormat(Format s) {
		format = s;
		return true;
	}

	public static boolean setFormat(String name) {
		for(Format s: Format.values())
			if(name.equals(s.name())) {
				return setFormat(s);
			}
		System.out.println("Unsupported coordinate mode: " + name);
		return false;
	}

	public static Format getFormat() {
		return format;
	}

	// Check the given y coordinate for validity.
	//
	// CHESS : 9-1
	// JAVA  : 0-9
	// ROWCOL: R1-R9
	// SUDOKU: A-I
	//
	public static boolean validY(String y, int max) {
		// Check prefix R - ROWCOL
		if(format == Format.ROWCOL) {
			if(y.charAt(0) != 'R')
				return false;
			y = y.substring(1, y.length());
		}
		// Letter -> Number SUDOKU
		int i = 0;
		if(y.charAt(0) >= ALPHA) {
			if(format != Format.SUDOKU)
				return false;
			i = y.charAt(0) - ALPHA;
		} else
			i = Integer.valueOf(y);
		// Invert range - CHESS
		if(format == Format.CHESS)
			i = max - i;
		// Range shift - ROWCOL
		if(format == Format.ROWCOL)
			i--;
		if(i > max - 1)
			return false;
		return true;
	}

	// Check the given x coordinate for validity.
	//
	// CHESS : A-I
	// JAVA  : 0-9
	// ROWCOL: C1-C9
	// SUDOKU: 1-9
	//
	public static boolean validX(String x, int max) {
		// Check prefix C - ROWCOL
		if(format == Format.ROWCOL) {
			if(x.charAt(0) != 'C')
				return false;
			x = x.substring(1, x.length());
		}
		// Letter -> Number CHESS
		int i = 0;
		if(x.charAt(0) >= ALPHA) {
			if(format != Format.CHESS)
				return false;
			i = x.charAt(0) - ALPHA;
		} else
			i = Integer.valueOf(x);
		// Range shift - ROWCOL, SUDOKU
		if((format == Format.ROWCOL) || (format == Format.SUDOKU))
			i--;
		if(i > ZERO + max - 1)
			return false;
		return true;
	}

	// Converts the coordinate format row to the JAVA numbering (0..MAX-1).
	//
	// CHESS : 9-1
	// JAVA  : 0-9
	// ROWCOL: R1-R9
	// SUDOKU: A-I
	//
	public static int readY(String y, int max) {
		int i = 0;
		// Remove prefix R - ROWCOL
		if(format == Format.ROWCOL)
			y = y.substring(1, y.length());
		// Letter -> Number SUDOKU
		if(format == Format.SUDOKU)
			i = y.charAt(0) - ALPHA;
		else
			i = Integer.valueOf(y);
		// Invert range - CHESS
		if(format == Format.CHESS)
			i = max - i;
		// Range Shift - ROWCOL
		if(format == Format.ROWCOL)
			i--;
		return i;
	}

	// Converts the coordinate format column to the JAVA numbering (0..MAX-1).
	//
	// CHESS : A-I
	// JAVA  : 0-9
	// ROWCOL: C1-C9
	// SUDOKU: 1-9
	//
	public static int readX(String x) {
		int i = 0;
		// Check prefix C - ROWCOL
		if(format == Format.ROWCOL)
			x = x.substring(1, x.length());
		// Letter -> Number CHESS
		if(format == Format.CHESS)
			i = x.charAt(0) - ALPHA;
		else
			i = Integer.valueOf(x);
		// Range shift - ROWCOL, SUDOKU
		if((format == Format.ROWCOL) || (format == Format.SUDOKU))
			i--;
		return i;
	}

	// Returns the y according to the coordinate format.
	public static String Y(int y, int max) {
		String s = null;
		Integer i = y;
		switch(format) {
			case CHESS: // Y=9-1, X=A-I
				i = max - i;
				s =  i.toString();
				break;
			case JAVA: // Y=0-9, X=0-9
				s =  i.toString();
				break;
			case ROWCOL: // Y=R1-R9, X=C1-C9
				s =  "R" + (++i).toString();
				break;
			case SUDOKU: // Y=A-I, X=1-9
				char c = ALPHA;
				c += y;
				s =  Character.toString(c);
				break;
		}
		return s;
	}

	// Returns the x according to the coordinate format.
	public static String X(int x) {
		String s = null;
		Integer i = x;
		switch(format) {
			case CHESS: // Y=9-1, X=A-I
				char c = ALPHA;
				c += x;
				s = Character.toString(c);
				break;
			case JAVA: // Y=0-9, X=0-9
				s = i.toString();
				break;
			case ROWCOL: // Y=R1-R9, X=C1-C9
				s = "C" + (++i).toString();
				break;
			case SUDOKU: // Y=A-I, X=1-9
				s = (++i).toString();
				break;
		}
		return s;
	}
}
