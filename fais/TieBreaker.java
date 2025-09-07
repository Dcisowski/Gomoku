import fais.zti.oramus.gomoku.Move;

public final class TieBreaker {
    private TieBreaker() {
    }

    public static Move better(Board b, Move a, Move c) {
        if (a == null) return c;
        if (c == null) return a;
        int n = b.size();
        int ar = Math.abs(a.position().row() - (n / 2)) + Math.abs(a.position().col() - (n / 2));
        int cr = Math.abs(c.position().row() - (n / 2)) + Math.abs(c.position().col() - (n / 2));
        if (ar != cr) return ar < cr ? a : c;
        if (a.position().row() != c.position().row()) return a.position().row() < c.position().row() ? a : c;
        if (a.position().col() != c.position().col()) return a.position().col() < c.position().col() ? a : c;
        return a;
    }
}