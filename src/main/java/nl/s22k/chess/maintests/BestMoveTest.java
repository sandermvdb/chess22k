package nl.s22k.chess.maintests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.TimeUtil;

public class BestMoveTest {

	// TODO make multi-threaded?

	public static int positionTestOK, positionTestNOK;

	@BeforeClass
	public static void init() {
		MagicUtil.init();
		MainEngine.quiet = true;
	}

	private void doTest(String[] epdStrings) {
		int correctCounter = 0;
		for (String epdString : epdStrings) {
			Statistics.reset();
			EPD epd = new EPD(epdString);

			ChessBoard cb = ChessBoardUtil.getNewCB(epd.getFen());
			// 200000 is approximately 3 secs per move
			TimeUtil.setTimeWindow(100000L, cb.moveCounter, 0);
			NegamaxUtil.start(cb);

			MoveWrapper bestMove = new MoveWrapper(Statistics.bestMove.move);
			if (epd.isBestMove()) {
				if (epd.moveEquals(bestMove)) {
					System.out.println(epd.getId() + " BM OK");
					correctCounter++;
					positionTestOK++;
				} else {
					System.out.println(epd.getId() + " BM NOK " + bestMove + " - " + epd);
					positionTestNOK++;
				}
			} else {
				if (epd.moveEquals(bestMove)) {
					System.out.println(epd.getId() + " AM NOK " + epd);
					positionTestNOK++;
				} else {
					System.out.println(epd.getId() + " AM OK");
					correctCounter++;
					positionTestOK++;
				}
			}
		}
		System.out.println(correctCounter + "/" + epdStrings.length);
	}

	//@formatter:off
	/**
	 *  0-4   1300 - 1599
	 *  5-6   1600 - 1799
	 *  7-8   1800 - 1999
	 *  9-12  2000 - 2199
	 * 13-16  2200 - 2399
	 * 17-24  2400+ 
	 */
	//@formatter:on
	@Test
	public void doBratkoKopecTest() {
		System.out.println("\r\nBratko-Kopec Test");
		String[] epdStrings = new String[] { "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - bm Qd1+; id \"BK.01\";",
				"3r1k2/4npp1/1ppr3p/p6P/P2PPPP1/1NR5/5K2/2R5 w - - bm d5; id \"BK.02\";",
				"2q1rr1k/3bbnnp/p2p1pp1/2pPp3/PpP1P1P1/1P2BNNP/2BQ1PRK/7R b - - bm f5; id \"BK.03\";",
				"rnbqkb1r/p3pppp/1p6/2ppP3/3N4/2P5/PPP1QPPP/R1B1KB1R w KQkq - bm e6; id \"BK.04\";",
				"r1b2rk1/2q1b1pp/p2ppn2/1p6/3QP3/1BN1B3/PPP3PP/R4RK1 w - - bm Nd5 a4; id \"BK.05\";",
				"2r3k1/pppR1pp1/4p3/4P1P1/5P2/1P4K1/P1P5/8 w - - bm g6; id \"BK.06\";",
				"1nk1r1r1/pp2n1pp/4p3/q2pPp1N/b1pP1P2/B1P2R2/2P1B1PP/R2Q2K1 w - - bm Nf6; id \"BK.07\";",
				"4b3/p3kp2/6p1/3pP2p/2pP1P2/4K1P1/P3N2P/8 w - - bm f5; id \"BK.08\";",
				"2kr1bnr/pbpq4/2n1pp2/3p3p/3P1P1B/2N2N1Q/PPP3PP/2KR1B1R w - - bm f5; id \"BK.09\";",
				"3rr1k1/pp3pp1/1qn2np1/8/3p4/PP1R1P2/2P1NQPP/R1B3K1 b - - bm Ne5; id \"BK.10\";",
				"2r1nrk1/p2q1ppp/bp1p4/n1pPp3/P1P1P3/2PBB1N1/4QPPP/R4RK1 w - - bm f4; id \"BK.11\";",
				"r3r1k1/ppqb1ppp/8/4p1NQ/8/2P5/PP3PPP/R3R1K1 b - - bm Bf5; id \"BK.12\";",
				"r2q1rk1/4bppp/p2p4/2pP4/3pP3/3Q4/PP1B1PPP/R3R1K1 w - - bm b4; id \"BK.13\";",
				"rnb2r1k/pp2p2p/2pp2p1/q2P1p2/8/1Pb2NP1/PB2PPBP/R2Q1RK1 w - - bm Qd2 Qe1; id \"BK.14\";",
				"2r3k1/1p2q1pp/2b1pr2/p1pp4/6Q1/1P1PP1R1/P1PN2PP/5RK1 w - - bm Qxg7+; id \"BK.15\";",
				"r1bqkb1r/4npp1/p1p4p/1p1pP1B1/8/1B6/PPPN1PPP/R2Q1RK1 w kq - bm Ne4; id \"BK.16\";",
				"r2q1rk1/1ppnbppp/p2p1nb1/3Pp3/2P1P1P1/2N2N1P/PPB1QP2/R1B2RK1 b - - bm h5; id \"BK.17\";",
				"r1bq1rk1/pp2ppbp/2np2p1/2n5/P3PP2/N1P2N2/1PB3PP/R1B1QRK1 b - - bm Nb3; id \"BK.18\";",
				"3rr3/2pq2pk/p2p1pnp/8/2QBPP2/1P6/P5PP/4RRK1 b - - bm Rxe4; id \"BK.19\";",
				"r4k2/pb2bp1r/1p1qp2p/3pNp2/3P1P2/2N3P1/PPP1Q2P/2KRR3 w - - bm g4; id \"BK.20\";",
				"3rn2k/ppb2rpp/2ppqp2/5N2/2P1P3/1P5Q/PB3PPP/3RR1K1 w - - bm Nh6; id \"BK.21\";",
				"2r2rk1/1bqnbpp1/1p1ppn1p/pP6/N1P1P3/P2B1N1P/1B2QPP1/R2R2K1 b - - bm Bxe4; id \"BK.22\";",
				"r1bqk2r/pp2bppp/2p5/3pP3/P2Q1P2/2N1B3/1PP3PP/R4RK1 b kq - bm f6; id \"BK.23\";",
				"r2qnrnk/p2b2b1/1p1p2pp/2pPpp2/1PP1P3/PRNBB3/3QNPPP/5RK1 w - - bm f4; id \"BK.24\";" };
		doTest(epdStrings);
	}

	@Test
	public void doCCROneHourTest() {
		System.out.println("\r\nCCR One Hour Test");
		String[] epdStrings = new String[] { "rn1qkb1r/pp2pppp/5n2/3p1b2/3P4/2N1P3/PP3PPP/R1BQKBNR w KQkq - bm Qb3; id \"CCR01\";",
				"rn1qkb1r/pp2pppp/5n2/3p1b2/3P4/1QN1P3/PP3PPP/R1B1KBNR b KQkq - bm Bc8; id \"CCR02\";",
				"r1bqk2r/ppp2ppp/2n5/4P3/2Bp2n1/5N1P/PP1N1PP1/R2Q1RK1 b kq - bm Nh6; id \"CCR03\";",
				"r1bqrnk1/pp2bp1p/2p2np1/3p2B1/3P4/2NBPN2/PPQ2PPP/1R3RK1 w - - bm b4; id \"CCR04\";",
				"rnbqkb1r/ppp1pppp/5n2/8/3PP3/2N5/PP3PPP/R1BQKBNR b KQkq - bm e5; id \"CCR05\";",
				"rnbq1rk1/pppp1ppp/4pn2/8/1bPP4/P1N5/1PQ1PPPP/R1B1KBNR b KQ - bm Bcx3+; id \"CCR06\";",
				"r4rk1/3nppbp/bq1p1np1/2pP4/8/2N2NPP/PP2PPB1/R1BQR1K1 b - - bm Rfb8; id \"CCR07\";",
				"rn1qkb1r/pb1p1ppp/1p2pn2/2p5/2PP4/5NP1/PP2PPBP/RNBQK2R w KQkq c6 bm d5; id \"CCR08\";",
				"r1bq1rk1/1pp2pbp/p1np1np1/3Pp3/2P1P3/2N1BP2/PP4PP/R1NQKB1R b KQ - bm Nd4; id \"CCR09\";",
				"rnbqr1k1/1p3pbp/p2p1np1/2pP4/4P3/2N5/PP1NBPPP/R1BQ1RK1 w - - bm a4; id \"CCR10\";",
				"rnbqkb1r/pppp1ppp/5n2/4p3/4PP2/2N5/PPPP2PP/R1BQKBNR b KQkq f3 bm d5; id \"CCR11\";",
				"r1bqk1nr/pppnbppp/3p4/8/2BNP3/8/PPP2PPP/RNBQK2R w KQkq - bm Bxf7+; id \"CCR12\";",
				"rnbq1b1r/ppp2kpp/3p1n2/8/3PP3/8/PPP2PPP/RNBQKB1R b KQ d3 am Ne4; id \"CCR13\";",
				"rnbqkb1r/pppp1ppp/3n4/8/2BQ4/5N2/PPP2PPP/RNB2RK1 b kq - am Nxc4; id \"CCR14\";",
				"r2q1rk1/2p1bppp/p2p1n2/1p2P3/4P1b1/1nP1BN2/PP3PPP/RN1QR1K1 w - - bm exf6; id \"CCR15\";",
				"r1bqkb1r/2pp1ppp/p1n5/1p2p3/3Pn3/1B3N2/PPP2PPP/RNBQ1RK1 b kq - bm d5; id \"CCR16\";",
				"r2qkbnr/2p2pp1/p1pp4/4p2p/4P1b1/5N1P/PPPP1PP1/RNBQ1RK1 w kq - am hxg4; id \"CCR17\";",
				"r1bqkb1r/pp3ppp/2np1n2/4p1B1/3NP3/2N5/PPP2PPP/R2QKB1R w KQkq e6 bm Bxf6+; id \"CCR18\";",
				"rn1qk2r/1b2bppp/p2ppn2/1p6/3NP3/1BN5/PPP2PPP/R1BQR1K1 w kq - am Bxe6; id \"CCR19\";",
				"r1b1kb1r/1pqpnppp/p1n1p3/8/3NP3/2N1B3/PPP1BPPP/R2QK2R w KQkq - am Ndb5; id \"CCR20\";",
				"r1bqnr2/pp1ppkbp/4N1p1/n3P3/8/2N1B3/PPP2PPP/R2QK2R b KQ - am Kxe6; id \"CCR21\";",
				"r3kb1r/pp1n1ppp/1q2p3/n2p4/3P1Bb1/2PB1N2/PPQ2PPP/RN2K2R w KQkq - bm a4; id \"CCR22\";",
				"r1bq1rk1/pppnnppp/4p3/3pP3/1b1P4/2NB3N/PPP2PPP/R1BQK2R w KQ - bm Bxh7+; id \"CCR23\";",
				"r2qkbnr/ppp1pp1p/3p2p1/3Pn3/4P1b1/2N2N2/PPP2PPP/R1BQKB1R w KQkq - bm Nxe5; id \"CCR24\";",
				"rn2kb1r/pp2pppp/1qP2n2/8/6b1/1Q6/PP1PPPBP/RNB1K1NR b KQkq - am Qxb3; id \"CCR25\";" };
		doTest(epdStrings);
	}

