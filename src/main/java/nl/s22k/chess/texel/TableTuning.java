package nl.s22k.chess.texel;

import java.util.Arrays;

public class TableTuning extends Tuning {

	public int[][] values;
	public int[][] orgValues;

	public TableTuning(int[][] values, int step, String name) {
		super(null, step, name);
		this.values = values;
		orgValues = new int[values.length][];
		for (int i = 0; i < values.length; i++) {
			orgValues[i] = new int[values[i].length];
			System.arraycopy(values[i], 0, orgValues[i], 0, values[i].length);
		}
	}

	@Override
	public String toString() {
		return name + ": " + Arrays.deepToString(values);
	}

	public void printNewValues() {
		System.out.println(name + ":");
		for (int i = 0; i < values.length; i++) {
			if (i == values.length - 1) {
				System.out.println(Arrays.toString(values[i]).replace("[", "{").replace("]", "}"));
			} else {
				System.out.println(Arrays.toString(values[i]).replace("[", "{").replace("]", "}") + ",");
			}
		}
	}

	public int getNumberOfTunedValues() {
		return values.length * values[0].length;
	}

	public void addStep(int i) {
		values[i / values[0].length][i % values[0].length] += step;
	}

	public void removeStep(int i) {
		values[i / values[0].length][i % values[0].length] -= step;
	}

	public int numberOfParameters() {
		return values.length * values[0].length;
	}

	public boolean isUpdated() {
		for (int i = 0; i < orgValues.length; i++) {
			for (int j = 0; j < orgValues[0].length; j++) {
				if (orgValues[i][j] != values[i][j]) {
					return true;
				}
			}
		}
		return false;
	}

	public void clearValues() {
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < orgValues[0].length; j++) {
				values[i][j] = 0;
			}
		}
	}

	public void restoreValues() {
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < orgValues[0].length; j++) {
				values[i][j] = orgValues[i][j];
			}
		}
	}

}
