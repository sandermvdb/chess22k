package nl.s22k.chess.move;

import java.util.Arrays;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.SEEUtil;

public final class MoveList {

	private static final int[] moves = new int[1024];
	private static final int[] nextToGenerate = new int[EngineConstants.MAX_PLIES + EngineConstants.PLIES_EXTENDED];
	private static final int[] nextToMove = new int[EngineConstants.MAX_PLIES + EngineConstants.PLIES_EXTENDED];
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

	public static int previous() {
		return moves[nextToMove[currentPly] - 1];
	}

	public static boolean hasNext() {
		return nextToGenerate[currentPly] != nextToMove[currentPly];
	}

	public static void skipMoves() {
		nextToMove[currentPly] = nextToGenerate[currentPly];
	}

	public static void addMove(final int move) {

		if (EngineConstants.ASSERT) {
			assert MoveUtil.getCleanMove(move) == move : "Adding move with score to move-list!";
		}

		moves[nextToGenerate[currentPly]++] = move;
	}

	/**
	 * Required by QPerft
	 */
	public static int movesLeft() {
		return nextToGenerate[currentPly] - nextToMove[currentPly];
	}

	public static void setSeeScores(final ChessBoard cb) {
		// TODO
		/*
		 * Regarding move-ordering: for all captures of type M x N, if M is equal to or less valuable than N, just use
		 * val(N) - val(M) for the value (assume your piece will be recaptured)
		 */

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

	public static String getMovesAsString() {
		StringBuilder sb = new StringBuilder();
		for (int j = nextToMove[currentPly]; j < nextToGenerate[currentPly]; j++) {
			sb.append(new MoveWrapper(moves[j]) + ", ");
		}
		return sb.toString();
	}

}
