package nl.s22k.chess.texel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.eval.EvalUtil;

public class ErrorCalculator implements Callable<Double> {

	private Map<String, Double> fens = new HashMap<String, Double>();
	private ChessBoard cb;

	public ErrorCalculator(ChessBoard cb) {
		this.cb = cb;
	}

	public void addFenWithScore(String fen, double score) {
		fens.put(fen, score);
	}

	@Override
	public Double call() throws Exception {
		double totalError = 0;

		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoardUtil.setFenValues(entry.getKey(), cb);
			ChessBoardUtil.init(cb);
			// error += Math.pow(entry.getValue()
			// - calculateSigmoid(ChessConstants.COLOR_FACTOR[cb.colorToMove] * QuiescenceUtil.calculateBestMove(cb, 0,
			// Util.SHORT_MIN, Util.SHORT_MAX)),
			// 2);
			totalError += Math.pow(entry.getValue() - calculateSigmoid(EvalUtil.calculateScore(cb)), 2);
		}
		totalError /= fens.size();
		return totalError;
	}

	public static double calculateSigmoid(int score) {
		return 1 / (1 + Math.pow(10, -1.3 * score / 400));
	}

}
