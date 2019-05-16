import java.util.stream.IntStream;

class Debug {
	private static boolean DEBUG = false;

	private Debug() {
	}

	static void set(boolean d) {
		DEBUG = d;
	}

	static boolean get() {
		return DEBUG;
	}

	// Prints the left margin of the call stack
	static void margin(int depth) {
		IntStream.iterate(1, i -> i + 1).limit(depth * 2).forEach(i -> { System.out.print(" "); });
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
