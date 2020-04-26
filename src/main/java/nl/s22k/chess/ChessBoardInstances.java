package nl.s22k.chess;

import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.engine.UciOptions;

public class ChessBoardInstances {

	private static ChessBoard[] instances;
	static {
		if (EngineConstants.TEST_EVAL_VALUES) {
			init(2);
		} else {
			init(UciOptions.threadCount);
		}
	}

	public static ChessBoard get(final int instanceNumber) {
		return instances[instanceNumber];
	}

	public static void init(final int numberOfInstances) {
		instances = new ChessBoard[numberOfInstances];
		for (int i = 0; i < numberOfInstances; i++) {
			instances[i] = new ChessBoard();
		}
	}

}
