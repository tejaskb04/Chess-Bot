package chess.bots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import chess.bots.JamboreeSearcher.JamboreeTask;
import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class EnhancedJamboree<M extends Move<M>, B extends Board<M, B>> extends AbstractSearcher<M, B> {
	public final ForkJoinPool POOL = new ForkJoinPool();
	public final int DIVIDE_CUTOFF = 3;
	public final double PERCENTAGE_SEQ = 0.5;
	public static class MoveComparator<M extends Move<M>> implements Comparator<M>{

		@Override
		public int compare(M o1, M o2) {
			if(o1.isPromotion() != o2.isPromotion()) {
				return Boolean.compare(o1.isPromotion(), o2.isPromotion());
			}
			return Boolean.compare(o2.isCapture(), o1.isCapture());
		}
	}

	public M getBestMove(final B board, int myTime, int opTime) {
		MoveComparator<M> comp = new MoveComparator<M>();
		List<M> moves = board.generateMoves();
		Collections.sort(moves, comp);
		Collections.sort(moves, new Comparator<M>() {
			public int compare(M o1,M o2) {
				board.applyMove(o1);
				int val1 = evaluator.eval(board);
				board.undoMove();
				board.applyMove(o2);
				int val2 = evaluator.eval(board);
				board.undoMove();
				return Integer.compare(val1,  val2);
			}
		});
		BestMove<M> move = POOL.invoke(new JamboreeTask(moves, board, super.evaluator,
				-super.evaluator.infty(), super.evaluator.infty(),7, 2,
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
				board.applyMove(move);

				if (depth <= cutoff) {
					return AlphaBetaSearcher.alphabeta(eval, board, depth, alpha, beta);
				}
				MoveComparator<M> comp = new MoveComparator<M>();
				moves = board.generateMoves();
				Collections.sort(moves, comp);
				Collections.sort(moves, new Comparator<M>() {
					public int compare(M o1,M o2) {
						board.applyMove(o1);
						int val1 = evaluator.eval(board);
						board.undoMove();
						board.applyMove(o2);
						int val2 = evaluator.eval(board);
						board.undoMove();
						return Integer.compare(val1, val2);
					}
				});

				if (moves.isEmpty()) {
					if (board.inCheck()) {
						return new BestMove<M>(null, -eval.mate() - depth);
					} else {
						return new BestMove<M>(null, -eval.stalemate());
					}
				}

				int seqCutoff = (int) (moves.size() * PERCENTAGE_SEQ);

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

				for (int i = lo; i < hi; i++) {
					JamboreeTask result = new JamboreeTask(moves, board, eval, -beta, -alpha,
							depth - 1, cutoff, lo, hi, true, moves.get(i));
					result.fork();
					list.add(result);
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
