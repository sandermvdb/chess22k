package nl.s22k.chess.texel;

import java.util.Arrays;

import nl.s22k.chess.ChessConstants;

public class PsqtTuningObject extends TuningObject {

	public int[][] psqtValues;

	public PsqtTuningObject(int[][] psqtValues, int step, String name, boolean showAverage, Integer... skipValues) {
		super(psqtValues[ChessConstants.WHITE], step, name, showAverage, false, skipValues);
		this.psqtValues = psqtValues;
		this.tunedValues = psqtValues[ChessConstants.WHITE].length / 2 - skipValues.length / 2;
		orgValues = new int[psqtValues[ChessConstants.WHITE].length];
		System.arraycopy(psqtValues[ChessConstants.WHITE], 0, orgValues, 0, psqtValues[ChessConstants.WHITE].length);
	}

	@Override
	public String toString() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < psqtValues[ChessConstants.WHITE].length; i++) {
				sum += psqtValues[ChessConstants.WHITE][i];
			}
			return name + ": " + Arrays.toString(psqtValues[ChessConstants.WHITE]) + " (" + sum / psqtValues[ChessConstants.WHITE].length + ")";
		}
		return name + ": " + Arrays.toString(psqtValues[ChessConstants.WHITE]);
	}

	public void addStep(int i) {
		// add to white
		psqtValues[ChessConstants.WHITE][i] += step;

		// add to white mirrored
		psqtValues[ChessConstants.WHITE][TexelConstants.MIRRORED_LEFT_RIGHT[i]] += step;

		// add to black
		psqtValues[ChessConstants.BLACK][TexelConstants.MIRRORED_UP_DOWN[i]] -= step;

		// add to black mirrored
		psqtValues[ChessConstants.BLACK][TexelConstants.MIRRORED_LEFT_RIGHT[TexelConstants.MIRRORED_UP_DOWN[i]]] -= step;
	}

	public void removeStep(int i) {
		// remove from white
		psqtValues[ChessConstants.WHITE][i] -= step;

		// remove from white mirrored
		psqtValues[ChessConstants.WHITE][TexelConstants.MIRRORED_LEFT_RIGHT[i]] -= step;

		// remove from black
		psqtValues[ChessConstants.BLACK][TexelConstants.MIRRORED_UP_DOWN[i]] += step;

		// remove from black mirrored
		psqtValues[ChessConstants.BLACK][TexelConstants.MIRRORED_LEFT_RIGHT[TexelConstants.MIRRORED_UP_DOWN[i]]] += step;
	}

	public boolean scoreIsZero(int i) {
		return psqtValues[ChessConstants.WHITE][i] == 0;
	}

	public int numberOfParameters() {
		return psqtValues[ChessConstants.WHITE].length;
	}

	public boolean skip(int i) {
		return skipValues.contains(i) || (i & 7) > 3;
	}

}
