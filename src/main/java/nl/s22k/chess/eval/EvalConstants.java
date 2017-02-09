package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.move.StaticMoves;

public class EvalConstants {

	public static final int[] MATERIAL_SCORES = new int[] { 0, 100, 325, 350, 500, 950, 3200 };
	public static final int QUEEN_PROMOTION_SCORE = MATERIAL_SCORES[ChessConstants.QUEEN] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int KNIGHT_PROMOTION_SCORE = MATERIAL_SCORES[ChessConstants.NIGHT] - MATERIAL_SCORES[ChessConstants.PAWN];

	//@formatter:off
	
	//borrowed from Ed Schroder
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

	// scores with value 0 are not stored in the TT
	public static final int SCORE_DRAW_3FOLD = 0;
	public static final int SCORE_DRAW_STALEMATE = 1;
	public static final int SCORE_DRAW_MATERIAL = 2;
	public static final int SCORE_DRAW_BAD_BISHOP_ENDGAME = 3;

	public static final int SCORE_MATE_BOUND = 32000;

	public static final int[][] PAWN_POSITION_SCORES_ENDGAME =	new int[2][64];
	private static final int[][] PAWN_POSITION_SCORES_MIDDLE = 	new int[2][64];
	private static final int[][] PAWN_POSITION_SCORES_LEFT = 	new int[2][64];
	private static final int[][] PAWN_POSITION_SCORES_RIGHT = 	new int[2][64];
	public static final int[][][] PAWN_POSITION_SCORES = 		new int[][][]{
		PAWN_POSITION_SCORES_MIDDLE, PAWN_POSITION_SCORES_LEFT, PAWN_POSITION_SCORES_RIGHT};
		
	public static final int[][] BISHOP_POSITION_SCORES = 		new int[2][64];
	public static final int[][] ROOK_POSITION_SCORES = 			new int[2][64];
	public static final int[][] KNIGHT_POSITION_SCORES = 		new int[2][64];
	public static final int[][] KING_POSITION_SCORES = 			new int[2][64];
	public static final int[][] KING_POSITION_SCORES_ENDGAME =  new int[2][64];
	public static final int[][] KING_PAWN_INDEX = 				new int[2][64];
	public static final int[][] KING_SAFETY_COUNTER = 			new int[2][64];
	
	public static final long[][] KING_SAFETY_BEHIND = 			new long[2][64];
	public static final long[][] KING_SAFETY_FRONT = 			new long[2][64];
	public static final long[][] KING_SAFETY_FRONT_FURTHER = 	new long[2][64];
	
	public static final long[][] PASSED_PAWN_MASKS = 			new long[2][64];
	public static final long[][][] KING_PAWN_HOLE = 			new long[][][]{
		//middle, left, right
		{
			{0xffffffffffffffffL,0xffffffffffffffffL,0xffffffffffffffffL},
			{0x808000,0x404000,0x202000},
			{0x40400,0x20200,0x10100}},
		{
			{0xffffffffffffffffL,0xffffffffffffffffL,0xffffffffffffffffL},
			{0x4040000000000L, 0x2020000000000L, 0x1010000000000L},
			{0x80800000000000L, 0x40400000000000L, 0x20200000000000L}}};
		
	public static final int[] KNIGHT_MOBILITY = new int[] {-15, -5, -1, 2, 5, 7, 9, 11, 13}; 
	public static final int[] BISHOP_MOBILITY = new int[] {-15, -11, -6, -1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12};
	public static final int[] ROOK_MOBILITY = new int[] {-10, -4, -2, 0, 2, 3, 4, 5, 6, 8, 8, 9, 10, 11, 12};
	public static final int[] QUEEN_MOBILITY = new int[] {-10, -6, -5, -4, -2, -2, -1, 0, 1, 2, 2, 3, 3, 4, 4, 5, 6, 6, 6,
			7, 7, 8, 8, 9, 9, 10, 10, 10};
	
	public static final int[] PASSED_PAWN_ENDGAME_SCORE = new int[] {0, 20, 40, 80, 120, 160, 200, 0};
		
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

