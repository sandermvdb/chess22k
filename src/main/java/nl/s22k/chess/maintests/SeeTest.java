package nl.s22k.chess.maintests;

import java.util.Map;
import java.util.Map.Entry;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.SEEUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.texel.Tuner;

/**
 * compares SEE scores vs Quiescence scores (material score with attacks on the same square)
 *
 */
public class SeeTest {

	public static void main(String[] args) {

		// setup
		EngineConstants.isTuningSession = true;
		MagicUtil.init();

		// read all fens, including score
		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\violent.epd", false, true);
		System.out.println("Fens found : " + fens.size());

		double sameScore = 0;
		double totalAttacks = 0;
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoard cb = ChessBoardUtil.getNewCB(entry.getKey());
			MoveList.startPly();
			MoveGenerator.generateAttacks(cb);
			while (MoveList.hasNext()) {
				totalAttacks++;
				int move = MoveList.next();
				int seeScore = SEEUtil.getSeeCaptureScore(cb, move);
				final int materialScore = EvalUtil.calculateMaterialIncludingPawnScores(cb);
				int qScore = ChessConstants.COLOR_FACTOR[cb.colorToMoveInverse] * materialScore - calculateQScore(cb, move, true);
				if (seeScore == qScore) {
					sameScore++;
				}
				// else {
				// seeScore = SEEUtil.getSeeCaptureScore(cb, move);
				// qScore = ChessConstants.COLOR_FACTOR[cb.colorToMoveInverse] * materialScore - calculateQScore(cb,
				// move, true);
				// System.out.println();
				// }
			}
			MoveList.endPly();
		}
		System.out.println(String.format("%.0f %.0f = %.4f", sameScore, totalAttacks, sameScore / totalAttacks));

	}

	private static int calculateQScore(ChessBoard cb, int move, boolean isFirstMove) {
		int bestScore = Util.SHORT_MIN;

		cb.doMove(move);

		MoveList.startPly();
		MoveGenerator.generateAttacks(cb);

		boolean movePerformed = false;
		while (MoveList.hasNext()) {
			// only attacks on the same square
			int currentMove = MoveList.next();
			if (MoveUtil.getToIndex(currentMove) != MoveUtil.getToIndex(move)) {
				continue;
			}

			int score = -calculateQScore(cb, currentMove, false);
			score = Math.max(score, ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.calculateMaterialIncludingPawnScores(cb));

			movePerformed = true;
			if (score > bestScore) {
				bestScore = score;
			}
		}
		MoveList.endPly();

		if (!movePerformed) {
			bestScore = ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.calculateMaterialIncludingPawnScores(cb);
		}

		cb.undoMove(move);
		return bestScore;

	}

}
