package nl.s22k.chess.search;

import nl.s22k.chess.CheckUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.SEEUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;

public class QuiescenceUtil {

	public static int calculateBestMove(final ChessBoard cb, final int ply, int alpha, int beta) {

		Statistics.maxDepth = Math.max(Statistics.maxDepth, ply);

		/* stand-pat check */
		int score = ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.getScore(cb);
		if (score >= beta) {
			return score;
		}

		alpha = Math.max(alpha, score);

		MoveList.startPly();
		MoveGenerator.generateAttacks(cb);
		if (EngineConstants.ENABLE_Q_SEE) {
			MoveList.setMVVLVAScores(cb);
		}
		MoveList.sort();

		while (MoveList.hasNext()) {
			final int move = MoveList.next();
			if (MoveUtil.isPromotion(move) && MoveUtil.getMoveType(move) != MoveUtil.TYPE_PROMOTION_Q) {
				continue;
			}

			/* prune bad-captures */
			if (EngineConstants.ENABLE_Q_PRUNE_BAD_CAPTURES && SEEUtil.getSeeCaptureScore(cb, move) < 0) {
				continue;
			}

			cb.doMove(move);

			if (EngineConstants.ASSERT) {
				cb.changeSideToMove();
				assert CheckUtil.getCheckingPieces(cb) == 0 : "Q: Just did an illegal move...";
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
