package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import java.util.Arrays;
import java.util.BitSet;

import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.search.HeuristicUtil;
import nl.s22k.chess.search.RepetitionTable;

public class ChessBoardUtil {

	public static ChessBoard getNewCB() {
		return getNewCB(ChessConstants.FEN_START);
	}

	public static ChessBoard getNewCB(String fen) {
		RepetitionTable.clearValues();
		HeuristicUtil.clearTables();

		ChessBoard cb = ChessBoard.getInstance();
		clearValues(cb);

		cb.moveCounter = 0;

		String[] fenArray = fen.split(" ");

		// 1: pieces: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
		setPieces(cb, fenArray[0].split("/"));

		// 2: active-color: w
		cb.colorToMove = fenArray[1].equals("w") ? WHITE : BLACK;

		// 3: castling: KQkq
		cb.castlingRights = 15;
		if (!fenArray[2].contains("K")) {
			cb.castlingRights &= 7;
		}
		if (!fenArray[2].contains("Q")) {
			cb.castlingRights &= 11;
		}
		if (!fenArray[2].contains("k")) {
			cb.castlingRights &= 13;
		}
		if (!fenArray[2].contains("q")) {
			cb.castlingRights &= 14;
		}

		// 4: en-passant: -
		if (fenArray[3].equals("-")) {
			cb.pawn2MoveFromColumn = ChessConstants.PAWN_2_MOVE_DEFAULT_COLUMN;
		} else {
			cb.pawn2MoveFromColumn = 104 - fenArray[3].charAt(0);
		}

		if (fenArray.length > 4) {
			// TODO
			// 5: half-counter since last capture or pawn advance: 1

			// 6: counter: 1
			cb.moveCounter = Integer.parseInt(fenArray[4]) * 2;
			if (cb.colorToMove == BLACK) {
				cb.moveCounter++;
			}
		} else {
			// if counter is not set, try to guess
			// assume in the beginning every 2 moves, a pawn is moved
			int pawnsNotAtStartingPosition = 16 - Long.bitCount(cb.pieces[WHITE][PAWN] & 0xff00) - Long.bitCount(cb.pieces[BLACK][PAWN] & 0xff000000000000L);
			cb.moveCounter = pawnsNotAtStartingPosition * 2;
		}

		init(cb);
		return cb;
	}

	private static void clearValues(ChessBoard cb) {
		// history
		Arrays.fill(cb.psqtScoreHistory, 0);
		Arrays.fill(cb.castlingHistory, 0);
		Arrays.fill(cb.pawn2MoveHistory, 0);
		Arrays.fill(cb.zobristKeyHistory, 0);
		Arrays.fill(cb.pawnZobristKeyHistory, 0);
		Arrays.fill(cb.checkingPiecesHistory, 0);
		Arrays.fill(cb.pinnedPiecesHistory[WHITE], 0);
		Arrays.fill(cb.pinnedPiecesHistory[BLACK], 0);

		// pieces
		for (int color = 0; color < 2; color++) {
			for (int pieceIndex = 1; pieceIndex <= KING; pieceIndex++) {
				cb.pieces[color][pieceIndex] = 0;
			}
		}
	}

