package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public class EvalUtil {

	// TODO tropism?

	// TODO fianchetto
	// TODO extract constants
	// TODO 50-move rule
	// TODO knight outpost: 4,5,6,7th rank, protected by a pawn, cannot be attacked by enemy pawns

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
		score += calculateMaterialScores(cb) + calculateMobilityScores(cb) + calculateBonusses(cb) + calculatePenalties(cb) + calculatePawnScores(cb);

		EvalCache.addValue(cb.zobristKey, score);

		return score;
	}

	public static int calculatePawnScores(final ChessBoard cb) {

		if (EngineConstants.ENABLE_PAWN_EVAL_CACHE && !EngineConstants.TEST_PAWN_EVAL_CACHE) {
			if (PawnEvalCache.hasScore(cb.pawnZobristKey)) {
				return PawnEvalCache.getScore(cb.pawnZobristKey);
			}
		}

		int score = 0;

		// penalty for doubled pawns
		for (int i = 0; i < 8; i++) {
			if (Long.bitCount(cb.pieces[WHITE][PAWN] & ChessConstants.MASKS_FILE[i]) > 1) {
				score -= 10;
			}
			if (Long.bitCount(cb.pieces[BLACK][PAWN] & ChessConstants.MASKS_FILE[i]) > 1) {
				score += 10;
			}
		}

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

		// white
		long pawns = cb.pieces[WHITE][PAWN];
		while (pawns != 0) {
			// isolated pawns
			if ((ChessConstants.MASK_ADJACENT_FILE[Long.numberOfTrailingZeros(pawns) & 7] & cb.pieces[WHITE][PAWN]) == 0) {
				score -= 15;
			}

			// passed pawns
			if ((EvalConstants.PASSED_PAWN_MASKS[WHITE][Long.numberOfTrailingZeros(pawns)] & cb.pieces[BLACK][PAWN]) == 0) {
				if (cb.isEndGame[WHITE]) {
					score += EvalConstants.PASSED_PAWN_ENDGAME_SCORE[Long.numberOfTrailingZeros(pawns) / 8];
				} else {
					score += 20;
				}
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

			// passed pawns
			if ((EvalConstants.PASSED_PAWN_MASKS[BLACK][Long.numberOfTrailingZeros(pawns)] & cb.pieces[WHITE][PAWN]) == 0) {
				if (cb.isEndGame[BLACK]) {
					score -= EvalConstants.PASSED_PAWN_ENDGAME_SCORE[7 - Long.numberOfTrailingZeros(pawns) / 8];
				} else {
					score -= 20;
				}
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

		// score += tropismScore;

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

		PawnEvalCache.addValue(cb.pawnZobristKey, score);

		return score;
	}

	public static int calculateBonusses(final ChessBoard cb) {
		int score = 0;

		// double bishop bonus
		if (Long.bitCount(cb.pieces[WHITE][BISHOP]) == 2) {
			score += 50;
		}
		if (Long.bitCount(cb.pieces[BLACK][BISHOP]) == 2) {
			score -= 50;
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

		return score;
	}

	public static int calculatePenalties(final ChessBoard cb) {
		int score = 0;

		// penalty for being in check
		if (cb.checkingPieces != 0) {
			score -= cb.colorFactor * 10;
		}

		if (cb.moveCounter < 16) {
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
