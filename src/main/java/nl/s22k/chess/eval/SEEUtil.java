package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.StaticMoves;

public class SEEUtil {

	private static int getSmallestAttackSeeMove(final ChessBoard cb, final int colorToMove, final int toIndex, final long allPieces, final long movablePieces) {

		// TODO stop when bad-capture

		// put 'super-piece' in see position
		long attackMove;

		// pawn non-promotion attacks
		attackMove = StaticMoves.PAWN_NON_PROMOTION_ATTACKS[1 - colorToMove][toIndex] & cb.pieces[colorToMove][PAWN] & movablePieces;
		if (attackMove != 0) {
			return MoveUtil.createSeeAttackMove(Long.numberOfTrailingZeros(attackMove), PAWN);
		}

		// knight attacks
		attackMove = cb.pieces[colorToMove][NIGHT] & StaticMoves.KNIGHT_MOVES[toIndex] & movablePieces;
		if (attackMove != 0) {
			return MoveUtil.createSeeAttackMove(Long.numberOfTrailingZeros(attackMove), NIGHT);
		}

		// bishop attacks
		if ((cb.pieces[colorToMove][BISHOP] & MagicUtil.bishopMovesEmptyBoard[toIndex]) != 0) {
			attackMove = cb.pieces[colorToMove][BISHOP] & MagicUtil.getBishopMoves(toIndex, allPieces) & movablePieces;
			if (attackMove != 0) {
				return MoveUtil.createSeeAttackMove(Long.numberOfTrailingZeros(attackMove), BISHOP);
			}
		}

		// rook attacks
		if ((cb.pieces[colorToMove][ROOK] & MagicUtil.rookMovesEmptyBoard[toIndex]) != 0) {
			attackMove = cb.pieces[colorToMove][ROOK] & MagicUtil.getRookMoves(toIndex, allPieces) & movablePieces;
			if (attackMove != 0) {
				return MoveUtil.createSeeAttackMove(Long.numberOfTrailingZeros(attackMove), ROOK);
			}
		}

		// pawn promotion attacks
		attackMove = StaticMoves.PAWN_PROMOTION_ATTACKS[1 - colorToMove][toIndex] & cb.pieces[colorToMove][PAWN] & movablePieces;
		if (attackMove != 0) {
			return MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_Q, Long.numberOfTrailingZeros(attackMove), toIndex, 0);
		}

		// queen attacks
		if (cb.pieces[colorToMove][QUEEN] != 0) {
			attackMove = (cb.pieces[colorToMove][QUEEN] & MagicUtil.getRookMoves(toIndex, allPieces)
					| cb.pieces[colorToMove][QUEEN] & MagicUtil.getBishopMoves(toIndex, allPieces)) & movablePieces;
			if (attackMove != 0) {
				return MoveUtil.createSeeAttackMove(Long.numberOfTrailingZeros(attackMove), QUEEN);
			}
		}

		// king attacks
		// split-up because of inlining (max 320 lines)
		return kingAttacks(toIndex, colorToMove, cb);
	}

	private static int kingAttacks(final int toIndex, final int colorToMove, final ChessBoard cb) {
		if ((StaticMoves.KING_MOVES[toIndex] & cb.pieces[colorToMove][KING]) != 0) {
			return MoveUtil.createSeeAttackMove(cb.kingIndex[colorToMove], KING);
		}

		return 0;
	}

	private static int getSeeScore(final ChessBoard cb, final int colorToMove, final int toIndex, final int attackedPieceIndex, long allPieces,
			long pinnedPieces) {

		final int move = getSmallestAttackSeeMove(cb, colorToMove, toIndex, allPieces, allPieces & ~pinnedPieces);

		/* skip if the square isn't attacked anymore by this side */
		if (move == 0) {
			return 0;
		}
		if (attackedPieceIndex == KING) {
			return EvalConstants.MATERIAL_SCORES[KING];
		}

		allPieces ^= Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)];
		pinnedPieces = cb.getPinnedPieces(1 - colorToMove, allPieces ^ Util.POWER_LOOKUP[toIndex]);

		// add score when promotion
		if (MoveUtil.isPromotion(move)) {

			/* Do not consider captures if they lose material, therefore max zero */
			return Math.max(0, EvalConstants.PROMOTION_SCORE[ChessConstants.QUEEN] + EvalConstants.MATERIAL_SCORES[attackedPieceIndex]
					- getSeeScore(cb, 1 - colorToMove, toIndex, QUEEN, allPieces, pinnedPieces));
		} else {

			/* Do not consider captures if they lose material, therefore max zero */
			return Math.max(0, EvalConstants.MATERIAL_SCORES[attackedPieceIndex]
					- getSeeScore(cb, 1 - colorToMove, toIndex, MoveUtil.getSourcePieceIndex(move), allPieces, pinnedPieces));
		}

	}

	public static int getSeeCaptureScore(final ChessBoard cb, final int move) {

		if (Statistics.ENABLED) {
			Statistics.seeNodes++;
		}

		if (EngineConstants.ASSERT) {
			if (MoveUtil.getAttackedPieceIndex(move) == 0) {
				assert MoveUtil.getMoveType(move) != 0 : "Calculating seeScore for quiet move";
			}
		}

		final long allPieces = cb.allPieces & ~Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)];
		final long pinnedPieces = cb.getPinnedPieces(cb.colorToMoveInverse, allPieces);

		// add score when promotion
		if (MoveUtil.isPromotion(move)) {
			return EvalConstants.PROMOTION_SCORE[MoveUtil.getMoveType(move)] + EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]
					- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), MoveUtil.getMoveType(move), allPieces, pinnedPieces);
		} else {
			return EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]
					- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), MoveUtil.getSourcePieceIndex(move), allPieces, pinnedPieces);
		}

	}
}
