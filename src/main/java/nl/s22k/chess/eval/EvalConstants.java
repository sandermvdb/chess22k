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
	public static final int SCORE_MATE_BOUND 				= 30000;

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
	public static final int INDEX_ROOK_BATTERY 					= 11;
	public static final int INDEX_SPACE 						= 12;
	public static final int INDEX_LONG_BISHOP 					= 13;
	
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
			-6,	// ROOK FILE SEMI OPEN
			14,	// ROOK FILE SEMI OPEN ISOLATED
			20,	// ROOK FILE OPEN
			54,	// BISHOP DOUBLE 
			4,	// PAWN DOUBLE
			10,	// PAWN ISOLATED
			30,	// ROOK PAIR
			18,	// QUEEN NIGHT
			8,	// PAWN BACKWARD
			148,// BISHOP PRISON - low error improvement...
			8,	// ROOK 7TH RANK
			22,	// ROOK BATTERY
			12,	// SPACE
			10	// LONG BISHOP
	};
	
	public static final int[] THREAT_SCORES = { 
			40,	// MULTIPLE PAWN ATTACKS
			48,	// PAWN ATTACKS
			56,	// QUEEN ATTACKED
			18,	// PAWN PUSH THREAT
			44, // NIGHT FORK
			150,// NIGHT FORK KING
			34,	// ROOK ATTACKED
			40,	// QUEEN ATTACKED MINOR
			14,	// MAJOR ATTACKED
			10	// UNUSED OUTPOSTS
	};
	
	public static final int[] PHASE 					= { 0, 0, 6, 6, 13, 28 };
	
	public static final int[] MATERIAL 					= {0, 100, 395, 415, 700, 1305, 3000};
	public static final int[] PINNED 					= {0, -4, 18, 46, 76, 92};
	public static final int[] PINNED_ATTACKED			= {0, 36, 142, 286, 340, 228};
	public static final int[] DISCOVERED		 		= {0, -12, 116, 100, 180, 0, 24};
	public static final int[] KNIGHT_OUTPOST			= {0, 0, 12, 24, 24, 36, 4, 20};
	public static final int[] BISHOP_OUTPOST			= {0, 0, 24, 20, 20, 20, 48, 48};
	public static final int[] NIGHT_PAWN				= {52, -8, 0, 2, 4, 6, 14, 28, 44};
	public static final int[] HANGING 					= {0, 12, 8, 4, -8, -12, 48}; //qsearch could set the other in check
	public static final int[] HANGING_2 				= {0, 28, 72, 64, 44, -208};
	public static final int[] ROOK_TRAPPED 				= {64, 60, 28};
	public static final int[] IMBALANCE 				= {0, 0, 0, 100, 55, -70, 0, -25, 5, -10, 5, -5}; // TODO negative values?!
	public static final int[] NO_MINOR_DEFENSE 			= {0, 5, 10, 25, 0, 15, 0};
	public static final int[] SAME_COLORED_BISHOP_PAWN 	= {-20, -8, -4, 0, 8, 12, 20, 28, 32};
	
	public static final int[] PAWN_BLOCKAGE 			= {0, 0, -12, 0, 8, 32, 68, 200};
	public static final int[] PAWN_CONNECTED 			= {0, 0, 12, 12, 16, 56, 124};
	public static final int[] PAWN_NEIGHBOUR 			= {0, 0, 4, 12, 24, 76, 312};
	
	public static final int[][] SHIELD_BONUS 			= {	{0, 8, 12, 0, -12, -78, -360}, //TODO low values for rank 0?
															{0, 48, 32, 4, 32, 150, -216},
															{0, 48, 8, 4, 96, 226, 36},
															{0, 4, 0, 4, 36, 140, -8}};

	public static final int[] PASSED_SCORE 				= {0, 15, 20, 40, 70, 145, 270};
	public static final int[] PASSED_CANDIDATE			= {0, 0, 0, 10, 15, 30};
	public static final float[] PASSED_KING 			= {0, 1.4f, 1.3f, 1.1f, 1.1f, 1.0f, 0.8f, 0.7f};
	public static final float[] PASSED_MULTIPLIERS	= {
			0.5f,	// blocked
			1.2f,	// next square attacked
			0.3f,	// enemy king in front
			1.2f,	// next square defended
			0.7f,	// attacked
			1.6f,	// defended by rook from behind
			0.6f,	// attacked by rook from behind
			1.7f	// no enemy attacks in front
	};	
	
	//concept borrowed from Ed Schroder
	public static final int[] KS_SCORES = { //TODO negative values? //TODO first values are not used
			0, 0, 0, 0, 0, -90, -100, -80, -50, 0, 80, 100, 
			110, 120, 140, 160, 190, 220, 250, 320, 350, 410, 470, 560, 
			610, 610, 730, 680, 980, 1000, 1100, 1200, 1300, 1400, 1500};
	public static final int[] KS_QUEEN_TROPISM 		= {0, 0, 1, 1, 1, 1, 0, 0};	// index 0 and 1 are never evaluated	
	public static final int[] KS_RANK 				= {0, 0, 1, 1, 0, 0, 0, 0};
	public static final int[] KS_CHECK				= {0, 0, 3, 2, 3};
	public static final int[] KS_UCHECK				= {0, 0, 1, 1, 1};
	public static final int[] KS_CHECK_QUEEN 		= {0, 0, 0, 0, 1, 3, 4, 4, 4, 4, 3, 3, 3, 2, 1, 0, 0};
	public static final int[] KS_NO_FRIENDS 		= {6, 4, 0, 5, 5, 5, 5, 6, 7, 8, 9, 10};
	public static final int[] KS_ATTACKS 			= {0, 3, 3, 3, 3, 3, 4, 4, 5, 5, 7, 1, 9};
	public static final int[] KS_ATTACK_PATTERN		= {	
		 //                                                 Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  
		 // 	                    R  R  R  R  R  R  R  R                          R  R  R  R  R  R  R  R  
		 //             B  B  B  B              B  B  B  B              B  B  B  B              B  B  B  B  
		 //       N  N        N  N        N  N        N  N        N  N        N  N        N  N        N  N  
		 //    P     P     P     P     P     P     P     P     P     P     P     P     P     P     P     P
			5, 2, 3, 3, 3, 2, 3, 3, 2, 1, 2, 2, 2, 2, 2, 3, 3, 2, 3, 3, 3, 3, 4, 4, 2, 3, 5, 4, 3, 5, 5, 5
	};
	
	public static final int[] KS_OTHER	= {		
			5,		// queen-touch check
			5,		// king at blocked first rank check
			1		// open file
	};		
		
	public static final int[] MOBILITY_KNIGHT		= {-38, -18, -6, -2, 8, 16, 24, 26, 50};
	public static final int[] MOBILITY_KNIGHT_EG	= {-94, -34, -10, 2, 8, 12, 12, 14, 2};
	public static final int[] MOBILITY_BISHOP 		= {-16, 2, 14, 22, 28, 34, 36, 36, 36, 38, 60, 84, 30, 104};
	public static final int[] MOBILITY_BISHOP_EG	= {-40, -10, 6, 18, 28, 30, 36, 40, 44, 42, 32, 32, 58, 32};
	public static final int[] MOBILITY_ROOK 		= {-28, -22, -18, -14, -14, -4, -2, 6, 16, 26, 30, 40, 54, 64, 68};
	public static final int[] MOBILITY_ROOK_EG 		= {-36, -14, 2, 10, 22, 24, 30, 30, 32, 34, 38, 40, 38, 40, 44};
	public static final int[] MOBILITY_QUEEN 		= {-20, -18, -14, -16, -10, -6, -8, -8, -8, -4, -4, -4, -4, 0, -2, 0, -2, 14, 12, 14, 36, 60, 42, 156, 166, 232, 90, 320};
	public static final int[] MOBILITY_QUEEN_EG 	= {0, -82, -110, -84, -74, -62, -48, -24, -12, -8, 8, 20, 28, 32, 38, 52, 62, 42, 64, 74, 60, 64, 50, 12, -6, -36, 42, -88};
	public static final int[] MOBILITY_KING			= {2, -8, -8, 0, 8, 26, 36, 66, 106};
	public static final int[] MOBILITY_KING_EG		= {-38, -8, 8, 8, 4, -14, -12, -26, -62};
		
	/** piece, color, square */
	public static final int[][][] PSQT				= new int[7][2][64];
	public static final int[][][] PSQT_EG			= new int[7][2][64];
	
	static
	{	
		PSQT[ChessConstants.PAWN][WHITE] = new int[] {
				   0,  0,  0,  0,  0,  0,  0,  0,
				   180,165,165,210,210,165,165,180,
				    20, 30, 70, 65, 65, 70, 30, 20,
				   -15,  0,  0, 15, 15,  0,  0,-15,
				   -30,-25,-10,  5,  5,-10,-25,-30,
				   -30,-20,-15,-15,-15,-15,-20,-30,
				   -25,  5,-15, -5, -5,-15,  5,-25,
				     0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_EG[ChessConstants.PAWN][WHITE] = new int[] {
				   0,  0,  0,  0,  0,  0,  0,  0,
				   -55,-50,-60,-70,-70,-60,-50,-55,
				    30, 15,-15,-30,-30,-15, 15, 30,
				    30, 15,  5, -5, -5,  5, 15, 30,
				    15, 10,  0, -5, -5,  0, 10, 15,
				     5,  0,  5, 10, 10,  5,  0,  5,
				    15,  5, 15, 15, 15, 15,  5, 15,
				     0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -220,-115,-140,-35,-35,-140,-115,-220,
				 -75,-60, 15,-40,-40, 15,-60,-75,
				 -20, 45, 25, 35, 35, 25, 45,-20,
				  20, 35, 35, 45, 45, 35, 35, 20,
				   5, 35, 35, 35, 35, 35, 35,  5,
				  10, 35, 35, 40, 40, 35, 35, 10,
				  -5,  0, 20, 35, 35, 20,  0, -5,
				 -30,  0,-10, 20, 20,-10,  0,-30
		};
		
		PSQT_EG[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -15,  5, 35, 15, 15, 35,  5,-15,
				   0, 25,  5, 40, 40,  5, 25,  0,
				 -10,  5, 30, 30, 30, 30,  5,-10,
				  15, 25, 40, 45, 45, 40, 25, 15,
				  15, 20, 35, 40, 40, 35, 20, 15,
				   5, 10, 15, 30, 30, 15, 10,  5,
				  -5,  5, 10, 15, 15, 10,  5, -5,
				 -10,  0, 10, 15, 15, 10,  0,-10
		};
		
		PSQT[ChessConstants.BISHOP][WHITE] = new int[] {
				 -20, 10,-90,-85,-85,-90, 10,-20,
				 -45, 10,  5,-15,-15,  5, 10,-45,
				  35, 50, 45, 30, 30, 45, 50, 35,
				  30, 30, 40, 55, 55, 40, 30, 30,
				  25, 35, 35, 55, 55, 35, 35, 25,
				  35, 50, 50, 40, 40, 50, 50, 35,
				  35, 60, 45, 40, 40, 45, 60, 35,
				   5, 25, 30, 45, 45, 30, 25,  5
		};
		
		PSQT_EG[ChessConstants.BISHOP][WHITE] = new int[]{	
				 -30,-15, -5,  0,  0, -5,-15,-30,
				  -5,-25,-10, -5, -5,-10,-25, -5,
				 -15,-15,-15,-10,-10,-15,-15,-15,
				  -5, -5,  0,  0,  0,  0, -5, -5,
				 -20,-15, -5, -5, -5, -5,-15,-20,
				 -20,-15,-15,  0,  0,-15,-15,-20,
				 -35,-40,-20,-10,-10,-20,-40,-35,
				 -30,-15,-15,-20,-20,-15,-15,-30
		};
		
		PSQT[ChessConstants.ROOK][WHITE] = new int[] {
				 -50,-20,-75, -5, -5,-75,-20,-50,
				 -35,-20, 15, 25, 25, 15,-20,-35,
				 -25, 10,  0, -5, -5,  0, 10,-25,
				 -45,-20, 15,  5,  5, 15,-20,-45,
				 -45,-15,-20,  0,  0,-20,-15,-45,
				 -40,-15,  0, -5, -5,  0,-15,-40,
				 -50, -5,-10,  5,  5,-10, -5,-50,
				 -10,-15,  0, 15, 15,  0,-15,-10
		};
		
		PSQT_EG[ChessConstants.ROOK][WHITE] = new int[]{	
				  50, 45, 65, 45, 45, 65, 45, 50,
				  40, 40, 30, 20, 20, 30, 40, 40,
				  35, 35, 35, 35, 35, 35, 35, 35,
				  45, 40, 40, 35, 35, 40, 40, 45,
				  35, 35, 35, 25, 25, 35, 35, 35,
				  20, 25, 15, 15, 15, 15, 25, 20,
				  15,  5, 10, 10, 10, 10,  5, 15,
				   5, 15, 10,  0,  0, 10, 15,  5
		};
		
		PSQT[ChessConstants.QUEEN][WHITE] = new int[] {
				 -65,-45,-65,-55,-55,-65,-45,-65,
				 -35,-90,-65,-85,-85,-65,-90,-35,
				 -10,-35,-45,-70,-70,-45,-35,-10,
				 -45,-55,-55,-70,-70,-55,-55,-45,
				 -25,-35,-25,-35,-35,-25,-35,-25,
				 -10, 10,-15,-10,-10,-15, 10,-10,
				  -5, 15, 25, 20, 20, 25, 15, -5,
				  15,  5, 10, 25, 25, 10,  5, 15
		};
		
		PSQT_EG[ChessConstants.QUEEN][WHITE] = new int[]{	
				  30, 15, 40, 30, 30, 40, 15, 30,
				   5, 30, 15, 55, 55, 15, 30,  5,
				  -5, 10, 15, 45, 45, 15, 10, -5,
				  45, 45, 25, 40, 40, 25, 45, 45,
				  10, 20,  5, 25, 25,  5, 20, 10,
				   5,-35,  0,-10,-10,  0,-35,  5,
				 -30,-55,-40,-25,-25,-40,-55,-30,
				 -50,-40,-35,-35,-35,-35,-40,-50
		};
		
		PSQT[ChessConstants.KING][WHITE] = new int[] {
				 -60,175,-40,-10,-10,-40,175,-60,
				  40,-10,-90,-35,-35,-90,-10, 40,
				  20, 50, 15,-50,-50, 15, 50, 20,
				 -55,-45,-70,-100,-100,-70,-45,-55,
				 -60,-35,-40,-90,-90,-40,-35,-60,
				 -15,-10,-30,-35,-35,-30,-10,-15,
				  10,-10,-60,-75,-75,-60,-10, 10,
				  10, 25,-10,  0,  0,-10, 25, 10
		};
		
		PSQT_EG[ChessConstants.KING][WHITE] = new int[] {
				 -105,-100,-10,-65,-65,-10,-100,-105,
				 -50, -5, 30, 10, 10, 30, -5,-50,
				 -20, 20, 25, 20, 20, 25, 20,-20,
				 -15, 20, 30, 30, 30, 30, 20,-15,
				 -30, -5, 10, 25, 25, 10, -5,-30,
				 -40,-15,  0, 10, 10,  0,-15,-40,
				 -65,-30, -5,  5,  5, -5,-30,-65,
				 -95,-75,-50,-55,-55,-50,-75,-95
		};
		
	}

	static {

		// fix white arrays
		for(int piece=ChessConstants.PAWN; piece<=ChessConstants.KING; piece++){
			Util.reverse(PSQT[piece][ChessConstants.WHITE]);
			Util.reverse(PSQT_EG[piece][ChessConstants.WHITE]);
		}

		// create black arrays
		for(int piece=ChessConstants.PAWN; piece<=ChessConstants.KING; piece++){
			for (int i = 0; i < 64; i++) {
				PSQT[piece][BLACK][i] = -PSQT[piece][WHITE][63 - i];
				PSQT_EG[piece][BLACK][i] = -PSQT_EG[piece][WHITE][63 - i];
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
			MATERIAL[ChessConstants.NIGHT] 	- MATERIAL[ChessConstants.PAWN],
			MATERIAL[ChessConstants.BISHOP] 	- MATERIAL[ChessConstants.PAWN],
			MATERIAL[ChessConstants.ROOK] 	- MATERIAL[ChessConstants.PAWN],
			MATERIAL[ChessConstants.QUEEN] 	- MATERIAL[ChessConstants.PAWN],
	};

}
