class Matrix extends Print {
	private Cell[][] cellList;
	
	public Matrix(int max) {
		Position.setMax(max);
		cellList = new Cell[max][max]; // rows x columns
	}

	int setNumber(Position p, int n) {
		cellList[p.getY()][p.getX()].setNumber(n);
		removeCandidate(p, Area.HORIZONTAL, n);
		removeCandidate(p, Area.VERTICAL, n);
		removeCandidate(p, Area.BLOCK, n);
		return 1;
	}

	void removeCandidate(Position p, Area a, int c) {
		Position b = p.base();
		int max = p.getMax();
		int min = p.getMin();
		switch(a) {
			case POINT:
				cellList[p.getY()][p.getX()].removeCandidate(c);
				break;
			case BLOCK_HORIZONTAL:
				for(int j = 0, x = b.getX(); j < min; j++, x++)
					cellList[p.getY()][x].removeCandidate(c);
				break;
			case BLOCK_VERTICAL:
				for(int i = 0, y = b.getY(); i < min; i++, y++)
					cellList[y][p.getX()].removeCandidate(c);
				break;
			case BLOCK:
				for(int i = 0, y = b.getY(); i < min; i++, y++)
					for(int j = 0, x = b.getX(); j < min; j++, x++)
						cellList[y][x].removeCandidate(c);
				break;
			case HORIZONTAL:
				for(int x = 0; x < max; x++)
					cellList[p.getY()][x].removeCandidate(c);
				break;
			case VERTICAL:
				for(int y = 0; y < max; y++)
					cellList[y][p.getX()].removeCandidate(c);
				break;
			case ALL:
				for(int y = 0; y < max; y++)
					for(int x = 0; x < max; x++)
						cellList[y][x].removeCandidate(c);
				break;
		}
	}
	void printMissing(int depth, Area a) {
		Position p = new Position(0, 0);
		int max = Position.getMax();
		int min = Position.getMin();
		System.out.println();
		System.out.println(diluted(a));
		switch(a) {
			case BLOCK:
				for(int blockY = 0; blockY < min; blockY++)
					for(int blockX = 0; blockX < min; blockX++) {
						System.out.println();
						for(int y = 0; y < min; y++)
							for(int x = 0; x < min; x++, p.plus(Area.HORIZONTAL))
								cellList[blockY * min + y][blockX * min + x].printMissing(depth, p);
					}
				break;
			case HORIZONTAL:
				for(int y = 0; y < max; y++) {
					System.out.println();
					for(int x = 0; x < max; x++, p.plus(a))
						cellList[y][x].printMissing(depth, p);
				}
				break;
			case VERTICAL:
				for(int x = 0; x < max; x++) {
					System.out.println();
					for(int y = 0; y < max; y++, p.plus(a))
						cellList[y][x].printMissing(depth, p);
				}
				break;
		}
	}
}
