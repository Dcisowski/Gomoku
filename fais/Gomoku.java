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

        if(boardState!=null){
            Iterator<Move> it = boardState.iterator();
            while(it.hasNext()){
                Move m = it.next();
                int r = m.position().row(); int c = m.position().col();
                if(r<0||r>=n||c<0||c>=n) throw new WrongBoardStateException();
                if(!b.isEmpty(r,c)) throw new WrongBoardStateException();
                b.set(r,c, m.mark());
            }
        }

        // NEW: kolektor decyzji – obserwator (nie zmienia logiki analyze())
        MoveDecisionCollector collector = new MoveDecisionCollector();
        MoveAnalyzer analyzer = new MoveAnalyzer(collector);

        AnalysisResult res = analyzer.analyze(b, nextMoveMark);
        // Można by porównać z collector.best(), ale zostawiamy wynik analyze() jako źródło prawdy:
        if(res.type == MoveType.RESIGN) throw new ResignException();
        return res.move;
    }
}
