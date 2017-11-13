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

import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.search.HeuristicUtil;
import nl.s22k.chess.search.RepetitionTable;

public class ChessBoardUtil {

	public static ChessBoard getNewCB() {
		return getNewCB(ChessConstants.FEN_START);
	}

	public static ChessBoard getNewCB(String fen) {
		ChessBoard cb = ChessBoard.getInstance();

		if (!EngineConstants.isTuningSession) {
			RepetitionTable.clearValues();
			HeuristicUtil.clearTables();
			clearHistoryValues(cb);
		}

		setFenValues(fen, cb);
		init(cb);
		return cb;
	}

	public static void setFenValues(String fen, ChessBoard cb) {
		cb.moveCounter = 0;

		String[] fenArray = fen.split(" ");

		// 1: pieces: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
		setPieces(cb, fenArray[0]);

		// 2: active-color: w
		cb.colorToMove = fenArray[1].equals("w") ? WHITE : BLACK;

		// 3: castling: KQkq
		cb.castlingRights = 15;
		if (fenArray.length > 2) {
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
		} else {
			// try to guess the castling rights
			if (cb.kingIndex[WHITE] != 3) {
				cb.castlingRights &= 3; // 0011
			}
			if (cb.kingIndex[BLACK] != 59) {
				cb.castlingRights &= 12; // 1100
			}
		}

		if (fenArray.length > 3) {
			// 4: en-passant: -
			if (fenArray[3].equals("-") || fenArray[3].equals("–")) {
				cb.epIndex = 0;
			} else {
				cb.epIndex = 104 - fenArray[3].charAt(0) + 8 * (Integer.parseInt(fenArray[3].substring(1)) - 1);
			}
		}

		if (fenArray.length > 4) {
			// TODO
			// 5: half-counter since last capture or pawn advance: 1
			// fenArray[4]

			// 6: counter: 1
			cb.moveCounter = Integer.parseInt(fenArray[5]) * 2;
			if (cb.colorToMove == BLACK) {
				cb.moveCounter++;
			}
		} else {
			// if counter is not set, try to guess
			// assume in the beginning every 2 moves, a pawn is moved
			int pawnsNotAtStartingPosition = 16 - Long.bitCount(cb.pieces[WHITE][PAWN] & Bitboard.RANK_2)
					- Long.bitCount(cb.pieces[BLACK][PAWN] & Bitboard.RANK_7);
			cb.moveCounter = pawnsNotAtStartingPosition * 2;
		}
	}

	public static void clearHistoryValues(ChessBoard cb) {
		// history
		Arrays.fill(cb.psqtScoreHistory, 0);
		Arrays.fill(cb.castlingHistory, 0);
		Arrays.fill(cb.epIndexHistory, 0);
		Arrays.fill(cb.zobristKeyHistory, 0);
		Arrays.fill(cb.pawnZobristKeyHistory, 0);
		Arrays.fill(cb.checkingPiecesHistory, 0);
		Arrays.fill(cb.pinnedPiecesHistory, 0);
	}

	public static void calculateZobristKeys(ChessBoard cb) {
		cb.zobristKey = 0;

		for (int color = 0; color < 2; color++) {
			for (int piece = PAWN; piece <= KING; piece++) {
				long pieces = cb.pieces[color][piece];
				while (pieces != 0) {
					cb.zobristKey ^= ChessBoard.zkPieceValues[Long.numberOfTrailingZeros(pieces)][color][piece];
					pieces &= pieces - 1;
				}
			}
		}

		cb.zobristKey ^= ChessBoard.zkCastling[cb.castlingRights];
		if (cb.colorToMove == WHITE) {
			cb.zobristKey ^= ChessBoard.zkWhiteToMove;
		}
		cb.zobristKey ^= ChessBoard.zkEPIndex[cb.epIndex];
	}

	public static void calculatePawnZobristKeys(ChessBoard cb) {
		cb.pawnZobristKey = 0;

		long pieces = cb.pieces[WHITE][PAWN];
		while (pieces != 0) {
			cb.pawnZobristKey ^= ChessBoard.zkPieceValues[Long.numberOfTrailingZeros(pieces)][WHITE][PAWN];
			pieces &= pieces - 1;
		}
		pieces = cb.pieces[BLACK][PAWN];
		while (pieces != 0) {
			cb.pawnZobristKey ^= ChessBoard.zkPieceValues[Long.numberOfTrailingZeros(pieces)][BLACK][PAWN];
			pieces &= pieces - 1;
		}
	}

	// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
	private static void setPieces(final ChessBoard cb, final String fenPieces) {

		// clear pieces
		for (int color = 0; color < 2; color++) {
			for (int pieceIndex = 1; pieceIndex <= KING; pieceIndex++) {
				cb.pieces[color][pieceIndex] = 0;
			}
		}

		int positionCount = 63;
		for (int i = 0; i < fenPieces.length(); i++) {

			final char character = fenPieces.charAt(i);
			switch (character) {
			case '/':
				continue;
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
				positionCount -= Character.digit(character, 10);
				break;
			case 'P':
				cb.pieces[WHITE][PAWN] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'N':
				cb.pieces[WHITE][NIGHT] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'B':
				cb.pieces[WHITE][BISHOP] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'R':
				cb.pieces[WHITE][ROOK] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'Q':
				cb.pieces[WHITE][QUEEN] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'K':
				cb.pieces[WHITE][KING] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'p':
				cb.pieces[BLACK][PAWN] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'n':
				cb.pieces[BLACK][NIGHT] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'b':
				cb.pieces[BLACK][BISHOP] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'r':
				cb.pieces[BLACK][ROOK] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'q':
				cb.pieces[BLACK][QUEEN] |= Util.POWER_LOOKUP[positionCount--];
				break;
			case 'k':
				cb.pieces[BLACK][KING] |= Util.POWER_LOOKUP[positionCount--];
				break;
			}
		}
	}

	public static void init(ChessBoard cb) {
		cb.kingIndex[WHITE] = Long.numberOfTrailingZeros(cb.pieces[WHITE][KING]);
		cb.kingIndex[BLACK] = Long.numberOfTrailingZeros(cb.pieces[BLACK][KING]);
		cb.kingArea[WHITE] = ChessConstants.KING_SAFETY_FRONT_FURTHER[WHITE][cb.kingIndex[WHITE]] | ChessConstants.KING_SAFETY_FRONT[WHITE][cb.kingIndex[WHITE]]
				| ChessConstants.KING_SAFETY_NEXT[cb.kingIndex[WHITE]] | ChessConstants.KING_SAFETY_BEHIND[WHITE][cb.kingIndex[WHITE]];
		cb.kingArea[BLACK] = ChessConstants.KING_SAFETY_FRONT_FURTHER[BLACK][cb.kingIndex[BLACK]] | ChessConstants.KING_SAFETY_FRONT[BLACK][cb.kingIndex[BLACK]]
				| ChessConstants.KING_SAFETY_NEXT[cb.kingIndex[BLACK]] | ChessConstants.KING_SAFETY_BEHIND[BLACK][cb.kingIndex[BLACK]];

		cb.colorToMoveInverse = 1 - cb.colorToMove;
		cb.friendlyPieces[WHITE] = cb.pieces[WHITE][PAWN] | cb.pieces[WHITE][BISHOP] | cb.pieces[WHITE][NIGHT] | cb.pieces[WHITE][KING] | cb.pieces[WHITE][ROOK]
				| cb.pieces[WHITE][QUEEN];
		cb.friendlyPieces[BLACK] = cb.pieces[BLACK][PAWN] | cb.pieces[BLACK][BISHOP] | cb.pieces[BLACK][NIGHT] | cb.pieces[BLACK][KING] | cb.pieces[BLACK][ROOK]
				| cb.pieces[BLACK][QUEEN];
		cb.allPieces = cb.friendlyPieces[WHITE] | cb.friendlyPieces[BLACK];
		cb.emptySpaces = ~cb.allPieces;

		Arrays.fill(cb.pieceIndexes, ChessConstants.EMPTY);
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
		cb.pinnedPieces = cb.getPinnedPieces();
		cb.psqtScore = EvalUtil.calculatePositionScores(cb);
		cb.psqtScoreEg = EvalUtil.calculatePositionEgScores(cb);

		if (!EngineConstants.isTuningSession) {
			// cached scores are not used
			calculatePawnZobristKeys(cb);
			calculateZobristKeys(cb);
		}
	}

	public static String toString(ChessBoard cb) {
		// TODO castling, EP, moves
		StringBuilder sb = new StringBuilder();
		for (int i = 63; i >= 0; i--) {
			if ((cb.friendlyPieces[WHITE] & Util.POWER_LOOKUP[i]) != 0) {
				sb.append(ChessConstants.FEN_WHITE_PIECES[cb.pieceIndexes[i]]);
			} else {
				sb.append(ChessConstants.FEN_BLACK_PIECES[cb.pieceIndexes[i]]);
			}
			if (i % 8 == 0 && i != 0) {
				sb.append("/");
			}
		}

		// color to move
		String colorToMove = cb.colorToMove == WHITE ? "w" : "b";
		sb.append(" ").append(colorToMove).append(" ");

		// castling rights
		if (cb.castlingRights == 0) {
			sb.append("-");
		} else {
			if ((cb.castlingRights & 8) != 0) { // 1000
				sb.append("K");
			}
			if ((cb.castlingRights & 4) != 0) { // 0100
				sb.append("Q");
			}
			if ((cb.castlingRights & 2) != 0) { // 0010
				sb.append("k");
			}
			if ((cb.castlingRights & 1) != 0) { // 0001
				sb.append("q");
			}
		}

		String fen = sb.toString();
		fen = fen.replaceAll("11111111", "8");
		fen = fen.replaceAll("1111111", "7");
		fen = fen.replaceAll("111111", "6");
		fen = fen.replaceAll("11111", "5");
		fen = fen.replaceAll("1111", "4");
		fen = fen.replaceAll("111", "3");
		fen = fen.replaceAll("11", "2");

		return fen;
	}

}
