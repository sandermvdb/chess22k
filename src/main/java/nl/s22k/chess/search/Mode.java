package nl.s22k.chess.search;

public class Mode {

	/**
	 * stop signal by GUI or no time left. abort all calculation
	 */
	public static final int STOP = 0;

	/**
	 * main thread or any slave thread ready
	 */
	public static final int STOP_SLAVES = 1;

	public static final int START = 2;
	public static final int ANY_SLAVE_READY = 3;

}
