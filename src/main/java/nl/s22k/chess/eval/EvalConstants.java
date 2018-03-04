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
	public static final int INDEX_BISHOP_PRISON 				= 9;
	public static final int INDEX_ROOK_7TH_RANK 				= 10;
	public static final int INDEX_ROOK_PASSED_PAWN_FILE 		= 11;
	public static final int INDEX_ROOK_BATTERY 					= 12;
	public static final int INDEX_SPACE 						= 13;
	
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

	public static final int[] INDIVIDUAL_SCORES = { 
			-4,	// ROOK FILE SEMI OPEN
			18,	// ROOK FILE SEMI OPEN ISOLATED
			22,	// ROOK FILE OPEN
			54,	// BISHOP DOUBLE 
			8,	// PAWN DOUBLE
			10,	// PAWN ISOLATED
			28,	// ROOK PAIR
			24,	// QUEEN NIGHT
			10,	// PAWN BACKWARD
			140,// BISHOP PRISON - low error improvement...
			16,	// ROOK 7TH RANK
			12,	// ROOK ON SAME FILE AS PASSED PAWN
			18,	// ROOK BATTERY
			12	// SPACE
	};
	
	public static final int[] THREAT_SCORES = { 
			44,	// MULTIPLE PAWN ATTACKS
			46,	// PAWN ATTACKS
			56,	// QUEEN ATTACKED
			14,	// PAWN PUSH THREAT
			82, // NIGHT FORK
			316,// NIGHT FORK KING
			32,	// ROOK ATTACKED
			36,	// QUEEN ATTACKED MINOR
			16	// MAJOR ATTACKED
	};
	
	public static final int[] PHASE = { 0, 0, 10, 10, 20, 40 };
	
	public static final int[] MATERIAL_SCORES 				= {0, 100, 395, 415, 690, 1280, 3000};
	public static final int[] PINNED_PIECE_SCORES 			= {0, -4, 30, 54, 76, 96};
	public static final int[] DISCOVERED_PIECE_SCORES 		= {0, -20, 112, 96, 184, 0, 24};
	public static final int[] KNIGHT_OUTPOST				= {0, 0, 8, 24, 24, 32, -4, 24};
	public static final int[] BISHOP_OUTPOST				= {0, 0, 24, 20, 20, 16, 40, 24};
	public static final int[] NIGHT_PAWN_BONUS				= {40, -16, -4, -2, 4, 6, 14, 28, 44};
	public static final int[] HANGING_PIECES 				= {0, 8, 4, -8, -12, -16, 40}; //qsearch could set the other in check
	public static final int[] HANGING_PIECES_2 				= {0, 24, 68, 52, 48, -232};
	public static final int[] ROOK_TRAPPED 					= {64, 60, 32};
	public static final int[] IMBALANCE 					= {0, 0, 0, 100, 65, -65, 10, -25, 5, -10, 5, -5}; // TODO negative values?!
	
	public static final int[] PASSED_PAWN_SCORE_EG 			= {0, 20, 20, 45, 75, 145, 295};
	public static final int[] PASSED_PAWN_CANDIDATE			= {0, 0, 0, 10, 15, 30, 0};
	public static final int[][] PAWN_SHIELD_BONUS 			= {	{0, -4, 0, -8, 16, 110, 228},
																{0, 32, 16, -8, 44, 250, 256},
																{0, 28, -4, -8, 64, 210, 356} };
	public static final int[] PAWN_BLOCKAGE 				= {0, 0, -12, 0, 4, 24, 48, 168};
	public static final int[] PAWN_CONNECTED 				= {0, 0, 12, 12, 16, 48, 84};
	public static final int[] PAWN_NEIGHBOUR 				= {0, 0, 4, 12, 24, 92, 356};
	
	public static final int[] PASSED_PAWN_MULTIPLIERS	= {
			18,	// blocked
			47,	// endgame vs midgame
			7,	// next square attacked
			40	// enemy king in front
	};	
	
	//concept borrowed from Ed Schroder
	public static final int[] KING_SAFETY_SCORES = { //TODO negative values? //TODO first values are not used
			50, 40, 30, 0, 30, 120, -130, -120, -100, -70, -30, 30, 90, 90, 
			110, 120, 140, 160, 190, 210, 260, 300, 360, 410, 450, 560, 
			610, 610, 730, 680, 980, 1000, 1100, 1200, 1300, 1400, 1500};
	public static final int[] KING_SAFETY_QUEEN_TROPISM = {0, 0, 1, 1, 1, 1, 0, 0};	// index 0 and 1 are never evaluated	
	public static final int[] KING_SAFETY_COUNTER_RANKS = {5, 1, 2, 2, 2, 2, 6, 8}; //TODO rebase to 0?
	public static final int[] KING_SAFETY_CHECK			= {0, 0, 3, 2, 3};
	public static final int[] KING_SAFETY_CHECK_QUEEN 	= {0, 0, 0, 0, 1, 3, 4, 4, 4, 4, 4, 3, 3, 2, 1, 1, 0};
	public static final int[] KING_SAFETY_NO_FRIENDS 	= {6, 4, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11};
	public static final int[] KING_SAFETY_ATTACKS 		= {0, 2, 2, 3, 3, 4, 4, 5, 6, 8, 7, 9, 10};
	public static final int[] KING_SAFETY_ATTACK_PATTERN_COUNTER = {	
		 //                                                 Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  
		 // 	                    R  R  R  R  R  R  R  R                          R  R  R  R  R  R  R  R  
		 //             B  B  B  B              B  B  B  B              B  B  B  B              B  B  B  B  
		 //       N  N        N  N        N  N        N  N        N  N        N  N        N  N        N  N  
		 //    P     P     P     P     P     P     P     P     P     P     P     P     P     P     P     P
			5, 2, 3, 3, 3, 2, 3, 3, 2, 1, 2, 2, 2, 2, 3, 3, 2, 2, 3, 3, 2, 3, 4, 5, 2, 3, 5, 6, 4, 4, 5, 5
	};
	
	public static final int[] KING_SAFETY_COUNTERS	= {		
			4,		// queen-touch check
			5,		// king at blocked first rank check
			38		// screwed
	};		
		
	public static final int[] MOBILITY_KNIGHT		= {-38, -18, -10, -2, 8, 12, 20, 22, 42};
	public static final int[] MOBILITY_KNIGHT_EG	= {-102, -34, -10, 2, 8, 16, 16, 18, 6};
	public static final int[] MOBILITY_BISHOP 		= {-20, -2, 14, 22, 28, 34, 40, 40, 36, 42, 60, 92, 18, 120};
	public static final int[] MOBILITY_BISHOP_EG	= {-48, -22, -6, 10, 20, 26, 32, 36, 44, 42, 36, 36, 70, 36};
	public static final int[] MOBILITY_ROOK 		= {-28, -22, -18, -14, -14, -4, -2, 10, 16, 30, 38, 44, 54, 60, 76};
	public static final int[] MOBILITY_ROOK_EG 		= {-40, -14, -2, 6, 18, 20, 26, 26, 32, 30, 34, 40, 38, 40, 36};
	public static final int[] MOBILITY_QUEEN 		= {-40, -38, -30, -28, -22, -14, -12, -12, -8, -4, -4, 4, -4, 4, 2, 0, -6, 18, 16, 14, 36, 68, 38, 164, 170, 224, 126, 276};
	public static final int[] MOBILITY_QUEEN_EG 	= {64, -70, -114, -104, -94, -82, -68, -36, -20, -16, 0, 0, 16, 24, 30, 44, 58, 38, 56, 66, 56, 48, 50, 12, -10, -24, 46, -52};
	public static final int[] MOBILITY_KING			= {14, 0, 0, 4, 8, 22, 20, 42, 86};
	public static final int[] MOBILITY_KING_EG		= {-34, -4, 4, 8, 4, -14, -12, -26, -66};
		
	/** piece, color, square */
	public static final int[][][] PSQT_SCORES			= new int[7][2][64];
	public static final int[][][] PSQT_EG_SCORES		= new int[7][2][64];
	
	static
	{	
		PSQT_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				   0,  0,  0,  0,  0,  0,  0,  0,
				   130, 90, 75,125,125, 75, 90,130,
				     5, 15, 50, 40, 40, 50, 15,  5,
				   -20, -5,-10, 15, 15,-10, -5,-20,
				   -30,-25,-10,  5,  5,-10,-25,-30,
				   -30,-20,-15,-15,-15,-15,-20,-30,
				   -25,  5, -5, -5, -5, -5,  5,-25,
				     0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.PAWN][WHITE] = new int[] {
				   0,  0,  0,  0,  0,  0,  0,  0,
				   -60,-55,-70,-90,-90,-70,-55,-60,
				    40, 25,  0,-20,-20,  0, 25, 40,
				    30, 15, 10, -5, -5, 10, 15, 30,
				    15, 10,  0,-10,-10,  0, 10, 15,
				     5,  0,  5,  5,  5,  5,  0,  5,
				    10,  0, 10, 15, 15, 10,  0, 10,
				     0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -205,-110,-135,-40,-40,-135,-110,-205,
				 -75,-50, 25,-20,-20, 25,-50,-75,
				 -25, 55, 30, 35, 35, 30, 55,-25,
				  25, 35, 45, 45, 45, 45, 35, 25,
				   5, 45, 40, 40, 40, 40, 45,  5,
				   5, 35, 35, 40, 40, 35, 35,  5,
				 -10,-10, 15, 30, 30, 15,-10,-10,
				 -40, -5,-10, 15, 15,-10, -5,-40
		};
		
		PSQT_EG_SCORES[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -20, -5, 25, 10, 10, 25, -5,-20,
				   0, 20,  0, 30, 30,  0, 20,  0,
				 -10, -5, 25, 25, 25, 25, -5,-10,
				  10, 20, 35, 40, 40, 35, 20, 10,
				  10, 10, 30, 40, 40, 30, 10, 10,
				   5,  5, 15, 30, 30, 15,  5,  5,
				 -15,  5,  5, 10, 10,  5,  5,-15,
				 -15, -5,  0, 10, 10,  0, -5,-15
		};
		
		PSQT_SCORES[ChessConstants.BISHOP][WHITE] = new int[] {
				   0, 10,-105,-75,-75,-105, 10,  0,
				   -55, 15,-10,-15,-15,-10, 15,-55,
				    20, 40, 45, 20, 20, 45, 40, 20,
				    15, 20, 30, 45, 45, 30, 20, 15,
				    25, 35, 30, 50, 50, 30, 35, 25,
				    30, 45, 50, 30, 30, 50, 45, 30,
				    30, 55, 40, 35, 35, 40, 55, 30,
				     5, 25, 20, 35, 35, 20, 25,  5
		};
		
		PSQT_EG_SCORES[ChessConstants.BISHOP][WHITE] = new int[]{	
				 -20,-15,  5,  5,  5,  5,-15,-20,
				   5,-15,  0, -5, -5,  0,-15,  5,
				  -5,-10, -5, -5, -5, -5,-10, -5,
				   0, -5,  0,  0,  0,  0, -5,  0,
				 -15,-15,  0, -5, -5,  0,-15,-15,
				 -15,-15,-10,  5,  5,-10,-15,-15,
				 -30,-25,-20,-10,-10,-20,-25,-30,
				 -20,-15,-10,-15,-15,-10,-15,-20
		};
		
		PSQT_SCORES[ChessConstants.ROOK][WHITE] = new int[] {
				 -35,-20,-75,  0,  0,-75,-20,-35,
				 -30,-20,  5, 30, 30,  5,-20,-30,
				 -30, 10, 10, -5, -5, 10, 10,-30,
				 -40,-15, 10, 10, 10, 10,-15,-40,
				 -45, -5,-10,  5,  5,-10, -5,-45,
				 -40,-15, -5, -5, -5, -5,-15,-40,
				 -55, -5,-10, 10, 10,-10, -5,-55,
				 -10,-15,  0, 15, 15,  0,-15,-10
		};
		
		PSQT_EG_SCORES[ChessConstants.ROOK][WHITE] = new int[]{	
				  40, 40, 55, 35, 35, 55, 40, 40,
				  30, 30, 25, 10, 10, 25, 30, 30,
				  30, 30, 25, 30, 30, 25, 30, 30,
				  35, 30, 35, 25, 25, 35, 30, 35,
				  30, 25, 25, 15, 15, 25, 25, 30,
				  15, 20, 10, 10, 10, 10, 20, 15,
				  20,  5, 10,  5,  5, 10,  5, 20,
				   5, 15, 10,  0,  0, 10, 15,  5
		};
		
		PSQT_SCORES[ChessConstants.QUEEN][WHITE] = new int[] {
				 -45,-50,-50,-55,-55,-50,-50,-45,
				 -40,-85,-50,-70,-70,-50,-85,-40,
				  -5,-30,-40,-65,-65,-40,-30, -5,
				 -35,-55,-45,-65,-65,-45,-55,-35,
				 -30,-25,-15,-25,-25,-15,-25,-30,
				  -5, 10,-15,-15,-15,-15, 10, -5,
				 -10,  5, 25, 15, 15, 25,  5,-10,
				   0,  0,  5, 20, 20,  5,  0,  0
		};
		
		PSQT_EG_SCORES[ChessConstants.QUEEN][WHITE] = new int[]{	
				  10, 15, 25, 20, 20, 25, 15, 10,
				  -20,  0,-10, 20, 20,-10,  0,-20,
				  -10,  0,  5, 40, 40,  5,  0,-10,
				   35, 40, 10, 30, 30, 10, 40, 35,
				   25, 20, -5, 10, 10, -5, 20, 25,
				    0,-40, -5,-10,-10, -5,-40,  0,
				  -20,-50,-50,-20,-20,-50,-50,-20,
				  -45,-40,-40,-40,-40,-40,-40,-45
		};
		
		PSQT_SCORES[ChessConstants.KING][WHITE] = new int[] {
				 -60,215,-25, 30, 30,-25,215,-60,
				  20, -5,-55,-25,-25,-55, -5, 20,
				  25, 55, 25,-10,-10, 25, 55, 25,
				 -65,-40,-40,-50,-50,-40,-40,-65,
				 -70,-70,-55,-80,-80,-55,-70,-70,
				 -20,-20,-40,-30,-30,-40,-20,-20,
				   0,-20,-70,-80,-80,-70,-20,  0,
				  15, 30,-10, 15, 15,-10, 30, 15
		};
		
		PSQT_EG_SCORES[ChessConstants.KING][WHITE] = new int[] {
				 -110,-115,-20,-70,-70,-20,-115,-110,
				 -45, 10, 35, 25, 25, 35, 10,-45,
				 -30, 25, 30, 20, 20, 30, 25,-30,
				 -20, 25, 35, 35, 35, 35, 25,-20,
				 -40,  0, 15, 30, 30, 15,  0,-40,
				 -55,-20,  0, 10, 10,  0,-20,-55,
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
