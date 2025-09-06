import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;
import fais.zti.oramus.gomoku.Position;

import java.util.ArrayList;
import java.util.List;

class GameStatePublisher {

    private Mark playerMark;
    public GameStatePublisher(Mark playerMark){
        this.playerMark = playerMark;
    }
    private List<GameObserver> observers = new ArrayList<>();

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void publishMove(Cell cell, MoveType type) {
        Move move = new Move(new Position(cell.getCol(), cell.getRow()), playerMark);
        for (GameObserver obs : observers) {
            obs.onCandidateMove(move, type);
        }
    }

    public void publishInvalid(String reason) {
        for (GameObserver obs : observers) {
            obs.onInvalidState(reason);
        }
    }
}