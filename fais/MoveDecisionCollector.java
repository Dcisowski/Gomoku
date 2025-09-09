import fais.zti.oramus.gomoku.Move;

public final class MoveDecisionCollector implements MoveObserver {

    private Move win;
    private Move blockWinning;
    private Move block;
    private Move openFour;
    private Move bestDoubleThreat;
    private Move doubleThreat;
    private Move any;

    @Override
    public void onCandidate(MoveType type, Move move) {
        if (move == null) return;
        switch (type) {
            case WINNING:             win = better(win, move); break;
            case BLOCKING_WINNING:    blockWinning = better(blockWinning, move); break;
            case BLOCKING:            block = better(block, move); break;
            case CREATE_OPEN_FOUR:    openFour = better(openFour, move); break;
            case CREATE_BEST_DOUBLE_THREAT:bestDoubleThreat = better(openFour, move); break;
            case CREATE_DOUBLE_THREAT:doubleThreat = better(doubleThreat, move); break;
            case ANY:                 any = better(any, move); break;
            default: /* RESIGN nie trafia tu */
        }
    }

    /** Rekomendowany ruch wg priorytetów. */
    public Move best() {
        if (win != null) return win;
        if (blockWinning != null) return blockWinning;
        if (block != null) return block;
        if (openFour != null) return openFour;
        if (bestDoubleThreat != null) return bestDoubleThreat;
        if (doubleThreat != null) return doubleThreat;
        return any;
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

