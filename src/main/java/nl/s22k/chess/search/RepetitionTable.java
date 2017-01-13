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

	private static final byte[] repetitionValues = new byte[MAX_TABLE_ENTRIES];

	public static void clearValues() {
		Arrays.fill(repetitionValues, (byte) 0);
	}

	private static int getZobristIndex(final long zobristKey) {
		// TODO optimal distribution??
		return (int) (zobristKey >>> POWER_2_TABLE_SHIFTS);
	}

	public static void addValue(final long zobristKey) {
		if (EngineConstants.TEST_VALUES) {
			// if (repetitionValues[getZobristIndex(zobristKey)] == 1) {
			// System.out.println("Adding a move that is already a repetition. Index-collision which is OK?");
			// }
		}
		repetitionValues[getZobristIndex(zobristKey)]++;
	}

	public static void removeValue(final long zobristKey) {
		if (EngineConstants.TEST_VALUES) {
			if (repetitionValues[getZobristIndex(zobristKey)] == 0) {
				System.out.println("Removing move from repetitiontable that has not been added");
			}
		}
		repetitionValues[getZobristIndex(zobristKey)]--;
	}

	public static boolean isRepetition(final ChessBoard cb) {
		if (repetitionValues[getZobristIndex(cb.zobristKey)] > 1) {
			if (Statistics.ENABLED) {
				Statistics.repetitionTests++;
			}

			for (int i = cb.moveCounter - 2; i > 1; i -= 2) {
				// TODO if move was an attacking-move or pawn move, no repetition!
				if (cb.zobristKey == cb.zobristKeyHistory[i]) {
					return true;
				}
			}
		}
		return false;
	}

}
