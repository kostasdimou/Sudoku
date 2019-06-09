import java.util.ArrayList;
import java.lang.IllegalArgumentException;

class Cell {
	public static final int MISSING = 0;

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

	boolean equalsCandidates(ArrayList<Integer> candidates) {
		return candidates.equals(this.candidates);
	}

	// Returns true if the candidates contain at least minimum of the given numbers and none different
	boolean cleanSubset(ArrayList<Integer> candidates, int minimum) {
		int counter = 0;
		for(Integer candidate: this.candidates)
			if(candidates.indexOf(candidate) == -1)
				return false;
			else
				counter++;
		if(counter >= minimum)
			return true;
		return false;
	}

	// Returns true if the candidates contain at least minimum of the given numbers and maybe some more different
	boolean dirtySubset(ArrayList<Integer> candidates, int minimum) {
		int counter = 0;
		for(Integer candidate: this.candidates)
			if(candidates.indexOf(candidate) != -1)
				counter++;
		if(counter >= minimum)
			return true;
		return false;
	}

	boolean existsCandidate(int candidate) {
		int index = candidates.indexOf(candidate);
		if(index >= 0)
			return true;
		return false;
	}

	boolean removeCandidate(int candidate) {
		int index = candidates.indexOf(candidate);
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

	// PRINT //

	public String toString() {
		String s = "Cell" + position;
		if(number == MISSING) {
			s += " Candidates" + candidates;
		}
		else
			s += " Number(" + number + ")";
		return s;
	}

	void println() {
		System.out.println(this);
	}
}
