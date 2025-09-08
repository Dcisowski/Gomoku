import fais.zti.oramus.gomoku.Mark;

public final class RepeatPattern implements Pattern {
    private final Pattern unit;
    private final int times; // stała liczba powtórzeń (ułatwienie – wystarcza do naszych wzorców)

    public RepeatPattern(Pattern unit, int times){
        this.unit = unit; this.times = times;
    }

    public int span(){ return unit.span() * times; }

    public boolean matches(Mark[] line, int start, Mark mine, Mark opp){
        int off = start; int i;
        for(i=0;i<times;i++){
            if(!unit.matches(line, off, mine, opp)) return false;
            off += unit.span();
        }
        return true;
    }

    public void suggest(LineSlice ctx, int start, Mark mine, Mark opp, MoveObserver obs, MoveType asType){
        int off = start; int i;
        for(i=0;i<times;i++){
            unit.suggest(ctx, off, mine, opp, obs, asType);
            off += unit.span();
        }
    }
}
