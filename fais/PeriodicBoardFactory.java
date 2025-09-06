class PeriodicBoardFactory implements BoardFactory {
    public Board createBoard(int size) {

        return new PeriodicBoard(size);
    }
    public GameNeighborhoodResolver createResolver() {

        return new PeriodicalNeighborhoodResolver();
    }
}