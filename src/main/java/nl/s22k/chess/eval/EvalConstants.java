package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;

/**
 * Values have been tuned using the Texel's tuning method
 */
public class EvalConstants {
	//@formatter:off

	public static final int SCORE_DRAW 			= 0;
	public static final int SCORE_DRAWISH		= 10;
	public static final int SCORE_MATE_BOUND 	= 32000;

	public static final int INDEX_OPENING_MOVE_COUNTER	 		= 0;
	public static final int INDEX_KING_MOVING_OPENINGGAME 		= 1;
	public static final int INDEX_QUEEN_MOVING_OPENINGGAME 		= 2;
	public static final int INDEX_ROOK_PRISON 					= 3;
	public static final int INDEX_ROOK_FILE_SEMI_OPEN	 		= 4;
	public static final int INDEX_ROOK_FILE_OPEN 				= 5;
	public static final int INDEX_BISHOP_DOUBLE 				= 6;
	public static final int INDEX_BISHOP_PRISON 				= 7;
	public static final int INDEX_PAWN_DOUBLE 					= 8;
	public static final int INDEX_PAWN_ISOLATED 				= 9;
	public static final int INDEX_ROOK_PAIR		 				= 10;
	public static final int INDEX_BAD_BISHOP 					= 11;
	public static final int INDEX_SIDE_TO_MOVE 					= 12;
	public static final int INDEX_QUEEN_NIGHT 					= 13;
	public static final int INDEX_PAWN_BACKWARD 				= 14;
	public static final int INDEX_DOUBLE_PAWN_ATTACK 			= 15;

	public static final int[] INDIVIDUAL_SCORES = new int[] { 
			32,	// OPENING MOVE COUNTER
			10,	// KING MOVING OPENINGGAME
			16,	// QUEEN MOVING OPENINGGAME 
			48,	// ROOK PRISON
			18,	// ROOK FILE SEMI OPEN 
			22,	// ROOK FILE OPEN
			58,	// BISHOP DOUBLE 
			60,	// BISHOP PRISON 
			12,	// PAWN DOUBLE
			16,	// PAWN ISOLATED
			30,	// ROOK PAIR
			16,	// BAD BISHOP
			10,	// SIDE TO MOVE
			18,	// QUEEN NIGHT
			12,	// PAWN BACKWARD
			90	// DOUBLE PAWN ATTACK
	};
	
	public static final int[] PHASE = new int[] { 0, 0, 10, 10, 20, 40 };
	
	public static final int[] MATERIAL_SCORES 				= new int[] {0, 90, 395, 410, 670, 1235, 3000};
	public static final int[] PINNED_PIECE_SCORES 			= new int[] {0, -4, 50, 42, 52, 88, 0};
	public static final int[] PASSED_PAWN_SCORE_EG 			= new int[] {0, 15, 15, 35, 70, 145, 235, 0};
	public static final int[] PAWN_SHIELD_BONUS 			= new int[] {0, 28, 24, 8, 36, 170, 320, 0};
	public static final int[] KNIGHT_OUTPOST				= new int[] {0, 0, 8, 28, 28, 28, -4, 36};
	public static final int[] BISHOP_OUTPOST				= new int[] {0, 0, 24, 20, 20, 16, 40, 4};
	public static final int[] NIGHT_PAWN_BONUS				= new int[] {-28, -28, -16, -10, -4, 2, 10, 24, 40};
	
	public static final int[] PASSED_PAWN_MULTIPLIERS	= new int[] {
			13,	// protected
			21,	// blocked
			20	// endgame vs midgame
	};	
	