	@Test
	public void doKaufmanTest() {
		System.out.println("\r\nKaufman Test");
		String[] epdStrings = new String[] { "1rbq1rk1/p1b1nppp/1p2p3/8/1B1pN3/P2B4/1P3PPP/2RQ1R1K w - - bm Nf6+; id \"position01\";",
				"3r2k1/p2r1p1p/1p2p1p1/q4n2/3P4/PQ5P/1P1RNPP1/3R2K1 b - - bm Nxd4; id \"position02\";",
				"3r2k1/1p3ppp/2pq4/p1n5/P6P/1P6/1PB2QP1/1K2R3 w - - am Rd1; id \"position03\";",
				"r1b1r1k1/1ppn1p1p/3pnqp1/8/p1P1P3/5P2/PbNQNBPP/1R2RB1K w - - bm Rxb2; id \"position04\";",
				"2r4k/pB4bp/1p4p1/6q1/1P1n4/2N5/P4PPP/2R1Q1K1 b - - bm Qxc1; id \"position05\";",
				"r5k1/3n1ppp/1p6/3p1p2/3P1B2/r3P2P/PR3PP1/2R3K1 b - - am Rxa2; id \"position06\";",
				"2r2rk1/1bqnbpp1/1p1ppn1p/pP6/N1P1P3/P2B1N1P/1B2QPP1/R2R2K1 b - - bm Bxe4; id \"position07\";",
				"5r1k/6pp/1n2Q3/4p3/8/7P/PP4PK/R1B1q3 b - - bm h6; id \"position08\";",
				"r3k2r/pbn2ppp/8/1P1pP3/P1qP4/5B2/3Q1PPP/R3K2R w KQkq - bm Be2; id \"position09\";",
				"3r2k1/ppq2pp1/4p2p/3n3P/3N2P1/2P5/PP2QP2/K2R4 b - - bm Nxc3; id \"position10\";",
				"q3rn1k/2QR4/pp2pp2/8/P1P5/1P4N1/6n1/6K1 w - - bm Nf5; id \"position11\";",
				"6k1/p3q2p/1nr3pB/8/3Q1P2/6P1/PP5P/3R2K1 b - - bm Rd6; id \"position12\";",
				"1r4k1/7p/5np1/3p3n/8/2NB4/7P/3N1RK1 w - - bm Nxd5; id \"position13\";",
				"1r2r1k1/p4p1p/6pB/q7/8/3Q2P1/PbP2PKP/1R3R2 w - - bm Rxb2; id \"position14\";",
				"r2q1r1k/pb3p1p/2n1p2Q/5p2/8/3B2N1/PP3PPP/R3R1K1 w - - bm Bxf5; id \"position15\";",
				"8/4p3/p2p4/2pP4/2P1P3/1P4k1/1P1K4/8 w - - bm b4; id \"position16\";",
				"1r1q1rk1/p1p2pbp/2pp1np1/6B1/4P3/2NQ4/PPP2PPP/3R1RK1 w - - bm e5; id \"position17\";",
				"q4rk1/1n1Qbppp/2p5/1p2p3/1P2P3/2P4P/6P1/2B1NRK1 b - - bm Qc8; id \"position18\";",
				"r2q1r1k/1b1nN2p/pp3pp1/8/Q7/PP5P/1BP2RPN/7K w - - bm Qxd7; id \"position19\";",
				"8/5p2/pk2p3/4P2p/2b1pP1P/P3P2B/8/7K w - - bm Bg4; id \"position20\"; ZZZZZZZ",
				"8/2k5/4p3/1nb2p2/2K5/8/6B1/8 w - - bm Kxb5; id \"position21\"; ZZZZZZZZZZZZZZZZZZZZ",
				"1B1b4/7K/1p6/1k6/8/8/8/8 w - - bm Ba7; id \"position22\";",
				"rn1q1rk1/1b2bppp/1pn1p3/p2pP3/3P4/P2BBN1P/1P1N1PP1/R2Q1RK1 b - - bm Ba6; id \"position23\";",
				"8/p1ppk1p1/2n2p2/8/4B3/2P1KPP1/1P5P/8 w - - bm Bxc6; id \"position24\"; ZZZZZZZZZZZZZZZ",
				"8/3nk3/3pp3/1B6/8/3PPP2/4K3/8 w - - bm Bxd7; id \"position25\";" };
		doTest(epdStrings);
	}

	@Test
	public void doLCTIITest() {
		System.out.println("\r\nLouguet Chess Test II");
		String[] epdStrings = new String[] {
				"r3kb1r/3n1pp1/p6p/2pPp2q/Pp2N3/3B2PP/1PQ2P2/R3K2R w KQkq - bm d6; id \"LCTII.POS.01\"; c0 \"Chernin - Miles, Tunis 1985\";",
				"1k1r3r/pp2qpp1/3b1n1p/3pNQ2/2pP1P2/2N1P3/PP4PP/1K1RR3 b - - bm Bb4; id \"LCTII.POS.02\"; c0 \"Lilienthal - Botvinnik, Moskau 1945\";",
				"r6k/pp4p1/2p1b3/3pP3/7q/P2B3r/1PP2Q1P/2K1R1R1 w - - bm Qc5; id \"LCTII.POS.03\"; c0 \"Boissel - Boulard, corr. 1994\";",
				"1nr5/2rbkppp/p3p3/Np6/2PRPP2/8/PKP1B1PP/3R4 b - - bm e5; id \"LCTII.POS.04\"; c0 \"Kaplan - Kopec, USA 1975\";",
				"2r2rk1/1p1bq3/p3p2p/3pPpp1/1P1Q4/P7/2P2PPP/2R1RBK1 b - - bm Bb5; id \"LCTII.POS.05\"; c0 \"Estrin - Pytel, Albena 1973\";",
				"3r1bk1/p4ppp/Qp2p3/8/1P1B4/Pq2P1P1/2r2P1P/R3R1K1 b - - bm e5; id \"LCTII.POS.06\"; c0 \"Nimzowitsch - Capablanca, New York 1927\";",
				"r1b2r1k/pp2q1pp/2p2p2/2p1n2N/4P3/1PNP2QP/1PP2RP1/5RK1 w - - bm Nd1; id \"LCTII.POS.07\"; c0 \"Tartakower - Rubinstein, Moskau 1925\";",
				"r2qrnk1/pp3ppb/3b1n1p/1Pp1p3/2P1P2N/P5P1/1B1NQPBP/R4RK1 w - - bm Bh3; id \"LCTII.POS.08\"; c0 \"Polugaevsky - Unzicker, Kislovodsk 1972\";",
				"5nk1/Q4bpp/5p2/8/P1n1PN2/q4P2/6PP/1R4K1 w - - bm Qd4; id \"LCTII.POS.09\"; c0 \"Boissel - Del Gobbo, corr. 1994\";",
				"r3k2r/3bbp1p/p1nppp2/5P2/1p1NP3/5NP1/PPPK3P/3R1B1R b kq - bm Bf8; id \"LCTII.POS.10\"; c0 \"Cucka - Jansa, Brno 1960\";",
				"bn6/1q4n1/1p1p1kp1/2pPp1pp/1PP1P1P1/3N1P1P/4B1K1/2Q2N2 w - - bm h4; id \"LCTII.POS.11\"; c0 \"Landau - Schmidt, Noordwijk 1938\";",
				"3r2k1/pp2npp1/2rqp2p/8/3PQ3/1BR3P1/PP3P1P/3R2K1 b - - bm Rb6; id \"LCTII.POS.12\"; c0 \"Korchnoi - Karpov, Meran 1981\";",
				"1r2r1k1/4ppbp/B5p1/3P4/pp1qPB2/2n2Q1P/P4PP1/4RRK1 b - - bm Nxa2; id \"LCTII.POS.13\"; c0 \"Barbero - Kouatly, Budapest 1987\";",
				"r2qkb1r/1b3ppp/p3pn2/1p6/1n1P4/1BN2N2/PP2QPPP/R1BR2K1 w kq - bm d5; id \"LCTII.POS.14\"; c0 \"Spasski - Aftonomov, Leningrad 1949\";",
				"1r4k1/1q2bp2/3p2p1/2pP4/p1N4R/2P2QP1/1P3PK1/8 w - - bm Nxd6; id \"LCTII.CMB.01\"; c0 \"Romanishin - Gdansky, Polonica Zdroj 1992\";",
				"rn3rk1/pbppq1pp/1p2pb2/4N2Q/3PN3/3B4/PPP2PPP/R3K2R w KQ - bm Qxh7+; id \"LCTII.CMB.02\"; c0 \"Lasker,Ed - Thomas, London 1911\";",
				"4r1k1/3b1p2/5qp1/1BPpn2p/7n/r3P1N1/2Q1RPPP/1R3NK1 b - - bm Qf3; id \"LCTII.CMB.03\"; c0 \"Andruet - Spassky, BL 1988\";",
				"2k2b1r/1pq3p1/2p1pp2/p1n1PnNp/2P2B2/2N4P/PP2QPP1/3R2K1 w - - bm exf6; id \"LCTII.CMB.04\"; c0 \"Vanka - Jansa, Prag 1957\";",
				"2r2r2/3qbpkp/p3n1p1/2ppP3/6Q1/1P1B3R/PBP3PP/5R1K w - - bm Rxh7+; id \"LCTII.CMB.05\"; c0 \"Boros - Szabo, Budapest 1937\";",
				"2r1k2r/2pn1pp1/1p3n1p/p3PP2/4q2B/P1P5/2Q1N1PP/R4RK1 w q - bm exf6; id \"LCTII.CMB.06\"; c0 \"Lilienthal - Capablanca, Hastings 1934\";",
				"2rr2k1/1b3ppp/pb2p3/1p2P3/1P2BPnq/P1N3P1/1B2Q2P/R4R1K b - - bm Rxc3; id \"LCTII.CMB.07\"; c0 \"Rotlewi - Rubinstein, Lodz 1907\";",
				"2b1r1k1/r4ppp/p7/2pNP3/4Q3/q6P/2P2PP1/3RR1K1 w - - bm Nf6+; id \"LCTII.CMB.08\"; c0 \"Zarkov - Mephisto, Albuquerque 1991\";",
				"6k1/5p2/3P2p1/7n/3QPP2/7q/r2N3P/6RK b - - bm Rxd2; id \"LCTII.CMB.09\"; c0 \"Portisch - Kasparov, Moskau 1981\";",
				"rq2rbk1/6p1/p2p2Pp/1p1Rn3/4PB2/6Q1/PPP1B3/2K3R1 w - - bm Bxh6; id \"LCTII.CMB.10\"; c0 \"Tchoudinovskikh - Merchiev, UdSSR 1987\";",
				"rnbq2k1/p1r2p1p/1p1p1Pp1/1BpPn1N1/P7/2P5/6PP/R1B1QRK1 w - - bm Nxh7; id \"LCTII.CMB.11\"; c0 \"Vaisser - Genius 2, Aubervilliers, 1994\";",
				"r2qrb1k/1p1b2p1/p2ppn1p/8/3NP3/1BN5/PPP3QP/1K3RR1 w - - bm e5; id \"LCTII.CMB.12\"; c0 \"Spassky - Petrosian, Moskau 1969\";",
				"8/1p3pp1/7p/5P1P/2k3P1/8/2K2P2/8 w - - bm f6; id \"LCTII.FIN.01\"; c0 \"NN - Lasker,Ed\";",
				"8/pp2r1k1/2p1p3/3pP2p/1P1P1P1P/P5KR/8/8 w - - bm f5; id \"LCTII.FIN.02\"; c0 \"Capablanca - Eliskases, Moskau 1936\";",
				"8/3p4/p1bk3p/Pp6/1Kp1PpPp/2P2P1P/2P5/5B2 b - - bm Bxe4; id \"LCTII.FIN.03\"; c0 \"Studie 1994\";",
				"5k2/7R/4P2p/5K2/p1r2P1p/8/8/8 b - - bm h3; id \"LCTII.FIN.04\"; c0 \"Karpov - Deep Thought, Analyse 1990\";",
				"6k1/6p1/7p/P1N5/1r3p2/7P/1b3PP1/3bR1K1 w - - bm a6; id \"LCTII.FIN.05\"; c0 \"Karpov - Kasparov, Moskau 1985 [Analyse]\";",
				"8/3b4/5k2/2pPnp2/1pP4N/pP1B2P1/P3K3/8 b - - bm f4; id \"LCTII.FIN.06\"; c0 \"Minev - Portisch, Halle 1967\";",
				"6k1/4pp1p/3p2p1/P1pPb3/R7/1r2P1PP/3B1P2/6K1 w - - bm Bb4; id \"LCTII.FIN.07\"; c0 \"Lengyel - Kaufman, Los Angeles 1974\";",
				"2k5/p7/Pp1p1b2/1P1P1p2/2P2P1p/3K3P/5B2/8 w - - bm c5; id \"LCTII.FIN.08\"; c0 \"Spassky - Byrne, 1974\";",
				"8/5Bp1/4P3/6pP/1b1k1P2/5K2/8/8 w - - bm Kg4; id \"LCTII.FIN.09\"; c0 \"Klimenok - Kabanov, UdSSR 1969\";" };
		doTest(epdStrings);
	}

