import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;
import fais.zti.oramus.gomoku.TheWinnerIsException;
import fais.zti.oramus.gomoku.WrongBoardStateException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class GomokuTests {

    private Board board;
    private GameStatePublisher publisher;
    private MoveDecisionCollector collector;
    private MoveAnalyzer analyzer;

    @BeforeEach
    void setup() {
        board = new Board(10);
        publisher = new GameStatePublisher(Mark.CROSS);
        collector = new MoveDecisionCollector(board, Mark.CROSS);
        publisher.addObserver(collector);
        analyzer = new MoveAnalyzer(publisher, Mark.CROSS);
    }

    @Test
    void testWinningMoveDetection() throws TheWinnerIsException, WrongBoardStateException  {
        for (int i = 0; i < 4; i++) {
            board.getCell(5, i).setSymbol(Mark.CROSS);
        }
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        assertEquals(4, move.position().col());
        assertEquals(5, move.position().row());
    }

    @Test
    void testBlockingMoveDetection() throws TheWinnerIsException, WrongBoardStateException  {
        for (int i = 0; i < 4; i++) {
            board.getCell(i, 0).setSymbol(Mark.NOUGHT);
        }
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        assertEquals(0, move.position().col());
        assertEquals(4, move.position().row());
    }

    @Test
    void testOpenFourMoveDetection() throws TheWinnerIsException, WrongBoardStateException  {
        board.getCell(3, 3).setSymbol(Mark.CROSS);
        board.getCell(4, 4).setSymbol(Mark.CROSS);
        board.getCell(5, 5).setSymbol(Mark.CROSS);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        assertTrue(move.position().col() == 2 || move.position().col() == 6);
        assertTrue(move.position().row() == 2 || move.position().row() == 6);
    }

    @Test
    void testDoubleThreatMoveDetection() throws TheWinnerIsException, WrongBoardStateException  {
        board.getCell(1, 1).setSymbol(Mark.CROSS);
        board.getCell(1, 2).setSymbol(Mark.CROSS);
        board.getCell(1, 3).setSymbol(Mark.CROSS);
        board.getCell(2, 1).setSymbol(Mark.CROSS);
        board.getCell(3, 1).setSymbol(Mark.CROSS);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
    }

    @Test
    void testWinnerIsExceptionThrown() {
        for (int i = 0; i < 5; i++) {
            board.getCell(0, i).setSymbol(Mark.CROSS);
        }
        assertThrows(TheWinnerIsException.class, () -> analyzer.analyze(board));
    }

    @Test
    void testNoMoveWhenBoardIsEmpty() throws TheWinnerIsException, WrongBoardStateException {
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        // ??????
    }

    @Test
    void testAvoidWrongThreatInterpretation() throws TheWinnerIsException, WrongBoardStateException {
        board.getCell(1, 1).setSymbol(Mark.NOUGHT);
        board.getCell(1, 2).setSymbol(Mark.NOUGHT);
        board.getCell(1, 3).setSymbol(Mark.NOUGHT);
        board.getCell(2, 3).setSymbol(Mark.NOUGHT);
        board.getCell(3, 3).setSymbol(Mark.NOUGHT);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
    }

    // === Test z pkt. 12 – obrona przed natychmiastową wygraną przeciwnika ===
    @Test
    void testDefendAgainstImmediateLoss() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10);
        board.getCell(1, 1).setSymbol(Mark.NOUGHT);
        board.getCell(1, 2).setSymbol(Mark.NOUGHT);
        board.getCell(1, 3).setSymbol(Mark.CROSS);
        board.getCell(2, 3).setSymbol(Mark.CROSS);
        board.getCell(3, 3).setSymbol(Mark.NOUGHT);
        board.getCell(4, 3).setSymbol(Mark.NOUGHT);
        board.getCell(5, 3).setSymbol(Mark.NOUGHT);
        board.getCell(6, 3).setSymbol(Mark.NOUGHT);
        analyzer = new MoveAnalyzer(publisher, Mark.CROSS);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        assertTrue((move.position().col() == 3 && move.position().row() == 7));
    }

    // === Test z pkt. 13 – błąd: błędny ruch nie zapewnia wygranej ===
    @Test
    void testAvoidWrongMoveChoice() throws TheWinnerIsException, WrongBoardStateException  {
        board = new Board(10);
        board.getCell(0, 1).setSymbol(Mark.CROSS);
        board.getCell(1, 1).setSymbol(Mark.CROSS);
        board.getCell(2, 1).setSymbol(Mark.CROSS);
        board.getCell(3, 1).setSymbol(Mark.NOUGHT);
        board.getCell(2, 3).setSymbol(Mark.NOUGHT);
        board.getCell(3, 3).setSymbol(Mark.CROSS);
        board.getCell(4, 3).setSymbol(Mark.CROSS);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
    }
    @Test
    void testChooseFastestWinningPath() throws TheWinnerIsException, WrongBoardStateException  {
        board = new Board(10);
        board.getCell(2, 2).setSymbol(Mark.CROSS);
        board.getCell(3, 3).setSymbol(Mark.CROSS);
        board.getCell(4, 4).setSymbol(Mark.CROSS);
        board.getCell(5, 5).setSymbol(Mark.CROSS);

        board.getCell(2, 5).setSymbol(Mark.CROSS);
        board.getCell(3, 5).setSymbol(Mark.CROSS);
        board.getCell(4, 5).setSymbol(Mark.CROSS);

        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        // powinna być kontynuowana ścieżka prowadząca do natychmiastowego zwycięstwa
        assertTrue((
                move.position().col() == 6 && move.position().row() == 6)
                || (move.position().col() == 1 && move.position().row() == 1)
                || (move.position().col() == 5 && move.position().row() == 6)
                || (move.position().col() == 5 && move.position().row() == 1)
        );
    }

    // === Punkt 9: gracz x powinien zablokować kółko (które dąży do 4) ===
    @Test
    void testBlockOpponentWinFromThree() throws TheWinnerIsException, WrongBoardStateException  {
        board = new Board(10);
        board.getCell(2, 2).setSymbol(Mark.NOUGHT);
        board.getCell(3, 3).setSymbol(Mark.NOUGHT);
        board.getCell(4, 4).setSymbol(Mark.NOUGHT);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        assertTrue((move.position().col() == 1 && move.position().row() == 1) || (move.position().col() == 5 && move.position().row() == 5));
    }

    // === Punkt 10: gracz x musi zablokować 3 kółka (przeciwnik o) ===
    @Test
    void testBlockThreeOpponentCircle() throws TheWinnerIsException, WrongBoardStateException  {
        board = new Board(10);
        board.getCell(2, 2).setSymbol(Mark.NOUGHT);
        board.getCell(3, 2).setSymbol(Mark.NOUGHT);
        board.getCell(4, 2).setSymbol(Mark.NOUGHT);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        assertTrue((move.position().col() == 2 && move.position().row() == 1) || (move.position().col() == 2 && move.position().row() == 5));
    }

    // === Punkt 11: unikamy błędnego poddania się przy pojedynczym zagrożeniu ===
    @Test
    void testAvoidUnnecessarySurrender() throws TheWinnerIsException, WrongBoardStateException  {
        board = new Board(10);
        board.getCell(3, 3).setSymbol(Mark.NOUGHT);
        board.getCell(3, 4).setSymbol(Mark.NOUGHT);
        board.getCell(3, 5).setSymbol(Mark.NOUGHT);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move); // x powinien próbować blokować, nie poddać się
    }
    @Test
    void testDefendAgainstImmediateWinInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException  {
        Board periodicBoard = new PeriodicBoard(10);
        periodicBoard.getCell(0, 9).setSymbol(Mark.CROSS);
        periodicBoard.getCell(2, 1).setSymbol(Mark.CROSS);
        periodicBoard.getCell(3, 2).setSymbol(Mark.CROSS);
        periodicBoard.getCell(4, 3).setSymbol(Mark.CROSS);

        periodicBoard.getCell(7, 5).setSymbol(Mark.NOUGHT);


        GameStatePublisher periodicPublisher = new GameStatePublisher(Mark.NOUGHT);
        MoveDecisionCollector periodicCollector = new MoveDecisionCollector(periodicBoard, Mark.NOUGHT);
        periodicPublisher.addObserver(periodicCollector);
        MoveAnalyzer periodicAnalyzer = new MoveAnalyzer(periodicPublisher, Mark.NOUGHT);

        periodicAnalyzer.analyze(periodicBoard);
        Move move = periodicCollector.getBestMove();
        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 0 && move.position().row() == 1) );
    }

    // === Punkt 13: niepoprawny ruch oznaczony ? nie prowadzi do zwycięstwa ===
    @Test
    void testRejectIncorrectMoveMarkedAsQuestion() throws TheWinnerIsException, WrongBoardStateException  {
        board = new Board(10);
        board.getCell(3, 3).setSymbol(Mark.NOUGHT);
        board.getCell(0, 5).setSymbol(Mark.NOUGHT);
        board.getCell(1, 4).setSymbol(Mark.NOUGHT);
        board.getCell(1, 9).setSymbol(Mark.NOUGHT);
        board.getCell(2, 3).setSymbol(Mark.NOUGHT);
        board.getCell(2, 6).setSymbol(Mark.NOUGHT);
        board.getCell(2, 8).setSymbol(Mark.NOUGHT);
        board.getCell(3, 7).setSymbol(Mark.NOUGHT);
        board.getCell(4, 4).setSymbol(Mark.NOUGHT);
        board.getCell(4, 8).setSymbol(Mark.NOUGHT);
        board.getCell(5, 9).setSymbol(Mark.NOUGHT);
        board.getCell(8, 8).setSymbol(Mark.NOUGHT);
        board.getCell(0, 1).setSymbol(Mark.CROSS);
        board.getCell(1, 1).setSymbol(Mark.CROSS);
        board.getCell(1, 5).setSymbol(Mark.CROSS);
        board.getCell(2, 1).setSymbol(Mark.CROSS);
        board.getCell(2, 7).setSymbol(Mark.CROSS);
        board.getCell(2, 9).setSymbol(Mark.CROSS);
        board.getCell(4, 5).setSymbol(Mark.CROSS);
        board.getCell(5, 5).setSymbol(Mark.CROSS);
        board.getCell(6, 6).setSymbol(Mark.CROSS);
        board.getCell(6, 7).setSymbol(Mark.CROSS);
        board.getCell(7, 7).setSymbol(Mark.CROSS);
        analyzer.analyze(board);
        Move move = collector.getBestMove();
        assertNotNull(move);
        // wybierany powinien być skuteczny ruch, nie przypadkowy błędny
        assertNotEquals(3, move.position().col());
        assertNotEquals(1, move.position().row());
    }

    @Test
    void testNoVictoryWithoutSpace() throws TheWinnerIsException, WrongBoardStateException  {
        board = new Board(10);
        board.getCell(0, 1).setSymbol(Mark.CROSS);
        board.getCell(0, 2).setSymbol(Mark.NOUGHT);
        board.getCell(0, 3).setSymbol(Mark.NOUGHT);
        board.getCell(0, 4).setSymbol(Mark.NOUGHT);
        board.getCell(0, 5).setSymbol(Mark.NOUGHT);
        board.getCell(1, 2).setSymbol(Mark.CROSS);
        board.getCell(1, 3).setSymbol(Mark.CROSS);
        board.getCell(1, 4).setSymbol(Mark.CROSS);
        board.getCell(1, 5).setSymbol(Mark.CROSS);

        analyzer.analyze(board);
        Move move = collector.getBestMove();
        // mimo 5 'x', „kółko” wygrało – sprawdzamy, czy poprawnie odrzucone
        assertNotNull(move);
        assertTrue((move.position().col() == 6 && move.position().row() == 1) || (move.position().col() == 1 && move.position().row() == 1));
    }

    // === Punkt 6: gra nie współpracuje – należy wybrać najlepszy atak zamiast błędnego ===
    @Test
    void testAttackOverridesIncorrectBlock() throws TheWinnerIsException, WrongBoardStateException  {
        board = new Board(10);
        board.getCell(2, 2).setSymbol(Mark.CROSS);
        board.getCell(3, 3).setSymbol(Mark.CROSS);
        board.getCell(4, 4).setSymbol(Mark.CROSS);
        board.getCell(5, 5).setSymbol(Mark.CROSS);

        board.getCell(2, 6).setSymbol(Mark.CROSS);
        board.getCell(3, 6).setSymbol(Mark.CROSS);
        board.getCell(4, 6).setSymbol(Mark.CROSS);

        analyzer.analyze(board);
        Move move = collector.getBestMove();
        // powinien wybrać dokończenie zwycięskiej linii (nie blokować innych)
        assertNotNull(move);
        assertTrue((move.position().col() == 6 && move.position().row() == 6) || (move.position().col() == 1 && move.position().row() == 1));
    }

    // === Punkt 7: plansza już zawiera zwycięstwo – powinien być wyjątek ===
    @Test
    void testWinnerAlreadyExistsThrowsException() {
        board = new Board(10);
        for (int i = 0; i < 5; i++) {
            board.getCell(5, i).setSymbol(Mark.NOUGHT);
        }
        assertThrows(TheWinnerIsException.class, () -> analyzer.analyze(board));
    }
}
