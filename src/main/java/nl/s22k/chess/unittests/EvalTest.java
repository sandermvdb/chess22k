package nl.s22k.chess.unittests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveUtil;

public class EvalTest {

	@BeforeClass
	public static void init() {
		MagicUtil.init();
	}

	@Test
	public void test() {
		ChessBoard cb = ChessBoardUtil.getNewCB("rn1qkbnr/pp1b3p/4ppp1/3p3Q/3p4/2PBP2P/PP3PP1/RNB2KNR w kq - 0 1 ");
		EvalUtil.calculateScore(cb);
	}

	@Test
	public void insufficientMaterialTest() {
		System.out.println("Insufficient material test");
		ChessBoard cb = ChessBoardUtil.getNewCB("8/8/8/8/K1k5/8/N7/8 w - - 16 1 ");
		System.out.println(EvalUtil.calculateScore(cb));
		cb.changeSideToMove();
		System.out.println(EvalUtil.calculateScore(cb));

		cb = ChessBoardUtil.getNewCB("8/8/8/8/K1k5/8/B7/8 w - - 16 1 ");
		System.out.println(EvalUtil.calculateScore(cb));
		cb.changeSideToMove();
		System.out.println(EvalUtil.calculateScore(cb));

		cb = ChessBoardUtil.getNewCB("8/8/8/8/K1k5/8/8/8 w - - 16 1 ");
		System.out.println(EvalUtil.calculateScore(cb));
		cb.changeSideToMove();
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void startPositionTest() {
		System.out.println("Start position test");
		ChessBoard cb = ChessBoardUtil.getNewCB();
		System.out.println(EvalUtil.calculateScore(cb));
		cb.changeSideToMove();
		System.out.println(EvalUtil.calculateScore(cb));
	}

	public void endTest() {
		System.out.println("End test");
		ChessBoard cb = ChessBoardUtil.getNewCB("4k3/4p3/8/8/8/8/4P3/4K3 w - - 0 1 ");
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void moveQueenTest() {
		System.out.println("Queen moved test");
		ChessBoard cb = ChessBoardUtil.getNewCB("3q4k78888K73Q4 w - -");
		System.out.println(EvalUtil.calculateScore(cb));
		int move = MoveUtil.createMove(4, 3, ChessConstants.QUEEN);
		cb.doMove(move);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void castlingTest() {
		System.out.println("Castling test");
		ChessBoard cb = ChessBoardUtil.getNewCB("3qk2r8888883QK2R w - - 20 20");
		System.out.println(EvalUtil.calculateScore(cb));
		int move = MoveUtil.createCastlingMove(3, 1);
		cb.doMove(move);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void doubledPawnTest() {
		System.out.println("Doubled pawn test");
		ChessBoard cb = ChessBoardUtil.getNewCB();
		System.out.println(EvalUtil.calculateScore(cb));
		int move = MoveUtil.createMove(10, 17, ChessConstants.PAWN);
		cb.doMove(move);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void moveRookTest() {
		System.out.println("Rook moved test");
		ChessBoard cb = ChessBoardUtil.getNewCB();
		System.out.println(EvalUtil.calculateScore(cb));
		int move = MoveUtil.createMove(0, 16, ChessConstants.ROOK);
		cb.doMove(move);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void moveKingTest() {
		System.out.println("King moved test");
		ChessBoard cb = ChessBoardUtil.getNewCB();
		System.out.println(EvalUtil.calculateScore(cb));
		int move = MoveUtil.createMove(3, 19, ChessConstants.KING);
		cb.doMove(move);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void passedPawnTest() {
		System.out.println("Pawn passed test");
		ChessBoard cb = ChessBoardUtil.getNewCB("4k3/7P/pppppppp/8/8/PPPPPPP1/8/4K3 w - -");
		System.out.println(EvalUtil.calculateScore(cb));
		cb = ChessBoardUtil.getNewCB("4k3/8/ppppppp1/8/8/PPPPPPPP/7p/4K3 b - -");
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void kingCastledTest() {
		System.out.println("King castled test");
		ChessBoard cb = ChessBoardUtil.getNewCB("1k6/ppp5/8/8/8/8/8/4K3 b - -");
		System.out.println(EvalUtil.calculateScore(cb));
		cb = ChessBoardUtil.getNewCB("6k1/5ppp/8/8/8/8/8/4K3 b - -");
		System.out.println(EvalUtil.calculateScore(cb));
		cb = ChessBoardUtil.getNewCB("4k3/8/8/8/8/8/PPP5/1K6 b - -");
		System.out.println(EvalUtil.calculateScore(cb));
		cb = ChessBoardUtil.getNewCB("4k3/8/8/8/8/8/5PPP/6K1 b - -");
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void highScoreTest() {
		System.out.println("High score test");
		ChessBoard cb = ChessBoardUtil.getNewCB("r5k1/5ppp/1rp1B3/2b1p3/P7/4P3/RB1n2PP/3R2K1 w - - 1 23 ");
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void isolatedPawnTest() {
		System.out.println("Isolated pawn test");
		ChessBoard cb = ChessBoardUtil.getNewCB("4k3/ppppppp1/8/8/8/8/PPPPPPP1/4K3 w - -");
		System.out.println(EvalUtil.calculateScore(cb));
		int move = MoveUtil.createMove(9, 8, ChessConstants.PAWN);
		cb.doMove(move);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void kingPawnShieldKingSideWhite() {
		ChessBoard cb = ChessBoardUtil.getNewCB("r1bq1rk1/ppp1bppp/2np4/4p3/4P3/2NP4/PPP1BPPP/R1BQ1RK1 w - - 4 1 ");
		// System.out.println(EvalUtil.calculateScore(cb));

		int move1 = MoveUtil.createMove(8, 24, ChessConstants.PAWN);
		cb.doMove(move1);
		System.out.println(EvalUtil.calculateScore(cb));

		cb.undoMove(move1);
		int move2 = MoveUtil.createMove(9, 25, ChessConstants.PAWN);
		cb.doMove(move2);
		System.out.println(EvalUtil.calculateScore(cb));

		cb.undoMove(move2);
		int move3 = MoveUtil.createMove(10, 26, ChessConstants.PAWN);
		cb.doMove(move3);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void kingPawnShieldKingSideBlack() {
		ChessBoard cb = ChessBoardUtil.getNewCB("r1bq1rk1/ppp1bppp/2np4/4p3/4P3/2NP4/PPP1BPPP/R1BQ1RK1 b - - 4 1 ");
		System.out.println(EvalUtil.calculateScore(cb));

		int move1 = MoveUtil.createMove(48, 32, ChessConstants.PAWN);
		cb.doMove(move1);
		System.out.println(EvalUtil.calculateScore(cb));

		cb.undoMove(move1);
		int move2 = MoveUtil.createMove(49, 33, ChessConstants.PAWN);
		cb.doMove(move2);
		System.out.println(EvalUtil.calculateScore(cb));

		cb.undoMove(move2);
		int move3 = MoveUtil.createMove(50, 34, ChessConstants.PAWN);
		cb.doMove(move3);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void kingPawnShieldQueenSideWhite() {
		ChessBoard cb = ChessBoardUtil.getNewCB("2kr1bnr/pppq1ppp/3pb3/4p3/4P3/3PB3/PPPQ1PPP/2KR1BNR w - - 6 1 ");
		System.out.println(EvalUtil.calculateScore(cb));

		int move1 = MoveUtil.createMove(15, 31, ChessConstants.PAWN);
		cb.doMove(move1);
		System.out.println(EvalUtil.calculateScore(cb));
		cb.undoMove(move1);
		int move2 = MoveUtil.createMove(14, 30, ChessConstants.PAWN);
		cb.doMove(move2);
		System.out.println(EvalUtil.calculateScore(cb));
		cb.undoMove(move2);
		int move3 = MoveUtil.createMove(13, 29, ChessConstants.PAWN);
		cb.doMove(move3);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void kingPawnShieldQueenSideBlack() {
		ChessBoard cb = ChessBoardUtil.getNewCB("2kr1bnr/pppq1ppp/3pb3/4p3/4P3/3PB3/PPPQ1PPP/2KR1BNR b - - 6 1 ");
		System.out.println(EvalUtil.calculateScore(cb));

		int move1 = MoveUtil.createMove(55, 39, ChessConstants.PAWN);
		cb.doMove(move1);
		System.out.println(EvalUtil.calculateScore(cb));
		cb.undoMove(move1);
		int move2 = MoveUtil.createMove(54, 38, ChessConstants.PAWN);
		cb.doMove(move2);
		System.out.println(EvalUtil.calculateScore(cb));
		cb.undoMove(move2);
		int move3 = MoveUtil.createMove(53, 37, ChessConstants.PAWN);
		cb.doMove(move3);
		System.out.println(EvalUtil.calculateScore(cb));
	}

	@Test
	public void psqtTest() {
		ChessBoard cb = ChessBoardUtil.getNewCB("r1bq1bnr/3k4/1pnpppQp/p7/2Pp1PP1/N2K3B/PP2P2P/1RB3NR w - - 2 14");
		System.out.println(cb.psqtScore);
		System.out.println(EvalUtil.calculatePositionScores(cb));

		int move = MoveUtil.createMove(6, 7, ChessConstants.ROOK);
		cb.doMove(move);
		System.out.println(cb.psqtScore);
		System.out.println(EvalUtil.calculatePositionScores(cb));
		cb.undoMove(move);
		System.out.println(cb.psqtScore);
		System.out.println(EvalUtil.calculatePositionScores(cb));

		move = MoveUtil.createAttackMove(41, 57, ChessConstants.QUEEN, ChessConstants.NIGHT);
		cb.doMove(move);
		System.out.println(cb.psqtScore);
		System.out.println(EvalUtil.calculatePositionScores(cb));
		cb.undoMove(move);
		System.out.println(cb.psqtScore);
		System.out.println(EvalUtil.calculatePositionScores(cb));
	}

	@Test
	public void passedPawnWithNightTest() {
		ChessBoard cb = ChessBoardUtil.getNewCB("8/8/6p1/4Np1n/N1K2n1k/6p1/8/8 b");
		System.out.println(EvalUtil.calculateScore(cb));
	}
}
