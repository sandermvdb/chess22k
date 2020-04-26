package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;

import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.StaticMoves;

public final class CheckUtil {

	//@formatter:off


	public static boolean isInCheck(final int kingIndex, final int colorToMove, final long[] enemyPieces, final long allPieces) {
	
		// put 'super-piece' in kings position
		return (enemyPieces[NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (enemyPieces[ROOK] | enemyPieces[QUEEN]) & MagicUtil.getRookMoves(kingIndex, allPieces)
				| (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & MagicUtil.getBishopMoves(kingIndex, allPieces) 
				| enemyPieces[PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex]
			)!= 0;
	}

	public static boolean isInCheckIncludingKing(final int kingIndex, final int colorToMove, final long[] enemyPieces, final long allPieces) {
	
		// put 'super-piece' in kings position
		return (enemyPieces[NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (enemyPieces[ROOK] | enemyPieces[QUEEN]) & MagicUtil.getRookMoves(kingIndex, allPieces)
				| (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & MagicUtil.getBishopMoves(kingIndex, allPieces) 
				| enemyPieces[PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex]
				| enemyPieces[KING] & StaticMoves.KING_MOVES[kingIndex]
			)!= 0;
	}
}
