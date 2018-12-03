package nl.s22k.chess.unittests;

import org.junit.Test;

import nl.s22k.chess.move.MagicUtil;

public class MagicTest {

	@Test
	public void test() {
		System.out.println("Performing board tests");

		// Rival chess
		doRookTest(21, 0x91078010b607600cl, 2483249164l, 551288832l);
		doRookTest(21, 0x91078010b603600cl, 2483249164l, 551288832l);
		doRookTest(21, 0x91078010b601600cl, 2483118092l, 551419904l);
		doRookTest(21, 0x91078010b600600cl, 2483052556l, 551485440l);
		doRookTest(21, 0x910780109600600cl, 2483052556l, 2314885530830962688l);
		doRookTest(21, 0x910780109600400cl, 2483044364l, 2314885530830970912l);

		// corner test rook
		doRookTest(0, 0x910780109600400cl, 2483044364l, 282578800148738l);
		doRookTest(0, 0x900780109600400cl, 2483044364l, 282578800148738l);
		doRookTest(0, 0x900680109600400cl, 2483044364l, 72340172838076674l);
		doRookTest(0, 0x9006801096004004l, 2483044356l, 72340172838076674l);
		doRookTest(0, 0x9006801096004000l, 2483044352l, 72340172838076926l);

		// middle test bishop
		doBishopTest(21, 0x9006801096004000l, 2483044352l, 550829559816l);
		doBishopTest(21, 0x9006801096000000l, 2483027968l, 550829576328l);
		doBishopTest(21, 0x9006801086000000l, 2214592512l, 567933457682568l);
		doBishopTest(21, 0x9004801086000000l, 2214592512l, 72625527495610504l);

		// corner test bishop
		doBishopTest(63, 0x1004801086000001l, 70934069248l, 18049582881570816l);
		doBishopTest(63, 0x1004800086000001l, 2214592512l, 18049651735527937l);

	}

	private static void doRookTest(int bitIndex, long allPieces, long friendly, long destination) {
		long calculatedDestination = MagicUtil.getRookMoves(bitIndex, allPieces) & ~friendly;
		if (calculatedDestination != destination) {
			System.out.println(String.format("Wrong destination!! calculated: %s , should be %s", calculatedDestination, destination));
		}
	}

	private static void doBishopTest(int bitIndex, long allPieces, long friendly, long destination) {
		long calculatedDestination = MagicUtil.getBishopMoves(bitIndex, allPieces) & ~friendly;
		if (calculatedDestination != destination) {
			System.out.println(String.format("Wrong destination!! calculated: %s , should be %s", calculatedDestination, destination));
		}
	}
}
