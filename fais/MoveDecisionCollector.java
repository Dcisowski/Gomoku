// NEW: MoveDecisionCollector.java
import fais.zti.oramus.gomoku.Move;

public final class MoveDecisionCollector implements MoveObserver {

    // przechowujemy po jednym „najlepszym” kandydacie na typ
    private Move win;
    private Move block;
    private Move openFour;
    private Move doubleThreat;
    private Move any;

    @Override
    public void onCandidate(MoveType type, Move move) {
        if (move == null) return;
        switch (type) {
            case WINNING:
                win = better(win, move);
                break;
            case BLOCKING:
                block = better(block, move);
                break;
            case CREATE_OPEN_FOUR:
                openFour = better(openFour, move);
                break;
            case CREATE_DOUBLE_THREAT:
                doubleThreat = better(doubleThreat, move);
                break;
            case ANY:
                any = better(any, move);
                break;
            default:
                // RESIGN nie trafia tu
        }
    }

    /** Zwraca najlepszy ruch wg priorytetów; może zwrócić null jeśli nic nie zarejestrowano. */
    public Move best() {
        if (win != null) return win;
        if (block != null) return block;
        if (openFour != null) return openFour;
        if (doubleThreat != null) return doubleThreat;
        return any;
    }

    /** Wyczyść zebrane propozycje. */
    public void clear() {
        win = block = openFour = doubleThreat = any = null;
    }

    /** Prosty tie-breaker: wolę bliżej środka; w razie remisu mniejszy (row,col). */
    private Move better(Move a, Move b) {
        if (a == null) return b;
        if (b == null) return a;
        return TieBreaker.better(nullCenterSize(a, b), a, b);
    }

    /** Hack: używamy TieBreaker, ale on potrzebuje Board do wyliczenia środka.
     *  Dokładne porównanie środka robimy tu lokalnie i opakowujemy wyniki.
     */
    private static class CmpBoard extends Board {
        private final int n;
        CmpBoard(int n) { super(n, false); this.n = n; }
        @Override public int size() { return n; }
    }
    private Board nullCenterSize(Move a, Move b) {
        int n = Math.max(
                Math.max(a.position().row(), b.position().row()),
                Math.max(a.position().col(), b.position().col())
        ) * 2 + 3; // byle dodatnia, tylko do wyznaczenia środka
        return new CmpBoard(n);
    }
}
