public class PeriodicBoardFactory implements BoardFactory {
    @Override
    public Board create(int n) {
        return new Board(n, true);
    }
}