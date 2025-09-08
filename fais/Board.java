import fais.zti.oramus.gomoku.Mark;


public class Board {
    private final int n;
    private final boolean periodic;
    private final Mark[][] grid;


    public Board(int n, boolean periodic) {
        this.n = n;
        this.periodic = periodic;
        this.grid = new Mark[n][n];
        int r, c;
        for (r = 0; r < n; r++) for (c = 0; c < n; c++) grid[r][c] = Mark.NULL;
    }

    public int size() {
        return n;
    }

    public boolean isPeriodic() {
        return periodic;
    }

    public Mark get(int r, int c) {
        return grid[r][c];
    }

    public void set(int r, int c, Mark m) {
        grid[r][c] = m;
    }

    public boolean isEmpty(int r, int c) {
        return grid[r][c] == Mark.NULL;
    }


    public boolean inBounds(int r, int c) {
        return r >= 0 && r < n && c >= 0 && c < n;
    }


    public Board copy() {
        Board b = new Board(n, periodic);
        int r, c;
        for (r = 0; r < n; r++) for (c = 0; c < n; c++) b.grid[r][c] = grid[r][c];
        return b;
    }


    public Point next(int r, int c, Direction d) {
        int nr = r + d.dr, nc = c + d.dc;
        if (periodic) {
            if (nr < 0) nr = n - 1;
            else if (nr >= n) nr = 0;
            if (nc < 0) nc = n - 1;
            else if (nc >= n) nc = 0;
            return new Point(nr, nc);
        } else {
            if (!inBounds(nr, nc)) return null;
            return new Point(nr, nc);
        }
    }


    public int countForward(int r, int c, Direction d, Mark m) {
        int cnt = 0, steps = 0;
        Point p = new Point(r, c);
        while (true) {
            Point q = next(p.r, p.c, d);
            if (q == null) break;
            if (q.r == r && q.c == c) break;
            if (grid[q.r][q.c] == m) {
                cnt++;
                p = q;
            } else break;
            steps++;
            if (steps >= n) break;
        }
        return cnt;
    }


    public int runLengthThrough(int r, int c, Direction d, Mark m) {
        int a = countForward(r, c, Direction.opposite(d), m);
        int b = countForward(r, c, d, m);
        return a + 1 + b;
    }


    public Point[] endsOfRun(int r, int c, Direction d, Mark m) {
        int lr = r, lc = c, steps = 0;
        while (true) {
            Point prev = next(lr, lc, Direction.opposite(d));
            if (prev == null) break;
            if (grid[prev.r][prev.c] == m) {
                lr = prev.r;
                lc = prev.c;
            } else break;
            steps++;
            if (steps >= n) break;
        }
        int rr = r, rc = c;
        steps = 0;
        while (true) {
            Point nxt = next(rr, rc, d);
            if (nxt == null) break;
            if (grid[nxt.r][nxt.c] == m) {
                rr = nxt.r;
                rc = nxt.c;
            } else break;
            steps++;
            if (steps >= n) break;
        }
        Point before = next(lr, lc, Direction.opposite(d));
        Point after = next(rr, rc, d);
        return new Point[]{before, after};
    }


    public int countMarks(Mark m) {
        int cnt = 0;
        int r, c;
        for (r = 0; r < n; r++)
            for (c = 0; c < n; c++)
                if (grid[r][c] == m)
                    cnt++;
        return cnt;
    }
}