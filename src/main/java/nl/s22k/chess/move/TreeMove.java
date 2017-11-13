package nl.s22k.chess.move;

import nl.s22k.chess.ChessConstants.ScoreType;

public class TreeMove {

	public int move;
	public TreeMove nextMove;
	public int score;
	public ScoreType scoreType;

	public TreeMove(int move) {
		this.move = move;
	}

	public TreeMove(int move, int score, ScoreType scoreType) {
		this.move = move;
		this.score = score;
		this.scoreType = scoreType;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(new MoveWrapper(move).toString());
		TreeMove childMove = nextMove;
		while (childMove != null) {
			result.append(" " + new MoveWrapper(childMove.move));
			childMove = childMove.nextMove;
		}

		return result.toString();
	}

	public void appendMove(TreeMove treeMove) {
		TreeMove childMove = nextMove;
		if (nextMove == null) {
			nextMove = treeMove;
		} else {
			while (childMove.nextMove != null) {
				childMove = childMove.nextMove;
			}
			childMove.nextMove = treeMove;
		}
	}

}
