package nl.s22k.chess.texel;

import java.util.Map;
import java.util.Map.Entry;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.move.MagicUtil;

public class LargestErrorCalculator {

	private static double[] largestError = new double[100];
	private static String[] largestErrorFen = new String[100];

	public static void main(String[] args) {

		// setup
		EngineConstants.ENABLE_PAWN_EVAL_CACHE = false;
		EngineConstants.ENABLE_MATERIAL_CACHE = false;
		EngineConstants.isTuningSession = true;
		MagicUtil.init();

		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println(fens.size() + " fens found");

		ChessBoard cb = ChessBoardUtil.getNewCB();
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoardUtil.setFenValues(entry.getKey(), cb);
			ChessBoardUtil.init(cb);
			double error = Math.pow(entry.getValue() - ErrorCalculator.calculateSigmoid(EvalUtil.calculateScore(cb)), 2);

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
