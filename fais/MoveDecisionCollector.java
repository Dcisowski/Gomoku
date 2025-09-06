import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;
import fais.zti.oramus.gomoku.Position;

import java.util.*;

class MoveDecisionCollector implements GameObserver {
    private Map<MoveType, List<Move>> moveMap = new EnumMap<>(MoveType.class);
    private Board board;
    private Mark playerMark;
    private final GameNeighborhoodResolver resolver;

    public MoveDecisionCollector(Board board, Mark player) {
        for (MoveType t : MoveType.values())
            moveMap.put(t, new ArrayList<>());
        this.board = board;
        this.playerMark = player;
        this.resolver = board.isPeriodic() ? new PeriodicalNeighborhoodResolver() : new StandardNeighborhoodResolver();
    }

    public void onCandidateMove(Move move, MoveType type) {

        List<Move> list = moveMap.get(type);
        // deduplikacja
        for (Move mv : list)
            if (mv.position().row()==move.position().row() && mv.position().col()==move.position().col())
                return;
        list.add(move);
    }

    public void onInvalidState(String reason) {
        System.out.println("Invalid state: " + reason);
    }

    public Move getBestMove() {
        for (MoveType type : new MoveType[]{MoveType.WINNING, MoveType.BLOCKING, MoveType.DOUBLE_THREAT, MoveType.OPEN_FOUR}) {
            List<Move> candidates = moveMap.get(type);
            if (!candidates.isEmpty()) {
                return chooseByTiebreak(candidates, type);
            }
        }
        return fallbackHeuristic();
//        for (MoveType type : MoveType.values()) {
//            if (!moveMap.get(type).isEmpty()) return moveMap.get(type).get(0);
//        }
//        return null;
    }

    private Move chooseByTiebreak(List<Move> candidates, MoveType type) {
        Move best = null; int bestScore = Integer.MIN_VALUE;
        for (Move mv : candidates) {
            int s = score(mv, type);
            if (s > bestScore) { bestScore = s; best = mv; }
        }
        return best;
    }
    private int score(Move move, MoveType type) {
        int base = switch (type) {
            case WINNING -> 1_000_000;
            case BLOCKING -> 900_000;           // > DOUBLE_THREAT
            case DOUBLE_THREAT -> 800_000;      // > OPEN_FOUR
            case OPEN_FOUR -> 700_000;
        };

        if (type == MoveType.BLOCKING) {
            Mark opp = (playerMark == Mark.CROSS) ? Mark.NOUGHT : Mark.CROSS;
            int mates = countBlockedFourPlusOne(move.position().row(), move.position().col(), opp);

            // strongly prefer blocking a mate-in-1 over blocking EEOOOEE
            base += 1_000_000 * mates;   // if mates>0, this dominates the tie-break
        }

        // 1) ile REALNIE mamy po ruchu w linii (liczy tylko nasze kamienie)
        int run = maxRunAfter(move.position().row(), move.position().col(), playerMark);
        // 2) maksymalna DŁUGOŚĆ linii, jaką można kiedykolwiek uzyskać w tym miejscu,
        //    jeśli wypełnimy wszystkie puste do pierwszego przeciwnika/krawędzi
        int potential = maxPotentialAfter(move.position().row(), move.position().col(), playerMark);
        // 3) liczba „żywych” końców po ruchu (0..2)
        int openEnds = openEndsAfter(move.position().row(), move.position().col(), playerMark);
        int openThreeDirs = countOpenThreeForksAfter(move.position().row(),
                move.position().col(),
                playerMark);
        Cell cell = new Cell(move.position().row(), move.position().col());
        // sanity: jeśli kandydat oznaczony jako WINNING nie daje 5 – odrzuć
        if (type == MoveType.WINNING && run < 5) return -1_000_000;

        // Ruchy bez szans na dojście do 5 (zamknięte z obu stron) mocno karzemy:
        if (type != MoveType.WINNING && potential < 5) return -500_000 + softPositionalBonus(cell);

        int easyFourPenalty = 0;
        if (run == 4 && openEnds <= 1 && openThreeDirs < 2) {
            easyFourPenalty = 200_000;  // skaluje się względem innych wag
        }

// 4)   Premia za forka (każdy kierunek open-3 daje premię, 2 kierunki = duża premia)
        int forkBonus = 300_000 * openThreeDirs;

        return base
                + 10_000 * run
                + 2_000 * potential
                + 200   * openEnds
                + forkBonus
                - easyFourPenalty
                + softPositionalBonus(cell);
    }

