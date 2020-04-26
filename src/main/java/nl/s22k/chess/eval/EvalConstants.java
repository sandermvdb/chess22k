package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.texel.PsqtTuning;

/**
 * Values have been tuned using the Texel's tuning method
 */
public class EvalConstants {
	//@formatter:off
	
	public static final int SIDE_TO_MOVE_BONUS = 16; //cannot be tuned //TODO lower in endgame
	
	public static final int SCORE_DRAW 						= 0;
	public static final int SCORE_MATE_BOUND 				= 30000;
	
	// other
	public static final int[] OTHER_SCORES = {-10, 14, 18, -8, 16, 12, -158, 12, 492, 24, -44, 26};
	public static final int IX_ROOK_FILE_SEMI_OPEN	 		= 0;
	public static final int IX_ROOK_FILE_SEMI_OPEN_ISOLATED = 1;
	public static final int IX_ROOK_FILE_OPEN 				= 2;
	public static final int IX_ROOK_7TH_RANK 				= 3;
	public static final int IX_ROOK_BATTERY 				= 4;
	public static final int IX_BISHOP_LONG 					= 5;
	public static final int IX_BISHOP_PRISON 				= 6;
	public static final int IX_SPACE 						= 7;
	public static final int IX_DRAWISH 						= 8;
	public static final int IX_CASTLING						= 9;
	public static final int IX_ROOK_TRAPPED					= 10;
	public static final int IX_OUTPOST						= 11;
	
	// threats
	public static final int[] THREATS_MG = {38, 66, 90, 16, 66, 38, 12, 16, -6};
	public static final int[] THREATS_EG = {34, 20, -64, 16, 10, -48, 28, 4, 14};
	public static final int[] THREATS = new int[THREATS_MG.length];
	public static final int IX_MULTIPLE_PAWN_ATTACKS 		= 0;
	public static final int IX_PAWN_ATTACKS 				= 1;
	public static final int IX_QUEEN_ATTACKED 				= 2;
	public static final int IX_PAWN_PUSH_THREAT 			= 3;
	public static final int IX_ROOK_ATTACKED 				= 4;
	public static final int IX_QUEEN_ATTACKED_MINOR			= 5;
	public static final int IX_MAJOR_ATTACKED				= 6;
	public static final int IX_UNUSED_OUTPOST				= 7;
	public static final int IX_PAWN_ATTACKED 				= 8;
	
	// pawn
	public static final int[] PAWN_SCORES = {10, 10, 12, 6};
	public static final int IX_PAWN_DOUBLE 					= 0;
	public static final int IX_PAWN_ISOLATED 				= 1;
	public static final int IX_PAWN_BACKWARD 				= 2;
	public static final int IX_PAWN_INVERSE					= 3;
	
	// imbalance
	public static final int[] IMBALANCE_SCORES = {-10, 50, 12};
	public static final int IX_ROOK_PAIR		 			= 0;
	public static final int IX_BISHOP_DOUBLE 				= 1;
	public static final int IX_QUEEN_NIGHT 					= 2;
	
	public static final int[] PHASE 					= {0, 0, 9, 10, 20, 40};
	
	public static final int[] MATERIAL 					= {0, 100, 398, 438, 710, 1380, 3000};
	public static final int[] NIGHT_PAWN				= {42, -16, 0, 4, 10, 12, 20, 30, 36};
	public static final int[] ROOK_PAWN					= {50, -2, -4, -2, -4, 0, 0, 0, 0};
	public static final int[] BISHOP_PAWN 				= {20, 8, 6, 0, -6, -12, -18, -28, -34};
	
	public static final int[] PINNED 					= {0, 6, -14, -52, -68, -88};
	public static final int[] DISCOVERED		 		= {0, -14, 124, 98, 176, 0, 32};
	public static final int[] DOUBLE_ATTACKED 			= {0, 16, 34, 72, 4, -14, 0};
	public static final int[] SPACE 					= {0, 0, 124, 0, 0, -6, -6, -8, -7, -4, -4, -2, 0, -1, 0, 3, 7};
	
	public static final int[] PAWN_BLOCKAGE 			= {0, 0, -8, 2, 6, 32, 66, 192};
	public static final int[] PAWN_CONNECTED			= {0, 0, 14, 16, 24, 62, 138};
	public static final int[] PAWN_NEIGHBOUR	 		= {0, 0, 4, 12, 28, 92, 326};
	
