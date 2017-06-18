package nl.s22k.chess.unittests;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.TimeUtil;

public class MainTest {

	public static final String FEN_MATE_IN_7 = "1Q6/8/8/2K5/4k3/8/8/8 b - - 4 14 "; // 32753
	public static final String FEN_MATE_IN_8 = "8/K7/8/1Q3k2/8/8/8/8 b - - 2 13 ";// 32751
	public static final String FEN_MATE_IN_9w = ""; // 32750
	public static final String FEN_MATE_IN_9 = "8/K7/8/1Q6/5k2/8/8/8 w - - 3 14 ";// 32749
	public static final String FEN_MATE_IN_10w = "8/KP6/4k3/8/8/8/8/8 w - - 1 12 "; // 32748
	public static final String FEN_MATE_IN_10 = "8/KP1k4/8/8/8/8/8/8 b - - 0 11 ";// 32747
	public static final String FEN_MATE_IN_11w = "8/K2k4/1P6/8/8/8/8/8 w - - 3 11 "; // 32746
	public static final String FEN_MATE_IN_11 = "2k5/K7/1P6/8/8/8/8/8 b - - 2 10 ";// 32745
	public static final String FEN_MATE_IN_12w = "2k5/8/KP6/8/8/8/8/8 w - - 1 10 "; // 32744
	public static final String FEN_MATE_IN_12 = "8/2k5/KP6/8/8/8/8/8 b - - 0 9 "; // 32743
	public static final String FEN_MATE_IN_21 = "2k5/8/1pP1K3/1P6/8/8/8/8 w – – 0 1";

	public static final String FEN_STANDARD_OPENING = "r2qr1k1/2p2ppp/p3bn2/2bpN1B1/8/2NQ4/PPP2PPP/3RR1K1 b - - 3 14 ";
	public static final String FEN_STANDARD_MIDDLEGAME = "2b5/1p3k2/7R/4p1rP/1qpnR3/8/P4PP1/3Q2K1 w - - 0 47 ";
	public static final String FEN_LOSING_CAPTURE = "7r/5Q2/7p/7k/P5R1/B1P3P1/3PP3/n3K3 b - - 0 44 ";
	public static final String FEN_ENDGAME = "8/2p2p2/3p1k2/1p1P2p1/5P1p/4K2P/p4P2/N7 b - - 1 82";
	public static final String FEN_STALEMATE = "8/8/4k3/5p2/5K2/8/8/8 w - - 0 63 ";

	public static void main(String[] args) {

		MagicUtil.init();

		ChessBoard cb = ChessBoardUtil.getNewCB(FEN_STANDARD_OPENING);
		TimeUtil.setTimeWindow(300000, cb.moveCounter, 0);
		// NegamaxUtil.maxDepth = 20;
		NegamaxUtil.start(cb);
		Statistics.print();

	}

}
