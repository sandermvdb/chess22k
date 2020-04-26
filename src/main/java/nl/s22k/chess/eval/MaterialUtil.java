package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.ChessBoard;

public class MaterialUtil {

	public static final int SCORE_UNKNOWN = 7777;

	//@formatter:off
	private static final int KPK	= 0x00000001;
	private static final int KPK_	= 0x00010000;
	private static final int KRKP	= 0x00010400;
	private static final int KRKP_	= 0x04000001;
	private static final int KQKP	= 0x00012000;
	private static final int KQKP_	= 0x20000001;
	private static final int KRKR	= 0x04000400;
	private static final int KQKQ	= 0x20002000;
	private static final int KRKB	= 0x00800400;
	private static final int KRKB_	= 0x04000080;
	private static final int KRNKR	= 0x04000410;
	private static final int KRNKR_	= 0x04100400;
	private static final int KRBKB	= 0x00800480;
	private static final int KRBKB_	= 0x04800080;
	private static final int KRBKR	= 0x04000480;
	private static final int KRBKR_	= 0x04800400;
	private static final int KRKN	= 0x00100400;
	private static final int KRKN_	= 0x04000010;
	private static final int KBNK 	= 0x00000090;
	private static final int KBNK_	= 0x00900000;
	private static final int KBPKP	= 0x00010081;
	private static final int KBPKP_ = 0x00810001;
	private static final int KBPK	= 0x00000081;
	private static final int KBPK_	= 0x00810000;
	//@formatter:on

	public static final int[][] VALUES = {
			// WHITE QQQRRRBBBNNNPPPP
			{ 0, 1 << 0, 1 << 4, 1 << 7, 1 << 10, 1 << 13 },
			// BLACK QQQRRRBBBNNNPPPP
			{ 0, 1 << 16, 1 << 20, 1 << 23, 1 << 26, 1 << 29 } };

	private static final int[] SHIFT = { 0, 16 };

	private static final int MASK_MINOR_MAJOR_ALL = 0xfff0fff0;
	private static final int MASK_MINOR_MAJOR_WHITE = 0xfff0;
	private static final int MASK_MINOR_MAJOR_BLACK = 0xfff00000;
	private static final int[] MASK_MINOR_MAJOR = { MASK_MINOR_MAJOR_WHITE, MASK_MINOR_MAJOR_BLACK };
	private static final int[] MASK_NON_NIGHTS = { 0xff8f, 0xff8f0000 };
	private static final int MASK_SINGLE_BISHOPS = 0x800080;
	private static final int MASK_SINGLE_BISHOP_NIGHT_WHITE = KBNK;
	private static final int MASK_SINGLE_BISHOP_NIGHT_BLACK = KBNK_;
	private static final int[] MASK_PAWNS_QUEENS = { 0xe00f, 0xe00f0000 };
	private static final int MASK_PAWNS = 0xf000f;
	private static final int[] MASK_SLIDING_PIECES = { 0xff80, 0xff800000 };

	public static void setKey(final ChessBoard cb) {
		cb.materialKey = 0;
		for (int color = WHITE; color <= BLACK; color++) {
			for (int pieceType = PAWN; pieceType <= QUEEN; pieceType++) {
				cb.materialKey += Long.bitCount(cb.pieces[color][pieceType]) * VALUES[color][pieceType];
			}
		}
	}

	public static boolean containsMajorPieces(final int material) {
		return (material & MASK_MINOR_MAJOR_ALL) != 0;
	}

	public static boolean hasNonPawnPieces(final int material, final int color) {
		return (material & MASK_MINOR_MAJOR[color]) != 0;
	}

	public static boolean hasWhiteNonPawnPieces(final int material) {
		return (material & MASK_MINOR_MAJOR_WHITE) != 0;
	}

	public static boolean hasBlackNonPawnPieces(final int material) {
		return (material & MASK_MINOR_MAJOR_BLACK) != 0;
	}

	public static boolean oppositeBishops(final int material) {
		return Long.bitCount(material & MASK_MINOR_MAJOR_ALL) == 2 && Long.bitCount(material & MASK_SINGLE_BISHOPS) == 2;
	}

	public static boolean onlyWhitePawnsOrOneNightOrBishop(final int material) {
		switch (Long.bitCount(material & MASK_MINOR_MAJOR_WHITE)) {
		case 0:
			return true;
		case 1:
			return Long.bitCount(material & MASK_SINGLE_BISHOP_NIGHT_WHITE) == 1;
		default:
			return false;
		}
	}