	public static final int[][] SHIELD_BONUS_MG			= {	
			{0, 22, 22, -8, -12, 22, -258},
			{0, 48, 40, -6, -6, 154, -234},
			{0, 48, 0, -14, 54, 148, 16},
			{0, 8, 0, -6, -20, 138, 34}};
	public static final int[][] SHIELD_BONUS_EG			= {	
			{0, -56, -22, -8, 28, 2, -52},
			{0, -16, -22, 6, 54, 38, 52},
			{0, 0, 20, 20, 28, 80, 40},
			{0, -26, -10, 20, 48, 46, 180}};
	public static final int[][] SHIELD_BONUS 			= new int[4][7];

	public static final int[] PASSED_SCORE_EG			= {0, 14, 16, 34, 62, 128, 232};
	public static final int[] PASSED_CANDIDATE			= {0, 0, 0, 8, 14, 42};
	public static final float[] PASSED_KING_MULTI 		= {0, 1.4f, 1.4f, 1.2f, 1.1f, 1.0f, 0.8f, 0.8f};														
	public static final float[] PASSED_MULTIPLIERS		= {
			0.5f,	// blocked
			1.3f,	// next square attacked
			0.4f,	// enemy king in front
			1.2f,	// next square defended
			0.7f,	// attacked
			1.7f,	// defended by rook from behind
			0.6f,	// attacked by rook from behind
			1.8f	// no enemy attacks in front
	};	
	
	//concept borrowed from Ed Schroder
	public static final int[] KS_SCORES = {
			0, 0, 0, 40, 60, 70, 80, 90, 100, 120, 150, 200, 260, 300, 390, 450, 520, 640, 740, 760, 1260 };
	public static final int[] KS_QUEEN_TROPISM 		= {0, 0, 2, 2, 2, 2, 1, 1};	// index 0 and 1 are never evaluated	
	public static final int[] KS_CHECK_QUEEN 		= {0, 0, 0, 0, 1, 2, 3, 3, 3, 3, 3, 3, 2, 1, 0, 0, 0};
	public static final int[] KS_FRIENDS 			= {2, 2, 1, 1, 0, 0, 0, 0, 3};
	public static final int[] KS_WEAK	 			= {0, 1, 2, 2, 2, 2, 2, 1, -5};
	public static final int[] KS_ATTACKS 			= {0, 2, 2, 2, 2, 2, 3, 4, 4};
	public static final int[] KS_NIGHT_DEFENDERS	= {1, 0, 0, 0, 0, 0, 0, 0, 0};
	public static final int[] KS_DOUBLE_ATTACKS 	= {0, 1, 1, 3, 3, 9, 0, 0, 0};
	public static final int[] KS_ATTACK_PATTERN		= {	
		 //                                                 Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  Q  
		 // 	                    R  R  R  R  R  R  R  R                          R  R  R  R  R  R  R  R  
		 //             B  B  B  B              B  B  B  B              B  B  B  B              B  B  B  B  
		 //       N  N        N  N        N  N        N  N        N  N        N  N        N  N        N  N  
		 //    P     P     P     P     P     P     P     P     P     P     P     P     P     P     P     P
			4, 1, 2, 2, 2, 2, 2, 3, 1, 0, 1, 2, 1, 1, 1, 2, 2, 2, 2, 3, 2, 2, 3, 4, 1, 2, 3, 3, 2, 3, 3, 5
	};
	
	public static final int[] KS_OTHER	= {		
			2,		// queen-touch check
			3,		// king blocked at first rank check
			3,		// safe check
			1		// unsafe check
	};		
		
