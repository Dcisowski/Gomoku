import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;
import fais.zti.oramus.gomoku.Position;

public final class LineSlice {
    public final Board b;
    public final int r0, c0;
    public final Direction dir;
    public final int len;
    public final Mark[] snap;

    public LineSlice(Board b, int r0, int c0, Direction d, int len){
        this.b = b; this.r0 = r0; this.c0 = c0; this.dir = d; this.len = len;
        this.snap = new Mark[len];
        int i; int rr=r0, cc=c0;
        this.snap[0] = b.get(rr, cc);
        for(i=1;i<len;i++){
            Point p = b.next(rr, cc, d);
            if(p==null){ this.snap[i] = null; rr=-1; cc=-1; }
            else{
                rr = p.r; cc = p.c; this.snap[i] = b.get(rr, cc);
            }
        }
    }

    public Position posAt(int offset){
        int rr = r0, cc = c0, i;
        for(i=0;i<offset;i++){
            Point p = b.next(rr, cc, dir);
            if(p==null) return null;
            rr = p.r; cc = p.c;
        }
        return new Position(cc, rr);
    }

    public Move moveAt(int offset, Mark m){
        Position p = posAt(offset);
        if(p==null) return null;
        return new Move(p, m);
    }
}
