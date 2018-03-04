package nl.s22k.chess.search;

import static org.junit.Assert.assertEquals;

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

	public static int calculateBestMove(final ChessBoard cb, int alpha, int beta) {

		if (Statistics.ENABLED) {
			Statistics.qNodes++;
		}

		/* stand-pat check */
		int score = ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.getScore(cb);
		if (score >= beta) {
			return score;
		}

		alpha = Math.max(alpha, score);

		MoveList.startPly();
		MoveGenerator.generateAttacks(cb);
		MoveList.setMVVLVAScores(cb);
		MoveList.sort();

		while (MoveList.hasNext()) {
			final int move = MoveList.next();

			if (!cb.isLegal(move)) {
				continue;
			}

			if (MoveUtil.isPromotion(move) && MoveUtil.getMoveType(move) != MoveUtil.TYPE_PROMOTION_Q) {
				continue;
			}

			/* prune bad-captures */
			if (EngineConstants.ENABLE_Q_PRUNE_BAD_CAPTURES && SEEUtil.getSeeCaptureScore(cb, move) <= 0) {
				continue;
			}

			cb.doMove(move);

			if (EngineConstants.ASSERT) {
				cb.changeSideToMove();
				assertEquals(0, CheckUtil.getCheckingPieces(cb));
				cb.changeSideToMove();
			}

			score = -calculateBestMove(cb, -beta, -alpha);

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
