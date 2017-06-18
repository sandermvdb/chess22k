package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.StaticMoves;

public class SEEUtil {

	private static int getSmallestAttackSeeMove(final ChessBoard cb, final int colorToMove, final int toIndex, final int attackedPieceIndex,
			final long allPieces) {

		// TODO EP?
		// TODO verify SEE-score against q-search?
		// TODO skip pawn and knight if they are already played
		// TODO pinned-pieces could maybe attack the pinner (except for night)

		// put 'super-piece' in see position
		long attackMove;

		// pawn non-promotion attacks
		attackMove = StaticMoves.PAWN_NON_PROMOTION_ATTACKS[1 - colorToMove][toIndex] & cb.pieces[colorToMove][PAWN] & allPieces;
		if (attackMove != 0) {
			return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, PAWN, attackedPieceIndex);
		}

		// knight attacks
		attackMove = cb.pieces[colorToMove][NIGHT] & StaticMoves.KNIGHT_MOVES[toIndex] & allPieces & ~cb.pinnedPieces[colorToMove];
		if (attackMove != 0) {
			return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, NIGHT, attackedPieceIndex);
		}

		// bishop attacks
		attackMove = cb.pieces[colorToMove][BISHOP] & MagicUtil.getBishopMoves(toIndex, allPieces) & allPieces & ~cb.pinnedPieces[colorToMove];
		if (attackMove != 0) {
			return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, BISHOP, attackedPieceIndex);
		}

		// rook attacks
		attackMove = cb.pieces[colorToMove][ROOK] & MagicUtil.getRookMoves(toIndex, allPieces) & allPieces & ~cb.pinnedPieces[colorToMove];
		if (attackMove != 0) {
			return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, ROOK, attackedPieceIndex);
		}

		// pawn promotion attacks
		attackMove = StaticMoves.PAWN_PROMOTION_ATTACKS[1 - colorToMove][toIndex] & cb.pieces[colorToMove][PAWN] & allPieces & ~cb.pinnedPieces[colorToMove];
		if (attackMove != 0) {
			return MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_Q, Long.numberOfTrailingZeros(attackMove), toIndex, attackedPieceIndex);
		}

		// queen attacks
		if (cb.pieces[colorToMove][QUEEN] != 0) {
			attackMove = (cb.pieces[colorToMove][QUEEN] & MagicUtil.getRookMoves(toIndex, allPieces)
					| cb.pieces[colorToMove][QUEEN] & MagicUtil.getBishopMoves(toIndex, allPieces)) & allPieces & ~cb.pinnedPieces[colorToMove];
			if (attackMove != 0) {
				return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, QUEEN, attackedPieceIndex);
			}
		}

		// king attacks
		attackMove = StaticMoves.KING_MOVES[toIndex] & cb.pieces[colorToMove][KING];
		if (attackMove != 0) {
			return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, KING, attackedPieceIndex);
		}

		return 0;
	}

	private static int getSeeScore(final ChessBoard cb, final int colorToMove, final int toIndex, final int attackedPieceIndex, long allPieces) {

		final int move = getSmallestAttackSeeMove(cb, colorToMove, toIndex, attackedPieceIndex, allPieces);

		/* skip if the square isn't attacked anymore by this side */
		if (move == 0) {
			return 0;
		}
		if (MoveUtil.getAttackedPieceIndex(move) == KING) {
			return EvalConstants.MATERIAL_SCORES[KING];
		}

		allPieces ^= Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)];

		// add score when promotion
		if (MoveUtil.getMoveType(move) == MoveUtil.PROMOTION_Q) {
			// TODO stop when bad-capture

			/* Do not consider captures if they lose material, therefore max zero */
			return EvalConstants.QUEEN_PROMOTION_SCORE + Math.max(0,
					EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)] - getSeeScore(cb, 1 - colorToMove, toIndex, QUEEN, allPieces));

		} else {
			// TODO stop when bad-capture

			/* Do not consider captures if they lose material, therefore max zero */
			return Math.max(0, EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]
					- getSeeScore(cb, 1 - colorToMove, toIndex, MoveUtil.getSourcePieceIndex(move), allPieces));
		}

	}

	public static int getSeeCaptureScore(final ChessBoard cb, final int move) {

		if (Statistics.ENABLED) {
			Statistics.seeNodes++;
		}

		final long allPieces = cb.allPieces & ~Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)];

		// promotion non-attack move
		if (MoveUtil.getAttackedPieceIndex(move) == 0) {
			if (EngineConstants.ASSERT) {
				assert MoveUtil.getMoveType(move) != 0 : "Calculating seeScore for quiet move";
			}

			switch (MoveUtil.getMoveType(move)) {
			case MoveUtil.PROMOTION_B:
				return EvalConstants.BISHOP_PROMOTION_SCORE - getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), BISHOP, allPieces);
			case MoveUtil.PROMOTION_N:
				return EvalConstants.KNIGHT_PROMOTION_SCORE - getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), NIGHT, allPieces);
			case MoveUtil.PROMOTION_R:
				return EvalConstants.ROOK_PROMOTION_SCORE - getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), ROOK, allPieces);
			case MoveUtil.PROMOTION_Q:
				return EvalConstants.QUEEN_PROMOTION_SCORE - getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), QUEEN, allPieces);
			}

		}

		// // TODO colorToMove can always stop the capture sequence (does not make any difference...)
		// if (EvalConstants.MATERIAL_SCORES[MoveUtil.getSourcePieceIndex(move)] <=
		// EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]) {
		// return EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)] -
		// EvalConstants.MATERIAL_SCORES[MoveUtil.getSourcePieceIndex(move)];
		// }

		// add score when promotion
		switch (MoveUtil.getMoveType(move)) {
		case MoveUtil.PROMOTION_B:
			return EvalConstants.BISHOP_PROMOTION_SCORE + EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]
					- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), BISHOP, allPieces);
		case MoveUtil.PROMOTION_N:
			return EvalConstants.KNIGHT_PROMOTION_SCORE + EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]
					- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), NIGHT, allPieces);
		case MoveUtil.PROMOTION_R:
			return EvalConstants.ROOK_PROMOTION_SCORE + EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]
					- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), ROOK, allPieces);
		case MoveUtil.PROMOTION_Q:
			return EvalConstants.QUEEN_PROMOTION_SCORE + EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]
					- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), QUEEN, allPieces);
		default:
			return EvalConstants.MATERIAL_SCORES[MoveUtil.getAttackedPieceIndex(move)]
					- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), MoveUtil.getSourcePieceIndex(move), allPieces);
		}
	}
}
