class PeriodicBoardFactory implements BoardFactory {
    public Board createBoard(int size) {
        return new Board(size);
    }
    public GameNeighborhoodResolver createResolver() {
        return new PeriodicalNeighborhoodResolver();
    }
}