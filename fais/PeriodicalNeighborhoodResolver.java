import java.util.ArrayList;
import java.util.List;

class PeriodicalNeighborhoodResolver implements GameNeighborhoodResolver {
    public List<Cell> getNeighbors(Board board, int x, int y) {
        List<Cell> result = new ArrayList<>();
        int size = board.getSize();
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = (x + dx + size) % size;
                int ny = (y + dy + size) % size;
                result.add(board.getCell(nx, ny));
            }
        return result;
    }
}