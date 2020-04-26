package nl.s22k.chess.texel;

import java.util.Map;
import java.util.Map.Entry;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.MaterialUtil;
import nl.s22k.chess.search.ThreadData;

public class EndgameEvaluator {

	public static void main(String[] args) {

		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println(fens.size() + " fens found");

		ErrorCount KPK = new ErrorCount("KPK ");
		ErrorCount KBNK = new ErrorCount("KBNK");
		ErrorCount KBPK = new ErrorCount("KBPK");
		ErrorCount KRKP = new ErrorCount("KRKP");
		ErrorCount KQKP = new ErrorCount("KQKP");
		ErrorCount KRKB = new ErrorCount("KRKB");
		ErrorCount KRKN = new ErrorCount("KRKN");
		ErrorCount KBPKP = new ErrorCount("KBPKP");
		ErrorCount KRBKB = new ErrorCount("KRBKB");
		ErrorCount KRBKR = new ErrorCount("KRBKR");

		ChessBoard cb = ChessBoardInstances.get(0);
		ThreadData threadData = ThreadData.getInstance(0);
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoardUtil.setFen(entry.getKey(), cb);

			double error = Math.pow(
					entry.getValue() - ErrorCalculator.calculateSigmoid(ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.calculateScore(cb, threadData)),
					2);
			if (MaterialUtil.isKBNK(cb.materialKey)) {
				KBNK.addError(error);
			} else if (MaterialUtil.isKQKP(cb.materialKey)) {
				KQKP.addError(error);
			} else if (MaterialUtil.isKRKP(cb.materialKey)) {
				KRKP.addError(error);
			} else if (MaterialUtil.isKRKB(cb.materialKey)) {
				KRKB.addError(error);
			} else if (MaterialUtil.isKRKN(cb.materialKey)) {
				KRKN.addError(error);
			} else if (MaterialUtil.isKPK(cb.materialKey)) {
				KRKN.addError(error);
			} else if (MaterialUtil.isKBPK(cb.materialKey)) {
				KBPK.addError(error);
			} else if (MaterialUtil.isKBPKP(cb.materialKey)) {
				KBPKP.addError(error);
			} else if (MaterialUtil.isKRBKB(cb.materialKey)) {
				KRBKB.addError(error);
			} else if (MaterialUtil.isKRBKR(cb.materialKey)) {
				KRBKR.addError(error);
			}

		}

		KPK.print();
		KBNK.print();
		KRKP.print();
		KQKP.print();
		KRKB.print();
		KRKN.print();
		KRBKB.print();
		KRBKR.print();
		KBPK.print();
		KBPKP.print();
	}

	public static class ErrorCount {
		int count;
		double totalError;
		String name;

		public ErrorCount(String name) {
			this.name = name;
		}

		public void addError(double error) {
			totalError += error;
			count++;
		}

		public void print() {
			if (count == 0) {
				System.out.println(name + " 0");
			} else {
				System.out.println(String.format("%s %f", name, totalError / count));
			}
		}
	}

}
