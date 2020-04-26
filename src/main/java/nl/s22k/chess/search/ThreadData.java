package nl.s22k.chess.search;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.WHITE;

import java.util.Arrays;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.ChessConstants.ScoreType;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.engine.UciOptions;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.move.PVUtil;

public final class ThreadData {

	private static ThreadData[] instances;
	static {
		initInstances(UciOptions.threadCount);
	}

	public int threadNumber = 0;
	private int ply;

	public int[] pv;
	public ScoreType scoreType;
	public int bestScore;
	public int depth;

	private final int[] nextToGenerate = new int[EngineConstants.MAX_PLIES * 2];
	private final int[] nextToMove = new int[EngineConstants.MAX_PLIES * 2];
	private final int[] KILLER_MOVE_1 = new int[EngineConstants.MAX_PLIES * 2];
	private final int[] KILLER_MOVE_2 = new int[EngineConstants.MAX_PLIES * 2];

	private final int[] moves = new int[1500];
	private final int[] moveScores = new int[1500];

	private final int[][][] COUNTER_MOVES = new int[2][7][64];

	private final int[][] HH_MOVES = new int[2][64 * 64];
	private final int[][] BF_MOVES = new int[2][64 * 64];

	// keys, scores
	public final int[] evalCache = new int[(1 << EngineConstants.POWER_2_EVAL_ENTRIES) * 2];
	// keys, passedPawnsOutposts, scores
	public final long[] pawnCache = new long[(1 << EngineConstants.POWER_2_PAWN_EVAL_ENTRIES) * 3];
	// keys, scores
	public final int[] materialCache = new int[(1 << EngineConstants.POWER_2_MATERIAL_ENTRIES) * 2];

	public static ThreadData getInstance(int instanceNumber) {
		return instances[instanceNumber];
	}

	public static void initInstances(int nrOfInstances) {
		instances = new ThreadData[nrOfInstances];
		for (int i = 0; i < instances.length; i++) {
			instances[i] = new ThreadData(i);
		}
	}

	public ThreadData(int threadNumber) {
		clearHistoryHeuristics();
		this.threadNumber = threadNumber;
		if (threadNumber == 0) {
			pv = new int[EngineConstants.PV_LENGTH];
		}
	}

	public void setBestMove(final ChessBoard cb, final int bestMove, final int alpha, final int beta, final int bestScore, final int depth) {

		if (threadNumber != 0) {
			return;
		}

		this.bestScore = bestScore;
		this.depth = depth;
		if (bestScore <= alpha) {
			scoreType = ScoreType.UPPER;
		} else if (bestScore >= beta) {
			scoreType = ScoreType.LOWER;
		} else {
			scoreType = ScoreType.EXACT;
		}

		PVUtil.set(cb, pv, bestMove);
	}

	public void initPV(final ChessBoard cb) {
		final long ttValue = TTUtil.getValue(cb.zobristKey);
		if (ttValue == 0 || TTUtil.getMove(ttValue) == 0) {
			Arrays.fill(pv, 0);
		} else {
			setBestMove(cb, TTUtil.getMove(ttValue), Util.SHORT_MIN, Util.SHORT_MAX, TTUtil.getScore(ttValue, 0), TTUtil.getDepth(ttValue));
		}
	}

	public int getBestMove() {
		return pv[0];
	}

	public int getPonderMove() {
		return pv[1];
	}

	public void clearCaches() {
		Arrays.fill(evalCache, 0);
		Arrays.fill(pawnCache, 0);
		Arrays.fill(materialCache, 0);
	}

	public void clearHistoryHeuristics() {
		Arrays.fill(HH_MOVES[WHITE], 1);
		Arrays.fill(HH_MOVES[BLACK], 1);
		Arrays.fill(BF_MOVES[WHITE], 1);
		Arrays.fill(BF_MOVES[BLACK], 1);
	}

