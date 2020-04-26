package nl.s22k.chess.maintests;

import java.util.Map;
import java.util.Map.Entry;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.QuiescenceUtil;
import nl.s22k.chess.search.TTUtil;
import nl.s22k.chess.search.ThreadData;
import nl.s22k.chess.texel.Tuner;

/**
 * compares regular search scores vs Quiescence scores
 *
 */
public class QSearchTest {

	private static ThreadData threadData = new ThreadData(0);

	public static void main(String[] args) {

		ChessBoard cb = ChessBoardInstances.get(0);

		// read all fens, including score
		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\violent.epd", false, true);
		System.out.println("Fens found : " + fens.size());

		// NegamaxUtil.isRunning = true;
		EngineConstants.POWER_2_TT_ENTRIES = 1;
		TTUtil.init(false);

		double totalPositions = 0;
		double sameScore = 0;
		long totalError = 0;
		final long start = System.currentTimeMillis();
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoardUtil.setFen(entry.getKey(), cb);
			if (cb.checkingPieces == 0) {
				continue;
			}
			totalPositions++;
			int searchScore = NegamaxUtil.calculateBestMove(cb, threadData, 0, 1, Util.SHORT_MIN, Util.SHORT_MAX, 0);
			TTUtil.clearValues();
			int qScore = QuiescenceUtil.calculateBestMove(cb, threadData, Util.SHORT_MIN, Util.SHORT_MAX);

			if (searchScore == qScore) {
				sameScore++;
			} else {
				int error = searchScore - qScore;
				// if (error > 500) {
				// System.out.println(searchScore + " " + qScore);
				// QuiescenceUtil.calculateBestMove(cb, threadData, Util.SHORT_MIN, Util.SHORT_MAX);
				// }

				totalError += error;
			}

		}

		int averageError = (int) (totalError / (totalPositions - sameScore));
		System.out.println(String.format("%.4f %s", sameScore / totalPositions, averageError));
		System.out.println("msec: " + (System.currentTimeMillis() - start));
	}

}
