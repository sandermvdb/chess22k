package nl.s22k.chess.texel;

import java.util.Map;
import java.util.Map.Entry;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.search.ThreadData;

public class LargestErrorCalculator {

	private static double[] largestError = new double[100];
	private static String[] largestErrorFen = new String[100];

	public static void main(String[] args) {

		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println(fens.size() + " fens found");

		ChessBoard cb = ChessBoardInstances.get(0);
		ThreadData threadData = ThreadData.getInstance(0);
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoardUtil.setFen(entry.getKey(), cb);
			double error = Math.pow(entry.getValue() - ErrorCalculator.calculateSigmoid(EvalUtil.calculateScore(cb, threadData)), 2);

			for (int i = 0; i < largestError.length; i++) {
				if (error > largestError[i]) {
					largestError[i] = error;
					largestErrorFen[i] = entry.getKey();
					break;
				}
			}

		}

		for (int i = 0; i < largestError.length; i++) {
			System.out.println(String.format("%60s -> %s", largestErrorFen[i], largestError[i]));
		}

	}

}
