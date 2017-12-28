package nl.s22k.chess.search;

import java.util.Arrays;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class RepetitionTable {

	// TODO alternative: use a very small table and if key is occupied, search at index + 1
	// TODO clear table when a piece is hit (when undoing moves this gives a problem?!)

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.REPETITION_TABLE_ENTRIES;
	public static int MAX_TABLE_ENTRIES = (int) Util.POWER_LOOKUP[EngineConstants.REPETITION_TABLE_ENTRIES];
	private static final byte ZERO_BYTE = 0;

	private static final byte[] repetitionValues = new byte[MAX_TABLE_ENTRIES];

	public static void clearValues() {
		Arrays.fill(repetitionValues, ZERO_BYTE);
	}

	private static int getZobristIndex(final long zobristKey) {
		// TODO optimal distribution??
		return (int) (zobristKey >>> POWER_2_TABLE_SHIFTS);
	}

	public static void addValue(final long zobristKey) {
		if (EngineConstants.ASSERT) {
			if (repetitionValues[getZobristIndex(zobristKey)] > 1) {
				System.out.println("Adding a move that is already a repetition. Index-collision which is OK? index : " + getZobristIndex(zobristKey));
			}
		}
		repetitionValues[getZobristIndex(zobristKey)]++;
	}

	public static void removeValue(final long zobristKey) {
		if (EngineConstants.ASSERT) {
			assert repetitionValues[getZobristIndex(zobristKey)] > 0 : "Removing move from repetitiontable that has not been added";
		}
		repetitionValues[getZobristIndex(zobristKey)]--;
	}

	public static boolean isRepetition(final ChessBoard cb) {

		if (!EngineConstants.ENABLE_REPETITION_TABLE) {
			return false;
		}

		// TODO 1 repetition is not a draw
		if (repetitionValues[getZobristIndex(cb.zobristKey)] > 0) {
			if (Statistics.ENABLED) {
				Statistics.repetitionTests++;
			}

			// TODO same position but other side to move is also a draw?
			for (int i = cb.moveCounter - 2; i >= 0 && i > cb.moveCounter - 50; i -= 2) {
				// TODO if move was an attacking-move or pawn move, no repetition!
				if (cb.zobristKey == cb.zobristKeyHistory[i]) {
					if (Statistics.ENABLED) {
						Statistics.repetitions++;
					}
					return true;
				}
			}
		}
		return false;
	}

}
