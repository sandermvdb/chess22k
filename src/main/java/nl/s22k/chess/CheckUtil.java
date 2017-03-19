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

	public static long getCheckingPieces(final ChessBoard cb) {
		final int kingIndex = cb.kingIndex[cb.colorToMove];

		// put 'super-piece' in kings position
		return (cb.pieces[cb.colorToMoveInverse][NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (cb.pieces[cb.colorToMoveInverse][ROOK]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getRookMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove])
				| (cb.pieces[cb.colorToMoveInverse][BISHOP]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getBishopMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) 
				| cb.pieces[cb.colorToMoveInverse][PAWN] & StaticMoves.PAWN_ALL_ATTACKS[cb.colorToMove][kingIndex]
			);
	}
	
	public static long getCheckingPiecesWithoutKnight(final ChessBoard cb) {
		final int kingIndex = cb.kingIndex[cb.colorToMove];

		// put 'super-piece' in kings position
		return ((cb.pieces[cb.colorToMoveInverse][ROOK]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getRookMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove])
				| (cb.pieces[cb.colorToMoveInverse][BISHOP]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getBishopMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) 
				| cb.pieces[cb.colorToMoveInverse][PAWN] & StaticMoves.PAWN_ALL_ATTACKS[cb.colorToMove][kingIndex]
			);
	}
	
	public static long getCheckingPiecesWithoutPawn(final ChessBoard cb) {
		final int kingIndex = cb.kingIndex[cb.colorToMove];

		// put 'super-piece' in kings position
		return (cb.pieces[cb.colorToMoveInverse][NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (cb.pieces[cb.colorToMoveInverse][ROOK]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getRookMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove])
				| (cb.pieces[cb.colorToMoveInverse][BISHOP]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getBishopMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) 
			);
	}
	
	public static long getCheckingPiecesWithoutKnightAndPawn(final ChessBoard cb) {
		final int kingIndex = cb.kingIndex[cb.colorToMove];

		// put 'super-piece' in kings position
		return ((cb.pieces[cb.colorToMoveInverse][ROOK]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getRookMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove])
				| (cb.pieces[cb.colorToMoveInverse][BISHOP]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getBishopMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) 
			);
	}
	
	public static boolean isInCheck(final ChessBoard cb) {
		final int kingIndex = cb.kingIndex[cb.colorToMove];

		// put 'super-piece' in kings position
		return(cb.pieces[cb.colorToMoveInverse][NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (cb.pieces[cb.colorToMoveInverse][ROOK]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getRookMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove])
				| (cb.pieces[cb.colorToMoveInverse][BISHOP]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getBishopMoves(kingIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) 
				|  cb.pieces[cb.colorToMoveInverse][PAWN] & StaticMoves.PAWN_ALL_ATTACKS[cb.colorToMove][kingIndex]
			) != 0;
	}

	public static boolean isInCheck(final int kingIndex, final int colorToMove, final long friendlyPieces, final long[] enemyPieces, final long allPieces) {

		// put 'super-piece' in kings position
		return (enemyPieces[NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (enemyPieces[ROOK] | enemyPieces[QUEEN]) & MagicUtil.getRookMoves(kingIndex, allPieces, friendlyPieces)
				| (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & MagicUtil.getBishopMoves(kingIndex, allPieces, friendlyPieces) 
				| enemyPieces[PAWN] & StaticMoves.PAWN_ALL_ATTACKS[colorToMove][kingIndex]
			)!= 0;
	}
	
	public static boolean isInCheckIncludingKing(final int kingIndex, final int colorToMove, final long friendlyPieces, final long[] enemyPieces, final long allPieces) {

		// put 'super-piece' in kings position
		return (enemyPieces[NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (enemyPieces[ROOK] | enemyPieces[QUEEN]) & MagicUtil.getRookMoves(kingIndex, allPieces, friendlyPieces)
				| (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & MagicUtil.getBishopMoves(kingIndex, allPieces, friendlyPieces) 
				| enemyPieces[PAWN] & StaticMoves.PAWN_ALL_ATTACKS[colorToMove][kingIndex]
				| enemyPieces[KING] & StaticMoves.KING_MOVES[kingIndex]
			)!= 0;
	}
}
