package nl.s22k.chess.texel;

import java.util.Map;
import java.util.Map.Entry;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.move.MagicUtil;

public class TestSetStatistics {

	private static int[] pieceCounts = new int[33];

	public static void main(String[] args) {

		MagicUtil.init();
		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println(fens.size() + " fens found");

		ChessBoard cb = ChessBoardUtil.getNewCB();
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoardUtil.setFenValues(entry.getKey(), cb);
			ChessBoardUtil.init(cb);

			pieceCounts[Long.bitCount(cb.allPieces)]++;

		}

		for (int i = 0; i < 33; i++) {
			System.out.println(i + " " + pieceCounts[i]);
		}

	}

}
