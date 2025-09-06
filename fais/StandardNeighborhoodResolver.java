import java.util.ArrayList;
import java.util.List;

class StandardNeighborhoodResolver implements GameNeighborhoodResolver {
    public List<Cell> getNeighbors(Board board, int x, int y) {
        List<Cell> result = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                Cell c = board.getCell(x + dx, y + dy);
                if (c != null) result.add(c);
            }
        return result;
    }
}