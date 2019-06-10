import java.util.stream.IntStream;

class Debug {
	public static final int NO_DEPTH = -1;

	// ATTRIBUTES //

	private static boolean DEBUG = false;

	// CONSTRUCTORS //

	private Debug() {
	}

	// DEBUG //

	static void set(boolean debug) {
		DEBUG = debug;
	}

	static boolean get() {
		return DEBUG;
	}

	// PRINT //

	// Prints the left margin of the call stack
	static void printMargin(int depth) {
		IntStream.iterate(1, i -> i + 1).limit(depth * 2).forEach(i -> { System.out.print(" "); });
	}

	// Displays debugging information.
	// Used for displaying the method calls.
	public static void log(String message, int depth) {
		if(!DEBUG)
			return;
		if(depth == NO_DEPTH)
			return;
		printMargin(depth);
		System.out.println(message);
	}

	// Displays debugging information.
	// Used for displaying the variable values.
	public static void var(String name, Object value, int depth) {
		if(!DEBUG)
			return;
		if(depth == NO_DEPTH)
			return;
		String message = name + " = " + value;
		log(message, depth);
	}
}
