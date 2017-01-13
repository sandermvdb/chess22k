package nl.s22k.chess.unittests;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.ROOK;

import org.junit.Test;

import nl.s22k.chess.CastlingUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.move.MoveUtil;

public class CastlingTest {

	// 4 bits: white-king,white-queen,black-king,black-queen

	@Test
	public void testStaticValues() {
		ChessBoard cb = ChessBoardUtil.getNewCB();
		cb.castlingRights = 0;
		System.out.println(CastlingUtil.getCastlingIndexes(cb) + " 0");
		cb.castlingRights = 10;
		System.out.println(CastlingUtil.getCastlingIndexes(cb) + " 2");
		cb.castlingRights = 15;
		System.out.println(CastlingUtil.getCastlingIndexes(cb) + " 34");

		cb.colorToMove = BLACK;
		cb.castlingRights = 0;
		System.out.println(CastlingUtil.getCastlingIndexes(cb) + " 0");
		cb.castlingRights = 10;
		System.out.println(CastlingUtil.getCastlingIndexes(cb) + " 144115188075855872");
		cb.castlingRights = 15;
		System.out.println(CastlingUtil.getCastlingIndexes(cb) + " 2449958197289549824");
	}

	@Test
	public void testRooksMoved() {
		ChessBoard cb = ChessBoardUtil.getNewCB();

		int move = MoveUtil.createMove(0, 16, ROOK);
		cb.doMove(move);
		System.out.println(cb.castlingRights + " 7");

		move = MoveUtil.createMove(56, 40, ROOK);
		cb.doMove(move);
		System.out.println(cb.castlingRights + " 5");

		move = MoveUtil.createMove(7, 23, ROOK);
		cb.doMove(move);
		System.out.println(cb.castlingRights + " 1");

		move = MoveUtil.createMove(63, 47, ROOK);
		cb.doMove(move);
		System.out.println(cb.castlingRights + " 0");
	}

	@Test
	public void testKingMoved() {
		ChessBoard cb = ChessBoardUtil.getNewCB();

		int move = MoveUtil.createMove(3, 19, KING);
		cb.doMove(move);
		System.out.println(cb.castlingRights + " 3");

		move = MoveUtil.createMove(59, 43, KING);
		cb.doMove(move);
		System.out.println(cb.castlingRights + " 0");
	}

}
