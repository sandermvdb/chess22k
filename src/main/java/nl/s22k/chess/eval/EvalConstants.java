package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;

/**
 * Most values have been tuned using the Texel's tuning method
 */
public class EvalConstants {
	//@formatter:off

	public static final int SCORE_DRAW 			= 0;
	public static final int SCORE_MATE_BOUND 	= 32000;

	public static final int INDEX_KING_MOVING_OPENINGGAME 		= 0;
	public static final int INDEX_QUEEN_MOVING_OPENINGGAME 		= 1;
	public static final int INDEX_ROOK_PRISON 					= 2;
	public static final int INDEX_ROOK_FILE_SEMI_OPEN	 		= 3;
	public static final int INDEX_ROOK_FILE_OPEN 				= 4;
	public static final int INDEX_BISHOP_DOUBLE 				= 5;
	public static final int INDEX_BISHOP_PRISON 				= 6;
	public static final int INDEX_KNIGHT_OUTPOST_UNATTACKABLE 	= 7;
	public static final int INDEX_KNIGHT_OUTPOST 				= 8;
	public static final int INDEX_PAWN_DOUBLE 					= 9;
	public static final int INDEX_PAWN_ISOLATED 				= 10;
	public static final int INDEX_ROOK_PAIR		 				= 11;
	public static final int INDEX_BAD_BISHOP 					= 12;

	public static final int[] INDIVIDUAL_SCORES = new int[] { 
			20,  // KING_MOVING_OPENINGGAME
			10,  // QUEEN_MOVING_OPENINGGAME 
			48,  // ROOK_PRISON
			10,  // ROOK_FILE_SEMI_OPEN 
			20,  // ROOK_FILE_OPEN
			48,  // BISHOP_DOUBLE 
			40,  // BISHOP_PRISON 
			32,  // KNIGHT_OUTPOST_UNATTACKABLE 
			 0,  // KNIGHT_OUTPOST
			 4,  // PAWN_DOUBLE
			16,  // PAWN_ISOLATED
			44,  // ROOK_PAIR
			16   // BAD_BISHOP
	};
	
	public static final int[] MATERIAL_SCORES 				= new int[] {0, 90, 370, 385, 645, 1205, 3000};
	public static final int[] MATERIAL_SCORES_ENDGAME 		= new int[] {0, 85};
	public static final int[] PINNED_PIECE_SCORES 			= new int[] {0, 0, 30, 30, 40, 80, 0};
	public static final int[] PAWN_SHIELD_BONUS 			= new int[] {0, 36, 32, 20, 32, 110, 160, 0};
	public static final int[] PAWN_STORM_BONUS 				= new int[] {0, 0, 0, 0,0,0, 0, 0};
	public static final int[] PASSED_PAWN_SCORE 			= new int[] {0, 20, 20, 40, 65, 135, 200, 0};
	
	public static final int[] PASSED_PAWN_MULTIPLIERS	= new int[] {
			15,		//not endgame 
			13, 	//protected
			20};	//blocked
	
	public static final int QUEEN_PROMOTION_SCORE 	= MATERIAL_SCORES[ChessConstants.QUEEN] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int KNIGHT_PROMOTION_SCORE 	= MATERIAL_SCORES[ChessConstants.NIGHT] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int ROOK_PROMOTION_SCORE 	= MATERIAL_SCORES[ChessConstants.ROOK] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int BISHOP_PROMOTION_SCORE 	= MATERIAL_SCORES[ChessConstants.BISHOP] - MATERIAL_SCORES[ChessConstants.PAWN];