	@Test
	public void doEigenmannTest() {
		System.out.println("\r\nEigenmann Endgame Test");
		String[] epdStrings = new String[] { "8/8/p2p3p/3k2p1/PP6/3K1P1P/8/8 b - - bm Kc6; id \"E_E_T001 - B vs B\";",
				"8/p5pp/3k1p2/3p4/1P3P2/P1K5/5P1P/8 b - - bm g5; id \"E_E_T002 - B vs B\";",
				"8/1p3p2/p7/8/2k5/4P1pP/2P1K1P1/8 w - - bm h4; id \"E_E_T003 - B vs B\";",
				"8/pp5p/3k2p1/8/4Kp2/2P1P2P/P7/8 w - - bm exf4; id \"E_E_T004 - B vs B\";",
				"8/7p/1p3pp1/p2K4/Pk3PPP/8/1P6/8 b - - bm Kb3 f5; id \"E_E_T005 - B vs B\";",
				"2k5/3b4/PP3K2/7p/4P3/1P6/8/8 w - - bm Ke7; id \"E_E_T006 - B vs L\";", "8/3Pb1p1/8/3P2P1/5P2/7k/4K3/8 w - - bm Kd3; id \"E_E_T007 - B vs L\";",
				"8/1Pk2Kpp/8/8/4nPP1/7P/8/8 b - - bm Nf2; id \"E_E_T008 - B vs S\";", "2n5/4k1p1/P6p/3PK3/7P/8/6P1/8 b - - bm g6; id \"E_E_T009 - B vs S\";",
				"4k3/8/3PP1p1/8/3K3p/8/3n2PP/8 b - - am Nf1; id \"E_E_T010 - B vs S\";",
				"6k1/5p2/P3p1p1/2Qp4/5q2/2K5/8/8 b - - am Qc1+ Qe5+; id \"E_E_T011 - D vs D\";",
				"8/6pk/8/2p2P1p/6qP/5QP1/8/6K1 w - - bm Qd3 Qf2; id \"E_E_T012 - D vs D\";",
				"5q1k/5P1p/Q7/5n1p/6P1/7K/8/8 w - - bm Qa1+; id \"E_E_T013 - D vs D&S\";",
				"4qr2/4p2k/1p2P1pP/5P2/1P3P2/6Q1/8/3B1K2 w - - bm Ba4; id \"E_E_T014 - D&L vs D&T\";",
				"8/kn4b1/P2B4/8/1Q6/6pP/1q4pP/5BK1 w - - bm Bc5+; id \"E_E_T015 - D&L&L vs D&L&S\";",
				"6k1/1p2p1bp/p5p1/4pb2/1q6/4Q3/1P2BPPP/2R3K1 w - - bm Qa3; id \"E_E_T017 - D&T&L vs D&L&L\";",
				"1rr2k2/p1q5/3p2Q1/3Pp2p/8/1P3P2/1KPRN3/8 w - e6 bm Rd1; id \"E_E_T018 - D&T&S vs D&T&T\";",
				"r5k1/3R2p1/p1r1q2p/P4p2/5P2/2p1P3/5P1P/1R1Q2K1 w - - am Rbb7; id \"E_E_T019 - D&T&T vs D&T&T\";",
				"8/1p4k1/pK5p/2B5/P4pp1/8/7P/8 b - - am g3; id \"E_E_T020 - L vs B\";",
				"8/6p1/6P1/6Pp/B1p1p2K/6PP/3k2P1/8 w - - bm Bd1; id \"E_E_T021 - L vs B\";",
				"8/4k3/8/2Kp3p/B3bp1P/P7/1P6/8 b - - bm Bg2; id \"E_E_T022 - L vs L\";",
				"8/8/2p1K1p1/2k5/p7/P4BpP/1Pb3P1/8 w - - am Kd7; id \"E_E_T023 - L vs L\";",
				"8/3p3B/5p2/5P2/p7/PP5b/k7/6K1 w - - bm b4; id \"E_E_T024 - L vs L\";",
				"8/p4p2/1p2k2p/6p1/P4b1P/1P6/3B1PP1/6K1 w - - am Bxf4; id \"E_E_T025 - L vs L\";",
				"3b3k/1p4p1/p5p1/4B3/8/7P/1P3PP1/5K2 b - - am Bf6; id \"E_E_T027 - L vs L\";",
				"4b1k1/1p3p2/4pPp1/p2pP1P1/P2P4/1P1B4/8/2K5 w - - bm b4; id \"E_E_T028 - L vs L\";",
				"8/3k1p2/n3pP2/1p2P2p/5K2/1PB5/7P/8 b - - am Kc6 b4; id \"E_E_T029 - L vs S\";",
				"8/8/4p1p1/1P1kP3/4n1PK/2B4P/8/8 b - - bm Kc5; id \"E_E_T030 - L vs S\";",
				"8/5k2/4p3/B2p2P1/3K2n1/1P6/8/8 b - - bm Kg6; id \"E_E_T031 - L vs S\";",
				"5b2/p4B2/5B2/1bN5/8/P3r3/4k1K1/8 w - - bm Bh5+; id \"E_E_T032 - L&L&S vs T&L&L\";",
				"8/p5pq/8/p2N3p/k2P3P/8/KP3PB1/8 w - - bm Be4; id \"E_E_T033 - L&S vs D\";",
				"1b6/1P6/8/2KB4/6pk/3N3p/8/8 b - - bm Kg3; id \"E_E_T034 - L&S vs L&B\";",
				"8/p7/7k/1P1K3P/8/1n6/4Bp2/5Nb1 b - - bm Na5; id \"E_E_T035 - L&S vs L&S\";",
				"8/8/8/3K4/2N5/p2B4/p7/k4r2 w - - bm Kc5; id \"E_E_T036 - L&S vs T&B\";",
				"8/8/2kp4/5Bp1/8/5K2/3N4/2rN4 w - - bm Nb3; id \"E_E_T037 - L&S&S vs T&B\";",
				"k2K4/1p4pN/P7/1p3P2/pP6/8/8/8 w - - bm f6 Kc7; id \"E_E_T038 - S vs B\";",
				"k2N4/1qpK1p2/1p6/1P4p1/1P4P1/8/8/8 w - - bm Nc6; id \"E_E_T039 - S vs D\";",
				"6k1/4b3/4p1p1/8/6pP/4PN2/5K2/8 w - - bm Ne5 Nh2; id \"E_E_T040 - S vs L\";",
				"8/8/6Np/2p3kP/1pPbP3/1P3K2/8/8 w - - bm e5; id \"E_E_T041 - S vs L\";",
				"8/3P4/1p3b1p/p7/P7/1P3NPP/4p1K1/3k4 w - - bm g4; id \"E_E_T042 - S vs L\";",
				"8/8/1p2p3/p3p2b/P1PpP2P/kP6/2K5/7N w - - bm Nf2; id \"E_E_T043 - S vs L\";",
				"4N3/8/3P3p/1k2P3/7p/1n1K4/8/8 w - - bm d7; id \"E_E_T044 - S vs S\";",
				"N5n1/2p1kp2/2P3p1/p4PP1/K4P2/8/8/8 w - - bm f6 Kb5; id \"E_E_T045 - S vs S\";",
				"8/8/2pn4/p4p1p/P4N2/1Pk2KPP/8/8 w - - bm Ne2 Ne6; id \"E_E_T046 - S vs S\";",
				"8/7k/2P5/2p4p/P3N2K/8/8/5r2 w - - bm Ng5+; id \"E_E_T047 - S vs T\";", "2k1r3/p7/K7/1P6/P2N4/8/P7/8 w - - bm Nc6; id \"E_E_T048 - S vs T\";",
				"1k6/8/8/1K6/5pp1/8/4Pp1p/R7 w - - bm Kb6; id \"E_E_T049 - T vs B\";", "6k1/8/8/1K4p1/3p2P1/2pp4/8/1R6 w - - bm Kc6; id \"E_E_T050 - T vs B\";",
				"8/5p2/3pp2p/p5p1/4Pk2/2p2P1P/P1Kb2P1/1R6 w - - bm a4 Rb5; id \"E_E_T051 - T vs L\";",
				"8/8/4pR2/3pP2p/6P1/2p4k/P1Kb4/8 b - - bm hxg4; id \"E_E_T052 - T vs L\";",
				"3k3K/p5P1/P3r3/8/1N6/8/8/8 w - - bm Kh7; id \"E_E_T053 - T vs S\";", "8/8/5p2/5k2/p4r2/PpKR4/1P5P/8 w - - am Rd4; id \"E_E_T054 - T vs T\";",
				"5k2/7R/8/4Kp2/5Pp1/P5rp/1P6/8 w - - bm Kf6; id \"E_E_T055 - T vs T\";", "2K5/p7/7P/5pR1/8/5k2/r7/8 w - - bm Rxf5+; id \"E_E_T056 - T vs T\";",
				"8/2R4p/4k3/1p2P3/pP3PK1/r7/8/8 b - - bm h5 Ra1; id \"E_E_T057 - T vs T\";",
				"2k1r3/5R2/3KP3/8/1pP3p1/1P5p/8/8 w - - bm Rc7+; id \"E_E_T058 - T vs T\";",
				"8/6p1/1p5p/1R2k3/4p3/1P2P3/1K4PP/3r4 b - - am Rd5; id \"E_E_T059 - T vs T\";",
				"5K2/kp3P2/2p5/2Pp4/3P4/r7/p7/6R1 w - - bm Ke7; id \"E_E_T060 - T vs T\";",
				"8/pp3K2/2P4k/5p2/8/6P1/R7/6r1 w - - bm Kf6; id \"E_E_T061 - T vs T\";",
				"2r3k1/6pp/3pp1P1/1pP5/1P6/P4R2/5K2/8 w - - bm c6; id \"E_E_T062 - T vs T\";",
				"r2k4/8/8/1P4p1/8/p5P1/6P1/1R3K2 w - - bm b6; id \"E_E_T063 - T vs T\";",
				"8/4k3/1p4p1/p7/P1r1P3/1R4Pp/5P1P/4K3 w - - bm Ke2; id \"E_E_T064 - T vs T\";",
				"R7/4kp2/P3p1p1/3pP1P1/3P1P2/p6r/3K4/8 w - - bm Kc2; id \"E_E_T065 - T vs T\";",
				"8/1pp1r3/p1p2k2/6p1/P5P1/1P3P2/2P1RK2/8 b - - am Rxe2+ Re5; id \"E_E_T066 - T vs T\";",
				"8/1p2R3/8/p5P1/3k4/P2p2K1/1P6/5r2 w - - bm Kg2; id \"E_E_T067 - T vs T\";",
				"R7/P5Kp/2P5/k3r2P/8/5p2/p4P2/8 w - - bm Rb8; id \"E_E_T068 - T vs T\";",
				"8/2p4K/4k1p1/p1p1P3/PpP2P2/5R1P/8/6r1 b - - bm Kf7; id \"E_E_T069 - T vs T\";",
				"8/B7/1R6/q3k2p/8/6p1/5P2/5K2 w - - bm Rb3; id \"E_E_T071 - T&L vs D\";",
				"5k2/8/2Pb1B2/8/6RK/7p/5p1P/8 w - - bm Be5; id \"E_E_T072 - T&L vs L&B\";",
				"3kB3/R4P2/8/8/1p6/pp6/5r2/1K6 w - - bm f8; id \"E_E_T073 - T&L vs T&B\";",
				"5k2/1p6/1P1p4/1K1p2p1/PB1P2P1/3pR2p/1P2p1pr/8 w - - bm Ba5; id \"E_E_T074 - T&L vs T&B\";",
				"6k1/p6p/1p1p2p1/2bP4/P1P5/2B3P1/4r2P/1R5K w - - bm a5; id \"E_E_T075 - T&L vs T&L\";",
				"3R3B/8/1r4b1/8/4pP2/7k/8/7K w - - bm Bd4; id \"E_E_T076 - T&L vs T&L\";",
				"rk1b4/p2p2p1/1P6/2R2P2/8/2K5/8/5B2 w - - bm Rc8+; id \"E_E_T077 - T&L vs T&L\";",
				"3r1k2/8/7R/8/8/pp1B4/P7/n1K5 w - - bm Rf6+; id \"E_E_T078 - T&L vs T&S\";",
				"r5k1/5ppp/1p6/2b1R3/1p3P2/2nP2P1/1B3PKP/5B2 w - - bm d4; id \"E_E_T079 - T&L&L vs T&L&S\";",
				"5k2/3p1b2/4pN2/3PPpp1/6R1/6PK/1B1q1P2/8 w - - bm Ba3+; id \"E_E_T080 - T&L&S vs D&L\";",
				"8/1p5p/6p1/1p4Pp/1PpR4/2P1K1kB/6Np/7b w - - bm Rd1; id \"E_E_T081 - T&L&S vs L&B\";",
				"7k/1p1Nr2P/3Rb3/8/3K4/6b1/8/5B2 w - - bm Ne5; id \"E_E_T082 - T&L&S vs T&L&L\";",
				"8/1B4k1/5pn1/6N1/1P3rb1/P1K4p/3R4/8 w - - bm Nxh3; id \"E_E_T083 - T&L&S vs T&L&S\";",
				"8/7p/6p1/3Np1bk/4Pp2/1R3PPK/5r1P/8 w - - bm Nc7; id \"E_E_T084 - T&S vs T&L\";",
				"1r6/3b1p2/2k4P/1N3p1P/5P2/8/3R4/2K5 w - - bm Na7+; id \"E_E_T085 - T&S vs T&L\";",
				"k6r/8/1R6/8/1pK2p2/8/7N/3b4 w - - bm Nf1; id \"E_E_T086 - T&S vs T&L\";",
				"8/8/8/p1p5/2P1k3/1Pn5/1N1R2K1/1r6 w - - bm Kg3; id \"E_E_T087 - T&S vs T&S\";",
				"5n1k/1r3P1p/p2p3P/P7/8/1N6/5R2/4K3 b - - bm Re7+; id \"E_E_T088 - T&S vs T&S\";",
				"6R1/P2k1N2/r7/7P/r7/p7/7K/8 w - - bm Nh6; id \"E_E_T089 - T&S vs T&T\";",
				"8/1rk1P3/7p/P7/1N2r3/5RKb/8/8 w - - bm Na6+; id \"E_E_T090 - T&S&B vs T&T&L\";",
				"2K5/k3q3/6pR/6p1/6Pp/7P/8/3R4 w - - bm Rh7; id \"E_E_T090 - T&T vs D\";",
				"R5bk/5r2/P7/1P1pR3/3P4/7p/5p1K/8 w - - bm Rh5+; id \"E_E_T092 - T&T vs T&L\";",
				"4k3/7r/3nb3/2R5/8/6n1/1R3K2/8 w - - bm Re5; id \"E_E_T093 - T&T vs T&L&S\";",
				"1r6/1r6/1P1KP3/6k1/1R4p1/7p/7R/8 w - - bm Kc6 Rb5; id \"E_E_T094 - T&T vs T&T\";",
				"1k1K4/1p6/P4P2/2R5/4p2R/r2p4/8/3r4 w - - bm Rf4; id \"E_E_T095 - T&T vs T&T\";",
				"5k2/R1p5/p1R3Pb/2K5/2B5/2q2b2/8/8 w - - bm g7+; id \"E_E_T096 - T&T&L vs D&L&L\";",
				"8/8/k7/n7/p1R5/p7/4r1p1/KB3R2 w - - bm Rc3; id \"E_E_T097 - T&T&L vs T&S&B\";",
				"3r2k1/p1R2ppp/1p6/P1b1PP2/3p4/3R2B1/5PKP/1r6 w - - bm f6; id \"E_E_T098 - T&T&L vs T&T&L\";",
				"8/5p2/5rp1/p2k1r1p/P1pNp2P/RP1bP1P1/5P1R/4K3 b - - bm c3; id \"E_E_T099 - T&T&S vs T&T&L\";",
				"1r4k1/6pp/3Np1b1/p1R1P3/6P1/P2pr2P/1P1R2K1/8 b - - bm Rf8; id \"E_E_T100 - T&T&S vs T&T&L\";",
				"4k1r1/pp2p2p/3pN1n1/2pP4/2P3P1/PP5P/8/5RK1 b - - am Nf8; id \"E_E_T16b - T&S vs T&S\";",
				"8/3k3p/1p2p3/p4p2/Pb1Pp3/2B3PP/1P3P2/5K2 w - - am Bxb4; id \"E_E_T26b - L vs L\";",
				"8/1k6/8/Q7/7p/6p1/6pr/6Kb w - - bm Qc5; id \"E_E_T70b - D vs T&L&B\";" };
		doTest(epdStrings);
	}

