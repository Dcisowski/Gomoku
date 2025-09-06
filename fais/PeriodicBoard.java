import java.util.ArrayList;
import java.util.List;

// === PeriodicBoard – wrap w getCell + linie z „dublowanym” prefiksem
class PeriodicBoard extends Board {

    public PeriodicBoard(int size) { super(size, true); }

    // zawijanie współrzędnych (modulo)
    @Override
    public Cell getCell(int x, int y) {
        int nx = ((x % size) + size) % size;
        int ny = ((y % size) + size) % size;
        return grid[nx][ny];
    }

    // linie periodyczne: każda linia ma dołączony prefiks pierwszych 6 pól,
    // żeby okna 5/6/7 „przechodziły” przez brzeg
    @Override
    public List<Line> getAllLines() {
        List<Line> lines = new ArrayList<>();

        // wiersze i kolumny (długość size + 6)
        for (int i = 0; i < size; i++) {
            Line row = new Line();
            for (int j = 0; j < size; j++) row.add(grid[i][j]);
            for (int j = 0; j < 6; j++) row.add(grid[i][j]);         // prefiks
            lines.add(row);

            Line col = new Line();
            for (int j = 0; j < size; j++) col.add(grid[j][i]);
            for (int j = 0; j < 6; j++) col.add(grid[j][i]);         // prefiks
            lines.add(col);
        }

        // diagonale (+1,+1) – N linii startujących w (0,s)
        for (int s = 0; s < size; s++) {
            Line d = new Line();
            int x = 0, y = s;
            for (int t = 0; t < size; t++) { d.add(grid[x][y]); x = (x + 1) % size; y = (y + 1) % size; }
            for (int t = 0; t < 6; t++) { d.add(d.getCells().get(t)); }
            lines.add(d);
        }
        // diagonale (+1,-1) – N linii startujących w (0,s)
        for (int s = 0; s < size; s++) {
            Line d = new Line();
            int x = 0, y = s;
            for (int t = 0; t < size; t++) { d.add(grid[x][y]); x = (x + 1) % size; y = (y - 1 + size) % size; }
            for (int t = 0; t < 6; t++) { d.add(d.getCells().get(t)); }
            lines.add(d);
        }
        return lines;
    }
}