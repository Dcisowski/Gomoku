import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;

/** Silnik skanujący linie planszy wzorcami i publikujący kandydatów do obserwatora. */
public final class PatternEngine {

    private static final Direction[] AXES = new Direction[]{Direction.E, Direction.S, Direction.SE, Direction.NE};

    public void scan(Board b, Mark mine, Mark opp, MoveObserver obs){
        if (obs == null) return;

        scanPattern(b, mine, opp, Patterns.openFour(), obs, MoveType.WINNING, true);
        scanPattern(b, mine, opp, Patterns.openThree(), obs, MoveType.CREATE_OPEN_FOUR, false);

        scanPattern(b, mine, opp, Patterns.brokenFour(), obs, MoveType.CREATE_OPEN_FOUR, false);
    }

    private void scanPattern(Board b, Mark mine, Mark opp, Pattern pat, MoveObserver obs, MoveType asType, boolean emitEndsAsMoves){
        int n = b.size();
        int i, j;

        // Horyzontalne (wiersze)
        for(i=0;i<n;i++){
            LineSlice ls = new LineSlice(b, i, 0, Direction.E, n); // wiersz ma zawsze n
            slideAndEmit(ls, mine, opp, pat, obs, asType, emitEndsAsMoves);
        }
        // Pionowe (kolumny)
        for(j=0;j<n;j++){
            LineSlice ls = new LineSlice(b, 0, j, Direction.S, n); // kolumna ma zawsze n
            slideAndEmit(ls, mine, opp, pat, obs, asType, emitEndsAsMoves);
        }
        // Skośne SE zaczynające z górnej krawędzi
        for(j=0;j<n;j++){
            int len = diagonalLength(b, 0, j, Direction.SE, n);
            LineSlice ls = new LineSlice(b, 0, j, Direction.SE, len);
            slideAndEmit(ls, mine, opp, pat, obs, asType, emitEndsAsMoves);
        }
        // Skośne SE zaczynające z lewej krawędzi (bez (0,0) – już skanowane)
        for(i=1;i<n;i++){
            int len = diagonalLength(b, i, 0, Direction.SE, n);
            LineSlice ls = new LineSlice(b, i, 0, Direction.SE, len);
            slideAndEmit(ls, mine, opp, pat, obs, asType, emitEndsAsMoves);
        }
        // Skośne NE zaczynające z dolnej krawędzi
        for(j=0;j<n;j++){
            int len = diagonalLength(b, n-1, j, Direction.NE, n);
            LineSlice ls = new LineSlice(b, n-1, j, Direction.NE, len);
            slideAndEmit(ls, mine, opp, pat, obs, asType, emitEndsAsMoves);
        }
        // Skośne NE zaczynające z lewej krawędzi (bez (n-1,0))
        for(i=n-2;i>=0;i--){
            int len = diagonalLength(b, i, 0, Direction.NE, n);
            LineSlice ls = new LineSlice(b, i, 0, Direction.NE, len);
            slideAndEmit(ls, mine, opp, pat, obs, asType, emitEndsAsMoves);
        }
    }

    private int diagonalLength(Board b, int r0, int c0, Direction d, int n){
        if (b.isPeriodic()) return n;
        int len = 1;
        int rr = r0, cc = c0;
        // maksymalnie n-1 kroków do brzegu w trybie bez periodyki
        for (int steps = 0; steps < n-1; steps++) {
            Point p = b.next(rr, cc, d);
            if (p == null) break;
            rr = p.r; cc = p.c;
            len++;
        }
        return len;
    }

    private void slideAndEmit(LineSlice ls, Mark mine, Mark opp, Pattern pat, MoveObserver obs, MoveType asType, boolean emitEndsAsMoves){
        int L = ls.len;
        int span = pat.span();
        if (span == 0 || L < span) return;
        int s;
        for(s=0; s <= L - span; s++){
            if (pat.matches(ls.snap, s, mine, opp)) {
                if (emitEndsAsMoves) {
                    // Pierwsze i ostatnie pole w oknie (E ... E)
                    Move m1 = ls.moveAt(s, mine);
                    Move m2 = ls.moveAt(s + span - 1, mine);
                    if (m1 != null) obs.onCandidate(asType, m1);
                    if (m2 != null) obs.onCandidate(asType, m2);
                } else {
                    pat.suggest(ls, s, mine, opp, obs, asType);
                }
            }
        }
    }
}
