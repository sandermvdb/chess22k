package nl.s22k.chess.texel;

import java.util.Arrays;
import java.util.List;

public class Tuning {

	public int[] values;
	public int[] orgValues;
	public int step;
	public String name;
	public List<Integer> skipValues;
	public int tunedValues;
	public boolean showAverage;

	public Tuning(int[] values, int step, String name) {
		this(values, step, name, false, -1);
	}

	public Tuning(int[] values, int step, String name, Integer... skipValues) {
		this(values, step, name, false, skipValues);
	}

	public Tuning(int[] values, int step, String name, boolean showAverage, Integer... skipValues) {
		this.values = values;
		this.step = step;
		this.showAverage = showAverage;
		while (name.length() < 20) {
			name += " ";
		}
		this.name = name;
		this.skipValues = Arrays.asList(skipValues);
		tunedValues = values.length - this.skipValues.size();
		orgValues = new int[values.length];
		System.arraycopy(values, 0, orgValues, 0, values.length);
	}

	public void printNewValues() {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		if (showAverage) {
			return name + ": " + Arrays.toString(values) + " (" + getAverage() + ")";
		}
		return name + ": " + Arrays.toString(values);
	}

	public int getAverage() {
		int sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		return sum / values.length;
	}

	public void addStep(int i) {
		values[i] += step;
	}

	public void removeStep(int i) {
		values[i] -= step;
	}

	public int numberOfParameters() {
		return values.length;
	}

	public boolean skip(int i) {
		return skipValues.contains(i);
	}

	public boolean scoreIsZero(int i) {
		return values[i] == 0;
	}

	public boolean isUpdated() {
		for (int i = 0; i < orgValues.length; i++) {
			if (orgValues[i] != values[i]) {
				return true;
			}
		}
		return false;
	}

	public void clearValues() {
		for (int i = 0; i < values.length; i++) {
			values[i] = 0;
		}
	}

	public void restoreValues() {
		for (int i = 0; i < values.length; i++) {
			values[i] = orgValues[i];
		}
	}

}
