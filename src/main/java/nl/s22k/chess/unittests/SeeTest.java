package nl.s22k.chess.unittests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.eval.SEEUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.MoveWrapper;

public class SeeTest {

	@BeforeClass
	public static void init() {
		MagicUtil.init();
	}

	@Test
	public void doSeeTest1() {
		System.out.println("SeeTest 1");
		ChessBoard cb = ChessBoardUtil.getNewCB("k5q1/8/6b1/8/8/6P1/2B3R1/K7 w - - 0 1 ");
		MoveGenerator.generateAttacks(cb);
		while (MoveList.hasNext()) {
			int move = MoveList.next();
			if (MoveUtil.getFromIndex(move) == 13 && MoveUtil.getToIndex(move) == 41) {
				System.out.println(new MoveWrapper(move) + " " + SEEUtil.getSeeCaptureScore(cb, move) + " 0");
			}
		}
	}

}