    private int countBlockedFourPlusOne(int row, int col, Mark opp) {
        int count = 0;
        for (Line ln : board.getAllLines()) {
            List<Cell> cells = ln.getCells();
            for (int i = 0; i <= cells.size() - 5; i++) {
                int oppCnt = 0, emptyCnt = 0;
                Cell emptyCell = null;

                for (int j = 0; j < 5; j++) {
                    Cell c = cells.get(i + j);
                    if (c.getSymbol() == opp) {
                        oppCnt++;
                    } else if (c.isEmpty()) {
                        emptyCnt++; emptyCell = c;
                    } else {
                        // our stone in the window -> not an opponent 4+1
                        oppCnt = 99; break;
                    }
                }
                if (oppCnt == 4 && emptyCnt == 1 &&
                        emptyCell != null &&
                        emptyCell.getRow() == row && emptyCell.getCol() == col) {
                    count++;
                }
            }
        }
        return count;
    }

    private int softPositionalBonus(Cell m) {
        int N = board.getSize(), cx = N/2, cy = N/2;
        int dist = Math.abs(m.getRow()-cx) + Math.abs(m.getCol()-cy);
        int center = (N*2 - dist);

        int adjOur = 0, adjEmpty = 0;
        for (Cell nb : resolver.getNeighbors(board, m.getRow(), m.getCol())) {
            if (nb.getSymbol() == playerMark) adjOur++;
            else if (nb.isEmpty()) adjEmpty++;
        }
        return 100*center + (4*adjOur + adjEmpty);
    }

    // Fallback: przeglądamy wszystkie puste pola, preferujemy wydłużenie NAJŁAŃCUCHA
    // ale uwzględniamy potencjał (nie wybieramy „zabetonowanych” miejsc).
    private Move fallbackHeuristic() {
        Cell best = null;
        int bestScore = Integer.MIN_VALUE;
        Mark opp = (playerMark == Mark.CROSS) ? Mark.NOUGHT : Mark.CROSS;

        for (int x = 0; x < board.getSize(); x++)
            for (int y = 0; y < board.getSize(); y++) {
                Cell c = board.getCell(x, y);
                if (c == null || !c.isEmpty()) continue;

                int run  = maxRunAfter(x, y, playerMark);
                int pot  = maxPotentialAfter(x, y, playerMark);
                int ends = openEndsAfter(x, y, playerMark);
                int oppRun = maxRunAfter(x, y, opp);
                int forks = countOpenThreeForksAfter(x, y, playerMark);

                int score = 20_000 * run + 5_000 * pot + 200 * ends
                        - 5_000 * oppRun + softPositionalBonus(c)
                        + 300_000 * forks;

                if (run == 4 && ends <= 1 && forks < 2) score -= 200_000;
                if (pot < 5) score -= 50_000;
                if (score > bestScore) { bestScore = score; best = c; }
            }

        return new Move(new Position(best.getCol(), best.getRow()), playerMark);
    }

