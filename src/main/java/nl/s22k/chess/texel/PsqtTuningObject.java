package nl.s22k.chess.texel;

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
	public void printOrgValues() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < orgValues.length; i++) {
				sum += orgValues[i];
			}
			System.out.println(name + ": (" + sum / orgValues.length + ")" + getArrayFriendlyFormatted(orgValues));
		} else {
			System.out.println(name + ": " + getArrayFriendlyFormatted(orgValues));
		}
	}

	@Override
	public void printNewValues() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < psqtValues[ChessConstants.WHITE].length; i++) {
				sum += psqtValues[ChessConstants.WHITE][i];
			}
			System.out
					.println(name + ": (" + sum / psqtValues[ChessConstants.WHITE].length + ")" + getArrayFriendlyFormatted(psqtValues[ChessConstants.WHITE]));
		} else {
			System.out.println(name + ": " + getArrayFriendlyFormatted(psqtValues[ChessConstants.WHITE]));
		}
	}

	@Override
	public String toString() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < psqtValues[ChessConstants.WHITE].length; i++) {
				sum += psqtValues[ChessConstants.WHITE][i];
			}
			return name + ": " + sum / psqtValues[ChessConstants.WHITE].length;
		}
		return name;
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

	public static String getArrayFriendlyFormatted(int[] values) {
		StringBuilder sb = new StringBuilder("\n");
		for (int i = 7; i >= 0; i--) {
			sb.append(" ");
			for (int j = 0; j < 8; j++) {
				sb.append(String.format("%3s", values[i * 8 + j])).append(",");
			}
			sb.append("\n");
		}
		sb.delete(sb.length() - 2, sb.length());
		return sb.toString();
	}

}
