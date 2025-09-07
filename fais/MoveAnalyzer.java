import fais.zti.oramus.gomoku.*;

public class MoveAnalyzer {
    private static final Direction[] AXES = new Direction[]{Direction.E, Direction.S, Direction.SE, Direction.NE};

    public AnalysisResult analyze(Board board, Mark toMove)
            throws WrongBoardStateException, TheWinnerIsException {
        validateBoard(board);
        Mark winner = detectWinner(board);
        if (winner != null) throw new TheWinnerIsException(winner);

        Mark mine = toMove;
        Mark opp = (toMove == Mark.CROSS ? Mark.NOUGHT : Mark.CROSS);

        // 1) Nasza wygrana teraz
        Move[] myWins = winningMoves(board, mine, 1);
        if (myWins[0] != null) return new AnalysisResult(MoveType.WINNING, myWins[0]);

        // 2) Obrona: natychmiastowe wygrane przeciwnika
        Move[] oppWins = winningMoves(board, opp, 3);
        int oppWinCount = countNonNull(oppWins);
        if (oppWinCount >= 2) return AnalysisResult.resign();
        if (oppWinCount == 1) return new AnalysisResult(MoveType.BLOCKING, oppWins[0]);

        // 3) Atak: NASZA otwarta czwórka (wygrywa szybciej niż ich O4)
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
        if (bestO4 != null) return new AnalysisResult(MoveType.CREATE_OPEN_FOUR, bestO4);

        // 4) Obrona: ich tworzenie O4 — próbujemy pojedynczym blokiem zneutralizować wszystkie
        Move o4Block = blockOpenFourIfPossible(board, opp, mine);
        if (o4Block != null) return new AnalysisResult(MoveType.BLOCKING, o4Block);
        // jeśli nadal istnieje wiele niezależnych tworzeń O4, po rozważeniu naszego O4 powyżej,
        // nie ma realnej obrony → rezygnacja
        if (collectO4Creation(board, opp, null, null) >= 2) return AnalysisResult.resign();

        // 5) Nasza podwójna trójka (wolniejsza niż O4)
        if (bestDouble3 != null) return new AnalysisResult(MoveType.CREATE_DOUBLE_THREAT, bestDouble3);

        // 6) Neutralny
        Move neutral = pickNeutral(board, mine);
        return new AnalysisResult(MoveType.ANY, neutral);
    }

    private void validateBoard(Board b) throws WrongBoardStateException {
//        int xs = b.count(Mark.CROSS); int os = b.count(Mark.NOUGHT);
//        int diff = xs - os; if (diff < 0) diff = -diff; if (diff > 1) throw new WrongBoardStateException();
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
        int r, c, i;
        for (r = 0; r < n; r++)
            for (c = 0; c < n; c++)
                if (b.get(r, c) == m) {
                    for (i = 0; i < AXES.length; i++) if (b.runLengthThrough(r, c, AXES[i], m) >= 5) return true;
                }
        return false;
    }

