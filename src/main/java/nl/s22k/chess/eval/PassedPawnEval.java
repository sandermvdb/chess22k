package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.move.StaticMoves;

public class PassedPawnEval {

	public static int calculatePassedPawnScores(final ChessBoard cb) {

		int score = 0;

		int whitePromotionDistance = Util.SHORT_MAX;
		int blackPromotionDistance = Util.SHORT_MAX;

		// white passed pawns
		long passedPawns = cb.passedPawns & cb.friendlyPieces[WHITE];
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
		passedPawns = cb.passedPawns & cb.friendlyPieces[BLACK];
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

		int score = EvalConstants.PASSED_PAWN_SCORE_EG[(7 * color) + ChessConstants.COLOR_FACTOR[color] * index / 8];

		// is piece blocked?
		if ((cb.allPieces & Util.POWER_LOOKUP[index + ChessConstants.COLOR_FACTOR_8[color]]) != 0) {
			score *= 10f / EvalConstants.PASSED_PAWN_MULTIPLIERS[0];
		}

		// check if next squared is attacked
		if ((cb.attacksAll[1 - color] & Util.POWER_LOOKUP[index + ChessConstants.COLOR_FACTOR_8[color]]) == 0) {
			score *= 10f / EvalConstants.PASSED_PAWN_MULTIPLIERS[2];
		}

		// check if enemy king is in front
		if (ChessConstants.COLOR_FACTOR[color] * cb.kingIndex[1 - color] > ChessConstants.COLOR_FACTOR[color] * index
				&& (cb.kingIndex[1 - color] & 7) == (index & 7)) {
			score *= 10f / EvalConstants.PASSED_PAWN_MULTIPLIERS[3];
		}

		return score;
	}

	private static int getBlackPromotionDistance(final ChessBoard cb, final int index) {
		// check if it cannot be stopped
		int promotionDistance = index / 8;
		if (promotionDistance == 1 && cb.colorToMove == BLACK) {
			if ((Util.POWER_LOOKUP[index - 8] & (cb.attacksAll[WHITE] | cb.allPieces)) == 0) {
				if ((Util.POWER_LOOKUP[index] & cb.attacksAll[WHITE]) == 0) {
					return 1;
				}
			}
		} else if (cb.majorPieces[WHITE] == 0 || cb.majorPieces[WHITE] == 1 && Long.bitCount(cb.pieces[WHITE][NIGHT] | cb.pieces[WHITE][BISHOP]) == 1) {

			// check if it is my turn
			if (cb.colorToMove == WHITE) {
				promotionDistance++;
			}

			// check if own pieces are blocking the path
			if (Long.numberOfTrailingZeros(cb.friendlyPieces[BLACK] & Bitboard.FILES[index & 7]) < index) {
				promotionDistance++;
			}

			// check if own king is defending the promotion square (including square just below)
			if ((StaticMoves.KING_MOVES[cb.kingIndex[BLACK]] & ChessConstants.PASSED_PAWN_MASKS[BLACK][index] & Bitboard.RANK_12) != 0) {
				promotionDistance--;
			}

			// check distance of enemy king to promotion square
			if (promotionDistance < Math.max(cb.kingIndex[WHITE] / 8, Math.abs((index & 7) - (cb.kingIndex[WHITE] & 7)))) {
				if (cb.majorPieces[WHITE] == 0) {
					return promotionDistance;
				}
				if (cb.pieces[WHITE][NIGHT] != 0) {
					// check distance of enemy night
					if (promotionDistance < Util.getDistance(Long.numberOfTrailingZeros(cb.pieces[WHITE][NIGHT]), index)) {
						return promotionDistance;
					}
				} else {
					// can bishop stop the passed pawn?
					if (index / 8 == 1) {
						if (((Util.POWER_LOOKUP[index] & Bitboard.WHITE_SQUARES) == 0) == ((cb.pieces[WHITE][BISHOP] & Bitboard.WHITE_SQUARES) == 0)) {
							if ((cb.attacksAll[WHITE] & Util.POWER_LOOKUP[index]) == 0) {
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
			if ((Util.POWER_LOOKUP[index + 8] & (cb.attacksAll[BLACK] | cb.allPieces)) == 0) {
				if ((Util.POWER_LOOKUP[index] & cb.attacksAll[BLACK]) == 0) {
					return 1;
				}
			}
		} else if (cb.majorPieces[BLACK] == 0 || cb.majorPieces[BLACK] == 1 && Long.bitCount(cb.pieces[BLACK][NIGHT] | cb.pieces[BLACK][BISHOP]) == 1) {

			// check if it is my turn
			if (cb.colorToMove == BLACK) {
				promotionDistance++;
			}

			// check if own pieces are blocking the path
			if (63 - Long.numberOfLeadingZeros(cb.friendlyPieces[WHITE] & Bitboard.FILES[index & 7]) > index) {
				promotionDistance++;
			}

			// TODO maybe the enemy king can capture the pawn!!
			// check if own king is defending the promotion square (including square just below)
			if ((StaticMoves.KING_MOVES[cb.kingIndex[WHITE]] & ChessConstants.PASSED_PAWN_MASKS[WHITE][index] & Bitboard.RANK_78) != 0) {
				promotionDistance--;
			}

			// check distance of enemy king to promotion square
			if (promotionDistance < Math.max(7 - cb.kingIndex[BLACK] / 8, Math.abs((index & 7) - (cb.kingIndex[BLACK] & 7)))) {
				if (cb.majorPieces[BLACK] == 0) {
					return promotionDistance;
				}
				if (cb.pieces[BLACK][NIGHT] != 0) {
					// check distance of enemy night
					if (promotionDistance < Util.getDistance(Long.numberOfTrailingZeros(cb.pieces[BLACK][NIGHT]), index)) {
						return promotionDistance;
					}
				} else {
					// can bishop stop the passed pawn?
					if (index / 8 == 6) { // rank 7
						if (((Util.POWER_LOOKUP[index] & Bitboard.WHITE_SQUARES) == 0) == ((cb.pieces[BLACK][BISHOP] & Bitboard.WHITE_SQUARES) == 0)) {
							// other color than promotion square
							if ((cb.attacksAll[BLACK] & Util.POWER_LOOKUP[index]) == 0) {
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
