package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.ALL;
import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.move.StaticMoves;

public class PassedPawnEval {

	public static int calculateScores(final ChessBoard cb) {

		int score = 0;

		int whitePromotionDistance = Util.SHORT_MAX;
		int blackPromotionDistance = Util.SHORT_MAX;

		// white passed pawns
		long passedPawns = cb.passedPawnsAndOutposts & cb.pieces[WHITE][ChessConstants.PAWN];
		while (passedPawns != 0) {
			final int index = 63 - Long.numberOfLeadingZeros(passedPawns);

			score += getPassedPawnScore(cb, index, WHITE);

			if (whitePromotionDistance == Util.SHORT_MAX) {
				whitePromotionDistance = getWhitePromotionDistance(cb, index);
			}

			// skip all passed pawns at same file
			passedPawns &= ~Bitboard.FILES[index & 7];
		}

		// black passed pawns
		passedPawns = cb.passedPawnsAndOutposts & cb.pieces[BLACK][ChessConstants.PAWN];
		while (passedPawns != 0) {
			final int index = Long.numberOfTrailingZeros(passedPawns);

			score -= getPassedPawnScore(cb, index, BLACK);

			if (blackPromotionDistance == Util.SHORT_MAX) {
				blackPromotionDistance = getBlackPromotionDistance(cb, index);
			}

			// skip all passed pawns at same file
			passedPawns &= ~Bitboard.FILES[index & 7];
		}

		if (whitePromotionDistance < blackPromotionDistance - 1) {
			score += 350;
		} else if (whitePromotionDistance > blackPromotionDistance + 1) {
			score -= 350;
		}

		return score;
	}

	private static int getPassedPawnScore(final ChessBoard cb, final int index, final int color) {

		final int nextIndex = index + ChessConstants.COLOR_FACTOR_8[color];
		final long square = Util.POWER_LOOKUP[index];
		final long maskNextSquare = Util.POWER_LOOKUP[nextIndex];
		final long maskPreviousSquare = Util.POWER_LOOKUP[index - ChessConstants.COLOR_FACTOR_8[color]];
		final long maskFile = Bitboard.FILES[index & 7];
		final int enemyColor = 1 - color;
		float multiplier = 1;

		// is piece blocked?
		if ((cb.allPieces & maskNextSquare) != 0) {
			multiplier *= EvalConstants.PASSED_MULTIPLIERS[0];
		}

		// is next squared attacked?
		if ((cb.attacks[enemyColor][ALL] & maskNextSquare) == 0) {

			// complete path free of enemy attacks?
			if ((ChessConstants.PINNED_MOVEMENT[nextIndex][index] & cb.attacks[enemyColor][ALL]) == 0) {
				multiplier *= EvalConstants.PASSED_MULTIPLIERS[7];
			} else {
				multiplier *= EvalConstants.PASSED_MULTIPLIERS[1];
			}
		}

		// is next squared defended?
		if ((cb.attacks[color][ALL] & maskNextSquare) != 0) {
			multiplier *= EvalConstants.PASSED_MULTIPLIERS[3];
		}

		// is enemy king in front?
		if ((ChessConstants.PINNED_MOVEMENT[nextIndex][index] & cb.pieces[enemyColor][ChessConstants.KING]) != 0) {
			multiplier *= EvalConstants.PASSED_MULTIPLIERS[2];
		}

		// under attack?
		if (cb.colorToMove != color && (cb.attacks[enemyColor][ALL] & square) != 0) {
			multiplier *= EvalConstants.PASSED_MULTIPLIERS[4];
		}

		// defended by rook from behind?
		if ((maskFile & cb.pieces[color][ROOK]) != 0 && (cb.attacks[color][ROOK] & square) != 0 && (cb.attacks[color][ROOK] & maskPreviousSquare) != 0) {
			multiplier *= EvalConstants.PASSED_MULTIPLIERS[5];
		}

		// attacked by rook from behind?
		else if ((maskFile & cb.pieces[enemyColor][ROOK]) != 0 && (cb.attacks[enemyColor][ROOK] & square) != 0
				&& (cb.attacks[enemyColor][ROOK] & maskPreviousSquare) != 0) {
			multiplier *= EvalConstants.PASSED_MULTIPLIERS[6];
		}

		// king tropism
		multiplier *= EvalConstants.PASSED_KING_MULTI[Util.getDistance(cb.kingIndex[color], index)];
		multiplier *= EvalConstants.PASSED_KING_MULTI[8 - Util.getDistance(cb.kingIndex[enemyColor], index)];

		final int scoreIndex = 7 * color + ChessConstants.COLOR_FACTOR[color] * index / 8;
		return (int) (EvalConstants.PASSED_SCORE_EG[scoreIndex] * multiplier);
	}

