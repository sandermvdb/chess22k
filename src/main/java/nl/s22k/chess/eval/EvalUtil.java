package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;
import static nl.s22k.chess.eval.EvalConstants.INDEX_PAWN_DOUBLE;
import static nl.s22k.chess.eval.EvalConstants.INDEX_PAWN_ISOLATED;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardTestUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public class EvalUtil {

	// TODO tropism?
	// TODO fianchetto
	// TODO 50-move rule
	// TODO bad-bishop?

	public static final int MG = 0;
	public static final int EG = 1;

	private static final int PHASE_TOTAL = 4 * EvalConstants.PHASE[NIGHT] + 4 * EvalConstants.PHASE[BISHOP] + 4 * EvalConstants.PHASE[ROOK]
			+ 2 * EvalConstants.PHASE[QUEEN];

	public static int calculateScore(ChessBoard cb) {

		if (Statistics.ENABLED) {
			Statistics.evalNodes++;
		}

		if (EngineConstants.ENABLE_EVAL_CACHE && !EngineConstants.TEST_PAWN_EVAL_CACHE) {
			if (EvalCache.hasScore(cb.zobristKey)) {
				return EvalCache.getScore(cb.zobristKey);
			}
		}

		int score = taperedEval(cb);

		// TODO check if material score has one side leading

		/* draw-by-material */
		if (score > 0) {
			if (cb.isDrawByMaterial(WHITE)) {
				if (Statistics.ENABLED) {
					Statistics.drawByMaterialCount++;
				}
				score = EvalConstants.SCORE_DRAW;
			} else if (cb.isDrawishByMaterial(WHITE)) {
				if (Statistics.ENABLED) {
					Statistics.drawishByMaterialCount++;
				}
				score = (cb.pieces[BLACK][KING] & Bitboard.CORNER_SQUARES) == 0 ? Math.min(EvalConstants.SCORE_DRAWISH, score)
						: Math.min(EvalConstants.SCORE_DRAWISH_KING_CORNERED, score);
			}
		} else {
			if (cb.isDrawByMaterial(BLACK)) {
				if (Statistics.ENABLED) {
					Statistics.drawByMaterialCount++;
				}
				score = EvalConstants.SCORE_DRAW;
			} else if (cb.isDrawishByMaterial(BLACK)) {
				if (Statistics.ENABLED) {
					Statistics.drawishByMaterialCount++;
				}
				score = (cb.pieces[WHITE][KING] & Bitboard.CORNER_SQUARES) == 0 ? Math.max(-EvalConstants.SCORE_DRAWISH, score)
						: Math.max(-EvalConstants.SCORE_DRAWISH_KING_CORNERED, score);
			}
		}

		EvalCache.addValue(cb.zobristKey, score);

		if (EngineConstants.TEST_EVAL_VALUES) {
			ChessBoard testCb = ChessBoardTestUtil.getHorizontalMirroredCb(cb);
			compareScores(cb, testCb, 1);

			testCb = ChessBoardTestUtil.getVerticalMirroredCb(cb);
			compareScores(cb, testCb, -1);
		}

		return score;
	}

	private static int taperedEval(ChessBoard cb) {
		calculateMobilityScoresAndSetAttackBoards(cb);

		final int phaseIndependentScore = calculateMaterialExcludingPawnScores(cb) + calculateKingSafetyScores(cb) + calculatePenalties(cb)
				+ calculatePawnIncludingMaterialScores(cb) + calculateBonusses(cb);
		final int passedPawnEgScore = calculatePassedPawnScores(cb);

		int scoreMg = EngineConstants.ENABLE_INCREMENTAL_PSQT ? cb.psqtScore : calculatePositionScores(cb);
		scoreMg += cb.mobilityScore[MG] + calculatePawnShieldBonus(cb) + passedPawnEgScore * 10 / EvalConstants.PASSED_PAWN_MULTIPLIERS[1]
				+ phaseIndependentScore;

		int scoreEg = EngineConstants.ENABLE_INCREMENTAL_PSQT ? cb.psqtScoreEg : calculatePositionEgScores(cb);
		scoreEg += cb.mobilityScore[EG] + passedPawnEgScore + phaseIndependentScore;

		final int phase = PHASE_TOTAL - (Long.bitCount(cb.pieces[WHITE][NIGHT] | cb.pieces[BLACK][NIGHT]) * EvalConstants.PHASE[NIGHT]
				+ Long.bitCount(cb.pieces[WHITE][BISHOP] | cb.pieces[BLACK][BISHOP]) * EvalConstants.PHASE[BISHOP]
				+ Long.bitCount(cb.pieces[WHITE][ROOK] | cb.pieces[BLACK][ROOK]) * EvalConstants.PHASE[ROOK]
				+ Long.bitCount(cb.pieces[WHITE][QUEEN] | cb.pieces[BLACK][QUEEN]) * EvalConstants.PHASE[QUEEN]);
		return ((scoreMg * (PHASE_TOTAL - phase)) + (scoreEg * phase)) / PHASE_TOTAL;
	}

	private static void compareScores(ChessBoard cb1, ChessBoard cb2, int factor) {

		calculateMobilityScoresAndSetAttackBoards(cb1);
		calculateMobilityScoresAndSetAttackBoards(cb2);

		if (calculateKingSafetyScores(cb2) != calculateKingSafetyScores(cb1) * factor) {
			System.out.println("Unequal king-safety: " + calculateKingSafetyScores(cb1) + " " + calculateKingSafetyScores(cb2) * factor);
		}
		if (calculateMaterialExcludingPawnScores(cb1) != calculateMaterialExcludingPawnScores(cb2) * factor) {
			System.out.println("Unequal material: " + calculateMaterialExcludingPawnScores(cb1) + " " + calculateMaterialExcludingPawnScores(cb2) * factor);
		}
		if (calculatePenalties(cb1) != calculatePenalties(cb2) * factor) {
			System.out.println("Unequal penalties: " + calculatePenalties(cb1) + " " + calculatePenalties(cb2) * factor);
		}
		if (calculatePositionScores(cb1) != calculatePositionScores(cb2) * factor) {
			System.out.println("Unequal position score: " + calculatePositionScores(cb1) + " " + calculatePositionScores(cb2) * factor);
		}
		if (calculatePawnIncludingMaterialScores(cb1) != calculatePawnIncludingMaterialScores(cb2) * factor) {
			System.out.println("Unequal pawns: " + calculatePawnIncludingMaterialScores(cb1) + " " + calculatePawnIncludingMaterialScores(cb2) * factor);
		}
		if (calculateBonusses(cb2) != calculateBonusses(cb1) * factor) {
			System.out.println("Unequal bonus: " + calculateBonusses(cb1) + " " + calculateBonusses(cb2) * factor);
		}
		if (calculatePassedPawnScores(cb1) != calculatePassedPawnScores(cb2) * factor) {
			System.out.println("Unequal passed-pawns: " + calculatePassedPawnScores(cb1) + " " + calculatePassedPawnScores(cb2) * factor);
		}
	}

	public static int calculatePassedPawnScores(final ChessBoard cb) {

		int score = 0;

		int promotionDistance;
		int index;

		int whitePromotionDistance = Util.SHORT_MAX;
		int blackPromotionDistance = Util.SHORT_MAX;

		// white passed pawns
		long passedPawns = cb.passedPawns & cb.pieces[WHITE][PAWN];
		while (passedPawns != 0) {
			index = 63 - Long.numberOfLeadingZeros(passedPawns);

			// is piece blocked?
			if ((cb.allPieces & Util.POWER_LOOKUP[index + 8]) != 0) {
				score += EvalConstants.PASSED_PAWN_SCORE_EG[index / 8] * 10 / EvalConstants.PASSED_PAWN_MULTIPLIERS[0];
			} else {
				score += EvalConstants.PASSED_PAWN_SCORE_EG[index / 8];
			}

			if (whitePromotionDistance != Util.SHORT_MAX) {
				passedPawns &= ~Bitboard.FILES[index & 7];
				continue;
			}

			// check if it cannot be stopped
			if (cb.hasOnlyPawns(BLACK)
					|| Long.bitCount(cb.pieces[BLACK][NIGHT]) == 1 && Long.bitCount(cb.pieces[BLACK][PAWN]) + 2 == Long.bitCount(cb.friendlyPieces[BLACK])
					|| Long.bitCount(cb.pieces[BLACK][BISHOP]) == 1 && Long.bitCount(cb.pieces[BLACK][PAWN]) + 2 == Long.bitCount(cb.friendlyPieces[BLACK])) {
				promotionDistance = 7 - index / 8;

				// check if it is my turn
				if (cb.colorToMove == BLACK) {
					promotionDistance++;
				}

				// check if own pieces are blocking the path
				if (63 - Long.numberOfLeadingZeros(cb.friendlyPieces[WHITE] & Bitboard.FILES[index & 7]) > index) {
					promotionDistance++;
				}

				// check if pawn can do 2-moves
				if (index / 8 == 1) {
					promotionDistance--;
				}

				// TODO maybe the enemy king can capture the pawn!!
				// check if own king is defending the promotion square (including square just below)
				if ((StaticMoves.KING_MOVES[cb.kingIndex[WHITE]] & ChessConstants.PASSED_PAWN_MASKS[WHITE][index] & Bitboard.RANK_78) != 0) {
					promotionDistance--;
				}

				// check distance of enemy king to promotion square
				if (promotionDistance < Math.max(7 - cb.kingIndex[BLACK] / 8, Math.abs((index & 7) - (cb.kingIndex[BLACK] & 7)))) {
					if (cb.hasOnlyPawns(BLACK)) {
						whitePromotionDistance = promotionDistance;
					} else if (cb.pieces[BLACK][NIGHT] != 0) {
						// check distance of enemy night
						if (promotionDistance < Util.getDistance(Long.numberOfTrailingZeros(cb.pieces[BLACK][NIGHT]), index)) {
							whitePromotionDistance = promotionDistance;
						}
					} else {
						// can bishop stop the passed pawn?
						if (index / 8 == 6) { // rank 7
							if (((Util.POWER_LOOKUP[index] & Bitboard.WHITE_SQUARES) == 0) == ((cb.pieces[BLACK][BISHOP] & Bitboard.WHITE_SQUARES) == 0)) {
								// other color than promotion square
								if ((cb.allPieces & Util.POWER_LOOKUP[index + 8]) == 0) {
									whitePromotionDistance = promotionDistance;
								}
							}
						}
					}
				}
			}

			// skip all passed pawns at same file
			passedPawns &= ~Bitboard.FILES[index & 7];
		}

		passedPawns = cb.passedPawns & cb.pieces[BLACK][PAWN];
		while (passedPawns != 0) {
			index = Long.numberOfTrailingZeros(passedPawns);

			// is piece blocked?
			if ((cb.allPieces & Util.POWER_LOOKUP[index - 8]) != 0) {
				score -= EvalConstants.PASSED_PAWN_SCORE_EG[7 - index / 8] * 10 / EvalConstants.PASSED_PAWN_MULTIPLIERS[0];
			} else {
				score -= EvalConstants.PASSED_PAWN_SCORE_EG[7 - index / 8];
			}

			if (blackPromotionDistance != Util.SHORT_MAX) {
				passedPawns &= ~Bitboard.FILES[index & 7];
				continue;
			}

			// check if it cannot be stopped
			if (cb.hasOnlyPawns(WHITE)
					|| Long.bitCount(cb.pieces[WHITE][NIGHT]) == 1 && Long.bitCount(cb.pieces[WHITE][PAWN]) + 2 == Long.bitCount(cb.friendlyPieces[WHITE])
					|| Long.bitCount(cb.pieces[WHITE][BISHOP]) == 1 && Long.bitCount(cb.pieces[WHITE][PAWN]) + 2 == Long.bitCount(cb.friendlyPieces[WHITE])) {
				promotionDistance = index / 8;

				// check if it is my turn
				if (cb.colorToMove == WHITE) {
					promotionDistance++;
				}

				// check if own pieces are blocking the path
				if (Long.numberOfTrailingZeros(cb.friendlyPieces[BLACK] & Bitboard.FILES[index & 7]) < index) {
					promotionDistance++;
				}

				// check if pawn can do 2-moves
				if (index / 8 == 6) {
					promotionDistance--;
				}

				// check if own king is defending the promotion square (including square just below)
				if ((StaticMoves.KING_MOVES[cb.kingIndex[BLACK]] & ChessConstants.PASSED_PAWN_MASKS[BLACK][index] & Bitboard.RANK_12) != 0) {
					promotionDistance--;
				}

				// check distance of enemy king to promotion square
				if (promotionDistance < Math.max(cb.kingIndex[WHITE] / 8, Math.abs((index & 7) - (cb.kingIndex[WHITE] & 7)))) {
					if (cb.hasOnlyPawns(WHITE)) {
						blackPromotionDistance = promotionDistance;
					} else if (cb.pieces[WHITE][NIGHT] != 0) {
						// check distance of enemy night
						if (promotionDistance < Util.getDistance(Long.numberOfTrailingZeros(cb.pieces[WHITE][NIGHT]), index)) {
							blackPromotionDistance = promotionDistance;
						}
					} else {
						// can bishop stop the passed pawn?
						if (index / 8 == 1) {
							if (((Util.POWER_LOOKUP[index] & Bitboard.WHITE_SQUARES) == 0) == ((cb.pieces[WHITE][BISHOP] & Bitboard.WHITE_SQUARES) == 0)) {
								if ((cb.allPieces & Util.POWER_LOOKUP[index - 8]) == 0) {
									blackPromotionDistance = promotionDistance;
								}
							}
						}
					}
				}
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

	public static int calculatePawnIncludingMaterialScores(final ChessBoard cb) {

		if (EngineConstants.ENABLE_PAWN_EVAL_CACHE && !EngineConstants.TEST_PAWN_EVAL_CACHE && !EngineConstants.TEST_EVAL_VALUES) {
			if (PawnEvalCache.hasScore(cb.pawnZobristKey)) {
				cb.passedPawns = PawnEvalCache.getPassedPawns(cb.pawnZobristKey);
				return PawnEvalCache.getScore(cb.pawnZobristKey);
			}
		}

		int score = 0;

		// penalty for doubled pawns
		for (int i = 0; i < 8; i++) {
			if (Long.bitCount(cb.pieces[WHITE][PAWN] & Bitboard.FILES[i]) > 1) {
				score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_PAWN_DOUBLE];
			}
			if (Long.bitCount(cb.pieces[BLACK][PAWN] & Bitboard.FILES[i]) > 1) {
				score += EvalConstants.INDIVIDUAL_SCORES[INDEX_PAWN_DOUBLE];
			}
		}

		// bonus for connected pawns
		score += Long.bitCount(cb.attacks[WHITE][PAWN] & cb.pieces[WHITE][PAWN]) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_CONNECTED];
		score -= Long.bitCount(cb.attacks[BLACK][PAWN] & cb.pieces[BLACK][PAWN]) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_CONNECTED];

		cb.passedPawns = 0;

		int index;

		// white
		long pawns = cb.pieces[WHITE][PAWN];
		while (pawns != 0) {
			index = Long.numberOfTrailingZeros(pawns);

			// isolated pawns
			if ((Bitboard.FILES_ADJACENT[index & 7] & cb.pieces[WHITE][PAWN]) == 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_PAWN_ISOLATED];
			}

			// backward pawns
			else if ((ChessConstants.MASK_ADJACENT_FILE_DOWN[index + 8] & cb.pieces[WHITE][PAWN]) == 0) {
				if ((StaticMoves.PAWN_ALL_ATTACKS[WHITE][index + 8] & cb.pieces[BLACK][PAWN]) != 0) {
					if ((StaticMoves.PAWN_ALL_ATTACKS[WHITE][index] & cb.pieces[BLACK][PAWN]) == 0) {
						score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_BACKWARD];
					}
				}
			}

			// set passed pawns
			if ((ChessConstants.PASSED_PAWN_MASKS[WHITE][index] & cb.pieces[BLACK][PAWN]) == 0) {
				cb.passedPawns |= Long.lowestOneBit(pawns);
			}

			// candidate passed pawns (no pawns in front, more friendly pawns behind and adjacent than enemy pawns)
			else if (63 - Long.numberOfLeadingZeros((cb.pieces[WHITE][PAWN] | cb.pieces[BLACK][PAWN]) & Bitboard.FILES[index & 7]) == index) {
				if (Long.bitCount(cb.pieces[WHITE][PAWN] & ChessConstants.MASK_ADJACENT_FILE_DOWN[index + 8]) >= Long
						.bitCount(cb.pieces[BLACK][PAWN] & ChessConstants.MASK_ADJACENT_FILE_UP[index - 8])) {
					score += EvalConstants.PASSED_PAWN_CANDIDATE[index / 8];
				}
			}

			pawns &= pawns - 1;
		}

		// black
		pawns = cb.pieces[BLACK][PAWN];
		while (pawns != 0) {
			index = Long.numberOfTrailingZeros(pawns);

			// isolated pawns
			if ((Bitboard.FILES_ADJACENT[index & 7] & cb.pieces[BLACK][PAWN]) == 0) {
				score += EvalConstants.INDIVIDUAL_SCORES[INDEX_PAWN_ISOLATED];
			}

			// backward pawns
			else if ((ChessConstants.MASK_ADJACENT_FILE_UP[index - 8] & cb.pieces[BLACK][PAWN]) == 0) {
				if ((StaticMoves.PAWN_ALL_ATTACKS[BLACK][index - 8] & cb.pieces[WHITE][PAWN]) != 0) {
					if ((StaticMoves.PAWN_ALL_ATTACKS[BLACK][index] & cb.pieces[WHITE][PAWN]) == 0) {
						score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_BACKWARD];
					}
				}
			}

			// set passed pawns
			if ((ChessConstants.PASSED_PAWN_MASKS[BLACK][index] & cb.pieces[WHITE][PAWN]) == 0) {
				cb.passedPawns |= Long.lowestOneBit(pawns);
			}

			// candidate passers
			else if (Long.numberOfTrailingZeros((cb.pieces[WHITE][PAWN] | cb.pieces[BLACK][PAWN]) & Bitboard.FILES[index & 7]) == index) {
				if (Long.bitCount(cb.pieces[BLACK][PAWN] & ChessConstants.MASK_ADJACENT_FILE_UP[index - 8]) >= Long
						.bitCount(cb.pieces[WHITE][PAWN] & ChessConstants.MASK_ADJACENT_FILE_DOWN[index + 8])) {
					score -= EvalConstants.PASSED_PAWN_CANDIDATE[7 - index / 8];
				}
			}

			pawns &= pawns - 1;
		}

		// pawn material
		score += (Long.bitCount(cb.pieces[WHITE][PAWN]) - Long.bitCount(cb.pieces[BLACK][PAWN])) * EvalConstants.MATERIAL_SCORES[PAWN];

		if (EngineConstants.TEST_PAWN_EVAL_CACHE) {
			if (PawnEvalCache.hasScore(cb.pawnZobristKey)) {
				int cachedScore = PawnEvalCache.getScore(cb.pawnZobristKey);
				if (cachedScore != score) {
					System.out.println(String.format("Cached pawn score != score: %s, %s", cachedScore, score));
				}
			}
		}

		PawnEvalCache.addValue(cb.pawnZobristKey, score, cb.passedPawns);

		return score;
	}

	public static int calculateBonusses(final ChessBoard cb) {
		int score = 0;

		// bonus for side to move
		score += ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalConstants.SIDE_TO_MOVE_BONUS;

		// pawn push threat
		long piece = (cb.pieces[WHITE][PAWN] << 8) & cb.emptySpaces & ~cb.attacksAll[BLACK];
		score += Long.bitCount(Bitboard.getWhitePawnAttacks(piece) & cb.friendlyPieces[BLACK])
				* EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_PUSH_THREAT];
		piece = (cb.pieces[BLACK][PAWN] >> 8) & cb.emptySpaces & ~cb.attacksAll[WHITE];
		score -= Long.bitCount(Bitboard.getBlackPawnAttacks(piece) & cb.friendlyPieces[WHITE])
				* EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_PUSH_THREAT];

		// knight bonus if there are a lot of pawns
		score += Long.bitCount(cb.pieces[WHITE][NIGHT]) * EvalConstants.NIGHT_PAWN_BONUS[Long.bitCount(cb.pieces[WHITE][PAWN])];
		score -= Long.bitCount(cb.pieces[BLACK][NIGHT]) * EvalConstants.NIGHT_PAWN_BONUS[Long.bitCount(cb.pieces[BLACK][PAWN])];

		// hanging pieces
		piece = cb.attacksAll[WHITE] & cb.friendlyPieces[BLACK] & ~cb.attacksAll[BLACK];
		while (piece != 0) {
			if (cb.colorToMove == WHITE) {
				score += EvalConstants.HANGING_PIECES[cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]];
			} else {
				score += EvalConstants.HANGING_PIECES_2[cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]];
			}
			piece &= piece - 1;
		}
		piece = cb.attacksAll[BLACK] & cb.friendlyPieces[WHITE] & ~cb.attacksAll[WHITE];
		while (piece != 0) {
			if (cb.colorToMove == BLACK) {
				score -= EvalConstants.HANGING_PIECES[cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]];
			} else {
				score -= EvalConstants.HANGING_PIECES_2[cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]];
			}
			piece &= piece - 1;
		}

		// double bishop bonus
		if (Long.bitCount(cb.pieces[WHITE][BISHOP]) == 2) {
			score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BISHOP_DOUBLE];
		}
		if (Long.bitCount(cb.pieces[BLACK][BISHOP]) == 2) {
			score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BISHOP_DOUBLE];
		}

		// bonus for small king-king distance in KKR and KKQ endgame
		if (Long.bitCount(cb.allPieces) == 3) {
			if (cb.pieces[WHITE][ROOK] != 0 || cb.pieces[WHITE][QUEEN] != 0) {
				score += EvalConstants.KKR_KKQ_KING_DISTANCE_SCORE[Util.getDistance(cb.kingIndex[WHITE], cb.kingIndex[BLACK])];
			} else if (cb.pieces[BLACK][ROOK] != 0 || cb.pieces[BLACK][QUEEN] != 0) {
				score -= EvalConstants.KKR_KKQ_KING_DISTANCE_SCORE[Util.getDistance(cb.kingIndex[WHITE], cb.kingIndex[BLACK])];
			}
		}

		// bonus for rook on open-file (no pawns) and semi-open-file (no friendly pawns)
		piece = cb.pieces[WHITE][ROOK];
		while (piece != 0) {
			if ((cb.pieces[WHITE][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
				if ((cb.pieces[BLACK][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
					score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_OPEN];
				} else {
					score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_SEMI_OPEN];
				}
			}

			// bonus if rook is on same file as passed-pawn
			if ((Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7] & cb.passedPawns) != 0) {
				score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_PASSED_PAWN_FILE];
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			if ((cb.pieces[BLACK][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
				if ((cb.pieces[WHITE][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
					score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_OPEN];
				} else {
					score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_SEMI_OPEN];
				}
			}
			// bonus if rook is on same file as passed-pawn
			if ((Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7] & cb.passedPawns) != 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_PASSED_PAWN_FILE];
			}
			piece &= piece - 1;
		}

		// knight outpost: protected by a pawn, cannot be attacked by enemy pawns
		piece = cb.pieces[WHITE][NIGHT];
		while (piece != 0) {
			if ((Long.lowestOneBit(piece) & cb.attacks[WHITE][PAWN]) != 0) {
				if ((ChessConstants.MASK_ADJACENT_FILE_UP[Long.numberOfTrailingZeros(piece)] & cb.pieces[BLACK][PAWN]) == 0) {
					score += EvalConstants.KNIGHT_OUTPOST[Long.numberOfTrailingZeros(piece) / 8];
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT];
		while (piece != 0) {
			if ((Long.lowestOneBit(piece) & cb.attacks[BLACK][PAWN]) != 0) {
				if ((ChessConstants.MASK_ADJACENT_FILE_DOWN[Long.numberOfTrailingZeros(piece)] & cb.pieces[WHITE][PAWN]) == 0) {
					score -= EvalConstants.KNIGHT_OUTPOST[7 - Long.numberOfTrailingZeros(piece) / 8];
				}
			}
			piece &= piece - 1;
		}

		// bishop outpost: protected by a pawn, cannot be attacked by enemy pawns
		piece = cb.pieces[WHITE][BISHOP];
		while (piece != 0) {
			if ((Long.lowestOneBit(piece) & cb.attacks[WHITE][PAWN]) != 0) {
				if ((ChessConstants.MASK_ADJACENT_FILE_UP[Long.numberOfTrailingZeros(piece)] & cb.pieces[BLACK][PAWN]) == 0) {
					score += EvalConstants.BISHOP_OUTPOST[Long.numberOfTrailingZeros(piece) / 8];
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][BISHOP];
		while (piece != 0) {
			if ((Long.lowestOneBit(piece) & cb.attacks[BLACK][PAWN]) != 0) {
				if ((ChessConstants.MASK_ADJACENT_FILE_DOWN[Long.numberOfTrailingZeros(piece)] & cb.pieces[WHITE][PAWN]) == 0) {
					score -= EvalConstants.BISHOP_OUTPOST[7 - Long.numberOfTrailingZeros(piece) / 8];
				}
			}
			piece &= piece - 1;
		}

		// queen and nights
		if (cb.pieces[WHITE][QUEEN] != 0) {
			score += Long.bitCount(cb.pieces[WHITE][NIGHT]) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_QUEEN_NIGHT];
		}
		if (cb.pieces[BLACK][QUEEN] != 0) {
			score -= Long.bitCount(cb.pieces[BLACK][NIGHT]) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_QUEEN_NIGHT];
		}

		// rook on 7th, king on 8th
		if (cb.kingIndex[BLACK] >= 56) {
			score += Long.bitCount(cb.pieces[WHITE][ROOK] & Bitboard.RANK_7) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_7TH_RANK];
		}
		if (cb.kingIndex[WHITE] <= 7) {
			score -= Long.bitCount(cb.pieces[BLACK][ROOK] & Bitboard.RANK_2) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_7TH_RANK];
		}

		// piece is attacked by a pawn that is not defended by a pawn
		score += Long.bitCount(cb.attacks[WHITE][PAWN] & cb.friendlyPieces[BLACK] & ~cb.pieces[BLACK][PAWN])
				* EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_ATTACKS];
		score -= Long.bitCount(cb.attacks[BLACK][PAWN] & cb.friendlyPieces[WHITE] & ~cb.pieces[WHITE][PAWN])
				* EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_ATTACKS];

		// multiple pawn attacks possible
		if (Long.bitCount(cb.attacks[WHITE][PAWN] & cb.friendlyPieces[BLACK]) > 1) {
			score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_MULTIPLE_PAWN_ATTACKS];
		}
		if (Long.bitCount(cb.attacks[BLACK][PAWN] & cb.friendlyPieces[WHITE]) > 1) {
			score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_MULTIPLE_PAWN_ATTACKS];
		}

		return score;
	}

	public static int calculatePawnShieldBonus(final ChessBoard cb) {
		int score = 0;
		long piece = 0;

		// pawn-shield
		if ((cb.pieces[WHITE][KING] & Bitboard.KING_SIDE) != 0) {
			// king-side
			piece = cb.pieces[WHITE][PAWN] & ChessConstants.KING_PAWN_SHIELD_KINGSIDE_MASK[WHITE][cb.kingIndex[WHITE] / 8];
		} else if ((cb.pieces[WHITE][KING] & Bitboard.QUEEN_SIDE) != 0) {
			// queen-side
			piece = cb.pieces[WHITE][PAWN] & ChessConstants.KING_PAWN_SHIELD_QUEENSIDE_MASK[WHITE][cb.kingIndex[WHITE] / 8];
		}
		while (piece != 0) {
			score += EvalConstants.PAWN_SHIELD_BONUS[Long.numberOfTrailingZeros(piece) / 8];
			piece &= ~Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7];
		}
		if ((cb.pieces[BLACK][KING] & Bitboard.KING_SIDE) != 0) {
			// king-side
			piece = cb.pieces[BLACK][PAWN] & ChessConstants.KING_PAWN_SHIELD_KINGSIDE_MASK[BLACK][cb.kingIndex[BLACK] / 8];
		} else if ((cb.pieces[BLACK][KING] & Bitboard.QUEEN_SIDE) != 0) {
			// queen-side
			piece = cb.pieces[BLACK][PAWN] & ChessConstants.KING_PAWN_SHIELD_QUEENSIDE_MASK[BLACK][cb.kingIndex[BLACK] / 8];
		}
		while (piece != 0) {
			score -= EvalConstants.PAWN_SHIELD_BONUS[7 - (63 - Long.numberOfLeadingZeros(piece)) / 8];
			piece &= ~Bitboard.FILES[(63 - Long.numberOfLeadingZeros(piece)) & 7];
		}

		return score;
	}

	public static int calculatePenalties(final ChessBoard cb) {
		int score = 0;

		if (cb.moveCounter < EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_OPENING_MOVE_COUNTER]) {
			// penalty for moving queen in opening
			if ((Bitboard.D1_E1_D2_E2 & cb.pieces[WHITE][QUEEN]) == 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_QUEEN_MOVING_OPENINGGAME];
			}
			if ((Bitboard.D7_E7_D8_E8 & cb.pieces[BLACK][QUEEN]) == 0) {
				score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_QUEEN_MOVING_OPENINGGAME];
			}

			// penalty for moving king in opening and not castling
			// TODO only apply if could castle?
			if (!EngineConstants.TEST_EVAL_VALUES) {
				if ((Bitboard.D1_F1 & cb.pieces[WHITE][KING]) != 0) {
					score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_KING_MOVING_OPENINGGAME];
				}
				if ((Bitboard.D8_F8 & cb.pieces[BLACK][KING]) != 0) {
					score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_KING_MOVING_OPENINGGAME];
				}
			}
		}

		// TODO weak pieces

		// queen under attack
		score -= Long.bitCount(cb.attacksAll[BLACK] & cb.pieces[WHITE][QUEEN]) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_QUEEN_ATTACKED];
		score += Long.bitCount(cb.attacksAll[WHITE] & cb.pieces[BLACK][QUEEN]) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_QUEEN_ATTACKED];

		// penalty for having pinned-pieces
		long pieces = cb.pinnedPieces & cb.friendlyPieces[WHITE];
		while (pieces != 0) {
			score -= EvalConstants.PINNED_PIECE_SCORES[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
			pieces &= pieces - 1;
		}
		pieces = cb.pinnedPieces & cb.friendlyPieces[BLACK];
		while (pieces != 0) {
			score += EvalConstants.PINNED_PIECE_SCORES[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
			pieces &= pieces - 1;
		}

		// rook pair
		if (Long.bitCount(cb.pieces[WHITE][ROOK]) > 1) {
			score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_PAIR];
		}
		if (Long.bitCount(cb.pieces[BLACK][ROOK]) > 1) {
			score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_PAIR];
		}

		// rook prison
		if (!EngineConstants.TEST_EVAL_VALUES) {
			if ((cb.pieces[WHITE][ROOK] & EvalConstants.ROOK_PRISON[cb.kingIndex[WHITE]]) != 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_PRISON];
			}
			if ((cb.pieces[BLACK][ROOK] & EvalConstants.ROOK_PRISON[cb.kingIndex[BLACK]]) != 0) {
				score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_PRISON];
			}
		}

		// bishop prison
		pieces = cb.pieces[WHITE][BISHOP];
		while (pieces != 0) {
			if (Long.bitCount((EvalConstants.BISHOP_PRISON[Long.numberOfTrailingZeros(pieces)]) & cb.pieces[BLACK][PAWN]) == 2) {
				score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BISHOP_PRISON];
			}
			pieces &= pieces - 1;
		}
		pieces = cb.pieces[BLACK][BISHOP];
		while (pieces != 0) {
			if (Long.bitCount((EvalConstants.BISHOP_PRISON[Long.numberOfTrailingZeros(pieces)]) & cb.pieces[WHITE][PAWN]) == 2) {
				score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BISHOP_PRISON];
			}
			pieces &= pieces - 1;
		}

		return score;
	}

	public static void calculateMobilityScoresAndSetAttackBoards(final ChessBoard cb) {

		if (!EngineConstants.ENABLE_EVAL_MOBILITY) {
			return;
		}

		cb.mobilityScore[MG] = 0;
		cb.mobilityScore[EG] = 0;

		// clear attacks
		for (int piece = NIGHT; piece <= QUEEN; piece++) {
			cb.attacks[WHITE][piece] = 0;
			cb.attacks[BLACK][piece] = 0;
		}
		long moves;

		// pawns
		cb.attacks[WHITE][PAWN] = Bitboard.getWhitePawnAttacks(cb.pieces[WHITE][PAWN]);
		cb.attacks[BLACK][PAWN] = Bitboard.getBlackPawnAttacks(cb.pieces[BLACK][PAWN]);

		// knights
		long piece = cb.pieces[WHITE][NIGHT] & ~cb.pinnedPieces;
		while (piece != 0) {
			moves = StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)];
			cb.attacks[WHITE][NIGHT] |= moves;
			cb.mobilityScore[MG] += EvalConstants.MOBILITY_KNIGHT[Long.bitCount(moves & ~cb.friendlyPieces[WHITE] & ~cb.attacks[BLACK][PAWN])];
			cb.mobilityScore[EG] += EvalConstants.MOBILITY_KNIGHT_EG[Long.bitCount(moves & ~cb.friendlyPieces[WHITE] & ~cb.attacks[BLACK][PAWN])];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT] & ~cb.pinnedPieces;
		while (piece != 0) {
			moves = StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)];
			cb.attacks[BLACK][NIGHT] |= moves;
			cb.mobilityScore[MG] -= EvalConstants.MOBILITY_KNIGHT[Long.bitCount(moves & ~cb.friendlyPieces[BLACK] & ~cb.attacks[WHITE][PAWN])];
			cb.mobilityScore[EG] -= EvalConstants.MOBILITY_KNIGHT_EG[Long.bitCount(moves & ~cb.friendlyPieces[BLACK] & ~cb.attacks[WHITE][PAWN])];
			piece &= piece - 1;
		}

		// bishops
		piece = cb.pieces[WHITE][BISHOP];
		while (piece != 0) {
			moves = MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			cb.attacks[WHITE][BISHOP] |= moves;
			cb.mobilityScore[MG] += EvalConstants.MOBILITY_BISHOP[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			cb.mobilityScore[EG] += EvalConstants.MOBILITY_BISHOP_EG[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][BISHOP];
		while (piece != 0) {
			moves = MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			cb.attacks[BLACK][BISHOP] |= moves;
			cb.mobilityScore[MG] -= EvalConstants.MOBILITY_BISHOP[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			cb.mobilityScore[EG] -= EvalConstants.MOBILITY_BISHOP_EG[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			piece &= piece - 1;
		}

		// rooks
		piece = cb.pieces[WHITE][ROOK];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			cb.attacks[WHITE][ROOK] |= moves;
			cb.mobilityScore[MG] += EvalConstants.MOBILITY_ROOK[Long.bitCount(moves & ~cb.friendlyPieces[WHITE] & ~cb.attacks[BLACK][PAWN])];
			cb.mobilityScore[EG] += EvalConstants.MOBILITY_ROOK_EG[Long.bitCount(moves & ~cb.friendlyPieces[WHITE] & ~cb.attacks[BLACK][PAWN])];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			cb.attacks[BLACK][ROOK] |= moves;
			cb.mobilityScore[MG] -= EvalConstants.MOBILITY_ROOK[Long.bitCount(moves & ~cb.friendlyPieces[BLACK] & ~cb.attacks[WHITE][PAWN])];
			cb.mobilityScore[EG] -= EvalConstants.MOBILITY_ROOK_EG[Long.bitCount(moves & ~cb.friendlyPieces[BLACK] & ~cb.attacks[WHITE][PAWN])];
			piece &= piece - 1;
		}

		// queens
		piece = cb.pieces[WHITE][QUEEN];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces)
					| MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			cb.attacks[WHITE][QUEEN] |= moves;
			cb.mobilityScore[MG] += EvalConstants.MOBILITY_QUEEN[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			cb.mobilityScore[EG] += EvalConstants.MOBILITY_QUEEN_EG[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][QUEEN];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces)
					| MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			cb.attacks[BLACK][QUEEN] |= moves;
			cb.mobilityScore[MG] -= EvalConstants.MOBILITY_QUEEN[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			cb.mobilityScore[EG] -= EvalConstants.MOBILITY_QUEEN_EG[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			piece &= piece - 1;
		}

		cb.attacksWithoutKing[WHITE] = cb.attacks[WHITE][PAWN] | cb.attacks[WHITE][NIGHT] | cb.attacks[WHITE][BISHOP] | cb.attacks[WHITE][ROOK]
				| cb.attacks[WHITE][QUEEN];
		cb.attacksWithoutKing[BLACK] = cb.attacks[BLACK][PAWN] | cb.attacks[BLACK][NIGHT] | cb.attacks[BLACK][BISHOP] | cb.attacks[BLACK][ROOK]
				| cb.attacks[BLACK][QUEEN];

		// kings
		// TODO king-attacks with or without enemy attacks?
		moves = StaticMoves.KING_MOVES[cb.kingIndex[WHITE]] & ~StaticMoves.KING_MOVES[cb.kingIndex[BLACK]] & ~cb.attacksWithoutKing[BLACK]
				& (cb.emptySpaces | cb.friendlyPieces[BLACK]);
		cb.attacks[WHITE][KING] = moves;
		cb.mobilityScore[MG] += EvalConstants.MOBILITY_KING[Long.bitCount(moves)];
		cb.mobilityScore[EG] += EvalConstants.MOBILITY_KING_EG[Long.bitCount(moves)];
		moves = StaticMoves.KING_MOVES[cb.kingIndex[BLACK]] & ~StaticMoves.KING_MOVES[cb.kingIndex[WHITE]] & ~cb.attacksWithoutKing[WHITE]
				& (cb.emptySpaces | cb.friendlyPieces[WHITE]);
		cb.attacks[BLACK][KING] = moves;
		cb.mobilityScore[MG] -= EvalConstants.MOBILITY_KING[Long.bitCount(moves)];
		cb.mobilityScore[EG] -= EvalConstants.MOBILITY_KING_EG[Long.bitCount(moves)];

		// set all attacks
		cb.attacksAll[WHITE] = cb.attacksWithoutKing[WHITE] | cb.attacks[WHITE][KING];
		cb.attacksAll[BLACK] = cb.attacksWithoutKing[BLACK] | cb.attacks[BLACK][KING];
	}

	public static int calculateKingSafetyScores(final ChessBoard cb) {

		// TODO does not see that 2 pieces of the same type are attacking the same position (chance is small...)
		// TODO battery
		// TODO rewrite

		if (!EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
			return 0;
		}

		int score = 0;

		long nearbyPosition;
		int counter;

		for (int kingColor = WHITE; kingColor <= BLACK; kingColor++) {
			final int enemyColor = 1 - kingColor;

			if (cb.pieces[enemyColor][QUEEN] == 0) {
				continue;
			}

			counter = EvalConstants.KING_SAFETY_COUNTER_RANKS[(7 * kingColor) + ChessConstants.COLOR_FACTOR[kingColor] * cb.kingIndex[kingColor] / 8];

			// check for kingArea attackers and friendlyPieces
			nearbyPosition = cb.kingArea[kingColor];
			while (nearbyPosition != 0) {
				if ((cb.attacksAll[enemyColor] & Long.lowestOneBit(nearbyPosition)) != 0) {
					counter++;
				}
				if ((cb.friendlyPieces[kingColor] & Long.lowestOneBit(nearbyPosition)) == 0) {
					// no friendlyPiece nearby
					counter++;
				}
				nearbyPosition &= nearbyPosition - 1;
			}

			// bonus for small king-queen distance
			if (Long.bitCount(cb.pieces[enemyColor][QUEEN]) == 1
					&& (cb.attacksAll[kingColor] & Util.POWER_LOOKUP[Long.numberOfTrailingZeros(cb.pieces[enemyColor][QUEEN])]) == 0) {
				counter += EvalConstants.KING_SAFETY_QUEEN_TROPISM[Util.getDistance(cb.kingIndex[kingColor],
						Long.numberOfTrailingZeros(cb.pieces[enemyColor][QUEEN]))];
			}

			// queen-touch check possible
			nearbyPosition = StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & ~cb.friendlyPieces[enemyColor];
			while (nearbyPosition != 0) {
				if ((cb.attacks[enemyColor][QUEEN] & Long.lowestOneBit(nearbyPosition)) != 0
						&& (cb.attacksWithoutKing[kingColor] & Long.lowestOneBit(nearbyPosition)) == 0) {
					if (((cb.attacks[enemyColor][PAWN] | cb.attacks[enemyColor][NIGHT] | cb.attacks[enemyColor][BISHOP] | cb.attacks[enemyColor][ROOK])
							& Long.lowestOneBit(nearbyPosition)) != 0) {
						counter += EvalConstants.KING_SAFETY_COUNTERS[0];
					}
				}
				nearbyPosition &= nearbyPosition - 1;
			}

			// safe check possible
			if (cb.pieces[enemyColor][NIGHT] != 0) {
				nearbyPosition = StaticMoves.KNIGHT_MOVES[cb.kingIndex[kingColor]] & ~cb.attacksAll[kingColor] & ~cb.friendlyPieces[enemyColor];
				while (nearbyPosition != 0) {
					if ((cb.attacks[enemyColor][NIGHT] & Long.lowestOneBit(nearbyPosition)) != 0) {
						counter += EvalConstants.KING_SAFETY_CHECK[NIGHT];
						break;
					}
					nearbyPosition &= nearbyPosition - 1;
				}
			}
			nearbyPosition = MagicUtil.getBishopMoves(cb.kingIndex[kingColor], cb.allPieces, cb.friendlyPieces[enemyColor])
					& ~StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & ~cb.attacksAll[kingColor];
			long queenMoves = nearbyPosition;
			if (cb.pieces[enemyColor][BISHOP] != 0) {
				while (nearbyPosition != 0) {
					if ((cb.attacks[enemyColor][BISHOP] & Long.lowestOneBit(nearbyPosition)) != 0) {
						counter += EvalConstants.KING_SAFETY_CHECK[BISHOP];
						break;
					}
					nearbyPosition &= nearbyPosition - 1;
				}
			}
			nearbyPosition = MagicUtil.getRookMoves(cb.kingIndex[kingColor], cb.allPieces, cb.friendlyPieces[enemyColor])
					& ~StaticMoves.KING_MOVES[cb.kingIndex[kingColor]] & ~cb.attacksAll[kingColor];
			queenMoves |= nearbyPosition;
			if (cb.pieces[enemyColor][ROOK] != 0) {
				while (nearbyPosition != 0) {
					if ((cb.attacks[enemyColor][ROOK] & Long.lowestOneBit(nearbyPosition)) != 0) {
						counter += EvalConstants.KING_SAFETY_CHECK[ROOK];
						break;
					}
					nearbyPosition &= nearbyPosition - 1;
				}
			}
			while (queenMoves != 0) {
				if ((cb.attacks[enemyColor][QUEEN] & Long.lowestOneBit(queenMoves)) != 0) {
					counter += EvalConstants.KING_SAFETY_CHECK_QUEEN[Long.bitCount(cb.friendlyPieces[kingColor])];
					break;
				}
				queenMoves &= queenMoves - 1;
			}

			// bonus for stm
			if (cb.colorToMove == enemyColor) {
				counter++;
			}

			// TODO ugly
			int flag = 0;
			for (int piece = PAWN; piece <= QUEEN; piece++) {
				if ((cb.attacks[enemyColor][piece] & cb.kingArea[kingColor]) != 0) {
					flag |= Util.POWER_LOOKUP[piece - 1];
				}
			}

			score += ChessConstants.COLOR_FACTOR[enemyColor]
					* EvalConstants.KING_SAFETY_SCORES[counter + EvalConstants.KING_SAFETY_ATTACK_PATTERN_COUNTER[flag]];
		}

		return score;
	}

	public static int calculatePositionScores(final ChessBoard cb) {

		int score = 0;
		for (int color = WHITE; color <= BLACK; color++) {
			for (int pieceType = PAWN; pieceType <= KING; pieceType++) {
				long piece = cb.pieces[color][pieceType];
				while (piece != 0) {
					score += EvalConstants.PSQT_SCORES[pieceType][color][Long.numberOfTrailingZeros(piece)];
					piece &= piece - 1;
				}
			}
		}
		return score;
	}

	public static int calculatePositionEgScores(final ChessBoard cb) {

		int score = 0;
		for (int color = WHITE; color <= BLACK; color++) {
			for (int pieceType = PAWN; pieceType <= KING; pieceType++) {
				long piece = cb.pieces[color][pieceType];
				while (piece != 0) {
					score += EvalConstants.PSQT_EG_SCORES[pieceType][color][Long.numberOfTrailingZeros(piece)];
					piece &= piece - 1;
				}
			}
		}
		return score;
	}

	public static int calculateMaterialExcludingPawnScores(final ChessBoard cb) {
		// @formatter:off
		return 	(Long.bitCount(cb.pieces[WHITE][NIGHT])		- Long.bitCount(cb.pieces[BLACK][NIGHT]))	* EvalConstants.MATERIAL_SCORES[NIGHT] + 
				(Long.bitCount(cb.pieces[WHITE][BISHOP])	- Long.bitCount(cb.pieces[BLACK][BISHOP]))	* EvalConstants.MATERIAL_SCORES[BISHOP] + 
				(Long.bitCount(cb.pieces[WHITE][ROOK])		- Long.bitCount(cb.pieces[BLACK][ROOK]))	* EvalConstants.MATERIAL_SCORES[ROOK] + 
				(Long.bitCount(cb.pieces[WHITE][QUEEN]) 	- Long.bitCount(cb.pieces[BLACK][QUEEN]))	* EvalConstants.MATERIAL_SCORES[QUEEN];
		// @formatter:on
	}

	public static int calculateMaterialIncludingPawnScores(final ChessBoard cb) {
		return calculateMaterialExcludingPawnScores(cb)
				+ (Long.bitCount(cb.pieces[ChessConstants.WHITE][PAWN]) - Long.bitCount(cb.pieces[BLACK][PAWN])) * EvalConstants.MATERIAL_SCORES[PAWN];
	}

	public static int calculatePawnWithoutMaterialScores(ChessBoard cb) {
		return calculatePawnIncludingMaterialScores(cb)
				- (Long.bitCount(cb.pieces[ChessConstants.WHITE][PAWN]) - Long.bitCount(cb.pieces[BLACK][PAWN])) * EvalConstants.MATERIAL_SCORES[PAWN];
	}

}
