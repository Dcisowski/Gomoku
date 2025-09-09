import fais.zti.oramus.gomoku.*;

public class MoveAnalyzer {
    private static final Direction[] AXES = new Direction[]{Direction.E, Direction.S, Direction.SE, Direction.NE};

    private MoveObserver[] observers = new MoveObserver[0];
    public MoveAnalyzer(MoveObserver... obs){
        if (obs != null) this.observers = obs;
    }
    private void notifyCandidate(MoveType t, Move m){
        int i;
        for (i = 0; i < observers.length; i++) observers[i].onCandidate(t, m);
    }

    public void analyze(Board board, Mark toMove)
            throws WrongBoardStateException, TheWinnerIsException, ResignException {

        Mark winner = detectWinner(board);
        if (winner != null) throw new TheWinnerIsException(winner);

        Mark mine = toMove;
        Mark opp = (toMove == Mark.CROSS ? Mark.NOUGHT : Mark.CROSS);

        if (observers != null && observers.length > 0) {
            PatternEngine pe = new PatternEngine();
            int i;
            for (i = 0; i < observers.length; i++) {
                pe.scan(board, mine, opp, observers[i]);
            }
        }

        // 1) Wygrana teraz
        Move[] myWins = winningMoves(board, mine, 1);
        if (myWins[0] != null) {
            notifyCandidate(MoveType.WINNING, myWins[0]);
        }

        // 2) Obrona: natychmiastowe wygrane przeciwnika
        Move[] oppWins = winningMoves(board, opp, 3);
        int oppWinCount = countNonNull(oppWins);
        if (oppWinCount >= 2) return ;
        if (oppWinCount == 1) {
            Move move = new Move(oppWins[0].position(), toMove);
            notifyCandidate(MoveType.BLOCKING_WINNING, move);
        }

        // 3) Atak: Otwarta czwórka gracza (wygrywa szybciej niż otwarta 4 przeciwnika)
        Move bestO4 = null;
        Move bestDouble3 = null;
        int n = board.size();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (!board.isEmpty(r, c)) continue;
                board.set(r, c, mine);
                int nextWins = countNonNull(winningMoves(board, mine, 3));
                if (nextWins >= 2) {
                    Move m = new Move(new Position(c, r), mine);
                    bestO4 = TieBreaker.better(board, bestO4, m);
                } else {
                    int d3 = countDisjointOpenThrees(board, mine);
                    if (d3 >= 2) {
                        Move m2 = new Move(new Position(c, r), mine);
                        bestDouble3 = TieBreaker.better(board, bestDouble3, m2);
                    }
                }
                board.set(r, c, Mark.NULL);
            }
        }
        if (bestO4 != null) {
            notifyCandidate(MoveType.CREATE_OPEN_FOUR, bestO4);
        }

        // 4) Obrana przed otwartą czwórka i trojka lub atak swoją otwartą trójką
        int initialO4 = collectO4Creation(board, opp, null, null);
        int initialD3 = collectLooseD3Creation(board, opp, null, null);
        int initialAll = initialO4 + initialD3;

        if (initialAll > 0) {
            Move bestBlock = bestUnifiedThreatBlock(board, opp, mine);
            if (bestBlock != null) {
                board.set(bestBlock.position().row(), bestBlock.position().col(), mine);
                int remainO4 = collectO4Creation(board, opp, null, null);
                int remainD3 = collectLooseD3Creation(board, opp, null, null);
                board.set(bestBlock.position().row(), bestBlock.position().col(), Mark.NULL);

                int remainAll = remainO4 + remainD3;

                if (initialAll >= 2 && remainAll >= 1) throw new ResignException();
                if (bestO4 == null) notifyCandidate(MoveType.BLOCKING, bestBlock);
           } else {
                if (initialAll >= 2) throw new ResignException();
            }
        }

        // 5) Luzny double-three → preferowany atak
        Move looseD3 = bestLooseDoubleThree(board, mine);
        if (looseD3 != null) {
            notifyCandidate(MoveType.CREATE_BEST_DOUBLE_THREAT, looseD3);
        }

        // 6) Mocny double-three
        if (bestDouble3 != null) {
            notifyCandidate(MoveType.CREATE_DOUBLE_THREAT, bestDouble3);
        }

        // 7) Neutralny
        Move neutral = pickNeutral(board, mine);
        notifyCandidate(MoveType.ANY, neutral);
    }

    private Mark detectWinner(Board b) throws WrongBoardStateException {
        boolean x = hasFive(b, Mark.CROSS);
        boolean o = hasFive(b, Mark.NOUGHT);
        if (x && o) throw new WrongBoardStateException();
        if (x) return Mark.CROSS;
        if (o) return Mark.NOUGHT;
        return null;
    }

    private boolean hasFive(Board b, Mark m) {
        int n = b.size();
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (b.get(r, c) == m)
                    for (int i = 0; i < AXES.length; i++)
                        if (b.runLengthThrough(r, c, AXES[i], m) >= 5) return true;
        return false;
    }

    private Move[] winningMoves(Board b, Mark who, int limit) {
        int n = b.size();
        Move[] out = new Move[limit];
        int found = 0;
        for (int r = 0; r < n && found < limit; r++)
            for (int c = 0; c < n && found < limit; c++)
                if (b.isEmpty(r, c)) {
                    b.set(r, c, who);
                    boolean win = false;
                    for (int i = 0; i < AXES.length; i++)
                        if (b.runLengthThrough(r, c, AXES[i], who) >= 5) { win = true; break; }
                    b.set(r, c, Mark.NULL);
                    if (win) out[found++] = new Move(new Position(c, r), who);
                }
        return out;
    }

    private Move bestLooseDoubleThree(Board b, Mark mine) {
        int n = b.size();
        Move best = null;

        int bestOpen3Axes = -1;
        int bestThreatAxes = -1;
        int bestCenter = Integer.MAX_VALUE;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (!b.isEmpty(r, c)) continue;

                b.set(r, c, mine);

                int open3Axes = 0;
                int threatAxes = 0;

                for (int i = 0; i < AXES.length; i++) {
                    Direction d = AXES[i];

                    int len = b.runLengthThrough(r, c, d, mine);
                    Point[] ends = b.endsOfRun(r, c, d, mine);

                    boolean eA = (ends[0] != null && b.isEmpty(ends[0].r, ends[0].c));
                    boolean eB = (ends[1] != null && b.isEmpty(ends[1].r, ends[1].c));
                    int open = (eA ? 1 : 0) + (eB ? 1 : 0);

                    boolean closedIsBorder = (ends[0] == null || ends[1] == null);
                    boolean fragileEdge4 = (len == 4 && open == 1 && closedIsBorder);

                    if (len >= 3) {
                        if (open == 2) open3Axes++;
                        if (open >= 1 && !fragileEdge4) threatAxes++;
                    }
                }

                b.set(r, c, Mark.NULL);


                if (threatAxes >= 2 && open3Axes >= 1) {

                    int center = Math.abs(r - (n / 2)) + Math.abs(c - (n / 2));
                    if (open3Axes > bestOpen3Axes
                            || (open3Axes == bestOpen3Axes && (threatAxes > bestThreatAxes
                            || (threatAxes == bestThreatAxes && center < bestCenter)))) {
                        bestOpen3Axes = open3Axes;
                        bestThreatAxes = threatAxes;
                        bestCenter = center;
                        best = new Move(new Position(c, r), mine);
                    }
                }
            }
        }
        return best;
    }

    private Move bestUnifiedThreatBlock(Board b, Mark opp, Mark me) {
        int n = b.size();

        int[] rO4 = new int[n*n], cO4 = new int[n*n];
        int kO4 = collectO4Creation(b, opp, rO4, cO4);

        int[] rD3 = new int[n*n], cD3 = new int[n*n];
        int kD3 = collectLooseD3Creation(b, opp, rD3, cD3);

        if (kO4 + kD3 == 0) return null;

        boolean[][] vis = new boolean[n][n];
        int i;
        for (i = 0; i < kO4; i++) vis[rO4[i]][cO4[i]] = true;
        for (i = 0; i < kD3; i++) vis[rD3[i]][cD3[i]] = true;

        int bestRemainO4 = Integer.MAX_VALUE;
        int bestRemainAll = Integer.MAX_VALUE;
        int bestCenter = Integer.MAX_VALUE;
        Move best = null;

        int r, c;
        for (r = 0; r < n; r++) {
            for (c = 0; c < n; c++) {
                if (!vis[r][c]) continue;

                b.set(r, c, me);
                int remO4  = collectO4Creation(b, opp, null, null);
                int remD3  = collectLooseD3Creation(b, opp, null, null);
                int remAll = remO4 + remD3;
                int center = Math.abs(r - (n / 2)) + Math.abs(c - (n / 2));
                b.set(r, c, Mark.NULL);

                boolean better = false;
                if (remO4 < bestRemainO4) better = true;
                else if (remO4 == bestRemainO4 && remAll < bestRemainAll) better = true;
                else if (remO4 == bestRemainO4 && remAll == bestRemainAll && center < bestCenter) better = true;

                if (better) {
                    bestRemainO4 = remO4;
                    bestRemainAll = remAll;
                    bestCenter = center;
                    best = new Move(new Position(c, r), me);
                }
            }
        }
        return best;
    }


    private int countNonNull(Move[] arr) {
        int k = 0, i;
        for (i = 0; i < arr.length; i++) if (arr[i] != null) k++;
        return k;
    }

    private int countDisjointOpenThrees(Board b, Mark mine) {
        int n = b.size();
        int r, c, ai;
        int count = 0;
        Point a1 = null, a2 = null;
        for (r = 0; r < n; r++)
            for (c = 0; c < n; c++)
                if (b.get(r, c) == mine) {
                    for (ai = 0; ai < AXES.length; ai++) {
                        Direction d = AXES[ai];
                        int len = b.runLengthThrough(r, c, d, mine);
                        if (len == 3) {
                            Point[] ends = b.endsOfRun(r, c, d, mine);
                            Point e1 = ends[0], e2 = ends[1];
                            boolean ok1 = e1 != null && b.isEmpty(e1.r, e1.c);
                            boolean ok2 = e2 != null && b.isEmpty(e2.r, e2.c);
                            if (ok1 && ok2) {
                                if (count == 0) {
                                    a1 = e1;
                                    a2 = e2;
                                    count++;
                                } else {
                                    if (!same(e1, a1) && !same(e1, a2) && !same(e2, a1) && !same(e2, a2)) {
                                        count++;
                                        return count;
                                    }
                                }
                            }
                        }
                    }
                }
        return count;
    }

    private boolean same(Point p, Point q) {
        if (p == null || q == null) return false;
        return p.r == q.r && p.c == q.c;
    }

    private int collectO4Creation(Board b, Mark opp, int[] rs, int[] cs) {
        int n = b.size(), cnt = 0;
        int r, c;
        for (r = 0; r < n; r++)
            for (c = 0; c < n; c++)
                if (b.isEmpty(r, c)) {
                    b.set(r, c, opp);
                    int wins = countNonNull(winningMoves(b, opp, 3));
                    b.set(r, c, Mark.NULL);
                    if (wins >= 2) {
                        if (rs != null) {
                            rs[cnt] = r;
                            cs[cnt] = c;
                        }
                        cnt++;
                    }
                }
        return cnt;
    }

    private int collectLooseD3Creation(Board b, Mark opp, int[] rs, int[] cs) {
        int n = b.size();
        int cnt = 0;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (!b.isEmpty(r, c)) continue;

                b.set(r, c, opp);

                int open3Axes = 0;
                int threatAxes = 0;

                for (int i = 0; i < AXES.length; i++) {
                    Direction d = AXES[i];

                    int len = b.runLengthThrough(r, c, d, opp);
                    Point[] ends = b.endsOfRun(r, c, d, opp);

                    boolean eA = (ends[0] != null && b.isEmpty(ends[0].r, ends[0].c));
                    boolean eB = (ends[1] != null && b.isEmpty(ends[1].r, ends[1].c));
                    int open = (eA ? 1 : 0) + (eB ? 1 : 0);

                    boolean closedIsBorder = (ends[0] == null || ends[1] == null);
                    boolean fragileEdge4 = (len == 4 && open == 1 && closedIsBorder);

                    if (len >= 3) {
                        if (open == 2) open3Axes++;
                        if (open >= 1 && !fragileEdge4) threatAxes++;
                    }
                }

                b.set(r, c, Mark.NULL);

                if (threatAxes >= 2 && open3Axes >= 1) {
                    if (rs != null) { rs[cnt] = r; cs[cnt] = c; }
                    cnt++;
                }
            }
        }
        return cnt;
    }

    private Move pickNeutral(Board b, Mark mine) {
        int n = b.size();
        Move best = null;

        int bestOpen3Axes = -1;
        int bestThreatAxes = -1;
        int bestFeasible  = -1;
        int bestFragile   = 1;
        int bestOpen      = -1;
        int bestLen       = 0;
        int bestFree      = -1;
        int bestSurviveThreat = -1;
        int bestSurviveOpen3  = -1;
        int bestAdj       = -1;
        int bestCenter    = Integer.MAX_VALUE;

        Mark opp = (mine == Mark.CROSS ? Mark.NOUGHT : Mark.CROSS);

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (!b.isEmpty(r, c)) continue;

                b.set(r, c, mine);

                int open3Axes = 0;
                int threatAxes = 0;

                int chosenFeas = 0;
                int chosenFrag = 1;
                int chosenOpen = 0;
                int chosenLen  = 1;
                int chosenFree = 0;

                int[] br = new int[8], bc = new int[8]; int bk = 0;

                for (int i = 0; i < AXES.length; i++) {
                    Direction d = AXES[i];

                    int len = b.runLengthThrough(r, c, d, mine);
                    Point[] ends = b.endsOfRun(r, c, d, mine);

                    boolean endAEmpty = (ends[0] != null && b.isEmpty(ends[0].r, ends[0].c));
                    boolean endBEmpty = (ends[1] != null && b.isEmpty(ends[1].r, ends[1].c));
                    int open = (endAEmpty ? 1 : 0) + (endBEmpty ? 1 : 0);

                    int free = 0;
                    free += countFreeInclusive(b, ends[0], Direction.opposite(d));
                    free += countFreeInclusive(b, ends[1], d);

                    int feas = (len + free >= 5) ? 1 : 0;

                    int frag = (len == 4 && open == 1) ? 1 : 0;

                    if (len >= 3) {
                        if (open == 2) open3Axes++;
                        if (open >= 1 && !(len == 4 && open == 1)) {
                            threatAxes++;
                        }
                    }

                    if (endAEmpty) {
                        boolean seen = false;
                        for (int t = 0; t < bk; t++) if (br[t] == ends[0].r && bc[t] == ends[0].c) { seen = true; break; }
                        if (!seen && bk < br.length) { br[bk] = ends[0].r; bc[bk] = ends[0].c; bk++; }
                    }
                    if (endBEmpty) {
                        boolean seen = false;
                        for (int t = 0; t < bk; t++) if (br[t] == ends[1].r && bc[t] == ends[1].c) { seen = true; break; }
                        if (!seen && bk < br.length) { br[bk] = ends[1].r; bc[bk] = ends[1].c; bk++; }
                    }

                    if (feas > chosenFeas
                            || (feas == chosenFeas && (frag < chosenFrag
                            || (frag == chosenFrag && (open > chosenOpen
                            || (open == chosenOpen && (len > chosenLen
                            || (len == chosenLen && free > chosenFree)))))))) {
                        chosenFeas = feas;
                        chosenFrag = frag;
                        chosenOpen = open;
                        chosenLen  = len;
                        chosenFree = free;
                    }
                }

                int adj = countAdjacentMine(b, r, c, mine);
                int center = Math.abs(r - (n / 2)) + Math.abs(c - (n / 2));

                int surviveThreat = threatAxes;
                int surviveOpen3  = open3Axes;
                if (bk > 0) {
                    int minTh = Integer.MAX_VALUE, minO3 = Integer.MAX_VALUE;
                    for (int t = 0; t < bk; t++) {
                        b.set(br[t], bc[t], opp);

                        int th2 = 0, o32 = 0;
                        for (int i = 0; i < AXES.length; i++) {
                            Direction d = AXES[i];
                            int len = b.runLengthThrough(r, c, d, mine);
                            Point[] ends = b.endsOfRun(r, c, d, mine);
                            boolean eA = (ends[0] != null && b.isEmpty(ends[0].r, ends[0].c));
                            boolean eB = (ends[1] != null && b.isEmpty(ends[1].r, ends[1].c));
                            int open = (eA ? 1 : 0) + (eB ? 1 : 0);
                            if (len >= 3) {
                                if (open == 2) o32++;
                                if (open >= 1 && !(len == 4 && open == 1)) th2++;
                            }
                        }

                        if (th2 < minTh || (th2 == minTh && o32 < minO3)) {
                            minTh = th2; minO3 = o32;
                        }
                        b.set(br[t], bc[t], Mark.NULL);
                    }
                    surviveThreat = minTh;
                    surviveOpen3  = minO3;
                }

                b.set(r, c, Mark.NULL);

                boolean better = false;
                if (open3Axes > bestOpen3Axes) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes > bestThreatAxes) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas > bestFeasible) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag < bestFragile) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen > bestOpen) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen > bestLen) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree > bestFree) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree == bestFree
                        && (surviveThreat > bestSurviveThreat
                        || (surviveThreat == bestSurviveThreat && surviveOpen3 > bestSurviveOpen3))) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree == bestFree
                        && surviveThreat == bestSurviveThreat && surviveOpen3 == bestSurviveOpen3 && adj > bestAdj) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree == bestFree
                        && surviveThreat == bestSurviveThreat && surviveOpen3 == bestSurviveOpen3 && adj == bestAdj && center < bestCenter) better = true;

                if (better) {
                    bestOpen3Axes = open3Axes;
                    bestThreatAxes = threatAxes;
                    bestFeasible  = chosenFeas;
                    bestFragile   = chosenFrag;
                    bestOpen      = chosenOpen;
                    bestLen       = chosenLen;
                    bestFree      = chosenFree;
                    bestSurviveThreat = surviveThreat;
                    bestSurviveOpen3  = surviveOpen3;
                    bestAdj       = adj;
                    bestCenter    = center;
                    best = new Move(new Position(c, r), mine);
                }
            }
        }
        if (best != null) return best;
        return new Move(new Position(n / 2, n / 2), mine);
    }

    private int countFreeInclusive(Board b, Point start, Direction d) {
        if (start == null) return 0;
        int n = b.size();
        int cnt = 0;
        Point p = start;
        for (int steps = 0; steps < 4 && p != null; steps++) {
            if (!b.isEmpty(p.r, p.c)) break;
            cnt++;
            p = b.next(p.r, p.c, d);
            if (p != null && p.r == start.r && p.c == start.c) break; // safety on torus
        }
        return cnt;
    }

    private int countAdjacentMine(Board b, int r, int c, Mark mine) {
        Direction[] dirs = Direction.values();
        int cnt = 0;
        for (int i = 0; i < dirs.length; i++) {
            Point p = b.next(r, c, dirs[i]);
            if (p == null) continue;
            if (b.get(p.r, p.c) == mine) cnt++;
        }
        return cnt;
    }

}