package nl.s22k.chess.eval;

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
import nl.s22k.chess.Util;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public class EndGameEvaluator {

	public static int getDrawishScore(final ChessBoard cb, final int color) {
		return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - color]) * 20 - Util.getDistance(cb.kingIndex[color], cb.kingIndex[1 - color]) * 10;
	}

	public static int calculateKBNKScore(final ChessBoard cb) {
		if (Long.bitCount(cb.friendlyPieces[WHITE]) > 1) {
			return 1000 + calculateKBNKScore(cb, WHITE);
		}
		return -1000 - calculateKBNKScore(cb, BLACK);
	}

	private static int calculateKBNKScore(final ChessBoard cb, final int color) {
		if ((cb.pieces[color][BISHOP] & Bitboard.WHITE_SQUARES) != 0) {
			return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - color]) * 10 * ((Bitboard.WHITE_CORNERS & cb.pieces[1 - color][KING]) != 0 ? 4 : -1);
		}
		return Bitboard.manhattanCenterDistance(cb.kingIndex[1 - color]) * 10 * ((Bitboard.BLACK_CORNERS & cb.pieces[1 - color][KING]) != 0 ? 4 : -1);
	}

	public static int calculateKRKNScore(final ChessBoard cb) {
		if (cb.pieces[WHITE][ROOK] != 0) {
			return 50 + Bitboard.manhattanCenterDistance(cb.kingIndex[BLACK]) * 10 + Util.getDistance(cb.pieces[BLACK][KING], cb.pieces[BLACK][NIGHT]) * 10;
		}
		return -50 - Bitboard.manhattanCenterDistance(cb.kingIndex[WHITE]) * 10 - Util.getDistance(cb.pieces[WHITE][KING], cb.pieces[WHITE][NIGHT]) * 10;
	}

	public static int calculateKRKBScore(final ChessBoard cb) {
		int sameColorKingBonus = Long.bitCount(Bitboard.WHITE_SQUARES & (cb.pieces[WHITE][KING] | cb.pieces[BLACK][KING])) == 1 ? 0 : 50;
		if (cb.pieces[WHITE][ROOK] != 0) {
			return 50 + Bitboard.manhattanCenterDistance(cb.kingIndex[BLACK]) * 10 + sameColorKingBonus;
		}
		return -50 - Bitboard.manhattanCenterDistance(cb.kingIndex[WHITE]) * 10 - sameColorKingBonus;
	}

	public static boolean isKQKPDrawish(final ChessBoard cb) {
		final int leadingColor = cb.pieces[WHITE][QUEEN] != 0 ? WHITE : BLACK;
		final long pawn = cb.pieces[1 - leadingColor][PAWN];
		final long queen = cb.pieces[leadingColor][QUEEN];

		if (cb.colorToMove == leadingColor) {
			if ((MagicUtil.getQueenMoves(Long.numberOfTrailingZeros(queen), cb.allPieces) & pawn) != 0
					&& (StaticMoves.KING_MOVES[cb.kingIndex[1 - leadingColor]] & pawn) == 0) {
				// queen can capture the pawn
				return false;
			}
		} else {
			if ((StaticMoves.KING_MOVES[cb.kingIndex[1 - leadingColor]] & queen) != 0) {
				// king can capture the queen
				return false;
			}
		}

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

}
