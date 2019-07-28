package nl.s22k.chess.search;

import nl.s22k.chess.Assert;
import nl.s22k.chess.CheckUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.MaterialUtil;
import nl.s22k.chess.eval.SEEUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveUtil;

public class QuiescenceUtil {

	private static final int FUTILITY_MARGIN = 200;

	public static int calculateBestMove(final ChessBoard cb, final MoveGenerator moveGen, int alpha, final int beta) {

		if (Statistics.ENABLED) {
			Statistics.qNodes++;
		}

		if (!NegamaxUtil.isRunning) {
			return ChessConstants.SCORE_NOT_RUNNING;
		}

		/* transposition-table */
		long ttValue = TTUtil.getValue(cb.zobristKey);
		int score = TTUtil.getScore(ttValue, 64);
		if (ttValue != 0) {
			if (!EngineConstants.TEST_TT_VALUES) {
				switch (TTUtil.getFlag(ttValue)) {
				case TTUtil.FLAG_EXACT:
					return score;
				case TTUtil.FLAG_LOWER:
					if (score >= beta) {
						return score;
					}
					break;
				case TTUtil.FLAG_UPPER:
					if (score <= alpha) {
						return score;
					}
				}
			}
		}

		if (cb.checkingPieces != 0) {
			return alpha;
		}

		/* stand-pat check */
		int eval = EvalUtil.getScore(cb);
		/* use tt value as eval */
		if (EngineConstants.USE_TT_SCORE_AS_EVAL) {
			if (TTUtil.canRefineEval(ttValue, eval, score)) {
				eval = score;
			}
		}
		if (eval >= beta) {
			return eval;
		}
		alpha = Math.max(alpha, eval);

		moveGen.startPly();
		moveGen.generateAttacks(cb);
		moveGen.setMVVLVAScores();
		moveGen.sort();

		while (moveGen.hasNext()) {
			final int move = moveGen.next();

			// skip under promotions
			if (MoveUtil.isPromotion(move)) {
				if (MoveUtil.getMoveType(move) != MoveUtil.TYPE_PROMOTION_Q) {
					continue;
				}
			} else if (EngineConstants.ENABLE_Q_FUTILITY_PRUNING
					&& eval + FUTILITY_MARGIN + EvalConstants.MATERIAL[MoveUtil.getAttackedPieceIndex(move)] < alpha) {
				// futility pruning
				continue;
			}

			if (!cb.isLegal(move)) {
				continue;
			}

			// skip bad-captures
			if (EngineConstants.ENABLE_Q_PRUNE_BAD_CAPTURES && !cb.isDiscoveredMove(MoveUtil.getFromIndex(move)) && SEEUtil.getSeeCaptureScore(cb, move) <= 0) {
				continue;
			}

			cb.doMove(move);

			if (EngineConstants.ASSERT) {
				cb.changeSideToMove();
				Assert.isTrue(0 == CheckUtil.getCheckingPieces(cb));
				cb.changeSideToMove();
			}

			score = MaterialUtil.isDrawByMaterial(cb) ? EvalConstants.SCORE_DRAW : -calculateBestMove(cb, moveGen, -beta, -alpha);

			cb.undoMove(move);

			if (score >= beta) {
				moveGen.endPly();
				return score;
			}
			alpha = Math.max(alpha, score);
		}

		moveGen.endPly();
		return alpha;
	}
}
