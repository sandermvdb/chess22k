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

	public static final int INDEX_ROOK_FILE_SEMI_OPEN	 		= 0;
	public static final int INDEX_ROOK_FILE_SEMI_OPEN_ISOLATED 	= 1;
	public static final int INDEX_ROOK_FILE_OPEN 				= 2;
	public static final int INDEX_BISHOP_DOUBLE 				= 3;
	public static final int INDEX_PAWN_DOUBLE 					= 4;
	public static final int INDEX_PAWN_ISOLATED 				= 5;
	public static final int INDEX_ROOK_PAIR		 				= 6;
	public static final int INDEX_QUEEN_NIGHT 					= 7;
	public static final int INDEX_PAWN_BACKWARD 				= 8;
	public static final int INDEX_MULTIPLE_PAWN_ATTACKS 		= 9;
	public static final int INDEX_PAWN_ATTACKS 					= 10;
	public static final int INDEX_BISHOP_PRISON 				= 11;
	public static final int INDEX_QUEEN_ATTACKED 				= 12;
	public static final int INDEX_PAWN_PUSH_THREAT 				= 13;
	public static final int INDEX_ROOK_7TH_RANK 				= 14;
	public static final int INDEX_ROOK_PASSED_PAWN_FILE 		= 15;
	public static final int INDEX_NIGHT_FORK 					= 16;
	public static final int INDEX_NIGHT_FORK_KING 				= 17;
	public static final int INDEX_ROOK_BATTERY 					= 18;

	public static final int[] INDIVIDUAL_SCORES = { 
			-6,	// ROOK FILE SEMI OPEN
			18,	// ROOK FILE SEMI OPEN ISOLATED
			22,	// ROOK FILE OPEN
			54,	// BISHOP DOUBLE 
			8,	// PAWN DOUBLE
			12,	// PAWN ISOLATED
			28,	// ROOK PAIR
			20,	// QUEEN NIGHT
			8,	// PAWN BACKWARD
			42,	// MULTIPLE PAWN ATTACKS
			48,	// PAWN ATTACKS
			158,// BISHOP PRISON - low error improvement...
			32,	// QUEEN ATTACKED
			16,	// PAWN PUSH THREAT
			16,	// ROOK 7TH RANK
			12,	// ROOK ON SAME FILE AS PASSED PAWN
			78, // NIGHT FORK
			308,// NIGHT FORK KING
			16	// ROOK BATTERY
	};
	
	public static final int[] PHASE = { 0, 0, 10, 10, 20, 40 };
	
	public static final int[] MATERIAL_SCORES 				= {0, 90, 395, 415, 680, 1265, 3000};
	public static final int[] PINNED_PIECE_SCORES 			= {0, 4, 34, 50, 60, 100};
	public static final int[] DISCOVERED_PIECE_SCORES 		= {0, -16, 116, 96, 180, 0, 28};
	public static final int[] KNIGHT_OUTPOST				= {0, 0, 4, 24, 20, 28, 0, 24};
	public static final int[] BISHOP_OUTPOST				= {0, 0, 24, 20, 20, 16, 36, 16};
	public static final int[] NIGHT_PAWN_BONUS				= {44, -28, -12, -6, 0, 6, 14, 28, 44};
	public static final int[] HANGING_PIECES 				= {0, 20, 40, 40, 56, 28};
	public static final int[] HANGING_PIECES_2 				= {0, 32, 84, 68, 64, 0};
	public static final int[] ROOK_TRAPPED 					= {64, 60, 28};
	
	public static final int[] PASSED_PAWN_SCORE_EG 			= {0, 15, 15, 40, 70, 140, 285};
	public static final int[] PASSED_PAWN_CANDIDATE			= {0, 0, 0, 10, 15, 40, 0};
	public static final int[][] PAWN_SHIELD_BONUS 			= {	{0, 4, 0, -8, 8, 110, 276},
																{0, 32, 16, -4, 32, 206, 288},
																{0, 32, -4, -8, 44, 170, 400}};
	public static final int[] BACKWARD_PAWN_ATTACKED 		= {24, 12, 12, 12, 8, 20, 12, 12, -4};
	public static final int[] PAWN_BLOCKAGE 				= {0, 0, -12, 0, 4, 28, 56, 180};
	public static final int[] PAWN_CONNECTED 				= {0, 0, 8, 8, 16, 48, 84};
	
	public static final int[] PASSED_PAWN_MULTIPLIERS	= {
			20,	// blocked
			42,	// endgame vs midgame
			7,	// next square attacked
			40	// enemy king in front
	};	
	
	//concept borrowed from Ed Schroder
	public static final int[] KING_SAFETY_SCORES = { 
			50, 40, 30, 0, 30, 30, 30, 30, 40, 50, 60, 70, 80, 90, 100, 110, 
			130, 150, 160, 190, 210, 240, 280, 330, 380, 430, 450, 550, 650, 
			750, 860, 1000, 1100, 1200, 1300, 1400, 1500};
	public static final int[] KING_SAFETY_QUEEN_TROPISM = {0, 0, 1, 1, 1, 1, 0, 0};	// index 0 and 1 are never evaluated	
	public static final int[] KING_SAFETY_COUNTER_RANKS = {4, 0, 0, 1, 1, 1, 4, 8};
	public static final int[] KING_SAFETY_CHECK_NIGHT	= {3, 0, 2, 3, 3, 4, 5, 3, 3};
	public static final int[] KING_SAFETY_CHECK_BISHOP	= {5, 2, 4, 4, 3, 2, 2, 1, 1};
	public static final int[] KING_SAFETY_CHECK_ROOK	= {0, 4, 4, 4, 4, 4, 4, 2, 3};
	public static final int[] KING_SAFETY_CHECK_QUEEN 	= {0, 0, 0, 0, 0, 1, 2, 3, 4, 4, 4, 4, 3, 3, 2, 2, 1};
	public static final int[] KING_SAFETY_NO_FRIENDS 	= {6, 3, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10};
	public static final int[] KING_SAFETY_ATTACKS 		= {0, 2, 2, 3, 4, 4, 5, 5, 6, 7, 6, 7, 10};
	public static final int[] KING_SAFETY_DOUBLE_PAWN 	= {7, 0, 0, 0, 2, 1, 2, 3, 3};
	public static final int[] KING_SAFETY_DOUBLE_NIGHT 	= {1, 0, 1, 2, 1, 3, 1, 0, 0};
	public static final int[] KING_SAFETY_DOUBLE_BISHOP	= {2, 0, 1, 0, 0, 2, 0, 1, 0};
	public static final int[] KING_SAFETY_DOUBLE_ROOK 	= {9, 0, 2, 1, 2, 3, 2, 0, 0};
	public static final int[] KING_SAFETY_ATTACK_PATTERN_COUNTER = {	
		 //                                                 Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  
		 // 	                    R  R  R  R  R  R  R  R                          R  R  R  R  R  R  R  R  
		 //             B  B  B  B              B  B  B  B              B  B  B  B              B  B  B  B  
		 //       N  N        N  N        N  N        N  N        N  N        N  N        N  N        N  N  
		 //    P     P     P     P     P     P     P     P     P     P     P     P     P     P     P     P
			5, 1, 3, 2, 3, 1, 3, 2, 2, 0, 2, 2, 2, 1, 4, 3, 2, 1, 3, 2, 2, 2, 4, 4, 1, 2, 4, 5, 3, 4, 6, 5
	};
	
	public static final int[] KING_SAFETY_COUNTERS	= {		
			4,		// queen-touch check possible
			10,		// king at blocked first rank check possible
			20		// screwed
	};		
	
	public static final int[] KKR_KKQ_KING_DISTANCE_SCORE = {0, 0, 60, 40, 30, 20, 10, 0};
		
	public static final int[] MOBILITY_KNIGHT		= {-42, -22, -10, -6, 8, 12, 20, 22, 46};
	public static final int[] MOBILITY_KNIGHT_EG	= {-86, -34, -14, -2, 4, 12, 12, 14, 2};
	public static final int[] MOBILITY_BISHOP 		= {-16, 2, 14, 22, 20, 34, 36, 36, 40, 42, 44, 44, 54, 76};
	public static final int[] MOBILITY_BISHOP_EG	= {-48, -26, -10, 2, 20, 22, 32, 36, 40, 42, 44, 44, 42, 52};
	public static final int[] MOBILITY_ROOK 		= {-24, -18, -18, -14, -14, -4, 2, 10, 20, 30, 38, 44, 58, 56, 68};
	public static final int[] MOBILITY_ROOK_EG 		= {-40, -18, -2, 6, 18, 20, 26, 26, 32, 30, 34, 36, 34, 40, 36};
	public static final int[] MOBILITY_QUEEN 		= {-40, -38, -34, -32, -22, -14, -12, -12, -8, -4, -4, 4, -8, 4, -2, 0, -2, 22, 24, 10, 40, 60, 42, 136, 154, 184, 122, 240};
	public static final int[] MOBILITY_QUEEN_EG 	= {40, -74, -94, -92, -94, -82, -68, -32, -28, -16, -8, -4, 16, 16, 30, 36, 46, 26, 40, 62, 52, 48, 46, 20, -22, -12, 18, -60};
	public static final int[] MOBILITY_KING			= {10, 0, 0, 4, 8, 14, 16, 42, 66};
	public static final int[] MOBILITY_KING_EG		= {-30, -4, 4, 8, 4, -14, -16, -30, -66};
		
	/** piece, color, square */
	public static final int[][][] PSQT_SCORES			= new int[7][2][64];
	public static final int[][][] PSQT_EG_SCORES		= new int[7][2][64];
	
	static
	{	
		PSQT_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				 0,  0,  0,  0,  0,  0,  0,  0,
				 150,105,110,130,130,110,105,150,
				  20, 30, 65, 55, 55, 65, 30, 20,
				 -10, 10, 10, 35, 35, 10, 10,-10,
				 -20,-15, 10, 20, 20, 10,-15,-20,
				 -15, -5,  0,  0,  0,  0, -5,-15,
				 -20, 15,  5,  5,  5,  5, 15,-20,
				   0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				0,  0,  0,  0,  0,  0,  0,  0,
				 -40,-35,-60,-65,-65,-60,-35,-40,
				  50, 35, 10, -5, -5, 10, 35, 50,
				  40, 25, 20,  5,  5, 20, 25, 40,
				  30, 25, 10,  5,  5, 10, 25, 30,
				  15, 10, 15, 15, 15, 15, 10, 15,
				  25, 15, 20, 30, 30, 20, 15, 25,
				   0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -210,-100,-135,-45,-45,-135,-100,-210,
				 -75,-55, 30,-20,-20, 30,-55,-75,
				 -25, 55, 25, 35, 35, 25, 55,-25,
				  25, 30, 35, 35, 35, 35, 30, 25,
				   0, 30, 35, 35, 35, 35, 30,  0,
				   0, 35, 35, 45, 45, 35, 35,  0,
				  -5,  0, 20, 35, 35, 20,  0, -5,
				 -30, -5,-10, 15, 15,-10, -5,-30
		};
		
		PSQT_EG_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				-20,-10, 20,  5,  5, 20,-10,-20,
				   0, 20, -5, 25, 25, -5, 20,  0,
				 -10, -5, 25, 20, 20, 25, -5,-10,
				  10, 20, 35, 40, 40, 35, 20, 10,
				  15, 15, 30, 35, 35, 30, 15, 15,
				   5,  5, 10, 25, 25, 10,  5,  5,
				 -10,  5,  0,  5,  5,  0,  5,-10,
				 -15,-10,  5, 10, 10,  5,-10,-15
		};
		
		PSQT_SCORES[ChessConstants.BISHOP][WHITE] = new int[] {
				-15, -5,-115,-90,-90,-115, -5,-15,
				 -65, 20,-20,-15,-15,-20, 20,-65,
				  15, 45, 50, 25, 25, 50, 45, 15,
				   5, 15, 25, 45, 45, 25, 15,  5,
				  15, 30, 20, 40, 40, 20, 30, 15,
				  25, 40, 45, 25, 25, 45, 40, 25,
				  20, 55, 35, 30, 30, 35, 55, 20,
				   0, 15, 10, 30, 30, 10, 15,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.BISHOP][WHITE] = new int[]{	
				-20,-15,  0,  0,  0,  0,-15,-20,
				   0,-20, -5,-15,-15, -5,-20,  0,
				 -10,-20,-15,-15,-15,-15,-20,-10,
				  -5,-10, -5,-10,-10, -5,-10, -5,
				 -15,-20, -5,-10,-10, -5,-20,-15,
				 -20,-20,-15,-10,-10,-15,-20,-20,
				 -30,-30,-25,-20,-20,-25,-30,-30,
				 -25,-20,-10,-20,-20,-10,-20,-25
		};
		
		PSQT_SCORES[ChessConstants.ROOK][WHITE] = new int[] {
				-40,-15,-80,  5,  5,-80,-15,-40,
				 -25,-25, 20, 35, 35, 20,-25,-25,
				 -25, 15,  0,  0,  0,  0, 15,-25,
				 -40,-20, 15, 15, 15, 15,-20,-40,
				 -40, -5,-10,  5,  5,-10, -5,-40,
				 -45,-10, -5,  0,  0, -5,-10,-45,
				 -50,  0,-10, 10, 10,-10,  0,-50,
				 -10,-15,  0, 15, 15,  0,-15,-10
		};
		
		PSQT_EG_SCORES[ChessConstants.ROOK][WHITE] = new int[]{	
				 40, 35, 55, 30, 30, 55, 35, 40,
				  25, 30, 15,  5,  5, 15, 30, 25,
				  25, 25, 25, 25, 25, 25, 25, 25,
				  35, 30, 30, 20, 20, 30, 30, 35,
				  25, 25, 25, 15, 15, 25, 25, 25,
				  15, 20, 10,  5,  5, 10, 20, 15,
				  15,  0, 10,  5,  5, 10,  0, 15,
				   5, 15, 10,  0,  0, 10, 15,  5
		};
		
		PSQT_SCORES[ChessConstants.QUEEN][WHITE] = new int[] {
				-45,-65,-70,-70,-70,-70,-65,-45,
				 -40,-85,-35,-70,-70,-35,-85,-40,
				   0,-10,-30,-50,-50,-30,-10,  0,
				 -35,-40,-35,-55,-55,-35,-40,-35,
				 -25,-25,-10,-25,-25,-10,-25,-25,
				 -10, 10,-10,-10,-10,-10, 10,-10,
				 -10,  5, 25, 15, 15, 25,  5,-10,
				   0, -5,  0, 15, 15,  0, -5,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.QUEEN][WHITE] = new int[]{	
				5, 20, 25, 15, 15, 25, 20,  5,
				   0, 15,-25, 20, 20,-25, 15,  0,
				 -15,-10,-15, 10, 10,-15,-10,-15,
				  35, 30,  0, 20, 20,  0, 30, 35,
				  15, 20,-20, 10, 10,-20, 20, 15,
				   5,-45,-15,-15,-15,-15,-45,  5,
				 -20,-45,-50,-20,-20,-50,-45,-20,
				 -40,-35,-30,-25,-25,-30,-35,-40
		};
		
		PSQT_SCORES[ChessConstants.KING][WHITE] = new int[] {
				 -125,205, 25, 30, 30, 25,205,-125,
				 -15,-20,-45,-10,-10,-45,-20,-15,
				  15, 90, 50, 10, 10, 50, 90, 15,
				 -75,-45,-35,-45,-45,-35,-45,-75,
				 -80,-70,-50,-70,-70,-50,-70,-80,
				 -35,-20,-30,-35,-35,-30,-20,-35,
				  -5,-15,-65,-70,-70,-65,-15, -5,
				  10, 25,-15, 20, 20,-15, 25, 10
		};
		
		PSQT_EG_SCORES[ChessConstants.KING][WHITE] = new int[] {
				 -80,-95,-15,-55,-55,-15,-95,-80,
				 -35, 15, 40, 25, 25, 40, 15,-35,
				 -30, 20, 30, 20, 20, 30, 20,-30,
				 -20, 25, 35, 35, 35, 35, 25,-20,
				 -35,  0, 15, 30, 30, 15,  0,-35,
				 -55,-20, -5,  5,  5, -5,-20,-55,
				 -80,-35, -5,  5,  5, -5,-35,-80,
				 -110,-80,-50,-55,-55,-50,-80,-110
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
	
	public static final long[] ROOK_PRISON = { 
			0, Bitboard.A8, Bitboard.A8_B8, Bitboard.A8_B8_C8, 0, Bitboard.G8_H8, Bitboard.H8, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, Bitboard.A1, Bitboard.A1_B1, Bitboard.A1_B1_C1, 0, Bitboard.G1_H1, Bitboard.H1, 0 
	};
	
	public static final long[] BISHOP_PRISON = { 
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
	
	public static final int[] PROMOTION_SCORE = {
			0,
			0,
			MATERIAL_SCORES[ChessConstants.NIGHT] 	- MATERIAL_SCORES[ChessConstants.PAWN],
			MATERIAL_SCORES[ChessConstants.BISHOP] 	- MATERIAL_SCORES[ChessConstants.PAWN],
			MATERIAL_SCORES[ChessConstants.ROOK] 	- MATERIAL_SCORES[ChessConstants.PAWN],
			MATERIAL_SCORES[ChessConstants.QUEEN] 	- MATERIAL_SCORES[ChessConstants.PAWN],
	};

}
