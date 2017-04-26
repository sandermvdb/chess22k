package nl.s22k.chess.unittests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;

public class ChessBoardTest {

	@BeforeClass
	public static void init() {
		MagicUtil.init();
	}

	@Test
	public void fenEPTest() {
		System.out.println("fenEPTest");
		ChessBoard cb = ChessBoardUtil.getNewCB("rnbqkbnr/ppppppp1/8/8/6Pp/8/PPPPPP1P/RNBQKBNR b KQkq g4 0 1");
		MoveGenerator.generateAttacks(cb);
		while (MoveList.hasNext()) {
			int move = MoveList.next();
			if (MoveUtil.getMoveType(move) == MoveUtil.EP) {
				System.out.println("EP-move foud");
			}
		}
	}

	@Test
	public void doubleKingMoveTest() {
		System.out.println("doubleKingMoveTest");
		ChessBoard cb = ChessBoardUtil.getNewCB("8/4R1k1/1p3n2/1q6/7P/8/p4K2/7R b - - 1 34");
		cb.doMove(MoveUtil.createMove(49, 58, ChessConstants.KING));
		cb.doMove(MoveUtil.createMove(10, 11, ChessConstants.KING));
	}

	@Test
	public void testWhitePromotion() {
		System.out.println("testWhitePromotion");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("rnbqkbn1/ppppppPp/8/8/8/8/PPPPPP1P/RNBQKBNR w - - 0 1");
		int move = MoveUtil.createPromotionMove(MoveUtil.PROMOTION_Q, 49, 56);
		chessBoard.doMove(move);
	}

	@Test
	public void testBlackPromotion() {
		System.out.println("testBlackPromotion");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("rnbqkbnr/pppppppp/8/8/8/8/PpPPPPPP/4KBNR b - - 0 1");
		int move = MoveUtil.createPromotionMove(MoveUtil.PROMOTION_Q, 14, 7);
		chessBoard.doMove(move);
	}

