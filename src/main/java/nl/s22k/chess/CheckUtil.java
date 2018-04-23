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
				| (cb.pieces[cb.colorToMoveInverse][ROOK]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getRookMoves(kingIndex, cb.allPieces)
				| (cb.pieces[cb.colorToMoveInverse][BISHOP]|cb.pieces[cb.colorToMoveInverse][QUEEN]) & MagicUtil.getBishopMoves(kingIndex, cb.allPieces) 
				| cb.pieces[cb.colorToMoveInverse][PAWN] & StaticMoves.PAWN_ATTACKS[cb.colorToMove][kingIndex]
			);
	}
	
	public static long getCheckingPieces(final ChessBoard cb, final int sourcePieceIndex) {
		switch(sourcePieceIndex) {
			case PAWN:
				return cb.pieces[cb.colorToMoveInverse][PAWN] & StaticMoves.PAWN_ATTACKS[cb.colorToMove][cb.kingIndex[cb.colorToMove]];
			case NIGHT:
				return cb.pieces[cb.colorToMoveInverse][NIGHT] & StaticMoves.KNIGHT_MOVES[cb.kingIndex[cb.colorToMove]];
			case BISHOP:
				return cb.pieces[cb.colorToMoveInverse][BISHOP] & MagicUtil.getBishopMoves(cb.kingIndex[cb.colorToMove], cb.allPieces);
			case ROOK:
				return cb.pieces[cb.colorToMoveInverse][ROOK] & MagicUtil.getRookMoves(cb.kingIndex[cb.colorToMove], cb.allPieces);
			case QUEEN:
				return cb.pieces[cb.colorToMoveInverse][QUEEN] & MagicUtil.getRookMoves(cb.kingIndex[cb.colorToMove], cb.allPieces) |
						cb.pieces[cb.colorToMoveInverse][QUEEN] & MagicUtil.getBishopMoves(cb.kingIndex[cb.colorToMove], cb.allPieces);
			default:
				//king can never set the other king in check
				return 0;	
		}
	}

	public static boolean isInCheck(final int kingIndex, final int colorToMove, final long[] enemyPieces, final long allPieces) {
	
		// put 'super-piece' in kings position
		return (enemyPieces[NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (enemyPieces[ROOK] | enemyPieces[QUEEN]) & MagicUtil.getRookMoves(kingIndex, allPieces)
				| (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & MagicUtil.getBishopMoves(kingIndex, allPieces) 
				| enemyPieces[PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex]
			)!= 0;
	}

	public static boolean isInCheckIncludingKing(final int kingIndex, final int colorToMove, final long[] enemyPieces, final long allPieces, final int enemyMajorPieces) {

		//TODO
		if(enemyMajorPieces==0) {
			return (enemyPieces[PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex]
					| enemyPieces[KING] & StaticMoves.KING_MOVES[kingIndex]
				)!= 0;
		}
		
		// put 'super-piece' in kings position
		return (enemyPieces[NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex]
				| (enemyPieces[ROOK] | enemyPieces[QUEEN]) & MagicUtil.getRookMoves(kingIndex, allPieces)
				| (enemyPieces[BISHOP] | enemyPieces[QUEEN]) & MagicUtil.getBishopMoves(kingIndex, allPieces) 
				| enemyPieces[PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex]
				| enemyPieces[KING] & StaticMoves.KING_MOVES[kingIndex]
			)!= 0;
	}
}
