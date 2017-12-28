package nl.s22k.chess.eval;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;

public class SchroderUtil {

	public static final int FLAG_PAWN = (int) Util.POWER_LOOKUP[ChessConstants.PAWN - 1];
	public static final int FLAG_NIGHT = (int) Util.POWER_LOOKUP[ChessConstants.NIGHT - 1];
	public static final int FLAG_BISHOP = (int) Util.POWER_LOOKUP[ChessConstants.BISHOP - 1];
	public static final int FLAG_ROOK = (int) Util.POWER_LOOKUP[ChessConstants.ROOK - 1];
	public static final int FLAG_QUEEN = (int) Util.POWER_LOOKUP[ChessConstants.QUEEN - 1];

}
