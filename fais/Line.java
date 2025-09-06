import fais.zti.oramus.gomoku.Mark;

import java.util.ArrayList;
import java.util.List;

public class Line{
    private List<Cell> cells = new ArrayList<>();

    public void add(Cell c) { cells.add(c); }
    public List<Cell> getCells() { return cells; }

    public int countConsecutive(Mark symbol) {
        int max = 0, count = 0;
        for (Cell c : cells) {
            if (c.getSymbol() == symbol) count++;
            else count = 0;
            max = Math.max(max, count);
        }
        return max;
    }
}