	@Test
	public void doZugzwangTest() {
		System.out.println("\r\nZugzwang Test");
		String[] epdStrings = new String[] { "8/8/p1p5/1p5p/1P5p/8/PPP2K1p/4R1rk w - - bm Rf1; id \"zugzwang.001\";",
				"1q1k4/2Rr4/8/2Q3K1/8/8/8/8 w - - bm Kh6; id \"zugzwang.002\";", "7k/5K2/5P1p/3p4/6P1/3p4/8/8 w - - bm g5; id \"zugzwang.003\";",
				"8/6B1/p5p1/Pp4kp/1P5r/5P1Q/4q1PK/8 w - - bm Qxh4; id \"zugzwang.004\";",
				"8/8/1p1r1k2/p1pPN1p1/P3KnP1/1P6/8/3R4 b - - bm Nxd5; id \"zugzwang.005\";" };
		doTest(epdStrings);
	}

	@Test
	public void doWacTest() {
		System.out.println("\r\nWin-at-chess Test");
		doTest(WAC_EPDS);
	}

	@AfterClass
	public static void showResults() {
		System.out.println("");
		System.out.println("Total: " + positionTestOK + "/" + (positionTestOK + positionTestNOK));
	}

	public static final String[] WAC_EPDS = new String[] { "2rr3k/pp3pp1/1nnqbN1p/3pN3/2pP4/2P3Q1/PPB4P/R4RK1 w - - bm Qg6; id \"WAC.001\";",
			"8/7p/5k2/5p2/p1p2P2/Pr1pPK2/1P1R3P/8 b - - bm Rxb2; id \"WAC.002\";",
			"5rk1/1ppb3p/p1pb4/6q1/3P1p1r/2P1R2P/PP1BQ1P1/5RKN w - - bm Rg3; id \"WAC.003\";",
			"r1bq2rk/pp3pbp/2p1p1pQ/7P/3P4/2PB1N2/PP3PPR/2KR4 w - - bm Qxh7+; id \"WAC.004\";",
			"5k2/6pp/p1qN4/1p1p4/3P4/2PKP2Q/PP3r2/3R4 b - - bm Qc4+; id \"WAC.005\";", "7k/p7/1R5K/6r1/6p1/6P1/8/8 w - - bm Rb7; id \"WAC.006\";",
			"rnbqkb1r/pppp1ppp/8/4P3/6n1/7P/PPPNPPP1/R1BQKBNR b KQkq - bm Ne3; id \"WAC.007\";",
			"r4q1k/p2bR1rp/2p2Q1N/5p2/5p2/2P5/PP3PPP/R5K1 w - - bm Rf7; id \"WAC.008\";",
			"3q1rk1/p4pp1/2pb3p/3p4/6Pr/1PNQ4/P1PB1PP1/4RRK1 b - - bm Bh2+; id \"WAC.009\";",
			"2br2k1/2q3rn/p2NppQ1/2p1P3/Pp5R/4P3/1P3PPP/3R2K1 w - - bm Rxh7; id \"WAC.010\";",
			"r1b1kb1r/3q1ppp/pBp1pn2/8/Np3P2/5B2/PPP3PP/R2Q1RK1 w kq - bm Bxc6; id \"WAC.011\";",
			"4k1r1/2p3r1/1pR1p3/3pP2p/3P2qP/P4N2/1PQ4P/5R1K b - - bm Qxf3+; id \"WAC.012\";",
			"5rk1/pp4p1/2n1p2p/2Npq3/2p5/6P1/P3P1BP/R4Q1K w - - bm Qxf8+; id \"WAC.013\";",
			"r2rb1k1/pp1q1p1p/2n1p1p1/2bp4/5P2/PP1BPR1Q/1BPN2PP/R5K1 w - - bm Qxh7+; id \"WAC.014\";",
			"1R6/1brk2p1/4p2p/p1P1Pp2/P7/6P1/1P4P1/2R3K1 w - - bm Rxb7; id \"WAC.015\";",
			"r4rk1/ppp2ppp/2n5/2bqp3/8/P2PB3/1PP1NPPP/R2Q1RK1 w - - bm Nc3; id \"WAC.016\";",
			"1k5r/pppbn1pp/4q1r1/1P3p2/2NPp3/1QP5/P4PPP/R1B1R1K1 w - - bm Ne5; id \"WAC.017\";", "R7/P4k2/8/8/8/8/r7/6K1 w - - bm Rh8; id \"WAC.018\";",
			"r1b2rk1/ppbn1ppp/4p3/1QP4q/3P4/N4N2/5PPP/R1B2RK1 w - - bm c6; id \"WAC.019\";",
			"r2qkb1r/1ppb1ppp/p7/4p3/P1Q1P3/2P5/5PPP/R1B2KNR b kq - bm Bb5; id \"WAC.020\";",
			"5rk1/1b3p1p/pp3p2/3n1N2/1P6/P1qB1PP1/3Q3P/4R1K1 w - - bm Qh6; id \"WAC.021\";",
			"r1bqk2r/ppp1nppp/4p3/n5N1/2BPp3/P1P5/2P2PPP/R1BQK2R w KQkq - bm Nxf7 Ba2; id \"WAC.022\";",
			"r3nrk1/2p2p1p/p1p1b1p1/2NpPq2/3R4/P1N1Q3/1PP2PPP/4R1K1 w - - bm g4; id \"WAC.023\";",
			"6k1/1b1nqpbp/pp4p1/5P2/1PN5/4Q3/P5PP/1B2B1K1 b - - bm Bd4; id \"WAC.024\";",
			"3R1rk1/8/5Qpp/2p5/2P1p1q1/P3P3/1P2PK2/8 b - - bm Qh4+; id \"WAC.025\";",
			"3r2k1/1p1b1pp1/pq5p/8/3NR3/2PQ3P/PP3PP1/6K1 b - - bm Bf5; id \"WAC.026\";",
			"7k/pp4np/2p3p1/3pN1q1/3P4/Q7/1r3rPP/2R2RK1 w - - bm Qf8+; id \"WAC.027\";",
			"1r1r2k1/4pp1p/2p1b1p1/p3R3/RqBP4/4P3/1PQ2PPP/6K1 b - - bm Qe1+; id \"WAC.028\";",
			"r2q2k1/pp1rbppp/4pn2/2P5/1P3B2/6P1/P3QPBP/1R3RK1 w - - bm c6; id \"WAC.029\";",
			"1r3r2/4q1kp/b1pp2p1/5p2/pPn1N3/6P1/P3PPBP/2QRR1K1 w - - bm Nxd6; id \"WAC.030\";",
			"rb3qk1/pQ3ppp/4p3/3P4/8/1P3N2/1P3PPP/3R2K1 w - - bm Qxa8 d6 dxe6 g3; id \"WAC.031\";",
			"6k1/p4p1p/1p3np1/2q5/4p3/4P1N1/PP3PPP/3Q2K1 w - - bm Qd8+; id \"WAC.032\";",
			"8/p1q2pkp/2Pr2p1/8/P3Q3/6P1/5P1P/2R3K1 w - - bm Qe5+ Qf4; id \"WAC.033\";",
			"7k/1b1r2p1/p6p/1p2qN2/3bP3/3Q4/P5PP/1B1R3K b - - bm Bg1; id \"WAC.034\";",
			"r3r2k/2R3pp/pp1q1p2/8/3P3R/7P/PP3PP1/3Q2K1 w - - bm Rxh7+; id \"WAC.035\";",
			"3r4/2p1rk2/1pQq1pp1/7p/1P1P4/P4P2/6PP/R1R3K1 b - - bm Re1+; id \"WAC.036\";",
			"2r5/2rk2pp/1pn1pb2/pN1p4/P2P4/1N2B3/nPR1KPPP/3R4 b - - bm Nxd4+; id \"WAC.037\";",
			"4k3/p4prp/1p6/2b5/8/2Q3P1/P2R1PKP/4q3 w - - bm Qd3 Rd8+; id \"WAC.038\";",
			"r1br2k1/pp2bppp/2nppn2/8/2P1PB2/2N2P2/PqN1B1PP/R2Q1R1K w - - bm Na4; id \"WAC.039\";",
			"3r1r1k/1p4pp/p4p2/8/1PQR4/6Pq/P3PP2/2R3K1 b - - bm Rc8; id \"WAC.040\";", "1k6/5RP1/1P6/1K6/6r1/8/8/8 w - - bm Ka5 Kc5 b7; id \"WAC.041\";",
			"r1b1r1k1/pp1n1pbp/1qp3p1/3p4/1B1P4/Q3PN2/PP2BPPP/R4RK1 w - - bm Ba5; id \"WAC.042\";",
			"r2q3k/p2P3p/1p3p2/3QP1r1/8/B7/P5PP/2R3K1 w - - bm Be7 Qxa8; id \"WAC.043\";",
			"3rb1k1/pq3pbp/4n1p1/3p4/2N5/2P2QB1/PP3PPP/1B1R2K1 b - - bm dxc4; id \"WAC.044\";",
			"7k/2p1b1pp/8/1p2P3/1P3r2/2P3Q1/1P5P/R4qBK b - - bm Qxa1; id \"WAC.045\";",
			"r1bqr1k1/pp1nb1p1/4p2p/3p1p2/3P4/P1N1PNP1/1PQ2PP1/3RKB1R w K - bm Nb5; id \"WAC.046\";",
			"r1b2rk1/pp2bppp/2n1pn2/q5B1/2BP4/2N2N2/PP2QPPP/2R2RK1 b - - bm Nxd4; id \"WAC.047\";",
			"1rbq1rk1/p1p1bppp/2p2n2/8/Q1BP4/2N5/PP3PPP/R1B2RK1 b - - bm Rb4; id \"WAC.048\";",
			"2b3k1/4rrpp/p2p4/2pP2RQ/1pP1Pp1N/1P3P1P/1q6/6RK w - - bm Qxh7+; id \"WAC.049\";",
			"k4r2/1R4pb/1pQp1n1p/3P4/5p1P/3P2P1/r1q1R2K/8 w - - bm Rxb6+; id \"WAC.050\";",
			"r1bq1r2/pp4k1/4p2p/3pPp1Q/3N1R1P/2PB4/6P1/6K1 w - - bm Rg4+; id \"WAC.051\";",
			"r1k5/1p3q2/1Qpb4/3N1p2/5Pp1/3P2Pp/PPPK3P/4R3 w - - bm Re7 c4; id \"WAC.052\";", // fails
																								// ..................
			"6k1/6p1/p7/3Pn3/5p2/4rBqP/P4RP1/5QK1 b - - bm Re1; id \"WAC.053\";",
			"r3kr2/1pp4p/1p1p4/7q/4P1n1/2PP2Q1/PP4P1/R1BB2K1 b q - bm Qh1+; id \"WAC.054\";",
			"r3r1k1/pp1q1pp1/4b1p1/3p2B1/3Q1R2/8/PPP3PP/4R1K1 w - - bm Qxg7+; id \"WAC.055\";",
			"r1bqk2r/pppp1ppp/5n2/2b1n3/4P3/1BP3Q1/PP3PPP/RNB1K1NR b KQkq - bm Bxf2+; id \"WAC.056\";",
			"r3q1kr/ppp5/3p2pQ/8/3PP1b1/5R2/PPP3P1/5RK1 w - - bm Rf8+; id \"WAC.057\";", "8/8/2R5/1p2qp1k/1P2r3/2PQ2P1/5K2/8 w - - bm Qd1+; id \"WAC.058\";",
			"r1b2rk1/2p1qnbp/p1pp2p1/5p2/2PQP3/1PN2N1P/PB3PP1/3R1RK1 w - - bm Nd5; id \"WAC.059\";",
			"rn1qr1k1/1p2np2/2p3p1/8/1pPb4/7Q/PB1P1PP1/2KR1B1R w - - bm Qh8+; id \"WAC.060\";",
			"3qrbk1/ppp1r2n/3pP2p/3P4/2P4P/1P3Q2/PB6/R4R1K w - - bm Qf7+; id \"WAC.061\";",
			"6r1/3Pn1qk/p1p1P1rp/2Q2p2/2P5/1P4P1/P3R2P/5RK1 b - - bm Rxg3+; id \"WAC.062\";",
			"r1brnbk1/ppq2pp1/4p2p/4N3/3P4/P1PB1Q2/3B1PPP/R3R1K1 w - - bm Nxf7; id \"WAC.063\";",
			"8/6pp/3q1p2/3n1k2/1P6/3NQ2P/5PP1/6K1 w - - bm g4+; id \"WAC.064\";",
			"1r1r1qk1/p2n1p1p/bp1Pn1pQ/2pNp3/2P2P1N/1P5B/P6P/3R1RK1 w - - bm Ne7+; id \"WAC.065\";",
			"1k1r2r1/ppq5/1bp4p/3pQ3/8/2P2N2/PP4P1/R4R1K b - - bm Qxe5; id \"WAC.066\";",
			"3r2k1/p2q4/1p4p1/3rRp1p/5P1P/6PK/P3R3/3Q4 w - - bm Rxd5; id \"WAC.067\";", "6k1/5ppp/1q6/2b5/8/2R1pPP1/1P2Q2P/7K w - - bm Qxe3; id \"WAC.068\";",
			"2k5/pppr4/4R3/4Q3/2pp2q1/8/PPP2PPP/6K1 w - - bm f3 h3; id \"WAC.069\";",
			"2kr3r/pppq1ppp/3p1n2/bQ2p3/1n1PP3/1PN1BN1P/1PP2PP1/2KR3R b - - bm Na2+; id \"WAC.070\";",
			"2kr3r/pp1q1ppp/5n2/1Nb5/2Pp1B2/7Q/P4PPP/1R3RK1 w - - bm Nxa7+; id \"WAC.071\";",
			"r3r1k1/pp1n1ppp/2p5/4Pb2/2B2P2/B1P5/P5PP/R2R2K1 w - - bm e6; id \"WAC.072\";",
			"r1q3rk/1ppbb1p1/4Np1p/p3pP2/P3P3/2N4R/1PP1Q1PP/3R2K1 w - - bm Qd2; id \"WAC.073\";",
			"5r1k/pp4pp/2p5/2b1P3/4Pq2/1PB1p3/P3Q1PP/3N2K1 b - - bm Qf1+; id \"WAC.074\";",
			"r3r1k1/pppq1ppp/8/8/1Q4n1/7P/PPP2PP1/RNB1R1K1 b - - bm Qd6; id \"WAC.075\";",
			"r1b1qrk1/2p2ppp/pb1pnn2/1p2pNB1/3PP3/1BP5/PP2QPPP/RN1R2K1 w - - bm Bxf6; id \"WAC.076\";",
			"3r2k1/ppp2ppp/6q1/b4n2/3nQB2/2p5/P4PPP/RN3RK1 b - - bm Ng3; id \"WAC.077\";",
			"r2q3r/ppp2k2/4nbp1/5Q1p/2P1NB2/8/PP3P1P/3RR1K1 w - - bm Ng5+; id \"WAC.078\";",
			"r3k2r/pbp2pp1/3b1n2/1p6/3P3p/1B2N1Pq/PP1PQP1P/R1B2RK1 b kq - bm Qxh2+; id \"WAC.079\";",
			"r4rk1/p1B1bpp1/1p2pn1p/8/2PP4/3B1P2/qP2QP1P/3R1RK1 w - - bm Ra1; id \"WAC.080\";",
			"r4rk1/1bR1bppp/4pn2/1p2N3/1P6/P3P3/4BPPP/3R2K1 b - - bm Bd6; id \"WAC.081\";", // fails: abrupte
																							// overgang naar endgame
			"3rr1k1/pp3pp1/4b3/8/2P1B2R/6QP/P3q1P1/5R1K w - - bm Bh7+; id \"WAC.082\";",
			"3rr1k1/ppqbRppp/2p5/8/3Q1n2/2P3N1/PPB2PPP/3R2K1 w - - bm Qxd7; id \"WAC.083\";",
			"r2q1r1k/2p1b1pp/p1n5/1p1Q1bN1/4n3/1BP1B3/PP3PPP/R4RK1 w - - bm Qg8+; id \"WAC.084\";",
			"kr2R3/p4r2/2pq4/2N2p1p/3P2p1/Q5P1/5P1P/5BK1 w - - bm Na6; id \"WAC.085\";",
			"8/p7/1ppk1n2/5ppp/P1PP4/2P1K1P1/5N1P/8 b - - bm Ng4+; id \"WAC.086\";", "8/p3k1p1/4r3/2ppNpp1/PP1P4/2P3KP/5P2/8 b - - bm Rxe5; id \"WAC.087\";",
			"r6k/p1Q4p/2p1b1rq/4p3/B3P3/4P3/PPP3P1/4RRK1 b - - bm Rxg2+; id \"WAC.088\";",
			"1r3b1k/p4rpp/4pp2/3q4/2ppbPPQ/6RK/PP5P/2B1NR2 b - - bm g5; id \"WAC.089\";",
			"3qrrk1/1pp2pp1/1p2bn1p/5N2/2P5/P1P3B1/1P4PP/2Q1RRK1 w - - bm Nxg7; id \"WAC.090\";",
			"2qr2k1/4b1p1/2p2p1p/1pP1p3/p2nP3/PbQNB1PP/1P3PK1/4RB2 b - - bm Be6; id \"WAC.091\";",
			"r4rk1/1p2ppbp/p2pbnp1/q7/3BPPP1/2N2B2/PPP4P/R2Q1RK1 b - - bm Bxg4; id \"WAC.092\";",
			"r1b1k1nr/pp3pQp/4pq2/3pn3/8/P1P5/2P2PPP/R1B1KBNR w KQkq - bm Bh6; id \"WAC.093\";", "8/k7/p7/3Qp2P/n1P5/3KP3/1q6/8 b - - bm e4+; id \"WAC.094\";",
			"2r5/1r6/4pNpk/3pP1qp/8/2P1QP2/5PK1/R7 w - - bm Ng4+; id \"WAC.095\";",
			"r1b4k/ppp2Bb1/6Pp/3pP3/1qnP1p1Q/8/PPP3P1/1K1R3R w - - bm Qd8+ b3; id \"WAC.096\";",
			"6k1/5p2/p5np/4B3/3P4/1PP1q3/P3r1QP/6RK w - - bm Qa8+; id \"WAC.097\";",
			"1r3rk1/5pb1/p2p2p1/Q1n1q2p/1NP1P3/3p1P1B/PP1R3P/1K2R3 b - - bm Nxe4; id \"WAC.098\";",
			"r1bq1r1k/1pp1Np1p/p2p2pQ/4R3/n7/8/PPPP1PPP/R1B3K1 w - - bm Rh5; id \"WAC.099\";",
			"8/k1b5/P4p2/1Pp2p1p/K1P2P1P/8/3B4/8 w - - bm Be3 b6+; id \"WAC.100\";", "5rk1/p5pp/8/8/2Pbp3/1P4P1/7P/4RN1K b - - bm Bc3; id \"WAC.101\";",
			"2Q2n2/2R4p/1p1qpp1k/8/3P3P/3B2P1/5PK1/r7 w - - bm Qxf8+; id \"WAC.102\";",
			"6k1/2pb1r1p/3p1PpQ/p1nPp3/1q2P3/2N2P2/PrB5/2K3RR w - - bm Qxg6+; id \"WAC.103\";",
			"b4r1k/pq2rp2/1p1bpn1p/3PN2n/2P2P2/P2B3K/1B2Q2N/3R2R1 w - - bm Qxh5; id \"WAC.104\";",
			"r2r2k1/pb3ppp/1p1bp3/7q/3n2nP/PP1B2P1/1B1N1P2/RQ2NRK1 b - - bm Bxg3 Qxh4; id \"WAC.105\";",
			"4rrk1/pppb4/7p/3P2pq/3Qn3/P5P1/1PP4P/R3RNNK b - - bm Nf2+; id \"WAC.106\";", "5n2/pRrk2p1/P4p1p/4p3/3N4/5P2/6PP/6K1 w - - bm Nb5; id \"WAC.107\";",
			"r5k1/1q4pp/2p5/p1Q5/2P5/5R2/4RKPP/r7 w - - bm Qe5; id \"WAC.108\";",
			"rn2k1nr/pbp2ppp/3q4/1p2N3/2p5/QP6/PB1PPPPP/R3KB1R b KQkq - bm c3; id \"WAC.109\";",
			"2kr4/bp3p2/p2p2b1/P7/2q5/1N4B1/1PPQ2P1/2KR4 b - - bm Be3; id \"WAC.110\";",
			"6k1/p5p1/5p2/2P2Q2/3pN2p/3PbK1P/7P/6q1 b - - bm Qf1+; id \"WAC.111\";",
			"r4kr1/ppp5/4bq1b/7B/2PR1Q1p/2N3P1/PP3P1P/2K1R3 w - - bm Rxe6; id \"WAC.112\";",
			"rnbqkb1r/1p3ppp/5N2/1p2p1B1/2P5/8/PP2PPPP/R2QKB1R b KQkq - bm Qxf6; id \"WAC.113\";",
			"r1b1rnk1/1p4pp/p1p2p2/3pN2n/3P1PPq/2NBPR1P/PPQ5/2R3K1 w - - bm Bxh7+; id \"WAC.114\";",
			"4N2k/5rpp/1Q6/p3q3/8/P5P1/1P3P1P/5K2 w - - bm Nd6; id \"WAC.115\";",
			"r2r2k1/2p2ppp/p7/1p2P1n1/P6q/5P2/1PB1QP1P/R5RK b - - bm Rd2; id \"WAC.116\";",
			"3r1rk1/q4ppp/p1Rnp3/8/1p6/1N3P2/PP3QPP/3R2K1 b - - bm Ne4; id \"WAC.117\";",
			"r5k1/pb2rpp1/1p6/2p4q/5R2/2PB2Q1/P1P3PP/5R1K w - - bm Rh4; id \"WAC.118\";",
			"r2qr1k1/p1p2ppp/2p5/2b5/4nPQ1/3B4/PPP3PP/R1B2R1K b - - bm Qxd3; id \"WAC.119\";",
			"r4rk1/1bn2qnp/3p1B1Q/p2P1pP1/1pp5/5N1P/PPB2P2/2KR3R w - - bm Rhg1 g6; id \"WAC.120\";",
			"6k1/5p1p/2bP2pb/4p3/2P5/1p1pNPPP/1P1Q1BK1/1q6 b - - bm Bxf3+; id \"WAC.121\";",
			"1k6/ppp4p/1n2pq2/1N2Rb2/2P2Q2/8/P4KPP/3r1B2 b - - bm Rxf1+; id \"WAC.122\";",
			"6k1/1b2rp2/1p4p1/3P4/PQ4P1/2N2q2/5P2/3R2K1 b - - bm Bxd5 Rc7 Re6; id \"WAC.123\";",
			"6k1/3r4/2R5/P5P1/1P4p1/8/4rB2/6K1 b - - bm g3; id \"WAC.124\";",
			"r1bqr1k1/pp3ppp/1bp5/3n4/3B4/2N2P1P/PPP1B1P1/R2Q1RK1 b - - bm Bxd4+; id \"WAC.125\";",
			"r5r1/pQ5p/1qp2R2/2k1p3/4P3/2PP4/P1P3PP/6K1 w - - bm Rxc6+; id \"WAC.126\";",
			"2k4r/1pr1n3/p1p1q2p/5pp1/3P1P2/P1P1P3/1R2Q1PP/1RB3K1 w - - bm Rxb7; id \"WAC.127\";",
			"6rk/1pp2Qrp/3p1B2/1pb1p2R/3n1q2/3P4/PPP3PP/R6K w - - bm Qg6; id \"WAC.128\";",
			"3r1r1k/1b2b1p1/1p5p/2p1Pp2/q1B2P2/4P2P/1BR1Q2K/6R1 b - - bm Bf3; id \"WAC.129\";",
			"6k1/1pp3q1/5r2/1PPp4/3P1pP1/3Qn2P/3B4/4R1K1 b - - bm Qh6 Qh8; id \"WAC.130\";",
			"2rq1bk1/p4p1p/1p4p1/3b4/3B1Q2/8/P4PpP/3RR1K1 w - - bm Re8; id \"WAC.131\";",
			"4r1k1/5bpp/2p5/3pr3/8/1B3pPq/PPR2P2/2R2QK1 b - - bm Re1; id \"WAC.132\";",
			"r1b1k2r/1pp1q2p/p1n3p1/3QPp2/8/1BP3B1/P5PP/3R1RK1 w kq - bm Bh4; id \"WAC.133\";",
			"3r2k1/p6p/2Q3p1/4q3/2P1p3/P3Pb2/1P3P1P/2K2BR1 b - - bm Rd1+; id \"WAC.134\";",
			"3r1r1k/N2qn1pp/1p2np2/2p5/2Q1P2N/3P4/PP4PP/3R1RK1 b - - bm Nd4; id \"WAC.135\";",
			"6kr/1q2r1p1/1p2N1Q1/5p2/1P1p4/6R1/7P/2R3K1 w - - bm Rc8+; id \"WAC.136\";",
			"3b1rk1/1bq3pp/5pn1/1p2rN2/2p1p3/2P1B2Q/1PB2PPP/R2R2K1 w - - bm Rd7; id \"WAC.137\";",
			"r1bq3r/ppppR1p1/5n1k/3P4/6pP/3Q4/PP1N1PP1/5K1R w - - bm h5; id \"WAC.138\";",
			"rnb3kr/ppp2ppp/1b6/3q4/3pN3/Q4N2/PPP2KPP/R1B1R3 w - - bm Nf6+; id \"WAC.139\";",
			"r2b1rk1/pq4p1/4ppQP/3pB1p1/3P4/2R5/PP3PP1/5RK1 w - - bm Bc7 Rc7; id \"WAC.140\";",
			"4r1k1/p1qr1p2/2pb1Bp1/1p5p/3P1n1R/1B3P2/PP3PK1/2Q4R w - - bm Qxf4; id \"WAC.141\";",
			"r2q3n/ppp2pk1/3p4/5Pr1/2NP1Qp1/2P2pP1/PP3K2/4R2R w - - bm Re8 f6+; id \"WAC.142\";",
			"5b2/pp2r1pk/2pp1pRp/4rP1N/2P1P3/1P4QP/P3q1P1/5R1K w - - bm Rxh6+; id \"WAC.143\";",
			"r2q1rk1/pp3ppp/2p2b2/8/B2pPPb1/7P/PPP1N1P1/R2Q1RK1 b - - bm d3; id \"WAC.144\";",
			"r1bq4/1p4kp/3p1n2/p4pB1/2pQ4/8/1P4PP/4RRK1 w - - bm Re8; id \"WAC.145\";", "8/8/2Kp4/3P1B2/2P2k2/5p2/8/8 w - - bm Bc8 Bd3 Bh3; id \"WAC.146\";",
			"r2r2k1/ppqbppbp/2n2np1/2pp4/6P1/1P1PPNNP/PBP2PB1/R2QK2R b KQ - bm Nxg4; id \"WAC.147\";",
			"2r1k3/6pr/p1nBP3/1p3p1p/2q5/2P5/P1R4P/K2Q2R1 w - - bm Rxg7; id \"WAC.148\";",
			"6k1/6p1/2p4p/4Pp2/4b1qP/2Br4/1P2RQPK/8 b - - bm Bxg2; id \"WAC.149\";",
			"r3r1k1/5p2/pQ1b2pB/1p6/4p3/6P1/Pq2BP1P/2R3K1 b - - bm e3; id \"WAC.150\";",
			"8/3b2kp/4p1p1/pr1n4/N1N4P/1P4P1/1K3P2/3R4 w - - bm Nc3; id \"WAC.151\";",
			"1br2rk1/1pqb1ppp/p3pn2/8/1P6/P1N1PN1P/1B3PP1/1QRR2K1 w - - bm Ne4; id \"WAC.152\";",
			"2r3k1/q4ppp/p3p3/pnNp4/2rP4/2P2P2/4R1PP/2R1Q1K1 b - - bm Nxd4; id \"WAC.153\";",
			"r1b2rk1/2p2ppp/p7/1p6/3P3q/1BP3bP/PP3QP1/RNB1R1K1 w - - bm Qxf7+; id \"WAC.154\";",
			"5bk1/1rQ4p/5pp1/2pP4/3n1PP1/7P/1q3BB1/4R1K1 w - - bm d6; id \"WAC.155\";",
			"r1b1qN1k/1pp3p1/p2p3n/4p1B1/8/1BP4Q/PP3KPP/8 w - - bm Qxh6+; id \"WAC.156\";",
			"5rk1/p4ppp/2p1b3/3Nq3/4P1n1/1p1B2QP/1PPr2P1/1K2R2R w - - bm Ne7+; id \"WAC.157\";",
			"5rk1/n1p1R1bp/p2p4/1qpP1QB1/7P/2P3P1/PP3P2/6K1 w - - bm Rxg7+; id \"WAC.158\";",
			"r1b2r2/5P1p/ppn3pk/2p1p1Nq/1bP1PQ2/3P4/PB4BP/1R3RK1 w - - bm Ne6+; id \"WAC.159\";",
			"qn1kr2r/1pRbb3/pP5p/P2pP1pP/3N1pQ1/3B4/3B1PP1/R5K1 w - - bm Qxd7+; id \"WAC.160\";",
			"3r3k/3r1P1p/pp1Nn3/2pp4/7Q/6R1/Pq4PP/5RK1 w - - bm Qxd8+; id \"WAC.161\";",
			"r3kbnr/p4ppp/2p1p3/8/Q1B3b1/2N1B3/PP3PqP/R3K2R w KQkq - bm Bd5; id \"WAC.162\";",
			"5rk1/2p4p/2p4r/3P4/4p1b1/1Q2NqPp/PP3P1K/R4R2 b - - bm Qg2+; id \"WAC.163\";",
			"8/6pp/4p3/1p1n4/1NbkN1P1/P4P1P/1PR3K1/r7 w - - bm Rxc4+; id \"WAC.164\";",
			"1r5k/p1p3pp/8/8/4p3/P1P1R3/1P1Q1qr1/2KR4 w - - bm Re2; id \"WAC.165\";",
			"r3r1k1/5pp1/p1p4p/2Pp4/8/q1NQP1BP/5PP1/4K2R b K - bm d4; id \"WAC.166\";",
			"7Q/ppp2q2/3p2k1/P2Ppr1N/1PP5/7R/5rP1/6K1 b - - bm Rxg2+; id \"WAC.167\";",
			"r3k2r/pb1q1p2/8/2p1pP2/4p1p1/B1P1Q1P1/P1P3K1/R4R2 b kq - bm Qd2+; id \"WAC.168\";",
			"5rk1/1pp3bp/3p2p1/2PPp3/1P2P3/2Q1B3/4q1PP/R5K1 b - - bm Bh6; id \"WAC.169\";",
			"5r1k/6Rp/1p2p3/p2pBp2/1qnP4/4P3/Q4PPP/6K1 w - - bm Qxc4; id \"WAC.170\";",
			"2rq4/1b2b1kp/p3p1p1/1p1nNp2/7P/1B2B1Q1/PP3PP1/3R2K1 w - - bm Bh6+; id \"WAC.171\";",
			"5r1k/p5pp/8/1P1pq3/P1p2nR1/Q7/5BPP/6K1 b - - bm Qe1+; id \"WAC.172\";",
			"2r1b3/1pp1qrk1/p1n1P1p1/7R/2B1p3/4Q1P1/PP3PP1/3R2K1 w - - bm Qh6+; id \"WAC.173\";",
			"2r2rk1/6p1/p3pq1p/1p1b1p2/3P1n2/PP3N2/3N1PPP/1Q2RR1K b - - bm Nxg2; id \"WAC.174\";",
			"r5k1/pppb3p/2np1n2/8/3PqNpP/3Q2P1/PPP5/R4RK1 w - - bm Nh5; id \"WAC.175\";",
			"r1bq3r/ppp2pk1/3p1pp1/8/2BbPQ2/2NP2P1/PPP4P/R4R1K b - - bm Rxh2+; id \"WAC.176\";",
			"r1b3r1/4qk2/1nn1p1p1/3pPp1P/p4P2/1p3BQN/PKPBN3/3R3R b - - bm Qa3+; id \"WAC.177\";",
			"3r2k1/p1rn1p1p/1p2pp2/6q1/3PQNP1/5P2/P1P4R/R5K1 w - - bm Nxe6; id \"WAC.178\";",
			"r1b2r1k/pp4pp/3p4/3B4/8/1QN3Pn/PP3q1P/R3R2K b - - bm Qg1+; id \"WAC.179\";",
			"r1q2rk1/p3bppb/3p1n1p/2nPp3/1p2P1P1/6NP/PP2QPB1/R1BNK2R b KQ - bm Nxd5; id \"WAC.180\";",
			"r3k2r/2p2p2/p2p1n2/1p2p3/4P2p/1PPPPp1q/1P5P/R1N2QRK b kq - bm Ng4; id \"WAC.181\";",
			"r1b2rk1/ppqn1p1p/2n1p1p1/2b3N1/2N5/PP1BP3/1B3PPP/R2QK2R w KQ - bm Qh5; id \"WAC.182\";",
			"1r2k1r1/5p2/b3p3/1p2b1B1/3p3P/3B4/PP2KP2/2R3R1 w - - bm Bf6; id \"WAC.183\";",
			"4kn2/r4p1r/p3bQ2/q1nNP1Np/1p5P/8/PPP3P1/2KR3R w - - bm Qe7+; id \"WAC.184\";",
			"1r1rb1k1/2p3pp/p2q1p2/3PpP1Q/Pp1bP2N/1B5R/1P4PP/2B4K w - - bm Qxh7+; id \"WAC.185\";",
			"r5r1/p1q2p1k/1p1R2pB/3pP3/6bQ/2p5/P1P1NPPP/6K1 w - - bm Bf8+; id \"WAC.186\";",
			"6k1/5p2/p3p3/1p3qp1/2p1Qn2/2P1R3/PP1r1PPP/4R1K1 b - - bm Nh3+; id \"WAC.187\";",
			"3RNbk1/pp3p2/4rQpp/8/1qr5/7P/P4P2/3R2K1 w - - bm Qg7+; id \"WAC.188\";",
			"3r1k2/1ppPR1n1/p2p1rP1/3P3p/4Rp1N/5K2/P1P2P2/8 w - - bm Re8+; id \"WAC.189\";",
			"8/p2b2kp/1q1p2p1/1P1Pp3/4P3/3B2P1/P2Q3P/2Nn3K b - - bm Bh3; id \"WAC.190\";",
			"2r1Rn1k/1p1q2pp/p7/5p2/3P4/1B4P1/P1P1QP1P/6K1 w - - bm Qc4; id \"WAC.191\";",
			"r3k3/ppp2Npp/4Bn2/2b5/1n1pp3/N4P2/PPP3qP/R2QKR2 b Qq - bm Nd3+; id \"WAC.192\";",
			"5bk1/p4ppp/Qp6/4B3/1P6/Pq2P1P1/2rr1P1P/R4RK1 b - - bm Qxe3; id \"WAC.193\";",
			"5rk1/ppq2ppp/2p5/4bN2/4P3/6Q1/PPP2PPP/3R2K1 w - - bm Nh6+; id \"WAC.194\";",
			"3r1rk1/1p3p2/p3pnnp/2p3p1/2P2q2/1P5P/PB2QPPN/3RR1K1 w - - bm g3; id \"WAC.195\";",
			"rr4k1/p1pq2pp/Q1n1pn2/2bpp3/4P3/2PP1NN1/PP3PPP/R1B1K2R b KQ - bm Nb4; id \"WAC.196\";",
			"7k/1p4p1/7p/3P1n2/4Q3/2P2P2/PP3qRP/7K b - - bm Qf1+; id \"WAC.197\";",
			"2br2k1/ppp2p1p/4p1p1/4P2q/2P1Bn2/2Q5/PP3P1P/4R1RK b - - bm Rd3; id \"WAC.198\";",
			"r1br2k1/pp2nppp/2n5/1B1q4/Q7/4BN2/PP3PPP/2R2RK1 w - - bm Bxc6 Rcd1 Rfd1; id \"WAC.199\";",
			"2rqrn1k/pb4pp/1p2pp2/n2P4/2P3N1/P2B2Q1/1B3PPP/2R1R1K1 w - - bm Bxf6; id \"WAC.200\";",
			"2b2r1k/4q2p/3p2pQ/2pBp3/8/6P1/1PP2P1P/R5K1 w - - bm Ra7; id \"WAC.201\";",
			"QR2rq1k/2p3p1/3p1pPp/8/4P3/8/P1r3PP/1R4K1 b - - bm Rxa2; id \"WAC.202\";",
			"r4rk1/5ppp/p3q1n1/2p2NQ1/4n3/P3P3/1B3PPP/1R3RK1 w - - bm Qh6; id \"WAC.203\";",
			"r1b1qrk1/1p3ppp/p1p5/3Nb3/5N2/P7/1P4PQ/K1R1R3 w - - bm Rxe5; id \"WAC.204\";",
			"r3rnk1/1pq2bb1/p4p2/3p1Pp1/3B2P1/1NP4R/P1PQB3/2K4R w - - bm Qxg5; id \"WAC.205\";",
			"1Qq5/2P1p1kp/3r1pp1/8/8/7P/p4PP1/2R3K1 b - - bm Rc6; id \"WAC.206\";",
			"r1bq2kr/p1pp1ppp/1pn1p3/4P3/2Pb2Q1/BR6/P4PPP/3K1BNR w - - bm Qxg7+; id \"WAC.207\";",
			"3r1bk1/ppq3pp/2p5/2P2Q1B/8/1P4P1/P6P/5RK1 w - - bm Bf7+; id \"WAC.208\";",
			"4kb1r/2q2p2/r2p4/pppBn1B1/P6P/6Q1/1PP5/2KRR3 w k - bm Rxe5+; id \"WAC.209\";",
			"3r1rk1/pp1q1ppp/3pn3/2pN4/5PP1/P5PQ/1PP1B3/1K1R4 w - - bm Rh1; id \"WAC.210\";",
			"r1bqrk2/pp1n1n1p/3p1p2/P1pP1P1Q/2PpP1NP/6R1/2PB4/4RBK1 w - - bm Qxf7+; id \"WAC.211\";",
			"rn1qr2Q/pbppk1p1/1p2pb2/4N3/3P4/2N5/PPP3PP/R4RK1 w - - bm Qxg7+; id \"WAC.212\";",
			"3r1r1k/1b4pp/ppn1p3/4Pp1R/Pn5P/3P4/4QP2/1qB1NKR1 w - - bm Rxh7+; id \"WAC.213\";",
			"r2r2k1/1p2qpp1/1np1p1p1/p3N3/2PPN3/bP5R/4QPPP/4R1K1 w - - bm Ng5; id \"WAC.214\";",
			"3r2k1/pb1q1pp1/1p2pb1p/8/3N4/P2QB3/1P3PPP/1Br1R1K1 w - - bm Qh7+; id \"WAC.215\";",
			"r2qr1k1/1b1nbppp/p3pn2/1p1pN3/3P1B2/2PB1N2/PP2QPPP/R4RK1 w - - bm Nxf7 a4; id \"WAC.216\";",
			"r3kb1r/1pp3p1/p3bp1p/5q2/3QN3/1P6/PBP3P1/3RR1K1 w kq - bm Qd7+; id \"WAC.217\";",
			"6k1/pp5p/2p3q1/6BP/2nPr1Q1/8/PP3R1K/8 w - - bm Bh6; id \"WAC.218\";", "7k/p4q1p/1pb5/2p5/4B2Q/2P1B3/P6P/7K b - - bm Qf1+; id \"WAC.219\";",
			"3rr1k1/ppp2ppp/8/5Q2/4n3/1B5R/PPP1qPP1/5RK1 b - - bm Qxf1+; id \"WAC.220\";",
			"r3k3/P5bp/2N1bp2/4p3/2p5/6NP/1PP2PP1/3R2K1 w q - bm Rd8+; id \"WAC.221\";",
			"2r1r2k/1q3ppp/p2Rp3/2p1P3/6QB/p3P3/bP3PPP/3R2K1 w - - bm Bf6; id \"WAC.222\";",
			"r1bqk2r/pp3ppp/5n2/8/1b1npB2/2N5/PP1Q2PP/1K2RBNR w kq - bm Nxe4; id \"WAC.223\";",
			"5rk1/p1q3pp/1p1r4/2p1pp1Q/1PPn1P2/3B3P/P2R2P1/3R2K1 b - - bm Rh6 e4; id \"WAC.224\";",
			"4R3/4q1kp/6p1/1Q3b2/1P1b1P2/6KP/8/8 b - - bm Qh4+; id \"WAC.225\";",
			"2b2rk1/p1p4p/2p1p1p1/br2N1Q1/1p2q3/8/PB3PPP/3R1RK1 w - - bm Nf7; id \"WAC.226\";",
			"2k1rb1r/ppp3pp/2np1q2/5b2/2B2P2/2P1BQ2/PP1N1P1P/2KR3R b - - bm d5; id \"WAC.227\";",
			"r4rk1/1bq1bp1p/4p1p1/p2p4/3BnP2/1N1B3R/PPP3PP/R2Q2K1 w - - bm Bxe4; id \"WAC.228\";",
			"8/8/8/1p5r/p1p1k1pN/P2pBpP1/1P1K1P2/8 b - - bm Rxh4 b4; id \"WAC.229\";",
			"2b5/1r6/2kBp1p1/p2pP1P1/2pP4/1pP3K1/1R3P2/8 b - - bm Rb4; id \"WAC.230\";", // fails: (rook sacrifice,
																							// king-passed-pawn
																							// tropism, stockfish
																							// heeft deze ook
																							// fout...)
			"r4rk1/1b1nqp1p/p5p1/1p2PQ2/2p5/5N2/PP3PPP/R1BR2K1 w - - bm Bg5; id \"WAC.231\";",
			"1R2rq1k/2p3p1/Q2p1pPp/8/4P3/8/P1r3PP/1R4K1 w - - bm Qb5 Rxe8; id \"WAC.232\";",
			"5rk1/p1p2r1p/2pp2p1/4p3/PPPnP3/3Pq1P1/1Q1R1R1P/4NK2 b - - bm Nb3; id \"WAC.233\";",
			"2kr1r2/p6p/5Pp1/2p5/1qp2Q1P/7R/PP6/1KR5 w - - bm Rb3; id \"WAC.234\";",
			"5r2/1p1RRrk1/4Qq1p/1PP3p1/8/4B3/1b3P1P/6K1 w - - bm Qe4 Qxf7+ Rxf7+; id \"WAC.235\";",
			"1R6/p5pk/4p2p/4P3/8/2r3qP/P3R1b1/4Q1K1 b - - bm Rc1; id \"WAC.236\";", "r5k1/pQp2qpp/8/4pbN1/3P4/6P1/PPr4P/1K1R3R b - - bm Rc1+; id \"WAC.237\";",
			"1k1r4/pp1r1pp1/4n1p1/2R5/2Pp1qP1/3P2QP/P4PB1/1R4K1 w - - bm Bxb7; id \"WAC.238\";",
			"8/6k1/5pp1/Q6p/5P2/6PK/P4q1P/8 b - - bm Qf1+; id \"WAC.239\";", "2b4k/p1b2p2/2p2q2/3p1PNp/3P2R1/3B4/P1Q2PKP/4r3 w - - bm Qxc6; id \"WAC.240\";",
			"2rq1rk1/pp3ppp/2n2b2/4NR2/3P4/PB5Q/1P4PP/3R2K1 w - - bm Qxh7+; id \"WAC.241\";",
			"r1b1r1k1/pp1nqp2/2p1p1pp/8/4N3/P1Q1P3/1P3PPP/1BRR2K1 w - - bm Rxd7; id \"WAC.242\";",
			"1r3r1k/3p4/1p1Nn1R1/4Pp1q/pP3P1p/P7/5Q1P/6RK w - - bm Qe2; id \"WAC.243\";",
			"r6r/pp3ppp/3k1b2/2pb4/B4Pq1/2P1Q3/P5PP/1RBR2K1 w - - bm Qxc5+; id \"WAC.244\";",
			"4rrn1/ppq3bk/3pPnpp/2p5/2PB4/2NQ1RPB/PP5P/5R1K w - - bm Qxg6+; id \"WAC.245\";",
			"6R1/4qp1p/ppr1n1pk/8/1P2P1QP/6N1/P4PP1/6K1 w - - bm Qh5+; id \"WAC.246\";",
			"2k1r3/1p2Bq2/p2Qp3/Pb1p1p1P/2pP1P2/2P5/2P2KP1/1R6 w - - bm Rxb5; id \"WAC.247\";",
			"5r1k/1p4pp/3q4/3Pp1R1/8/8/PP4PP/4Q1K1 b - - bm Qc5+; id \"WAC.248\";",
			"r4rk1/pbq2pp1/1ppbpn1p/8/2PP4/1P1Q1N2/PBB2PPP/R3R1K1 w - - bm c5 d5; id \"WAC.249\";",
			"1b5k/7P/p1p2np1/2P2p2/PP3P2/4RQ1R/q2r3P/6K1 w - - bm Re8+; id \"WAC.250\";",
			"k7/p4p2/P1q1b1p1/3p3p/3Q4/7P/5PP1/1R4K1 w - - bm Qe5 Qf4; id \"WAC.251\";",
			"1rb1r1k1/p1p2ppp/5n2/2pP4/5P2/2QB4/qNP3PP/2KRB2R b - - bm Re2; id \"WAC.252\";",
			"k5r1/p4b2/2P5/5p2/3P1P2/4QBrq/P5P1/4R1K1 w - - bm Qe8+; id \"WAC.253\";",
			"r6k/pp3p1p/2p1bp1q/b3p3/4Pnr1/2PP2NP/PP1Q1PPN/R2B2RK b - - bm Nxh3; id \"WAC.254\";", // fails
																									// ................
			"3r3r/p4pk1/5Rp1/3q4/1p1P2RQ/5N2/P1P4P/2b4K w - - bm Rfxg6+; id \"WAC.255\";",
			"3r1rk1/1pb1qp1p/2p3p1/p7/P2Np2R/1P5P/1BP2PP1/3Q1BK1 w - - bm Nf5; id \"WAC.256\";",
			"4r1k1/pq3p1p/2p1r1p1/2Q1p3/3nN1P1/1P6/P1P2P1P/3RR1K1 w - - bm Rxd4; id \"WAC.257\";",
			"r3brkn/1p5p/2p2Ppq/2Pp3B/3Pp2Q/4P1R1/6PP/5R1K w - - bm Bxg6; id \"WAC.258\";",
			"r1bq1rk1/ppp2ppp/2np4/2bN1PN1/2B1P3/3p4/PPP2nPP/R1BQ1K1R w - - bm Qh5; id \"WAC.259\";",
			"2r2b1r/p1Nk2pp/3p1p2/N2Qn3/4P3/q6P/P4PP1/1R3K1R w - - bm Qe6+; id \"WAC.260\";",
			"r5k1/1bp3pp/p2p4/1p6/5p2/1PBP1nqP/1PP3Q1/R4R1K b - - bm Nd4; id \"WAC.261\";",
			"6k1/p1B1b2p/2b3r1/2p5/4p3/1PP1N1Pq/P2R1P2/3Q2K1 b - - bm Rh6; id \"WAC.262\";",
			"rnbqr2k/pppp1Qpp/8/b2NN3/2B1n3/8/PPPP1PPP/R1B1K2R w KQ - bm Qg8+; id \"WAC.263\";",
			"r2r2k1/1R2qp2/p5pp/2P5/b1PN1b2/P7/1Q3PPP/1B1R2K1 b - - bm Qe5 Rab8; id \"WAC.264\";",
			"2r1k2r/2pn1pp1/1p3n1p/p3PP2/4q2B/P1P5/2Q1N1PP/R4RK1 w k - bm exf6; id \"WAC.265\";",
			"r3q2r/2p1k1p1/p5p1/1p2Nb2/1P2nB2/P7/2PNQbPP/R2R3K b - - bm Rxh2+; id \"WAC.266\";",
			"2r1kb1r/pp3ppp/2n1b3/1q1N2B1/1P2Q3/8/P4PPP/3RK1NR w Kk - bm Nc7+; id \"WAC.267\";",
			"2r3kr/ppp2n1p/7B/5q1N/1bp5/2Pp4/PP2RPPP/R2Q2K1 w - - bm Re8+; id \"WAC.268\";",
			"2kr2nr/pp1n1ppp/2p1p3/q7/1b1P1B2/P1N2Q1P/1PP1BPP1/R3K2R w KQ - bm axb4; id \"WAC.269\";",
			"2r1r1k1/pp1q1ppp/3p1b2/3P4/3Q4/5N2/PP2RPPP/4R1K1 w - - bm Qg4; id \"WAC.270\";",
			"2kr4/ppp3Pp/4RP1B/2r5/5P2/1P6/P2p4/3K4 w - - bm Rd6; id \"WAC.271\";",
			"nrq4r/2k1p3/1p1pPnp1/pRpP1p2/P1P2P2/2P1BB2/1R2Q1P1/6K1 w - - bm Bxc5; id \"WAC.272\";",
			"2k4B/bpp1qp2/p1b5/7p/1PN1n1p1/2Pr4/P5PP/R3QR1K b - - bm Ng3+ g3; id \"WAC.273\";", "8/1p6/p5R1/k7/Prpp4/K7/1NP5/8 w - - bm Rb6; id \"WAC.274\";",
			"r1b2rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN2N2/1P3PPP/3R1RK1 b - - bm Nc5 Nxg2 b5; id \"WAC.275\";",
			"r5k1/pp1RR1pp/1b6/6r1/2p5/B6P/P4qPK/3Q4 w - - bm Qd5+; id \"WAC.276\";",
			"1r4r1/p2kb2p/bq2p3/3p1p2/5P2/2BB3Q/PP4PP/3RKR2 b - - bm Rg3 Rxg2; id \"WAC.277\";",
			"r2qkb1r/pppb2pp/2np1n2/5pN1/2BQP3/2N5/PPP2PPP/R1B1K2R w KQkq - bm Bf7+; id \"WAC.278\";",
			"r7/4b3/2p1r1k1/1p1pPp1q/1P1P1P1p/PR2NRpP/2Q3K1/8 w - - bm Nxf5 Rc3; id \"WAC.279\";",
			"r1r2bk1/5p1p/pn4p1/N2b4/3Pp3/B3P3/2q1BPPP/RQ3RK1 b - - bm Bxa3; id \"WAC.280\";",
			"2R5/2R4p/5p1k/6n1/8/1P2QPPq/r7/6K1 w - - bm Rxh7+; id \"WAC.281\";", "6k1/2p3p1/1p1p1nN1/1B1P4/4PK2/8/2r3b1/7R w - - bm Rh8+; id \"WAC.282\";",
			"3q1rk1/4bp1p/1n2P2Q/3p1p2/6r1/Pp2R2N/1B4PP/7K w - - bm Ng5; id \"WAC.283\";",
			"3r3k/pp4pp/8/1P6/3N4/Pn2P1qb/1B1Q2B1/2R3K1 w - - bm Nf5; id \"WAC.284\";",
			"2rr3k/1b2bppP/p2p1n2/R7/3P4/1qB2P2/1P4Q1/1K5R w - - bm Qxg7+; id \"WAC.285\";",
			"3r1k2/1p6/p4P2/2pP2Qb/8/1P1KB3/P6r/8 b - - bm Rxd5+; id \"WAC.286\";",
			"rn3k1r/pp2bBpp/2p2n2/q5N1/3P4/1P6/P1P3PP/R1BQ1RK1 w - - bm Qg4 Qh5; id \"WAC.287\";",
			"r1b2rk1/p4ppp/1p1Qp3/4P2N/1P6/8/P3qPPP/3R1RK1 w - - bm Nf6+; id \"WAC.288\";",
			"2r3k1/5p1p/p3q1p1/2n3P1/1p1QP2P/1P4N1/PK6/2R5 b - - bm Qe5; id \"WAC.289\";",
			"2k2r2/2p5/1pq5/p1p1n3/P1P2n1B/1R4Pp/2QR4/6K1 b - - bm Ne2+; id \"WAC.290\";",
			"5r1k/3b2p1/p6p/1pRpR3/1P1P2q1/P4pP1/5QnP/1B4K1 w - - bm h3; id \"WAC.291\";",
			"4r3/1Q1qk2p/p4pp1/3Pb3/P7/6PP/5P2/4R1K1 w - - bm d6+; id \"WAC.292\";",
			"1nbq1r1k/3rbp1p/p1p1pp1Q/1p6/P1pPN3/5NP1/1P2PPBP/R4RK1 w - - bm Nfg5; id \"WAC.293\";",
			"3r3k/1r3p1p/p1pB1p2/8/p1qNP1Q1/P6P/1P4P1/3R3K w - - bm Bf8 Nf5 Qf4; id \"WAC.294\";",
			"4r3/p4r1p/R1p2pp1/1p1bk3/4pNPP/2P1K3/2P2P2/3R4 w - - bm Rxd5+; id \"WAC.295\";",
			"3r4/1p2k2p/p1b1p1p1/4Q1Pn/2B3KP/4pP2/PP2R1N1/6q1 b - - bm Rd4+ Rf8; id \"WAC.296\";",
			"3r1rk1/p3qp1p/2bb2p1/2p5/3P4/1P6/PBQN1PPP/2R2RK1 b - - bm Bxg2 Bxh2+; id \"WAC.297\";",
			"3Q4/p3b1k1/2p2rPp/2q5/4B3/P2P4/7P/6RK w - - bm Qh8+; id \"WAC.298\";",
			"1n2rr2/1pk3pp/pNn2p2/2N1p3/8/6P1/PP2PPKP/2RR4 w - - bm Nca4; id \"WAC.299\";",
			"b2b1r1k/3R1ppp/4qP2/4p1PQ/4P3/5B2/4N1K1/8 w - - bm g6; id \"WAC.300\";" };

}
