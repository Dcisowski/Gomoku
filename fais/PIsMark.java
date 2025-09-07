// NEW: PIsMark.java
import fais.zti.oramus.gomoku.Mark;

public final class PIsMark implements Pattern {
    private final Mark m;
    public PIsMark(Mark m){ this.m = m; }
    public int span(){ return 1; }
    public boolean matches(Mark[] line, int start, Mark mine, Mark opp){
        return line[start] == m;
    }
    public void suggest(LineSlice ctx, int start, Mark mine, Mark opp, MoveObserver obs, MoveType asType){ }
}
