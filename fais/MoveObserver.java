import fais.zti.oramus.gomoku.Move;

public interface MoveObserver {
    void onCandidate(MoveType type, Move move);
}
