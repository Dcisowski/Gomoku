// NEW: OrPattern.java
import fais.zti.oramus.gomoku.Mark;

public final class OrPattern implements Pattern {
    private final Pattern[] alts;
    private final int span;

    public OrPattern(Pattern[] alts){
        this.alts = alts;
        // wymagamy tej samej rozpiętości okna dla alternatyw
        this.span = (alts.length > 0 ? alts[0].span() : 0);
    }

    public int span(){ return span; }

    public boolean matches(Mark[] line, int start, Mark mine, Mark opp){
        int i;
        for(i=0;i<alts.length;i++){
            if(alts[i].matches(line, start, mine, opp)) return true;
        }
        return false;
    }

    public void suggest(LineSlice ctx, int start, Mark mine, Mark opp, MoveObserver obs, MoveType asType){
        int i;
        for(i=0;i<alts.length;i++){
            if(alts[i].matches(ctx.snap, start, mine, opp)){
                alts[i].suggest(ctx, start, mine, opp, obs, asType);
            }
        }
    }
}
