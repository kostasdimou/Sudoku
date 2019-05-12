import java.util.ArrayList;
import java.lang.IllegalArgumentException;

class Cell extends Print {
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

	void setPoint(Position p) {
		point = p;
	}

	Position getPoint() {
		return point;
	}

	void setNumber(int n) {
		if(n == MISSING)
			return;
		number = n;
		candidates.clear();
	}

	int getNumber() {
		return number;
	}

	void setCabdidates(ArrayList<Integer> l) {
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

	boolean equals(int n) {
		return (number == n);
	}

	boolean equals(Cell c) {
		return (number == c.number);
	}

	boolean candidateExists(int c) {
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

    int candidateCount() {
		return candidates.size();
	}

    int getFirstCandidate() {
		return candidates.get(0);
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

    boolean empty() {
		if(number == MISSING)
			return true;
		return false;
	}

    boolean exists() {
		if(number == MISSING)
			return false;
		return true;
	}

	public String toString() {
		String s = "Cell" + point.toString() + SPACE;
		if(number == MISSING)
			s += "?";
		else
			s += number;
		s += SPACE;
		s += DASH;
		int counter = candidates.size();
		for(Integer c: candidates) {
			s += SPACE;
			s += c;
		}
		if((counter > 0) && (counter < MULTIPLE.length))
			s += " << " + MULTIPLE[counter] + " >>";
		return s;
	}

	void print(int depth) {
		margin(depth);
		System.out.println(toString());
	}
}