	@Test
	public void testAttackedPromotion() {
		System.out.println("testAttackedPromotion");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("rnbqkbnr/ppppppPp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1");
		int move = MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_Q, 49, 56, ChessConstants.ROOK);
		chessBoard.doMove(move);
	}

	@Test
	public void testWhiteEP() {
		System.out.println("testWhiteEP");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		int move = MoveUtil.createMove(9, 33, ChessConstants.PAWN);
		chessBoard.doMove(move);
		int move2 = MoveUtil.createMove(48, 32, ChessConstants.PAWN);
		chessBoard.doMove(move2);
		MoveGenerator.generateAttacks(chessBoard);
		while (MoveList.hasNext()) {
			int epMove = MoveList.next();
			if (MoveUtil.getMoveType(move) == MoveUtil.EP) {
				System.out.println("EP-move found");
				chessBoard.doMove(epMove);
				chessBoard.undoMove(epMove);
			}
		}
	}

	@Test
	public void testBlackEP() {
		System.out.println("testBlackEP");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		int move = MoveUtil.createMove(15, 23, ChessConstants.PAWN);
		chessBoard.doMove(move);
		int move2 = MoveUtil.createMove(48, 24, ChessConstants.PAWN);
		chessBoard.doMove(move2);
		int move3 = MoveUtil.createMove(9, 25, ChessConstants.PAWN);
		chessBoard.doMove(move3);
		MoveGenerator.generateMoves(chessBoard);
		while (MoveList.hasNext()) {
			int epMove = MoveList.next();
			if (MoveUtil.getMoveType(move) == MoveUtil.EP) {
				System.out.println("EP-move found");
				chessBoard.doMove(epMove);
				chessBoard.undoMove(epMove);
			}
		}
	}

	@Test
	public void testWhiteKingCastling() {
		System.out.println("testWhiteKingCastling");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQK2R w KQkq - 0 1");
		MoveGenerator.generateMoves(chessBoard);
		while (MoveList.hasNext()) {
			int castlingMove = MoveList.next();
			if (MoveUtil.getMoveType(castlingMove) == MoveUtil.CASTLING) {
				System.out.println("Castling-move found");
				chessBoard.doMove(castlingMove);
				chessBoard.undoMove(castlingMove);
			}
		}
	}

	@Test
	public void testWhiteQueenCastling() {
		System.out.println("testWhiteQueenCastling");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R3KBNR w KQkq - 0 1");
		MoveGenerator.generateMoves(chessBoard);
		while (MoveList.hasNext()) {
			int castlingMove = MoveList.next();
			if (MoveUtil.getMoveType(castlingMove) == MoveUtil.CASTLING) {
				System.out.println("Castling-move found");
				chessBoard.doMove(castlingMove);
				chessBoard.undoMove(castlingMove);
			}
		}
	}

	@Test
	public void testBlackKingCastling() {
		System.out.println("testBlackKingCastling");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("rnbqk2r/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1");
		MoveGenerator.generateMoves(chessBoard);
		while (MoveList.hasNext()) {
			int castlingMove = MoveList.next();
			if (MoveUtil.getMoveType(castlingMove) == MoveUtil.CASTLING) {
				System.out.println("Castling-move found");
				chessBoard.doMove(castlingMove);
				chessBoard.undoMove(castlingMove);
			}
		}
	}

	@Test
	public void testBlackQueenCastling() {
		System.out.println("testBlackQueenCastling");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("r3kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1");
		MoveGenerator.generateMoves(chessBoard);
		while (MoveList.hasNext()) {
			int castlingMove = MoveList.next();
			if (MoveUtil.getMoveType(castlingMove) == MoveUtil.CASTLING) {
				System.out.println("Castling-move found");
				chessBoard.doMove(castlingMove);
				chessBoard.undoMove(castlingMove);
			}
		}
	}

	@Test
	public void testBothCastling() {
		System.out.println("testBothCastling");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("rnbqk2r/pppppppp/8/8/8/8/PPPPPPPP/R3KBNR w KQkq - 0 1");
		MoveGenerator.generateMoves(chessBoard);
		while (MoveList.hasNext()) {
			int castlingMove = MoveList.next();
			if (MoveUtil.getMoveType(castlingMove) == MoveUtil.CASTLING) {
				System.out.println("Castling-move found");
				chessBoard.doMove(castlingMove);
				chessBoard.undoMove(castlingMove);
				chessBoard.doMove(castlingMove);
			}
		}

		MoveGenerator.generateMoves(chessBoard);
		while (MoveList.hasNext()) {
			int castlingMove = MoveList.next();
			if (MoveUtil.getMoveType(castlingMove) == MoveUtil.CASTLING) {
				System.out.println("2nd castling-move found");
				chessBoard.doMove(castlingMove);
				chessBoard.undoMove(castlingMove);
			}
		}
	}

	@Test
	public void testRookHit() {
		System.out.println("testRookHit");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("rnbqk2r/pppppppp/6N1/8/8/8/PPPPPPPP/RNBQKB1R w KQkq - 0 1");
		MoveGenerator.generateAttacks(chessBoard);
		while (MoveList.hasNext()) {
			int attackMove = MoveList.next();
			if (MoveUtil.getFromIndex(attackMove) == 41 && MoveUtil.getToIndex(attackMove) == 56) {
				System.out.println("Attack-move found");
				chessBoard.doMove(attackMove);
				chessBoard.undoMove(attackMove);
				chessBoard.doMove(attackMove);
			}
		}

		MoveGenerator.generateMoves(chessBoard);
		while (MoveList.hasNext()) {
			int castlingMove = MoveList.next();
			if (MoveUtil.getMoveType(castlingMove) == MoveUtil.CASTLING) {
				System.out.println("Castling-move found");
				chessBoard.doMove(castlingMove);
				chessBoard.undoMove(castlingMove);
			}
		}
	}

}
