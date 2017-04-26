package nl.s22k.chess.move;

import java.util.Arrays;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.SEEUtil;

public final class MoveList {

	private static final int[] moves = new int[1024];
	private static final int[] nextToGenerate = new int[ChessConstants.MAX_PLIES + 8];
	private static final int[] nextToMove = new int[ChessConstants.MAX_PLIES + 8];
	private static int currentPly;

	public static void startPly() {
		nextToGenerate[currentPly + 1] = nextToGenerate[currentPly];
		nextToMove[currentPly + 1] = nextToGenerate[currentPly];
		currentPly++;
	}

	public static void endPly() {
		currentPly--;
	}

	public static int getIndex() {
		return nextToMove[currentPly];
	}

	public static void setIndex(int index) {
		nextToMove[currentPly] = index;
	}

	public static int next() {
		return moves[nextToMove[currentPly]++];
	}

	public static boolean hasNext() {
		return nextToGenerate[currentPly] != nextToMove[currentPly];
	}

	public static void skipMoves() {
		nextToMove[currentPly] = nextToGenerate[currentPly];
	}

	public static void addMove(final int cleanMove) {

		if (EngineConstants.ASSERT) {
			assert MoveUtil.getCleanMove(cleanMove) == cleanMove : "addQuietMove: Adding move with score to move-list!";
		}

		moves[nextToGenerate[currentPly]++] = cleanMove;
	}

	/**
	 * Required by QPerft
	 */
	public static int movesLeft() {
		return nextToGenerate[currentPly] - nextToMove[currentPly];
	}

	public static void setSeeScores(final ChessBoard cb) {
		for (int j = nextToMove[currentPly]; j < nextToGenerate[currentPly]; j++) {
			moves[j] = MoveUtil.setSeeMove(moves[j], SEEUtil.getSeeCaptureScore(cb, moves[j]));
		}
	}

	public static void setHHScores(final ChessBoard cb) {
		if (EngineConstants.ENABLE_HISTORY_HEURISTIC) {
			for (int j = nextToMove[currentPly]; j < nextToGenerate[currentPly]; j++) {
				moves[j] = MoveUtil.setHHMove(moves[j], cb.colorToMove);
			}
		}
	}

	public static void sort() {
		Arrays.sort(moves, nextToMove[currentPly], nextToGenerate[currentPly]);
		reverse();
	}

	public static void reverse() {
		int i = nextToMove[currentPly];
		int j = nextToGenerate[currentPly] - 1;
		int tmp;
		while (j > i) {
			tmp = moves[j];
			moves[j--] = moves[i];
			moves[i++] = tmp;
		}
	}

}
