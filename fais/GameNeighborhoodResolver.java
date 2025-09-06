import java.util.List;

public interface GameNeighborhoodResolver {
    List<Cell> getNeighbors(Board board, int x, int y);
}
