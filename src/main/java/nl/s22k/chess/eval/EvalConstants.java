package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.move.StaticMoves;

public class EvalConstants {

	public static final int[] MATERIAL_SCORES = new int[] { 0, 100, 325, 340, 500, 950, 3000 };
	public static final int QUEEN_PROMOTION_SCORE = MATERIAL_SCORES[ChessConstants.QUEEN] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int KNIGHT_PROMOTION_SCORE = MATERIAL_SCORES[ChessConstants.NIGHT] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int ROOK_PROMOTION_SCORE = MATERIAL_SCORES[ChessConstants.ROOK] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int BISHOP_PROMOTION_SCORE = MATERIAL_SCORES[ChessConstants.BISHOP] - MATERIAL_SCORES[ChessConstants.PAWN];

	//@formatter:off
	
	//concept borrowed from Ed Schroder
	public static final int[] KING_SAFETY_SCORES = { 
			0, 2, 3, 6, 12, 18, 25, 37, 50, 75, 100, 
			125, 150, 175, 200, 225, 250, 275, 300, 
			325, 350, 375, 400, 425, 450, 475, 500, 
			525, 550, 575, 600, 600, 600, 600, 600 };

	public static final int KING_SAFETY_ATTACK_PATTERN_COUNTER[] = {
		 // .  P  N  N  R  R  R  R  Q  Q  Q  Q  Q  Q  Q  Q  K  K  K  K  K  K  K  K  K  K  K  K  K  K  K  K
		 //          P     P  N  N     P  N  N  R  R  R  R     P  N  N  R  R  R  R  Q  Q  Q  Q  Q  Q  Q  Q
		 //                      P           P     N  N  N           P     P  N  N     P  N  N  R  R  R  R
			0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 2, 2, 2, 3, 3, 3, 0, 0, 0, 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3 };

	public static final int SCORE_DRAW = 0;

	public static final int SCORE_MATE_BOUND = 32000;

	public static final int[][] PAWN_POSITION_SCORES = 			new int[2][64];
	public static final int[][] PAWN_POSITION_SCORES_ENDGAME =	new int[2][64];
		
	public static final int[][] BISHOP_POSITION_SCORES = 		new int[2][64];
	public static final int[][] ROOK_POSITION_SCORES = 			new int[2][64];
	public static final int[][] KNIGHT_POSITION_SCORES = 		new int[2][64];
	public static final int[][] KING_POSITION_SCORES = 			new int[2][64];
	public static final int[][] KING_POSITION_SCORES_ENDGAME =  new int[2][64];
	
	public static final int[] KING_PAWN_SHIELD_RANK_BONUS = 	new int[]{ 0,40,32,16, 8, 0, 0, 0 };
	public static final int[] KING_SAFETY_COUNTER_RANKS =		new int[]{ 0, 1, 3, 4, 4, 4, 4, 4 };
	public static final long[][] KING_SAFETY_BEHIND = 			new long[2][64];
	public static final long[] KING_SAFETY_NEXT = 				new long[64]; //not color specific
	public static final long[][] KING_SAFETY_FRONT = 			new long[2][64];
	public static final long[][] KING_SAFETY_FRONT_FURTHER = 	new long[2][64];
	public static final long[][] KING_PAWN_SHIELD_KINGSIDE_MASK =  new long[2][8];
	public static final long[][] KING_PAWN_SHIELD_QUEENSIDE_MASK = new long[2][8];
	
	public static final long[][] PASSED_PAWN_MASKS = 			new long[2][64];
	public static final int[] PAWN_STORM_BONUS = 				new int[]{ 0, 0, 0, 8,16,32, 0, 0};
	public static final int[] PASSED_PAWN_SCORE = new int[] {0, 20, 40, 60, 120, 150, 250, 0};
		
	public static final int[] KNIGHT_MOBILITY = new int[] {-15, -5, -1, 2, 5, 7, 9, 11, 13}; 
	public static final int[] BISHOP_MOBILITY = new int[] {-15, -11, -6, -1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12};
	public static final int[] ROOK_MOBILITY = new int[] {-10, -4, -2, 0, 2, 3, 4, 5, 6, 8, 8, 9, 10, 11, 12};
	public static final int[] QUEEN_MOBILITY = new int[] {-10, -6, -5, -4, -2, -2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 6, 6, 6,
			7, 7, 8, 8, 9, 9, 10, 10, 10};
	
	
	public static final int[] KKR_KKQ_KING_DISTANCE_SCORE = new int[]{0, 0, 60, 40, 30, 20, 10, 0};
		
