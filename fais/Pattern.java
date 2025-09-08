import fais.zti.oramus.gomoku.Mark;

public interface Pattern {
    int span();

    boolean matches(Mark[] line, int start, Mark mine, Mark opp);

    void suggest(LineSlice ctx, int start, Mark mine, Mark opp, MoveObserver obs, MoveType asType);
}