	public static final int[] MOBILITY_KNIGHT_MG	= {-36, -16, -6, 2, 12, 16, 24, 24, 42};
	public static final int[] MOBILITY_KNIGHT_EG	= {-98, -30, -12, 0, 4, 16, 16, 18, 8};
	public static final int[] MOBILITY_BISHOP_MG	= {-32, -16, -4, 4, 8, 14, 16, 16, 14, 16, 30, 38, -14, 54};
	public static final int[] MOBILITY_BISHOP_EG	= {-54, -28, -10, 0, 8, 12, 16, 18, 22, 20, 14, 18, 38, 18};
	public static final int[] MOBILITY_ROOK_MG 		= {-54, -44, -40, -34, -32, -24, -20, -8, 0, 10, 14, 24, 32, 40, 26};
	public static final int[] MOBILITY_ROOK_EG 		= {-62, -38, -22, -12, 2, 4, 12, 8, 14, 14, 18, 20, 20, 22, 30};
	public static final int[] MOBILITY_QUEEN_MG		= {-10, -14, -8, -14, -8, -6, -10, -8, -6, -4, -2, 2, 0, 6, 2, 6, 0, 14, 10, 16, 32, 66, 6, 150, 152, 236, 72, 344};
	public static final int[] MOBILITY_QUEEN_EG 	= {-78, -100, -102, -78, -84, -54, -24, -16, -6, 6, 16, 20, 26, 32, 40, 46, 56, 46, 62, 66, 60, 56, 72, 18, 4, -24, 64, -90};
	public static final int[] MOBILITY_KING_MG		= {-4, -2, 0, 4, 10, 18, 24, 46, 62};
	public static final int[] MOBILITY_KING_EG		= {-22, 4, 12, 8, 2, -14, -16, -30, -64};
	public static final int[] MOBILITY_KNIGHT		= new int[MOBILITY_KNIGHT_MG.length];
	public static final int[] MOBILITY_BISHOP		= new int[MOBILITY_BISHOP_MG.length];
	public static final int[] MOBILITY_ROOK			= new int[MOBILITY_ROOK_MG.length];
	public static final int[] MOBILITY_QUEEN		= new int[MOBILITY_QUEEN_MG.length];
	public static final int[] MOBILITY_KING			= new int[MOBILITY_KING_MG.length];
		
	/** piece, color, square */
	public static final int[][][] PSQT				= new int[7][2][64];
	public static final int[][][] PSQT_MG			= new int[7][2][64];
	public static final int[][][] PSQT_EG			= new int[7][2][64];
	
