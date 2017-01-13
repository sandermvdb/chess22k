package nl.s22k.chess.search;

import nl.s22k.chess.CheckUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;

public class QuiescenceUtil {

	// TODO combine mobility calculation when evaluating and when generating moves

	public static int calculateBestMove(final ChessBoard cb, final int ply, int alpha, final int beta) {

		// check-extensions are not implemented because we only search for non-quiescence moves

		Statistics.maxDepth = Math.max(Statistics.maxDepth, ply);

		/* stand-pat check */
		int score = cb.colorFactor * EvalUtil.calculateScore(cb);
		if (score >= beta) {
			return score;
		}
		alpha = Math.max(alpha, score);

		MoveList.startPly();
		MoveGenerator.generateAttacks(cb);
		if (!MoveList.hasNext()) {
			MoveList.endPly();
			return score;
		}

		if (EngineConstants.ENABLE_Q_SEE) {
			MoveList.setSeeScores(cb);
		}

		MoveList.sort();

		while (MoveList.hasNext()) {
			final int move = MoveList.next();

			if (EngineConstants.ENABLE_DELTA_PRUNING) {
				if (!cb.isEndGame[cb.colorToMove] && MoveUtil.getScore(move) * 4 + EngineConstants.DELTA_MARGIN < alpha) {
					continue;
				}
			}

			// TODO prune bad-captures?
			if (EngineConstants.ENABLE_Q_PRUNE_BAD_CAPTURES && MoveList.currentMoveCounter() > 1 && MoveUtil.getScore(move) < 0) {
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