	static
	{	KNIGHT_POSITION_SCORES[WHITE] = new int[]{	
			-50,-30,-30,-30,-30,-30,-30,-50,
			-40,-20,  0,  0,  0,  0,-20,-40,
			-30,  0, 10, 15, 15, 10,  0,-30,
			-30,  5, 20, 25, 25, 20,  5,-30,
			-30,  0, 20, 25, 25, 20,  0,-30,
			-30,  5, 20, 20, 20, 20,  5,-30,
			-40,-20,  0, 10, 10,  0,-20,-40,
			-50,-10,-30,-30,-30,-30,-10,-50,
		};
		
		PAWN_POSITION_SCORES[WHITE] = new int[] {
				  0,  0,  0,  0,  0,  0,  0,  0,
				 50, 50, 50, 50, 50, 50, 50, 50,
				 30, 30, 30, 30, 30, 30, 30, 30,
				 20, 20, 20, 25, 25, 20, 20, 20,
				 10, 10, 10, 20, 20, 10, 10, 10,
				 10,  0,  0, 10, 10,  0,  0, 10,
				  0,  0,  0,-20,-20,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PAWN_POSITION_SCORES_ENDGAME[WHITE] = new int[] {
				  0,  0,  0,  0,  0,  0,  0,  0,
				100,100,100,100,100,100,100,100,
				 50, 50, 50, 50, 50, 50, 50, 50,
				 20, 20, 20, 25, 25, 20, 20, 20,
				 10, 10, 10, 20, 20, 10, 10, 10,
				  5,  5,  5, 10, 10,  5,  5,  5,
				  0,  0,  0,-20,-20,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0
		};
		
		BISHOP_POSITION_SCORES[WHITE] = new int[] {
				-20,-10,-10,-10,-10,-10,-10,-20,
				-10,  0,  0,  0,  0,  0,  0,-10,
				-10,  0,  5, 10, 10,  5,  0,-10,
				-10,  5,  5, 10, 10,  5,  5,-10,
				-10,  0, 10, 15, 15, 10,  0,-10,
				-10, 10, 10, 15, 15, 10, 10,-10,
				-10,  5,  0,  0,  0,  0,  5,-10,
				-20,-10,-10,-10,-10,-10,-10,-20
		};
		
		ROOK_POSITION_SCORES[WHITE] = new int[] {
				 0,  0,  0,  0,  0,  0,  0,  0,
				10, 20, 20, 20, 20, 20, 20, 10,
				 0, 10, 10, 10, 10, 10, 10,  0,
				-5,  0,  0,  0,  0,  0,  0, -5,
				-5,  0,  0,  0,  0,  0,  0, -5,
				-5,  0,  0,  0,  0,  0,  0, -5,
				-5,  0,  0,  0,  0,  0,  0, -5,
			   -10,  0,  0,  0,  0,  0,  0,-10
		};
		
		KING_POSITION_SCORES[WHITE] = new int[] {
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-20,-30,-30,-40,-40,-30,-30,-20,
				  0,  0,-10,-20,-20,-10,  0,  0,
				 10, 20,  0,  0,  0,  0, 20, 10,
				 20, 30, 20,-20,-20, 20, 30, 20
		};
		
		KING_POSITION_SCORES_ENDGAME[WHITE] = new int[] {
			   -10, 10, 20, 40, 40, 20, 10,-10,
				 5, 25, 45, 50, 50, 45, 25,  5,
				25, 45, 75, 75, 75, 75, 45, 25,
				25, 45, 75, 90, 90, 75, 45, 25,
				25, 45, 75, 90, 90, 75, 45, 25,
				25, 45, 75, 75, 75, 75, 45, 25,
				15, 35, 45, 45, 45, 45, 35, 15,
			   -10, 10, 20, 25, 25, 20, 10,-10
		};
		
	}
		
	//@formatter:on

	static {

		// fix white arrays
		Util.reverse(PAWN_POSITION_SCORES[WHITE]);
		Util.reverse(BISHOP_POSITION_SCORES[WHITE]);
		Util.reverse(ROOK_POSITION_SCORES[WHITE]);
		Util.reverse(KNIGHT_POSITION_SCORES[WHITE]);
		Util.reverse(KING_POSITION_SCORES[WHITE]);
		Util.reverse(KING_POSITION_SCORES_ENDGAME[WHITE]);
		Util.reverse(PAWN_POSITION_SCORES_ENDGAME[WHITE]);

		// create black arrays
		for (int i = 0; i < 64; i++) {
			PAWN_POSITION_SCORES[BLACK][i] = PAWN_POSITION_SCORES[WHITE][63 - i];
			BISHOP_POSITION_SCORES[BLACK][i] = BISHOP_POSITION_SCORES[WHITE][63 - i];
			ROOK_POSITION_SCORES[BLACK][i] = ROOK_POSITION_SCORES[WHITE][63 - i];
			KNIGHT_POSITION_SCORES[BLACK][i] = KNIGHT_POSITION_SCORES[WHITE][63 - i];
			KING_POSITION_SCORES[BLACK][i] = KING_POSITION_SCORES[WHITE][63 - i];
			KING_POSITION_SCORES_ENDGAME[BLACK][i] = KING_POSITION_SCORES_ENDGAME[WHITE][63 - i];
			PAWN_POSITION_SCORES_ENDGAME[BLACK][i] = PAWN_POSITION_SCORES_ENDGAME[WHITE][63 - i];
		}
	}

	static {
		// fill passed-pawn-masks
		for (int i = 8; i <= 47; i++) {
			// WHITE
			long mask = 0;
			int j = i + 7;
			do {
				if (j % 8 != 7) {
					mask |= Util.POWER_LOOKUP[j];
				}
				j++;
				mask |= Util.POWER_LOOKUP[j];
				j++;
				if (j % 8 != 0) {
					mask |= Util.POWER_LOOKUP[j];
				}
				j += 6;
			} while (j < 56);
			PASSED_PAWN_MASKS[WHITE][i] = mask;
		}
		for (int i = 55; i >= 16; i--) {
			// BLACK
			long mask = 0;
			int j = i - 7;
			do {
				if (j % 8 != 0) {
					mask |= Util.POWER_LOOKUP[j];
				}
				j--;
				mask |= Util.POWER_LOOKUP[j];
				j--;
				if (j % 8 != 7) {
					mask |= Util.POWER_LOOKUP[j];
				}
				j -= 6;
			} while (j > 7);
			PASSED_PAWN_MASKS[BLACK][i] = mask;
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
			KING_SAFETY_NEXT[i] = StaticMoves.KING_MOVES[i] & ChessConstants.MASKS_RANK[i / 8];

			if (i > 7) {
				KING_SAFETY_BEHIND[WHITE][i] |= StaticMoves.KING_MOVES[i] & ChessConstants.MASKS_RANK[i / 8 - 1];
				KING_SAFETY_FRONT[BLACK][i] = StaticMoves.KING_MOVES[i] & ChessConstants.MASKS_RANK[i / 8 - 1];
				if (i > 15) {
					KING_SAFETY_FRONT_FURTHER[BLACK][i] = StaticMoves.KING_MOVES[i] >>> 8 & ChessConstants.MASKS_RANK[i / 8 - 2];
				}
			}
			if (i < 56) {
				KING_SAFETY_BEHIND[BLACK][i] |= StaticMoves.KING_MOVES[i] & ChessConstants.MASKS_RANK[i / 8 + 1];
				KING_SAFETY_FRONT[WHITE][i] = StaticMoves.KING_MOVES[i] & ChessConstants.MASKS_RANK[i / 8 + 1];
				if (i < 48) {
					KING_SAFETY_FRONT_FURTHER[WHITE][i] = StaticMoves.KING_MOVES[i] << 8 & ChessConstants.MASKS_RANK[i / 8 + 2];
				}
			}
		}

		// always 3 wide, even at file 1 and 8
		for (int i = 0; i < 64; i++) {
			for (int color = 0; color < 2; color++) {
				if (i % 8 == 0) {
					KING_SAFETY_NEXT[i] |= KING_SAFETY_NEXT[i + 1] ^ Util.POWER_LOOKUP[i];
					KING_SAFETY_BEHIND[color][i] |= KING_SAFETY_BEHIND[color][i + 1];
					KING_SAFETY_FRONT[color][i] |= KING_SAFETY_FRONT[color][i + 1];
					KING_SAFETY_FRONT_FURTHER[color][i] |= KING_SAFETY_FRONT_FURTHER[color][i + 1];
				} else if (i % 8 == 7) {
					KING_SAFETY_NEXT[i] |= KING_SAFETY_NEXT[i - 1] ^ Util.POWER_LOOKUP[i];
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

}
