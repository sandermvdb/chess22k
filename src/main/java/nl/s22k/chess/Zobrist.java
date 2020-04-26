package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.WHITE;

import java.security.SecureRandom;

public class Zobrist {

	public static final long sideToMove;
	public static final long[] castling = new long[16];
	public static final long[] epIndex = new long[48];
	public static final long[][][] piece = new long[2][7][64];

	static {
		SecureRandom r = new SecureRandom();
		for (int colorIndex = 0; colorIndex <= BLACK; colorIndex++) {
			for (int pieceIndex = 0; pieceIndex <= KING; pieceIndex++) {
				for (int square = 0; square < 64; square++) {
					piece[colorIndex][pieceIndex][square] = r.nextLong();
				}
			}
		}
		for (int i = 0; i < castling.length; i++) {
			castling[i] = r.nextLong();
		}

		// skip first item: contains only zeros, default value and has no effect when xorring
		for (int i = 1; i < epIndex.length; i++) {
			epIndex[i] = r.nextLong();
		}
		sideToMove = r.nextLong();
	}

	public static void setKey(final ChessBoard cb) {
		cb.zobristKey = 0;

		for (int color = 0; color < 2; color++) {
			for (int pieceType = PAWN; pieceType <= KING; pieceType++) {
				long pieces = cb.pieces[color][pieceType];
				while (pieces != 0) {
					cb.zobristKey ^= piece[color][pieceType][Long.numberOfTrailingZeros(pieces)];
					pieces &= pieces - 1;
				}
			}
		}

		cb.zobristKey ^= castling[cb.castlingRights];
		if (cb.colorToMove == WHITE) {
			cb.zobristKey ^= sideToMove;
		}
		cb.zobristKey ^= epIndex[cb.epIndex];
	}

	public static void setPawnKey(final ChessBoard cb) {
		cb.pawnZobristKey = 0;

		long pieces = cb.pieces[WHITE][PAWN];
		while (pieces != 0) {
			cb.pawnZobristKey ^= piece[WHITE][PAWN][Long.numberOfTrailingZeros(pieces)];
			pieces &= pieces - 1;
		}
		pieces = cb.pieces[BLACK][PAWN];
		while (pieces != 0) {
			cb.pawnZobristKey ^= piece[BLACK][PAWN][Long.numberOfTrailingZeros(pieces)];
			pieces &= pieces - 1;
		}
	}

}