	//concept borrowed from Ed Schroder
	public static final int[] KING_SAFETY_SCORES = { 
			50, 40, 45, 55, 40, 60, 45, 55, 65, 55, 70, 65, 90, 90, 
			115, 125, 160, 160, 215, 225, 300, 345, 390, 450, 425, 
			1500, 1010, 745, 1300, 500, 600, 1500, 1500, 1500, 1500, 
			1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500}; //not used
	public static final int[] KING_SAFETY_QUEEN_TROPISM = new int[] {0, 0, 2, 2, 2, 0, 0, 0};	// index 0 and 1 are never evaluated	
	public static final int[] KING_SAFETY_COUNTER_RANKS = new int[] {2, 0, 0, 1, 0, 0, 0, 4};
	public static final int[] KING_SAFETY_ATTACK_PATTERN_COUNTER = {	
		 //                                                 Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  
		 // 	                    R  R  R  R  R  R  R  R                          R  R  R  R  R  R  R  R  
		 //             B  B  B  B              B  B  B  B              B  B  B  B              B  B  B  B  
		 //       N  N        N  N        N  N        N  N        N  N        N  N        N  N        N  N  
		 //    P     P     P     P     P     P     P     P     P     P     P     P     P     P     P     P
			0, 2, 0, 5, 2, 4, 3, 6, 2, 2, 3, 5, 4, 4, 8, 7, 0, 0, 4, 4, 3, 4, 6, 7, 2, 3, 5, 6, 5, 5, 4, 8
	};
	
	public static final int[] KING_SAFETY_COUNTERS	= new int[] {		
			2,		// FRONT_ATTACKED
			1,		// FRONT_NO_FRIENDLY_NEARBY
			1,		// NEXT_BEHIND_ATTACKED
			1,		// NEXT_BEHIND_NO_FRIENDLY_NEARBY
			3		// SIDE_TO_MOVE 
	};		
	
	public static final int[] KKR_KKQ_KING_DISTANCE_SCORE = new int[]{0, 0, 60, 40, 30, 20, 10, 0};
		
	public static final int[] MOBILITY_KNIGHT		= new int[] {-50, -26, -14, -10, 0, 8, 12, 14, 14};
	public static final int[] MOBILITY_BISHOP 		= new int[] {-32, -14, -2, 6, 12, 18, 24, 28, 32, 34, 36, 36, 38, 44};
	public static final int[] MOBILITY_ROOK 		= new int[] {-28, -18, -14, -10, -6, 4, 10, 14, 20, 22, 26, 28, 30, 28, 28};
	public static final int[] MOBILITY_QUEEN 		= new int[] {-40, -42, -38, -36, -26, -18, -20, -16, -12, -8, -4, 0, 0, 8, 
			14, 16, 18, 26, 32, 34, 36, 44, 46, 48, 34, 52, 38, 40};
	public static final int[] MOBILITY_KING			= new int[] {34, 24, 16, 8, 0, -22, -24, -26, -46};
		
	/** piece, color, square */
	public static final int[][][] PSQT_SCORES			= new int[7][2][64];
	public static final int[][][] PSQT_EG_SCORES		= new int[7][2][64];
	
	static
	{	
		PSQT_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				 0,  0,  0,  0,  0,  0,  0,  0,
				  80, 50, 30,110,110, 30, 50, 80,
				  15, 25, 60, 40, 40, 60, 25, 15,
				   0, 15, 20, 40, 40, 20, 15,  0,
				 -10, -5, 15, 35, 35, 15, -5,-10,
				   0, 10, 10, 20, 20, 10, 10,  0,
				 -15, 15,  0,  5,  5,  0, 15,-15,
				   0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				 0,  0,  0,  0,  0,  0,  0,  0,
				  75, 65, 40, 10, 10, 40, 65, 75,
				  65, 55, 20,  0,  0, 20, 55, 65,
				  40, 30, 20,  5,  5, 20, 30, 40,
				  30, 25, 15, 10, 10, 15, 25, 30,
				  15, 10, 15, 20, 20, 15, 10, 15,
				  20, 10, 25, 25, 25, 25, 10, 20,
				   0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				-220,-110,-130,-35,-35,-130,-110,-220,
				 -75,-45, 40,-20,-20, 40,-45,-75,
				 -40, 55, 45, 55, 55, 45, 55,-40,
				  15, 20, 30, 35, 35, 30, 20, 15,
				  -5, 30, 30, 30, 30, 30, 30, -5,
				 -15, 20, 25, 40, 40, 25, 20,-15,
				 -10,-15, 10, 30, 30, 10,-15,-10,
				 -50,-10,-20,  0,  0,-20,-10,-50
		};
		
