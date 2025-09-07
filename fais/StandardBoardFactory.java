public class StandardBoardFactory implements BoardFactory {
    @Override
    public Board create(int size) {
        return new Board(size, false);
    }
}