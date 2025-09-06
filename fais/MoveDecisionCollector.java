import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;
import fais.zti.oramus.gomoku.Position;

import java.util.*;

class MoveDecisionCollector implements GameObserver {
    private Map<MoveType, List<Move>> moveMap = new EnumMap<>(MoveType.class);
    private Board board;
    private Mark playerMark;
    private final GameNeighborhoodResolver resolver;

    public MoveDecisionCollector(Board board, Mark player, GameNeighborhoodResolver resolver) {
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

        // 1) ile REALNIE mamy po ruchu w linii (liczy tylko nasze kamienie)
        int run = maxRunAfter(board, move.position().row(), move.position().col(), playerMark);
        // 2) maksymalna DŁUGOŚĆ linii, jaką można kiedykolwiek uzyskać w tym miejscu,
        //    jeśli wypełnimy wszystkie puste do pierwszego przeciwnika/krawędzi
        int potential = maxPotentialAfter(board, move.position().row(), move.position().col(), playerMark);
        // 3) liczba „żywych” końców po ruchu (0..2)
        int openEnds = openEndsAfter(board, move.position().row(), move.position().col(), playerMark);
        Cell cell = new Cell(move.position().row(), move.position().col());
        // sanity: jeśli kandydat oznaczony jako WINNING nie daje 5 – odrzuć
        if (type == MoveType.WINNING && run < 5) return -1_000_000;

        // Ruchy bez szans na dojście do 5 (zamknięte z obu stron) mocno karzemy:
        if (type != MoveType.WINNING && potential < 5) return -500_000 + softPositionalBonus(cell);

        return base
                + 10_000 * run
                + 2_000 * potential
                + 200   * openEnds
                + softPositionalBonus(cell);
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

                int run  = maxRunAfter(board, x, y, playerMark);
                int pot  = maxPotentialAfter(board, x, y, playerMark);
                int ends = openEndsAfter(board, x, y, playerMark);
                int oppRun = maxRunAfter(board, x, y, opp);

                int score = 20_000 * run + 5_000 * pot + 200 * ends
                        - 5_000 * oppRun + softPositionalBonus(c);

                if (pot < 5) score -= 50_000; // nie ładujemy się w „ślepą uliczkę”

                if (score > bestScore) { bestScore = score; best = c; }
            }

        return new Move(new Position(best.getCol(), best.getRow()), playerMark);
    }

    private int maxRunAfter(Board b,int x,int y,Mark sym) {
        if(!b.getCell(x,y).isEmpty())
            return 0; // symulacja
        // policz max po wstawieniu symbolu
        int[][] dirs={{1,0},{0,1},{1,1},{1,-1}};
        int best=0;
        for(var d:dirs){
            int cnt=1;
            cnt+=count(b,x,y,d[0],d[1],sym);
            cnt+=count(b,x,y,-d[0],-d[1],sym);
            if(cnt>best)
                best=cnt;
        }
        return best;
    }
    // maksymalna DŁUGOŚĆ linii możliwa w przyszłości (kamienie + puste do pierwszego wroga/krawędzi)
    private int maxPotentialAfter(Board b, int x, int y, Mark sym) {
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        int best = 0;
        Mark opp = (playerMark == Mark.CROSS) ? Mark.NOUGHT : Mark.CROSS;
        for (int[] d : dirs) {
            int oursL = countStones(x, y, -d[0], -d[1], sym);
            int oursR = countStones(x, y,  d[0],  d[1], sym);
            int empL  = countEmpties(x, y, -d[0], -d[1], opp);
            int empR  = countEmpties(x, y,  d[0],  d[1], opp);
            int pot = 1 + oursL + oursR + empL + empR;
            best = Math.max(best, pot);
        }
        return best;
    }
    // liczba „żywych” końców po ruchu (czy bezpośrednie sąsiednie pola końców są puste)
    private int openEndsAfter(Board b, int x, int y, Mark sym) {
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
    private int count(Board b,int x,int y,int dx,int dy,Mark sym){
        int c=0;
        while(true){
            x+=dx;
            y+=dy;
            Cell cell=b.getCell(x,y);
            if(cell==null||cell.getSymbol()!=sym)
                break;
            c++;
        }
        return c;
    }
    // pomocnicze liczniki
    private int countStones(int x, int y, int dx, int dy, Mark sym) {
        int c = 0;
        while (true) {
            x += dx; y += dy;
            Cell cell = board.getCell(x, y);
            if (cell == null || cell.getSymbol() != sym) break;
            c++;
        }
        return c;
    }
    private int countEmpties(int x, int y, int dx, int dy, Mark opp) {
        int c = 0;
        while (true) {
            x += dx; y += dy;
            Cell cell = board.getCell(x, y);
            if (cell == null || cell.getSymbol() == opp) break;
            if (!cell.isEmpty()) break; // nasze -> nie liczymy dalej
            c++;
        }
        return c;
    }
    // 0 -> puste zaraz za końcem sekwencji, 1 -> nasze, 2 -> przeciwnik/ściana
    private int edgeAfter(int x, int y, int dx, int dy, Mark sym) {
        while (true) {
            x += dx; y += dy;
            Cell cell = board.getCell(x, y);
            if (cell == null) return 2;
            if (cell.getSymbol() == sym) continue;
            return cell.isEmpty() ? 0 : 2;
        }
    }
}