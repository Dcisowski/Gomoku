import fais.zti.oramus.gomoku.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MoveAnalyzer {
    private GameStatePublisher publisher;
    private Mark playerSymbol;
    private Mark opponentSymbol;

    public MoveAnalyzer(GameStatePublisher publisher, Mark playerSymbol) {
        this.publisher = publisher;
        this.playerSymbol = playerSymbol;
        this.opponentSymbol = (playerSymbol == Mark.CROSS) ? Mark.NOUGHT : Mark.CROSS;
    }

    public void analyze(Board board) throws TheWinnerIsException, WrongBoardStateException {
        boolean playerAlreadyWon = false;
        boolean opponentAlreadyWon = false;

        Map<String, Integer> threatCounter = new HashMap<>(); // key: x:y, value: count

        List<Line> lines = board.getAllLines();
        for (int l = 0; l < lines.size(); l++) {
            List<Cell> cells = lines.get(l).getCells();
            for (int i = 0; i <= cells.size() - 5; i++) {
                int playerCount = 0, opponentCount = 0, emptyCount = 0;
                Cell lastEmpty = null;
                List<Cell> emptyCells = new ArrayList<>();

                for (int j = 0; j < 5; j++) {
                    Cell c = cells.get(i + j);
                    if (c.getSymbol() == playerSymbol) playerCount++;
                    else if (c.getSymbol() == opponentSymbol) opponentCount++;
                    else {
                        emptyCount++;
                        lastEmpty = c;
                        emptyCells.add(c);
                    }
                }

                // Zwycięstwo (nie wolno kontynuować)
                if (playerCount >= 5) {
                    playerAlreadyWon = true;
//                    throw new TheWinnerIsException(playerSymbol);
                }
                if (opponentCount >= 5) {
                    opponentAlreadyWon = true;
//                    throw new TheWinnerIsException(opponentSymbol);
                }

                if (playerCount == 4 && emptyCount == 1) {
                    publisher.publishMove(lastEmpty, MoveType.WINNING);
                } else if (opponentCount == 4 && emptyCount == 1) {
                    publisher.publishMove(lastEmpty, MoveType.BLOCKING);
                } else if (playerCount == 3 && emptyCount == 2 && isOpenEnds(cells, i, i + 4)) {
                    publisher.publishMove(lastEmpty, MoveType.OPEN_FOUR);
                    for (Cell ec : emptyCells) {
                        String key = ec.getRow() + ":" + ec.getCol();
                        threatCounter.put(key, threatCounter.getOrDefault(key, 0) + 1);
                        publisher.publishMove(ec, MoveType.OPEN_FOUR);
                    }

                }

            }
            // 2) Okna długości 5 – wykrywanie OPEN_FOUR przeciwnika: E O O O E,
            for (int i = 0; i <= cells.size() - 5; i++) {
                Cell c0 = cells.get(i), c4 = cells.get(i + 4);
                if (c0.isEmpty() && c4.isEmpty()) {
                    boolean oppFour = true;
                    for (int k = 1; k <= 3; k++) {
                        if (cells.get(i + k).getSymbol() != opponentSymbol) {
                            oppFour = false;
                            break;
                        }
                    }
                    if (oppFour) {
                        // Musimy blokować – oba końce są kandydatami BLOCKING
                        publisher.publishMove(c0, MoveType.BLOCKING);
                        publisher.publishMove(c4, MoveType.BLOCKING);
                    }
                }
            }
        }


        // 3) DOUBLE_THREAT – to pole należy do co najmniej dwóch OPEN_FOUR (naszych)
        for (Map.Entry<String, Integer> e : threatCounter.entrySet()) {
            if (e.getValue() >= 2) {
                String[] p = e.getKey().split(":");
                Cell c = board.getCell(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
                if (c != null && c.isEmpty()) publisher.publishMove(c, MoveType.DOUBLE_THREAT);
            }
        }

        if (playerAlreadyWon || opponentAlreadyWon) {
            Mark mark = (playerAlreadyWon) ? playerSymbol : opponentSymbol;
            throw new TheWinnerIsException(mark);
        }
//        if (bothAlreadyWon) {
//            throw new WrongBoardStateException();
//        }
    }

    private boolean isOpenEnds(List<Cell> cells, int from, int to) {
        Cell before = (from > 0) ? cells.get(from - 1) : null;
        Cell after = (to < cells.size() - 1) ? cells.get(to + 1) : null;
        return (before != null && before.isEmpty()) && (after != null && after.isEmpty());
    }
}