	public static void calculateZobristKeys(ChessBoard cb) {
		cb.zobristKey = 0;
		cb.pawnZobristKey = 0;

		// white pieces
		calculatePiecesKey(cb, cb.pieces[WHITE][BISHOP], WHITE, BISHOP);
		calculatePiecesKey(cb, cb.pieces[WHITE][KING], WHITE, KING);
		calculatePiecesKey(cb, cb.pieces[WHITE][NIGHT], WHITE, NIGHT);
		calculatePiecesKey(cb, cb.pieces[WHITE][PAWN], WHITE, PAWN);
		calculatePiecesKey(cb, cb.pieces[WHITE][QUEEN], WHITE, QUEEN);
		calculatePiecesKey(cb, cb.pieces[WHITE][ROOK], WHITE, ROOK);

		// black pieces
		calculatePiecesKey(cb, cb.pieces[BLACK][BISHOP], BLACK, BISHOP);
		calculatePiecesKey(cb, cb.pieces[BLACK][KING], BLACK, KING);
		calculatePiecesKey(cb, cb.pieces[BLACK][NIGHT], BLACK, NIGHT);
		calculatePiecesKey(cb, cb.pieces[BLACK][PAWN], BLACK, PAWN);
		calculatePiecesKey(cb, cb.pieces[BLACK][QUEEN], BLACK, QUEEN);
		calculatePiecesKey(cb, cb.pieces[BLACK][ROOK], BLACK, ROOK);

		cb.zobristKey ^= cb.zkCastling[cb.castlingRights];
		if (cb.colorToMove == WHITE) {
			cb.zobristKey ^= cb.zkWhiteToMove;
		}
		cb.zobristKey ^= cb.zk2Move[cb.pawn2MoveFromColumn];

		// pawn zobrist key
		long pieces = cb.pieces[WHITE][PAWN];
		while (pieces != 0) {
			cb.pawnZobristKey ^= cb.zkPieceValues[Long.numberOfTrailingZeros(pieces)][WHITE][PAWN];
			pieces &= pieces - 1;
		}
		pieces = cb.pieces[BLACK][PAWN];
		while (pieces != 0) {
			cb.pawnZobristKey ^= cb.zkPieceValues[Long.numberOfTrailingZeros(pieces)][BLACK][PAWN];
			pieces &= pieces - 1;
		}
		cb.pawnZobristKey ^= cb.zkKingPosition[WHITE][EvalConstants.getKingPositionIndex(WHITE, cb.kingIndex[WHITE])]
				^ cb.zkKingPosition[BLACK][EvalConstants.getKingPositionIndex(BLACK, cb.kingIndex[BLACK])];
	}

	// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
	private static void setPieces(ChessBoard cb, String[] fenPieceRows) {
		int positionCount = 0;
		for (String fenPieceRow : fenPieceRows) {
			for (int i = 0; i < fenPieceRow.length(); i++) {
				char fenPieceChar = fenPieceRow.charAt(i);
				if (Character.isDigit(fenPieceChar)) {
					positionCount += Character.digit(fenPieceChar, 10);
				}

				// black pieces
				else if (ChessConstants.FEN_BLACK_PIECES[BISHOP].equals(fenPieceChar + "")) {
					cb.pieces[BLACK][BISHOP] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_BLACK_PIECES[ROOK].equals(fenPieceChar + "")) {
					cb.pieces[BLACK][ROOK] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_BLACK_PIECES[PAWN].equals(fenPieceChar + "")) {
					cb.pieces[BLACK][PAWN] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_BLACK_PIECES[QUEEN].equals(fenPieceChar + "")) {
					cb.pieces[BLACK][QUEEN] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_BLACK_PIECES[KING].equals(fenPieceChar + "")) {
					cb.pieces[BLACK][KING] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_BLACK_PIECES[NIGHT].equals(fenPieceChar + "")) {
					cb.pieces[BLACK][NIGHT] |= Util.POWER_LOOKUP[63 - positionCount++];
				}

				// white pieces
				else if (ChessConstants.FEN_WHITE_PIECES[BISHOP].equals(fenPieceChar + "")) {
					cb.pieces[WHITE][BISHOP] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_WHITE_PIECES[ROOK].equals(fenPieceChar + "")) {
					cb.pieces[WHITE][ROOK] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_WHITE_PIECES[PAWN].equals(fenPieceChar + "")) {
					cb.pieces[WHITE][PAWN] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_WHITE_PIECES[KING].equals(fenPieceChar + "")) {
					cb.pieces[WHITE][KING] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_WHITE_PIECES[QUEEN].equals(fenPieceChar + "")) {
					cb.pieces[WHITE][QUEEN] |= Util.POWER_LOOKUP[63 - positionCount++];
				} else if (ChessConstants.FEN_WHITE_PIECES[NIGHT].equals(fenPieceChar + "")) {
					cb.pieces[WHITE][NIGHT] |= Util.POWER_LOOKUP[63 - positionCount++];
				}
			}
		}
	}

	private static void calculatePiecesKey(ChessBoard cb, long pieces, int colorIndex, int pieceIndex) {
		while (pieces != 0) {
			cb.zobristKey ^= cb.zkPieceValues[Long.numberOfTrailingZeros(pieces)][colorIndex][pieceIndex];
			pieces &= pieces - 1;
		}
	}