	public void addHHValue(final int color, final int move, final int depth) {
		HH_MOVES[color][MoveUtil.getFromToIndex(move)] += depth * depth;
		if (EngineConstants.ASSERT) {
			Assert.isTrue(HH_MOVES[color][MoveUtil.getFromToIndex(move)] >= 0);
		}
	}

	public void addBFValue(final int color, final int move, final int depth) {
		BF_MOVES[color][MoveUtil.getFromToIndex(move)] += depth * depth;
		if (EngineConstants.ASSERT) {
			Assert.isTrue(BF_MOVES[color][MoveUtil.getFromToIndex(move)] >= 0);
		}
	}

	public int getHHScore(final int color, final int fromToIndex) {
		if (!EngineConstants.ENABLE_HISTORY_HEURISTIC) {
			return 1;
		}
		return 100 * HH_MOVES[color][fromToIndex] / BF_MOVES[color][fromToIndex];
	}

	public void addKillerMove(final int move, final int ply) {
		if (EngineConstants.ENABLE_KILLER_MOVES) {
			if (KILLER_MOVE_1[ply] != move) {
				KILLER_MOVE_2[ply] = KILLER_MOVE_1[ply];
				KILLER_MOVE_1[ply] = move;
			}
		}
	}

	public void addCounterMove(final int color, final int parentMove, final int counterMove) {
		if (EngineConstants.ENABLE_COUNTER_MOVES) {
			COUNTER_MOVES[color][MoveUtil.getSourcePieceIndex(parentMove)][MoveUtil.getToIndex(parentMove)] = counterMove;
		}
	}

	public int getCounter(final int color, final int parentMove) {
		return COUNTER_MOVES[color][MoveUtil.getSourcePieceIndex(parentMove)][MoveUtil.getToIndex(parentMove)];
	}

	public int getKiller1(final int ply) {
		return KILLER_MOVE_1[ply];
	}

	public int getKiller2(final int ply) {
		return KILLER_MOVE_2[ply];
	}

	public void startPly() {
		nextToGenerate[ply + 1] = nextToGenerate[ply];
		nextToMove[ply + 1] = nextToGenerate[ply];
		ply++;
	}

	public void endPly() {
		ply--;
	}

	public int next() {
		return moves[nextToMove[ply]++];
	}

	public int getMoveScore() {
		return moveScores[nextToMove[ply] - 1];
	}

	public int previous() {
		return moves[nextToMove[ply] - 1];
	}

	public boolean hasNext() {
		return nextToGenerate[ply] != nextToMove[ply];
	}

	public void addMove(final int move) {
		moves[nextToGenerate[ply]++] = move;
	}

	public void setMVVLVAScores() {
		for (int j = nextToMove[ply]; j < nextToGenerate[ply]; j++) {
			moveScores[j] = MoveUtil.getAttackedPieceIndex(moves[j]) * 6 - MoveUtil.getSourcePieceIndex(moves[j]);
			if (MoveUtil.getMoveType(moves[j]) == MoveUtil.TYPE_PROMOTION_Q) {
				moveScores[j] += ChessConstants.QUEEN * 6;
			}
		}
	}

	public void setHHScores(final int colorToMove) {
		for (int j = nextToMove[ply]; j < nextToGenerate[ply]; j++) {
			moveScores[j] = getHHScore(colorToMove, MoveUtil.getFromToIndex(moves[j]));
		}
	}

	public void sort() {
		final int left = nextToMove[ply];
		for (int i = left, j = i; i < nextToGenerate[ply] - 1; j = ++i) {
			final int score = moveScores[i + 1];
			final int move = moves[i + 1];
			while (score > moveScores[j]) {
				moveScores[j + 1] = moveScores[j];
				moves[j + 1] = moves[j];
				if (j-- == left) {
					break;
				}
			}
			moveScores[j + 1] = score;
			moves[j + 1] = move;
		}
	}

	public String getMovesAsString() {
		StringBuilder sb = new StringBuilder();
		for (int j = nextToMove[ply]; j < nextToGenerate[ply]; j++) {
			sb.append(new MoveWrapper(moves[j]) + ", ");
		}
		return sb.toString();
	}

}
