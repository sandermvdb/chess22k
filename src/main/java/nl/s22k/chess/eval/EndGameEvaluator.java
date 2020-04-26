package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.ALL;
import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;

public class EndGameEvaluator {

	public static int calculateKingCorneredScore(final ChessBoard cb, final int leadingColor) {
		return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - leadingColor]) * ChessConstants.COLOR_FACTOR[leadingColor];
	}

	public static int calculateKBNKScore(final ChessBoard cb) {
		if (Long.bitCount(cb.pieces[WHITE][ALL]) > 1) {
			return 1000 + calculateKBNKScore(cb, WHITE);
		}
		return -1000 - calculateKBNKScore(cb, BLACK);
	}

	private static int calculateKBNKScore(final ChessBoard cb, final int color) {
		if ((cb.pieces[color][BISHOP] & Bitboard.WHITE_SQUARES) != 0) {
			return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - color]) * 100 * ((Bitboard.WHITE_CORNERS & cb.pieces[1 - color][KING]) != 0 ? 4 : 0);
		}
		return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - color]) * 100 * ((Bitboard.BLACK_CORNERS & cb.pieces[1 - color][KING]) != 0 ? 4 : 0);
	}

	public static int calculateKRKNScore(final ChessBoard cb) {
		if (cb.pieces[WHITE][ROOK] != 0) {
			return Bitboard.manhattanCenterDistance(cb.kingIndex[BLACK]) * 5 + Util.getDistance(cb.pieces[BLACK][KING], cb.pieces[BLACK][NIGHT]) * 10;
		}
		return -Bitboard.manhattanCenterDistance(cb.kingIndex[WHITE]) * 5 - Util.getDistance(cb.pieces[WHITE][KING], cb.pieces[WHITE][NIGHT]) * 10;
	}

	public static int calculateKRKBScore(final ChessBoard cb) {
		if (cb.pieces[WHITE][ROOK] != 0) {
			return Bitboard.manhattanCenterDistance(cb.kingIndex[BLACK]) * 2 + (cb.pinnedPieces == 0 ? 0 : 10);
		}
		return -Bitboard.manhattanCenterDistance(cb.kingIndex[WHITE]) * 2 - (cb.pinnedPieces == 0 ? 0 : 10);
	}

	public static boolean isKRKPDrawish(final ChessBoard cb) {
		final int leadingColor = cb.pieces[WHITE][ROOK] != 0 ? WHITE : BLACK;
		final long rook = cb.pieces[leadingColor][ROOK];
		final long pawn = cb.pieces[1 - leadingColor][PAWN];
		final int pawnIndex = Long.numberOfTrailingZeros(pawn);
		final long winningKing = cb.pieces[leadingColor][KING];
		final long losingKing = cb.pieces[1 - leadingColor][KING];

		if ((Bitboard.getFile(pawn) & winningKing) != 0
				&& (leadingColor == WHITE && pawnIndex > cb.kingIndex[leadingColor] || leadingColor == BLACK && pawnIndex < cb.kingIndex[leadingColor])) {
			// If the stronger side's king is in front of the pawn, it's a win
			return false;
		}
		if (Util.getDistance(losingKing, pawn) >= 3 + (cb.colorToMove == 1 - leadingColor ? 1 : 0) && Util.getDistance(losingKing, rook) >= 3) {
			// If the weaker side's king is too far from the pawn and the rook, it's a win.
			return false;
		}
		if (leadingColor == WHITE) {
			if (Bitboard.getRank(losingKing) <= Bitboard.RANK_3 && Util.getDistance(losingKing, pawn) == 1 && Bitboard.getRank(winningKing) >= Bitboard.RANK_4
					&& Util.getDistance(winningKing, pawn) > 2 + (cb.colorToMove == leadingColor ? 1 : 0)) {
				// If the pawn is far advanced and supported by the defending king, the position is drawish
				return true;
			}
		} else {
			if (Bitboard.getRank(losingKing) >= Bitboard.RANK_5 && Util.getDistance(losingKing, pawn) == 1 && Bitboard.getRank(winningKing) <= Bitboard.RANK_5
					&& Util.getDistance(winningKing, pawn) > 2 + (cb.colorToMove == leadingColor ? 1 : 0)) {
				// If the pawn is far advanced and supported by the defending king, the position is drawish
				return true;
			}
		}
		return false;
	}

	public static boolean isKQKPDrawish(final ChessBoard cb) {
		final int leadingColor = cb.pieces[WHITE][QUEEN] != 0 ? WHITE : BLACK;
		final long pawn = cb.pieces[1 - leadingColor][PAWN];

		final long ranks12 = leadingColor == WHITE ? Bitboard.RANK_12 : Bitboard.RANK_78;
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

	public static boolean isKBPKDraw(final long[][] pieces) {

		if (pieces[WHITE][BISHOP] != 0) {
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

	public static boolean isKBPKPDraw(final long[][] pieces) {
		if (pieces[WHITE][BISHOP] != 0) {
			if ((pieces[BLACK][PAWN] & Bitboard.RANK_5678) == 0) {
				return false;
			}
		} else {
			if ((pieces[WHITE][PAWN] & Bitboard.RANK_1234) == 0) {
				return false;
			}
		}

		return isKBPKDraw(pieces);
	}

}
