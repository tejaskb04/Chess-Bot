package chess.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class JamboreeSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {
    
    public static final ForkJoinPool POOL = new ForkJoinPool();
    public static final int DIVIDE_CUTOFF = 3;
    public static final double PERCENTAGE_SEQ = 0.5;

    public M getBestMove(B board, int myTime, int opTime) {
        List<M> moves = board.generateMoves();
        BestMove<M> move = POOL.invoke(new JamboreeTask(moves, board, super.evaluator,
        		-super.evaluator.infty(), super.evaluator.infty(), super.ply, super.cutoff,
        		0, moves.size(), false, null));
        
        reportNewBestMove(move.move);
        return move.move;
    }
    
    public class JamboreeTask extends RecursiveTask<BestMove<M>> {
        
        private int lo, hi, alpha, beta, depth, cutoff;
        private boolean flag;
        private List<M> moves;
        private B board;
        private Evaluator<B> eval;
        private M move;
        
        public JamboreeTask(List<M> moves, B board, Evaluator<B> eval, int alpha,
        		int beta, int depth, int cutoff, int lo, int hi, boolean flag, M move) {
            this.moves = moves;
            this.board = board;
            this.eval = eval;
            this.alpha = alpha;
            this.beta = beta;
            this.depth = depth;
            this.cutoff = cutoff;
            this.lo = lo;
            this.hi = hi;
            this.flag = flag;
            this.move = move;
        }
        
        @Override
        protected BestMove<M> compute() {
            BestMove<M> best = new BestMove<M>(null, alpha);

            if(flag) {
                board = board.copy();
                //System.err.println(move.toString());
                board.applyMove(move);
               
                if (depth <= cutoff) {
                    return AlphaBetaSearcher.alphabeta(eval, board, depth, alpha, beta);
                }
                
                //moves = board.generateMoves();
                //lo = 0;
                //hi = moves.size();
                
                moves = board.generateMoves();
                
                if (moves.isEmpty()) {
                    if (board.inCheck()) {
                        return new BestMove<M>(null, -eval.mate() - depth);
                    } else {
                        return new BestMove<M>(null, -eval.stalemate());
                    }
                }
                
                int seqCutoff = (int) (moves.size() * PERCENTAGE_SEQ);
                //lo = seqCutoff;
                //hi = moves.size();
                //int seqCutoff = (int) (moves.size() * PERCENTAGE_SEQ);
                
                for (int i = 0; i < seqCutoff; i++) {
                    int value = -(new JamboreeTask(moves, board, eval, -beta, -alpha,
                    				depth - 1, cutoff, lo, hi, true, moves.get(i)).compute().value);
                    
                    if (value > alpha) {
                        alpha = value;
                    }
                                
                    if (alpha >= beta) {
                        best.value = alpha;
                        best.move = move;
                        return best;
                    } else if (value > best.value) {
                        best.value = value;
                        best.move = move;
                    }
                }
                
                lo = seqCutoff;
                hi = moves.size();
            }
            
            if (hi - lo > DIVIDE_CUTOFF) {
                int mid = lo + (hi - lo) / 2;
                
                JamboreeTask left = new JamboreeTask(moves, board, eval, alpha, beta, depth,
                									 cutoff, lo, mid, false, null);
                JamboreeTask right = new JamboreeTask(moves, board, eval, alpha, beta, depth,
                									  cutoff, mid, hi, false, null);
                
                left.fork();
                
                BestMove<M> rightResult = right.compute();
                BestMove<M> leftResult = left.join();
                
                if (leftResult.value > best.value) {
            	        best.move = leftResult.move;
            	        best.value = leftResult.value;
                }
                
                if (rightResult.value > best.value) {
                	    best.move = rightResult.move;
                	    best.value = rightResult.value;
                }
                
                return best;
            } else {
                // sequential fork
                List<JamboreeTask> list = new ArrayList<JamboreeTask>();
                
                for (int i = lo+1; i < hi; i++) {
                    JamboreeTask result = new JamboreeTask(moves, board, eval, -beta, -alpha,
                    							depth - 1, cutoff, lo, hi, true, moves.get(i));
                    result.fork();
                    list.add(result);
                }
                JamboreeTask computer = new JamboreeTask(moves, board, eval, -beta, -alpha,
						depth - 1, cutoff, lo, hi, true, moves.get(lo));
                BestMove<M> computerResult = computer.compute().negate();
                
                if(computerResult.value > best.value) {
					best.value = computerResult.value;
					best.move = moves.get(lo);
				}
                for (int i = 0; i < list.size(); i++) {
                    M move3 = list.get(i).move;
                    BestMove<M> temp = list.get(i).join().negate();
					if(temp.value > best.value) {
						best.value = temp.value;
						best.move = move3;
					}
				}
                return best;
            }
        }  
    }
}