package nl.s22k.test.chess;

import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.RepetitionTable;
import nl.s22k.chess.search.TimeUtil;
import nl.s22k.chess.unittests.MainTest;

public class CoverageTest {

	@Test
	public void doTest() {
		MagicUtil.init();

		ChessBoard cb = ChessBoardUtil.getNewCB(MainTest.FEN_STANDARD_MIDDLEGAME);

		/* time-managed */
		TimeUtil.setSimpleTimeWindow(5000);
		NegamaxUtil.start(cb);
	}

	// @Test
	public void doTestMovesPerformed() {
		MagicUtil.init();
		ChessBoard cb = ChessBoardUtil.getNewCB();

		final String moves = "d2d4 d7d5 c2c4 c7c6 g1f3 g8f6 b1c3 d5c4 a2a4 b8a6 e2e3 c8g4 f1c4 e7e6 h2h3 g4h5 "
				+ "e1g1 a6b4 d1e2 f8e7 a4a5 e8g8 f1d1 d8c7 a5a6 b4a6 c4a6 b7a6 e3e4 f8d8 a1a6 a8b8 g2g4 h5g6 "
				+ "f3e5 b8b6 c1f4 c7b7 d1a1 d8a8 a6a2 f6d7 e5g6 h7g6 f4e3 b6b4 d4d5 c6d5 a2a7 a8a7 a1a7 b7c6 "
				+ "e4d5 e6d5 e2f3 b4b2 c3d5 b2b1 g1g2 g8h7 d5e7 b1g1 g2g1 c6f3 a7d7 f3h3 d7d4 g6g5 e7f5 f7f6 "
				+ "f5g3 h7g6 e3d2 g6f7 d2b4 g7g6 b4d2 f7e6 d4a4 e6e7 a4c4 e7e8 d2e3 e8d7 e3c1 d7e8 c4a4 e8d7 "
				+ "a4e4 d7c8 e4d4 c8c7 g3f1 h3c3 c1e3 c3a1 d4c4 c7d7 e3d4 a1a2 c4c5 d7e6 f1e3 a2b1 g1h2 b1e4 "
				+ "d4c3 e4f4 h2g1 f4e4 c5d5 e4b1 g1g2 b1e4 g2f1 e4b1 f1e2 b1a2 e2f3 a2a8 f3g2 a8b7 f2f3 b7a6 "
				+ "c3e1 a6e2 e1f2 e6e7 d5c5 e7d6 c5c2 e2a6 c2b2 d6d7 b2b4 a6a2 b4b5 a2a6 b5c5 d7e6 e3c2 a6a2 "
				+ "c2d4 e6e7 d4c6 e7d7 c5a5 a2d2 a5a6 d2d3 c6b8 d7c8 a6b6 f6f5 b8a6 d3e2 b6b8 c8d7 a6c5 d7c7 "
				+ "b8b4 c7d8 c5b3 e2a2 b4b8 d8c7 b8b5 f5g4 b5c5 c7d6 c5c3 g4f3 c3f3 d6c6 g2g3 c6d7 f3e3 d7c6 "
				+ "b3d4 c6d5 d4f3 d5d6 e3d3 d6e7 f2c5 e7f6 c5d4 f6e7 d3e3 e7d7 d4c5 d7c6 c5a3 a2g8 e3c3 c6d5 "
				+ "c3d3 d5e6 d3e3 e6d5 e3e5 d5c4 e5e4 c4b3 a3c5 b3c2 e4d4 c2b3 d4d6 g8b8 f3d2 b3b2 d2c4 b2c2 "
				+ "c4e3 c2c3 g3f3 b8b7 f3f2 b7e4 d6d1 g5g4 c5b6 e4f3 f2g1 f3g3 e3g2 g3f3 d1e1 c3c4 g2e3 c4b5 "
				+ "e1b1 b5c6 b6d4 g4g3 b1f1 f3e4 f1d1 g6g5 e3f1 e4f3 d1e1 g5g4 d4e3 g3g2 f1d2 f3g3 e1c1 c6d7 "
				+ "e3b6 g3h3 c1c7 d7d6 d2e4 d6d5 e4f2 h3f3 c7c5 d5e6 c5c1 e6f7 c1e1 f3c3 e1d1 g4g3 f2e4 c3c6 "
				+ "d1e1 c6b6 g1g2 f7e6 e1e2 e6d5 g2g3 b6h6 g3f3 d5d4 e2d2 d4e5 e4d6 h6f6 f3e3 f6f1 d2d3 f1f4 "
				+ "e3e2 f4g4 e2f2 g4h4 f2e2 h4h2 e2e3 h2g1 e3f3 g1h1 f3f2 h1c1 f2e2 c1f4 d3d2 f4b4 d2d3 b4c5 "
				+ "e2f3 c5c7 f3e2 c7c2 e2e3 c2a2 d6b5 a2c4 b5d6 c4g4 e3f2 g4h4 f2e2 e5e6 e2e3 h4e7 d6e4 e7a7 "
				+ "e3f3 e6e5 e4d6 a7a2 f3e3 a2e6 e3f2 e6f6 f2e3 f6h6 e3e2 h6c1 e2f3 c1c6 f3e3 c6b6 e3f3 b6c5 "
				+ "f3e2 e5e6 d6e4 c5c2 e2e3 c2c1 e3f3 c1h1 f3e3 e6e5 e4d6 h1h3 e3d2 h3g4 d2e3 g4g5 e3f3 e5e6 " + "d6e4";
		String[] movesArray = moves.split(" ");
		for (String moveToken : movesArray) {
			MoveWrapper move = new MoveWrapper(moveToken, cb);
			cb.doMove(move.move);
			RepetitionTable.addValue(cb.zobristKey);
		}

		/* time-managed */
		TimeUtil.setSimpleTimeWindow(5000);
		NegamaxUtil.start(cb);
	}

}
