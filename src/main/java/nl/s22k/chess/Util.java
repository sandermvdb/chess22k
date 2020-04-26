package nl.s22k.chess;

public class Util {

	public static final int SHORT_MIN = -32767;
	public static final int SHORT_MAX = 32767;

	private static final byte[][] DISTANCE = new byte[64][64];
	static {
		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				DISTANCE[i][j] = (byte) Math.max(Math.abs((i >>> 3) - (j >>> 3)), Math.abs((i & 7) - (j & 7)));
			}
		}
	}

	public static final long[] POWER_LOOKUP = new long[64];
	static {
		for (int i = 0; i < 64; i++) {
			POWER_LOOKUP[i] = 1L << i;
		}
	}

	public static void reverse(int[] array) {
		for (int i = 0; i < array.length / 2; i++) {
			int temp = array[i];
			array[i] = array[array.length - 1 - i];
			array[array.length - 1 - i] = temp;
		}
	}

	public static void reverse(long[] array) {
		for (int i = 0; i < array.length / 2; i++) {
			long temp = array[i];
			array[i] = array[array.length - 1 - i];
			array[array.length - 1 - i] = temp;
		}
	}

	public static long mirrorHorizontal(long bitboard) {
		long k1 = 0x5555555555555555L;
		long k2 = 0x3333333333333333L;
		long k4 = 0x0f0f0f0f0f0f0f0fL;
		bitboard = ((bitboard >>> 1) & k1) | ((bitboard & k1) << 1);
		bitboard = ((bitboard >>> 2) & k2) | ((bitboard & k2) << 2);
		bitboard = ((bitboard >>> 4) & k4) | ((bitboard & k4) << 4);
		return bitboard;
	}

	public static int flipHorizontalIndex(int index) {
		return (index & 0xF8) | (7 - (index & 7));
	}

	public static long mirrorVertical(long bitboard) {
		return Long.reverseBytes(bitboard);
	}

	public static int getDistance(final int index1, final int index2) {
		return DISTANCE[index1][index2];
	}

	public static int getDistance(final long sq1, final long sq2) {
		return getDistance(Long.numberOfTrailingZeros(sq1), Long.numberOfTrailingZeros(sq2));
	}

	public static int getUsagePercentage(long keys[]) {
		int usage = 0;
		for (int i = 0; i < 1000; i++) {
			if (keys[i] != 0) {
				usage++;
			}
		}
		return usage / 10;
	}

	public static int getUsagePercentage(int keys[]) {
		int usage = 0;
		for (int i = 0; i < 1000; i++) {
			if (keys[i] != 0) {
				usage++;
			}
		}
		return usage / 10;
	}

	/**
	 * returns the black corresponding square
	 */
	public static int getRelativeSquare(final int color, final int index) {
		return index ^ (56 * color);
	}

}
