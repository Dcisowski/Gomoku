public class StandardBoardFactory implements BoardFactory {
    @Override
    public Board create(int n) {
        return new Board(n, false);
    }
}