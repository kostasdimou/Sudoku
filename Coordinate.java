public class Coordinate {
	public enum Format {
		CHESS, // Y=9-1, X=A-I
		JAVA, // Y=0-9, X=0-9 (default)
		SUDOKU; // Y=A-I, X=1-9
	}

	private static final Coordinate instance = new Coordinate();
	private static Format system = Format.JAVA;

    private Coordinate() {
	}

    public static Coordinate getInstance() {
        return instance;
    }

	public void setFormat(Format s) {
		system = s;
	}

	public boolean setFormat(String name) {
		for(Format s: Format.values())
			if(name.equals(s.name())) {
				setFormat(s);
				return true;
			}
		System.out.println("Unsupported coordinate mode: " + name);
		return false;
	}

	public Format getFormat() {
		return system;
	}
}
