package nl.s22k.chess.maintests;

import java.util.ArrayList;
import java.util.List;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.MoveWrapper;

public class EPD {

	/** bm or am */
	private boolean isBestMove;
	private List<String> moveStrings = new ArrayList<String>();
	private String fen;
	private String id;

	// 3r1k2/4npp1/1ppr3p/p6P/P2PPPP1/1NR5/5K2/2R5 w - - bm d5; id \"BK.02\";
	// r1bqk1nr/pppnbppp/3p4/8/2BNP3/8/PPP2PPP/RNBQK2R w KQkq - bm Bxf7+; id \"CCR12\";
	// 6k1/p3q2p/1nr3pB/8/3Q1P2/6P1/PP5P/3R2K1 b - - bm Rd6; id \"position12\";
	public EPD(String epdString) {

		String[] tokens = epdString.split(";");

		// fen
		String[] fenToken = tokens[0].split(" ");
		fen = fenToken[0] + " " + fenToken[1] + " " + fenToken[2] + " " + fenToken[3];
		isBestMove = fenToken[4].equals("bm");

		// there could be multiple best-moves
		for (int i = 5; i < fenToken.length; i++) {
			// remove check indication
			String moveString = fenToken[i];
			if (moveString.endsWith("+")) {
				moveString = moveString.replace("+", "");
			}

			// remove capture indication
			if (moveString.contains("x")) {
				moveString = moveString.replace("x", "");
			}
			moveStrings.add(moveString);
		}

		// id
		String idToken = tokens[1];
		id = idToken.split(" ")[2].replaceAll("\"", "");
	}

	public boolean moveEquals(MoveWrapper bestMove) {
		for (String moveString : moveStrings) {
			if (moveEquals(moveString, bestMove)) {
				return true;
			}
		}
		return false;
	}

	private boolean moveEquals(String moveString, MoveWrapper bestMove) {

		int move = bestMove.move;
		int sourceIndex = MoveUtil.getSourcePieceIndex(move);
		if (moveString.length() == 2) {
			// d5, g6
			// must be a pawn
			return sourceIndex == ChessConstants.PAWN && moveString.substring(0, 1).equals(bestMove.toFile + "")
					&& moveString.substring(1, 2).equals(bestMove.toRank + "");
		}
		if (moveString.length() == 3) {
			if (moveString.substring(0, 1).toLowerCase().equals(moveString.substring(0, 1))) {
				// ef6
				return sourceIndex == ChessConstants.PAWN && moveString.substring(0, 1).equals(bestMove.fromFile + "")
						&& moveString.substring(1, 2).equals(bestMove.toFile + "") && moveString.substring(2, 3).equals(bestMove.toRank + "");
			} else {
				// Bf5, Qd2
				return moveString.substring(0, 1).equals(ChessConstants.FEN_WHITE_PIECES[sourceIndex])
						&& moveString.substring(1, 2).equals(bestMove.toFile + "") && moveString.substring(2, 3).equals(bestMove.toRank + "");
			}
		}

		if (moveString.length() == 4) {
			// Rfb8, Ndb5
			return moveString.substring(0, 1).equals(ChessConstants.FEN_WHITE_PIECES[sourceIndex]) && moveString.substring(1, 2).equals(bestMove.fromFile + "")
					&& moveString.substring(2, 3).equals(bestMove.toFile + "") && moveString.substring(3, 4).equals(bestMove.toRank + "");
		}
		throw new RuntimeException("Unknown move string: " + moveString);

	}

	public String getId() {
		return id;
	}

	public boolean isBestMove() {
		return isBestMove;
	}

	public String getFen() {
		return fen;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String moveString : moveStrings) {
			sb.append(moveString).append(" ");
		}
		return sb.toString();
	}

}
