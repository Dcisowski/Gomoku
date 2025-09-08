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

        int numPlayerMoves = 0;
        int numOpponentMoves = 0;

        if(boardState!=null){
            Iterator<Move> it = boardState.iterator();
            while(it.hasNext()){
                Move m = it.next();
                int r = m.position().row(); int c = m.position().col();
                if(r<0||r>=n||c<0||c>=n) throw new WrongBoardStateException();
                if(!b.isEmpty(r,c)) throw new WrongBoardStateException();
                b.set(r,c, m.mark());
                if (m.mark() == nextMoveMark) numPlayerMoves++;
                if (m.mark() != nextMoveMark) numOpponentMoves++;
            }
        }

        // >>> POLICY PLUG-IN: kolektor jest JEDYNYM źródłem decyzji
        MoveDecisionCollector collector = new MoveDecisionCollector();
        MoveAnalyzer analyzer = new MoveAnalyzer(collector);

        // analyze() publikuje kandydatów do kolektora i może rzucić RESIGN
        AnalysisResult res = analyzer.analyze(b, nextMoveMark);

        // ostateczna decyzja pochodzi z kolektora
        Move decided = collector.best();
        if (decided != null) {
            return decided;
        }

        // awaryjny fallback (nie powinien zajść, ale zostawiamy na wszelki wypadek)
        if(res != null && res.move != null) return res.move;

        // jeśli nic nie ma — to sytuacja błędna; bezpiecznie zasygnalizuj poddanie
        throw new ResignException();
    }
}
