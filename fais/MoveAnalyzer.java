import fais.zti.oramus.gomoku.*;

public class MoveAnalyzer {
    private static final Direction[] AXES = new Direction[]{Direction.E, Direction.S, Direction.SE, Direction.NE};

    // === OBSERVER SUPPORT ===
    private MoveObserver[] observers = new MoveObserver[0];
    public MoveAnalyzer(){ }
    public MoveAnalyzer(MoveObserver... obs){
        if (obs != null) this.observers = obs;
    }
    private void notifyCandidate(MoveType t, Move m){
        int i;
        for (i = 0; i < observers.length; i++) observers[i].onCandidate(t, m);
    }
    // ========================

    public AnalysisResult analyze(Board board, Mark toMove)
            throws WrongBoardStateException, TheWinnerIsException, ResignException {

        Mark winner = detectWinner(board);
        if (winner != null) throw new TheWinnerIsException(winner);

        Mark mine = toMove;
        Mark opp = (toMove == Mark.CROSS ? Mark.NOUGHT : Mark.CROSS);

        // NEW: skan katalogu wzorców — tylko do raportowania przez obserwatora (bez wpływu na wybór ruchu)
        if (observers != null && observers.length > 0) {
            PatternEngine pe = new PatternEngine();
            int i;
            for (i = 0; i < observers.length; i++) {
                pe.scan(board, mine, opp, observers[i]);
            }
        }

        // 1) Nasza wygrana teraz
        Move[] myWins = winningMoves(board, mine, 1);
        if (myWins[0] != null) {
            notifyCandidate(MoveType.WINNING, myWins[0]);
            return new AnalysisResult(MoveType.WINNING, myWins[0]);
        }

        // 2) Obrona: natychmiastowe wygrane przeciwnika
        Move[] oppWins = winningMoves(board, opp, 3);
        int oppWinCount = countNonNull(oppWins);
        if (oppWinCount >= 2) return AnalysisResult.resign();
        if (oppWinCount == 1) {
            notifyCandidate(MoveType.BLOCKING, oppWins[0]);
            return new AnalysisResult(MoveType.BLOCKING, oppWins[0]);
        }

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
        if (bestO4 != null) {
            notifyCandidate(MoveType.CREATE_OPEN_FOUR, bestO4);
            return new AnalysisResult(MoveType.CREATE_OPEN_FOUR, bestO4);
        }

        // 4) Unified defense: O4 ∪ loose D3 (Twoja aktualna logika – bez zmian funkcjonalnych)
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

                notifyCandidate(MoveType.BLOCKING, bestBlock);
                return new AnalysisResult(MoveType.BLOCKING, bestBlock);
            } else {
                if (initialAll >= 2) throw new ResignException();
            }
        }

        // 5) Luźny double-three → preferujemy atak
        Move looseD3 = bestLooseDoubleThree(board, mine);
        if (looseD3 != null) {
            notifyCandidate(MoveType.CREATE_DOUBLE_THREAT, looseD3);
            return new AnalysisResult(MoveType.CREATE_DOUBLE_THREAT, looseD3);
        }

        // 6) Nasz „twardy” double-three
        if (bestDouble3 != null) {
            notifyCandidate(MoveType.CREATE_DOUBLE_THREAT, bestDouble3);
            return new AnalysisResult(MoveType.CREATE_DOUBLE_THREAT, bestDouble3);
        }

        // 7) Neutralny
        Move neutral = pickNeutral(board, mine);
        notifyCandidate(MoveType.ANY, neutral);
        return new AnalysisResult(MoveType.ANY, neutral);
    }

    private Mark detectWinner(Board b) throws WrongBoardStateException {
        boolean x = hasFive(b, Mark.CROSS);
        boolean o = hasFive(b, Mark.NOUGHT);
        if (x && o) throw new WrongBoardStateException();
        if (x) return Mark.CROSS;
        if (o) return Mark.NOUGHT;
        return null;
    }

    // === Użycie Line (Composite) w 2 miejscach — bez zmiany logiki ===
    private boolean hasFive(Board b, Mark m) {
        int n = b.size();
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (b.get(r, c) == m)
                    for (int i = 0; i < AXES.length; i++)
                        if (new Line(b, r, c, AXES[i], m).length() >= 5) return true;
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
                        if (new Line(b, r, c, AXES[i], who).length() >= 5) { win = true; break; }
                    b.set(r, c, Mark.NULL);
                    if (win) out[found++] = new Move(new Position(c, r), who);
                }
        return out;
    }

    /** Finds a \"looser\" double-3: at least two axes with len>=3,
     *  where at least one of them is an OPEN-3 (open ends == 2).
     *  Semi-open 3 (open==1) is allowed for the second axis.
     *  Fragile edge-4 (len==4 & open==1 & closed side is a border) is ignored.
     *  Returns the best such move or null if none.
     */
    private Move bestLooseDoubleThree(Board b, Mark mine) {
        int n = b.size();
        Move best = null;

        int bestOpen3Axes = -1;  // number of axes that become open-3
        int bestThreatAxes = -1; // axes with len>=3 & open>=1 (excluding fragile edge-4)
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

                // We require at least two threat axes and at least one open-3 among them.
                if (threatAxes >= 2 && open3Axes >= 1) {
                    // tie-break: more open-3 axes, then more threat axes, then closer to center
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

    /** Pick a single blocking move that minimizes the total remaining opponent threats:
     *  O4-creation + loose double-three creation. Evaluates only creation squares (O4 ∪ D3).
     *  Returns null if there are no such threats.
     */
    private Move bestUnifiedThreatBlock(Board b, Mark opp, Mark me) {
        int n = b.size();

        // collect O4 creation squares
        int[] rO4 = new int[n*n], cO4 = new int[n*n];
        int kO4 = collectO4Creation(b, opp, rO4, cO4);

        // collect loose D3 creation squares
        int[] rD3 = new int[n*n], cD3 = new int[n*n];
        int kD3 = collectLooseD3Creation(b, opp, rD3, cD3);

        if (kO4 + kD3 == 0) return null;

        // build candidate set = union of creation squares
        // (use a simple visited grid to avoid duplicates)
        boolean[][] vis = new boolean[n][n];
        int i;
        for (i = 0; i < kO4; i++) vis[rO4[i]][cO4[i]] = true;
        for (i = 0; i < kD3; i++) vis[rD3[i]][cD3[i]] = true;

        // evaluate each candidate by remaining threats; tie-break: fewer remaining O4,
        // then fewer total threats, then closer to center.
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


    /** Count opponent squares that would create a (loose) double-three if they played there:
     *  - at least two axes with len >= 3 and open >= 1 (excluding fragile edge-4),
     *  - at least one of those axes is OPEN-3 (open == 2).
     *  If rs/cs are not null, fills them with the coordinates of such creation squares.
     */
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


    /** Neutral move:
     *  prefer squares adjacent to the largest number of our stones (local support),
     *  then tie-break by closeness to center. Falls back to board center.
     *  This avoids playing weak extensions like "...oooA" near the edge (QA#19).
     */
    private Move pickNeutral(Board b, Mark mine) {
        int n = b.size();
        Move best = null;

        int bestOpen3Axes = -1;   // axes that become OPEN-3 (len>=3 & open==2)
        int bestThreatAxes = -1;  // axes that become len>=3 & open>=1 (excluding fragile edge-4)
        int bestFeasible  = -1;   // on best axis: len+free>=5
        int bestFragile   = 1;    // 0 better (not fragile), 1 fragile edge-4
        int bestOpen      = -1;   // open ends (0..2) on best axis
        int bestLen       = 0;    // run length on best axis
        int bestFree      = -1;   // extra empties beyond ends on best axis
        int bestAdj       = -1;   // 8-neighborhood friends
        int bestCenter    = Integer.MAX_VALUE;

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

                    boolean closedIsBorder = (ends[0] == null || ends[1] == null);
                    int frag = (len == 4 && open == 1 && closedIsBorder) ? 1 : 0;

                    // counts for global “double-3 / multi-3” preference
                    if (len >= 3) {
                        if (open == 2) open3Axes++;
                        // count as a threat axis if it has at least one open end
                        // BUT exclude the fragile edge-4 pattern (easy to shut down).
                        if (open >= 1 && !(len == 4 && open == 1 && closedIsBorder)) {
                            threatAxes++;
                        }
                    }

                    // choose best axis by (feasible, !fragile, open, len, free)
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

                // global comparison
                boolean better = false;
                if (open3Axes > bestOpen3Axes) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes > bestThreatAxes) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas > bestFeasible) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag < bestFragile) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen > bestOpen) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen > bestLen) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree > bestFree) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree == bestFree && adj > bestAdj) better = true;
                else if (open3Axes == bestOpen3Axes && threatAxes == bestThreatAxes && chosenFeas == bestFeasible && chosenFrag == bestFragile && chosenOpen == bestOpen && chosenLen == bestLen && chosenFree == bestFree && adj == bestAdj && center < bestCenter) better = true;

                if (better) {
                    bestOpen3Axes = open3Axes;
                    bestThreatAxes = threatAxes;
                    bestFeasible  = chosenFeas;
                    bestFragile   = chosenFrag;
                    bestOpen      = chosenOpen;
                    bestLen       = chosenLen;
                    bestFree      = chosenFree;
                    bestAdj       = adj;
                    bestCenter    = center;
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

}