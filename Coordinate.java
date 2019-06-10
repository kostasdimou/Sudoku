public class Coordinate {
	private static final char ALPHA = 'A';
	private static final char ZERO = '0';

	// ENUMS //

	public enum Format {
		CHESS,  // Y=9-1, X=A-I
		JAVA,   // Y=0-9, X=0-9 (default)
		ROWCOL, // Y=R1-R9, X=C1-C9
		SUDOKU; // Y=A-I, X=1-9
	}

	// ATTRIBUTES //

	private static Format format = Format.JAVA;

	// CONSTRUCTORS //

	private Coordinate() {
	}

	// FORMAT //

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

	// VALID //

	// Check the given y coordinate for validity.
	//
	// CHESS : 9-1
	// JAVA  : 0-9
	// ROWCOL: R1-R9
	// SUDOKU: A-I
	//
	public static boolean validY(String y, int max) {
		// Check prefix R - ROWCOL
		int widthY = Coordinate.widthY(max);
		if(y.length() < widthY)
			return false;
		if(format == Format.ROWCOL) {
			if(y.charAt(0) != 'R')
				return false;
			y = y.substring(1);
		}
		// Letter -> Number SUDOKU
		if(y.charAt(0) >= ALPHA) {
			if(format != Format.SUDOKU)
				return false;
			String a2n = "";
			for(int i = 0; i < widthY; i++)
				a2n += (char)(y.charAt(i) - ALPHA + ZERO);
			y = a2n;
		}
		int n = Integer.parseInt(y);
		// Invert range - CHESS
		if(format == Format.CHESS)
			n = max - n;
		// Range shift - ROWCOL
		if(format == Format.ROWCOL)
			n--;
		if((n < 0) || (n >= max))
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
		int widthX = Coordinate.widthX(max);
		if(x.length() < widthX)
			return false;
		if(format == Format.ROWCOL) {
			if(x.charAt(0) != 'C')
				return false;
			x = x.substring(1);
		}
		// Letter -> Number CHESS
		if(x.charAt(0) >= ALPHA) {
			if(format != Format.CHESS)
				return false;
			String a2n = "";
			for(int i = 0; i < widthX; i++)
				a2n += (char)(x.charAt(i) - ALPHA + ZERO);
			x = a2n;
		}
		int n = Integer.parseInt(x);
		// Range shift - ROWCOL, SUDOKU
		if((format == Format.ROWCOL) || (format == Format.SUDOKU))
			n--;
		if((n < 0) || (n >= max))
			return false;
		return true;
	}

	// READ //

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

	// PRINT //

	// Returns the y according to the coordinate format.
	public static String Y(Integer y, int max) {
		String s = null;
		switch(format) {
			case CHESS: // Y=9-1, X=A-I
				y = max - y;
				s =  y.toString();
				break;
			case JAVA: // Y=0-9, X=0-9
				s =  y.toString();
				break;
			case ROWCOL: // Y=R1-R9, X=C1-C9
				s =  "R" + (++y).toString();
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
	public static String X(Integer x) {
		String s = null;
		switch(format) {
			case CHESS: // Y=9-1, X=A-I
				char c = ALPHA;
				c += x;
				s = Character.toString(c);
				break;
			case JAVA: // Y=0-9, X=0-9
				s = x.toString();
				break;
			case ROWCOL: // Y=R1-R9, X=C1-C9
				s = "C" + (++x).toString();
				break;
			case SUDOKU: // Y=A-I, X=1-9
				s = (++x).toString();
				break;
		}
		return s;
	}

	public static int digitsMax(Integer max) {
		String digits = max.toString();
		return  digits.length();
	}

	public static int lettersMax(Integer max) {
		return (max - 1) / 26 + 1;
	}

	// Returns the maximum width of the y according to the coordinate format.
	public static int widthY(int max) {
		switch(format) {
			case ROWCOL: // Y=R1-R9, X=C1-C9
				return 1 + digitsMax(max);
			case SUDOKU: // Y=A-I, X=1-9
				return lettersMax(max);
		}
		return digitsMax(max);
	}

	// Returns the maximum width of the x according to the coordinate format.
	public static int widthX(int max) {
		switch(format) {
			case CHESS: // Y=9-1, X=A-I
				return lettersMax(max);
			case ROWCOL: // Y=R1-R9, X=C1-C9
				 return 1 + digitsMax(max);
		}
		return digitsMax(max);
	}

	// Returns the formatted y according to the coordinate format.
	public static String formattedY(String y, int max) {
		String yFormat = "%";
		int width =  widthY(max);
		if(width > 1)
			yFormat += width;
		yFormat += "s";
		return String.format(yFormat, y);
	}

	// Returns the formatted x according to the coordinate format.
	public static String formattedX(String x, int max) {
		String xFormat = "%";
		int width =  widthX(max);
		if(width > 1)
			xFormat += width;
		xFormat += "s";
		return String.format(xFormat, x);
	}
}
