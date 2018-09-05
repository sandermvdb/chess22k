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
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public class KingSafetyEval {

	public static int calculateKingSafetyScores(final ChessBoard cb) {

		int score = 0;

		for (int kingColor = WHITE; kingColor <= BLACK; kingColor++) {
			final int enemyColor = 1 - kingColor;

			if ((cb.pieces[enemyColor][QUEEN] | cb.pieces[enemyColor][ROOK]) == 0) {
				continue;
			}

			int counter = EvalConstants.KS_RANK[(7 * kingColor) + ChessConstants.COLOR_FACTOR[kingColor] * cb.kingIndex[kingColor] / 8];

			counter += EvalConstants.KS_NO_FRIENDS[Long.bitCount(cb.kingArea[kingColor] & ~cb.friendlyPieces[kingColor])];
			counter += openFiles(cb.pieces, kingColor, cb.pieces[kingColor][PAWN]);

			// king can move?
			if ((cb.attacks[kingColor][KING] & ~cb.friendlyPieces[kingColor]) == 0) {
				counter++;
			}
			counter += EvalConstants.KS_ATTACKS[Long.bitCount(cb.kingArea[kingColor] & cb.attacksAll[enemyColor])];
			counter += checks(cb, kingColor);

			counter += EvalConstants.KS_DOUBLE_ATTACKS[Long
					.bitCount(StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & cb.doubleAttacks[1 - kingColor] & ~cb.attacks[kingColor][PAWN])];

			if ((cb.checkingPieces & cb.friendlyPieces[enemyColor]) != 0) {
				counter++;
			}

			// bonus for stm
			counter += 1 - cb.colorToMove ^ enemyColor;

			// bonus if there are discovered checks possible
			counter += Long.bitCount(cb.discoveredPieces & cb.friendlyPieces[enemyColor]) * 2;

			// pinned at first rank
			if ((cb.pinnedPieces & Bitboard.RANK_FIRST[kingColor]) != 0) {
				counter++;
			}

			if (cb.pieces[enemyColor][QUEEN] == 0) {
				counter /= 2;
			} else if (Long.bitCount(cb.pieces[enemyColor][QUEEN]) == 1) {
				// bonus for small king-queen distance
				if ((cb.attacksAll[kingColor] & cb.pieces[enemyColor][QUEEN]) == 0) {
					counter += EvalConstants.KS_QUEEN_TROPISM[Util.getDistance(cb.kingIndex[kingColor],
							Long.numberOfTrailingZeros(cb.pieces[enemyColor][QUEEN]))];
				}
			}

			counter += EvalConstants.KS_ATTACK_PATTERN[cb.kingAttackersFlag[enemyColor]];
			score += ChessConstants.COLOR_FACTOR[enemyColor] * EvalConstants.KS_SCORES[Math.min(counter, EvalConstants.KS_SCORES.length - 1)];
		}

		return score;
	}

	private static int openFiles(final long pieces[][], final int kingColor, final long pawns) {

		if (pieces[1 - kingColor][QUEEN] == 0) {
			return 0;
		}
		if (Long.bitCount(pieces[1 - kingColor][ROOK]) < 2) {
			return 0;
		}

		if ((Bitboard.RANK_FIRST[kingColor] & pieces[kingColor][KING]) != 0) {
			if ((Bitboard.KING_SIDE & pieces[kingColor][KING]) != 0) {
				if ((Bitboard.FILE_G & pawns) == 0 || (Bitboard.FILE_H & pawns) == 0) {
					return EvalConstants.KS_OTHER[2];
				}
			} else if ((Bitboard.QUEEN_SIDE & pieces[kingColor][KING]) != 0) {
				if ((Bitboard.FILE_A & pawns) == 0 || (Bitboard.FILE_B & pawns) == 0) {
					return EvalConstants.KS_OTHER[2];
				}
			}
		}
		return 0;
	}

	private static int checks(final ChessBoard cb, final int kingColor) {
		final int enemyColor = 1 - kingColor;
		final int kingIndex = cb.kingIndex[kingColor];
		final long possibleSquares = ~cb.friendlyPieces[enemyColor]
				& (~StaticMoves.KING_MOVES[kingIndex] | StaticMoves.KING_MOVES[kingIndex] & cb.doubleAttacks[enemyColor] & ~cb.doubleAttacks[kingColor]);

		int counter = checkNight(cb, kingColor, StaticMoves.KNIGHT_MOVES[kingIndex] & possibleSquares & cb.attacks[1 - kingColor][NIGHT]);

		long moves;
		long queenMoves = 0;
		if ((cb.pieces[enemyColor][QUEEN] | cb.pieces[enemyColor][BISHOP]) != 0) {
			moves = MagicUtil.getBishopMoves(kingIndex, cb.allPieces) & possibleSquares;
			queenMoves = moves;
			counter += checkBishop(cb, kingColor, moves & cb.attacks[enemyColor][BISHOP]);
		}
		if ((cb.pieces[enemyColor][QUEEN] | cb.pieces[enemyColor][ROOK]) != 0) {
			moves = MagicUtil.getRookMoves(kingIndex, cb.allPieces) & possibleSquares;
			queenMoves |= moves;
			counter += checkRook(cb, kingColor, moves & cb.attacks[enemyColor][ROOK]);
		}

		if (Long.bitCount(cb.pieces[enemyColor][QUEEN]) == 1) {
			counter += safeCheckQueen(cb, kingColor, queenMoves & ~cb.attacksAll[kingColor] & cb.attacks[enemyColor][QUEEN]);
			counter += safeCheckQueenTouch(cb, kingColor);
		}

		return counter;
	}

	private static int safeCheckQueenTouch(final ChessBoard cb, final int kingColor) {
		if ((cb.kingAttackersFlag[1 - kingColor] & SchroderUtil.FLAG_QUEEN) == 0) {
			return 0;
		}
		final int enemyColor = 1 - kingColor;
		if ((StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & ~cb.friendlyPieces[enemyColor] & cb.attacks[enemyColor][QUEEN] & ~cb.doubleAttacks[kingColor]
				& cb.doubleAttacks[enemyColor]) != 0) {
			return EvalConstants.KS_OTHER[0];
		}
		return 0;
	}

	private static int safeCheckQueen(final ChessBoard cb, final int kingColor, final long safeQueenMoves) {

		if ((safeQueenMoves) != 0) {
			return EvalConstants.KS_CHECK_QUEEN[Long.bitCount(cb.friendlyPieces[kingColor])];
		}

		return 0;
	}

	private static int checkRook(final ChessBoard cb, final int kingColor, final long rookMoves) {

		if ((rookMoves) == 0) {
			return 0;
		}

		int counter = 0;
		if ((rookMoves & ~cb.attacksAll[kingColor]) != 0) {
			counter += EvalConstants.KS_CHECK[ROOK];

			// last rank?
			if (kingBlockedAtLastRank(kingColor, cb, StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & cb.emptySpaces & ~cb.attacksAll[1 - kingColor])) {
				counter += EvalConstants.KS_OTHER[1];
			}

		} else {
			counter += EvalConstants.KS_UCHECK[ROOK];
		}

		return counter;
	}

	private static int checkBishop(final ChessBoard cb, final int kingColor, final long bishopMoves) {

		if ((bishopMoves) != 0) {
			if ((bishopMoves & ~cb.attacksAll[kingColor]) != 0) {
				return EvalConstants.KS_CHECK[BISHOP];
			} else {
				return EvalConstants.KS_UCHECK[BISHOP];
			}
		}
		return 0;
	}

	private static int checkNight(final ChessBoard cb, final int kingColor, final long nightMoves) {
		if ((nightMoves) != 0) {
			if ((nightMoves & ~cb.attacksAll[kingColor]) != 0) {
				return EvalConstants.KS_CHECK[NIGHT];
			} else {
				return EvalConstants.KS_UCHECK[NIGHT];
			}
		}
		return 0;
	}

	private static boolean kingBlockedAtLastRank(final int kingColor, final ChessBoard cb, final long safeKingMoves) {
		return (Bitboard.RANKS[7 * kingColor] & cb.pieces[kingColor][KING]) != 0 && (safeKingMoves & Bitboard.RANKS[7 * kingColor]) == safeKingMoves;
	}

}
