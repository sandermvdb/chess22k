package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Util;

public class EndGameEvaluator {

	public static int getDrawishScore(final ChessBoard cb, final int color) {
		// TODO KRKN: night close to king in the center?
		// TODO KRKB?
		return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - color]) * 10;
	}

	public static int calculateKBKNScore(final ChessBoard cb) {
		if (Long.bitCount(cb.friendlyPieces[WHITE]) > 1) {
			return 1000 + calculateKBKNScore(cb, WHITE);
		}
		return -1000 - calculateKBKNScore(cb, BLACK);
	}

	private static int calculateKBKNScore(final ChessBoard cb, final int color) {
		if ((cb.pieces[color][BISHOP] & Bitboard.WHITE_SQUARES) != 0) {
			return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - color]) * 10 * ((Bitboard.WHITE_CORNERS & cb.pieces[1 - color][KING]) != 0 ? 4 : -1);
		}
		return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - color]) * 10 * ((Bitboard.BLACK_CORNERS & cb.pieces[1 - color][KING]) != 0 ? 4 : -1);
	}

	public static boolean isKBPKDraw(final long[][] pieces) {

		if (pieces[WHITE][PAWN] != 0) {
			if ((pieces[WHITE][PAWN] & Bitboard.FILE_A) != 0 && (Bitboard.WHITE_SQUARES & pieces[WHITE][BISHOP]) == 0) {
				return (pieces[BLACK][KING] & Bitboard.A7B7A8B8) != 0;
			} else if ((pieces[WHITE][PAWN] & Bitboard.FILE_H) != 0 && (Bitboard.BLACK_SQUARES & pieces[WHITE][BISHOP]) == 0) {
				return (pieces[BLACK][KING] & Bitboard.G7H7G8H8) != 0;
			}
		} else {
			if ((pieces[BLACK][PAWN] & Bitboard.FILE_A) != 0 && (Bitboard.BLACK_SQUARES & pieces[BLACK][BISHOP]) == 0) {
				return (pieces[WHITE][KING] & Bitboard.A1B1A2B2) != 0;
			} else if ((pieces[BLACK][PAWN] & Bitboard.FILE_H) != 0 && (Bitboard.WHITE_SQUARES & pieces[BLACK][BISHOP]) == 0) {
				return (pieces[WHITE][KING] & Bitboard.G1H1G2H2) != 0;
			}
		}

		return false;
	}

	public static boolean isKQKPDraw(final ChessBoard cb) {
		final int leadingColor = cb.pieces[WHITE][QUEEN] != 0 ? WHITE : BLACK;
		final long ranks12 = leadingColor == WHITE ? Bitboard.RANK_12 : Bitboard.RANK_78;
		final long pawn = cb.pieces[1 - leadingColor][PAWN];
		long pawnZone;

		if ((Bitboard.FILE_A & pawn) != 0) {
			pawnZone = Bitboard.FILE_ABC & ranks12;
		} else if ((Bitboard.FILE_C & pawn) != 0) {
			pawnZone = Bitboard.FILE_ABC & ranks12;
		} else if ((Bitboard.FILE_F & pawn) != 0) {
			pawnZone = Bitboard.FILE_FGH & ranks12;
		} else if ((Bitboard.FILE_H & pawn) != 0) {
			pawnZone = Bitboard.FILE_FGH & ranks12;
		} else {
			return false;
		}

		if ((pawn & pawnZone) == 0) {
			return false;
		}

		if ((pawnZone & cb.pieces[1 - leadingColor][KING]) != 0) {
			if (Util.getDistance(cb.kingIndex[leadingColor], Long.numberOfTrailingZeros(pawn)) >= 4) {
				return true;
			}
		}

		return false;
	}

}
