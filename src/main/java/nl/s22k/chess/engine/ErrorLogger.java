package nl.s22k.chess.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.search.TTUtil;

public class ErrorLogger {

	private static final Logger logger = Logger.getLogger(ErrorLogger.class.getName());

	public static void log(ChessBoard cb, Throwable t) {
		try {

			// print to System.out
			t.printStackTrace(System.out);

			// setup logger
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
			String completeFilePath = "%t/" + "chess22k_" + sdf.format(new Date()) + ".log";
			Handler fh = new FileHandler(completeFilePath, true);
			fh.setFormatter(new SimpleFormatter());
			logger.addHandler(fh);

			// redirect System.out
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			System.setOut(ps);

			// print info
			System.out.println();
			System.out.println();
			System.out.println("chess22k " + MainEngine.getVersion());
			System.out.println();
			System.out.println("start fen");
			System.out.println(MainEngine.startFen);
			System.out.println();
			System.out.println("crashed fen");
			System.out.println(cb);
			System.out.println();

			// print last move
			System.out.println("last move");
			System.out.println(new MoveWrapper(MoveList.previous()));
			System.out.println("tt move");
			MoveWrapper move = new MoveWrapper(TTUtil.getMove(TTUtil.getTTValue(cb.zobristKeyHistory[cb.moveCounter - 1])));
			System.out.println(move);
			System.out.println("source: " + move.pieceIndex + " target:" + move.pieceIndexAttacked);
			cb.undoMove(move.move);
			System.out.println("valid move: " + cb.isValidMove(move.move));
			System.out.println();

			Statistics.print();
			System.out.flush();

			// print exception
			logger.info(baos.toString());
			logger.log(Level.SEVERE, "An exception occurred", t);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.exit(1);
		}
	}

}
