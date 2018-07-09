package nl.s22k.chess.texel;

import java.util.Arrays;

public class MultiTuningObject extends TuningObject {

	float[] floatValues;

	public MultiTuningObject(float[] floatValues, String name) {
		super(new int[floatValues.length], 1, name, false, true);
		this.floatValues = floatValues;

		for (int i = 0; i < floatValues.length; i++) {
			values[i] = (int) (floatValues[i] * 10);
			orgValues[i] = (int) (floatValues[i] * 10);
		}
	}

	public void addStep(int i) {
		values[i] += 1;
		floatValues[i] += 0.1f;
	}

	public void removeStep(int i) {
		values[i] -= 1;
		floatValues[i] -= 0.1f;
	}

	@Override
	public String toString() {
		return name + ": " + Arrays.toString(floatValues);
	}

	@Override
	public void restoreValues() {
		for (int i = 0; i < values.length; i++) {
			floatValues[i] = (float) orgValues[i] / 10;
		}
	}

	@Override
	public void clearValues() {
		for (int i = 0; i < values.length; i++) {
			values[i] = 10;
			floatValues[i] = 1.0f;
		}
	}

}
