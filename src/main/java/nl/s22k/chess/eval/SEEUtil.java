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
		// TODO skip 'semi'-pinned-pieces? (unless it is attacking the checking-piece...)

		// put 'super-piece' in see position. we ignore checks

		// pawn non-promotion attacks
		long attackMove = StaticMoves.PAWN_NON_PROMOTION_ATTACKS[colorToMove * -1 + 1][toIndex] & cb.pieces[colorToMove][PAWN] & allPieces;
		if (attackMove != 0) {
			return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, PAWN, attackedPieceIndex);
		}

		// knight attacks
		attackMove = StaticMoves.KNIGHT_MOVES[toIndex] & cb.pieces[colorToMove][NIGHT] & allPieces;
		if (attackMove != 0) {
			return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, NIGHT, attackedPieceIndex);
		}

		// bishop attacks
		// check if this colorToMove bishops attack this square at all
		if ((cb.bishopRayAttacks[colorToMove] & Util.POWER_LOOKUP[toIndex]) != 0) {
			attackMove = MagicUtil.getBishopMoves(toIndex, allPieces) & cb.pieces[colorToMove][BISHOP] & allPieces;
			if (attackMove != 0) {
				return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, BISHOP, attackedPieceIndex);
			}
		}

		// rook attacks
		// check if this colorToMove rooks attack this square at all
		if ((cb.rookRayAttacks[colorToMove] & Util.POWER_LOOKUP[toIndex]) != 0) {
			attackMove = MagicUtil.getRookMoves(toIndex, allPieces) & cb.pieces[colorToMove][ROOK] & allPieces;
			if (attackMove != 0) {
				return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, ROOK, attackedPieceIndex);
			}
		}

		// pawn promotion attacks
		attackMove = StaticMoves.PAWN_PROMOTION_ATTACKS[colorToMove * -1 + 1][toIndex] & cb.pieces[colorToMove][PAWN] & allPieces;
		if (attackMove != 0) {
			return MoveUtil.createPromotionAttack(Long.numberOfTrailingZeros(attackMove), toIndex, attackedPieceIndex);
		}

		// queen attacks
		// check if this colorToMove queens attack this square at all
		if ((cb.queenRayAttacks[colorToMove] & Util.POWER_LOOKUP[toIndex]) != 0) {
			attackMove = (MagicUtil.getRookMoves(toIndex, allPieces) & cb.pieces[colorToMove][QUEEN]
					| MagicUtil.getBishopMoves(toIndex, allPieces) & cb.pieces[colorToMove][QUEEN]) & allPieces;
			if (attackMove != 0) {
				return MoveUtil.createAttackMove(Long.numberOfTrailingZeros(attackMove), toIndex, QUEEN, attackedPieceIndex);
			}
		}

		// king attacks
		attackMove = StaticMoves.KING_MOVES[toIndex] & cb.pieces[colorToMove][KING] & allPieces;
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
		if (MoveUtil.getZKAttackedPieceIndex(move) == KING) {
			return EvalConstants.MATERIAL_SCORES[KING];
		}

		allPieces ^= Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)];

		// add score when promotion
		if (MoveUtil.isPromotion(move)) {
			// TODO stop when bad-capture

			/* Do not consider captures if they lose material, therefore max zero */
			return EvalConstants.QUEEN_PROMOTION_SCORE + Math.max(0,
					EvalConstants.MATERIAL_SCORES[MoveUtil.getZKAttackedPieceIndex(move)] - getSeeScore(cb, colorToMove * -1 + 1, toIndex, QUEEN, allPieces));

		} else {
			// TODO stop when bad-capture

			/* Do not consider captures if they lose material, therefore max zero */
			return Math.max(0, EvalConstants.MATERIAL_SCORES[MoveUtil.getZKAttackedPieceIndex(move)]
					- getSeeScore(cb, colorToMove * -1 + 1, toIndex, MoveUtil.getZKSourcePieceIndex(move), allPieces));
		}

	}

	public static int getSeeCaptureScore(final ChessBoard cb, final int move) {

		if (Statistics.ENABLED) {
			Statistics.seeNodes++;
		}

		// promotion non-attack move
		// TODO could be a knight-promotion
		if (MoveUtil.getZKAttackedPieceIndex(move) == 0) {
			if (EngineConstants.TEST_VALUES) {
				if (!MoveUtil.isPromotion(move)) {
					System.out.println("Calculating seeScore for quiet move");
				}
			}
			if (MoveUtil.isNightPromotion(move)) {
				return EvalConstants.KNIGHT_PROMOTION_SCORE;
			} else {
				return EvalConstants.QUEEN_PROMOTION_SCORE;
			}
		}

		// TODO colorToMove can always stop the capture sequence (does not make any difference...)
		// if (EvalConstants.MATERIAL_SCORES[MoveUtil.getZKSourcePieceIndex(move)] <=
		// EvalConstants.MATERIAL_SCORES[MoveUtil.getZKAttackedPieceIndex(move)]) {
		// return EvalConstants.MATERIAL_SCORES[MoveUtil.getZKAttackedPieceIndex(move)] -
		// EvalConstants.MATERIAL_SCORES[MoveUtil.getZKSourcePieceIndex(move)];
		// }

		long allPieces = cb.allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)];

		// add score when promotion
		// TODO could be a knight-promotion
		if (MoveUtil.isPromotion(move)) {
			if (MoveUtil.isNightPromotion(move)) {
				return EvalConstants.KNIGHT_PROMOTION_SCORE + EvalConstants.MATERIAL_SCORES[MoveUtil.getZKAttackedPieceIndex(move)]
						- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), NIGHT, allPieces);
			} else {
				return EvalConstants.QUEEN_PROMOTION_SCORE + EvalConstants.MATERIAL_SCORES[MoveUtil.getZKAttackedPieceIndex(move)]
						- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), QUEEN, allPieces);
			}

		} else {
			return EvalConstants.MATERIAL_SCORES[MoveUtil.getZKAttackedPieceIndex(move)]
					- getSeeScore(cb, cb.colorToMoveInverse, MoveUtil.getToIndex(move), MoveUtil.getZKSourcePieceIndex(move), allPieces);
		}

	}

}
