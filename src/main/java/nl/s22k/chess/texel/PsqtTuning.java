package nl.s22k.chess.texel;

import nl.s22k.chess.ChessConstants;

public class PsqtTuning extends Tuning {

	public int[][] psqtValues;

	public PsqtTuning(int[][] psqtValues, int step, String name) {
		super(psqtValues[ChessConstants.WHITE], step, name, true);
		this.psqtValues = psqtValues;
		this.tunedValues = 32;
		orgValues = new int[64];
		System.arraycopy(psqtValues[ChessConstants.WHITE], 0, orgValues, 0, 64);
	}

	public PsqtTuning(int[][] psqtValues, int step, String name, boolean pawnPsqt) {
		super(psqtValues[ChessConstants.WHITE], step, name, true, 0, 1, 2, 3, 4, 5, 6, 7, 56, 57, 58, 59, 60, 61, 62, 63);
		this.psqtValues = psqtValues;
		this.tunedValues = 24;
		orgValues = new int[64];
		System.arraycopy(psqtValues[ChessConstants.WHITE], 0, orgValues, 0, 64);
	}

	@Override
	public void printNewValues() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < 64; i++) {
				sum += psqtValues[ChessConstants.WHITE][i];
			}
			System.out.println(name + ": (" + sum / 64 + ")" + getArrayFriendlyFormatted(psqtValues[ChessConstants.WHITE]));
		} else {
			System.out.println(name + ": " + getArrayFriendlyFormatted(psqtValues[ChessConstants.WHITE]));
		}
	}

	@Override
	public String toString() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < 64; i++) {
				sum += psqtValues[ChessConstants.WHITE][i];
			}
			return name + ": " + sum / 64;
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
		return 64;
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

	@Override
	public void clearValues() {
		for (int i = 0; i < 64; i++) {
			psqtValues[ChessConstants.WHITE][i] = 0;
			psqtValues[ChessConstants.BLACK][i] = 0;
		}
	}

	@Override
	public void restoreValues() {
		for (int i = 0; i < 64; i++) {
			psqtValues[ChessConstants.WHITE][i] = orgValues[i];
		}

		for (int i = 0; i < 64; i++) {
			psqtValues[ChessConstants.BLACK][i] = -psqtValues[ChessConstants.WHITE][63 - i];
		}
	}

	@Override
	public boolean isUpdated() {
		for (int i = 0; i < 64; i++) {
			if (orgValues[i] != psqtValues[ChessConstants.WHITE][i]) {
				return true;
			}
		}
		return false;
	}

}
