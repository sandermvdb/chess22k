package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.EMPTY;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.eval.EvalConstants;;

public final class CastlingUtil {

	// 4 bits: white-king,white-queen,black-king,black-queen
	public static long getCastlingIndexes(final ChessBoard cb) {
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
				return 0x20L;// 5
			case 8:
			case 9:
			case 10:
			case 11:
				return 0x2L; // 1
			case 12:
			case 13:
			case 14:
			case 15:
				return 0x22; // 1|5
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
				return 0x2000000000000000L; // 61
			case 2:
			case 6:
			case 10:
			case 14:
				return 0x200000000000000L; // 57
			case 3:
			case 7:
			case 11:
			case 15:
				return 0x2200000000000000L; // 57|61
			}
		}
		throw new RuntimeException("Unknown castling-right: " + cb.castlingRights);
	}

	public static void setRookMovedOrAttackedCastlingRights(final ChessBoard cb, final int rookIndex) {
		switch (rookIndex) {
		case 0:
			cb.castlingRights &= 7; // 0111
			break;
		case 7:
			cb.castlingRights &= 11; // 1011
			break;
		case 56:
			cb.castlingRights &= 13; // 1101
			break;
		case 63:
			cb.castlingRights &= 14; // 1110
		}
	}

	public static void setKingMovedCastlingRights(final ChessBoard cb, final int kingIndex) {
		switch (kingIndex) {
		case 3:
			cb.castlingRights &= 3; // 0011
			break;
		case 59:
			cb.castlingRights &= 12; // 1100
		}
	}

	public static long getRookInBetweenIndex(final int castlingIndex) {
		switch (castlingIndex) {
		case 1:
			return 0x6L; // 1|2
		case 5:
			return 0x70L; // 4|5|6
		case 57:
			return 0x600000000000000L; // 57|58
		case 61:
			return 0x7000000000000000L; // 60|61|62
		}
		throw new RuntimeException("Incorrect castling-index: " + castlingIndex);
	}

	public static long getKingInBetweenIndex(final int castlingIndex) {
		switch (castlingIndex) {
		case 1:
			return 0x6L; // 1|2
		case 5:
			return 0x30L; // 4|5
		case 57:
			return 0x600000000000000L; // 57|58
		case 61:
			return 0x3000000000000000L; // 60|61
		}
		throw new RuntimeException("Incorrect castling-index: " + castlingIndex);
	}

	public static void uncastleRook(final ChessBoard cb, final int kingToIndex) {
		switch (kingToIndex) {
		case 1:
			// white rook from 2 to 0
			cb.pieces[cb.colorToMoveInverse][ROOK] ^= 5;
			cb.friendlyPieces[cb.colorToMoveInverse] ^= 5;
			cb.pieceIndexes[2] = EMPTY;
			cb.pieceIndexes[0] = ROOK;
			return;
		case 57:
			// black rook from 58 to 56
			cb.pieces[cb.colorToMoveInverse][ROOK] ^= 0x500000000000000L;
			cb.friendlyPieces[cb.colorToMoveInverse] ^= 0x500000000000000L;
			cb.pieceIndexes[58] = EMPTY;
			cb.pieceIndexes[56] = ROOK;
			return;
		case 5:
			// white rook from 4 to 7
			cb.pieces[cb.colorToMoveInverse][ROOK] ^= 0x90;
			cb.friendlyPieces[cb.colorToMoveInverse] ^= 0x90;
			cb.pieceIndexes[4] = EMPTY;
			cb.pieceIndexes[7] = ROOK;
			return;
		case 61:
			// black rook from 60 to 63
			cb.pieces[cb.colorToMoveInverse][ROOK] ^= 0x9000000000000000L;
			cb.friendlyPieces[cb.colorToMoveInverse] ^= 0x9000000000000000L;
			cb.pieceIndexes[60] = EMPTY;
			cb.pieceIndexes[63] = ROOK;
			return;
		}
		throw new RuntimeException("Incorrect king castling to-index: " + kingToIndex);
	}

	public static void castleRookUpdateKeyAndPsqt(final ChessBoard cb, final int kingToIndex) {
		switch (kingToIndex) {
		case 1:
			// white rook from 0 to 2
			cb.pieces[cb.colorToMove][ROOK] ^= 5;
			cb.friendlyPieces[cb.colorToMove] ^= 5;
			cb.pieceIndexes[0] = EMPTY;
			cb.pieceIndexes[2] = ROOK;
			cb.zobristKey ^= ChessBoard.zkPieceValues[0][WHITE][ROOK] ^ ChessBoard.zkPieceValues[2][WHITE][ROOK];
			cb.psqtScore += EvalConstants.ROOK_POSITION_SCORES[cb.colorToMove][2] - EvalConstants.ROOK_POSITION_SCORES[cb.colorToMove][0];
			return;
		case 57:
			// black rook from 56 to 58
			cb.pieces[cb.colorToMove][ROOK] ^= 0x500000000000000L;
			cb.friendlyPieces[cb.colorToMove] ^= 0x500000000000000L;
			cb.pieceIndexes[56] = EMPTY;
			cb.pieceIndexes[58] = ROOK;
			cb.zobristKey ^= ChessBoard.zkPieceValues[56][BLACK][ROOK] ^ ChessBoard.zkPieceValues[58][BLACK][ROOK];
			cb.psqtScore -= EvalConstants.ROOK_POSITION_SCORES[cb.colorToMove][58] - EvalConstants.ROOK_POSITION_SCORES[cb.colorToMove][56];
			return;
		case 5:
			// white rook from 7 to 4
			cb.pieces[cb.colorToMove][ROOK] ^= 0x90;
			cb.friendlyPieces[cb.colorToMove] ^= 0x90;
			cb.pieceIndexes[7] = EMPTY;
			cb.pieceIndexes[4] = ROOK;
			cb.zobristKey ^= ChessBoard.zkPieceValues[7][WHITE][ROOK] ^ ChessBoard.zkPieceValues[4][WHITE][ROOK];
			cb.psqtScore += EvalConstants.ROOK_POSITION_SCORES[cb.colorToMove][4] - EvalConstants.ROOK_POSITION_SCORES[cb.colorToMove][7];
			return;
		case 61:
			// black rook from 63 to 60
			cb.pieces[cb.colorToMove][ROOK] ^= 0x9000000000000000L;
			cb.friendlyPieces[cb.colorToMove] ^= 0x9000000000000000L;
			cb.pieceIndexes[63] = EMPTY;
			cb.pieceIndexes[60] = ROOK;
			cb.zobristKey ^= ChessBoard.zkPieceValues[63][BLACK][ROOK] ^ ChessBoard.zkPieceValues[60][BLACK][ROOK];
			cb.psqtScore -= EvalConstants.ROOK_POSITION_SCORES[cb.colorToMove][60] - EvalConstants.ROOK_POSITION_SCORES[cb.colorToMove][63];
			return;
		}
		throw new RuntimeException("Incorrect king castling to-index: " + kingToIndex);

	}

	public static boolean isValidCastlingMove(final ChessBoard cb, final int fromIndex, final int toIndex) {
		if (cb.checkingPieces != 0) {
			return false;
		}
		if ((cb.allPieces & getRookInBetweenIndex(toIndex)) != 0) {
			return false;
		}

		long kingInBetweenIndexes = getKingInBetweenIndex(toIndex);
		while (kingInBetweenIndexes != 0) {
			// king does not move through a checked position?
			if (CheckUtil.isInCheckIncludingKing(Long.numberOfTrailingZeros(kingInBetweenIndexes), cb.colorToMove, cb.friendlyPieces[cb.colorToMove],
					cb.pieces[cb.colorToMoveInverse], cb.allPieces)) {
				return false;
			}
			kingInBetweenIndexes &= kingInBetweenIndexes - 1;
		}

		return true;
	}

}
