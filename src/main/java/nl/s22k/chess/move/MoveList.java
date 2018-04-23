package nl.s22k.chess.move;

import static org.junit.Assert.assertEquals;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.search.HeuristicUtil;

public final class MoveList {

	private static final int[] moves = new int[1500];
	private static final int[] nextToGenerate = new int[EngineConstants.MAX_PLIES * 2];
	private static final int[] nextToMove = new int[EngineConstants.MAX_PLIES * 2];
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

	public static int getNextScore() {
		return MoveUtil.getScore(moves[nextToMove[currentPly]]);
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
			assertEquals(MoveUtil.getCleanMove(move), move);
		}

		moves[nextToGenerate[currentPly]++] = move;
	}

	/**
	 * Required by QPerft
	 */
	public static int movesLeft() {
		return nextToGenerate[currentPly] - nextToMove[currentPly];
	}

	public static void setMVVLVAScores(final ChessBoard cb) {
		for (int j = nextToMove[currentPly]; j < nextToGenerate[currentPly]; j++) {
			moves[j] = MoveUtil.setScoredMove(moves[j], MoveUtil.getAttackedPieceIndex(moves[j]) * 6 - MoveUtil.getSourcePieceIndex(moves[j]));
		}
	}

	public static void setHHScores(final ChessBoard cb) {
		if (EngineConstants.ENABLE_HISTORY_HEURISTIC) {
			for (int j = nextToMove[currentPly]; j < nextToGenerate[currentPly]; j++) {
				moves[j] = MoveUtil.setScoredMove(moves[j], HeuristicUtil.getHHScore(cb.colorToMove, MoveUtil.getFromToIndex(moves[j])));
			}
		}
	}

	public static void sort() {
		final int left = nextToMove[currentPly];
		for (int i = left, j = i; i < nextToGenerate[currentPly] - 1; j = ++i) {
			final int ai = moves[i + 1];
			while (ai > moves[j]) {
				moves[j + 1] = moves[j];
				if (j-- == left) {
					break;
				}
			}
			moves[j + 1] = ai;
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
