package nl.s22k.chess.move;

import java.util.Arrays;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.search.TTUtil;

public class PVUtil {

	public static void set(final ChessBoard cb, final int[] moves, final int bestMove) {
		Arrays.fill(moves, 0);
		moves[0] = bestMove;
		cb.doMove(bestMove);

		for (int i = 1; i < moves.length; i++) {
			long ttValue = TTUtil.getValue(cb.zobristKey);
			if (ttValue == 0) {
				break;
			}
			int move = TTUtil.getMove(ttValue);
			if (move == 0) {
				break;
			}
			moves[i] = move;
			cb.doMove(move);
		}
		for (int i = moves.length - 1; i >= 0; i--) {
			if (moves[i] == 0) {
				continue;
			}
			cb.undoMove(moves[i]);
		}
	}

	public static String asString(final int[] moves) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < moves.length; i++) {
			int move = moves[i];
			if (move == 0) {
				break;
			}
			sb.append(new MoveWrapper(move)).append(" ");
		}
		return sb.toString();
	}

}
