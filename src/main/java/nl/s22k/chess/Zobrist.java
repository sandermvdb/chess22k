package nl.s22k.chess;

import java.security.SecureRandom;

public class Zobrist {

	public static long sideToMove;
	public static final long[] castling = new long[16];
	public static final long[] epIndex = new long[48];
	public static final long[][][] piece = new long[2][7][64];

	static {
		SecureRandom r = new SecureRandom();
		for (int colorIndex = 0; colorIndex <= ChessConstants.BLACK; colorIndex++) {
			for (int pieceIndex = 0; pieceIndex <= ChessConstants.KING; pieceIndex++) {
				for (int square = 0; square < 64; square++) {
					piece[colorIndex][pieceIndex][square] = r.nextLong();
				}
			}
		}
		for (int i = 0; i < castling.length; i++) {
			castling[i] = r.nextLong();
		}

		// skip first item: contains only zeros, default value and has no effect when xorring
		for (int i = 1; i < epIndex.length; i++) {
			epIndex[i] = r.nextLong();
		}
		sideToMove = r.nextLong();
	}

}
