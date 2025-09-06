import fais.zti.oramus.gomoku.Move;

interface GameObserver {
    void onCandidateMove(Move move, MoveType type);
    void onInvalidState(String reason);
}