	public static void init(ChessBoard cb) {
		cb.kingIndex[WHITE] = Long.numberOfTrailingZeros(cb.pieces[WHITE][KING]);
		cb.kingIndex[BLACK] = Long.numberOfTrailingZeros(cb.pieces[BLACK][KING]);
		cb.colorFactor = ChessConstants.COLOR_FACTOR[cb.colorToMove];
		cb.colorToMoveInverse = cb.colorToMove * -1 + 1;
		calculateZobristKeys(cb);
		cb.friendlyPieces[WHITE] = cb.pieces[WHITE][PAWN] | cb.pieces[WHITE][BISHOP] | cb.pieces[WHITE][NIGHT] | cb.pieces[WHITE][KING] | cb.pieces[WHITE][ROOK]
				| cb.pieces[WHITE][QUEEN];
		cb.friendlyPieces[BLACK] = cb.pieces[BLACK][PAWN] | cb.pieces[BLACK][BISHOP] | cb.pieces[BLACK][NIGHT] | cb.pieces[BLACK][KING] | cb.pieces[BLACK][ROOK]
				| cb.pieces[BLACK][QUEEN];
		cb.allPieces = cb.friendlyPieces[WHITE] | cb.friendlyPieces[BLACK];
		cb.emptySpaces = ~cb.allPieces;

		for (int color = 0; color < cb.pieces.length; color++) {
			for (int pieceIndex = 1; pieceIndex < cb.pieces[0].length; pieceIndex++) {
				long piece = cb.pieces[color][pieceIndex];
				while (piece != 0) {
					cb.pieceIndexes[Long.numberOfTrailingZeros(piece)] = pieceIndex;
					piece &= piece - 1;
				}
			}
		}

		cb.checkingPieces = CheckUtil.getCheckingPieces(cb);

		cb.updateBishopRayAttacks(WHITE);
		cb.updateRookRayAttacks(WHITE);
		cb.updateQueenRayAttacks(WHITE);
		cb.updateBishopRayAttacks(BLACK);
		cb.updateRookRayAttacks(BLACK);
		cb.updateQueenRayAttacks(BLACK);

		cb.updatePinnedPieces(WHITE);
		cb.updatePinnedPieces(BLACK);

		cb.isEndGame[WHITE] = cb.isEndGame(WHITE);
		cb.isEndGame[BLACK] = cb.isEndGame(BLACK);

		if (cb.isEndGame[WHITE]) {
			cb.pawnZobristKey ^= cb.zkEndGame[WHITE];
		}
		if (cb.isEndGame[BLACK]) {
			cb.pawnZobristKey ^= cb.zkEndGame[BLACK];
		}

		cb.psqtScore = EvalUtil.calculatePositionScores(cb);
	}

	public static String toString(ChessBoard cb) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 64; i++) {
			sb.append(".");
		}

		for (int i = 0; i < 64; i++) {
			// white pieces
			if (BitSet.valueOf(new long[] { cb.pieces[WHITE][KING] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_WHITE_PIECES[KING]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[WHITE][QUEEN] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_WHITE_PIECES[QUEEN]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[WHITE][PAWN] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_WHITE_PIECES[PAWN]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[WHITE][ROOK] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_WHITE_PIECES[ROOK]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[WHITE][BISHOP] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_WHITE_PIECES[BISHOP]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[WHITE][NIGHT] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_WHITE_PIECES[NIGHT]);
			}

			// black pieces
			else if (BitSet.valueOf(new long[] { cb.pieces[BLACK][KING] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_BLACK_PIECES[KING]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[BLACK][QUEEN] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_BLACK_PIECES[QUEEN]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[BLACK][PAWN] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_BLACK_PIECES[PAWN]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[BLACK][ROOK] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_BLACK_PIECES[ROOK]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[BLACK][BISHOP] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_BLACK_PIECES[BISHOP]);
			} else if (BitSet.valueOf(new long[] { cb.pieces[BLACK][NIGHT] }).get(i)) {
				sb.replace(63 - i, 63 - i + 1, ChessConstants.FEN_BLACK_PIECES[NIGHT]);
			}
		}

		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			sb2.append(8 - i + sb.substring(i * 8, i * 8 + 8) + "\r\n");
		}

		sb2.append(" abcdefgh");
		return sb2.toString();
	}

}
