package nl.s22k.chess.eval;

public class MaterialUtil {

	public static final int[][] VALUES = {
			// WHITE QQQRRRBBBNNNPPPP
			{ 0, 1 << 0, 1 << 4, 1 << 7, 1 << 10, 1 << 13 },
			// BLACK QQQRRRBBBNNNPPPP
			{ 0, 1 << 16, 1 << 20, 1 << 23, 1 << 26, 1 << 29 } };

	public static final int[] SHIFT = { 0, 16 };

	private static final int MASK_MAJORS_ALL = 0xfff0fff0;
	private static final int MASK_MAJORS_WHITE = 0xfff0;
	private static final int MASK_MAJORS_BLACK = 0xfff00000;
	private static final int[] MASK_MAJORS = { MASK_MAJORS_WHITE, MASK_MAJORS_BLACK };
	private static final int MASK_NIGHTS[] = { 0x70, 0x700000 };
	private static final int MASK_SINGLE_BISHOPS = 0x800080;
	private static final int MASK_SINGLE_BISHOP_NIGHT_WHITE = 0x90;
	private static final int MASK_SINGLE_BISHOP_NIGHT_BLACK = 0x900000;
	private static final int MASK_PAWNS_QUEENS[] = { 0xe00f, 0xe00f0000 };
	private static final int MASK_SLIDING_PIECES[] = { 0xff80, 0xff800000 };

	public static boolean containsMajorPieces(final int material) {
		return (material & MASK_MAJORS_ALL) != 0;
	}

	public static boolean hasMajorPieces(final int material, final int color) {
		return (material & MASK_MAJORS[color]) != 0;
	}

	public static boolean hasWhiteMajorPieces(final int material) {
		return (material & MASK_MAJORS_WHITE) != 0;
	}

	public static boolean hasBlackMajorPieces(final int material) {
		return (material & MASK_MAJORS_BLACK) != 0;
	}

	public static boolean oppositeBishops(final int material) {
		return Long.bitCount(material & MASK_MAJORS_ALL) == 2 && Long.bitCount(material & MASK_SINGLE_BISHOPS) == 2;
	}

	public static boolean noWhiteMajorPiecesOrOneNightOrBishop(final int material) {
		switch (Long.bitCount(material & MASK_MAJORS_WHITE)) {
		case 0:
			return true;
		case 1:
			return Long.bitCount(material & MASK_SINGLE_BISHOP_NIGHT_WHITE) == 1;
		default:
			return false;
		}
	}

	public static boolean noBlackMajorPiecesOrOneNightOrBishop(final int material) {
		switch (Long.bitCount(material & MASK_MAJORS_BLACK)) {
		case 0:
			return true;
		case 1:
			return Long.bitCount(material & MASK_SINGLE_BISHOP_NIGHT_BLACK) == 1;
		default:
			return false;
		}
	}

	public static boolean hasPawnsOrQueens(final int material, final int color) {
		return (material & MASK_PAWNS_QUEENS[color]) != 0;
	}

	public static boolean hasOnlyNights(final int material, final int color) {
		return (material & MASK_MAJORS[color]) == (material & MASK_NIGHTS[color]);
	}

	public static int getMajorPieces(final int material, final int color) {
		return (material & MASK_MAJORS[color]) >>> SHIFT[color];
	}

	public static boolean hasSlidingPieces(final int material, final int color) {
		return (material & MASK_SLIDING_PIECES[color]) != 0;
	}

}
