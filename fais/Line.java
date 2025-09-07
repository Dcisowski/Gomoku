// Line.java
import fais.zti.oramus.gomoku.Mark;

public final class Line {
    private final Board b;
    private final int r, c;
    private final Direction d;
    private final Mark m;

    public Line(Board b, int r, int c, Direction d, Mark m){
        this.b = b; this.r = r; this.c = c; this.d = d; this.m = m;
    }

    /** Długość ciągu przechodzącego przez (r,c) w kierunku d. */
    public int length(){ return b.runLengthThrough(r, c, d, m); }

    /** Krańce ciągu (pole przed i pole za), null jeśli poza planszą przy trybie bez periodyki. */
    public Point[] ends(){ return b.endsOfRun(r, c, d, m); }

    /** Liczba otwartych końców (0..2). */
    public int openEnds(){
        Point[] e = ends();
        int open = 0;
        if (e[0] != null && b.isEmpty(e[0].r, e[0].c)) open++;
        if (e[1] != null && b.isEmpty(e[1].r, e[1].c)) open++;
        return open;
    }
}
