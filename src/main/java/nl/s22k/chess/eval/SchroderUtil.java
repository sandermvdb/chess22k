package nl.s22k.chess.eval;

import static nl.s22k.chess.ChessConstants.KING;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.move.MoveUtil;

public class SchroderUtil {

	public static final int MASK_PAWN = 8;
	public static final int MASK_NIGHT_BISHOP = 16;
	public static final int MASK_ROOK = 32;
	public static final int MASK_QUEEN = 64;
	public static final int MASK_KING = 128;

	public static final int PIECE_INDEXES[] = new int[] { ChessConstants.KING, ChessConstants.QUEEN, ChessConstants.ROOK, ChessConstants.BISHOP,
			ChessConstants.PAWN };

	private static int getSmallestAttackMove(final int[] attacks, final int colorToMove, final int attackedPieceIndex) {

		if ((MASK_PAWN & attacks[colorToMove]) != 0) {
			// only remove bit if no pawn attacks are possible anymore
			if (Long.bitCount(attacks[colorToMove] & 0xf8) == (attacks[colorToMove] & 7)) {
				attacks[colorToMove] ^= MASK_PAWN;
			}
			attacks[colorToMove]--;
			return MoveUtil.createAttackMove(0, 0, ChessConstants.PAWN, attackedPieceIndex);
		}

		if ((MASK_NIGHT_BISHOP & attacks[colorToMove]) != 0) {
			// only remove bit if no bishop/knight attacks are possible anymore
			if (Long.bitCount(attacks[colorToMove] & 0xf8) == (attacks[colorToMove] & 7)) {
				attacks[colorToMove] ^= MASK_NIGHT_BISHOP;
			}
			attacks[colorToMove]--;
			return MoveUtil.createAttackMove(0, 0, ChessConstants.BISHOP, attackedPieceIndex);
		}

		if ((MASK_ROOK & attacks[colorToMove]) != 0) {
			// only remove bit if no rook attacks are possible anymore
			if (Long.bitCount(attacks[colorToMove] & 0xf8) == (attacks[colorToMove] & 7)) {
				attacks[colorToMove] ^= MASK_ROOK;
			}
			attacks[colorToMove]--;
			return MoveUtil.createAttackMove(0, 0, ChessConstants.ROOK, attackedPieceIndex);
		}

		if ((MASK_QUEEN & attacks[colorToMove]) != 0) {
			// only remove bit if no pawn attacks are possible anymore
			if (Long.bitCount(attacks[colorToMove] & 0xf8) == (attacks[colorToMove] & 7)) {
				attacks[colorToMove] ^= MASK_QUEEN;
			}
			attacks[colorToMove]--;
			return MoveUtil.createAttackMove(0, 0, ChessConstants.QUEEN, attackedPieceIndex);
		}

		if ((MASK_KING & attacks[colorToMove]) != 0) {
			// only 1 king should be able to hit
			if (attacks[colorToMove] != 0x81) {
				throw new RuntimeException("Incorrect king Shroder-byte: " + attacks[colorToMove]);
			}
			attacks[colorToMove] = 0;
			return MoveUtil.createAttackMove(0, 0, ChessConstants.KING, attackedPieceIndex);
		}

		return 0;
	}

	public static int getAttackScore(final int[] attacks, final int colorToMove, final int attackedPieceIndex) {

		final int move = getSmallestAttackMove(attacks, colorToMove, attackedPieceIndex);

		/* skip if the square isn't attacked anymore by this side */
		if (move == 0) {
			return 0;
		}
		if (MoveUtil.getZKAttackedPieceIndex(move) == KING) {
			return EvalConstants.MATERIAL_SCORES[KING];
		}

		// TODO stop when bad-capture

		/* Do not consider captures if they lose material, therefore max zero */
		return Math.max(0, EvalConstants.MATERIAL_SCORES[MoveUtil.getZKAttackedPieceIndex(move)]
				- getAttackScore(attacks, colorToMove * -1 + 1, MoveUtil.getZKSourcePieceIndex(move)));

	}

}
