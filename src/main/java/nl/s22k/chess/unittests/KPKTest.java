package nl.s22k.chess.unittests;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.KPKBitbase;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.texel.Tuner;

public class KPKTest {
	
	@Test
	public void test() {
		// setup
		EngineConstants.isTuningSession = true;
		MagicUtil.init();

		// read all fens, including score
		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, true);
		System.out.println("Fens found : " + fens.size());
		
		int tested = 0;
		int ok = 0;
		int nok = 0;
		for (Entry<String, Double> entry : fens.entrySet()) {
			ChessBoard cb = ChessBoardUtil.getNewCB(entry.getKey());
			if(Long.bitCount(cb.allPieces) > 3) {
				continue;
			}
			if(cb.pieces[ChessConstants.WHITE][ChessConstants.PAWN] == 0 && cb.pieces[ChessConstants.BLACK][ChessConstants.PAWN] == 0) {
				continue;
			}
			tested++;
			if (KPKBitbase.isDraw(cb) == (entry.getValue() == 0.5)) {
				ok++;
			} else {
				nok++;
			}
		}
		
		System.out.println();
		System.out.println("Tested " + tested);
		System.out.println("OK     " + ok);
		System.out.println("NOK    " + nok);
	
	}
	

}