	static
	{	
		PSQT_MG[ChessConstants.PAWN][WHITE] = new int[] {
				   0,  0,  0,  0,  0,  0,  0,  0,
				   146,150,174,216,216,174,150,146,
				    10, 20, 62, 54, 54, 62, 20, 10,
				   -18,-10, -8, 10, 10, -8,-10,-18,
				   -32,-30,-12,  2,  2,-12,-30,-32,
				   -30,-22,-14,-16,-16,-14,-22,-30,
				   -24,  2,-18,-10,-10,-18,  2,-24,
				     0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_EG[ChessConstants.PAWN][WHITE] = new int[] {
				   0,  0,  0,  0,  0,  0,  0,  0,
				   -24,-22,-34,-46,-46,-34,-22,-24,
				    32, 20, -4,-18,-18, -4, 20, 32,
				    26, 14, 10, -4, -4, 10, 14, 26,
				    16, 12,  6, -2, -2,  6, 12, 16,
				     8,  6,  4, 14, 14,  4,  6,  8,
				    18,  6, 18, 24, 24, 18,  6, 18,
				     0,  0,  0,  0,  0,  0,  0,  0
		};
		
		PSQT_MG[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -218,-114,-132,-34,-34,-132,-114,-218,
				 -78,-60, 10,-30,-30, 10,-60,-78,
				 -12, 48, 40, 56, 56, 40, 48,-12,
				  14, 50, 46, 58, 58, 46, 50, 14,
				  12, 40, 44, 42, 42, 44, 40, 12,
				   6, 40, 40, 38, 38, 40, 40,  6,
				  -8,  6, 18, 32, 32, 18,  6, -8,
				 -30,  2, -6, 12, 12, -6,  2,-30
		};
		
		PSQT_EG[ChessConstants.NIGHT][WHITE] = new int[]{	
				 -16,  6, 36, 18, 18, 36,  6,-16,
				   4, 36, 12, 42, 42, 12, 36,  4,
				  -6, 10, 32, 30, 30, 32, 10, -6,
				  18, 18, 38, 40, 40, 38, 18, 18,
				  14, 18, 32, 40, 40, 32, 18, 14,
				   8,  4, 12, 30, 30, 12,  4,  8,
				 -12,  8, 10, 14, 14, 10,  8,-12,
				 -10, -6,  8, 16, 16,  8, -6,-10
		};
		
		PSQT_MG[ChessConstants.BISHOP][WHITE] = new int[] {
				 -26,  4,-92,-88,-88,-92,  4,-26,
				 -52,-14, -2,-38,-38, -2,-14,-52,
				  36, 48, 40, 30, 30, 40, 48, 36,
				  18, 34, 40, 54, 54, 40, 34, 18,
				  32, 40, 44, 60, 60, 44, 40, 32,
				  36, 52, 52, 42, 42, 52, 52, 36,
				  34, 62, 48, 44, 44, 48, 62, 34,
				   8, 36, 30, 50, 50, 30, 36,  8
		};
		
		PSQT_EG[ChessConstants.BISHOP][WHITE] = new int[]{	
				 -30,-10, -2,  6,  6, -2,-10,-30,
				   0,-10,  4,  6,  6,  4,-10,  0,
				 -10,-10,-10, -4, -4,-10,-10,-10,
				  -4, -6,  0,  4,  4,  0, -6, -4,
				 -20,-12, -6, -4, -4, -6,-12,-20,
				 -20,-16,-18,  0,  0,-18,-16,-20,
				 -30,-40,-22,-10,-10,-22,-40,-30,
				 -30,-18, -8,-18,-18, -8,-18,-30
		};
		
		PSQT_MG[ChessConstants.ROOK][WHITE] = new int[] {
				 -48,-14,-76, -4, -4,-76,-14,-48,
				 -20,-16, 18, 42, 42, 18,-16,-20,
				 -28,  0, -8, -4, -4, -8,  0,-28,
				 -40,-18,  6,  6,  6,  6,-18,-40,
				 -40,-10,-14,  6,  6,-14,-10,-40,
				 -38,-16, -6, -2, -2, -6,-16,-38,
				 -50,-10,-10,  8,  8,-10,-10,-50,
				 -26,-14, -4, 10, 10, -4,-14,-26
		};
		
		PSQT_EG[ChessConstants.ROOK][WHITE] = new int[]{	
				  54, 48, 68, 46, 46, 68, 48, 54,
				  40, 44, 32, 20, 20, 32, 44, 40,
				  36, 36, 34, 30, 30, 34, 36, 36,
				  42, 38, 40, 30, 30, 40, 38, 42,
				  34, 32, 32, 24, 24, 32, 32, 34,
				  22, 26, 16, 14, 14, 16, 26, 22,
				  22, 10, 14, 10, 10, 14, 10, 22,
				  14, 16, 14,  6,  6, 14, 16, 14
		};
		
		PSQT_MG[ChessConstants.QUEEN][WHITE] = new int[] {
				 -52,-44,-60,-54,-54,-60,-44,-52,
				 -38,-80,-52,-74,-74,-52,-80,-38,
				   0,-18,-40,-60,-60,-40,-18,  0,
				 -30,-40,-36,-48,-48,-36,-40,-30,
				 -24,-22,-14,-18,-18,-14,-22,-24,
				  -6,  8,-14, -4, -4,-14,  8, -6,
				  -8, 10, 26, 20, 20, 26, 10, -8,
				   6,  2,  8, 20, 20,  8,  2,  6
		};
		
		PSQT_EG[ChessConstants.QUEEN][WHITE] = new int[]{	
				  20, 18, 34, 28, 28, 34, 18, 20,
				   6, 22,  8, 48, 48,  8, 22,  6,
				 -12, -2,  8, 34, 34,  8, -2,-12,
				  32, 40, 16, 26, 26, 16, 40, 32,
				  18, 26,  0, 14, 14,  0, 26, 18,
				   8,-28,  8, -4, -4,  8,-28,  8,
				 -14,-32,-28, -6, -6,-28,-32,-14,
				 -24,-24,-18,-10,-10,-18,-24,-24
		};
		
		PSQT_MG[ChessConstants.KING][WHITE] = new int[] {
				 -16,204,-18, 16, 16,-18,204,-16,
				  38, -2,-60,-20,-20,-60, -2, 38,
				  36, 60, 56,-20,-20, 56, 60, 36,
				 -26, -8,-46,-80,-80,-46, -8,-26,
				 -64,-24,-40,-72,-72,-40,-24,-64,
				  -6,  8, -6,-18,-18, -6,  8, -6,
				  32, 26,-22,-28,-28,-22, 26, 32,
				  28, 44, 12, -2, -2, 12, 44, 28
		};
		
		PSQT_EG[ChessConstants.KING][WHITE] = new int[] {
				 -104,-82, 14,-52,-52, 14,-82,-104,
				 -28, 22, 50, 28, 28, 50, 22,-28,
				  -4, 36, 36, 34, 34, 36, 36, -4,
				  -4, 36, 44, 46, 46, 44, 36, -4,
				 -12, 10, 28, 40, 40, 28, 10,-12,
				 -18, 10, 18, 24, 24, 18, 10,-18,
				 -38,  0, 20, 24, 24, 20,  0,-38,
				 -72,-40,-24,-30,-30,-24,-40,-72
		};
		
	}
	
	public static final long[] ROOK_PRISON = { 
			0, Bitboard.A8, Bitboard.A8_B8, Bitboard.A8B8C8, 0, Bitboard.G8_H8, Bitboard.H8, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 
			0, Bitboard.A1, Bitboard.A1_B1, Bitboard.A1B1C1, 0, Bitboard.G1_H1, Bitboard.H1, 0 
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
	
	public static final int[] PROMOTION_SCORE = {
			0,
			0,
			MATERIAL[ChessConstants.NIGHT] 	- MATERIAL[ChessConstants.PAWN],
			MATERIAL[ChessConstants.BISHOP] - MATERIAL[ChessConstants.PAWN],
			MATERIAL[ChessConstants.ROOK] 	- MATERIAL[ChessConstants.PAWN],
			MATERIAL[ChessConstants.QUEEN] 	- MATERIAL[ChessConstants.PAWN],
	};
	
	
	public static void initMgEg() {
		initMgEg(MOBILITY_KNIGHT,	MOBILITY_KNIGHT_MG,	MOBILITY_KNIGHT_EG);
		initMgEg(MOBILITY_BISHOP, 	MOBILITY_BISHOP_MG, MOBILITY_BISHOP_EG);
		initMgEg(MOBILITY_ROOK,		MOBILITY_ROOK_MG,	MOBILITY_ROOK_EG);
		initMgEg(MOBILITY_QUEEN,	MOBILITY_QUEEN_MG,	MOBILITY_QUEEN_EG);
		initMgEg(MOBILITY_KING,		MOBILITY_KING_MG,	MOBILITY_KING_EG);
		initMgEg(THREATS,			THREATS_MG,			THREATS_EG);
		
		for (int i = 0; i < 4; i++) {
			initMgEg(SHIELD_BONUS[i], SHIELD_BONUS_MG[i], SHIELD_BONUS_EG[i]);
		}
		
		for (int color = WHITE; color <= BLACK; color++) {
			for (int piece = ChessConstants.PAWN; piece <= ChessConstants.KING; piece++) {
				initMgEg(PSQT[piece][color], PSQT_MG[piece][color], PSQT_EG[piece][color]);
			}
		}
	}

	private static void initMgEg(int[] array, int[] arrayMg, int[] arrayEg) {
		for(int i = 0; i < array.length; i++) {
			array[i] = EvalUtil.score(arrayMg[i], arrayEg[i]);
		}
	}
	
	public static final int[] MIRRORED_LEFT_RIGHT = new int[64];
	static {
		for (int i = 0; i < 64; i++) {
			MIRRORED_LEFT_RIGHT[i] = (i / 8) * 8 + 7 - (i & 7);
		}
	}

	public static final int[] MIRRORED_UP_DOWN = new int[64];
	static {
		for (int i = 0; i < 64; i++) {
			MIRRORED_UP_DOWN[i] = (7 - i / 8) * 8 + (i & 7);
		}
	}
	
	static {
		
		// fix white arrays
		for (int piece = ChessConstants.PAWN; piece <= ChessConstants.KING; piece++){
			Util.reverse(PSQT_MG[piece][WHITE]);
			Util.reverse(PSQT_EG[piece][WHITE]);
		}

		// create black arrays
		for (int piece = ChessConstants.PAWN; piece <= ChessConstants.KING; piece++){
			for (int i = 0; i < 64; i++) {
				PSQT_MG[piece][BLACK][i] = -PSQT_MG[piece][WHITE][MIRRORED_UP_DOWN[i]];
				PSQT_EG[piece][BLACK][i] = -PSQT_EG[piece][WHITE][MIRRORED_UP_DOWN[i]];
			}
		}
		
		Util.reverse(ROOK_PRISON);
		Util.reverse(BISHOP_PRISON);
		
		initMgEg();
	}
	
	public static void main(String[] args) {
		//increment a psqt with a constant
		for(int i=0; i<64; i++) {
			PSQT_EG[ChessConstants.KING][WHITE][i]+=20;
		}
		System.out.println(PsqtTuning.getArrayFriendlyFormatted(PSQT_EG[ChessConstants.KING][WHITE]));
	}

}
