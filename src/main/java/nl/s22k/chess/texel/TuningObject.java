package nl.s22k.chess.texel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TuningObject {

	public int[] values;
	public int[] orgValues;
	public int step;
	public String name;
	public List<Integer> skipValues;
	public int tunedValues;
	public boolean showAverage;
	public boolean allScoresAboveZero;
	public int maxValue = Integer.MAX_VALUE;

	public TuningObject(int[] values, int step, String name) {
		this.values = values;
		this.step = step;
		this.showAverage = false;
		this.allScoresAboveZero = false;
		while (name.length() < 20) {
			name += " ";
		}
		this.name = name;
		this.skipValues = new ArrayList<>();
		tunedValues = values.length - this.skipValues.size();
		orgValues = new int[values.length];
		System.arraycopy(values, 0, orgValues, 0, values.length);
	}

	public TuningObject(int[] values, int step, String name, boolean showAverage, boolean allScoresAboveZero, Integer... skipValues) {
		this.values = values;
		this.step = step;
		this.showAverage = showAverage;
		this.allScoresAboveZero = allScoresAboveZero;
		while (name.length() < 20) {
			name += " ";
		}
		this.name = name;
		this.skipValues = Arrays.asList(skipValues);
		tunedValues = values.length - this.skipValues.size();
		orgValues = new int[values.length];
		System.arraycopy(values, 0, orgValues, 0, values.length);
	}

	public TuningObject(int[] values, int step, int maxValue, String name, boolean showAverage, boolean allScoresAboveZero, Integer... skipValues) {
		this(values, step, name, showAverage, allScoresAboveZero, skipValues);
		this.maxValue = maxValue;
	}

	public void printNewValues() {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < values.length; i++) {
				sum += values[i];
			}
			return name + ": " + Arrays.toString(values) + " (" + sum / values.length + ")";
		}
		return name + ": " + Arrays.toString(values);
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

	public boolean isMaxReached(int i) {
		return values[i] >= maxValue;
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
