package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;
import static nl.s22k.chess.eval.EvalConstants.INDEX_BISHOP_DOUBLE;
import static nl.s22k.chess.eval.EvalConstants.INDEX_KNIGHT_OUTPOST;
import static nl.s22k.chess.eval.EvalConstants.INDEX_KNIGHT_OUTPOST_UNATTACKABLE;
import static nl.s22k.chess.eval.EvalConstants.INDEX_PAWN_DOUBLE;
import static nl.s22k.chess.eval.EvalConstants.INDEX_PAWN_ISOLATED;
import static nl.s22k.chess.eval.EvalConstants.INDEX_ROOK_FILE_OPEN;
import static nl.s22k.chess.eval.EvalConstants.INDEX_ROOK_FILE_SEMI_OPEN;
import static nl.s22k.chess.eval.EvalConstants.INDEX_ROOK_PRISON;

import java.util.Arrays;

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

	public static int calculateScore(ChessBoard cb) {

		if (Statistics.ENABLED) {
			Statistics.evalNodes++;
		}

		if (EngineConstants.ENABLE_EVAL_CACHE && !EngineConstants.TEST_PAWN_EVAL_CACHE) {
			if (EvalCache.hasScore(cb.zobristKey)) {
				return EvalCache.getScore(cb.zobristKey);
			}
		}

		int score = EngineConstants.ENABLE_INCREMENTAL_PSQT ? cb.psqtScore : calculatePositionScores(cb);
		score += calculateMaterialScores(cb) + calculateMobilityScores(cb) + calculateKingSafetyScores(cb) + calculateBonusses(cb) + calculatePenalties(cb)
				+ calculatePawnScores(cb) + calculatePassedPawnScores(cb);

		/* draw-by-material */
		if (score > 0) {
			if (cb.isDrawByMaterial(WHITE)) {
				if (Statistics.ENABLED) {
					Statistics.drawByMaterialCount++;
				}
				score = EvalConstants.SCORE_DRAW;
			}
		} else {
			if (cb.isDrawByMaterial(BLACK)) {
				if (Statistics.ENABLED) {
					Statistics.drawByMaterialCount++;
				}
				score = EvalConstants.SCORE_DRAW;
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

	public static void compareScores(ChessBoard cb1, ChessBoard cb2, int factor) {
		if (calculateBonusses(cb1) != calculateBonusses(cb2) * factor) {
			System.out.println("Unequal bonus: " + calculateBonusses(cb1) + " " + calculateBonusses(cb2) * factor);
		}
		if (calculateMobilityScores(cb1) != calculateMobilityScores(cb2) * factor) {
			System.out.println("Unequal mobility: " + calculateMobilityScores(cb1) + " " + calculateMobilityScores(cb2) * factor);
		}

		// depends on mobility which is stored in the chessboard instance
		int kingSafetyScore2 = calculateKingSafetyScores(cb2);
		calculateMobilityScores(cb1);
		if (kingSafetyScore2 != calculateKingSafetyScores(cb1) * factor) {
			System.out.println("Unequal king-safety: " + calculateKingSafetyScores(cb1) + " " + kingSafetyScore2 * factor);
		}
		if (calculateMaterialScores(cb1) != calculateMaterialScores(cb2) * factor) {
			System.out.println("Unequal material: " + calculateMaterialScores(cb1) + " " + calculateMaterialScores(cb2) * factor);
		}
		if (calculatePenalties(cb1) != calculatePenalties(cb2) * factor) {
			System.out.println("Unequal penalties: " + calculatePenalties(cb1) + " " + calculatePenalties(cb2) * factor);
		}
		if (calculatePositionScores(cb1) != calculatePositionScores(cb2) * factor) {
			System.out.println("Unequal position score: " + calculatePositionScores(cb1) + " " + calculatePositionScores(cb2) * factor);
		}

		// depends on the passer information which is stored in the chessboard instance
		int pawn1Score = calculatePawnScores(cb1) * factor;
		int passedPawn1Score = calculatePassedPawnScores(cb1) * factor;

		if (pawn1Score != calculatePawnScores(cb2)) {
			System.out.println("Unequal pawns: " + pawn1Score + " " + calculatePawnScores(cb2));
		}
		if (passedPawn1Score != calculatePassedPawnScores(cb2)) {
			System.out.println("Unequal passed-pawns: " + passedPawn1Score + " " + calculatePassedPawnScores(cb2));
		}
	}

	public static int calculatePassedPawnScores(final ChessBoard cb) {
		int score = 0;

		// white passed pawns
		int passedPawnFiles = cb.passerFiles[WHITE];
		int passerScore;
		int promotionDistance;
		int index;
		while (passedPawnFiles != 0) {
			index = 63 - Long.numberOfLeadingZeros(cb.pieces[WHITE][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(passedPawnFiles)]);

			passerScore = EvalConstants.PASSED_PAWN_SCORE[index / 8];
			if (!cb.isEndGame[WHITE]) {
				passerScore /= EvalConstants.PASSED_PAWN_MULTIPLIERS[0] / 10f;
			}

			// is pawn protected by other passed pawn?
			if ((cb.protectedPasserFiles[WHITE] & Long.lowestOneBit(passedPawnFiles)) != 0) {
				passerScore *= EvalConstants.PASSED_PAWN_MULTIPLIERS[1] / 10f;
			}

			// is piece blocked?
			if ((cb.allPieces & Util.POWER_LOOKUP[index + 8]) != 0) {
				passerScore /= EvalConstants.PASSED_PAWN_MULTIPLIERS[2] / 10f;
			}
			score += passerScore;

			// check if it cannot be stopped
			if (cb.hasOnlyPawns(BLACK)) {
				promotionDistance = 7 - index / 8;

				// check if it is my turn
				if (cb.colorToMove == BLACK) {
					promotionDistance++;
				}
				// check if own pieces are blocking the path
				if (63 - Long.numberOfLeadingZeros(cb.friendlyPieces[WHITE] & Bitboard.FILES[Long.numberOfTrailingZeros(passedPawnFiles)]) > index) {
					promotionDistance++;
				}

				// check if pawn can do 2-moves
				if (index / 8 == 1) {
					promotionDistance--;
				}

				// TODO maybe the enemy king can capture the pawn!!
				// check if own king is defending the promotion square (including square just below)
				if ((StaticMoves.KING_MOVES[cb.kingIndex[WHITE]] & ChessConstants.PASSED_PAWN_MASKS[WHITE][index] & Bitboard.RANK_78) != 0) {
					promotionDistance -= 2;
				}

				// check distance of enemy king to promotion square
				if (promotionDistance < Math.max(7 - cb.kingIndex[BLACK] / 8, Math.abs((index & 7) - (cb.kingIndex[BLACK] & 7)))) {
					score += 600;
				}
			}

			passedPawnFiles &= passedPawnFiles - 1;
		}

		passedPawnFiles = cb.passerFiles[BLACK];
		while (passedPawnFiles != 0) {
			index = Long.numberOfTrailingZeros(cb.pieces[BLACK][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(passedPawnFiles)]);

			passerScore = EvalConstants.PASSED_PAWN_SCORE[7 - index / 8];
			if (!cb.isEndGame[BLACK]) {
				passerScore /= EvalConstants.PASSED_PAWN_MULTIPLIERS[0] / 10f;
			}

			// is pawn protected by other passed pawn?
			if ((cb.protectedPasserFiles[BLACK] & Long.lowestOneBit(passedPawnFiles)) != 0) {
				passerScore *= EvalConstants.PASSED_PAWN_MULTIPLIERS[1] / 10f;
			}

			// is piece blocked?
			if ((cb.allPieces & Util.POWER_LOOKUP[index - 8]) != 0) {
				passerScore /= EvalConstants.PASSED_PAWN_MULTIPLIERS[2] / 10f;
			}
			score -= passerScore;

			// check if it cannot be stopped
			if (cb.hasOnlyPawns(WHITE)) {
				promotionDistance = index / 8;

				// check if it is my turn
				if (cb.colorToMove == WHITE) {
					promotionDistance++;
				}

				// check if own pieces are blocking the path
				if (Long.numberOfTrailingZeros(cb.friendlyPieces[BLACK] & Bitboard.FILES[Long.numberOfTrailingZeros(passedPawnFiles)]) < index) {
					promotionDistance++;
				}

				// check if pawn can do 2-moves
				if (index / 8 == 6) {
					promotionDistance--;
				}

				// check if own king is defending the promotion square (including square just below)
				if ((StaticMoves.KING_MOVES[cb.kingIndex[BLACK]] & ChessConstants.PASSED_PAWN_MASKS[BLACK][index] & Bitboard.RANK_12) != 0) {
					promotionDistance -= 2;
				}

				// check distance of enemy king to promotion square
				if (promotionDistance < Math.max(cb.kingIndex[WHITE] / 8, Math.abs((index & 7) - (cb.kingIndex[WHITE] & 7)))) {
					score -= 600;
				}
			}

			passedPawnFiles &= passedPawnFiles - 1;
		}

		return score;
	}

	public static int calculatePawnScores(final ChessBoard cb) {

		if (EngineConstants.ENABLE_PAWN_EVAL_CACHE && !EngineConstants.TEST_PAWN_EVAL_CACHE && !EngineConstants.TEST_EVAL_VALUES) {
			if (PawnEvalCache.hasScore(cb.pawnZobristKey)) {
				cb.passerFiles[WHITE] = PawnEvalCache.getPasserFiles(cb.pawnZobristKey, WHITE);
				cb.passerFiles[BLACK] = PawnEvalCache.getPasserFiles(cb.pawnZobristKey, BLACK);
				cb.protectedPasserFiles[WHITE] = PawnEvalCache.getProtectedPasserFiles(cb.pawnZobristKey, WHITE);
				cb.protectedPasserFiles[BLACK] = PawnEvalCache.getProtectedPasserFiles(cb.pawnZobristKey, BLACK);
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

		cb.passerFiles[WHITE] = 0;
		cb.passerFiles[BLACK] = 0;
		long whitePassers = 0;
		long blackPassers = 0;
		int index;

		// white
		long pawns = cb.pieces[WHITE][PAWN];
		while (pawns != 0) {
			index = Long.numberOfTrailingZeros(pawns);

			// isolated pawns
			if ((Bitboard.FILES_ADJACENT[index & 7] & cb.pieces[WHITE][PAWN]) == 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_PAWN_ISOLATED];
			}

			// set passed pawns
			if ((ChessConstants.PASSED_PAWN_MASKS[WHITE][index] & cb.pieces[BLACK][PAWN]) == 0) {
				cb.passerFiles[WHITE] |= Util.POWER_LOOKUP[index & 7];
				whitePassers |= Util.POWER_LOOKUP[index];
			}

			// pawn position score
			if (cb.isEndGame[WHITE]) {
				score += EvalConstants.PSQT_PAWN_ENDGAME[WHITE][index];
			} else {
				score += EvalConstants.PSQT_PAWN[WHITE][index];
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

			// set passed pawns
			if ((ChessConstants.PASSED_PAWN_MASKS[BLACK][index] & cb.pieces[WHITE][PAWN]) == 0) {
				cb.passerFiles[BLACK] |= Util.POWER_LOOKUP[index & 7];
				blackPassers |= Util.POWER_LOOKUP[index];
			}

			// pawn position score
			if (cb.isEndGame[BLACK]) {
				score += EvalConstants.PSQT_PAWN_ENDGAME[BLACK][index];
			} else {
				score += EvalConstants.PSQT_PAWN[BLACK][index];
			}

			pawns &= pawns - 1;
		}

		cb.protectedPasserFiles[WHITE] = 0;
		cb.protectedPasserFiles[BLACK] = 0;

		// set white protected passers
		pawns = whitePassers;
		while (pawns != 0) {
			if ((StaticMoves.PAWN_ALL_ATTACKS[BLACK][Long.numberOfTrailingZeros(pawns)] & whitePassers) != 0) {
				cb.protectedPasserFiles[WHITE] |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(pawns) & 7];
			}
			pawns &= pawns - 1;
		}

		// set black protected passers
		pawns = blackPassers;
		while (pawns != 0) {
			if ((StaticMoves.PAWN_ALL_ATTACKS[WHITE][Long.numberOfTrailingZeros(pawns)] & blackPassers) != 0) {
				cb.protectedPasserFiles[BLACK] |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(pawns) & 7];
			}
			pawns &= pawns - 1;
		}

		// pawn material
		if (cb.isEndGame[WHITE]) {
			score += Long.bitCount(cb.pieces[WHITE][PAWN]) * EvalConstants.MATERIAL_SCORES_ENDGAME[PAWN];
		} else {
			score += Long.bitCount(cb.pieces[WHITE][PAWN]) * EvalConstants.MATERIAL_SCORES[PAWN];
		}
		if (cb.isEndGame[BLACK]) {
			score -= Long.bitCount(cb.pieces[BLACK][PAWN]) * EvalConstants.MATERIAL_SCORES_ENDGAME[PAWN];
		} else {
			score -= Long.bitCount(cb.pieces[BLACK][PAWN]) * EvalConstants.MATERIAL_SCORES[PAWN];
		}

		// score += (Long.bitCount(cb.pieces[WHITE][PAWN]) - Long.bitCount(cb.pieces[BLACK][PAWN])) *
		// EvalConstants.MATERIAL_SCORES[PAWN];

		if (EngineConstants.TEST_PAWN_EVAL_CACHE) {
			if (PawnEvalCache.hasScore(cb.pawnZobristKey)) {
				int cachedScore = PawnEvalCache.getScore(cb.pawnZobristKey);
				if (cachedScore != score) {
					System.out.println(String.format("Cached pawn score != score: %s, %s", cachedScore, score));
				}
			}
		}

		PawnEvalCache.addValue(cb.pawnZobristKey, score, cb.passerFiles[WHITE], cb.passerFiles[BLACK], cb.protectedPasserFiles[WHITE],
				cb.protectedPasserFiles[BLACK]);

		return score;
	}

	public static int calculateBonusses(final ChessBoard cb) {
		int score = 0;

		// bonus for side to move
		score += ChessConstants.COLOR_FACTOR[cb.colorToMove] * 10;

		// double bishop bonus
		if (Long.bitCount(cb.pieces[WHITE][BISHOP]) == 2) {
			score += EvalConstants.INDIVIDUAL_SCORES[INDEX_BISHOP_DOUBLE];
		}
		if (Long.bitCount(cb.pieces[BLACK][BISHOP]) == 2) {
			score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_BISHOP_DOUBLE];
		}

		// bonus for small king-king distance in KKR and KKQ endgame
		// TODO should this be 7 - instead of 8 -??
		if (Long.bitCount(cb.allPieces) == 3) {
			if (cb.pieces[WHITE][ROOK] != 0 || cb.pieces[WHITE][QUEEN] != 0) {
				score += EvalConstants.KKR_KKQ_KING_DISTANCE_SCORE[Math.max(Math.abs(cb.kingIndex[WHITE] / 8 - cb.kingIndex[BLACK] / 8),
						Math.abs((cb.kingIndex[WHITE] & 7) - (cb.kingIndex[BLACK] & 7)))];
			} else if (cb.pieces[BLACK][ROOK] != 0 || cb.pieces[BLACK][QUEEN] != 0) {
				score -= EvalConstants.KKR_KKQ_KING_DISTANCE_SCORE[Math.max(Math.abs(cb.kingIndex[WHITE] / 8 - cb.kingIndex[BLACK] / 8),
						Math.abs((cb.kingIndex[WHITE] & 7) - (cb.kingIndex[BLACK] & 7)))];
			}
		}

		// bonus for rook on open-file (no pawns) and semi-open-file (no friendly pawns)
		long piece = cb.pieces[WHITE][ROOK];
		while (piece != 0) {
			if ((cb.pieces[WHITE][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
				if ((cb.pieces[BLACK][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
					score += EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_FILE_OPEN];
				} else {
					score += EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_FILE_SEMI_OPEN];
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			if ((cb.pieces[BLACK][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
				if ((cb.pieces[WHITE][PAWN] & Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7]) == 0) {
					score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_FILE_OPEN];
				} else {
					score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_FILE_SEMI_OPEN];
				}
			}
			piece &= piece - 1;
		}

		// knight outpost: 4,5,6,7th rank, protected by a pawn, cannot be attacked by enemy pawns
		piece = cb.pieces[WHITE][NIGHT];
		while (piece != 0) {
			if ((Bitboard.RANK_4567 & Long.lowestOneBit(piece)) != 0
					&& (StaticMoves.PAWN_ALL_ATTACKS[BLACK][Long.numberOfTrailingZeros(piece)] & cb.pieces[WHITE][PAWN]) != 0) {
				score += EvalConstants.INDIVIDUAL_SCORES[INDEX_KNIGHT_OUTPOST];
				if ((ChessConstants.MASK_ADJACENT_FILE_UP[Long.numberOfTrailingZeros(piece)] & cb.pieces[BLACK][PAWN]) == 0) {
					score += EvalConstants.INDIVIDUAL_SCORES[INDEX_KNIGHT_OUTPOST_UNATTACKABLE];
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT];
		while (piece != 0) {
			if ((Bitboard.RANK_2345 & Long.lowestOneBit(piece)) != 0
					&& (StaticMoves.PAWN_ALL_ATTACKS[WHITE][Long.numberOfTrailingZeros(piece)] & cb.pieces[BLACK][PAWN]) != 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_KNIGHT_OUTPOST];
				if ((ChessConstants.MASK_ADJACENT_FILE_DOWN[Long.numberOfTrailingZeros(piece)] & cb.pieces[WHITE][PAWN]) == 0) {
					score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_KNIGHT_OUTPOST_UNATTACKABLE];
				}
			}
			piece &= piece - 1;
		}

		// pawn-shield
		if (!cb.isEndGame[WHITE]) {
			if ((cb.pieces[WHITE][KING] & Bitboard.KING_SIDE) != 0) {
				// king-side
				piece = cb.pieces[WHITE][PAWN] & ChessConstants.KING_PAWN_SHIELD_KINGSIDE_MASK[WHITE][cb.kingIndex[WHITE] / 8];
				// lower endgame transition effect
				score -= 100;
			} else if ((cb.pieces[WHITE][KING] & Bitboard.QUEEN_SIDE) != 0) {
				// queen-side
				piece = cb.pieces[WHITE][PAWN] & ChessConstants.KING_PAWN_SHIELD_QUEENSIDE_MASK[WHITE][cb.kingIndex[WHITE] / 8];
				// lower endgame transition effect
				score -= 100;
			}
			while (piece != 0) {
				score += EvalConstants.PAWN_SHIELD_BONUS[Long.numberOfTrailingZeros(piece) / 8];
				piece &= ~Bitboard.FILES[Long.numberOfTrailingZeros(piece) & 7];
			}
		}
		if (!cb.isEndGame[BLACK]) {
			if ((cb.pieces[BLACK][KING] & Bitboard.KING_SIDE) != 0) {
				// king-side
				piece = cb.pieces[BLACK][PAWN] & ChessConstants.KING_PAWN_SHIELD_KINGSIDE_MASK[BLACK][cb.kingIndex[BLACK] / 8];
				// lower endgame transition effect
				score += 100;
			} else if ((cb.pieces[BLACK][KING] & Bitboard.QUEEN_SIDE) != 0) {
				// queen-side
				piece = cb.pieces[BLACK][PAWN] & ChessConstants.KING_PAWN_SHIELD_QUEENSIDE_MASK[BLACK][cb.kingIndex[BLACK] / 8];
				// lower endgame transition effect
				score += 100;
			}
			while (piece != 0) {
				score -= EvalConstants.PAWN_SHIELD_BONUS[7 - (63 - Long.numberOfLeadingZeros(piece)) / 8];
				piece &= ~Bitboard.FILES[(63 - Long.numberOfLeadingZeros(piece)) & 7];
			}
		}

		// pawn-storm
		if (!cb.isEndGame[BLACK]) {
			if ((cb.pieces[BLACK][KING] & Bitboard.KING_SIDE) != 0) {
				// king-side
				piece = cb.pieces[WHITE][PAWN] & Bitboard.KING_SIDE;

			} else if ((cb.pieces[BLACK][KING] & Bitboard.QUEEN_SIDE) != 0) {
				// queen-side
				piece = cb.pieces[WHITE][PAWN] & Bitboard.QUEEN_SIDE;
			}
			while (piece != 0) {
				score += EvalConstants.PAWN_STORM_BONUS[Long.numberOfTrailingZeros(piece) / 8];
				piece &= piece - 1;
			}
		}
		if (!cb.isEndGame[WHITE]) {
			if ((cb.pieces[WHITE][KING] & Bitboard.KING_SIDE) != 0) {
				// king-side
				piece = cb.pieces[BLACK][PAWN] & Bitboard.KING_SIDE;

			} else if ((cb.pieces[WHITE][KING] & Bitboard.QUEEN_SIDE) != 0) {
				// queen-side
				piece = cb.pieces[BLACK][PAWN] & Bitboard.QUEEN_SIDE;
			}
			while (piece != 0) {
				score -= EvalConstants.PAWN_STORM_BONUS[7 - Long.numberOfTrailingZeros(piece) / 8];
				piece &= piece - 1;
			}
		}

		return score;
	}

	public static int calculatePenalties(final ChessBoard cb) {
		int score = 0;

		if (cb.moveCounter < 12) {
			// penalty for moving queen in opening
			if ((Bitboard.D1_E1_D2_E2 & cb.pieces[WHITE][QUEEN]) == 0) {
				score -= 10;
			}
			if ((Bitboard.D7_E7_D8_E8 & cb.pieces[BLACK][QUEEN]) == 0) {
				score += 10;
			}

			// penalty for moving king in opening and not castling
			// TODO only apply if could castle?
			// TODO is this necessary?
			if (!EngineConstants.TEST_EVAL_VALUES) {
				if ((Bitboard.D1_F1 & cb.pieces[WHITE][KING]) != 0) {
					score -= 20;
				}
				if ((Bitboard.D8_F8 & cb.pieces[BLACK][KING]) != 0) {
					score += 20;
				}
			}
		}

		// penalty for having pinned-pieces
		long pieces = cb.pinnedPieces[WHITE];
		while (pieces != 0) {
			score -= EvalConstants.PINNED_PIECE_SCORES[cb.pieceIndexes[Long.numberOfTrailingZeros(pieces)]];
			pieces &= pieces - 1;
		}
		pieces = cb.pinnedPieces[BLACK];
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

		// bad-bishop
		pieces = cb.pieces[WHITE][BISHOP];
		while (pieces != 0) {
			if ((cb.pieces[WHITE][PAWN] & EvalConstants.BAD_BISHOP[Long.numberOfTrailingZeros(pieces)]) != 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BAD_BISHOP];
			}
			pieces &= pieces - 1;
		}
		pieces = cb.pieces[BLACK][BISHOP];
		while (pieces != 0) {
			if ((cb.pieces[BLACK][PAWN] & EvalConstants.BAD_BISHOP[Long.numberOfTrailingZeros(pieces)]) != 0) {
				score += EvalConstants.INDIVIDUAL_SCORES[EvalConstants.INDEX_BAD_BISHOP];
			}
			pieces &= pieces - 1;
		}

		// rook-prison
		if ((cb.pieces[WHITE][ROOK] & Bitboard.A1_B1_A2_B2) != 0 && (cb.pieces[WHITE][KING] & Bitboard.B1_C1) != 0) {
			score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_PRISON];
		} else if ((cb.pieces[WHITE][ROOK] & Bitboard.G1_H1_G2_H2) != 0 && (cb.pieces[WHITE][KING] & Bitboard.F1_G1) != 0) {
			score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_PRISON];
		}
		if ((cb.pieces[BLACK][ROOK] & Bitboard.A7_B7_A8_B8) != 0 && (cb.pieces[BLACK][KING] & Bitboard.B8_C8) != 0) {
			score += EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_PRISON];
		} else if ((cb.pieces[BLACK][ROOK] & Bitboard.G7_H7_G8_H8) != 0 && (cb.pieces[BLACK][KING] & Bitboard.F8_G8) != 0) {
			score += EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_PRISON];
		}

		// bishop-prison
		if (!cb.isEndGame[WHITE]) {
			if ((cb.pieces[WHITE][BISHOP] & Bitboard.A7) != 0 && (cb.pieces[BLACK][PAWN] & Bitboard.B6) != 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_PRISON];
			}
			if ((cb.pieces[WHITE][BISHOP] & Bitboard.H7) != 0 && (cb.pieces[BLACK][PAWN] & Bitboard.G6) != 0) {
				score -= EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_PRISON];
			}
		}
		if (!cb.isEndGame[BLACK]) {
			if ((cb.pieces[BLACK][BISHOP] & Bitboard.A2) != 0 && (cb.pieces[WHITE][PAWN] & Bitboard.B3) != 0) {
				score += EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_PRISON];
			}
			if ((cb.pieces[BLACK][BISHOP] & Bitboard.H2) != 0 && (cb.pieces[WHITE][PAWN] & Bitboard.G3) != 0) {
				score += EvalConstants.INDIVIDUAL_SCORES[INDEX_ROOK_PRISON];
			}
		}

		return score;
	}

	public static int calculateMobilityScores(final ChessBoard cb) {

		// TODO disable mobility if piece is pinned?
		// TODO penalty for pieces that are attacked by a pawn?

		if (!EngineConstants.ENABLE_EVAL_MOBILITY) {
			return 0;
		}

		boolean queenInGame = (cb.pieces[WHITE][QUEEN] | cb.pieces[BLACK][QUEEN]) != 0;
		long whiteKingArea = 0;
		long blackKingArea = 0;

		if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE && queenInGame) {
			Arrays.fill(cb.attackBoards[WHITE], 0);
			Arrays.fill(cb.attackBoards[BLACK], 0);
			whiteKingArea = ChessConstants.KING_SAFETY_BEHIND[WHITE][cb.kingIndex[WHITE]] | ChessConstants.KING_SAFETY_NEXT[cb.kingIndex[WHITE]]
					| ChessConstants.KING_SAFETY_FRONT[WHITE][cb.kingIndex[WHITE]] | ChessConstants.KING_SAFETY_FRONT_FURTHER[WHITE][cb.kingIndex[WHITE]];
			blackKingArea = ChessConstants.KING_SAFETY_BEHIND[BLACK][cb.kingIndex[BLACK]] | ChessConstants.KING_SAFETY_NEXT[cb.kingIndex[BLACK]]
					| ChessConstants.KING_SAFETY_FRONT[BLACK][cb.kingIndex[BLACK]] | ChessConstants.KING_SAFETY_FRONT_FURTHER[BLACK][cb.kingIndex[BLACK]];
		}

		int score = 0;
		long moves;

		if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE && queenInGame) {
			moves = (cb.pieces[WHITE][PAWN] << 9 & Bitboard.NOT_FILE_H | cb.pieces[WHITE][PAWN] << 7 & Bitboard.NOT_FILE_A) & blackKingArea;
			while (moves != 0) {
				cb.attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_PAWN;
				moves &= moves - 1;
			}
			moves = (cb.pieces[BLACK][PAWN] >>> 9 & Bitboard.NOT_FILE_A | cb.pieces[BLACK][PAWN] >>> 7 & Bitboard.NOT_FILE_H) & whiteKingArea;
			while (moves != 0) {
				cb.attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_PAWN;
				moves &= moves - 1;
			}
		}

		// knights
		long piece = cb.pieces[WHITE][NIGHT];
		while (piece != 0) {
			moves = StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)];
			score += EvalConstants.MOBILITY_KNIGHT[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE && queenInGame) {
				moves &= blackKingArea;
				while (moves != 0) {
					cb.attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_NIGHT_BISHOP;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT];
		while (piece != 0) {
			moves = StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)];
			score -= EvalConstants.MOBILITY_KNIGHT[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE && queenInGame) {
				moves &= whiteKingArea;
				while (moves != 0) {
					cb.attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_NIGHT_BISHOP;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}

		// bishops
		piece = cb.pieces[WHITE][BISHOP];
		while (piece != 0) {
			moves = MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score += EvalConstants.MOBILITY_BISHOP[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE && queenInGame) {
				moves &= blackKingArea;
				while (moves != 0) {
					cb.attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_NIGHT_BISHOP;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][BISHOP];
		while (piece != 0) {
			moves = MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score -= EvalConstants.MOBILITY_BISHOP[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE && queenInGame) {
				moves &= whiteKingArea;
				while (moves != 0) {
					cb.attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_NIGHT_BISHOP;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}

		// rooks
		piece = cb.pieces[WHITE][ROOK];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score += EvalConstants.MOBILITY_ROOK[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE && queenInGame) {
				moves &= blackKingArea;
				while (moves != 0) {
					cb.attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_ROOK;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score -= EvalConstants.MOBILITY_ROOK[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE && queenInGame) {
				moves &= whiteKingArea;
				while (moves != 0) {
					cb.attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_ROOK;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}

		// queens
		piece = cb.pieces[WHITE][QUEEN];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces)
					| MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score += EvalConstants.MOBILITY_QUEEN[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				moves &= blackKingArea;
				while (moves != 0) {
					cb.attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_QUEEN;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][QUEEN];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces)
					| MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score -= EvalConstants.MOBILITY_QUEEN[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				moves &= whiteKingArea;
				while (moves != 0) {
					cb.attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] |= SchroderUtil.MASK_QUEEN;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}

		return score;
	}

	public static int calculateKingSafetyScores(final ChessBoard cb) {

		// TODO does not see that 2 pieces of the same type are attacking the same position (chance is small...)

		if (!EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
			return 0;
		}

		int score = 0;

		long nearbyPosition;
		int counter;
		int flag;

		for (int kingColor = WHITE; kingColor <= BLACK; kingColor++) {
			final int enemyColor = 1 - kingColor;

			// TODO skip if queen is only major piece?
			if (cb.pieces[enemyColor][QUEEN] != 0) {
				// TODO counter = 0 if black has only 2 minor pieces
				counter = EvalConstants.KING_SAFETY_COUNTER_RANKS[(7 * kingColor) + ChessConstants.COLOR_FACTOR[kingColor] * cb.kingIndex[kingColor] / 8];
				flag = 0;

				// front-further
				nearbyPosition = ChessConstants.KING_SAFETY_FRONT_FURTHER[kingColor][cb.kingIndex[kingColor]];
				while (nearbyPosition != 0) {
					flag |= cb.attackBoards[enemyColor][Long.numberOfTrailingZeros(nearbyPosition)];
					nearbyPosition &= nearbyPosition - 1;
				}
				// front
				nearbyPosition = ChessConstants.KING_SAFETY_FRONT[kingColor][cb.kingIndex[kingColor]];
				while (nearbyPosition != 0) {
					if (cb.attackBoards[enemyColor][Long.numberOfTrailingZeros(nearbyPosition)] != 0) {
						counter += 2;
						flag |= cb.attackBoards[enemyColor][Long.numberOfTrailingZeros(nearbyPosition)];
						if ((cb.friendlyPieces[kingColor] & Long.lowestOneBit(nearbyPosition)) == 0) {
							// no friendlyPiece nearby
							counter++;
						}
					}
					nearbyPosition &= nearbyPosition - 1;
				}
				// next and behind
				nearbyPosition = ChessConstants.KING_SAFETY_BEHIND[kingColor][cb.kingIndex[kingColor]]
						| ChessConstants.KING_SAFETY_NEXT[cb.kingIndex[kingColor]];
				while (nearbyPosition != 0) {
					if (cb.attackBoards[enemyColor][Long.numberOfTrailingZeros(nearbyPosition)] != 0) {
						counter += 2;
						flag |= cb.attackBoards[enemyColor][Long.numberOfTrailingZeros(nearbyPosition)];
					}
					nearbyPosition &= nearbyPosition - 1;
				}

				score += ChessConstants.COLOR_FACTOR[enemyColor]
						* EvalConstants.KING_SAFETY_SCORES[counter + EvalConstants.KING_SAFETY_ATTACK_PATTERN_COUNTER[flag]];
			}
		}

		return score;
	}

	public static int calculatePositionScores(final ChessBoard cb) {

		int score = 0;

		// knights
		long piece = cb.pieces[WHITE][NIGHT];
		while (piece != 0) {
			score += EvalConstants.PSQT_KNIGHT[WHITE][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT];
		while (piece != 0) {
			score += EvalConstants.PSQT_KNIGHT[BLACK][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}

		// bishops
		piece = cb.pieces[WHITE][BISHOP];
		while (piece != 0) {
			score += EvalConstants.PSQT_BISHOP[WHITE][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][BISHOP];
		while (piece != 0) {
			score += EvalConstants.PSQT_BISHOP[BLACK][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}

		// rooks
		piece = cb.pieces[WHITE][ROOK];
		while (piece != 0) {
			score += EvalConstants.PSQT_ROOK[WHITE][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			score += EvalConstants.PSQT_ROOK[BLACK][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}

		// king
		if (cb.isEndGame[WHITE]) {
			score += EvalConstants.PSQT_KING_ENDGAME[WHITE][cb.kingIndex[WHITE]];
		} else {
			score += EvalConstants.PSQT_KING[WHITE][cb.kingIndex[WHITE]];
		}
		if (cb.isEndGame[BLACK]) {
			score += EvalConstants.PSQT_KING_ENDGAME[BLACK][cb.kingIndex[BLACK]];
		} else {
			score += EvalConstants.PSQT_KING[BLACK][cb.kingIndex[BLACK]];
		}

		return score;
	}

	public static int calculateMaterialScores(final ChessBoard cb) {
		// @formatter:off
		return 	(Long.bitCount(cb.pieces[WHITE][NIGHT])		- Long.bitCount(cb.pieces[BLACK][NIGHT]))	* EvalConstants.MATERIAL_SCORES[NIGHT] + 
				(Long.bitCount(cb.pieces[WHITE][BISHOP])	- Long.bitCount(cb.pieces[BLACK][BISHOP]))	* EvalConstants.MATERIAL_SCORES[BISHOP] + 
				(Long.bitCount(cb.pieces[WHITE][ROOK])		- Long.bitCount(cb.pieces[BLACK][ROOK]))	* EvalConstants.MATERIAL_SCORES[ROOK] + 
				(Long.bitCount(cb.pieces[WHITE][QUEEN]) 	- Long.bitCount(cb.pieces[BLACK][QUEEN]))	* EvalConstants.MATERIAL_SCORES[QUEEN];
		// @formatter:on
	}

}