	private static int getBlackPromotionDistance(final ChessBoard cb, final int index) {
		// check if it cannot be stopped
		int promotionDistance = index >>> 3;
		if (promotionDistance == 1 && cb.colorToMove == BLACK) {
			if ((Util.POWER_LOOKUP[index - 8] & (cb.attacks[WHITE][ALL] | cb.allPieces)) == 0) {
				if ((Util.POWER_LOOKUP[index] & cb.attacks[WHITE][ALL]) == 0) {
					return 1;
				}
			}
		} else if (MaterialUtil.onlyWhitePawnsOrOneNightOrBishop(cb.materialKey)) {

			// check if it is my turn
			if (cb.colorToMove == WHITE) {
				promotionDistance++;
			}

			// check if own pieces are blocking the path
			if (Long.numberOfTrailingZeros(cb.pieces[BLACK][ALL] & Bitboard.FILES[index & 7]) < index) {
				promotionDistance++;
			}

			// check if own king is defending the promotion square (including square just below)
			if ((StaticMoves.KING_MOVES[cb.kingIndex[BLACK]] & ChessConstants.KING_AREA[index] & Bitboard.RANK_12) != 0) {
				promotionDistance--;
			}

			// check distance of enemy king to promotion square
			if (promotionDistance < Math.max(cb.kingIndex[WHITE] >>> 3, Math.abs((index & 7) - (cb.kingIndex[WHITE] & 7)))) {
				if (!MaterialUtil.hasWhiteNonPawnPieces(cb.materialKey)) {
					return promotionDistance;
				}
				if (cb.pieces[WHITE][NIGHT] != 0) {
					// check distance of enemy night
					if (promotionDistance < Util.getDistance(Long.numberOfTrailingZeros(cb.pieces[WHITE][NIGHT]), index)) {
						return promotionDistance;
					}
				} else {
					// can bishop stop the passed pawn?
					if (index >>> 3 == 1) {
						if (((Util.POWER_LOOKUP[index] & Bitboard.WHITE_SQUARES) == 0) == ((cb.pieces[WHITE][BISHOP] & Bitboard.WHITE_SQUARES) == 0)) {
							if ((cb.attacks[WHITE][ALL] & Util.POWER_LOOKUP[index]) == 0) {
								return promotionDistance;
							}
						}
					}
				}
			}
		}
		return Util.SHORT_MAX;
	}

	private static int getWhitePromotionDistance(final ChessBoard cb, final int index) {
		// check if it cannot be stopped
		int promotionDistance = 7 - index / 8;
		if (promotionDistance == 1 && cb.colorToMove == WHITE) {
			if ((Util.POWER_LOOKUP[index + 8] & (cb.attacks[BLACK][ALL] | cb.allPieces)) == 0) {
				if ((Util.POWER_LOOKUP[index] & cb.attacks[BLACK][ALL]) == 0) {
					return 1;
				}
			}
		} else if (MaterialUtil.onlyBlackPawnsOrOneNightOrBishop(cb.materialKey)) {

			// check if it is my turn
			if (cb.colorToMove == BLACK) {
				promotionDistance++;
			}

			// check if own pieces are blocking the path
			if (63 - Long.numberOfLeadingZeros(cb.pieces[WHITE][ALL] & Bitboard.FILES[index & 7]) > index) {
				promotionDistance++;
			}

			// TODO maybe the enemy king can capture the pawn!!
			// check if own king is defending the promotion square (including square just below)
			if ((StaticMoves.KING_MOVES[cb.kingIndex[WHITE]] & ChessConstants.KING_AREA[index] & Bitboard.RANK_78) != 0) {
				promotionDistance--;
			}

			// check distance of enemy king to promotion square
			if (promotionDistance < Math.max(7 - cb.kingIndex[BLACK] / 8, Math.abs((index & 7) - (cb.kingIndex[BLACK] & 7)))) {
				if (!MaterialUtil.hasBlackNonPawnPieces(cb.materialKey)) {
					return promotionDistance;
				}
				if (cb.pieces[BLACK][NIGHT] != 0) {
					// check distance of enemy night
					if (promotionDistance < Util.getDistance(Long.numberOfTrailingZeros(cb.pieces[BLACK][NIGHT]), index)) {
						return promotionDistance;
					}
				} else {
					// can bishop stop the passed pawn?
					if (index >>> 3 == 6) { // rank 7
						if (((Util.POWER_LOOKUP[index] & Bitboard.WHITE_SQUARES) == 0) == ((cb.pieces[BLACK][BISHOP] & Bitboard.WHITE_SQUARES) == 0)) {
							// other color than promotion square
							if ((cb.attacks[BLACK][ALL] & Util.POWER_LOOKUP[index]) == 0) {
								return promotionDistance;
							}
						}
					}
				}
			}
		}
		return Util.SHORT_MAX;
	}

}
