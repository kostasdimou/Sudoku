import java.util.ArrayList;
import java.lang.IllegalArgumentException;

class Cell {
    public static final int MISSING = 0;

	private static final String[] MULTIPLE = {"NOTHING", "SINGLE", "PAIR", "TRIPLE"};

	private int number = MISSING;
	private ArrayList<Integer> candidates;
	private Position point; // for printing purposes

	Cell(Position p) {
		point = new Position(p);
		number = MISSING;
		candidates = allNumbers(p.getMax());
	}

	Cell(Position p, int n) {
		point = p;
		number = n;
	}

	boolean equals(int n) {
		return (number == n);
	}

	boolean equals(Cell c) {
		return (number == c.number);
	}

	// POINT //
	
	void setPoint(Position p) {
		point = p;
	}

	Position getPoint() {
		return point;
	}

	// NUMBER //

	void setNumber(int n) {
		if(n == MISSING)
			return;
		number = n;
		candidates.clear();
	}

	int getNumber() {
		return number;
	}

    int count(int exception) {
		if((number != MISSING) && (number != exception))
			return 1;
		return 0;
	}

    int count() {
		if(number != MISSING)
			return 1;
		return 0;
	}

    boolean isEmpty() {
		if(number == MISSING)
			return true;
		return false;
	}

    boolean exists() {
		if(number == MISSING)
			return false;
		return true;
	}

	// CANDIDATES //
	
	void setCandidate(int n) {
		candidates.add(n);
	}

	void setCandidates(ArrayList<Integer> l) {
		candidates = l;
	}

	ArrayList<Integer> getCandidates() {
		return candidates;
	}

	ArrayList<Integer> allNumbers(int max) {
		ArrayList<Integer> all = new ArrayList<Integer>();
		for(int n = 1; n <= max; n++)
			all.add(n);
		return all;
	}

	boolean equalsCandidates(ArrayList<Integer> sample) {
		return sample.equals(candidates);
	}

	boolean existsCandidate(int c) {
		int index = candidates.indexOf(c);
        if(index >= 0)
			return true;
		return false;
	}

	void removeCandidate(int c) {
		int index = candidates.indexOf(c);
		if(index >= 0)
			candidates.remove(index);
	}

    int countCandidates() {
		return candidates.size();
	}

    int getCandidateAt(int index) {
		return candidates.get(index);
	}

	// PRINT //

	public String toString() {
		String s = "Cell" + point.toString();
		if(number == MISSING)
			s += " ?";
		else
			s += " " + number;
		s += " - " + candidates.toString();
		int size = candidates.size();
		if((size > 0) && (size < MULTIPLE.length))
			s += " << " + MULTIPLE[size] + " >>";
		return s;
	}

	void println() {
		System.out.println(toString());
	}
}