	//concept borrowed from Ed Schroder
	public static final int[] KING_SAFETY_SCORES = { 
			50, 28, 46, 22, 46, 36, 48, 52, 60, 76, 
			84, 100, 110, 126, 160, 162, 214, 220, 
			252, 320, 340, 360, 438, 550, 670, 700, 730, 
			530, 550, 575}; //not used??
	public static final int[] KING_SAFETY_ATTACK_PATTERN_COUNTER = {
		 // .  P  N  N  R  R  R  R  Q  Q  Q  Q  Q  Q  Q  Q  K  K  K  K  K  K  K  K  K  K  K  K  K  K  K  K
		 //          P     P  N  N     P  N  N  R  R  R  R     P  N  N  R  R  R  R  Q  Q  Q  Q  Q  Q  Q  Q
		 //                      P           P     N  N  N           P     P  N  N     P  N  N  R  R  R  R
			0, 0, 0, 4, 0, 0, 2, 4, 0, 0, 2, 4, 2, 3, 5, 6, 0, 0, 0, 0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3};
	public static final int[] KING_SAFETY_COUNTER_RANKS = new int[] {1, 0, 0, 0, 0, 0, 0, 2};
	
	public static final int[] KKR_KKQ_KING_DISTANCE_SCORE = new int[]{0, 0, 60, 40, 30, 20, 10, 0};
		
	public static final int[] MOBILITY_KNIGHT		= new int[] {-4, 8, 12, 10, 16, 16, 14, 14, 8};
	public static final int[] MOBILITY_BISHOP 		= new int[] {-24, -10, 2, 8, 14, 20, 24, 28, 32, 32, 32, 32, 28, 32};
	public static final int[] MOBILITY_ROOK 		= new int[] {-20, -16, -10, -8, -6, 0, 6, 10, 14, 18, 24, 26, 28, 30, 24};
	public static final int[] MOBILITY_QUEEN 		= new int[] {-20, -24, -18, -16, -8, 0, -4, -4, -6, -2, -4, -4, -6, 0, 2, 
			0, 4, 12, 18, 20, 26, 32, 42, 42, 44, 54, 44, 46};
	
	public static final long[] BAD_BISHOP = new long[]{
			0, Bitboard.F2, 0, 0, 0, 0, Bitboard.C2, 0,
			Bitboard.G3, 0, 0, 0, 0, 0, 0, Bitboard.B3,
			Bitboard.G4, 0, 0, 0, 0, 0, 0, Bitboard.B4,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			Bitboard.G5, 0, 0, 0, 0, 0, 0, Bitboard.B5,
			Bitboard.G6, 0, 0, 0, 0, 0, 0, Bitboard.B6,
			0, Bitboard.F7, 0, 0, 0, 0, Bitboard.C7, 0
	};
		
	public static final int[][] PSQT_PAWN 			= new int[2][64];
	public static final int[][] PSQT_PAWN_ENDGAME 	= new int[2][64];
	public static final int[][] PSQT_BISHOP 		= new int[2][64];
	public static final int[][] PSQT_ROOK 			= new int[2][64];
	public static final int[][] PSQT_KNIGHT 		= new int[2][64];
	public static final int[][] PSQT_KING 			= new int[2][64];
	public static final int[][] PSQT_KING_ENDGAME 	= new int[2][64];
	
