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

	@Test
	public void doSeeTest2() {
		System.out.println("SeeTest 2");
		ChessBoard cb = ChessBoardUtil.getNewCB("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - -");
		MoveGenerator.generateAttacks(cb);
		MoveList.setSeeScores(cb);
		while (MoveList.hasNext()) {
			int move = MoveList.next();
			if (MoveUtil.getFromIndex(move) == 20 && MoveUtil.getToIndex(move) == 35) {
				System.out.println(new MoveWrapper(move) + " " + SEEUtil.getSeeCaptureScore(cb, move) + " -225");
			}
		}
	}

	@Test
	public void doSeeTest3() {
		System.out.println("SeeTest 3");
		ChessBoard cb = ChessBoardUtil.getNewCB("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - -");
		MoveGenerator.generateAttacks(cb);
		MoveList.setSeeScores(cb);
		while (MoveList.hasNext()) {
			int move = MoveList.next();
			System.out.println(new MoveWrapper(move) + " " + SEEUtil.getSeeCaptureScore(cb, move) + " 100");
		}
		System.out.println("");
	}

	@Test
	public void doSeeTest4() {
		// 12 see-moves possible!
		// 2 white queens + pawn vs 2 black bishops = 1800+100-700 = 1200
		// ends with equal pieces -> SEE = -1200
		// IF Math.max(0, score) is disabled!!
		System.out.println("SeeTest 4");
		ChessBoard cb = ChessBoardUtil.getNewCB("2N2r2/nP4pk/b3Q1pp/5b2/6Q1/KPR4q/PPR5/Q1r5 b - - 0 1 ");
		MoveGenerator.generateAttacks(cb);
		MoveList.setSeeScores(cb);
		while (MoveList.hasNext()) {
			int move = MoveList.next();
			if (MoveUtil.getFromIndex(move) == 55 && MoveUtil.getToIndex(move) == 61) {
				System.out.println(new MoveWrapper(move) + " " + SEEUtil.getSeeCaptureScore(cb, move) + " +1200");
			}
		}
		System.out.println("");
	}
}
