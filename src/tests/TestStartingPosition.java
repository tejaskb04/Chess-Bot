package tests;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.AlphaBetaSearcher;
import chess.bots.JamboreeSearcher;
import chess.bots.LazySearcher;
import chess.bots.ParallelSearcher;
import chess.bots.SimpleSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

public class TestStartingPosition {
	public static final String[] FENS = {"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -",
			"r2q1rk1/p1N2p1p/2n2np1/3pp1B1/8/3P1NP1/PP2PRBP/R2Q2K1 b - -",
	"3Q2k1/p4r1p/8/4p3/3n4/3P1NP1/PP1BPRB1/R5K1 b - -"};
	
	public static final ParallelSearcher<ArrayMove, ArrayBoard> parallel = new ParallelSearcher<>();
	public static final JamboreeSearcher<ArrayMove, ArrayBoard> jamboree = new JamboreeSearcher<>();
    public static final AlphaBetaSearcher<ArrayMove, ArrayBoard> alphabeta = new AlphaBetaSearcher<>();
	public static final SimpleSearcher<ArrayMove, ArrayBoard> minimax = new SimpleSearcher<>();
    
    public static ArrayMove getBestMove(String fen, Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) { 
		searcher.setDepth(depth);
		searcher.setCutoff(cutoff);
		searcher.setEvaluator(new SimpleEvaluator());

		return searcher.getBestMove(ArrayBoard.FACTORY.create().init(fen), 0, 0);
	}

	public static void printMove(String fen, Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
		getBestMove(fen, searcher, 5, cutoff);
	}

	public static void cutoffTester() {
		//boards
		for(int i = 0; i< FENS.length; i++) {
			System.out.println("BOARD: " + i);
			for(int j = 0; j<6; j++) {
				System.out.println("CUTOFF : " + j);
				System.out.println();
				for(int k = 0; k<6; k++) { // 6 trials get rid of first one
					System.out.println("TRIAL: " + i);

					long x = System.currentTimeMillis();
					getBestMove(FENS[i], parallel, 5, j);
					long y = System.currentTimeMillis();

					System.out.println("parallel Searcher trial " + k + " and cutoff "+j+ ": " + (y-x));

					long z = System.currentTimeMillis();
					getBestMove(FENS[i], jamboree, 5, j);
					long w = System.currentTimeMillis();
					System.out.println("jamboree Searcher trial " + k + " and cutoff "+j+ ": "+ (w-z));
					System.out.println("_____________________________________________________");
					System.out.println();
				}
			}

			System.out.println("******************************************************");
			System.out.println("NEXT BOARD");
			System.out.println("******************************************************");
		}
	}

	public static void coreTester() {
		for(String fen: FENS) {
			for(int i = 0; i < 6; i++) {
				long x = System.currentTimeMillis();
				getBestMove(fen, parallel, 5, 2);
				long y = System.currentTimeMillis();

				System.out.println("parallel Searcher trial " + i + " and cutoff "+2+ ": " + (y-x));

				long z = System.currentTimeMillis();
				getBestMove(fen, jamboree, 5, 2);
				long w = System.currentTimeMillis();
				System.out.println("jamboree Searcher trial " + i + " and cutoff "+2+ ": "+ (w-z));
				System.out.println("_____________________________________________________");
				System.out.println();
			}
			System.out.println("******************************************************");
			System.out.println("NEXT BOARD");
			System.out.println("******************************************************");
		}
	}
	public static void algoTester() {
		for(String fen: FENS) {
			for(int i = 0; i <6; i++) {
				
				long a = System.currentTimeMillis();
				getBestMove(fen, minimax, 5, 2);
				long b = System.currentTimeMillis();
				System.out.println("minimax Searcher trial " + i + ": " + (b-a));

				long c = System.currentTimeMillis();
				getBestMove(fen, alphabeta, 5, 2);
				long d = System.currentTimeMillis();
				System.out.println("alphabeta Searcher trial " + i + ": "+ (d-c));
				
				long e = System.currentTimeMillis();
				getBestMove(fen, parallel, 5, 2);
				long f = System.currentTimeMillis();
				System.out.println("Parallel Searcher trial " + i + ": "+ (f-e));
				
				
				long g = System.currentTimeMillis();
				getBestMove(fen, jamboree, 5, 2);
				long h = System.currentTimeMillis();
				System.out.println("jamboree Searcher trial " + i +": "+ (h-g));
				System.out.println("_____________________________________________________");
				System.out.println();
			}
				
			System.out.println("******************************************************");
			System.out.println("NEXT BOARD");
			System.out.println("******************************************************");
		}
	}
	public static void main(String[] args) {
		//cutoffTester();
		//coreTester();
		algoTester();
	}
}
