// CHANGED: Gomoku.java  (tylko fragment nextMove - wstrzyknięcie kolektora)
import fais.zti.oramus.gomoku.*;
import java.util.Iterator;
import java.util.Set;

public class Gomoku implements Game {
    private int n = 10; private boolean periodic = false; private Mark first = Mark.CROSS;

    @Override public void firstMark(Mark first){ this.first = first; }
    @Override public void size(int size){ this.n = size; }
    @Override public void periodicBoundaryConditionsInUse(){ this.periodic = true; }

    @Override
    public Move nextMove(Set<Move> boardState, Mark nextMoveMark)
            throws ResignException, TheWinnerIsException, WrongBoardStateException {
        BoardFactory f = periodic ? new PeriodicBoardFactory() : new StandardBoardFactory();
        Board b = f.create(n);

        if (first == null){
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

        // === NOWA WALIDACJA: zgodność liczby ruchów z regułami gry względem 'first' ===
        int cross = b.count(Mark.CROSS);
        int nought = b.count(Mark.NOUGHT);

        if (first == Mark.CROSS) {
            // X zaczyna: dozwolone układy to  X==O  lub  X==O+1
            if (nought > cross) throw new WrongBoardStateException();
            if (cross - nought > 1) throw new WrongBoardStateException();
        } else { // first == Mark.NOUGHT
            // O zaczyna: dozwolone układy to  O==X  lub  O==X+1
            if (cross > nought) throw new WrongBoardStateException();
            if (nought - cross > 1) throw new WrongBoardStateException();
        }
        // === koniec walidacji ===

        // >>> POLICY PLUG-IN: kolektor jest JEDYNYM źródłem decyzji
        MoveDecisionCollector collector = new MoveDecisionCollector();
        MoveAnalyzer analyzer = new MoveAnalyzer(collector);

        AnalysisResult res = analyzer.analyze(b, nextMoveMark);

        Move decided = collector.best();
        if (decided != null) {
            return decided;
        }
        if (res != null && res.move != null) return res.move;

        throw new ResignException();
    }
}
