import fais.zti.oramus.gomoku.Move;
import fais.zti.oramus.gomoku.ResignException;

public class AnalysisResult {
    public final MoveType type; public final Move move;
    public AnalysisResult(MoveType t, Move m){ this.type=t; this.move=m; }
    public static AnalysisResult resign() throws ResignException { throw new ResignException();}
}