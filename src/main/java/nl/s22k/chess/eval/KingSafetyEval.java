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
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public class KingSafetyEval {

	public static int calculateKingSafetyScores(final ChessBoard cb) {

		// TODO count number of attacks on a certain square
		// TODO battery

		if (!EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
			return 0;
		}

		int score = 0;

		for (int kingColor = WHITE; kingColor <= BLACK; kingColor++) {
			final int enemyColor = 1 - kingColor;

			int counter = EvalConstants.KING_SAFETY_COUNTER_RANKS[(7 * kingColor) + ChessConstants.COLOR_FACTOR[kingColor] * cb.kingIndex[kingColor] / 8];

			counter += EvalConstants.KING_SAFETY_NO_FRIENDS[Long.bitCount(cb.kingArea[kingColor] & ~cb.friendlyPieces[kingColor])];
			if ((cb.kingArea[kingColor] & cb.attacksAll[enemyColor]) != 0) {

				// king can move?
				if ((cb.attacks[kingColor][KING] & ~cb.friendlyPieces[kingColor]) == 0) {
					counter++;
				}
				counter += EvalConstants.KING_SAFETY_ATTACKS[Long.bitCount(cb.kingArea[kingColor] & cb.attacksAll[enemyColor])];
				counter += doubleAttacks(cb, kingColor);
				counter += safeChecks(cb, kingColor);
			}

			// bonus for stm
			counter += 1 - cb.colorToMove ^ enemyColor;

			// bonus if there are discovered checks possible
			counter += Long.bitCount(cb.discoveredPieces & cb.friendlyPieces[enemyColor]) * 2;

			if (cb.pieces[enemyColor][QUEEN] == 0) {
				counter /= 2;
			} else if (Long.bitCount(cb.pieces[enemyColor][QUEEN]) == 1){
				// bonus for small king-queen distance
				if ((cb.attacksAll[kingColor] & Util.POWER_LOOKUP[Long.numberOfTrailingZeros(cb.pieces[enemyColor][QUEEN])]) == 0) {
					counter += EvalConstants.KING_SAFETY_QUEEN_TROPISM[Util.getDistance(cb.kingIndex[kingColor],
							Long.numberOfTrailingZeros(cb.pieces[enemyColor][QUEEN]))];
				}
			}

			counter += EvalConstants.KING_SAFETY_ATTACK_PATTERN_COUNTER[cb.kingAttackersFlag[enemyColor]];
			score += ChessConstants.COLOR_FACTOR[enemyColor] * EvalConstants.KING_SAFETY_SCORES[Math.min(counter, EvalConstants.KING_SAFETY_SCORES.length - 1)];
		}

		return score;
	}

	private static int safeChecks(final ChessBoard cb, final int kingColor) {
		int counter = safeCheckNight(cb, kingColor);
		final int enemyColor = 1 - kingColor;

		long moves;
		long queenMoves = 0;
		if ((cb.pieces[enemyColor][QUEEN] | cb.pieces[enemyColor][BISHOP]) != 0) {
			moves = MagicUtil.getBishopMoves(cb.kingIndex[kingColor], cb.allPieces, cb.friendlyPieces[enemyColor])
					& ~StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & ~cb.attacksAll[kingColor];
			queenMoves = moves;
			counter += safeCheckBishop(cb, kingColor, moves);
		}
		if ((cb.pieces[enemyColor][QUEEN] | cb.pieces[enemyColor][ROOK]) != 0) {
			moves = MagicUtil.getRookMoves(cb.kingIndex[kingColor], cb.allPieces, cb.friendlyPieces[enemyColor])
					& ~StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & ~cb.attacksAll[kingColor];
			queenMoves |= moves;
			counter += safeCheckRook(cb, kingColor, moves);
		}

		if (Long.bitCount(cb.pieces[enemyColor][QUEEN]) == 1) {
			counter += safeCheckQueen(cb, kingColor, queenMoves);
			counter += safeCheckQueenTouch(cb, kingColor);
		}

		return counter;
	}

	private static int safeCheckQueenTouch(final ChessBoard cb, final int kingColor) {
		final int enemyColor = 1 - kingColor;
		long moves = StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & ~cb.friendlyPieces[enemyColor] & cb.attacks[enemyColor][QUEEN]
				& ~cb.attacksWithoutKing[kingColor];
		while (moves != 0) {
			if (((cb.attacks[enemyColor][PAWN] | cb.attacks[enemyColor][NIGHT] | cb.attacks[enemyColor][BISHOP] | cb.attacks[enemyColor][ROOK])
					& Long.lowestOneBit(moves)) != 0) {
				return EvalConstants.KING_SAFETY_COUNTERS[0];
			}
			moves &= moves - 1;
		}
		return 0;
	}

	private static int safeCheckQueen(ChessBoard cb, int kingColor, long queenMoves) {

		int counter = 0;

		if ((queenMoves & cb.attacks[1 - kingColor][QUEEN]) != 0) {
			counter += EvalConstants.KING_SAFETY_CHECK_QUEEN[Long.bitCount(cb.friendlyPieces[kingColor])];

			// last rank?
			if (kingBlockedAtLastRank(cb.kingIndex[kingColor], kingColor, cb)) {
				counter += EvalConstants.KING_SAFETY_COUNTERS[1];
			}

			// skewed pieces
			if (cb.colorToMove != kingColor) {
				long screwed = (cb.pieces[kingColor][ROOK] | cb.pieces[kingColor][QUEEN]) & ~cb.attacksAll[kingColor]
						& ~StaticMoves.KING_MOVES[cb.kingIndex[kingColor]];
				if (screwed != 0) {
					if ((cb.allPieces & ChessConstants.ROOK_IN_BETWEEN[Long.numberOfTrailingZeros(queenMoves & cb.attacks[1 - kingColor][QUEEN])][Long
							.numberOfTrailingZeros(screwed)]) == cb.pieces[kingColor][KING]) {
						counter += EvalConstants.KING_SAFETY_COUNTERS[2];
					} else if ((cb.allPieces & ChessConstants.BISHOP_IN_BETWEEN[Long.numberOfTrailingZeros(queenMoves & cb.attacks[1 - kingColor][QUEEN])][Long
							.numberOfTrailingZeros(screwed)]) == cb.pieces[kingColor][KING]) {
						counter += EvalConstants.KING_SAFETY_COUNTERS[2];
					}
				}
			}
		}

		return counter;
	}

	private static int safeCheckRook(final ChessBoard cb, final int kingColor, long rookMoves) {

		int counter = 0;

		if ((rookMoves & cb.attacks[1 - kingColor][ROOK]) != 0) {
			counter += EvalConstants.KING_SAFETY_CHECK_ROOK[cb.majorPieces[kingColor]];

			// last rank?
			if (kingBlockedAtLastRank(cb.kingIndex[kingColor], kingColor, cb)) {
				counter += EvalConstants.KING_SAFETY_COUNTERS[1];
			}

			// skewed pieces
			if (cb.colorToMove != kingColor) {
				long screwed = (cb.pieces[kingColor][ROOK] | cb.pieces[kingColor][QUEEN]) & ~cb.attacksAll[kingColor]
						& ~StaticMoves.KING_MOVES[cb.kingIndex[kingColor]];
				if (screwed != 0) {
					if ((cb.allPieces & ChessConstants.ROOK_IN_BETWEEN[Long.numberOfTrailingZeros(rookMoves & cb.attacks[1 - kingColor][ROOK])][Long
							.numberOfTrailingZeros(screwed)]) == cb.pieces[kingColor][KING]) {
						counter += EvalConstants.KING_SAFETY_COUNTERS[2];
					}
				}
			}
		}

		return counter;
	}

	private static int safeCheckBishop(final ChessBoard cb, final int kingColor, final long bishopMoves) {

		int counter = 0;

		if ((bishopMoves & cb.attacks[1 - kingColor][BISHOP]) != 0) {
			counter += EvalConstants.KING_SAFETY_CHECK_BISHOP[cb.majorPieces[kingColor]];

			// skewed pieces
			if (cb.colorToMove != kingColor) {
				long screwed = (cb.pieces[kingColor][ROOK] | cb.pieces[kingColor][QUEEN]) & ~cb.attacksAll[kingColor]
						& ~StaticMoves.KING_MOVES[cb.kingIndex[kingColor]];
				if (screwed != 0) {
					if ((cb.allPieces & ChessConstants.BISHOP_IN_BETWEEN[Long.numberOfTrailingZeros(bishopMoves & cb.attacks[1 - kingColor][BISHOP])][Long
							.numberOfTrailingZeros(screwed)]) == cb.pieces[kingColor][KING]) {
						counter += EvalConstants.KING_SAFETY_COUNTERS[2];
					}
				}
			}

		}
		return counter;
	}

	private static int safeCheckNight(ChessBoard cb, final int kingColor) {
		// safe check possible
		if (cb.pieces[1 - kingColor][NIGHT] != 0) {
			if ((StaticMoves.KNIGHT_MOVES[cb.kingIndex[kingColor]] & ~cb.attacksAll[kingColor] & ~cb.friendlyPieces[1 - kingColor]
					& cb.attacks[1 - kingColor][NIGHT]) != 0) {
				return EvalConstants.KING_SAFETY_CHECK_NIGHT[cb.majorPieces[kingColor]];
			}
		}
		return 0;
	}

	private static int doubleAttacks(final ChessBoard cb, final int kingColor) {
		int counter = 0;

		// double pawn?
		if (Long.bitCount(cb.attacks[1 - kingColor][PAWN] & cb.kingArea[kingColor]) > 2) {
			counter += EvalConstants.KING_SAFETY_DOUBLE_PAWN[cb.majorPieces[kingColor]];
		}

		// double night?
		if (Long.bitCount(cb.pieces[1 - kingColor][NIGHT] & cb.kingAreaAttackers) == 2) {
			counter += EvalConstants.KING_SAFETY_DOUBLE_NIGHT[cb.majorPieces[kingColor]];
		}

		// double bishop?
		if (Long.bitCount(cb.pieces[1 - kingColor][BISHOP] & cb.kingAreaAttackers) == 2) {
			counter += EvalConstants.KING_SAFETY_DOUBLE_BISHOP[cb.majorPieces[kingColor]];
		}

		// double rook?
		if (Long.bitCount(cb.pieces[1 - kingColor][ROOK] & cb.kingAreaAttackers) == 2) {
			counter += EvalConstants.KING_SAFETY_DOUBLE_ROOK[cb.majorPieces[kingColor]];
		}

		return counter;
	}

	private static boolean kingBlockedAtLastRank(final int kingIndex, final int kingColor, final ChessBoard cb) {
		return cb.colorToMove != kingColor && (Bitboard.RANKS[7 * kingColor] & cb.pieces[kingColor][KING]) != 0
				&& (StaticMoves.KING_MOVES[kingIndex] & cb.emptySpaces & Bitboard.RANKS[7 * kingColor]) == (StaticMoves.KING_MOVES[kingIndex] & cb.emptySpaces);
	}

}
