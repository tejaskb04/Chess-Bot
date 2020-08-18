package chess.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;


public class ParallelSearcher<M extends Move<M>, B extends Board<M, B>> extends
AbstractSearcher<M, B> {
    
	public static final ForkJoinPool POOL = new ForkJoinPool();
	public static final int DIVIDE_CUTOFF = 2;

	public M getBestMove(B board, int myTime, int opTime) {
		List<M> moves = board.generateMoves();
		BestMove<M> move = POOL.invoke(new MoveTask(board, moves,evaluator, super.ply, 0, moves.size(), super.cutoff, null, false));
		reportNewBestMove(move.move);
		return move.move;
	}

	private class MoveTask extends RecursiveTask<BestMove<M>>{
		private B board;
		private int depth, lo, hi, cutoff;
		private Evaluator<B> evaluator;
		private List<M> moves;
		private boolean flag;
		private M move;

		public MoveTask(B board,List<M> moves,Evaluator<B> evaluator, int depth, int lo, int hi, int cutoff, M move, Boolean flag) {
			this.board = board;
			this.depth = depth;
			this.lo = lo;
			this.hi = hi;
			this.evaluator = evaluator;
			this.moves = moves;
			this.cutoff = cutoff;
			this.flag = flag;
			this.move = move;
		}

		@Override
		protected BestMove<M> compute() {
			if(flag) {
				board = board.copy();
				board.applyMove(move);
				moves = board.generateMoves();
				lo = 0;
				hi = moves.size();
			}
			if(this.depth <= this.cutoff) {
				return SimpleSearcher.minimax(evaluator, board, depth);
			}else if(hi - lo > DIVIDE_CUTOFF) {

				
				int mid = lo + (hi - lo) /  2;
				MoveTask left = new MoveTask(board, moves,evaluator, this.depth, lo,mid, this.cutoff, null, false);
				MoveTask right = new MoveTask(board, moves,evaluator, this.depth, mid,hi, this.cutoff, null, false);
				left.fork();
				BestMove<M> rightResult = right.compute();
				BestMove<M> leftResult = left.join();

				if(rightResult.value > leftResult.value) {
					return rightResult;
				}else {
					return leftResult;
				}

			}else {   // sequentially fork has to take place here,
				List<MoveTask> list = new ArrayList<MoveTask>();
				for(int i = lo; i < hi; i++) {
					MoveTask result = new MoveTask(board, moves,evaluator, this.depth-1,lo, hi, this.cutoff, moves.get(i), true);
					result.fork();
					list.add(result);
				}
				BestMove<M> best = new BestMove<M>(null, -evaluator.infty());
				for(int i = 0 ; i < list.size(); i++) {
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