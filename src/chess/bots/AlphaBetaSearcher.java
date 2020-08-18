package chess.bots;

import java.util.List;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;
import cse332.exceptions.NotYetImplementedException;

public class AlphaBetaSearcher<M extends Move<M>, B extends Board<M, B>> extends AbstractSearcher<M, B> {
    public M getBestMove(B board, int myTime, int opTime) {
        /* Calculate the best move */
        BestMove<M> best = alphabeta(this.evaluator, board, ply, -this.evaluator.infty(), this.evaluator.infty());
        reportNewBestMove(best.move); // aesthetics  
        return best.move;
    }
    
    static <M extends Move<M>, B extends Board<M, B>> BestMove<M> alphabeta(Evaluator<B> evaluator, B board, int depth, int alpha, int beta) {
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

        BestMove<M> result = new BestMove<M>(null, alpha);

        for(M move : moves) {
            board.applyMove(move);
            int value = -alphabeta(evaluator, board, depth-1, -beta, -alpha).value;
            board.undoMove();
            
            if (value > alpha) {
                alpha = value;
            }
                        
            if (alpha >= beta) {
                result.value = alpha;
                result.move = move;
                return result;
            } else if (value > result.value) {
                result.value = value;
                result.move = move;
            }
        }
        return result;
    }
}