	public static boolean onlyBlackPawnsOrOneNightOrBishop(final int material) {
		switch (Long.bitCount(material & MASK_MINOR_MAJOR_BLACK)) {
		case 0:
			return true;
		case 1:
			return Long.bitCount(material & MASK_SINGLE_BISHOP_NIGHT_BLACK) == 1;
		default:
			return false;
		}
	}

	public static boolean hasPawns(final int material) {
		return (material & MASK_PAWNS) != 0;
	}

	public static boolean hasPawnsOrQueens(final int material, final int color) {
		return (material & MASK_PAWNS_QUEENS[color]) != 0;
	}

	public static boolean hasOnlyNights(final int material, final int color) {
		return (material & MASK_NON_NIGHTS[color]) == 0;
	}

	public static int getMajorPieces(final int material, final int color) {
		return (material & MASK_MINOR_MAJOR[color]) >>> SHIFT[color];
	}

	public static boolean hasSlidingPieces(final int material, final int color) {
		return (material & MASK_SLIDING_PIECES[color]) != 0;
	}

	public static boolean isKPK(final int material) {
		return material == KPK || material == KPK_;
	}

	public static boolean isKBPK(final int material) {
		return material == KBPK || material == KBPK_;
	}

	public static boolean isKBPKP(final int material) {
		return material == KBPKP || material == KBPKP_;
	}

	public static boolean isKBNK(final int material) {
		return material == KBNK || material == KBNK_;
	}

	public static boolean isKRKN(final int material) {
		return material == KRKN || material == KRKN_;
	}

	public static boolean isKRKB(final int material) {
		return material == KRKB || material == KRKB_;
	}

	public static boolean isKRBKB(final int material) {
		return material == KRBKB || material == KRBKB_;
	}

	public static boolean isKRBKR(final int material) {
		return material == KRBKR || material == KRBKR_;
	}

	public static boolean isKQKP(final int material) {
		return material == KQKP || material == KQKP_;
	}

	public static boolean isKRKP(final int material) {
		return material == KRKP || material == KRKP_;
	}

	public static boolean isDrawByMaterial(final ChessBoard cb) {
		switch (cb.materialKey) {
		case 0x0: // KK
		case 0x10: // KNK
		case 0x20: // KNNK
		case 0x80: // KBK
		case 0x100000: // KKN
		case 0x100010: // KNKN
		case 0x100080: // KNKB
		case 0x200000: // KKNN
		case 0x800000: // KKB
		case 0x800010: // KBKN
		case 0x800080: // KBKB
			return true;
		case KPK: // KPK
		case KPK_: // KPK
			return KPKBitbase.isDraw(cb);
		case KBPK: // KBPK
		case KBPK_: // KBPK
			return EndGameEvaluator.isKBPKDraw(cb.pieces);
		case KBPKP: // KBPKP
		case KBPKP_: // KBPKP
			return EndGameEvaluator.isKBPKPDraw(cb.pieces);
		}

		return false;

	}

	public static int calculateEndgameScore(final ChessBoard cb) {
		switch (cb.materialKey) {
		case KRKR:
		case KQKQ:
			return EvalConstants.SCORE_DRAW;
		case KBNK:
		case KBNK_:
			return EndGameEvaluator.calculateKBNKScore(cb);
		case KRKN:
		case KRKN_:
			return EndGameEvaluator.calculateKRKNScore(cb);
		case KQKP:
		case KQKP_:
			if (EndGameEvaluator.isKQKPDrawish(cb)) {
				return cb.pieces[WHITE][QUEEN] == 0 ? -50 : 50;
			}
			return SCORE_UNKNOWN;
		case KRKP:
		case KRKP_:
			if (EndGameEvaluator.isKRKPDrawish(cb)) {
				return cb.pieces[WHITE][ROOK] == 0 ? -50 : 50;
			}
			return SCORE_UNKNOWN;

		case KRKB:
		case KRKB_:
			return EndGameEvaluator.calculateKRKBScore(cb);
		case KRNKR:
		case KRBKR:
			return EndGameEvaluator.calculateKingCorneredScore(cb, WHITE);
		case KRNKR_:
		case KRBKR_:
			return EndGameEvaluator.calculateKingCorneredScore(cb, BLACK);

		}

		return SCORE_UNKNOWN;
	}

}
