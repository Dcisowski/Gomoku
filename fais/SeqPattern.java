// NEW: SeqPattern.java
import fais.zti.oramus.gomoku.Mark;

public final class SeqPattern implements Pattern {
    private final Pattern[] parts;
    private final int span;

    public SeqPattern(Pattern[] parts){
        this.parts = parts;
        int s = 0, i;
        for(i=0;i<parts.length;i++) s += parts[i].span();
        this.span = s;
    }

    public int span(){ return span; }

    public boolean matches(Mark[] line, int start, Mark mine, Mark opp){
        int off = start; int i;
        for(i=0;i<parts.length;i++){
            if(!parts[i].matches(line, off, mine, opp)) return false;
            off += parts[i].span();
        }
        return true;
    }

    public void suggest(LineSlice ctx, int start, Mark mine, Mark opp, MoveObserver obs, MoveType asType){
        int off = start; int i;
        for(i=0;i<parts.length;i++){
            parts[i].suggest(ctx, off, mine, opp, obs, asType);
            off += parts[i].span();
        }
    }
}
