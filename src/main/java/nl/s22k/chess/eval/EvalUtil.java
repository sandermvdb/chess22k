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
import nl.s22k.chess.ChessBoardTestUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public class EvalUtil {

	public static final int MG = 0;
	public static final int EG = 1;

	public static final int PHASE_TOTAL = 4 * EvalConstants.PHASE[NIGHT] + 4 * EvalConstants.PHASE[BISHOP] + 4 * EvalConstants.PHASE[ROOK]
			+ 2 * EvalConstants.PHASE[QUEEN];

	public static int getScore(final ChessBoard cb) {

		if (Statistics.ENABLED) {
			Statistics.evalNodes++;
		}

		if (EngineConstants.ENABLE_EVAL_CACHE && !EngineConstants.TEST_EVAL_CACHES) {
			final int score = EvalCache.getScore(cb.zobristKey);
			if (score != ChessConstants.CACHE_MISS) {
				return score;
			}
		}

		return calculateScore(cb);
	}

	public static int calculateScore(final ChessBoard cb) {

		int score = 0;
		if (!cb.isDrawByMaterial()) {
			score = taperedEval(cb);
		}

		// TODO check if material score has one side leading

		/* draw-by-material */
		if (score > 25) {
			if (cb.isDrawByMaterial(WHITE)) {
				if (Statistics.ENABLED) {
					Statistics.drawByMaterialCount++;
				}
				score = EvalConstants.SCORE_DRAW;
			} else if (cb.isDrawishByMaterial(WHITE)) {
				if (Statistics.ENABLED) {
					Statistics.drawishByMaterialCount++;
				}
				// keep king out of the corners
				score = Bitboard.manhattanCenterDistance(cb.kingIndex[BLACK]) * 10;
			}
		} else if (score < -25) {
			if (cb.isDrawByMaterial(BLACK)) {
				if (Statistics.ENABLED) {
					Statistics.drawByMaterialCount++;
				}
				score = EvalConstants.SCORE_DRAW;
			} else if (cb.isDrawishByMaterial(BLACK)) {
				if (Statistics.ENABLED) {
					Statistics.drawishByMaterialCount++;
				}
				// keep king out of the corners
				score = -Bitboard.manhattanCenterDistance(cb.kingIndex[WHITE]) * 10;
			}
		}

		if (EngineConstants.TEST_EVAL_CACHES) {
			final int cachedScore = EvalCache.getScore(cb.zobristKey);
			if (cachedScore != ChessConstants.CACHE_MISS) {
				if (cachedScore != score) {
					throw new RuntimeException(String.format("Cached eval score != score: %s, %s", cachedScore, score));
				}
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

	private static int taperedEval(final ChessBoard cb) {
		final int mobilityScore = calculateMobilityScoresAndSetAttackBoards(cb);
		final int phaseIndependentScore = getPawnScores(cb) + calculateOthers(cb) + getImbalances(cb) + calculateThreats(cb);

		int scoreMg = 0;
		if (cb.phase != PHASE_TOTAL) {
			scoreMg = EngineConstants.ENABLE_INCREMENTAL_PSQT ? cb.psqtScore : calculatePositionScores(cb);
			scoreMg += getMgScore(mobilityScore) + KingSafetyEval.calculateKingSafetyScores(cb) + calculateSpace(cb) + calculatePawnShieldBonus(cb)
					+ phaseIndependentScore;
		}

		int scoreEg = EngineConstants.ENABLE_INCREMENTAL_PSQT ? cb.psqtScoreEg : calculatePositionEgScores(cb);
		scoreEg += getEgScore(mobilityScore) + PassedPawnEval.calculatePassedPawnScores(cb) + phaseIndependentScore;

		return ((scoreMg * (PHASE_TOTAL - cb.phase)) + (scoreEg * cb.phase)) / PHASE_TOTAL / calculateScaleFactor(cb);
	}

	public static int score(final int mgScore, final int egScore) {
		return (mgScore << 16) + egScore;
	}

	public static int getMgScore(final int score) {
		return (score + 0x8000) >> 16;
	}

	public static int getEgScore(final int score) {
		return (short) (score & 0xffff);
	}

	private static int calculateScaleFactor(final ChessBoard cb) {
		// opposite bishops endgame?
		if (MaterialUtil.oppositeBishops(cb.materialKey)) {
			if (((cb.pieces[WHITE][BISHOP] & Bitboard.BLACK_SQUARES) == 0) == ((cb.pieces[BLACK][BISHOP] & Bitboard.WHITE_SQUARES) == 0)) {
				return 2;
			}
		}
		return 1;
	}

	private static void compareScores(final ChessBoard cb1, final ChessBoard cb2, final int factor) {

		calculateMobilityScoresAndSetAttackBoards(cb1);
		calculateMobilityScoresAndSetAttackBoards(cb2);

		if (KingSafetyEval.calculateKingSafetyScores(cb2) != KingSafetyEval.calculateKingSafetyScores(cb1) * factor) {
			System.out.println(
					"Unequal king-safety: " + KingSafetyEval.calculateKingSafetyScores(cb1) + " " + KingSafetyEval.calculateKingSafetyScores(cb2) * factor);
		}
		if (calculatePositionScores(cb1) != calculatePositionScores(cb2) * factor) {
			System.out.println("Unequal position score: " + calculatePositionScores(cb1) + " " + calculatePositionScores(cb2) * factor);
		}
		if (calculatePawnScores(cb1) != calculatePawnScores(cb2) * factor) {
			System.out.println("Unequal pawns: " + calculatePawnScores(cb1) + " " + calculatePawnScores(cb2) * factor);
		}
		if (calculateImbalances(cb1) != calculateImbalances(cb2) * factor) {
			System.out.println("Unequal imbalances: " + calculateImbalances(cb1) + " " + calculateImbalances(cb2) * factor);
		}
		if (calculateOthers(cb2) != calculateOthers(cb1) * factor) {
			System.out.println("Unequal others: " + calculateOthers(cb1) + " " + calculateOthers(cb2) * factor);
		}
		if (PassedPawnEval.calculatePassedPawnScores(cb1) != PassedPawnEval.calculatePassedPawnScores(cb2) * factor) {
			System.out.println(
					"Unequal passed-pawns: " + PassedPawnEval.calculatePassedPawnScores(cb1) + " " + PassedPawnEval.calculatePassedPawnScores(cb2) * factor);
		}
	}

	public static int calculateSpace(final ChessBoard cb) {

		int score = 0;

		score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_SPACE]
				* Long.bitCount((cb.pieces[WHITE][PAWN] >>> 8) & (cb.pieces[WHITE][NIGHT] | cb.pieces[WHITE][BISHOP]) & Bitboard.RANK_234);
		score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_SPACE]
				* Long.bitCount((cb.pieces[BLACK][PAWN] << 8) & (cb.pieces[BLACK][NIGHT] | cb.pieces[BLACK][BISHOP]) & Bitboard.RANK_567);

		return score;
	}

	public static int getPawnScores(final ChessBoard cb) {
		if (!EngineConstants.TEST_EVAL_CACHES) {
			final int score = PawnEvalCache.updateBoardAndGetScore(cb);
			if (score != ChessConstants.CACHE_MISS) {
				return PawnEvalCache.updateBoardAndGetScore(cb);
			}
		}

		return calculatePawnScores(cb);
	}

	private static int calculatePawnScores(final ChessBoard cb) {

		int score = 0;

		// penalty for doubled pawns
		for (int i = 0; i < 8; i++) {
			if (Long.bitCount(cb.pieces[WHITE][PAWN] & Bitboard.FILES[i]) > 1) {
				score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_DOUBLE];
			}
			if (Long.bitCount(cb.pieces[BLACK][PAWN] & Bitboard.FILES[i]) > 1) {
				score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_DOUBLE];
			}
		}

		// bonus for connected pawns
		long pawns = Bitboard.getWhitePawnAttacks(cb.pieces[WHITE][PAWN]) & cb.pieces[WHITE][PAWN];
		while (pawns != 0) {
			score += EvalConstants.PAWN_CONNECTED[Long.numberOfTrailingZeros(pawns) / 8];
			pawns &= pawns - 1;
		}
		pawns = Bitboard.getBlackPawnAttacks(cb.pieces[BLACK][PAWN]) & cb.pieces[BLACK][PAWN];
		while (pawns != 0) {
			score -= EvalConstants.PAWN_CONNECTED[7 - Long.numberOfTrailingZeros(pawns) / 8];
			pawns &= pawns - 1;
		}

		// bonus for neighbour pawns
		pawns = Bitboard.getPawnNeighbours(cb.pieces[WHITE][PAWN]) & cb.pieces[WHITE][PAWN];
		while (pawns != 0) {
			score += EvalConstants.PAWN_NEIGHBOUR[Long.numberOfTrailingZeros(pawns) / 8];
			pawns &= pawns - 1;
		}
		pawns = Bitboard.getPawnNeighbours(cb.pieces[BLACK][PAWN]) & cb.pieces[BLACK][PAWN];
		while (pawns != 0) {
			score -= EvalConstants.PAWN_NEIGHBOUR[7 - Long.numberOfTrailingZeros(pawns) / 8];
			pawns &= pawns - 1;
		}

		// set outposts
		cb.passedPawnsAndOutposts = 0;
		pawns = cb.attacks[WHITE][PAWN] & ~cb.pieces[WHITE][PAWN] & ~cb.pieces[BLACK][PAWN];
		while (pawns != 0) {
			if ((ChessConstants.MASK_ADJACENT_FILE_UP[Long.numberOfTrailingZeros(pawns)] & cb.pieces[BLACK][PAWN]) == 0) {
				cb.passedPawnsAndOutposts |= Long.lowestOneBit(pawns);
			}
			pawns &= pawns - 1;
		}
		pawns = cb.attacks[BLACK][PAWN] & ~cb.pieces[WHITE][PAWN] & ~cb.pieces[BLACK][PAWN];
		while (pawns != 0) {
			if ((ChessConstants.MASK_ADJACENT_FILE_DOWN[Long.numberOfTrailingZeros(pawns)] & cb.pieces[WHITE][PAWN]) == 0) {
				cb.passedPawnsAndOutposts |= Long.lowestOneBit(pawns);
			}
			pawns &= pawns - 1;
		}

		int index;

		// white
		pawns = cb.pieces[WHITE][PAWN];
		while (pawns != 0) {
			index = Long.numberOfTrailingZeros(pawns);

			// isolated pawns
			if ((Bitboard.FILES_ADJACENT[index & 7] & cb.pieces[WHITE][PAWN]) == 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_ISOLATED];
			}

			// backward pawns
			else if ((ChessConstants.MASK_ADJACENT_FILE_DOWN[index + 8] & cb.pieces[WHITE][PAWN]) == 0) {
				if ((StaticMoves.PAWN_ATTACKS[WHITE][index + 8] & cb.pieces[BLACK][PAWN]) != 0) {
					if ((StaticMoves.PAWN_ATTACKS[WHITE][index] & cb.pieces[BLACK][PAWN]) == 0) {
						score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_BACKWARD];
					}
				}
			}

			// set passed pawns
			if ((ChessConstants.PASSED_PAWN_MASKS[WHITE][index] & cb.pieces[BLACK][PAWN]) == 0) {
				cb.passedPawnsAndOutposts |= Long.lowestOneBit(pawns);
			}

			// candidate passed pawns (no pawns in front, more friendly pawns behind and adjacent than enemy pawns)
			else if (63 - Long.numberOfLeadingZeros((cb.pieces[WHITE][PAWN] | cb.pieces[BLACK][PAWN]) & Bitboard.FILES[index & 7]) == index) {
				if (Long.bitCount(cb.pieces[WHITE][PAWN] & ChessConstants.MASK_ADJACENT_FILE_DOWN[index + 8]) >= Long
						.bitCount(cb.pieces[BLACK][PAWN] & ChessConstants.MASK_ADJACENT_FILE_UP[index])) {
					score += EvalConstants.PASSED_CANDIDATE[index / 8];
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
				score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_ISOLATED];
			}

			// backward pawns
			else if ((ChessConstants.MASK_ADJACENT_FILE_UP[index - 8] & cb.pieces[BLACK][PAWN]) == 0) {
				if ((StaticMoves.PAWN_ATTACKS[BLACK][index - 8] & cb.pieces[WHITE][PAWN]) != 0) {
					if ((StaticMoves.PAWN_ATTACKS[BLACK][index] & cb.pieces[WHITE][PAWN]) == 0) {
						score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_PAWN_BACKWARD];
					}
				}
			}

			// set passed pawns
			if ((ChessConstants.PASSED_PAWN_MASKS[BLACK][index] & cb.pieces[WHITE][PAWN]) == 0) {
				cb.passedPawnsAndOutposts |= Long.lowestOneBit(pawns);
			}

			// candidate passers
			else if (Long.numberOfTrailingZeros((cb.pieces[WHITE][PAWN] | cb.pieces[BLACK][PAWN]) & Bitboard.FILES[index & 7]) == index) {
				if (Long.bitCount(cb.pieces[BLACK][PAWN] & ChessConstants.MASK_ADJACENT_FILE_UP[index - 8]) >= Long
						.bitCount(cb.pieces[WHITE][PAWN] & ChessConstants.MASK_ADJACENT_FILE_DOWN[index])) {
					score -= EvalConstants.PASSED_CANDIDATE[7 - index / 8];
				}
			}

			pawns &= pawns - 1;
		}

		if (EngineConstants.TEST_EVAL_CACHES) {
			final int cachedScore = PawnEvalCache.updateBoardAndGetScore(cb);
			if (cachedScore != ChessConstants.CACHE_MISS) {
				if (cachedScore != score) {
					throw new RuntimeException(String.format("Cached pawn eval score != score: %s, %s", cachedScore, score));
				}
			}
		}

		PawnEvalCache.addValue(cb.pawnZobristKey, score, cb.passedPawnsAndOutposts);

		return score;
	}

	public static int getImbalances(final ChessBoard cb) {
		if (!EngineConstants.TEST_EVAL_CACHES) {
			final int score = MaterialCache.getScore(cb.materialKey);
			if (score != ChessConstants.CACHE_MISS) {
				return score;
			}
		}
		return calculateImbalances(cb);
	}

	private static int calculateImbalances(final ChessBoard cb) {

		int score = 0;

		// material
		score += calculateMaterialScore(cb);

		// knight bonus if there are a lot of pawns
		score += Long.bitCount(cb.pieces[WHITE][NIGHT]) * EvalConstants.NIGHT_PAWN[Long.bitCount(cb.pieces[WHITE][PAWN])];
		score -= Long.bitCount(cb.pieces[BLACK][NIGHT]) * EvalConstants.NIGHT_PAWN[Long.bitCount(cb.pieces[BLACK][PAWN])];

		// double bishop bonus
		if (Long.bitCount(cb.pieces[WHITE][BISHOP]) == 2) {
			score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BISHOP_DOUBLE];
		}
		if (Long.bitCount(cb.pieces[BLACK][BISHOP]) == 2) {
			score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BISHOP_DOUBLE];
		}

		// queen and nights
		if (cb.pieces[WHITE][QUEEN] != 0) {
			score += Long.bitCount(cb.pieces[WHITE][NIGHT]) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_QUEEN_NIGHT];
		}
		if (cb.pieces[BLACK][QUEEN] != 0) {
			score -= Long.bitCount(cb.pieces[BLACK][NIGHT]) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_QUEEN_NIGHT];
		}

		// rook pair
		if (Long.bitCount(cb.pieces[WHITE][ROOK]) > 1) {
			score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_PAIR];
		}
		if (Long.bitCount(cb.pieces[BLACK][ROOK]) > 1) {
			score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_PAIR];
		}

		// advantage in having more pieces
		if (Long.bitCount(cb.allPieces) < EvalConstants.IMBALANCE.length) {
			final int diff = Long.bitCount(cb.friendlyPieces[WHITE]) - Long.bitCount(cb.friendlyPieces[BLACK]);
			score += diff * EvalConstants.IMBALANCE[Long.bitCount(cb.allPieces)];
		}

		if (EngineConstants.TEST_EVAL_CACHES) {
			final int cachedScore = MaterialCache.getScore(cb.materialKey);
			if (cachedScore != ChessConstants.CACHE_MISS) {
				if (cachedScore != score) {
					throw new RuntimeException(String.format("Cached material score != score: %s, %s", cachedScore, score));
				}
			}
		}

		MaterialCache.addValue(cb.materialKey, score);

		return score;
	}

	public static int calculateThreats(final ChessBoard cb) {
		int score = 0;
		final long whiteMinorAttacks = cb.attacks[WHITE][NIGHT] | cb.attacks[WHITE][BISHOP];
		final long blackMinorAttacks = cb.attacks[BLACK][NIGHT] | cb.attacks[BLACK][BISHOP];

		// unused outposts
		score += Long.bitCount(cb.passedPawnsAndOutposts & cb.emptySpaces & whiteMinorAttacks & cb.attacks[WHITE][PAWN])
				* EvalConstants.THREAT_SCORES[EvalConstants.INDEX_UNUSED_OUTPOST];
		score -= Long.bitCount(cb.passedPawnsAndOutposts & cb.emptySpaces & blackMinorAttacks & cb.attacks[BLACK][PAWN])
				* EvalConstants.THREAT_SCORES[EvalConstants.INDEX_UNUSED_OUTPOST];

		// piece attacked and only defended by a rook or queen
		long piece = cb.friendlyPieces[WHITE] & cb.attacksAll[BLACK] & cb.attacksAll[WHITE] & ~(cb.attacks[WHITE][PAWN] | whiteMinorAttacks);
		while (piece != 0) {
			score -= EvalConstants.NO_MINOR_DEFENSE[cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]];
			piece &= piece - 1;
		}
		piece = cb.friendlyPieces[BLACK] & cb.attacksAll[WHITE] & cb.attacksAll[BLACK] & ~(cb.attacks[BLACK][PAWN] | blackMinorAttacks);
		while (piece != 0) {
			score += EvalConstants.NO_MINOR_DEFENSE[cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]];
			piece &= piece - 1;
		}

		// pawn push threat
		piece = (cb.pieces[WHITE][PAWN] << 8) & cb.emptySpaces & ~cb.attacksAll[BLACK];
		score += Long.bitCount(Bitboard.getWhitePawnAttacks(piece) & cb.friendlyPieces[BLACK])
				* EvalConstants.THREAT_SCORES[EvalConstants.INDEX_PAWN_PUSH_THREAT];
		piece = (cb.pieces[BLACK][PAWN] >>> 8) & cb.emptySpaces & ~cb.attacksAll[WHITE];
		score -= Long.bitCount(Bitboard.getBlackPawnAttacks(piece) & cb.friendlyPieces[WHITE])
				* EvalConstants.THREAT_SCORES[EvalConstants.INDEX_PAWN_PUSH_THREAT];

		// hanging pieces
		piece = cb.attacksAll[WHITE] & cb.friendlyPieces[BLACK] & ~cb.attacksAll[BLACK];
		int hangingIndex;
		if (piece != 0) {
			if (Long.bitCount(piece) > 1) {
				hangingIndex = ChessConstants.QUEEN;
				while (piece != 0) {
					hangingIndex = Math.min(hangingIndex, cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]);
					piece &= piece - 1;
				}

				score += EvalConstants.HANGING_2[hangingIndex];
			} else {
				score += EvalConstants.HANGING[cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]];
			}
		}
		piece = cb.attacksAll[BLACK] & cb.friendlyPieces[WHITE] & ~cb.attacksAll[WHITE];
		if (piece != 0) {
			if (Long.bitCount(piece) > 1) {
				hangingIndex = ChessConstants.QUEEN;
				while (piece != 0) {
					hangingIndex = Math.min(hangingIndex, cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]);
					piece &= piece - 1;
				}
				score -= EvalConstants.HANGING_2[hangingIndex];
			} else {
				score -= EvalConstants.HANGING[cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]];
			}
		}

		// piece is attacked by a pawn
		score += Long.bitCount(cb.attacks[WHITE][PAWN] & cb.friendlyPieces[BLACK] & ~cb.pieces[BLACK][PAWN])
				* EvalConstants.THREAT_SCORES[EvalConstants.INDEX_PAWN_ATTACKS];
		score -= Long.bitCount(cb.attacks[BLACK][PAWN] & cb.friendlyPieces[WHITE] & ~cb.pieces[WHITE][PAWN])
				* EvalConstants.THREAT_SCORES[EvalConstants.INDEX_PAWN_ATTACKS];

		// multiple pawn attacks possible
		if (Long.bitCount(cb.attacks[WHITE][PAWN] & cb.friendlyPieces[BLACK]) > 1) {
			score += EvalConstants.THREAT_SCORES[EvalConstants.INDEX_MULTIPLE_PAWN_ATTACKS];
		}
		if (Long.bitCount(cb.attacks[BLACK][PAWN] & cb.friendlyPieces[WHITE]) > 1) {
			score -= EvalConstants.THREAT_SCORES[EvalConstants.INDEX_MULTIPLE_PAWN_ATTACKS];
		}

		// minors under attack and not defended by a pawn
		score += Long.bitCount(cb.attacksAll[WHITE] & (cb.pieces[BLACK][NIGHT] | cb.pieces[BLACK][BISHOP] & ~cb.attacksAll[BLACK]))
				* EvalConstants.THREAT_SCORES[EvalConstants.INDEX_MAJOR_ATTACKED];
		score -= Long.bitCount(cb.attacksAll[BLACK] & (cb.pieces[WHITE][NIGHT] | cb.pieces[WHITE][BISHOP] & ~cb.attacksAll[WHITE]))
				* EvalConstants.THREAT_SCORES[EvalConstants.INDEX_MAJOR_ATTACKED];

		if (cb.pieces[BLACK][QUEEN] != 0) {
			// queen under attack by rook
			score += Long.bitCount(cb.attacks[WHITE][ROOK] & cb.pieces[BLACK][QUEEN]) * EvalConstants.THREAT_SCORES[EvalConstants.INDEX_QUEEN_ATTACKED];
			// queen under attack by minors
			score += Long.bitCount(whiteMinorAttacks & cb.pieces[BLACK][QUEEN]) * EvalConstants.THREAT_SCORES[EvalConstants.INDEX_QUEEN_ATTACKED_MINOR];
		}

		if (cb.pieces[WHITE][QUEEN] != 0) {
			// queen under attack by rook
			score -= Long.bitCount(cb.attacks[BLACK][ROOK] & cb.pieces[WHITE][QUEEN]) * EvalConstants.THREAT_SCORES[EvalConstants.INDEX_QUEEN_ATTACKED];
			// queen under attack by minors
			score -= Long.bitCount(blackMinorAttacks & cb.pieces[WHITE][QUEEN]) * EvalConstants.THREAT_SCORES[EvalConstants.INDEX_QUEEN_ATTACKED_MINOR];
		}

		// rook under attack by minors
		score += Long.bitCount(whiteMinorAttacks & cb.pieces[BLACK][ROOK]) * EvalConstants.THREAT_SCORES[EvalConstants.INDEX_ROOK_ATTACKED];
		score -= Long.bitCount(blackMinorAttacks & cb.pieces[WHITE][ROOK]) * EvalConstants.THREAT_SCORES[EvalConstants.INDEX_ROOK_ATTACKED];

		// knight fork
		// skip when testing eval values because we break the loop if any fork has been found
		long pieces;
		if (!EngineConstants.TEST_EVAL_VALUES) {
			long forked;
			pieces = cb.attacks[WHITE][NIGHT] & ~cb.attacksAll[BLACK] & cb.emptySpaces;
			while (pieces != 0) {
				forked = cb.friendlyPieces[BLACK] & ~cb.pieces[BLACK][PAWN] & StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(pieces)];
				if (Long.bitCount(forked) > 1) {
					if ((cb.pieces[BLACK][KING] & forked) == 0) {
						score += EvalConstants.THREAT_SCORES[EvalConstants.INDEX_NIGHT_FORK];
					} else {
						score += EvalConstants.THREAT_SCORES[EvalConstants.INDEX_NIGHT_FORK_KING];
					}
					break;
				}
				pieces &= pieces - 1;
			}
			pieces = cb.attacks[BLACK][NIGHT] & ~cb.attacksAll[WHITE] & cb.emptySpaces;
			while (pieces != 0) {
				forked = cb.friendlyPieces[WHITE] & ~cb.pieces[WHITE][PAWN] & StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(pieces)];
				if (Long.bitCount(forked) > 1) {
					if ((cb.pieces[WHITE][KING] & forked) == 0) {
						score -= EvalConstants.THREAT_SCORES[EvalConstants.INDEX_NIGHT_FORK];
					} else {
						score -= EvalConstants.THREAT_SCORES[EvalConstants.INDEX_NIGHT_FORK_KING];
					}
					break;
				}
				pieces &= pieces - 1;
			}
		}

		// penalty for having pinned-pieces
		if (cb.pinnedPieces != 0) {
			pieces = cb.pinnedPieces & cb.friendlyPieces[WHITE] & ~cb.attacks[BLACK][PAWN];
			while (pieces != 0) {
				score -= EvalConstants.PINNED[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
				pieces &= pieces - 1;
			}
			pieces = cb.pinnedPieces & cb.friendlyPieces[BLACK] & ~cb.attacks[WHITE][PAWN];
			while (pieces != 0) {
				score += EvalConstants.PINNED[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
				pieces &= pieces - 1;
			}

			pieces = cb.pinnedPieces & cb.friendlyPieces[WHITE] & cb.attacks[BLACK][PAWN];
			while (pieces != 0) {
				score -= EvalConstants.PINNED_ATTACKED[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
				pieces &= pieces - 1;
			}
			pieces = cb.pinnedPieces & cb.friendlyPieces[BLACK] & cb.attacks[WHITE][PAWN];
			while (pieces != 0) {
				score += EvalConstants.PINNED_ATTACKED[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
				pieces &= pieces - 1;
			}
		}

		// bonus for having discovered-pieces
		if (cb.discoveredPieces != 0) {
			pieces = cb.discoveredPieces & cb.friendlyPieces[WHITE];
			while (pieces != 0) {
				score += EvalConstants.DISCOVERED[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
				pieces &= pieces - 1;
			}
			pieces = cb.discoveredPieces & cb.friendlyPieces[BLACK];
			while (pieces != 0) {
				score -= EvalConstants.DISCOVERED[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
				pieces &= pieces - 1;
			}
		}

		// quiescence search could leave one side in check
		if (cb.checkingPieces != 0) {
			if (Statistics.ENABLED) {
				Statistics.evaluatedInCheck++;
			}
			score += ChessConstants.COLOR_FACTOR[cb.colorToMoveInverse] * EvalConstants.IN_CHECK;
		}

		return score;
	}

	public static int calculateOthers(final ChessBoard cb) {
		int score = 0;
		long piece;

		// bonus for side to move
		score += ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalConstants.SIDE_TO_MOVE_BONUS;

		// WHITE ROOK
		if (cb.pieces[WHITE][ROOK] != 0) {
			// rook battery (same file)
			if (Long.bitCount(cb.pieces[WHITE][ROOK]) == 2) {
				if ((Long.numberOfTrailingZeros(cb.pieces[WHITE][ROOK]) & 7) == (63 - Long.numberOfLeadingZeros(cb.pieces[WHITE][ROOK]) & 7)) {
					score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_BATTERY];
				}
			}
			// bonus for rook on open-file (no pawns) and semi-open-file (no friendly pawns)
			piece = cb.pieces[WHITE][ROOK];
			while (piece != 0) {
				if ((cb.pieces[WHITE][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
					if ((cb.pieces[BLACK][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
						score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_OPEN];
					} else if ((cb.pieces[BLACK][PAWN] & cb.attacks[BLACK][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
						score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_SEMI_OPEN_ISOLATED];
					} else {
						score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_SEMI_OPEN];
					}
				}

				piece &= piece - 1;
			}

			// rook on 7th, king on 8th
			if (cb.kingIndex[BLACK] >= 56) {
				score += Long.bitCount(cb.pieces[WHITE][ROOK] & Bitboard.RANK_7) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_7TH_RANK];
			}

			// prison
			final long trapped = cb.pieces[WHITE][ROOK] & EvalConstants.ROOK_PRISON[cb.kingIndex[WHITE]];
			if (trapped != 0) {
				for (int i = 8; i <= 24; i += 8) {
					if ((trapped << i & cb.pieces[WHITE][PAWN]) != 0) {
						score -= EvalConstants.ROOK_TRAPPED[(i / 8) - 1];
						break;
					}
				}
			}
		}

		// BLACK ROOK
		if (cb.pieces[BLACK][ROOK] != 0) {

			// rook battery (same file)
			if (Long.bitCount(cb.pieces[BLACK][ROOK]) == 2) {
				if ((Long.numberOfTrailingZeros(cb.pieces[BLACK][ROOK]) & 7) == (63 - Long.numberOfLeadingZeros(cb.pieces[BLACK][ROOK]) & 7)) {
					score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_BATTERY];
				}
			}

			// bonus for rook on open-file (no pawns) and semi-open-file (no friendly pawns)
			piece = cb.pieces[BLACK][ROOK];
			while (piece != 0) {
				// TODO JITWatch unpredictable branch
				if ((cb.pieces[BLACK][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
					if ((cb.pieces[WHITE][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
						score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_OPEN];
					} else if ((cb.pieces[WHITE][PAWN] & cb.attacks[WHITE][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
						score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_SEMI_OPEN_ISOLATED];
					} else {
						score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_FILE_SEMI_OPEN];
					}
				}
				piece &= piece - 1;
			}

			// rook on 2nd, king on 1st
			if (cb.kingIndex[WHITE] <= 7) {
				score -= Long.bitCount(cb.pieces[BLACK][ROOK] & Bitboard.RANK_2) * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_ROOK_7TH_RANK];
			}

			// prison
			final long trapped = cb.pieces[BLACK][ROOK] & EvalConstants.ROOK_PRISON[cb.kingIndex[BLACK]];
			if (trapped != 0) {
				for (int i = 8; i <= 24; i += 8) {
					if ((trapped >>> i & cb.pieces[BLACK][PAWN]) != 0) {
						score += EvalConstants.ROOK_TRAPPED[(i / 8) - 1];
						break;
					}
				}
			}
		}

		// WHITE BISHOP
		if (cb.pieces[WHITE][BISHOP] != 0) {

			// bishop outpost: protected by a pawn, cannot be attacked by enemy pawns
			piece = cb.pieces[WHITE][BISHOP] & cb.passedPawnsAndOutposts & cb.attacks[WHITE][PAWN];
			while (piece != 0) {
				score += EvalConstants.BISHOP_OUTPOST[Long.numberOfTrailingZeros(piece) >>> 3];
				piece &= piece - 1;
			}

			// prison
			piece = cb.pieces[WHITE][BISHOP];
			while (piece != 0) {
				if (Long.bitCount((EvalConstants.BISHOP_PRISON[Long.numberOfTrailingZeros(piece)]) & cb.pieces[BLACK][PAWN]) == 2) {
					score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BISHOP_PRISON];
				}
				piece &= piece - 1;
			}

			if ((cb.pieces[WHITE][BISHOP] & Bitboard.WHITE_SQUARES) != 0) {
				// penalty for many pawns on same color as bishop
				score -= EvalConstants.SAME_COLORED_BISHOP_PAWN[Long.bitCount(cb.pieces[WHITE][PAWN] & Bitboard.WHITE_SQUARES)];

				// bonus for attacking center squares
				score += Long.bitCount(cb.attacks[WHITE][BISHOP] & Bitboard.E4_D5) / 2 * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_LONG_BISHOP];
			}
			if ((cb.pieces[WHITE][BISHOP] & Bitboard.BLACK_SQUARES) != 0) {
				// penalty for many pawns on same color as bishop
				score -= EvalConstants.SAME_COLORED_BISHOP_PAWN[Long.bitCount(cb.pieces[WHITE][PAWN] & Bitboard.BLACK_SQUARES)];

				// bonus for attacking center squares
				score += Long.bitCount(cb.attacks[WHITE][BISHOP] & Bitboard.D4_E5) / 2 * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_LONG_BISHOP];
			}

		}

		// BLACK BISHOP
		if (cb.pieces[BLACK][BISHOP] != 0) {

			// bishop outpost: protected by a pawn, cannot be attacked by enemy pawns
			piece = cb.pieces[BLACK][BISHOP] & cb.passedPawnsAndOutposts & cb.attacks[BLACK][PAWN];
			while (piece != 0) {
				score -= EvalConstants.BISHOP_OUTPOST[7 - Long.numberOfTrailingZeros(piece) / 8];
				piece &= piece - 1;
			}

			// prison
			piece = cb.pieces[BLACK][BISHOP];
			while (piece != 0) {
				if (Long.bitCount((EvalConstants.BISHOP_PRISON[Long.numberOfTrailingZeros(piece)]) & cb.pieces[WHITE][PAWN]) == 2) {
					score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BISHOP_PRISON];
				}
				piece &= piece - 1;
			}

			if ((cb.pieces[BLACK][BISHOP] & Bitboard.WHITE_SQUARES) != 0) {
				// penalty for many pawns on same color as bishop
				score += EvalConstants.SAME_COLORED_BISHOP_PAWN[Long.bitCount(cb.pieces[BLACK][PAWN] & Bitboard.WHITE_SQUARES)];

				// bonus for attacking center squares
				score -= Long.bitCount(cb.attacks[BLACK][BISHOP] & Bitboard.E4_D5) / 2 * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_LONG_BISHOP];
			}
			if ((cb.pieces[BLACK][BISHOP] & Bitboard.BLACK_SQUARES) != 0) {
				// penalty for many pawns on same color as bishop
				score += EvalConstants.SAME_COLORED_BISHOP_PAWN[Long.bitCount(cb.pieces[BLACK][PAWN] & Bitboard.BLACK_SQUARES)];

				// bonus for attacking center squares
				score -= Long.bitCount(cb.attacks[BLACK][BISHOP] & Bitboard.D4_E5) / 2 * EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_LONG_BISHOP];
			}
		}

		// pieces supporting our pawns
		piece = (cb.pieces[WHITE][PAWN] << 8) & cb.friendlyPieces[WHITE];
		while (piece != 0) {
			score += EvalConstants.PAWN_BLOCKAGE[Long.numberOfTrailingZeros(piece) >>> 3];
			piece &= piece - 1;
		}
		piece = (cb.pieces[BLACK][PAWN] >>> 8) & cb.friendlyPieces[BLACK];
		while (piece != 0) {
			score -= EvalConstants.PAWN_BLOCKAGE[7 - Long.numberOfTrailingZeros(piece) / 8];
			piece &= piece - 1;
		}

		// knight outpost: protected by a pawn, cannot be attacked by enemy pawns
		piece = cb.pieces[WHITE][NIGHT] & cb.passedPawnsAndOutposts & cb.attacks[WHITE][PAWN];
		while (piece != 0) {
			score += EvalConstants.KNIGHT_OUTPOST[Long.numberOfTrailingZeros(piece) >>> 3];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT] & cb.passedPawnsAndOutposts & cb.attacks[BLACK][PAWN];
		while (piece != 0) {
			score -= EvalConstants.KNIGHT_OUTPOST[7 - Long.numberOfTrailingZeros(piece) / 8];
			piece &= piece - 1;
		}

		return score;
	}

	public static int calculatePawnShieldBonus(final ChessBoard cb) {

		int file;

		int whiteScore = 0;
		long piece = cb.pieces[WHITE][PAWN] & cb.kingArea[WHITE];
		while (piece != 0) {
			file = Long.numberOfTrailingZeros(piece) & 7;
			whiteScore += EvalConstants.SHIELD_BONUS[Math.min(7 - file, file)][Long.numberOfTrailingZeros(piece) >>> 3];
			piece &= ~Bitboard.FILES[file];
		}
		if (cb.pieces[BLACK][QUEEN] == 0) {
			whiteScore /= 2;
		}

		int blackScore = 0;
		piece = cb.pieces[BLACK][PAWN] & cb.kingArea[BLACK];
		while (piece != 0) {
			file = (63 - Long.numberOfLeadingZeros(piece)) & 7;
			blackScore += EvalConstants.SHIELD_BONUS[Math.min(7 - file, file)][7 - (63 - Long.numberOfLeadingZeros(piece)) / 8];
			piece &= ~Bitboard.FILES[file];
		}
		if (cb.pieces[WHITE][QUEEN] == 0) {
			blackScore /= 2;
		}

		return whiteScore - blackScore;
	}

	public static int calculateMobilityScoresAndSetAttackBoards(final ChessBoard cb) {

		// clear values
		cb.clearEvalAttacks();

		long moves;

		// white pawns
		cb.attacks[WHITE][PAWN] = Bitboard.getWhitePawnAttacks(cb.pieces[WHITE][PAWN] & ~cb.pinnedPieces);
		if ((cb.attacks[WHITE][PAWN] & cb.kingArea[BLACK]) != 0) {
			cb.kingAttackersFlag[WHITE] = SchroderUtil.FLAG_PAWN;
		}
		long pinned = cb.pieces[WHITE][PAWN] & cb.pinnedPieces;
		while (pinned != 0) {
			cb.attacks[WHITE][PAWN] |= StaticMoves.PAWN_ATTACKS[WHITE][Long.numberOfTrailingZeros(pinned)]
					& ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(pinned)][cb.kingIndex[WHITE]];
			pinned &= pinned - 1;
		}
		// black pawns
		cb.attacks[BLACK][PAWN] = Bitboard.getBlackPawnAttacks(cb.pieces[BLACK][PAWN] & ~cb.pinnedPieces);
		if ((cb.attacks[BLACK][PAWN] & cb.kingArea[WHITE]) != 0) {
			cb.kingAttackersFlag[BLACK] = SchroderUtil.FLAG_PAWN;
		}
		pinned = cb.pieces[BLACK][PAWN] & cb.pinnedPieces;
		while (pinned != 0) {
			cb.attacks[BLACK][PAWN] |= StaticMoves.PAWN_ATTACKS[BLACK][Long.numberOfTrailingZeros(pinned)]
					& ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(pinned)][cb.kingIndex[BLACK]];
			pinned &= pinned - 1;
		}

		int scoreMg = 0;
		int scoreEg = 0;
		for (int color = WHITE; color <= BLACK; color++) {

			int tempScoreMg = 0;
			int tempScoreEg = 0;

			final long kingArea = cb.kingArea[1 - color];
			final long safeMoves = ~cb.friendlyPieces[color] & ~cb.attacks[1 - color][PAWN];

			// knights
			long piece = cb.pieces[color][NIGHT] & ~cb.pinnedPieces;
			while (piece != 0) {
				moves = StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)];
				if ((moves & kingArea) != 0) {
					cb.kingAttackersFlag[color] |= SchroderUtil.FLAG_NIGHT;
				}
				cb.attacks[color][NIGHT] |= moves;
				tempScoreMg += EvalConstants.MOBILITY_KNIGHT[Long.bitCount(moves & safeMoves)];
				tempScoreEg += EvalConstants.MOBILITY_KNIGHT_EG[Long.bitCount(moves & safeMoves)];
				piece &= piece - 1;
			}

			// bishops
			piece = cb.pieces[color][BISHOP];
			while (piece != 0) {
				moves = MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces ^ cb.pieces[color][QUEEN]);
				if ((moves & kingArea) != 0) {
					cb.kingAttackersFlag[color] |= SchroderUtil.FLAG_BISHOP;
				}
				cb.attacks[color][BISHOP] |= moves;
				tempScoreMg += EvalConstants.MOBILITY_BISHOP[Long.bitCount(moves & safeMoves)];
				tempScoreEg += EvalConstants.MOBILITY_BISHOP_EG[Long.bitCount(moves & safeMoves)];
				piece &= piece - 1;
			}

			// rooks
			piece = cb.pieces[color][ROOK];
			while (piece != 0) {
				moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces ^ cb.pieces[color][ROOK] ^ cb.pieces[color][QUEEN]);
				if ((moves & kingArea) != 0) {
					cb.kingAttackersFlag[color] |= SchroderUtil.FLAG_ROOK;
				}
				cb.attacks[color][ROOK] |= moves;
				tempScoreMg += EvalConstants.MOBILITY_ROOK[Long.bitCount(moves & safeMoves)];
				tempScoreEg += EvalConstants.MOBILITY_ROOK_EG[Long.bitCount(moves & safeMoves)];
				piece &= piece - 1;
			}

			// queens
			piece = cb.pieces[color][QUEEN];
			while (piece != 0) {
				moves = MagicUtil.getQueenMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
				if ((moves & kingArea) != 0) {
					cb.kingAttackersFlag[color] |= SchroderUtil.FLAG_QUEEN;
				}
				cb.attacks[color][QUEEN] |= moves;
				tempScoreMg += EvalConstants.MOBILITY_QUEEN[Long.bitCount(moves & safeMoves)];
				tempScoreEg += EvalConstants.MOBILITY_QUEEN_EG[Long.bitCount(moves & safeMoves)];
				piece &= piece - 1;
			}

			cb.attacksWithoutKing[color] = cb.attacks[color][PAWN] | cb.attacks[color][NIGHT] | cb.attacks[color][BISHOP] | cb.attacks[color][ROOK]
					| cb.attacks[color][QUEEN];

			scoreMg += tempScoreMg * ChessConstants.COLOR_FACTOR[color];
			scoreEg += tempScoreEg * ChessConstants.COLOR_FACTOR[color];

		}

		// kings
		// TODO king-attacks with or without enemy attacks?
		moves = StaticMoves.KING_MOVES[cb.kingIndex[WHITE]] & ~StaticMoves.KING_MOVES[cb.kingIndex[BLACK]] & ~cb.attacksWithoutKing[BLACK];
		cb.attacks[WHITE][KING] = moves;
		scoreMg += EvalConstants.MOBILITY_KING[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
		scoreEg += EvalConstants.MOBILITY_KING_EG[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
		moves = StaticMoves.KING_MOVES[cb.kingIndex[BLACK]] & ~StaticMoves.KING_MOVES[cb.kingIndex[WHITE]] & ~cb.attacksWithoutKing[WHITE];
		cb.attacks[BLACK][KING] = moves;
		scoreMg -= EvalConstants.MOBILITY_KING[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
		scoreEg -= EvalConstants.MOBILITY_KING_EG[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];

		// set all attacks
		cb.attacksAll[WHITE] = cb.attacksWithoutKing[WHITE] | cb.attacks[WHITE][KING];
		cb.attacksAll[BLACK] = cb.attacksWithoutKing[BLACK] | cb.attacks[BLACK][KING];

		return score(scoreMg, scoreEg);
	}

	public static int calculatePositionScores(final ChessBoard cb) {

		int score = 0;
		for (int color = WHITE; color <= BLACK; color++) {
			for (int pieceType = PAWN; pieceType <= KING; pieceType++) {
				long piece = cb.pieces[color][pieceType];
				while (piece != 0) {
					score += EvalConstants.PSQT[pieceType][color][Long.numberOfTrailingZeros(piece)];
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
					score += EvalConstants.PSQT_EG[pieceType][color][Long.numberOfTrailingZeros(piece)];
					piece &= piece - 1;
				}
			}
		}
		return score;
	}

	public static int calculateMaterialScore(final ChessBoard cb) {
		return (Long.bitCount(cb.pieces[WHITE][PAWN]) - Long.bitCount(cb.pieces[BLACK][PAWN])) * EvalConstants.MATERIAL[PAWN]
				+ (Long.bitCount(cb.pieces[WHITE][NIGHT]) - Long.bitCount(cb.pieces[BLACK][NIGHT])) * EvalConstants.MATERIAL[NIGHT]
				+ (Long.bitCount(cb.pieces[WHITE][BISHOP]) - Long.bitCount(cb.pieces[BLACK][BISHOP])) * EvalConstants.MATERIAL[BISHOP]
				+ (Long.bitCount(cb.pieces[WHITE][ROOK]) - Long.bitCount(cb.pieces[BLACK][ROOK])) * EvalConstants.MATERIAL[ROOK]
				+ (Long.bitCount(cb.pieces[WHITE][QUEEN]) - Long.bitCount(cb.pieces[BLACK][QUEEN])) * EvalConstants.MATERIAL[QUEEN];
	}

}
