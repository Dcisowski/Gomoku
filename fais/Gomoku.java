import fais.zti.oramus.gomoku.*;

import java.util.Set;

public class Gomoku implements Game {
    private boolean periodicBoundaryConditionsFlag;
    private int size;
    private Mark firstMark;

    public Gomoku() {
        periodicBoundaryConditionsFlag = false;
    }

    public boolean getPeriodicBoundaryConditionsFlag() {
        return periodicBoundaryConditionsFlag;
    }

    @Override
    public void firstMark(Mark first) {
        this.firstMark = first;
    }

    @Override
    public void size(int size) {
        this.size=size;
    }

    @Override
    public void periodicBoundaryConditionsInUse() {
        periodicBoundaryConditionsFlag = true;
    }

    @Override
    public Move nextMove(Set<Move> boardState, Mark nextMoveMark) throws ResignException, TheWinnerIsException, WrongBoardStateException {
        Board board = GameInitializer.initialize(size, periodicBoundaryConditionsFlag);
        board.initalizeBoard(boardState);

        GameStatePublisher publisher = new GameStatePublisher(nextMoveMark);
        MoveDecisionCollector collector = new MoveDecisionCollector(board, nextMoveMark);
        publisher.addObserver(collector);
        MoveAnalyzer analyzer = new MoveAnalyzer(publisher, nextMoveMark);
        analyzer.analyze(board);


        return collector.getBestMove();
    }
}
