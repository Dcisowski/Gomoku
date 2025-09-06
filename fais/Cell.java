import fais.zti.oramus.gomoku.Mark;

public class Cell{
    private int row, col;
    private Mark symbol = Mark.NULL;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setSymbol(Mark symbol) {
        this.symbol = symbol;
    }

    public Mark getSymbol() { return symbol; }
    public boolean isEmpty() { return symbol == Mark.NULL; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}
