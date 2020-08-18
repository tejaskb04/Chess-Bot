package chess.bots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;
import cse332.exceptions.NotYetImplementedException;

/**
 * This class should implement the minimax algorithm as described in the
 * assignment handouts.
 */
public class SimpleSearcher<M extends Move<M>, B extends Board<M, B>> extends
AbstractSearcher<M, B> {


	public M getBestMove(B board, int myTime, int opTime) {
		/* Calculate the best move */
		BestMove<M> best = minimax(this.evaluator, board, ply);
		reportNewBestMove(best.move); // aesthetics  
		return best.move;
	}

	static <M extends Move<M>, B extends Board<M, B>> BestMove<M> minimax(Evaluator<B> evaluator, B board, int depth) {
		if(depth == 0) {
			return new BestMove<M>(null, evaluator.eval(board));
		}
		List<M> moves = board.generateMoves();
		if(moves.isEmpty()) {
			if (board.inCheck()) {
				return  new BestMove<M>(null,-evaluator.mate() - depth);
			}
			return  new BestMove<M>(null,-evaluator.stalemate());
		}

		BestMove<M> result = new BestMove<M>(null, -evaluator.infty());

		for(M move : moves) {
			board.applyMove(move);
			int value = -minimax(evaluator, board, depth-1).value;
			board.undoMove();
			if(value > result.value) {
				result.value = value;
				result.move = move;
			}
		}
		return result;
	}
}