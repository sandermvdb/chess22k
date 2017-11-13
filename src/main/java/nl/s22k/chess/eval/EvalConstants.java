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

	public static final int SIDE_TO_MOVE_BONUS = 10; //cannot be tuned //TODO lower in endgame
	
	public static final int SCORE_DRAW 						= 0;
	public static final int SCORE_DRAWISH					= 10;
	public static final int SCORE_DRAWISH_KING_CORNERED		= 20;
	public static final int SCORE_MATE_BOUND 				= 32000;

	public static final int INDEX_OPENING_MOVE_COUNTER	 		= 0;
	public static final int INDEX_KING_MOVING_OPENINGGAME 		= 1;
	public static final int INDEX_QUEEN_MOVING_OPENINGGAME 		= 2;
	public static final int INDEX_ROOK_PRISON 					= 3;
	public static final int INDEX_ROOK_FILE_SEMI_OPEN	 		= 4;
	public static final int INDEX_ROOK_FILE_OPEN 				= 5;
	public static final int INDEX_BISHOP_DOUBLE 				= 6;
	public static final int INDEX_PAWN_DOUBLE 					= 7;
	public static final int INDEX_PAWN_ISOLATED 				= 8;
	public static final int INDEX_ROOK_PAIR		 				= 9;
	public static final int INDEX_QUEEN_NIGHT 					= 10;
	public static final int INDEX_PAWN_BACKWARD 				= 11;
	public static final int INDEX_MULTIPLE_PAWN_ATTACKS 		= 12;
	public static final int INDEX_PAWN_CONNECTED 				= 13;
	public static final int INDEX_PAWN_ATTACKS 					= 14;
	public static final int INDEX_BISHOP_PRISON 				= 15;
	public static final int INDEX_QUEEN_ATTACKED 				= 16;
	public static final int INDEX_PAWN_PUSH_THREAT 				= 17;
	public static final int INDEX_ROOK_7TH_RANK 				= 18;
	public static final int INDEX_ROOK_PASSED_PAWN_FILE 		= 19;
	public static final int INDEX_NIGHT_PAIR 					= 20;

	public static final int[] INDIVIDUAL_SCORES = new int[] { 
			32,	// OPENING MOVE COUNTER
			4,	// KING MOVING OPENINGGAME
			8,	// QUEEN MOVING OPENINGGAME 
			50,	// ROOK PRISON
			14,	// ROOK FILE SEMI OPEN 
			26,	// ROOK FILE OPEN
			52,	// BISHOP DOUBLE 
			12,	// PAWN DOUBLE
			12,	// PAWN ISOLATED
			26,	// ROOK PAIR
			16,	// QUEEN NIGHT
			12,	// PAWN BACKWARD
			48,	// MULTIPLE PAWN ATTACKS
			10,	// PAWN CONNECTED
			38,	// PAWN ATTACKS
			140,// BISHOP PRISON - low error improvement...
			44,	// QUEEN ATTACKED
			18,	// PAWN PUSH THREAT
			20,	// ROOK 7TH RANK
			16	// ROOK ON SAME FILE AS PASSED PAWN
	};
	
	public static final int[] PHASE = new int[] { 0, 0, 10, 10, 20, 40 };
	
	public static final int[] MATERIAL_SCORES 				= new int[] {0, 90, 395, 410, 670, 1210, 3000};
	public static final int[] PINNED_PIECE_SCORES 			= new int[] {0, 0, 34, 42, 56, 92};
	public static final int[] PASSED_PAWN_SCORE_EG 			= new int[] {0, 15, 15, 45, 80, 155, 260};
	public static final int[] PASSED_PAWN_CANDIDATE			= new int[] {0, -5, 0, 10, 15, 40, 0};
	public static final int[] PAWN_SHIELD_BONUS 			= new int[] {0, 28, 16, 0, 32, 162, 352};
	public static final int[] KNIGHT_OUTPOST				= new int[] {0, 0, 8, 24, 20, 24, -4, 20};
	public static final int[] BISHOP_OUTPOST				= new int[] {0, 0, 24, 20, 16, 12, 36, 12};
	public static final int[] NIGHT_PAWN_BONUS				= new int[] {-24, -24, -12, -6, 0, 6, 14, 28, 44};
	public static final int[] HANGING_PIECES 				= new int[] {0, 20, 44, 40, 44, 0};
	public static final int[] HANGING_PIECES_2 				= new int[] {0, 4, 16, 20, 12, -12};
	
	public static final int[] PASSED_PAWN_MULTIPLIERS	= new int[] {
			20,	// blocked
			39	// endgame vs midgame
	};	
	
	//concept borrowed from Ed Schroder
	public static final int[] KING_SAFETY_SCORES = { 
			50, 40, 30, 40, 60, 50, 50, 50, 50, 60, 70, 70, 80, 90, 100, 110, 
			120, 130, 160, 170, 210, 240, 290, 360, 390, 430, 450, 550, 650, 
			750, 860, 1400, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 
			1500, 1500, 1500, 1500, 1500, 1500,
			1500, 1500, 1500, 1500, 1500, 1500,
			1500, 1500, 1500, 1500, 1500, 1500}; //not used
	public static final int[] KING_SAFETY_QUEEN_TROPISM = new int[] {0, 0, 0, 1, 1, 1, 0, 0};	// index 0 and 1 are never evaluated	
	public static final int[] KING_SAFETY_COUNTER_RANKS = new int[] {4, 0, 0, 0, 0, 0, 4, 8};
	public static final int[] KING_SAFETY_CHECK 		= new int[] {0, 0, 3, 2, 3};
	public static final int[] KING_SAFETY_CHECK_QUEEN 	= new int[] {0, 0, 0, 0, 0, 1, 2, 3, 4, 4, 5, 4, 4, 3, 2, 2, 1};
	public static final int[] KING_SAFETY_ATTACK_PATTERN_COUNTER = {	
		 //                                                 Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  
		 // 	                    R  R  R  R  R  R  R  R                          R  R  R  R  R  R  R  R  
		 //             B  B  B  B              B  B  B  B              B  B  B  B              B  B  B  B  
		 //       N  N        N  N        N  N        N  N        N  N        N  N        N  N        N  N  
		 //    P     P     P     P     P     P     P     P     P     P     P     P     P     P     P     P
			3, 1, 2, 3, 2, 1, 3, 3, 1, 1, 2, 3, 2, 1, 6, 4, 1, 0, 2, 1, 1, 1, 4, 4, 1, 1, 3, 4, 3, 3, 5, 4
	};
	
	public static final int[] KING_SAFETY_COUNTERS	= new int[] {		
			3		// QUEEN-TOUCH CHECK
	};		
	
	public static final int[] KKR_KKQ_KING_DISTANCE_SCORE = new int[]{0, 0, 60, 40, 30, 20, 10, 0};
		
	public static final int[] MOBILITY_KNIGHT		= new int[] {-42, -22, -10, -6, 4, 12, 20, 26, 46};
	public static final int[] MOBILITY_BISHOP 		= new int[] {-12, 2, 14, 22, 24, 30, 36, 36, 40, 42, 40, 40, 34, 72};
	public static final int[] MOBILITY_ROOK 		= new int[] {-24, -18, -18, -10, -10, 0, 6, 10, 24, 26, 30, 44, 50, 52, 64};
	public static final int[] MOBILITY_QUEEN 		= new int[] {-32, -38, -30, -28, -18, -10, -16, -12, -8, -4, 0, -4, -8, 0, 2, 0, 2, 10, 16, 2, 8, 8, 26, 76, 102, 100, 130, 140};
	public static final int[] MOBILITY_KING			= new int[] {2, 0, 0, 4, 4, 10, 8, 26, 62};
	
	public static final int[] MOBILITY_KNIGHT_EG	= new int[] {-86, -38, -18, -2, 4, 12, 12, 14, 6};
	public static final int[] MOBILITY_BISHOP_EG	= new int[] {-56, -30, -14, -2, 12, 22, 28, 36, 40, 42, 44, 48, 54, 52};
	public static final int[] MOBILITY_ROOK_EG 		= new int[] {-56, -18, 6, 6, 14, 20, 26, 26, 28, 30, 34, 32, 34, 32, 32};
	public static final int[] MOBILITY_QUEEN_EG 	= new int[] {-8, -22, -62, -96, -94, -78, -52, -24, -44, -24, -28, 4, 8, 20, 26, 28, 22, 30, 32, 46, 44, 52, 46, 20, -18, 24, -30, -8};
	public static final int[] MOBILITY_KING_EG		= new int[] {-30, 0, 8, 8, 4, -14, -12, -30, -74};
		
	/** piece, color, square */
	public static final int[][][] PSQT_SCORES			= new int[7][2][64];
	public static final int[][][] PSQT_EG_SCORES		= new int[7][2][64];
	
	static
	{	
		PSQT_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				0,  0,  0,  0,  0,  0,  0,  0,
				 120, 70, 70,140,140, 70, 70,120,
				  20, 30, 70, 55, 55, 70, 30, 20,
				  -5, 15, 15, 35, 35, 15, 15, -5,
				 -15, -5, 15, 30, 30, 15, -5,-15,
				 -15,  0,  0,  5,  5,  0,  0,-15,
				 -20, 15,  5,  5,  5,  5, 15,-20,
				   0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				0,  0,  0,  0,  0,  0,  0,  0,
				  55, 50, 25,  0,  0, 25, 50, 55,
				  65, 55, 20,  0,  0, 20, 55, 65,
				  40, 30, 20,  5,  5, 20, 30, 40,
				  25, 20, 10,  0,  0, 10, 20, 25,
				  15, 10, 15, 15, 15, 15, 10, 15,
				  20, 10, 20, 25, 25, 20, 10, 20,
				   0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				-215,-95,-125,-35,-35,-125,-95,-215,
				 -75,-55, 30,-25,-25, 30,-55,-75,
				 -25, 55, 25, 30, 30, 25, 55,-25,
				  25, 30, 35, 35, 35, 35, 30, 25,
				   5, 30, 30, 35, 35, 30, 30,  5,
				 -10, 25, 25, 40, 40, 25, 25,-10,
				  -5, -5, 15, 30, 30, 15, -5, -5,
				 -35, -5,-15, 10, 10,-15, -5,-35
		};
		
		PSQT_EG_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				-15,-10, 25, 10, 10, 25,-10,-15,
				   0, 25,  0, 35, 35,  0, 25,  0,
				  -5, -5, 30, 30, 30, 30, -5, -5,
				  10, 20, 40, 50, 50, 40, 20, 10,
				   5, 15, 35, 40, 40, 35, 15,  5,
				   0,  0,  5, 25, 25,  5,  0,  0,
				 -20,  0,  0,  5,  5,  0,  0,-20,
				 -20,-20,  0,  5,  5,  0,-20,-20
		};
		
		PSQT_SCORES[ChessConstants.BISHOP][WHITE] = new int[] {
				-15,-10,-95,-75,-75,-95,-10,-15,
				 -60, 20,-20,-20,-20,-20, 20,-60,
				  20, 45, 50, 25, 25, 50, 45, 20,
				   5, 15, 25, 45, 45, 25, 15,  5,
				  10, 25, 20, 40, 40, 20, 25, 10,
				  15, 30, 40, 25, 25, 40, 30, 15,
				  20, 55, 35, 30, 30, 35, 55, 20,
				  -5, 15, 10, 30, 30, 10, 15, -5
		};
		
		PSQT_EG_SCORES[ChessConstants.BISHOP][WHITE] = new int[]{	
				-20,-15, -5,  0,  0, -5,-15,-20,
				   0,-20, -5,-15,-15, -5,-20,  0,
				 -10,-20,-15,-15,-15,-15,-20,-10,
				  -5,-10, -5,-10,-10, -5,-10, -5,
				 -20,-20,-10,-10,-10,-10,-20,-20,
				 -20,-20,-15,-10,-10,-15,-20,-20,
				 -30,-35,-25,-20,-20,-25,-35,-30,
				 -25,-20,-15,-20,-20,-15,-20,-25
		};
		
		PSQT_SCORES[ChessConstants.ROOK][WHITE] = new int[] {
				-45,-20,-80,-10,-10,-80,-20,-45,
				 -40,-40, 10, 20, 20, 10,-40,-40,
				 -35, 15, 10,-10,-10, 10, 15,-35,
				 -40,-20, 15, 10, 10, 15,-20,-40,
				 -40,-10,-10,  0,  0,-10,-10,-40,
				 -40,-15,-10, -5, -5,-10,-15,-40,
				 -50,-10, -5, 10, 10, -5,-10,-50,
				  -5,-10,  5, 20, 20,  5,-10, -5
		};
		
		PSQT_EG_SCORES[ChessConstants.ROOK][WHITE] = new int[]{	
				40, 35, 50, 30, 30, 50, 35, 40,
				  30, 30, 15,  5,  5, 15, 30, 30,
				  25, 20, 15, 20, 20, 15, 20, 25,
				  25, 20, 20, 15, 15, 20, 20, 25,
				  15, 15, 15, 10, 10, 15, 15, 15,
				   0, 10,  0,  0,  0,  0, 10,  0,
				   5,  0,  0, -5, -5,  0,  0,  5,
				   0, 10,  5, -5, -5,  5, 10,  0
		};
		
		PSQT_SCORES[ChessConstants.QUEEN][WHITE] = new int[] {
				-60,-60,-95,-60,-60,-95,-60,-60,
				 -35,-75,-35,-70,-70,-35,-75,-35,
				   0,-15,-30,-60,-60,-30,-15,  0,
				 -30,-40,-40,-55,-55,-40,-40,-30,
				 -20,-25,-10,-20,-20,-10,-25,-20,
				 -15,  5,-15, -5, -5,-15,  5,-15,
				  -5, 10, 30, 15, 15, 30, 10, -5,
				  15, 15, 15, 15, 15, 15, 15, 15
		};
		
		PSQT_EG_SCORES[ChessConstants.QUEEN][WHITE] = new int[]{	
				15, 25, 45, 10, 10, 45, 25, 15,
				   0, 20, -5, 35, 35, -5, 20,  0,
				 -15,-10, -5, 40, 40, -5,-10,-15,
				  15, 15,  5, 25, 25,  5, 15, 15,
				   5, 25,-10, 15, 15,-10, 25,  5,
				   5,-35, -5,-15,-15, -5,-35,  5,
				 -20,-45,-55,-40,-40,-55,-45,-20,
				 -45,-45,-40,-30,-30,-40,-45,-45
		};
		
		PSQT_SCORES[ChessConstants.KING][WHITE] = new int[] {
				 -35,215, 10, 55, 55, 10,215,-35,
				  65,  0,-55, -5, -5,-55,  0, 65,
				  35, 85, 50,-25,-25, 50, 85, 35,
				 -75,-60,-70,-80,-80,-70,-60,-75,
				 -95,-75,-95,-115,-115,-95,-75,-95,
				 -40,-35,-65,-65,-65,-65,-35,-40,
				  -5,-15,-70,-55,-55,-70,-15, -5,
				  -5, 15,-30, 20, 20,-30, 15, -5
		};
		
		PSQT_EG_SCORES[ChessConstants.KING][WHITE] = new int[] {
				-75,-75,  5,-45,-45,  5,-75,-75,
				 -35, 25, 55, 35, 35, 55, 25,-35,
				 -15, 35, 45, 40, 40, 45, 35,-15,
				   0, 45, 55, 55, 55, 55, 45,  0,
				 -15, 20, 45, 55, 55, 45, 20,-15,
				 -25, 10, 30, 40, 40, 30, 10,-25,
				 -45, -5, 25, 30, 30, 25, -5,-45,
				 -85,-55,-20,-35,-35,-20,-55,-85
		};
		
	}

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
	
	public static final long[] ROOK_PRISON = new long[] { 
			0, Bitboard.A8, Bitboard.A8_B8, Bitboard.A8_B8_C8, 0, Bitboard.G8_H8, Bitboard.H8, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, Bitboard.A1, Bitboard.A1_B1, Bitboard.A1_B1_C1, 0, Bitboard.G1_H1, Bitboard.H1, 0 
	};
	
	public static final long[] BISHOP_PRISON = new long[] { 
			0, 0, 0, 0, 0, 0, 0, 0, //8
			Bitboard.B6_C7, 0, 0, 0, 0, 0, 0, Bitboard.G6_F7, //7
			0, 0, 0, 0, 0, 0, 0, 0, //6
			0, 0, 0, 0, 0, 0, 0, 0, //5
			0, 0, 0, 0, 0, 0, 0, 0, //4
			0, 0, 0, 0, 0, 0, 0, 0, //3
			Bitboard.B3_C2, 0, 0, 0, 0, 0, 0, Bitboard.G3_F2, //2
			0, 0, 0, 0, 0, 0, 0, 0  //1
		 // A  B  C  D  E  F  G  H
	};
	
	static {
		Util.reverse(ROOK_PRISON);
		Util.reverse(BISHOP_PRISON);
	}
	
	public static final int[] PROMOTION_SCORE = new int[]{
			0,
			0,
			MATERIAL_SCORES[ChessConstants.NIGHT] 	- MATERIAL_SCORES[ChessConstants.PAWN],
			MATERIAL_SCORES[ChessConstants.BISHOP] 	- MATERIAL_SCORES[ChessConstants.PAWN],
			MATERIAL_SCORES[ChessConstants.ROOK] 	- MATERIAL_SCORES[ChessConstants.PAWN],
			MATERIAL_SCORES[ChessConstants.QUEEN] 	- MATERIAL_SCORES[ChessConstants.PAWN],
	};

}
