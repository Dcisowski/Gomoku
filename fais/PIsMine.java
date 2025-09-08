import fais.zti.oramus.gomoku.Mark;

public final class PIsMine implements Pattern {
    public int span(){ return 1; }
    public boolean matches(Mark[] line, int start, Mark mine, Mark opp){
        return line[start] == mine;
    }
    public void suggest(LineSlice ctx, int start, Mark mine, Mark opp, MoveObserver obs, MoveType asType){ }
}
