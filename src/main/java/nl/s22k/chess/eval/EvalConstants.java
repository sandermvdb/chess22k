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

	public static final int SIDE_TO_MOVE_BONUS = 16; //cannot be tuned //TODO lower in endgame
	public static final int IN_CHECK = 20;
	
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
	public static final int INDEX_BISHOP_PRISON 				= 9;
	public static final int INDEX_ROOK_7TH_RANK 				= 10;
	public static final int INDEX_ROOK_PASSED_PAWN_FILE 		= 11;
	public static final int INDEX_ROOK_BATTERY 					= 12;
	public static final int INDEX_SPACE 						= 13;
	public static final int INDEX_LONG_BISHOP 					= 14;
	
	// threats
	public static final int INDEX_MULTIPLE_PAWN_ATTACKS 		= 0;
	public static final int INDEX_PAWN_ATTACKS 					= 1;
	public static final int INDEX_QUEEN_ATTACKED 				= 2;
	public static final int INDEX_PAWN_PUSH_THREAT 				= 3;
	public static final int INDEX_NIGHT_FORK 					= 4;
	public static final int INDEX_NIGHT_FORK_KING 				= 5;
	public static final int INDEX_ROOK_ATTACKED 				= 6;
	public static final int INDEX_QUEEN_ATTACKED_MINOR			= 7;
	public static final int INDEX_MAJOR_ATTACKED				= 8;
	public static final int INDEX_UNUSED_OUTPOST				= 9;

	public static final int[] INDIVIDUAL_SCORES = { 
			-8,	// ROOK FILE SEMI OPEN
			14,	// ROOK FILE SEMI OPEN ISOLATED
			20,	// ROOK FILE OPEN
			54,	// BISHOP DOUBLE 
			4,	// PAWN DOUBLE
			10,	// PAWN ISOLATED
			28,	// ROOK PAIR
			20,	// QUEEN NIGHT
			8,	// PAWN BACKWARD
			148,// BISHOP PRISON - low error improvement...
			12,	// ROOK 7TH RANK
			12,	// ROOK ON SAME FILE AS PASSED PAWN
			22,	// ROOK BATTERY
			12,	// SPACE
			10	// LONG BISHOP
	};
	
	public static final int[] THREAT_SCORES = { 
			42,	// MULTIPLE PAWN ATTACKS
			50,	// PAWN ATTACKS
			60,	// QUEEN ATTACKED
			16,	// PAWN PUSH THREAT
			78, // NIGHT FORK
			318,// NIGHT FORK KING
			36,	// ROOK ATTACKED
			44,	// QUEEN ATTACKED MINOR
			14,	// MAJOR ATTACKED
			10	// UNUSED OUTPOSTS
	};
	
	public static final int[] PHASE = { 0, 0, 6, 6, 13, 28 };
	
	public static final int[] MATERIAL_SCORES 				= {0, 100, 395, 415, 700, 1295, 3000};
	public static final int[] PINNED_PIECE_SCORES 			= {0, -4, 18, 50, 76, 92};
	public static final int[] DISCOVERED_PIECE_SCORES 		= {0, -16, 116, 100, 176, 0, 28};
	public static final int[] KNIGHT_OUTPOST				= {0, 0, 12, 28, 24, 32, 0, 16};
	public static final int[] BISHOP_OUTPOST				= {0, 0, 24, 24, 20, 24, 44, 32};
	public static final int[] NIGHT_PAWN_BONUS				= {52, -8, 0, 2, 4, 6, 14, 28, 44};
	public static final int[] HANGING_PIECES 				= {0, 12, 12, 4, -8, -12, 40}; //qsearch could set the other in check
	public static final int[] HANGING_PIECES_2 				= {0, 32, 80, 64, 52, -256};
	public static final int[] ROOK_TRAPPED 					= {64, 64, 28};
	public static final int[] IMBALANCE 					= {0, 0, 0, 100, 55, -70, 0, -25, 5, -10, 5, -5}; // TODO negative values?!
	public static final int[] NO_MINOR_DEFENDERS 			= {0, 5, 10, 25, 5, 15, 0};
	public static final int[] SAME_COLORED_BISHOP_PAWN 		= {-20, -8, -4, 0, 8, 12, 20, 28, 36};
	
	public static final int[] PAWN_BLOCKAGE 				= {0, 0, -12, 0, 4, 24, 40, 140};
	public static final int[] PAWN_CONNECTED 				= {0, 0, 12, 12, 16, 52, 96};
	public static final int[] PAWN_NEIGHBOUR 				= {0, 0, 4, 12, 24, 76, 324};
	
	public static final int[][] PAWN_SHIELD_BONUS 			= {	{0, 8, 12, 0, 8, -50, -344}, //TODO low values for rank 0?
																{0, 40, 24, -4, 56, 130, -184},
																{0, 40, 4, 4, 80, 150, 12},
																{0, 0, 0, 0, 48, 152, -92}};

	public static final int[] PASSED_PAWN_SCORE 			= {0, 15, 20, 40, 70, 150, 310};
	public static final int[] PASSED_PAWN_CANDIDATE			= {0, 0, 0, 10, 15, 30};
	public static final int[] PASSED_PAWN_KING 				= {0, 7, 8, 9, 9, 10, 12, 14};
	public static final int[] PASSED_PAWN_MULTIPLIERS	= {
			16,	// blocked
			7,	// next square attacked
			33,	// enemy king in front
			9,	// next square defended
			13	// atacked
	};	
	
	//concept borrowed from Ed Schroder
	public static final int[] KING_SAFETY_SCORES = { //TODO negative values? //TODO first values are not used
			0, 0, 0, 0, 0, -90, -100, -80, -50, 0, 80, 100, 
			110, 120, 140, 160, 190, 220, 250, 320, 350, 410, 470, 560, 
			610, 610, 730, 680, 980, 1000, 1100, 1200, 1300, 1400, 1500};
	public static final int[] KING_SAFETY_QUEEN_TROPISM = {0, 0, 1, 1, 1, 1, 0, 0};	// index 0 and 1 are never evaluated	
	public static final int[] KING_SAFETY_COUNTER_RANKS = {0, 0, 1, 1, 0, 0, 0, 0};
	public static final int[] KING_SAFETY_CHECK			= {0, 0, 3, 2, 3};
	public static final int[] KING_SAFETY_UCHECK		= {0, 0, 1, 1, 1};
	public static final int[] KING_SAFETY_CHECK_QUEEN 	= {0, 0, 0, 0, 1, 3, 4, 4, 4, 3, 3, 3, 3, 2, 1, 0, 0};
	public static final int[] KING_SAFETY_NO_FRIENDS 	= {6, 4, 0, 5, 5, 5, 5, 6, 7, 8, 9, 10};
	public static final int[] KING_SAFETY_ATTACKS 		= {0, 3, 3, 3, 3, 3, 4, 4, 5, 6, 6, 7, 10};
	public static final int[] KING_SAFETY_ATTACK_PATTERN_COUNTER = {	
		 //                                                 Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  
		 // 	                    R  R  R  R  R  R  R  R                          R  R  R  R  R  R  R  R  
		 //             B  B  B  B              B  B  B  B              B  B  B  B              B  B  B  B  
		 //       N  N        N  N        N  N        N  N        N  N        N  N        N  N        N  N  
		 //    P     P     P     P     P     P     P     P     P     P     P     P     P     P     P     P
			5, 2, 3, 3, 3, 2, 3, 3, 2, 1, 2, 2, 2, 2, 2, 3, 3, 2, 3, 3, 3, 3, 3, 4, 2, 3, 4, 5, 4, 4, 5, 5
	};
	
	public static final int[] KING_SAFETY_COUNTERS	= {		
			4,		// queen-touch check
			5,		// king at blocked first rank check
			38		// screwed
	};		
		
	public static final int[] MOBILITY_KNIGHT		= {-34, -18, -6, -2, 8, 16, 24, 26, 46};
	public static final int[] MOBILITY_KNIGHT_EG	= {-90, -30, -10, 2, 8, 12, 12, 14, 2};
	public static final int[] MOBILITY_BISHOP 		= {-16, 2, 18, 22, 28, 34, 36, 36, 36, 42, 60, 88, 10, 112};
	public static final int[] MOBILITY_BISHOP_EG	= {-36, -10, 2, 18, 28, 30, 36, 40, 44, 42, 32, 32, 66, 32};
	public static final int[] MOBILITY_ROOK 		= {-28, -22, -18, -14, -14, -4, -2, 6, 16, 30, 34, 40, 54, 64, 72};
	public static final int[] MOBILITY_ROOK_EG 		= {-32, -14, 2, 6, 18, 20, 26, 26, 32, 30, 34, 40, 38, 40, 40};
	public static final int[] MOBILITY_QUEEN 		= {-24, -22, -18, -20, -14, -6, -8, -8, -8, -4, -4, 0, -8, 4, -2, 4, -6, 14, 20, 10, 36, 52, 42, 160, 162, 228, 110, 300};
	public static final int[] MOBILITY_QUEEN_EG 	= {32, -82, -110, -92, -82, -82, -60, -28, -8, -4, 8, 16, 32, 32, 46, 48, 66, 42, 60, 74, 56, 60, 50, 8, -18, -36, 38, -88};
	public static final int[] MOBILITY_KING			= {10, -4, -4, 0, 8, 26, 32, 62, 110};
	public static final int[] MOBILITY_KING_EG		= {-38, -8, 4, 8, 4, -14, -12, -26, -62};
		
	/** piece, color, square */
	public static final int[][][] PSQT_SCORES			= new int[7][2][64];
	public static final int[][][] PSQT_EG_SCORES		= new int[7][2][64];
	
	static
	{	
		PSQT_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				   0,  0,  0,  0,  0,  0,  0,  0,
				   185,155,145,190,190,145,155,185,
				    15, 30, 60, 55, 55, 60, 30, 15,
				   -15, -5, -5, 15, 15, -5, -5,-15,
				   -30,-25,-15,  5,  5,-15,-25,-30,
				   -30,-20,-15,-15,-15,-15,-20,-30,
				   -25,  5,-15, -5, -5,-15,  5,-25,
				     0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				   0,  0,  0,  0,  0,  0,  0,  0,
				   -70,-70,-75,-80,-80,-75,-70,-70,
				    35, 15,-10,-25,-25,-10, 15, 35,
				    30, 15,  5,-10,-10,  5, 15, 30,
				    15, 10,  0, -5, -5,  0, 10, 15,
				     5,  0,  5,  5,  5,  5,  0,  5,
				    10,  0, 10, 15, 15, 10,  0, 10,
				     0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -220,-115,-130,-45,-45,-130,-115,-220,
				 -75,-60, 20,-30,-30, 20,-60,-75,
				 -20, 55, 25, 40, 40, 25, 55,-20,
				  20, 35, 40, 45, 45, 40, 35, 20,
				   5, 35, 30, 35, 35, 30, 35,  5,
				  10, 35, 35, 40, 40, 35, 35, 10,
				  -5, -5, 20, 35, 35, 20, -5, -5,
				 -35,  0, -5, 20, 20, -5,  0,-35
		};
		
		PSQT_EG_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -15,  0, 25, 15, 15, 25,  0,-15,
				   0, 25,  0, 30, 30,  0, 25,  0,
				 -10,  0, 30, 25, 25, 30,  0,-10,
				  15, 20, 40, 40, 40, 40, 20, 15,
				  10, 15, 35, 40, 40, 35, 15, 10,
				   5, 10, 15, 30, 30, 15, 10,  5,
				 -10,  5,  5, 15, 15,  5,  5,-10,
				 -10, -5,  5, 10, 10,  5, -5,-10
		};
		
		PSQT_SCORES[ChessConstants.BISHOP][WHITE] = new int[] {
				 -10,  5,-85,-70,-70,-85,  5,-10,
				 -40, 10,  5, -5, -5,  5, 10,-40,
				  35, 45, 35, 30, 30, 35, 45, 35,
				  35, 30, 40, 50, 50, 40, 30, 35,
				  25, 30, 30, 50, 50, 30, 30, 25,
				  35, 50, 45, 35, 35, 45, 50, 35,
				  30, 60, 45, 40, 40, 45, 60, 30,
				   5, 25, 25, 45, 45, 25, 25,  5
		};
		
		PSQT_EG_SCORES[ChessConstants.BISHOP][WHITE] = new int[]{	
				 -35,-15,-10,-10,-10,-10,-15,-35,
				 -10,-25,-10,-15,-15,-10,-25,-10,
				 -15,-15,-15,-15,-15,-15,-15,-15,
				 -10,-10, -5,  0,  0, -5,-10,-10,
				 -25,-15, -5, -5, -5, -5,-15,-25,
				 -20,-15,-15,  0,  0,-15,-15,-20,
				 -35,-40,-25,-15,-15,-25,-40,-35,
				 -30,-15,-10,-20,-20,-10,-15,-30
		};
		
		PSQT_SCORES[ChessConstants.ROOK][WHITE] = new int[] {
				 -40,-25,-70,  0,  0,-70,-25,-40,
				 -30,-15,  5, 20, 20,  5,-15,-30,
				 -30,  0,  5,-10,-10,  5,  0,-30,
				 -45,-20, 10,  5,  5, 10,-20,-45,
				 -45,-15,-20,  0,  0,-20,-15,-45,
				 -40,-10,  0,  0,  0,  0,-10,-40,
				 -50, -5,-10, 10, 10,-10, -5,-50,
				 -10,-15,  0, 15, 15,  0,-15,-10
		};
		
		PSQT_EG_SCORES[ChessConstants.ROOK][WHITE] = new int[]{	
				  40, 40, 55, 35, 35, 55, 40, 40,
				  30, 30, 25, 15, 15, 25, 30, 30,
				  30, 35, 25, 30, 30, 25, 35, 30,
				  40, 35, 35, 30, 30, 35, 35, 40,
				  30, 30, 30, 20, 20, 30, 30, 30,
				  20, 20, 10, 10, 10, 10, 20, 20,
				  20,  5, 15, 10, 10, 15,  5, 20,
				   5, 15, 10,  0,  0, 10, 15,  5
		};
		
		PSQT_SCORES[ChessConstants.QUEEN][WHITE] = new int[] {
				 -60,-35,-45,-55,-55,-45,-35,-60,
				 -40,-95,-60,-75,-75,-60,-95,-40,
				 -10,-35,-45,-70,-70,-45,-35,-10,
				 -40,-50,-45,-65,-65,-45,-50,-40,
				 -25,-35,-25,-35,-35,-25,-35,-25,
				  -5, 10,-15,-10,-10,-15, 10, -5,
				  -5, 15, 25, 20, 20, 25, 15, -5,
				   5,  0,  5, 25, 25,  5,  0,  5
		};
		
		PSQT_EG_SCORES[ChessConstants.QUEEN][WHITE] = new int[]{	
				  20,  5, 25, 30, 30, 25,  5, 20,
				  -10, 10,  0, 25, 25,  0, 10,-10,
				  -10,  5, 10, 50, 50, 10,  5,-10,
				   45, 35, 10, 35, 35, 10, 35, 45,
				   15, 20,  0, 20, 20,  0, 20, 15,
				   -5,-35,  0,-10,-10,  0,-35, -5,
				  -25,-50,-50,-25,-25,-50,-50,-25,
				  -45,-40,-40,-40,-40,-40,-40,-45
		};
		
		PSQT_SCORES[ChessConstants.KING][WHITE] = new int[] {
				 -50,170,-35,  0,  0,-35,170,-50,
				  25, 15,-65,-20,-20,-65, 15, 25,
				  35, 60, 15,-35,-35, 15, 60, 35,
				 -55,-45,-60,-75,-75,-60,-45,-55,
				 -60,-45,-35,-80,-80,-35,-45,-60,
				 -15,-10,-30,-35,-35,-30,-10,-15,
				   5,-10,-60,-70,-70,-60,-10,  5,
				  10, 25,-15,  5,  5,-15, 25, 10
		};
		
		PSQT_EG_SCORES[ChessConstants.KING][WHITE] = new int[] {
				 -105,-100,-10,-65,-65,-10,-100,-105,
				 -50,-10, 25,  5,  5, 25,-10,-50,
				 -25, 15, 25, 15, 15, 25, 15,-25,
				 -15, 20, 30, 25, 25, 30, 20,-15,
				 -30, -5, 10, 25, 25, 10, -5,-30,
				 -40,-15,  0, 10, 10,  0,-15,-40,
				 -65,-30, -5,  5,  5, -5,-30,-65,
				 -95,-75,-45,-55,-55,-45,-75,-95
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
