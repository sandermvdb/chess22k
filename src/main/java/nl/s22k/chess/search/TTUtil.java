package nl.s22k.chess.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants.ScoreType;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.TreeMove;

public class TTUtil {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_TT_ENTRIES;
	public static int MAX_TABLE_ENTRIES = (int) Util.POWER_LOOKUP[EngineConstants.POWER_2_TT_ENTRIES];
	static {
		if (EngineConstants.ENABLE_BUCKETS) {
			MAX_TABLE_ENTRIES++;
		}
	}

	private static final int[] transpositionKeys = new int[MAX_TABLE_ENTRIES];
	private static final long[] transpositionValues = new long[MAX_TABLE_ENTRIES];
	public static long usageCounter;

	public static final int FLAG_EXACT = 0;
	public static final int FLAG_UPPER = 1;
	public static final int FLAG_LOWER = 2;

	public static void clearValues() {
		Arrays.fill(transpositionKeys, 0);
		Arrays.fill(transpositionValues, 0);
		usageCounter = 0;
	}

	public static long getTTValue(final long zkKey) {
		if (transpositionKeys[getZobristIndex(zkKey)] == (int) zkKey) {
			if (Statistics.ENABLED) {
				Statistics.ttHits += 1;
			}

			return transpositionValues[getZobristIndex(zkKey)];

		}
		if (Statistics.ENABLED) {
			Statistics.ttMisses += 1;
		}

		return 0;
	}

	private static int getZobristIndex(final long zobristKey) {
		// TODO optimal distribution??
		if (POWER_2_TABLE_SHIFTS == 64) {
			return 0;
		}
		return (int) (zobristKey >>> POWER_2_TABLE_SHIFTS);
	}

	public static void setBestMoveInStatistics(ChessBoard chessBoard, int depth, ScoreType scoreType) {
		long value = getTTValue(chessBoard.zobristKey);
		if (value == 0) {
			throw new RuntimeException("No best-move found!!");
		}

		int move = getMove(value);
		List<Integer> moves = new ArrayList<Integer>();
		moves.add(move);
		TreeMove bestMove = new TreeMove(move, getScore(value, 0), scoreType);

		chessBoard.doMove(move);

		value = getTTValue(chessBoard.zobristKey);

		int ply = 0;
		while (value != 0 && TTUtil.getFlag(value) == TTUtil.FLAG_EXACT && depth >= 0) {
			ply++;
			depth--;
			move = getMove(value);
			moves.add(move);
			bestMove.appendMove(new TreeMove(move, getScore(value, ply), ScoreType.EXACT));
			chessBoard.doMove(move);
			value = getTTValue(chessBoard.zobristKey);
		}
		for (int i = moves.size() - 1; i >= 0; i--) {
			chessBoard.undoMove(moves.get(i));
		}

		Statistics.bestMove = bestMove;
	}

	public static void addValue(final long zobristKey, int score, final int ply, final int depth, final int flag, final int cleanMove) {

		if (EngineConstants.TEST_VALUES) {
			if (depth < 1) {
				System.out.println("Cannot add depth < 1 to TT");
			}
			if (cleanMove == 0) {
				System.out.println("Adding empty move to TT");
			}
			if (score > Util.SHORT_MAX) {
				System.out.println("Adding score to TT > MAX");
			}
			if (score < Util.SHORT_MIN) {
				System.out.println("Adding score to TT < MIN");
			}
			if (MoveUtil.getCleanMove(cleanMove) != cleanMove) {
				System.out.println("Adding non-clean move to TT");
			}
		}

		// correct mate-score
		if (score > EvalConstants.SCORE_MATE_BOUND) {
			score += ply;
		} else if (score < -EvalConstants.SCORE_MATE_BOUND) {
			score -= ply;
		}

		if (EngineConstants.TEST_VALUES) {
			if (score > Util.SHORT_MAX) {
				System.out.println("Adding score to tt > MAX: " + score);
			} else if (score < Util.SHORT_MIN) {
				System.out.println("Adding score to tt < MIN: " + score);
			}
		}

		final int ttIndex = getZobristIndex(zobristKey);
		final long value = createValue(score, depth, flag, cleanMove);

		transpositionKeys[ttIndex] = (int) zobristKey;
		transpositionValues[ttIndex] = value;
	}

	public static int getScore(final long value, final int ply) {
		int score = (int) (value >> 48);

		// correct mate-score
		if (score > EvalConstants.SCORE_MATE_BOUND) {
			score -= ply;
		} else if (score < -EvalConstants.SCORE_MATE_BOUND) {
			score += ply;
		}

		if (EngineConstants.TEST_VALUES) {
			if (score > Util.SHORT_MAX) {
				System.out.println("Retrieving score from tt > MAX");
			} else if (score < Util.SHORT_MIN) {
				System.out.println("Retrieving score from tt < MIN");
			}
		}

		return score;
	}

	public static int getDepth(final long value) {
		return (int) (value >> 40 & 255);
	}

	public static int getFlag(final long value) {
		return (int) (value >> 32 & 255);
	}

	public static int getMove(final long value) {
		return (int) value;
	}

	// score,depth,flag,move
	// 16,8,8,32 (min = 16+8+2+20=46)
	public static long createValue(final long score, final long depth, final long flag, final int move) {
		if (EngineConstants.TEST_VALUES) {
			if (move < 0) {
				System.out.println("Adding negative move to tt");
			}
		}
		return score << 48 | depth << 40 | flag << 32 | move;
	}

}
