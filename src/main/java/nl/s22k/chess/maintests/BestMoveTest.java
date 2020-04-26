package nl.s22k.chess.maintests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.engine.UciOut;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.search.SearchUtil;
import nl.s22k.chess.search.TTUtil;
import nl.s22k.chess.search.ThreadData;
import nl.s22k.chess.search.TimeUtil;

public class BestMoveTest {

	public static int positionTestOK, positionTestNOK;
	public static ThreadData threadData = ThreadData.getInstance(0);

	public static void main(String[] args) {
		UciOut.noOutput = true;
		TTUtil.init(false);

		doTest(getEpdStrings("WAC-201.epd"));
		// doTest(getEpdStrings("EigenmannEndgame.epd"));

		System.out.println("");
		System.out.println("Total: " + positionTestOK + "/" + (positionTestOK + positionTestNOK));
	}

	public static List<String> getEpdStrings(String fileName) {
		try {
			System.out.println(fileName);
			System.out.println();
			File file = new File(ClassLoader.getSystemClassLoader().getResource(fileName).getFile());
			return Files.readAllLines(file.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void doTest(List<String> epdStrings) {
		int correctCounter = 0;
		for (String epdString : epdStrings) {
			EPD epd = new EPD(epdString);
			ChessBoard cb = ChessBoardInstances.get(0);
			ChessBoardUtil.setFen(epd.getFen(), cb);

			TimeUtil.reset();
			TimeUtil.setSimpleTimeWindow(5000);
			SearchUtil.start(cb);

			MoveWrapper bestMove = new MoveWrapper(threadData.getBestMove());
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
		System.out.println(correctCounter + "/" + epdStrings.size());
	}

}
