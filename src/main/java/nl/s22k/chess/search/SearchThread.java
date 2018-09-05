package nl.s22k.chess.search;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.move.MoveGenerator;

public class SearchThread extends Thread {

	private ChessBoard cb;
	private MoveGenerator moveGen;
	private int depth;
	private int alpha;
	private int beta;

	public SearchThread(ChessBoard cb, MoveGenerator moveGen, int depth, int alpha, int beta) {
		this.cb = cb;
		this.moveGen = moveGen;
		this.depth = depth;
		this.alpha = alpha;
		this.beta = beta;
	}

	@Override
	public void run() {
		// System.out.println("Starting slave thread. depth=" + depth + " alpha=" + alpha + " beta=" + beta);
		NegamaxUtil.calculateBestMove(cb, moveGen, 0, depth, alpha, beta, 0);
		NegamaxUtil.mode.compareAndSet(Mode.START, Mode.ANY_SLAVE_READY);
		NegamaxUtil.nrOfActiveSlaveThreads.decrementAndGet();
		// System.out.println("Slave thread done");
	}

}