		PAWN_POSITION_SCORES_MIDDLE[WHITE] = new int[] {
				  0,  0,  0,  0,  0,  0,  0,  0,
				 80, 90, 90, 90, 90, 90, 90, 80,
				 30, 50, 60, 60, 60, 60, 50, 30,
				 -5,  5, 10, 25, 25, 10,  5, -5,
				-10,  0,  0, 20, 20,  0,  0,-10,
				 -5, -5,  0, 10, 10,  0, -5, -5,
				 -5,  0, 10,-30,-30, 10,  0, -5,
				  0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PAWN_POSITION_SCORES_LEFT[WHITE] = new int[] {
				  0,  0,  0,  0,  0,  0,  0,  0,
				 30, 40, 50, 50, 50, 50, 40, 30,
				 20, 20, 20, 30, 30, 20, 20, 20,
				 10, 10, 10, 25, 25, 10, 10, 10,
				  0,  0,  0, 20, 20,  0,  0,  0,
				 25, 15, 15, 10, 10,-10,-10,-10,
				 10, 25, 25,-30,-30,-20,-20,-20,
				  0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PAWN_POSITION_SCORES_ENDGAME[WHITE] = new int[] {
				  0,  0,  0,  0,  0,  0,  0,  0,
				100,100,100,100,100,100,100,100,
				 50, 50, 50, 50, 50, 50, 50, 50,
				 20, 20, 20, 25, 25, 20, 20, 20,
				  0,  0,  0,  0,  0,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0,
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
				-10,-20,-20,-20,-20,-20,-20,-10,
				 10, 20,  0,  0,  0,  0, 20, 10,
				 20, 30, 20,-20,-20, 20, 30, 20
		};
		
		KING_POSITION_SCORES_ENDGAME[WHITE] = new int[] {
				-50,-40,-30,-10,-10,-30,-40,-50,
				-45,-25, -5,  0,  0, -5,-25,-45,
				-25, -5, 10, 15, 15, 10, -5,-25,
				-25, -5, 15, 20, 20, 15, -5,-25,
				-25, -5, 15, 20, 20, 15, -5,-25,
				-25, -5, 10, 15, 15, 10, -5,-25,
				-35,-25,  0,  0,  0,  0,-25,-35,
				-50,-40,-20,-15,-15,-20,-40,-50
		};
		
		KING_SAFETY_COUNTER[WHITE] = new int[]{
				4,4,4,4,4,4,4,4,
				4,4,4,4,4,4,4,4,
				4,4,4,4,4,4,4,4,
				4,4,4,4,4,4,4,4,
				4,4,4,4,4,4,4,4,
				4,3,3,3,3,3,3,4,
				2,1,1,1,1,1,1,2,
				2,0,0,0,0,0,0,2
		};
		
		KING_PAWN_INDEX[WHITE] = new int[] {
				  0,  0,  0,  0,  0,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0,
				  0,  0,  0,  0,  0,  0,  0,  0,
				  1,  1,  1,  0,  0,  2,  2,  2,
				  1,  1,  1,  0,  0,  2,  2,  2
		};
		
	}
		
	//@formatter:on

	static {

		// fix white arrays
		Util.reverse(PAWN_POSITION_SCORES_MIDDLE[WHITE]);
		Util.reverse(PAWN_POSITION_SCORES_LEFT[WHITE]);
		Util.reverse(BISHOP_POSITION_SCORES[WHITE]);
		Util.reverse(ROOK_POSITION_SCORES[WHITE]);
		Util.reverse(KNIGHT_POSITION_SCORES[WHITE]);
		Util.reverse(KING_PAWN_INDEX[WHITE]);
		Util.reverse(KING_POSITION_SCORES[WHITE]);
		Util.reverse(KING_POSITION_SCORES_ENDGAME[WHITE]);
		Util.reverse(PAWN_POSITION_SCORES_ENDGAME[WHITE]);
		Util.reverse(KING_SAFETY_COUNTER[WHITE]);

		// fill PAWN_POSITION_SCORES_RIGHT
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				PAWN_POSITION_SCORES_RIGHT[WHITE][i * 8 + j] = PAWN_POSITION_SCORES_LEFT[WHITE][i * 8 + 7 - j];
			}
		}

		// create black arrays
		for (int i = 0; i < 64; i++) {
			PAWN_POSITION_SCORES_MIDDLE[BLACK][i] = PAWN_POSITION_SCORES_MIDDLE[WHITE][63 - i];
			PAWN_POSITION_SCORES_LEFT[BLACK][i] = PAWN_POSITION_SCORES_LEFT[WHITE][63 - i];
			PAWN_POSITION_SCORES_RIGHT[BLACK][i] = PAWN_POSITION_SCORES_RIGHT[WHITE][63 - i];
			BISHOP_POSITION_SCORES[BLACK][i] = BISHOP_POSITION_SCORES[WHITE][63 - i];
			ROOK_POSITION_SCORES[BLACK][i] = ROOK_POSITION_SCORES[WHITE][63 - i];
			KNIGHT_POSITION_SCORES[BLACK][i] = KNIGHT_POSITION_SCORES[WHITE][63 - i];
			KING_POSITION_SCORES[BLACK][i] = KING_POSITION_SCORES[WHITE][63 - i];
			KING_PAWN_INDEX[BLACK][i] = KING_PAWN_INDEX[WHITE][63 - i];
			KING_POSITION_SCORES_ENDGAME[BLACK][i] = KING_POSITION_SCORES_ENDGAME[WHITE][63 - i];
			PAWN_POSITION_SCORES_ENDGAME[BLACK][i] = PAWN_POSITION_SCORES_ENDGAME[WHITE][63 - i];
			KING_SAFETY_COUNTER[BLACK][i] = KING_SAFETY_COUNTER[WHITE][63 - i];
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
		// fill king-safety masks
		for (int i = 0; i < 64; i++) {
			KING_SAFETY_BEHIND[WHITE][i] = StaticMoves.KING_MOVES[i] & ChessConstants.MASKS_RANK[i / 8];
			KING_SAFETY_BEHIND[BLACK][i] = StaticMoves.KING_MOVES[i] & ChessConstants.MASKS_RANK[i / 8];

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
	}

	public static int getKingPositionIndex(final int color, final int kingIndex) {
		return EvalConstants.KING_PAWN_INDEX[color][kingIndex];
	}

}
