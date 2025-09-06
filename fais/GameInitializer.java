class GameInitializer {
    public static Board initialize(int size, boolean periodic) {
        BoardFactory factory = periodic ? new PeriodicBoardFactory() : new StandardBoardFactory();
        return factory.createBoard(size);
    }
}