		PSQT_EG_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				-25,-10, 20,  5,  5, 20,-10,-25,
				   0, 20, -5, 30, 30, -5, 20,  0,
				   0, -5, 20, 20, 20, 20, -5,  0,
				  10, 15, 35, 40, 40, 35, 15, 10,
				  10, 10, 30, 35, 35, 30, 10, 10,
				   0,  0, 10, 25, 25, 10,  0,  0,
				 -20,  5,  5,  5,  5,  5,  5,-20,
				 -25,-20, 10, 10, 10, 10,-20,-25
		};
		
		PSQT_SCORES[ChessConstants.BISHOP][WHITE] = new int[] {
				-45,-30,-105,-95,-95,-105,-30,-45,
				 -75, 25,-25,-25,-25,-25, 25,-75,
				 -20, 30, 30, 15, 15, 30, 30,-20,
				  -5, -5, 15, 40, 40, 15, -5, -5,
				   5, 15, 10, 35, 35, 10, 15,  5,
				  15, 30, 40, 20, 20, 40, 30, 15,
				  15, 45, 30, 25, 25, 30, 45, 15,
				 -15, 10,  5, 20, 20,  5, 10,-15
		};
		
		PSQT_EG_SCORES[ChessConstants.BISHOP][WHITE] = new int[]{	
				 -25,-20,-10, -5, -5,-10,-20,-25,
				   0,-25, -5,-15,-15, -5,-25,  0,
				  -5,-20,-15,-20,-20,-15,-20, -5,
				 -10,-10,-10,-10,-10,-10,-10,-10,
				 -25,-20,-10,-10,-10,-10,-20,-25,
				 -25,-20,-15, -5, -5,-15,-20,-25,
				 -40,-30,-25,-20,-20,-25,-30,-40,
				 -30,-25,-20,-20,-20,-20,-25,-30
		};
		
		PSQT_SCORES[ChessConstants.ROOK][WHITE] = new int[] {
				-25, 15,-55, 10, 10,-55, 15,-25,
				  10, -5, 45, 55, 55, 45, -5, 10,
				 -10, 25, 20, 10, 10, 20, 25,-10,
				 -35,-15, 10, 20, 20, 10,-15,-35,
				 -35,-10,-15,  0,  0,-15,-10,-35,
				 -35,-10,  0,  0,  0,  0,-10,-35,
				 -45,-10, -5, 10, 10, -5,-10,-45,
				   0, -5, 15, 25, 25, 15, -5,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.ROOK][WHITE] = new int[]{	
				 35, 20, 40, 20, 20, 40, 20, 35,
				  25, 30, 15,  5,  5, 15, 30, 25,
				  20, 15, 10, 10, 10, 10, 15, 20,
				  25, 15, 20,  5,  5, 20, 15, 25,
				  15, 10, 15,  5,  5, 15, 10, 15,
				   5,  5, -5, -5, -5, -5,  5,  5,
				   5, -5,  0, -5, -5,  0, -5,  5,
				  -5,  5,  0, -5, -5,  0,  5, -5
		};
		
		PSQT_SCORES[ChessConstants.QUEEN][WHITE] = new int[] {
				-70,-65,-110,-85,-85,-110,-65,-70,
				 -15,-60,-40,-80,-80,-40,-60,-15,
				  -5,-20,-25,-70,-70,-25,-20, -5,
				 -30,-45,-40,-55,-55,-40,-45,-30,
				 -15,-25,-15,-25,-25,-15,-25,-15,
				  -5, 15,  0,  5,  5,  0, 15, -5,
				  -5, 15, 35, 15, 15, 35, 15, -5,
				  25, 15, 20, 15, 15, 20, 15, 25
		};
		
		PSQT_EG_SCORES[ChessConstants.QUEEN][WHITE] = new int[]{	
				30, 45, 70, 40, 40, 70, 45, 30,
				  -5, 25, 25, 60, 60, 25, 25, -5,
				 -10, 15, 20, 70, 70, 20, 15,-10,
				  30, 50, 30, 65, 65, 30, 50, 30,
				   5, 45, 15, 40, 40, 15, 45,  5,
				   5,-20, 10,-10,-10, 10,-20,  5,
				 -30,-45,-50,-45,-45,-50,-45,-30,
				 -55,-50,-45,-45,-45,-45,-50,-55
		};
		
		PSQT_SCORES[ChessConstants.KING][WHITE] = new int[] {
				-115,135, -5, 35, 35, -5,135,-115,
				  25, 85, 15, 70, 70, 15, 85, 25,
				  25,120,120, 40, 40,120,120, 25,
				 -75,-20,-35,-35,-35,-35,-20,-75,
				 -85,-35,-40,-60,-60,-40,-35,-85,
				 -35, -5,-25,-20,-20,-25, -5,-35,
				   5, 20,-50,-25,-25,-50, 20,  5,
				 -20, 10,-25, 25, 25,-25, 10,-20
		};
		
		PSQT_EG_SCORES[ChessConstants.KING][WHITE] = new int[] {
				-35,-40, 15,-25,-25, 15,-40,-35,
				 -10,  5, 45, 20, 20, 45,  5,-10,
				   5, 30, 35, 30, 30, 35, 30,  5,
				  15, 45, 55, 55, 55, 55, 45, 15,
				   0, 20, 40, 50, 50, 40, 20,  0,
				 -10, 10, 25, 35, 35, 25, 10,-10,
				 -30,-10, 20, 25, 25, 20,-10,-30,
				 -65,-35,-10,-30,-30,-10,-35,-65
		};
		
	}
	
	public static final int QUEEN_PROMOTION_SCORE 	= MATERIAL_SCORES[ChessConstants.QUEEN] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int KNIGHT_PROMOTION_SCORE 	= MATERIAL_SCORES[ChessConstants.NIGHT] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int ROOK_PROMOTION_SCORE 	= MATERIAL_SCORES[ChessConstants.ROOK] - MATERIAL_SCORES[ChessConstants.PAWN];
	public static final int BISHOP_PROMOTION_SCORE 	= MATERIAL_SCORES[ChessConstants.BISHOP] - MATERIAL_SCORES[ChessConstants.PAWN];

	static {

		// fix white arrays
		for(int piece=ChessConstants.PAWN; piece<=ChessConstants.KING; piece++){
			Util.reverse(PSQT_SCORES[piece][ChessConstants.WHITE]);
			Util.reverse(PSQT_EG_SCORES[piece][ChessConstants.WHITE]);
		}

		// create black arrays
		for(int piece=ChessConstants.PAWN; piece<=ChessConstants.KING; piece++){
			for (int i = 0; i < 64; i++) {
				PSQT_SCORES[piece][BLACK][i] = -PSQT_SCORES[piece][WHITE][63 - i];
				PSQT_EG_SCORES[piece][BLACK][i] = -PSQT_EG_SCORES[piece][WHITE][63 - i];
			}
		}
		
	}

	public static final long[] BAD_BISHOP = new long[] { 
			0, Bitboard.F2, 0, 0, 0, 0, Bitboard.C2, 0, 
			Bitboard.G3, 0, 0, 0, 0, 0, 0, Bitboard.B3, 
			Bitboard.G4, 0, 0, 0, 0, 0, 0, Bitboard.B4, 
			0,           0, 0, 0, 0, 0, 0,           0, 
			0,           0, 0, 0, 0, 0, 0,           0,
			Bitboard.G5, 0, 0, 0, 0, 0, 0, Bitboard.B5, 
			Bitboard.G6, 0, 0, 0, 0, 0, 0, Bitboard.B6, 
			0, Bitboard.F7, 0, 0, 0, 0, Bitboard.C7, 0 
	};
	
	public static final long[] BISHOP_PRISON = new long[] { 
			0,           0, 0, 0, 0, 0, 0,           0, 
			Bitboard.G3, 0, 0, 0, 0, 0, 0, Bitboard.B3, 
			0,           0, 0, 0, 0, 0, 0,           0, 
			0,           0, 0, 0, 0, 0, 0,           0, 
			0,           0, 0, 0, 0, 0, 0,           0,
			0,           0, 0, 0, 0, 0, 0,           0, 
			Bitboard.G6, 0, 0, 0, 0, 0, 0, Bitboard.B6, 
			0,           0, 0, 0, 0, 0, 0,           0
	};
	
	public static final long[] ROOK_PRISON = new long[] { 
			0, Bitboard.H1, Bitboard.G1_H1, 0, 0, Bitboard.A1_B1, Bitboard.A1, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, Bitboard.H8, Bitboard.G8_H8, 0, 0, Bitboard.A8_B8, Bitboard.A8, 0 
	};

}
