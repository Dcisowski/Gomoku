import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;
import fais.zti.oramus.gomoku.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class Board {
    protected final int size;
    protected final Cell[][] grid;
    protected final boolean periodic;

    public Board(int size) { this(size, false); }

    protected Board(int size, boolean periodic) {
        this.size = size;
        this.periodic = periodic;
        this.grid = new Cell[size][size];
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                grid[x][y] = new Cell(x, y);
    }

    public int getSize() { return size; }
    public boolean isPeriodic() { return periodic; }

    // standard: poza planszą -> null
    public Cell getCell(int x, int y) {
        if (x < 0 || y < 0 || x >= size || y >= size) return null;
        return grid[x][y];
    }

    // linie bez zawijania (tak jak dotąd)
    public List<Line> getAllLines() {
        List<Line> lines = new ArrayList<>();

        // „wiersze i kolumny” w naszej konwencji indeksów
        for (int i = 0; i < size; i++) {
            Line row = new Line();
            Line col = new Line();
            for (int j = 0; j < size; j++) {
                row.add(grid[i][j]);
                col.add(grid[j][i]);
            }
            lines.add(row);
            lines.add(col);
        }

        // diagonale bez zawijania
        for (int k = 0; k <= 2 * (size - 1); k++) {
            Line d1 = new Line();
            Line d2 = new Line();
            for (int i = 0; i < size; i++) {
                int j = k - i;
                if (j >= 0 && j < size) d1.add(grid[i][j]);
                int jj = size - 1 - k + i;
                if (jj >= 0 && jj < size) d2.add(grid[i][jj]);
            }
            if (d1.getCells().size() >= 5) lines.add(d1);
            if (d2.getCells().size() >= 5) lines.add(d2);
        }
        return lines;
    }
}