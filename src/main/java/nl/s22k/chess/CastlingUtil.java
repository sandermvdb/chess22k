package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.ALL;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.EMPTY;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.eval.EvalConstants;

public final class CastlingUtil {

	// 4 bits: white-king,white-queen,black-king,black-queen
	public static long getCastlingIndexes(final ChessBoard cb) {
		if (cb.castlingRights == 0) {
			return 0;
		}
		if (cb.colorToMove == WHITE) {
			switch (cb.castlingRights) {
			case 0:
			case 1:
			case 2:
			case 3:
				return 0;
			case 4:
			case 5:
			case 6:
			case 7:
				return Bitboard.C1;
			case 8:
			case 9:
			case 10:
			case 11:
				return Bitboard.G1;
			case 12:
			case 13:
			case 14:
			case 15:
				return Bitboard.C1_G1;
			}
		} else {
			switch (cb.castlingRights) {
			case 0:
			case 4:
			case 8:
			case 12:
				return 0;
			case 1:
			case 5:
			case 9:
			case 13:
				return Bitboard.C8;
			case 2:
			case 6:
			case 10:
			case 14:
				return Bitboard.G8;
			case 3:
			case 7:
			case 11:
			case 15:
				return Bitboard.C8_G8;
			}
		}
		throw new RuntimeException("Unknown castling-right: " + cb.castlingRights);
	}

	public static int getRookMovedOrAttackedCastlingRights(final int castlingRights, final int rookIndex) {
		switch (rookIndex) {
		case 0:
			return castlingRights & 7; // 0111
		case 7:
			return castlingRights & 11; // 1011
		case 56:
			return castlingRights & 13; // 1101
		case 63:
			return castlingRights & 14; // 1110
		}
		return castlingRights;
	}

	public static int getKingMovedCastlingRights(final int castlingRights, final int kingIndex) {
		switch (kingIndex) {
		case 3:
			return castlingRights & 3; // 0011
		case 59:
			return castlingRights & 12; // 1100
		}
		return castlingRights;
	}

	public static long getRookInBetweenIndex(final int castlingIndex) {
		switch (castlingIndex) {
		case 1:
			return Bitboard.F1_G1;
		case 5:
			return Bitboard.B1C1D1;
		case 57:
			return Bitboard.F8_G8;
		case 61:
			return Bitboard.B8C8D8;
		}
		throw new RuntimeException("Incorrect castling-index: " + castlingIndex);
	}

	public static void uncastleRookUpdatePsqt(final ChessBoard cb, final int kingToIndex) {
		switch (kingToIndex) {
		case 1:
			// white rook from 2 to 0
			castleRookUpdatePsqt(cb, 2, 0, WHITE);
			return;
		case 57:
			// black rook from 58 to 56
			castleRookUpdatePsqt(cb, 58, 56, BLACK);
			return;
		case 5:
			// white rook from 4 to 7
			castleRookUpdatePsqt(cb, 4, 7, WHITE);
			return;
		case 61:
			// black rook from 60 to 63
			castleRookUpdatePsqt(cb, 60, 63, BLACK);
			return;
		}
		throw new RuntimeException("Incorrect king castling to-index: " + kingToIndex);
	}

	public static void castleRookUpdateKeyAndPsqt(final ChessBoard cb, final int kingToIndex) {
		switch (kingToIndex) {
		case 1:
			// white rook from 0 to 2
			castleRookUpdatePsqt(cb, 0, 2, WHITE);
			cb.zobristKey ^= Zobrist.piece[WHITE][ROOK][0] ^ Zobrist.piece[WHITE][ROOK][2];
			return;
		case 57:
			// black rook from 56 to 58
			castleRookUpdatePsqt(cb, 56, 58, BLACK);
			cb.zobristKey ^= Zobrist.piece[BLACK][ROOK][56] ^ Zobrist.piece[BLACK][ROOK][58];
			return;
		case 5:
			// white rook from 7 to 4
			castleRookUpdatePsqt(cb, 7, 4, WHITE);
			cb.zobristKey ^= Zobrist.piece[WHITE][ROOK][7] ^ Zobrist.piece[WHITE][ROOK][4];
			return;
		case 61:
			// black rook from 63 to 60
			castleRookUpdatePsqt(cb, 63, 60, BLACK);
			cb.zobristKey ^= Zobrist.piece[BLACK][ROOK][63] ^ Zobrist.piece[BLACK][ROOK][60];
			return;
		}
		throw new RuntimeException("Incorrect king castling to-index: " + kingToIndex);
	}

	private static void castleRookUpdatePsqt(final ChessBoard cb, final int fromIndex, final int toIndex, final int color) {
		cb.pieces[color][ALL] ^= Util.POWER_LOOKUP[fromIndex] | Util.POWER_LOOKUP[toIndex];
		cb.pieces[color][ROOK] ^= Util.POWER_LOOKUP[fromIndex] | Util.POWER_LOOKUP[toIndex];
		cb.pieceIndexes[fromIndex] = EMPTY;
		cb.pieceIndexes[toIndex] = ROOK;
		cb.psqtScore += EvalConstants.PSQT[ROOK][color][toIndex] - EvalConstants.PSQT[ROOK][color][fromIndex];
	}

	public static boolean isValidCastlingMove(final ChessBoard cb, final int fromIndex, final int toIndex) {
		if (cb.checkingPieces != 0) {
			return false;
		}
		if ((cb.allPieces & getRookInBetweenIndex(toIndex)) != 0) {
			return false;
		}

		long kingIndexes = ChessConstants.IN_BETWEEN[fromIndex][toIndex] | Util.POWER_LOOKUP[toIndex];
		while (kingIndexes != 0) {
			// king does not move through a checked position?
			if (CheckUtil.isInCheckIncludingKing(Long.numberOfTrailingZeros(kingIndexes), cb.colorToMove, cb.pieces[cb.colorToMoveInverse], cb.allPieces)) {
				return false;
			}
			kingIndexes &= kingIndexes - 1;
		}

		return true;
	}

}
