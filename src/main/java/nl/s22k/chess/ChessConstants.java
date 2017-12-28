package nl.s22k.chess;

import nl.s22k.chess.move.StaticMoves;

public class ChessConstants {

	public static final String FEN_WHITE_PIECES[] = { "1", "P", "N", "B", "R", "Q", "K" };
	public static final String FEN_BLACK_PIECES[] = { "1", "p", "n", "b", "r", "q", "k" };

	public static final String FEN_START = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

	public static final int EMPTY = 0;
	public static final int PAWN = 1;
	public static final int NIGHT = 2;
	public static final int BISHOP = 3;
	public static final int ROOK = 4;
	public static final int QUEEN = 5;
	public static final int KING = 6;

	public static final int WHITE = 0;
	public static final int BLACK = 1;

	public static final int[] COLOR_FACTOR = { 1, -1 };
	public static final int[] COLOR_FACTOR_8 = { 8, -8 };

	public static final long[][] KING_SAFETY_BEHIND = new long[2][64];
	public static final long[] KING_SAFETY_NEXT = new long[64]; // not color specific
	public static final long[][] KING_SAFETY_FRONT = new long[2][64];
	public static final long[][] KING_SAFETY_FRONT_FURTHER = new long[2][64];
	public static final long[][] KING_PAWN_SHIELD_KINGSIDE_MASK = new long[2][8];
	public static final long[][] KING_PAWN_SHIELD_QUEENSIDE_MASK = new long[2][8];

