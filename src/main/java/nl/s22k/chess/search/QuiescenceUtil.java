package nl.s22k.chess.search;

import nl.s22k.chess.CheckUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;

public class QuiescenceUtil {

	// TODO do not generate under-promotions?

	public static int calculateBestMove(final ChessBoard cb, final int ply, int alpha, final int beta) {

		Statistics.maxDepth = Math.max(Statistics.maxDepth, ply);

		int score = Util.SHORT_MIN + ply;

		if (cb.checkingPieces == 0) {
			/* stand-pat check */
			score = cb.colorFactor * EvalUtil.calculateScore(cb);
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

			if (EngineConstants.ENABLE_DELTA_PRUNING) {
				if (!cb.isEndGame[cb.colorToMove] && MoveUtil.getScore(move) * MoveUtil.SEE_CAPTURE_DIVIDER + EngineConstants.DELTA_MARGIN < alpha) {
					continue;
				}
			}

			/* prune bad-captures */
			if (EngineConstants.ENABLE_Q_PRUNE_BAD_CAPTURES && MoveUtil.getScore(move) < 0) {
				// no SEE score is assigned when in check so no moves will then be skipped
				continue;
			}

			cb.doMove(move);

			if (EngineConstants.TEST_VALUES) {
				cb.changeSideToMove();
				if (CheckUtil.isInCheck(cb)) {
					System.out.println("Q: Just did an illegal move...");
				}
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