    private int maxRunAfter(int x,int y,Mark sym) {
        if(!board.getCell(x,y).isEmpty())
            return 0; // symulacja
        // policz max po wstawieniu symbolu
        int[][] dirs={{1,0},{0,1},{1,1},{1,-1}};
        int best=0;
        for(var d:dirs){
            int cnt=1;
            cnt+=countSymbols(x,y,d[0],d[1],sym);
            cnt+=countSymbols(x,y,-d[0],-d[1],sym);
            if(cnt>best)
                best=cnt;
        }
        return best;
    }
    // maksymalna DŁUGOŚĆ linii możliwa w przyszłości (kamienie + puste do pierwszego wroga/krawędzi)
    private int maxPotentialAfter(int x, int y, Mark sym) {
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        int best = 0;
        Mark opp = (playerMark == Mark.CROSS) ? Mark.NOUGHT : Mark.CROSS;
        for (int[] d : dirs) {
            int oursL = countSymbols(x, y, -d[0], -d[1], sym);
            int oursR = countSymbols(x, y,  d[0],  d[1], sym);
            int empL  = countEmpties(x, y, -d[0], -d[1], opp);
            int empR  = countEmpties(x, y,  d[0],  d[1], opp);
            int pot = 1 + oursL + oursR + empL + empR;
            best = Math.max(best, pot);
        }
        return best;
    }
    // liczba „żywych” końców po ruchu (czy bezpośrednie sąsiednie pola końców są puste)
    private int openEndsAfter(int x, int y, Mark sym) {
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        int ends = 0;
        Mark opp = (playerMark == Mark.CROSS) ? Mark.NOUGHT : Mark.CROSS;
        for (int[] d : dirs) {
            int left  = edgeAfter(x, y, -d[0], -d[1], sym);
            int right = edgeAfter(x, y,  d[0],  d[1], sym);
            if (left == 0)  ends++;
            if (right == 0) ends++;
        }
        return Math.min(2, ends);
    }

    // Stones of 'sym' contiguous after (x,y)
    private int countSymbols(int x, int y, int dx, int dy, Mark sym) {
        int c = 0, n = board.getSize();
        for (int step = 1; step < n; step++) {
            Cell cell = board.getCell(x + dx * step, y + dy * step);
            if (cell == null) break;
            if (cell.getSymbol() != sym) break;
            c++;
        }
        return c;
    }

    // Empty cells until the first opponent or our own stone
    private int countEmpties(int x, int y, int dx, int dy, Mark opp) {
        int c = 0, n = board.getSize();
        for (int step = 1; step < n; step++) {
            Cell cell = board.getCell(x + dx * step, y + dy * step);
            if (cell == null) break;                 // non-periodic edge
            if (cell.getSymbol() == opp) break;      // blocked by opponent
            if (!cell.isEmpty()) break;              // our stone blocks potential
            c++;
        }
        return c;
    }

    private int countOpenThreeForksAfter(int row, int col, Mark sym) {
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        int n = board.getSize();
        int dirsOpen3 = 0;

        for (int[] d : dirs) {
            int left  = countSymbolsBounded(row, col, -d[0], -d[1], sym, n);
            int right = countSymbolsBounded(row, col,  d[0],  d[1], sym, n);
            int len   = left + 1 + right;

            // komórki tuż za końcami naszego segmentu
            Cell leftEnd  = board.getCell(row - d[0] * (left  + 1), col - d[1] * (left  + 1));
            Cell rightEnd = board.getCell(row + d[0] * (right + 1), col + d[1] * (right + 1));

            boolean openL = (leftEnd  != null) && leftEnd.isEmpty();
            boolean openR = (rightEnd != null) && rightEnd.isEmpty();

            if (len >= 3 && openL && openR) dirsOpen3++;
        }
        return dirsOpen3;
    }

    // wersja bounded – bez pętli nieskończonych na planszy periodycznej
    private int countSymbolsBounded(int x, int y, int dx, int dy, Mark sym, int n) {
        int c = 0;
        for (int step = 1; step < n; step++) {
            Cell cell = board.getCell(x + dx * step, y + dy * step);
            if (cell == null || cell.getSymbol() != sym) break;
            c++;
        }
        return c;
    }

    // 0 -> open end (next cell empty), 2 -> closed (opponent or wall)
// bounded to avoid looping on torus when the whole ray is ours/empty
    private int edgeAfter(int x, int y, int dx, int dy, Mark sym) {
        int n = board.getSize();
        for (int step = 1; step < n; step++) {
            Cell cell = board.getCell(x + dx * step, y + dy * step);
            if (cell == null) return 2;              // hard edge (standard board)
            if (cell.getSymbol() == sym) continue;   // keep skipping our stones
            return cell.isEmpty() ? 0 : 2;           // empty -> open, else closed
        }
        // we wrapped the whole ring without finding a non-our cell -> treat as closed
        return 2;
    }
}