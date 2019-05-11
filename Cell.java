import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.lang.IllegalArgumentException;

class Cell extends Print {
    public static final int MISSING = 0;

	private static final String[] MULTIPLE = {"NOTHING", "SINGLE", "PAIR", "TRIPLE"};

	private int number;
	private List<Integer> candidates;

	Cell(int max) {
		number = MISSING;
		candidates = allNumbers(max);
	}

	Cell(int n, int max) {
		number = n;
		candidates = allNumbers(max);
	}

	private static final List<Integer> allNumbers(int max) {
		return Stream.iterate(1, n -> n + 1).limit(max).collect(Collectors.toList());
	}

	boolean equals(Cell c) {
		return (number == c.number);
	}

	void removeCandidate(int c) {
		int index = candidates.indexOf(c);
		if(index >= 0)
			candidates.remove(index);
	}

	void setNumber(int n) {
		if(number != MISSING)
			throw new IllegalArgumentException(Integer.toString(n));
		number = n;
		candidates.clear();
	}

	void print(int depth, Position p, Coordinate c) {
		margin(depth);
		System.out.print("Cell");
		p.print(0);
		System.out.print(EMPTY + SPACE + number);
	}

	void println(int depth, Position p, Coordinate c) {
		print(depth, p, c);
		System.out.println();
	}

	void printMissing(int depth, Position p) {
		if(candidates.size() > 0) {
			System.out.print("Missing");
			p.print(0);
			int counter = 0;
			for(int n = 1; n <= p.getMax(); n++) {
				System.out.print(SPACE);
				if(candidates.indexOf(n) >= 0) {
					System.out.print(n);
					counter++;
				}
				else
					System.out.print(SPACE);
			}
			if((counter > 0) && (counter < MULTIPLE.length))
				System.out.print(" << " + MULTIPLE[counter] + " >>");
			System.out.println();
		}
	}
}
