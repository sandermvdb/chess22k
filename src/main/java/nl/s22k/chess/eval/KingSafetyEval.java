package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.ALL;
import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public class KingSafetyEval {

	public static int calculateScores(final ChessBoard cb) {

		int score = 0;

		for (int kingColor = WHITE; kingColor <= BLACK; kingColor++) {
			final int enemyColor = 1 - kingColor;

			if ((cb.pieces[enemyColor][ROOK] | cb.pieces[enemyColor][QUEEN]) == 0) {
				continue;
			}

			final int kingIndex = cb.kingIndex[kingColor];
			int counter = 0;

			final long kingArea = ChessConstants.KING_AREA[kingIndex];
			counter += EvalConstants.KS_FRIENDS[Long.bitCount(kingArea & cb.pieces[kingColor][ALL])];
			counter += EvalConstants.KS_ATTACKS[Long.bitCount(kingArea & cb.attacks[enemyColor][ALL])];
			counter += EvalConstants.KS_NIGHT_DEFENDERS[Long.bitCount(kingArea & cb.attacks[kingColor][NIGHT])];
			counter += EvalConstants.KS_WEAK[Long.bitCount(kingArea & cb.attacks[enemyColor][ALL]
					& ~(cb.attacks[kingColor][PAWN] | cb.attacks[kingColor][NIGHT] | cb.attacks[kingColor][BISHOP] | cb.attacks[kingColor][ROOK]))];
			counter += EvalConstants.KS_DOUBLE_ATTACKS[Long.bitCount(kingArea & cb.doubleAttacks[enemyColor] & ~cb.attacks[kingColor][PAWN])];

			counter += checks(cb, kingColor);

			// bonus for stm
			counter += 1 - cb.colorToMove ^ enemyColor;

			// bonus if there are discovered checks possible
			if (cb.discoveredPieces != 0) {
				counter += Long.bitCount(cb.discoveredPieces & cb.pieces[enemyColor][ALL]) * 2;
			}

			if (cb.pieces[enemyColor][QUEEN] != 0) {
				// bonus for small king-queen distance
				if ((cb.attacks[kingColor][ALL] & cb.pieces[enemyColor][QUEEN]) == 0) {
					counter += EvalConstants.KS_QUEEN_TROPISM[Util.getDistance(kingIndex, Long.numberOfTrailingZeros(cb.pieces[enemyColor][QUEEN]))];
				}
			}

			counter += EvalConstants.KS_ATTACK_PATTERN[cb.kingAttackersFlag[enemyColor]];
			score += ChessConstants.COLOR_FACTOR[enemyColor] * EvalConstants.KS_SCORES[Math.min(counter, EvalConstants.KS_SCORES.length - 1)];
		}

		return score;
	}

	private static int checks(final ChessBoard cb, final int kingColor) {
		final int kingIndex = cb.kingIndex[kingColor];
		final int enemyColor = 1 - kingColor;
		final long notDefended = ~cb.attacks[kingColor][ALL];
		final long unOccupied = ~cb.pieces[enemyColor][ALL];
		final long unsafeKingMoves = StaticMoves.KING_MOVES[kingIndex] & cb.doubleAttacks[enemyColor] & ~cb.doubleAttacks[kingColor];

		int counter = 0;
		if (cb.pieces[enemyColor][NIGHT] != 0) {
			counter += checkMinor(notDefended, StaticMoves.KNIGHT_MOVES[kingIndex] & unOccupied & cb.attacks[enemyColor][NIGHT]);
		}

		long moves;
		long queenMoves = 0;
		if ((cb.pieces[enemyColor][QUEEN] | cb.pieces[enemyColor][BISHOP]) != 0) {
			moves = MagicUtil.getBishopMoves(kingIndex, cb.allPieces ^ cb.pieces[kingColor][QUEEN]) & unOccupied;
			queenMoves = moves;
			counter += checkMinor(notDefended, moves & cb.attacks[enemyColor][BISHOP]);
		}
		if ((cb.pieces[enemyColor][QUEEN] | cb.pieces[enemyColor][ROOK]) != 0) {
			moves = MagicUtil.getRookMoves(kingIndex, cb.allPieces ^ cb.pieces[kingColor][QUEEN]) & unOccupied;
			queenMoves |= moves;
			counter += checkRook(cb, kingColor, moves & cb.attacks[enemyColor][ROOK], unsafeKingMoves | notDefended);
		}

		queenMoves &= cb.attacks[enemyColor][QUEEN];
		if (queenMoves != 0) {

			// safe check queen
			if ((queenMoves & notDefended) != 0) {
				counter += EvalConstants.KS_CHECK_QUEEN[Long.bitCount(cb.pieces[kingColor][ALL])];
			}
			// safe check queen touch
			if ((queenMoves & unsafeKingMoves) != 0) {
				counter += EvalConstants.KS_OTHER[0];
			}
		}

		return counter;
	}

	private static int checkRook(final ChessBoard cb, final int kingColor, final long rookMoves, final long safeSquares) {
		if (rookMoves == 0) {
			return 0;
		}

		if ((rookMoves & safeSquares) == 0) {
			return EvalConstants.KS_OTHER[3];
		}

		int counter = EvalConstants.KS_OTHER[2];
		if (kingBlockedAtLastRank(StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & cb.emptySpaces & ~cb.attacks[1 - kingColor][ALL])) {
			counter += EvalConstants.KS_OTHER[1];
		}
		return counter;
	}

	private static int checkMinor(final long safeSquares, final long bishopMoves) {
		if (bishopMoves == 0) {
			return 0;
		}
		if ((bishopMoves & safeSquares) == 0) {
			return EvalConstants.KS_OTHER[3];
		}
		return EvalConstants.KS_OTHER[2];
	}

	private static boolean kingBlockedAtLastRank(final long safeKingMoves) {
		return (Bitboard.RANK_234567 & safeKingMoves) == 0;
	}

}