    private Move[] winningMoves(Board b, Mark who, int limit) {
        int n = b.size();
        Move[] out = new Move[limit];
        int found = 0;
        int r, c, i;
        for (r = 0; r < n && found < limit; r++)
            for (c = 0; c < n && found < limit; c++)
                if (b.isEmpty(r, c)) {
                    b.set(r, c, who);
                    boolean win = false;
                    for (i = 0; i < AXES.length; i++)
                        if (b.runLengthThrough(r, c, AXES[i], who) >= 5) {
                            win = true;
                            break;
                        }
                    b.set(r, c, Mark.NULL);
                    if (win) {
                        out[found] = new Move(new Position(c, r), who);
                        found++;
                    }
                }
        return out;
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

    // === O4 defense helpers ===
    // Zbiera pola, na których przeciwnik po zagraniu będzie miał >=2 natychmiastowe wygrane.
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



    // Próbujemy pojedynczym ruchem (naszym) wyzerować wszystkie ich możliwości utworzenia O4.
    private Move blockOpenFourIfPossible(Board b, Mark opp, Mark me) {
        int n = b.size();
        int[] rr = new int[n * n], cc = new int[n * n];
        int k = collectO4Creation(b, opp, rr, cc);
        if (k == 0) return null;
        if (k == 1) return new Move(new Position(cc[0], rr[0]), me);
        for (int i = 0; i < k; i++) {
            int br = rr[i], bc = cc[i];
            b.set(br, bc, me);
            int remain = collectO4Creation(b, opp, null, null);
            b.set(br, bc, Mark.NULL);
            if (remain == 0) {
                return new Move(new Position(bc, br), me);
            }
        }
        return null;
    }

    /** Neutral move:
     *  prefer squares adjacent to the largest number of our stones (local support),
     *  then tie-break by closeness to center. Falls back to board center.
     *  This avoids playing weak extensions like "...oooA" near the edge (QA#19).
     */
    private Move pickNeutral(Board b, Mark mine) {
        int n = b.size();
        Move best = null;

        int bestFeasible = -1;      // 1 if some axis can still reach 5
        int bestFragile = 1;        // 0 is better (not fragile), 1 is fragile edge-four
        int bestOpen = -1;          // open ends (0..2)
        int bestLen = 0;            // longest run created
        int bestFree = -1;          // extra empties beyond the two ends
        int bestAdj = -1;           // friendly neighbors (8-neighborhood)
        int bestCenter = Integer.MAX_VALUE;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (!b.isEmpty(r, c)) continue;

                b.set(r, c, mine);

                int chosenFeas = 0;
                int chosenOpen = 0;
                int chosenLen  = 1;
                int chosenFree = 0;
                int chosenFrag = 1; // assume fragile until proven otherwise

                // pick the best axis for this square:
                for (int i = 0; i < AXES.length; i++) {
                    Direction d = AXES[i];

                    int len = b.runLengthThrough(r, c, d, mine);
                    Point[] ends = b.endsOfRun(r, c, d, mine);
                    int open = 0;
                    boolean endAEmpty = (ends[0] != null && b.isEmpty(ends[0].r, ends[0].c));
                    boolean endBEmpty = (ends[1] != null && b.isEmpty(ends[1].r, ends[1].c));
                    if (endAEmpty) open++;
                    if (endBEmpty) open++;

                    // count empties continuing beyond each empty end
                    int free = 0;
                    free += countFreeInclusive(b, ends[0], Direction.opposite(d));
                    free += countFreeInclusive(b, ends[1], d);

                    int feas = (len + free >= 5) ? 1 : 0;

                    // fragile edge-four: len==4, open==1, and the closed side is off-board
                    boolean closedIsBorder = (ends[0] == null || ends[1] == null);
                    int frag = (len == 4 && open == 1 && closedIsBorder) ? 1 : 0;

                    // choose axis by (feas, !frag, open, len, free)
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

                b.set(r, c, Mark.NULL);

                // global choice by (feasible, !fragile, open, len, free, adj, -center)
                boolean better = false;
                if (chosenFeas > bestFeasible) better = true;
                else if (chosenFeas == bestFeasible && chosenFrag < bestFragile) better = true;
                else if (chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen > bestOpen) better = true;
                else if (chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen > bestLen) better = true;
                else if (chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree > bestFree) better = true;
                else if (chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree == bestFree && adj > bestAdj) better = true;
                else if (chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree == bestFree && adj == bestAdj && center < bestCenter) better = true;

                if (better) {
                    bestFeasible = chosenFeas;
                    bestFragile = chosenFrag;
                    bestOpen = chosenOpen;
                    bestLen = chosenLen;
                    bestFree = chosenFree;
                    bestAdj = adj;
                    bestCenter = center;
                    best = new Move(new Position(c, r), mine);
                }
            }
        }
        if (best != null) return best;
        return new Move(new Position(n / 2, n / 2), mine);
    }

    /** Counts contiguous empty cells starting at 'start' (inclusive) going in 'd'.
     *  Capped to 4 steps to avoid cycles on torus; enough because we only need up to 4
     *  additional cells to reach 5-in-a-row.
     */
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

    /** Counts how many of our stones are in the 8-neighborhood of (r,c). */
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

    private boolean hasNeighbor(Board b, int r, int c) {
        Direction[] dirs = Direction.values();
        int i;
        for (i = 0; i < dirs.length; i++) {
            Point p = b.next(r, c, dirs[i]);
            if (p == null) continue;
            if (!b.isEmpty(p.r, p.c)) return true;
        }
        return false;
    }
}