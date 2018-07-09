package nl.s22k.chess.unittests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveUtil;

public class RepetitionTest {

	@BeforeClass
	public static void init() {
		MagicUtil.init();
	}

	@Test
	public void doRepetitionTest() {
		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB("8/8/4k3/5p2/5K2/8/8/8 w - - 0 63 ");

		int move1 = MoveUtil.createMove(26, 19, ChessConstants.KING);
		int move2 = MoveUtil.createMove(43, 36, ChessConstants.KING);
		int move3 = MoveUtil.createMove(19, 26, ChessConstants.KING);
		int move4 = MoveUtil.createMove(36, 43, ChessConstants.KING);

		System.out.println(cb.zobristKey);
		cb.doMove(move1);
		System.out.println(cb.zobristKey);
		cb.doMove(move2);
		System.out.println(cb.zobristKey);
		cb.doMove(move3);
		System.out.println(cb.zobristKey);
		cb.doMove(move4);
		System.out.println(cb.zobristKey);

		System.out.println(cb.isRepetition());
	}

}
