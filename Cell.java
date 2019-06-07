import java.util.ArrayList;
import java.lang.IllegalArgumentException;

class Cell {
	public static final int MISSING = 0;

	private static final String[] MULTIPLE = {"NOTHING", "SINGLE", "PAIR", "TRIPLE", "QUAD", "QUINT"};

	private int number = MISSING;
	private ArrayList<Integer> candidates;
	private Position position; // for printing purposes

	Cell(Position position) {
		setPosition(position);
		number = MISSING;
		candidates = allNumbers(position.getMax());
	}

	Cell(Position position, int number) {
		setPosition(position);
		setNumber(number);
	}

	boolean equals(int number) {
		return (number == this.number);
	}

	boolean equals(Cell cell) {
		return (number == cell.number);
	}

	// POINT //
	
	void setPosition(Position position) {
		if(position == null)
			throw new IllegalArgumentException();
		this.position = new Position(position);
	}

	Position getPosition() {
		return position;
	}

	// NUMBER //

	void setNumber(int number) {
		if(number == MISSING)
			return;
		this.number = number;
		candidates.clear();
	}

	int getNumber() {
		return number;
	}

	String formattedNumber() {
		int width = Coordinate.digitsMax(position.getMax());
		if(number == MISSING)
			return String.format("%" + width + "s", " ");
		return String.format("%" + width + "d", number);
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
	
	void setCandidate(int candidate) {
		candidates.add(candidate);
	}

	void setCandidates(ArrayList<Integer> candidates) {
		this.candidates = candidates;
	}

	ArrayList<Integer> getCandidates() {
		return candidates;
	}

	int countCantidates() {
		return candidates.size();
	}

	ArrayList<Integer> allNumbers(int max) {
		ArrayList<Integer> all = new ArrayList<Integer>();
		for(int number = 1; number <= max; number++)
			all.add(number);
		return all;
	}

	boolean equalsCandidates(ArrayList<Integer> numbers) {
		return numbers.equals(candidates);
	}

	// Returns true if the candidates contain at least minimum of the given numbers and none different
	boolean cleanSubset(ArrayList<Integer> numbers, int minimum) {
		int counter = 0;
		for(Integer number: candidates)
			if(numbers.indexOf(number) == -1)
				return false;
			else
				counter++;
		if(counter >= minimum)
			return true;
		return false;
	}

	// Returns true if the candidates contain at least minimum of the given numbers and maybe some more different
	boolean dirtySubset(ArrayList<Integer> numbers, int minimum) {
		int counter = 0;
		for(Integer number: candidates)
			if(numbers.indexOf(number) != -1)
				counter++;
		if(counter >= minimum)
			return true;
		return false;
	}

	boolean existsCandidate(int number) {
		int index = candidates.indexOf(number);
		if(index >= 0)
			return true;
		return false;
	}

	boolean removeCandidate(int number) {
		int index = candidates.indexOf(number);
		if(index >= 0)
		{
			candidates.remove(index);
			return true;
		}
		return false;
	}

	int countCandidates() {
		return candidates.size();
	}

	int getCandidateAt(int index) {
		return candidates.get(index);
	}

	// PRINT //

	public String toString() {
		String s = "Cell" + position;
		if(number == MISSING) {
			s += " Candidates" + candidates;
			int size = candidates.size();
			if((size > 0) && (size < MULTIPLE.length))
				s += " - " + MULTIPLE[size];
		}
		else
			s += " Number(" + number + ")";
		return s;
	}

	void println() {
		System.out.println(this);
	}
}
