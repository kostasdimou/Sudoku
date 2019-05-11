import java.util.stream.IntStream;

class Print {
    public static final char SPACE = ' ';
    public static final String EMPTY = "";

	String diluted(String dense) {
		String thin = EMPTY;
		for(int i = 0; i < dense.length(); i++) {
			if(i != 0)
				thin += SPACE;
			thin += dense.charAt(i);
		}
		return thin;
	}

	String diluted(Area area) {
		return diluted(area.name());
	}

	void margin(int depth) {
		IntStream.iterate(1, i -> i + 1).limit(depth * 2).forEach(i -> { System.out.print(SPACE); });
	}
}
