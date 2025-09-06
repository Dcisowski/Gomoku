import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;

import java.util.*;

class MoveDecisionCollector implements GameObserver {
    private Map<MoveType, List<Move>> moveMap = new EnumMap<>(MoveType.class);
    private Board board;
    private Mark playerMark;

    public MoveDecisionCollector(Board board, Mark playerSymbol) {
        for (MoveType type : MoveType.values()) {
            moveMap.put(type, new ArrayList<>());
        }
        this.board = board;
        this.playerMark = playerSymbol;
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
        return null;
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
        if (board == null) return 0;
        int N = board.getSize();
        int cx = N/2, cy = N/2;
        int dist = Math.abs(move.position().row()-cx) + Math.abs(move.position().col()-cy);
        int centerScore = (N*2 - dist); // im bliżej środka, tym lepiej

        // lokalne wsparcie: sąsiedzi 8-kierunkowi
        int adjOur = 0, adjEmpty = 0;
        Mark opp = (playerMark==Mark.CROSS) ? Mark.NOUGHT:Mark.CROSS;
//        for (int dx=-1; dx<=1; dx++) for (int dy=-1; dy<=1; dy++) {
//            if (dx==0 && dy==0) continue;
//            Cell nb = board.getCell(move.position().row()+dx, move.position().col()+dy);
//            if (nb==null) continue;
//            if (nb.getSymbol()==playerMark) adjOur++;
//            else if (nb.isEmpty()) adjEmpty++;
//        }
//        int localScore = 3*adjOur + adjEmpty;

        int base; // waga kategorii
        switch (type) {
            case WINNING: base = 1_000_000; break;
            case BLOCKING: base = 900_000; break;           // zawsze nad DOUBLE_THREAT
            case DOUBLE_THREAT: base = 800_000; break;      // priorytet wyższy niż OPEN_FOUR
            case OPEN_FOUR: default: base = 700_000; break;
        }
        int maxRun = maxRunAfter(board, move.position().row(), move.position().col(), playerMark);
        if(type==MoveType.WINNING && maxRun<5)
            base = 10; // sanity – jeżeli "WINNING" nie daje 5, odrzucamy

        for(int dx=-1;dx<=1;dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                Cell nb = board.getCell(move.position().row() + dx, move.position().col() + dy);
                if (nb == null)
                    continue;
                if (nb.getSymbol() == playerMark)
                    adjOur++;
                else if (nb.isEmpty())
                    adjEmpty++;
            }
        }
        int localScore = 4*adjOur + adjEmpty;

        return base + 10_000*maxRun + 100*centerScore + localScore;
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
}