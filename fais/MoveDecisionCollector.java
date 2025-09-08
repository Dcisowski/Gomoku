import fais.zti.oramus.gomoku.Move;

public final class MoveDecisionCollector implements MoveObserver {

    private Move win;
    private Move block;
    private Move openFour;
    private Move doubleThreat;
    private Move any;

    @Override
    public void onCandidate(MoveType type, Move move) {
        if (move == null) return;
        switch (type) {
            case WINNING:             win = better(win, move); break;
            case BLOCKING:            block = better(block, move); break;
            case CREATE_OPEN_FOUR:    openFour = better(openFour, move); break;
            case CREATE_DOUBLE_THREAT:doubleThreat = better(doubleThreat, move); break;
            case ANY:                 any = better(any, move); break;
            default: /* RESIGN nie trafia tu */
        }
    }

    /** Rekomendowany ruch wg priorytetów. */
    public Move best() {
        if (win != null) return win;
        if (block != null) return block;
        if (openFour != null) return openFour;
        if (doubleThreat != null) return doubleThreat;
        return any;
    }

    public MoveType bestType() {
        if (win != null) return MoveType.WINNING;
        if (block != null) return MoveType.BLOCKING;
        if (openFour != null) return MoveType.CREATE_OPEN_FOUR;
        if (doubleThreat != null) return MoveType.CREATE_DOUBLE_THREAT;
        if (any != null) return MoveType.ANY;
        return null;
    }


    private Move better(Move a, Move b) {
        if (a == null) return b;
        if (b == null) return a;
        return TieBreaker.better(nullCenterSize(a, b), a, b);
    }

    private static class CmpBoard extends Board { CmpBoard(int n){ super(n,false); } }
    private Board nullCenterSize(Move a, Move b) {
        int n = Math.max(
                Math.max(a.position().row(), b.position().row()),
                Math.max(a.position().col(), b.position().col())
        ) * 2 + 3;
        return new CmpBoard(n);
    }
}

