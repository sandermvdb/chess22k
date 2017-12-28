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
	public static final String FEN_ENDGAME_2 = "8/5p2/3p1k2/3P2p1/5P2/4K3/5P2/8 b - - 1 82 ";
	public static final String FEN_STALEMATE = "8/8/4k3/5p2/5K2/8/8/8 w - - 0 63 ";
	public static final String FEN_FUTILITY = "8/1p3pk1/1q1P1bp1/4P3/n1p1P3/P5P1/1PBQ2K1/8 w - - 0 50";
	public static final String FEN_FUTILITY_2 = "rnb1kq1r/1p1n1pp1/p3p1P1/3pP3/3p3N/2NQ4/PPP2P2/2KR1B1R w kq - 0 15 ";

	public static void main(String[] args) {

		MagicUtil.init();

		ChessBoard cb = ChessBoardUtil.getNewCB(FEN_STANDARD_OPENING);
		TimeUtil.setSimpleTimeWindow(5000);
		// NegamaxUtil.maxDepth = 20;
		NegamaxUtil.start(cb);
		Statistics.print();

	}

}
