package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import java.util.Arrays;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public class EvalUtil {

	// TODO tropism?

	// TODO fianchetto
	// TODO use constants
	// TODO 50-move rule

	private static final int KING_MOVING_OPENINGGAME = 20;
	private static final int KING_PINNED_PIECES = 15;
	private static final int KING_IN_CHECK = 10;
	private static final int KING_DEFENSE_HOLE = 40;

	private static final int QUEEN_MOVING_OPENINGGAME = 10;

	private static final int ROOK_PRISON = 40;
	private static final int ROOK_FILE_SEMI_OPEN = 15;
	private static final int ROOK_FILE_OPEN = 20;
	private static final int BISHOP_DOUBLE = 50;

	private static final int KNIGHT_OUTPOST_UNATTACKABLE = 10;
	private static final int KNIGHT_OUTPOST = 15;

	private static final int PAWN_PASSED = 20;
	private static final int PAWN_DOUBLE = 10;
	private static final int PAWN_ISOLATED = 15;

	// borrowed from Ed Schroder
	// k,q,r,bn,p,#
	private static int[][] attackBoards = new int[2][64];

	private static final int KING_SAFETY_ONLY_KING_ATTACK = 0x81;

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
		score += calculateMaterialScores(cb) + calculateMobilityWithKingDefenseScores(cb) + calculateKingSafetyScores(cb) + calculateBonusses(cb)
				+ calculatePenalties(cb) + calculatePawnScores(cb) + calculatePassedPawnScores(cb);

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

		return score;
	}

	public static int calculatePassedPawnScores(final ChessBoard cb) {
		int score = 0;

		// white passed pawns
		int passedPawnFiles = PawnEvalCache.getPasserFiles(cb.pawnZobristKey, WHITE);
		int passerScore;
		int promotionDistance;
		int index;
		while (passedPawnFiles != 0) {
			index = 63 - Long.numberOfLeadingZeros(cb.pieces[WHITE][PAWN] & ChessConstants.MASKS_FILE[Long.numberOfTrailingZeros(passedPawnFiles)]);

			// TODO check distance to enemy king?

			passerScore = EvalConstants.PASSED_PAWN_ENDGAME_SCORE[index / 8];
			if (!cb.isEndGame[WHITE]) {
				passerScore /= 2;
			}

			// is pawn protected by other passed pawn?
			if ((PawnEvalCache.getProtectedPasserFiles(cb.pawnZobristKey, WHITE) & Util.POWER_LOOKUP[Long.numberOfTrailingZeros(passedPawnFiles)]) != 0) {
				passerScore *= 1.5;
			}

			// is piece blocked?
			if ((cb.allPieces & Util.POWER_LOOKUP[index + 8]) != 0) {
				passerScore /= 2;
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
				if (63 - Long.numberOfLeadingZeros(cb.friendlyPieces[WHITE] & ChessConstants.MASKS_FILE[Long.numberOfTrailingZeros(passedPawnFiles)]) > index) {
					promotionDistance++;
				}

				// check if pawn can do 2-moves
				if (index / 8 == 1) {
					promotionDistance--;
				}

				// check distance of enemy king to promotion square
				if (promotionDistance < Math.max(7 - cb.kingIndex[BLACK] / 8, Math.abs((index & 7) - (cb.kingIndex[BLACK] & 7)))) {
					score += 600;
				}
			}

			passedPawnFiles &= passedPawnFiles - 1;
		}

		passedPawnFiles = PawnEvalCache.getPasserFiles(cb.pawnZobristKey, BLACK);
		while (passedPawnFiles != 0) {
			index = Long.numberOfTrailingZeros(cb.pieces[BLACK][PAWN] & ChessConstants.MASKS_FILE[Long.numberOfTrailingZeros(passedPawnFiles)]);

			passerScore = EvalConstants.PASSED_PAWN_ENDGAME_SCORE[7 - index / 8];
			if (!cb.isEndGame[BLACK]) {
				passerScore /= 2;
			}

			// is pawn protected by other passed pawn?
			if ((PawnEvalCache.getProtectedPasserFiles(cb.pawnZobristKey, BLACK) & Util.POWER_LOOKUP[Long.numberOfTrailingZeros(passedPawnFiles)]) != 0) {
				passerScore *= 1.5;
			}

			// is piece blocked?
			if ((cb.allPieces & Util.POWER_LOOKUP[index - 8]) != 0) {
				passerScore /= 2;
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
				if (Long.numberOfTrailingZeros(cb.friendlyPieces[BLACK] & ChessConstants.MASKS_FILE[Long.numberOfTrailingZeros(passedPawnFiles)]) < index) {
					promotionDistance++;
				}

				// check if pawn can do 2-moves
				if (index / 8 == 6) {
					promotionDistance--;
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

		if (EngineConstants.ENABLE_PAWN_EVAL_CACHE && !EngineConstants.TEST_PAWN_EVAL_CACHE) {
			if (PawnEvalCache.hasScore(cb.pawnZobristKey)) {
				return PawnEvalCache.getScore(cb.pawnZobristKey);
			}
		}

		int score = 0;

		// penalty for hole in king-pawn-defense
		if (!cb.isEndGame[WHITE]) {
			for (int i = 0; i < 3; i++) {
				if ((EvalConstants.KING_PAWN_HOLE[WHITE][EvalConstants.getKingPositionIndex(WHITE, cb.kingIndex[WHITE])][i] & cb.pieces[WHITE][PAWN]) == 0) {
					score -= 40;
				}
			}
		}
		if (!cb.isEndGame[BLACK]) {
			for (int i = 0; i < 3; i++) {
				if ((EvalConstants.KING_PAWN_HOLE[BLACK][EvalConstants.getKingPositionIndex(BLACK, cb.kingIndex[BLACK])][i] & cb.pieces[BLACK][PAWN]) == 0) {
					score += 40;
				}
			}
		}

		// penalty for doubled pawns
		for (int i = 0; i < 8; i++) {
			if (Long.bitCount(cb.pieces[WHITE][PAWN] & ChessConstants.MASKS_FILE[i]) > 1) {
				score -= 10;
			}
			if (Long.bitCount(cb.pieces[BLACK][PAWN] & ChessConstants.MASKS_FILE[i]) > 1) {
				score += 10;
			}
		}

		int whitePasserFiles = 0;
		int blackPasserFiles = 0;
		long whitePassers = 0;
		long blackPassedFiles = 0;

		// white
		long pawns = cb.pieces[WHITE][PAWN];
		while (pawns != 0) {
			// isolated pawns
			if ((ChessConstants.MASK_ADJACENT_FILE[Long.numberOfTrailingZeros(pawns) & 7] & cb.pieces[WHITE][PAWN]) == 0) {
				score -= 15;
			}

			// set passed pawns
			if ((EvalConstants.PASSED_PAWN_MASKS[WHITE][Long.numberOfTrailingZeros(pawns)] & cb.pieces[BLACK][PAWN]) == 0) {
				whitePasserFiles |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(pawns) & 7];
				whitePassers |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(pawns)];
			}

			// pawn position score
			if (cb.isEndGame[WHITE]) {
				score += EvalConstants.PAWN_POSITION_SCORES_ENDGAME[WHITE][Long.numberOfTrailingZeros(pawns)];
			} else {
				score += EvalConstants.PAWN_POSITION_SCORES[EvalConstants.KING_PAWN_INDEX[WHITE][cb.kingIndex[WHITE]]][WHITE][Long
						.numberOfTrailingZeros(pawns)];
			}

			pawns &= pawns - 1;
		}

		// black
		pawns = cb.pieces[BLACK][PAWN];
		while (pawns != 0) {
			// isolated pawns
			if ((ChessConstants.MASK_ADJACENT_FILE[Long.numberOfTrailingZeros(pawns) & 7] & cb.pieces[BLACK][PAWN]) == 0) {
				score += 15;
			}

			// set passed pawns
			if ((EvalConstants.PASSED_PAWN_MASKS[BLACK][Long.numberOfTrailingZeros(pawns)] & cb.pieces[WHITE][PAWN]) == 0) {
				blackPasserFiles |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(pawns) & 7];
				blackPassedFiles |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(pawns)];
			}

			// pawn position score
			if (cb.isEndGame[BLACK]) {
				score -= EvalConstants.PAWN_POSITION_SCORES_ENDGAME[BLACK][Long.numberOfTrailingZeros(pawns)];
			} else {
				score -= EvalConstants.PAWN_POSITION_SCORES[EvalConstants.KING_PAWN_INDEX[BLACK][cb.kingIndex[BLACK]]][BLACK][Long
						.numberOfTrailingZeros(pawns)];
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

		int whiteProtectedPasserFiles = 0;
		int blackProtectedPasserFiles = 0;

		// set white protected passers
		pawns = whitePassers;
		while (pawns != 0) {
			if ((StaticMoves.PAWN_ALL_ATTACKS[BLACK][Long.numberOfTrailingZeros(pawns)] & whitePassers) != 0) {
				whiteProtectedPasserFiles |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(pawns) & 7];
			}
			pawns &= pawns - 1;
		}

		// set black protected passers
		pawns = blackPassedFiles;
		while (pawns != 0) {
			if ((StaticMoves.PAWN_ALL_ATTACKS[WHITE][Long.numberOfTrailingZeros(pawns)] & blackPassedFiles) != 0) {
				blackProtectedPasserFiles |= Util.POWER_LOOKUP[Long.numberOfTrailingZeros(pawns) & 7];
			}
			pawns &= pawns - 1;
		}

		PawnEvalCache.addValue(cb.pawnZobristKey, score, whitePasserFiles, blackPasserFiles, whiteProtectedPasserFiles, blackProtectedPasserFiles);

		return score;
	}

	public static int calculateBonusses(final ChessBoard cb) {
		int score = 0;

		// double bishop bonus
		if (Long.bitCount(cb.pieces[WHITE][BISHOP]) == 2) {
			score += 40;
		}
		if (Long.bitCount(cb.pieces[BLACK][BISHOP]) == 2) {
			score -= 40;
		}

		// bonus for small king-king distance in KKR and KKQ endgame
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
			if ((cb.pieces[WHITE][PAWN] & ChessConstants.MASKS_FILE[Long.numberOfTrailingZeros(piece) % 8]) == 0) {
				if ((cb.pieces[BLACK][PAWN] & ChessConstants.MASKS_FILE[Long.numberOfTrailingZeros(piece) % 8]) == 0) {
					score += 20;
				} else {
					score += 15;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			if ((cb.pieces[BLACK][PAWN] & ChessConstants.MASKS_FILE[Long.numberOfTrailingZeros(piece) % 8]) == 0) {
				if ((cb.pieces[WHITE][PAWN] & ChessConstants.MASKS_FILE[Long.numberOfTrailingZeros(piece) % 8]) == 0) {
					score -= 20;
				} else {
					score -= 15;
				}
			}
			piece &= piece - 1;
		}

		// knight outpost: 4,5,6,7th rank, protected by a pawn, cannot be attacked by enemy pawns
		piece = cb.pieces[WHITE][NIGHT];
		while (piece != 0) {
			if ((ChessConstants.MASK_RANK_4567 & Util.POWER_LOOKUP[Long.numberOfTrailingZeros(piece)]) != 0
					&& (StaticMoves.PAWN_ALL_ATTACKS[BLACK][Long.numberOfTrailingZeros(piece)] & cb.pieces[WHITE][PAWN]) != 0) {
				score += 15;
				if ((ChessConstants.MASK_ADJACENT_FILE_UP[Long.numberOfTrailingZeros(piece)] & cb.pieces[BLACK][PAWN]) == 0) {
					score += 10;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT];
		while (piece != 0) {
			if ((ChessConstants.MASK_RANK_2345 & Util.POWER_LOOKUP[Long.numberOfTrailingZeros(piece)]) != 0
					&& (StaticMoves.PAWN_ALL_ATTACKS[WHITE][Long.numberOfTrailingZeros(piece)] & cb.pieces[BLACK][PAWN]) != 0) {
				score -= 15;
				if ((ChessConstants.MASK_ADJACENT_FILE_DOWN[Long.numberOfTrailingZeros(piece)] & cb.pieces[WHITE][PAWN]) == 0) {
					score -= 10;
				}
			}
			piece &= piece - 1;
		}

		return score;
	}

	public static int calculatePenalties(final ChessBoard cb) {
		int score = 0;

		// penalty for being in check
		if (cb.checkingPieces != 0) {
			score -= cb.colorFactor * 10;
		}

		if (cb.moveCounter < 12) {
			// penalty for moving queen in opening
			if ((0x1818 & cb.pieces[WHITE][QUEEN]) == 0) {
				score -= 10;
			}
			if ((0x1818000000000000L & cb.pieces[BLACK][QUEEN]) == 0) {
				score += 10;
			}

			// penalty for moving king in opening and not castling
			// TODO only apply if could castle?
			// TODO is this necessary?
			if ((0xeb & cb.pieces[WHITE][KING]) == 0) {
				score -= 20;
			}
			if ((0xeb00000000000000L & cb.pieces[BLACK][KING]) == 0) {
				score += 20;
			}
		}

		// penalty for having pinned-pieces
		score -= Long.bitCount(cb.pinnedPieces[WHITE]) * 15;
		score += Long.bitCount(cb.pinnedPieces[BLACK]) * 15;

		// rook-prison
		if ((cb.pieces[WHITE][ROOK] & 0xc0c0) != 0 && (cb.pieces[WHITE][KING] & 0x60) != 0) {
			score -= 40;
		} else if ((cb.pieces[WHITE][ROOK] & 0x303) != 0 && (cb.pieces[WHITE][KING] & 0x6) != 0) {
			score -= 40;
		}
		if ((cb.pieces[BLACK][ROOK] & 0xc0c0000000000000L) != 0 && (cb.pieces[BLACK][KING] & 0x6000000000000000L) != 0) {
			score += 40;
		} else if ((cb.pieces[BLACK][ROOK] & 0x303000000000000L) != 0 && (cb.pieces[BLACK][KING] & 0x600000000000000L) != 0) {
			score += 40;
		}

		// hanging pieces (undefended or attacking a bigger piece)
		if (EngineConstants.ENABLE_EVAL_HANGING_PIECES && EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
			long piece = cb.friendlyPieces[cb.colorToMoveInverse];
			while (piece != 0) {

				int index = Long.numberOfTrailingZeros(piece);

				// int schroderScore = SchroderUtil.getAttackScore(new int[] { attackBoards[WHITE][index],
				// attackBoards[BLACK][index] }, cb.colorToMove,
				// cb.pieceIndexes[index]);
				// int seeScore = SEEUtil.getSeeScore(cb, cb.colorToMove, index, cb.pieceIndexes[index], cb.allPieces);

				// TODO SEE vs schroder statistics
				// TODO cache schroder-scores

				// if (Math.abs(schroderScore - seeScore) > 150) {
				// System.out.println("See-score != schroder-score: " + seeScore + " " + schroderScore);
				// }

				if ((attackBoards[cb.colorToMove][index] & 7) != 0) {
					if ((attackBoards[cb.colorToMoveInverse][index] & 7) == 0) {
						score += cb.colorFactor * EvalConstants.MATERIAL_SCORES[cb.pieceIndexes[index]];
						// score += cb.colorFactor * seeScore;
					}
					// else if
					// (SchroderUtil.PIECE_INDEXES[Integer.numberOfLeadingZeros(attackBoards[cb.colorToMove][index]) -
					// 24] < cb.pieceIndexes[index]) {
					// score += cb.colorFactor * schroderScore;
					// }
				}
				piece &= piece - 1;
			}

		}

		return score;
	}

	public static int calculateMobilityScores(final ChessBoard cb) {

		if (!EngineConstants.ENABLE_EVAL_MOBILITY) {
			return 0;
		}

		int score = 0;

		// bishops
		long piece = cb.pieces[WHITE][BISHOP];
		while (piece != 0) {
			score += EvalConstants.BISHOP_MOBILITY[Long
					.bitCount(MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces, cb.friendlyPieces[WHITE]))];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][BISHOP];
		while (piece != 0) {
			score -= EvalConstants.BISHOP_MOBILITY[Long
					.bitCount(MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces, cb.friendlyPieces[BLACK]))];
			piece &= piece - 1;
		}

		// rooks
		piece = cb.pieces[WHITE][ROOK];
		while (piece != 0) {
			score += EvalConstants.ROOK_MOBILITY[Long
					.bitCount(MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces, cb.friendlyPieces[WHITE]))];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			score -= EvalConstants.ROOK_MOBILITY[Long
					.bitCount(MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces, cb.friendlyPieces[BLACK]))];
			piece &= piece - 1;
		}

		// queens
		piece = cb.pieces[WHITE][QUEEN];
		while (piece != 0) {
			score += EvalConstants.QUEEN_MOBILITY[Long
					.bitCount(MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces, cb.friendlyPieces[WHITE]))
					+ Long.bitCount(MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces, cb.friendlyPieces[WHITE]))];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][QUEEN];
		while (piece != 0) {
			score -= EvalConstants.QUEEN_MOBILITY[Long
					.bitCount(MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces, cb.friendlyPieces[BLACK]))
					+ Long.bitCount(MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces, cb.friendlyPieces[BLACK]))];
			piece &= piece - 1;
		}

		// knights
		piece = cb.pieces[WHITE][NIGHT];
		while (piece != 0) {
			score += EvalConstants.KNIGHT_MOBILITY[Long
					.bitCount(StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)] & (cb.friendlyPieces[BLACK] | cb.emptySpaces))];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT];
		while (piece != 0) {
			score -= EvalConstants.KNIGHT_MOBILITY[Long
					.bitCount(StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)] & (cb.friendlyPieces[WHITE] | cb.emptySpaces))];
			piece &= piece - 1;
		}

		return score;
	}

	public static int calculateMobilityWithKingDefenseScores(final ChessBoard cb) {

		// TODO disable mobility if piece is pinned?

		if (!EngineConstants.ENABLE_EVAL_MOBILITY) {
			return 0;
		}

		if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
			Arrays.fill(attackBoards[WHITE], 0);
			Arrays.fill(attackBoards[BLACK], 0);
		}

		int score = 0;
		long moves;

		// pawns
		// TODO get ALL pawn attacks??
		if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
			moves = cb.pieces[WHITE][PAWN] << 9 & ChessConstants.MASK_NOT_H_FILE | cb.pieces[WHITE][PAWN] << 7 & ChessConstants.MASK_NOT_A_FILE;
			while (moves != 0) {
				attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] = ++attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] | SchroderUtil.MASK_PAWN;
				moves &= moves - 1;
			}
			moves = cb.pieces[BLACK][PAWN] >>> 9 & ChessConstants.MASK_NOT_A_FILE | cb.pieces[BLACK][PAWN] >>> 7 & ChessConstants.MASK_NOT_H_FILE;
			while (moves != 0) {
				attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] = ++attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] | SchroderUtil.MASK_PAWN;
				moves &= moves - 1;
			}
		}

		// knights
		long piece = cb.pieces[WHITE][NIGHT];
		while (piece != 0) {
			moves = StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)];
			score += EvalConstants.KNIGHT_MOBILITY[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				while (moves != 0) {
					attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] = ++attackBoards[WHITE][Long.numberOfTrailingZeros(moves)]
							| SchroderUtil.MASK_NIGHT_BISHOP;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT];
		while (piece != 0) {
			moves = StaticMoves.KNIGHT_MOVES[Long.numberOfTrailingZeros(piece)];
			score -= EvalConstants.KNIGHT_MOBILITY[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				while (moves != 0) {
					attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] = ++attackBoards[BLACK][Long.numberOfTrailingZeros(moves)]
							| SchroderUtil.MASK_NIGHT_BISHOP;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}

		// bishops
		piece = cb.pieces[WHITE][BISHOP];
		while (piece != 0) {
			moves = MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score += EvalConstants.BISHOP_MOBILITY[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				while (moves != 0) {
					attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] = ++attackBoards[WHITE][Long.numberOfTrailingZeros(moves)]
							| SchroderUtil.MASK_NIGHT_BISHOP;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][BISHOP];
		while (piece != 0) {
			moves = MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score -= EvalConstants.BISHOP_MOBILITY[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				while (moves != 0) {
					attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] = ++attackBoards[BLACK][Long.numberOfTrailingZeros(moves)]
							| SchroderUtil.MASK_NIGHT_BISHOP;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}

		// rooks
		piece = cb.pieces[WHITE][ROOK];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score += EvalConstants.ROOK_MOBILITY[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				while (moves != 0) {
					attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] = ++attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] | SchroderUtil.MASK_ROOK;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score -= EvalConstants.ROOK_MOBILITY[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				while (moves != 0) {
					attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] = ++attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] | SchroderUtil.MASK_ROOK;
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
			score += EvalConstants.QUEEN_MOBILITY[Long.bitCount(moves & ~cb.friendlyPieces[WHITE])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				while (moves != 0) {
					attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] = ++attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] | SchroderUtil.MASK_QUEEN;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][QUEEN];
		while (piece != 0) {
			moves = MagicUtil.getRookMoves(Long.numberOfTrailingZeros(piece), cb.allPieces)
					| MagicUtil.getBishopMoves(Long.numberOfTrailingZeros(piece), cb.allPieces);
			score -= EvalConstants.QUEEN_MOBILITY[Long.bitCount(moves & ~cb.friendlyPieces[BLACK])];
			if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
				while (moves != 0) {
					attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] = ++attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] | SchroderUtil.MASK_QUEEN;
					moves &= moves - 1;
				}
			}
			piece &= piece - 1;
		}

		// kings
		if (EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
			moves = StaticMoves.KING_MOVES[cb.kingIndex[WHITE]];
			while (moves != 0) {
				attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] = ++attackBoards[WHITE][Long.numberOfTrailingZeros(moves)] | SchroderUtil.MASK_KING;
				moves &= moves - 1;
			}
			moves = StaticMoves.KING_MOVES[cb.kingIndex[BLACK]];
			while (moves != 0) {
				attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] = ++attackBoards[BLACK][Long.numberOfTrailingZeros(moves)] | SchroderUtil.MASK_KING;
				moves &= moves - 1;
			}
		}

		return score;
	}

	public static int calculateKingSafetyScores(final ChessBoard cb) {

		if (!EngineConstants.ENABLE_EVAL_MOBILITY_KING_DEFENSE) {
			return 0;
		}

		int score = 0;

		long nearbyPosition;
		int counter;
		int flag;

		// WHITE
		// TODO skip if only 1 queen?
		if (cb.pieces[BLACK][QUEEN] != 0) {
			// TODO counter = 0 if black has only 2 minor pieces
			counter = EvalConstants.KING_SAFETY_COUNTER[WHITE][cb.kingIndex[WHITE]];
			flag = 0;

			// front-further
			nearbyPosition = EvalConstants.KING_SAFETY_FRONT_FURTHER[WHITE][cb.kingIndex[WHITE]];
			while (nearbyPosition != 0) {
				flag |= attackBoards[BLACK][Long.numberOfTrailingZeros(nearbyPosition)];
				nearbyPosition &= nearbyPosition - 1;
			}
			// front
			nearbyPosition = EvalConstants.KING_SAFETY_FRONT[WHITE][cb.kingIndex[WHITE]];
			while (nearbyPosition != 0) {
				if (attackBoards[BLACK][Long.numberOfTrailingZeros(nearbyPosition)] != 0) {
					counter++;
					flag |= attackBoards[BLACK][Long.numberOfTrailingZeros(nearbyPosition)];
					if ((cb.friendlyPieces[WHITE] & Util.POWER_LOOKUP[Long.numberOfTrailingZeros(nearbyPosition)]) == 0) {
						// no friendlyPiece nearby
						counter++;
					}
					if (attackBoards[WHITE][Long.numberOfTrailingZeros(nearbyPosition)] == KING_SAFETY_ONLY_KING_ATTACK) {
						// no friendlyPiece protecting
						counter++;
					}
				}
				nearbyPosition &= nearbyPosition - 1;
			}
			// behind
			nearbyPosition = EvalConstants.KING_SAFETY_BEHIND[WHITE][cb.kingIndex[WHITE]];
			while (nearbyPosition != 0) {
				if (attackBoards[BLACK][Long.numberOfTrailingZeros(nearbyPosition)] != 0) {
					counter++;
					flag |= attackBoards[BLACK][Long.numberOfTrailingZeros(nearbyPosition)];
					if (attackBoards[WHITE][Long.numberOfTrailingZeros(nearbyPosition)] == KING_SAFETY_ONLY_KING_ATTACK) {
						// no friendlyPiece protecting
						counter++;
					}
				}
				nearbyPosition &= nearbyPosition - 1;
			}

			score -= EvalConstants.KING_SAFETY_SCORES[counter + EvalConstants.KING_SAFETY_ATTACK_PATTERN_COUNTER[flag >> 3]];
		}

		// BLACK
		if (cb.pieces[WHITE][QUEEN] != 0) {
			counter = EvalConstants.KING_SAFETY_COUNTER[BLACK][cb.kingIndex[BLACK]];
			flag = 0;

			// front-further
			nearbyPosition = EvalConstants.KING_SAFETY_FRONT_FURTHER[BLACK][cb.kingIndex[BLACK]];
			while (nearbyPosition != 0) {
				flag |= attackBoards[WHITE][Long.numberOfTrailingZeros(nearbyPosition)];
				nearbyPosition &= nearbyPosition - 1;
			}
			// front
			nearbyPosition = EvalConstants.KING_SAFETY_FRONT[BLACK][cb.kingIndex[BLACK]];
			while (nearbyPosition != 0) {
				if (attackBoards[WHITE][Long.numberOfTrailingZeros(nearbyPosition)] != 0) {
					counter++;
					flag |= attackBoards[WHITE][Long.numberOfTrailingZeros(nearbyPosition)];
					if ((cb.friendlyPieces[BLACK] & Util.POWER_LOOKUP[Long.numberOfTrailingZeros(nearbyPosition)]) == 0) {
						// no friendlyPiece nearby
						counter++;
					}
					if (attackBoards[BLACK][Long.numberOfTrailingZeros(nearbyPosition)] == KING_SAFETY_ONLY_KING_ATTACK) {
						// no friendlyPiece protecting
						counter++;
					}
				}
				nearbyPosition &= nearbyPosition - 1;
			}
			// behind
			nearbyPosition = EvalConstants.KING_SAFETY_BEHIND[BLACK][cb.kingIndex[BLACK]];
			while (nearbyPosition != 0) {
				if (attackBoards[WHITE][Long.numberOfTrailingZeros(nearbyPosition)] != 0) {
					counter++;
					flag |= attackBoards[WHITE][Long.numberOfTrailingZeros(nearbyPosition)];
					if (attackBoards[BLACK][Long.numberOfTrailingZeros(nearbyPosition)] == KING_SAFETY_ONLY_KING_ATTACK) {
						// no friendlyPiece protecting
						counter++;
					}
				}
				nearbyPosition &= nearbyPosition - 1;
			}

			score += EvalConstants.KING_SAFETY_SCORES[counter + EvalConstants.KING_SAFETY_ATTACK_PATTERN_COUNTER[flag >> 3]];
		}

		return score;
	}

	public static int calculatePositionScores(final ChessBoard cb) {

		int score = 0;

		// knights
		long piece = cb.pieces[WHITE][NIGHT];
		while (piece != 0) {
			score += EvalConstants.KNIGHT_POSITION_SCORES[WHITE][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][NIGHT];
		while (piece != 0) {
			score -= EvalConstants.KNIGHT_POSITION_SCORES[BLACK][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}

		// bishops
		piece = cb.pieces[WHITE][BISHOP];
		while (piece != 0) {
			score += EvalConstants.BISHOP_POSITION_SCORES[WHITE][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][BISHOP];
		while (piece != 0) {
			score -= EvalConstants.BISHOP_POSITION_SCORES[BLACK][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}

		// rooks
		piece = cb.pieces[WHITE][ROOK];
		while (piece != 0) {
			score += EvalConstants.ROOK_POSITION_SCORES[WHITE][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}
		piece = cb.pieces[BLACK][ROOK];
		while (piece != 0) {
			score -= EvalConstants.ROOK_POSITION_SCORES[BLACK][Long.numberOfTrailingZeros(piece)];
			piece &= piece - 1;
		}

		// king
		if (cb.isEndGame[WHITE]) {
			score += EvalConstants.KING_POSITION_SCORES_ENDGAME[WHITE][cb.kingIndex[WHITE]];
		} else {
			score += EvalConstants.KING_POSITION_SCORES[WHITE][cb.kingIndex[WHITE]];
		}
		if (cb.isEndGame[BLACK]) {
			score -= EvalConstants.KING_POSITION_SCORES_ENDGAME[BLACK][cb.kingIndex[BLACK]];
		} else {
			score -= EvalConstants.KING_POSITION_SCORES[BLACK][cb.kingIndex[BLACK]];
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
