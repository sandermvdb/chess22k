package nl.s22k.chess.search;

import nl.s22k.chess.CheckUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;

public class QuiescenceUtil {

	public static int calculateBestMove(final ChessBoard cb, final int ply, int alpha, int beta) {

		Statistics.maxDepth = Math.max(Statistics.maxDepth, ply);

		/* mate-distance pruning */
		if (EngineConstants.ENABLE_MATE_DISTANCE_PRUNING) {
			alpha = Math.max(alpha, Util.SHORT_MIN + ply);
			beta = Math.min(beta, Util.SHORT_MAX - ply - 1);
			if (alpha >= beta) {
				return alpha;
			}
		}

		int score = Util.SHORT_MIN + ply;

		if (cb.checkingPieces == 0) {
			/* stand-pat check */
			// TODO include hanging pieces in score (use SEE-score?)
			score = ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.calculateScore(cb);
			if (score >= beta) {
				return score;
			}

			alpha = Math.max(alpha, score);

			MoveList.startPly();
			MoveGenerator.generateAttacks(cb);
			if (EngineConstants.ENABLE_Q_SEE) {
				MoveList.setSeeScores(cb);
			}
			MoveList.sort();
		} else {
			// generate ALL evasive moves
			if (Statistics.ENABLED) {
				Statistics.qChecks++;
			}
			MoveList.startPly();
			MoveGenerator.generateAttacks(cb);
			MoveGenerator.generateMoves(cb);
		}

		if (!MoveList.hasNext()) {
			MoveList.endPly();
			return score;
		}

		while (MoveList.hasNext()) {
			final int move = MoveList.next();
			if (MoveUtil.isPromotion(move) && MoveUtil.getMoveType(move) != MoveUtil.TYPE_PROMOTION_Q) {
				continue;
			}

			/* prune bad-captures */
			if (EngineConstants.ENABLE_Q_PRUNE_BAD_CAPTURES && MoveUtil.getScore(move) < 0) {
				// no SEE score is assigned when in check so no moves will then be skipped
				break;
			}

			cb.doMove(move);

			if (EngineConstants.ASSERT) {
				cb.changeSideToMove();
				assert !CheckUtil.isInCheck(cb) : "Q: Just did an illegal move...";
				cb.changeSideToMove();
			}

			score = -calculateBestMove(cb, ply + 1, -beta, -alpha);

			cb.undoMove(move);

			if (score >= beta) {
				MoveList.endPly();
				return score;
			}
			alpha = Math.max(alpha, score);
		}

		MoveList.endPly();
		return alpha;
	}
}
