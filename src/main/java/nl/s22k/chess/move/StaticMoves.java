package nl.s22k.chess.move;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Util;

public class StaticMoves {

	public static final long[] KNIGHT_MOVES = new long[64];
	public static final long[] KING_MOVES = new long[64];

	public static final long[][] PAWN_MOVES_1 = new long[2][64];
	public static final long[][] PAWN_PROMOTION_MOVES = new long[2][64];
	public static final long[][] PAWN_PROMOTION_ATTACKS = new long[2][64];
	public static final long[][] PAWN_NON_PROMOTION_ATTACKS = new long[2][64];
	public static final long[][] PAWN_ALL_ATTACKS = new long[2][64];

	// PAWN
	static {
		for (int currentPosition = 0; currentPosition < 64; currentPosition++) {
			for (int newPosition = 0; newPosition < 64; newPosition++) {
				// 1-moves
				if (newPosition == currentPosition + 8 && currentPosition > 7 && currentPosition < 48) {
					PAWN_MOVES_1[WHITE][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
				if (newPosition == currentPosition - 8 && currentPosition < 56 && currentPosition > 15) {
					PAWN_MOVES_1[BLACK][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}

				// promotion-moves
				if (newPosition == currentPosition + 8 && currentPosition >= 48) {
					PAWN_PROMOTION_MOVES[WHITE][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
				if (newPosition == currentPosition - 8 && currentPosition <= 15) {
					PAWN_PROMOTION_MOVES[BLACK][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}

				// attacks
				if (newPosition == currentPosition + 7 && newPosition % 8 != 7 && currentPosition > 7 && currentPosition < 48) {
					PAWN_NON_PROMOTION_ATTACKS[WHITE][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
				if (newPosition == currentPosition + 9 && newPosition % 8 != 0 && currentPosition > 7 && currentPosition < 48) {
					PAWN_NON_PROMOTION_ATTACKS[WHITE][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
				if (newPosition == currentPosition - 7 && newPosition % 8 != 0 && currentPosition < 56 && currentPosition > 15) {
					PAWN_NON_PROMOTION_ATTACKS[BLACK][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
				if (newPosition == currentPosition - 9 && newPosition % 8 != 7 && currentPosition < 56 && currentPosition > 15) {
					PAWN_NON_PROMOTION_ATTACKS[BLACK][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}

				// promotion attacks
				// added white pawns at first rank because these could be mirrored black pieces (super-piece used in
				// SEE). Same for black
				if (newPosition == currentPosition + 7 && newPosition % 8 != 7 && (currentPosition >= 48 || currentPosition < 8)) {
					PAWN_PROMOTION_ATTACKS[WHITE][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
				if (newPosition == currentPosition + 9 && newPosition % 8 != 0 && (currentPosition >= 48 || currentPosition < 8)) {
					PAWN_PROMOTION_ATTACKS[WHITE][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
				if (newPosition == currentPosition - 7 && newPosition % 8 != 0 && (currentPosition <= 15 || currentPosition > 55)) {
					PAWN_PROMOTION_ATTACKS[BLACK][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
				if (newPosition == currentPosition - 9 && newPosition % 8 != 7 && (currentPosition <= 15 || currentPosition > 55)) {
					PAWN_PROMOTION_ATTACKS[BLACK][currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
			}
		}
	}

	// pawn-all-attacks
	static {
		for (int i = 0; i < 64; i++) {
			PAWN_ALL_ATTACKS[WHITE][i] = PAWN_PROMOTION_ATTACKS[WHITE][i] | PAWN_NON_PROMOTION_ATTACKS[WHITE][i];
			PAWN_ALL_ATTACKS[BLACK][i] = PAWN_PROMOTION_ATTACKS[BLACK][i] | PAWN_NON_PROMOTION_ATTACKS[BLACK][i];
		}
	}

	// knight
	static {
		for (int currentPosition = 0; currentPosition < 64; currentPosition++) {

			for (int newPosition = 0; newPosition < 64; newPosition++) {
				// check if newPosition is a correct move
				if (isKnightMove(currentPosition, newPosition)) {
					KNIGHT_MOVES[currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
			}
		}
	}

	// king
	static {
		for (int currentPosition = 0; currentPosition < 64; currentPosition++) {
			for (int newPosition = 0; newPosition < 64; newPosition++) {
				// check if newPosition is a correct move
				if (isKingMove(currentPosition, newPosition)) {
					KING_MOVES[currentPosition] |= Util.POWER_LOOKUP[newPosition];
				}
			}
		}
	}

	private static boolean isKnightMove(int currentPosition, int newPosition) {
		if (currentPosition / 8 - newPosition / 8 == 1) {
			return currentPosition - 10 == newPosition || currentPosition - 6 == newPosition;
		}
		if (newPosition / 8 - currentPosition / 8 == 1) {
			return currentPosition + 10 == newPosition || currentPosition + 6 == newPosition;
		}
		if (currentPosition / 8 - newPosition / 8 == 2) {
			return currentPosition - 17 == newPosition || currentPosition - 15 == newPosition;
		}
		if (newPosition / 8 - currentPosition / 8 == 2) {
			return currentPosition + 17 == newPosition || currentPosition + 15 == newPosition;
		}
		return false;
	}

	private static boolean isKingMove(int currentPosition, int newPosition) {
		if (currentPosition / 8 - newPosition / 8 == 0) {
			return currentPosition - newPosition == -1 || currentPosition - newPosition == 1;
		}
		if (currentPosition / 8 - newPosition / 8 == 1) {
			return currentPosition - newPosition == 7 || currentPosition - newPosition == 8 || currentPosition - newPosition == 9;
		}
		if (currentPosition / 8 - newPosition / 8 == -1) {
			return currentPosition - newPosition == -7 || currentPosition - newPosition == -8 || currentPosition - newPosition == -9;
		}
		return false;
	}

}
