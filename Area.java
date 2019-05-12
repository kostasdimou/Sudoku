public enum Area {
	POINT, // 1 x 1
	BUCKET_HORIZONTAL, // 1 x 3
	BUCKET_VERTICAL, // 3 x 1
	DIAGONAL, // used only for Position.forward() & Position.backward()
	HORIZONTAL, // 1 x 9
	VERTICAL, // 9 x 1
	BLOCK, // 3 x 3
	ALL // 9 x 9
}
