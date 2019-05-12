import java.util.stream.IntStream;

class Print {
    public static final char DASH = '-';
    public static final char PIPE = '|';
    public static final char PLUS = '+';
    public static final char SPACE = ' ';

	private static boolean DEBUG = false;

	static void setDebug(boolean d) {
		DEBUG = d;
	}

	static boolean getDebug() {
		return DEBUG;
	}

	// Dilutes the given string (characters separated by spaces)
	String diluted(String dense) {
		String thin = "";
		for(int i = 0; i < dense.length(); i++) {
			if(i != 0)
				thin += SPACE;
			thin += dense.charAt(i);
		}
		return thin;
	}

	// Returns the name of the given area diluted.
	String diluted(Area area) {
		return diluted(area.name());
	}

	// Prints the left margin of the call stack
	static void margin(int depth) {
		IntStream.iterate(1, i -> i + 1).limit(depth * 2).forEach(i -> { System.out.print(SPACE); });
	}

	// Displays debugging information.
	// Used for displaying the method calls.
	public static void log(String message, int depth) {
		if(DEBUG) {
			margin(depth);
			System.out.println(message);
		}
	}

	// Displays debugging information.
	// Used for displaying the variable values.
	public static void var(String name, Object value, int depth) {
		String message = name + " = " + value;
		log(message, depth);
	}
}