	static
	{	
		PSQT_KNIGHT[WHITE] = new int[]{	
				-125,-60,-40,-25,-25,-40,-60,-125,
				 -40,-15, 10, 10, 10, 10,-15, -40, 
				 -30, 10, 30, 35, 35, 30, 10, -30, 
				  -5, 15, 35, 40, 40, 35, 15,  -5, 
				 -15, 10, 30, 30, 30, 30, 10, -15, 
				 -25, 10, 20, 30, 30, 20, 10, -25, 
				 -25,-20,  0, 15, 15,  0,-20, -25, 
				 -55,-25,-20,-15,-15,-20,-25, -55
			};
		
		PSQT_PAWN[WHITE] = new int[] {
				  0,  0,  0,  0,  0,  0,  0,  0, 
				 90, 85, 85, 95, 95, 85, 85, 90, 
				 25, 40, 55, 40, 40, 55, 40, 25, 
				  5, 25, 25, 35, 35, 25, 25,  5, 
				-10,  5, 15, 30, 30, 15,  5,-10, 
				-10,  5, 10, 20, 20, 10,  5,-10, 
				-20, 10,  5,  5,  5,  5, 10,-20, 
				  0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_PAWN_ENDGAME[WHITE] = new int[] {
				 0,  0,  0,  0,  0,  0,  0,  0, 
				95, 80, 50, 40, 40, 50, 80, 95, 
				55, 45, 25,  0,  0, 25, 45, 55, 
				30, 25, 20, 10, 10, 20, 25, 30, 
				15, 15, 10, 10, 10, 10, 15, 15, 
				10, 10, 15, 15, 15, 15, 10, 10, 
				10, 10, 15, 15, 15, 15, 10, 10, 
				 0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_BISHOP[WHITE] = new int[] {
				-35,-20,-25,-15,-15,-25,-20,-35, 
				-25, -5, -5,-10,-10, -5, -5,-25, 
				-10,  5, 10,  5,  5, 10,  5,-10, 
				 -5,-10, 10, 20, 20, 10,-10, -5, 
				-10,  0,  0, 15, 15,  0,  0,-10, 
				-10,  5, 10,  5,  5, 10,  5,-10, 
				-15, 15,  0,  0,  0,  0, 15,-15, 
				-25,-10,-10, -5, -5,-10,-10,-25, 
		};
		
		PSQT_ROOK[WHITE] = new int[] {
				 20, 15, 15, 15, 15, 15, 15, 20,
				 20, 20, 20, 15, 15, 20, 20, 20, 
				  5, 10,  5,  5,  5,  5, 10,  5, 
				  5,  0, 10,  0,  0, 10,  0,  5, 
				-10,  0,  0, -5, -5,  0,  0,-10, 
				-20, -5,-10,-10,-10,-10, -5,-20, 
				-25, -5, -5, -5, -5, -5, -5,-25, 
				 -5, -5,  5,  5,  5,  5, -5, -5
		};
		
		PSQT_KING[WHITE] = new int[] {
				40, 35, 35, -80, -80, 35, 35, 40,
				45, 35, 25, -20, -20, 25, 35, 45, 
				45, 35, 35, -50, -50, 35, 35, 45, 
				45, 35, 35, -30, -30, 35, 35, 45, 
				30, 45, 45, -55, -55, 45, 45, 30, 
				60, 70, 65, -20, -20, 65, 70, 60, 
				55, 55, 25, -30, -30, 25, 55, 55, 
				30, 45, 25, -20, -20, 25, 45, 30 
		};
		
		PSQT_KING_ENDGAME[WHITE] = new int[] {
				  0, 25, 40, 20, 20, 40, 25,  0, 
				 35, 60, 70, 60, 60, 70, 60, 35, 
				 50, 80, 80, 60, 60, 80, 80, 50, 
				 35, 65, 70, 65, 65, 70, 65, 35, 
				 20, 45, 60, 60, 60, 60, 45, 20, 
				 20, 40, 50, 55, 55, 50, 40, 20, 
				 20, 35, 40, 45, 45, 40, 35, 20, 
				-15, 15, 20, 15, 15, 20, 15,-15 
			};
		
	}
		
	//@formatter:on

	static {

		// fix white arrays
		Util.reverse(PSQT_PAWN[WHITE]);
		Util.reverse(PSQT_BISHOP[WHITE]);
		Util.reverse(PSQT_ROOK[WHITE]);
		Util.reverse(PSQT_KNIGHT[WHITE]);
		Util.reverse(PSQT_KING[WHITE]);
		Util.reverse(PSQT_KING_ENDGAME[WHITE]);
		Util.reverse(PSQT_PAWN_ENDGAME[WHITE]);

		// create black arrays
		for (int i = 0; i < 64; i++) {
			PSQT_PAWN[BLACK][i] = -PSQT_PAWN[WHITE][63 - i];
			PSQT_BISHOP[BLACK][i] = -PSQT_BISHOP[WHITE][63 - i];
			PSQT_ROOK[BLACK][i] = -PSQT_ROOK[WHITE][63 - i];
			PSQT_KNIGHT[BLACK][i] = -PSQT_KNIGHT[WHITE][63 - i];
			PSQT_KING[BLACK][i] = -PSQT_KING[WHITE][63 - i];
			PSQT_KING_ENDGAME[BLACK][i] = -PSQT_KING_ENDGAME[WHITE][63 - i];
			PSQT_PAWN_ENDGAME[BLACK][i] = -PSQT_PAWN_ENDGAME[WHITE][63 - i];
		}
	}

}
