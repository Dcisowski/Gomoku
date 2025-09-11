import fais.zti.oramus.gomoku.*;

import java.util.Iterator;
import java.util.Set;

public class Gomoku implements Game {
    private int n = 10;
    private boolean periodic = false;
    private Mark first = Mark.CROSS;

    @Override
    public void firstMark(Mark first) {
        this.first = first;
    }

    @Override
    public void size(int size) {
        this.n = size;
    }

    @Override
    public void periodicBoundaryConditionsInUse() {
        this.periodic = true;
    }

    @Override
    public Move nextMove(Set<Move> boardState, Mark nextMoveMark)
            throws ResignException, TheWinnerIsException, WrongBoardStateException {
        BoardFactory f = periodic ? new PeriodicBoardFactory() : new StandardBoardFactory();
        Board b = f.create(n);

        if (first == null) {
            throw new WrongBoardStateException();
        }

        if (boardState != null) {
            Iterator<Move> it = boardState.iterator();
            while (it.hasNext()) {
                Move m = it.next();
                int r = m.position().row();
                int c = m.position().col();
                if (r < 0 || r >= n || c < 0 || c >= n) throw new WrongBoardStateException();
                if (!b.isEmpty(r, c)) throw new WrongBoardStateException();
                b.set(r, c, m.mark());
            }
        }

        int cross = b.countMarks(Mark.CROSS);
        int nought = b.countMarks(Mark.NOUGHT);
        int diff = cross - nought;

        if (first == Mark.CROSS) {
            if (nought > cross) throw new WrongBoardStateException();
            if (diff == 0 && nextMoveMark != Mark.CROSS) throw new WrongBoardStateException();
            if (diff == 1 && nextMoveMark != Mark.NOUGHT) throw new WrongBoardStateException();
            if (cross - nought > 1) throw new WrongBoardStateException();
        } else {
            if (cross > nought) throw new WrongBoardStateException();
            if (diff == 0 && nextMoveMark != Mark.NOUGHT) throw new WrongBoardStateException();
            if (diff == -1 && nextMoveMark != Mark.CROSS) throw new WrongBoardStateException();
            if (nought - cross > 1) throw new WrongBoardStateException();
        }

        MoveDecisionCollector collector = new MoveDecisionCollector();
        MoveAnalyzer analyzer = new MoveAnalyzer(collector);

        analyzer.analyze(b, nextMoveMark);

        Move decided = collector.best();
        if (decided != null) {
            return decided;
        }

        throw new ResignException();
    }
}
