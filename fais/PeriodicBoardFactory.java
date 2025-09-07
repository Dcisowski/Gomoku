public class PeriodicBoardFactory implements BoardFactory {
    @Override
    public Board create(int size) {
        return new Board(size, true);
    }
}