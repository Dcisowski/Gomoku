interface BoardFactory {
    Board createBoard(int size);
    GameNeighborhoodResolver createResolver();
}