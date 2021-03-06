package tests.gitlab;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

import chess.bots.ParallelSearcher;

import tests.TestsUtility;
import tests.gitlab.TestingInputs;

public class ParallelMinimaxTests extends SearcherTests {

    public static void main(String[] args) { new ParallelMinimaxTests().run(); }
    public static void init() { STUDENT = new ParallelSearcher<ArrayMove, ArrayBoard>(); }

	
	@Override
	protected void run() {
        SHOW_TESTS = true;
        PRINT_TESTERR = true;

        ALLOWED_TIME = Integer.MAX_VALUE;
	    
        //test("depth2", TestingInputs.FENS_TO_TEST.length);
        //test("depth3", TestingInputs.FENS_TO_TEST.length);
        //test("depth4", TestingInputs.FENS_TO_TEST.length);
        long start = System.currentTimeMillis();
        test("depth5", TestingInputs.FENS_TO_TEST.length);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
		
		finish();
	} 
}