	public static final long[] MASK_ADJACENT_FILE_UP = new long[64];
	public static final long[] MASK_ADJACENT_FILE_DOWN = new long[64];
	static {
		for (int i = 0; i < 64; i++) {
			long adjacentFile = Bitboard.FILES_ADJACENT[i % 8];
			while (adjacentFile != 0) {
				if (Long.numberOfTrailingZeros(adjacentFile) > i + 1) {
					MASK_ADJACENT_FILE_UP[i] |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(adjacentFile)];
				} else if (Long.numberOfTrailingZeros(adjacentFile) < i - 1) {
					MASK_ADJACENT_FILE_DOWN[i] |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(adjacentFile)];
				}
				adjacentFile &= adjacentFile - 1;
			}
		}
	}

	public static final long[][] ROOK_IN_BETWEEN = new long[64][64];
	static {
		int i;

		// fill from->to where to > from
		for (int from = 0; from < 64; from++) {
			for (int to = from + 1; to < 64; to++) {

				// horizontal
				if (from / 8 == to / 8) {
					i = to - 1;
					while (i > from) {
						ROOK_IN_BETWEEN[from][to] |= Util.POWER_LOOKUP[i];
						i--;
					}
				}

				// vertical
				if (from % 8 == to % 8) {
					i = to - 8;
					while (i > from) {
						ROOK_IN_BETWEEN[from][to] |= Util.POWER_LOOKUP[i];
						i -= 8;
					}
				}
			}
		}

		// fill from->to where to < from
		for (int from = 0; from < 64; from++) {
			for (int to = 0; to < from; to++) {
				ROOK_IN_BETWEEN[from][to] = ROOK_IN_BETWEEN[to][from];
			}
		}
	}

	public static final long[][] BISHOP_IN_BETWEEN = new long[64][64];
	static {
		int i;

		// fill from->to where to > from
		for (int from = 0; from < 64; from++) {
			for (int to = from + 1; to < 64; to++) {

				// diagonal \
				if ((to - from) % 9 == 0 && to % 8 > from % 8) {
					i = to - 9;
					while (i > from) {
						BISHOP_IN_BETWEEN[from][to] |= Util.POWER_LOOKUP[i];
						i -= 9;
					}
				}

				// diagonal /
				if ((to - from) % 7 == 0 && to % 8 < from % 8) {
					i = to - 7;
					while (i > from) {
						BISHOP_IN_BETWEEN[from][to] |= Util.POWER_LOOKUP[i];
						i -= 7;
					}
				}
			}
		}

		// fill from->to where to < from
		for (int from = 0; from < 64; from++) {
			for (int to = 0; to < from; to++) {
				BISHOP_IN_BETWEEN[from][to] = BISHOP_IN_BETWEEN[to][from];
			}
		}
	}

	public static final long[][] IN_BETWEEN = new long[64][64];
	static {
		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				IN_BETWEEN[i][j] = BISHOP_IN_BETWEEN[i][j] | ROOK_IN_BETWEEN[i][j];
			}
		}
	}

	public static final long[][] PASSED_PAWN_MASKS = new long[2][64];
	static {
		// fill passed-pawn-masks
		for (int i = 0; i < 64; i++) {
			PASSED_PAWN_MASKS[WHITE][i] = ((Bitboard.FILES[i & 7] | Bitboard.FILES_ADJACENT[i & 7]) & ~Bitboard.RANKS[i / 8]) >>> i << i;
			PASSED_PAWN_MASKS[BLACK][i] = ((Bitboard.FILES[i & 7] | Bitboard.FILES_ADJACENT[i & 7]) & ~Bitboard.RANKS[i / 8]) << (63 - i) >> (63 - i);
		}
	}

	static {
		// fill king-safety masks:
		//
		// UUU front-further
		// FFF front
		// NKN next
		// BBB behind
		//
		for (int i = 0; i < 64; i++) {
			KING_SAFETY_NEXT[i] = (StaticMoves.KING_MOVES[i] & Bitboard.RANKS[i / 8]) | Util.POWER_LOOKUP[i];

			if (i > 7) {
				KING_SAFETY_BEHIND[WHITE][i] |= StaticMoves.KING_MOVES[i] & Bitboard.RANKS[i / 8 - 1];
				KING_SAFETY_FRONT[BLACK][i] = StaticMoves.KING_MOVES[i] & Bitboard.RANKS[i / 8 - 1];
				if (i > 15) {
					KING_SAFETY_FRONT_FURTHER[BLACK][i] = StaticMoves.KING_MOVES[i] >>> 8 & Bitboard.RANKS[i / 8 - 2];
				}
			}
			if (i < 56) {
				KING_SAFETY_BEHIND[BLACK][i] |= StaticMoves.KING_MOVES[i] & Bitboard.RANKS[i / 8 + 1];
				KING_SAFETY_FRONT[WHITE][i] = StaticMoves.KING_MOVES[i] & Bitboard.RANKS[i / 8 + 1];
				if (i < 48) {
					KING_SAFETY_FRONT_FURTHER[WHITE][i] = StaticMoves.KING_MOVES[i] << 8 & Bitboard.RANKS[i / 8 + 2];
				}
			}
		}

		// always 3 wide, even at file 1 and 8
		for (int i = 0; i < 64; i++) {
			for (int color = 0; color < 2; color++) {
				if (i % 8 == 0) {
					KING_SAFETY_NEXT[i] |= KING_SAFETY_NEXT[i + 1];
					KING_SAFETY_BEHIND[color][i] |= KING_SAFETY_BEHIND[color][i + 1];
					KING_SAFETY_FRONT[color][i] |= KING_SAFETY_FRONT[color][i + 1];
					KING_SAFETY_FRONT_FURTHER[color][i] |= KING_SAFETY_FRONT_FURTHER[color][i + 1];
				} else if (i % 8 == 7) {
					KING_SAFETY_NEXT[i] |= KING_SAFETY_NEXT[i - 1];
					KING_SAFETY_BEHIND[color][i] |= KING_SAFETY_BEHIND[color][i - 1];
					KING_SAFETY_FRONT[color][i] |= KING_SAFETY_FRONT[color][i - 1];
					KING_SAFETY_FRONT_FURTHER[color][i] |= KING_SAFETY_FRONT_FURTHER[color][i - 1];
				}
			}
		}
	}

	static {
		// king-pawn-shield masks
		for (int i = 1; i < 64; i += 8) {
			// king-side
			KING_PAWN_SHIELD_KINGSIDE_MASK[WHITE][i / 8] |= KING_SAFETY_NEXT[i];
			KING_PAWN_SHIELD_KINGSIDE_MASK[WHITE][i / 8] |= KING_SAFETY_FRONT[WHITE][i];
			KING_PAWN_SHIELD_KINGSIDE_MASK[WHITE][i / 8] |= KING_SAFETY_FRONT_FURTHER[WHITE][i];

			KING_PAWN_SHIELD_KINGSIDE_MASK[BLACK][i / 8] |= KING_SAFETY_NEXT[i];
			KING_PAWN_SHIELD_KINGSIDE_MASK[BLACK][i / 8] |= KING_SAFETY_FRONT[BLACK][i];
			KING_PAWN_SHIELD_KINGSIDE_MASK[BLACK][i / 8] |= KING_SAFETY_FRONT_FURTHER[BLACK][i];
		}

		for (int i = 6; i < 64; i += 8) {
			// queen-side
			KING_PAWN_SHIELD_QUEENSIDE_MASK[WHITE][i / 8] |= KING_SAFETY_NEXT[i];
			KING_PAWN_SHIELD_QUEENSIDE_MASK[WHITE][i / 8] |= KING_SAFETY_FRONT[WHITE][i];
			KING_PAWN_SHIELD_QUEENSIDE_MASK[WHITE][i / 8] |= KING_SAFETY_FRONT_FURTHER[WHITE][i];

			KING_PAWN_SHIELD_QUEENSIDE_MASK[BLACK][i / 8] |= KING_SAFETY_NEXT[i];
			KING_PAWN_SHIELD_QUEENSIDE_MASK[BLACK][i / 8] |= KING_SAFETY_FRONT[BLACK][i];
			KING_PAWN_SHIELD_QUEENSIDE_MASK[BLACK][i / 8] |= KING_SAFETY_FRONT_FURTHER[BLACK][i];
		}
	}

	public enum ScoreType {
		EXACT(" "), ALPHA(" upperbound "), BETA(" lowerbound ");

		private ScoreType(String uci) {
			this.uci = uci;
		}

		private String uci;

		public String toString() {
			return uci;
		}
	}

}
