import fais.zti.oramus.gomoku.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collector;

public class GomokuTests {

    private Board board;
    private MoveAnalyzer analyzer;

    private Board makeBoard(char[][] grid, Mark aMark, Mark bMark) {
        int n = grid.length;
        Board b = new Board(n, false);
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                char ch = grid[r][c];
                if (ch == 'A') b.set(r, c, aMark);
                else if (ch == 'B') b.set(r, c, bMark);
                else b.set(r, c, Mark.NULL);
            }
        }
        return b;
    }

    private char[][] layout() {
        return new char[][]{
                "..........".toCharArray(), // row 0
                "..AA......".toCharArray(), // row 1
                ".......A..".toCharArray(), // row 2
                ".B.....A..".toCharArray(), // row 3
                ".B........".toCharArray(), // row 4
                ".B........".toCharArray(), // row 5
                ".B........".toCharArray(), // row 6
                "..........".toCharArray(), // row 7
                "..........".toCharArray(), // row 8
                "..........".toCharArray()  // row 9
        };
    }


    @BeforeEach
    void setup() {
        board = new Board(10, false);
        analyzer = new MoveAnalyzer();
    }

    @Test
    void testWinningMoveDetection() throws TheWinnerIsException, WrongBoardStateException {
        for (int i = 0; i < 4; i++) {
            board.set(5, i, Mark.CROSS);
        }
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        assertEquals(4, move.position().col());
        assertEquals(5, move.position().row());
    }

    @Test
    void testBlockingMoveDetection() throws TheWinnerIsException, WrongBoardStateException {
        for (int i = 0; i < 4; i++) {
            board.set(i, 0, Mark.NOUGHT);
        }
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        assertEquals(0, move.position().col());
        assertEquals(4, move.position().row());
    }

    @Test
    void testOpenFourMoveDetection() throws TheWinnerIsException, WrongBoardStateException {
        board.set(3, 3, Mark.CROSS);
        board.set(4, 4, Mark.CROSS);
        board.set(5, 5, Mark.CROSS);
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        assertTrue(move.position().col() == 2 || move.position().col() == 6);
        assertTrue(move.position().row() == 2 || move.position().row() == 6);
    }

    @Test
    void testDoubleThreatMoveDetection() throws TheWinnerIsException, WrongBoardStateException {
        board.set(1, 1, Mark.CROSS);
        board.set(1, 2, Mark.CROSS);
        board.set(1, 3, Mark.CROSS);
        board.set(2, 1, Mark.CROSS);
        board.set(3, 1, Mark.CROSS);
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
    }

    @Test
    void testWinnerIsExceptionThrown() {
        for (int i = 0; i < 5; i++) {
            board.set(0, i, Mark.CROSS);
        }
        assertThrows(TheWinnerIsException.class, () -> analyzer.analyze(board, Mark.CROSS));
    }

    @Test
    void testNoMoveWhenBoardIsEmpty() throws TheWinnerIsException, WrongBoardStateException {
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        // ??????
    }

    @Test
    void testAvoidWrongThreatInterpretation() throws TheWinnerIsException, WrongBoardStateException {
        board.set(1, 1, Mark.NOUGHT);
        board.set(1, 2, Mark.NOUGHT);
        board.set(1, 3, Mark.NOUGHT);
        board.set(2, 3, Mark.NOUGHT);
        board.set(3, 3, Mark.NOUGHT);
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;
        assertNotNull(move);
    }

    // === Test z pkt. 12 – obrona przed natychmiastową wygraną przeciwnika ===
    @Test
    void testDefendAgainstImmediateLoss() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(1, 1, Mark.NOUGHT);
        board.set(1, 2, Mark.NOUGHT);
        board.set(1, 3, Mark.CROSS);
        board.set(2, 3, Mark.CROSS);
        board.set(3, 3, Mark.NOUGHT);
        board.set(4, 3, Mark.NOUGHT);
        board.set(5, 3, Mark.NOUGHT);
        board.set(6, 3, Mark.NOUGHT);
        analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        assertTrue((move.position().col() == 3 && move.position().row() == 7));
    }

    // === Test z pkt. 13 – błąd: błędny ruch nie zapewnia wygranej ===
    @Test
    void testAvoidWrongMoveChoice() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(0, 1, Mark.CROSS);
        board.set(1, 1, Mark.CROSS);
        board.set(2, 1, Mark.CROSS);
        board.set(3, 1, Mark.NOUGHT);
        board.set(2, 3, Mark.NOUGHT);
        board.set(3, 3, Mark.CROSS);
        board.set(4, 3, Mark.CROSS);
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
    }

    @Test
    void testChooseFastestWinningPath() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(2, 2, Mark.CROSS);
        board.set(3, 3, Mark.CROSS);
        board.set(4, 4, Mark.CROSS);
        board.set(5, 5, Mark.CROSS);

        board.set(2, 5, Mark.CROSS);
        board.set(3, 5, Mark.CROSS);
        board.set(4, 5, Mark.CROSS);

        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
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
    void testBlockOpponentWinFromThree() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(2, 2, Mark.NOUGHT);
        board.set(3, 3, Mark.NOUGHT);
        board.set(4, 4, Mark.NOUGHT);
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        assertTrue((move.position().col() == 1 && move.position().row() == 1) || (move.position().col() == 5 && move.position().row() == 5));
    }

    // === Punkt 10: gracz x musi zablokować 3 kółka (przeciwnik o) ===
    @Test
    void testBlockThreeOpponentCircle() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(2, 2, Mark.NOUGHT);
        board.set(3, 2, Mark.NOUGHT);
        board.set(4, 2, Mark.NOUGHT);
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        assertTrue((move.position().col() == 2 && move.position().row() == 1) || (move.position().col() == 2 && move.position().row() == 5));
    }

    // === Punkt 11: unikamy błędnego poddania się przy pojedynczym zagrożeniu ===
    @Test
    void testAvoidUnnecessarySurrender() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(3, 3, Mark.NOUGHT);
        board.set(3, 4, Mark.NOUGHT);
        board.set(3, 5, Mark.NOUGHT);
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move); // x powinien próbować blokować, nie poddać się
    }

    @Test
    void testDefendAgainstImmediateWinInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException {
        Board periodicBoard = new Board(10, true);
        periodicBoard.set(0, 9, Mark.CROSS);
        periodicBoard.set(2, 1, Mark.CROSS);
        periodicBoard.set(3, 2, Mark.CROSS);
        periodicBoard.set(4, 3, Mark.CROSS);

        periodicBoard.set(7, 5, Mark.NOUGHT);


        MoveAnalyzer periodicAnalyzer = new MoveAnalyzer();
        AnalysisResult res = periodicAnalyzer.analyze(periodicBoard, Mark.NOUGHT);
        Move move = res.move;
        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 0 && move.position().row() == 1));
    }

    @Test
    void testWinInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException {
        Board periodicBoard = new Board(10, true);
        periodicBoard.set(2, 1, Mark.CROSS);
        periodicBoard.set(3, 0, Mark.CROSS);
        periodicBoard.set(4, 9, Mark.CROSS);
        periodicBoard.set(5, 8, Mark.CROSS);

        periodicBoard.set(7, 5, Mark.CROSS);


        MoveAnalyzer periodicAnalyzer = new MoveAnalyzer();
        AnalysisResult res = periodicAnalyzer.analyze(periodicBoard, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 2 && move.position().row() == 1) || (move.position().col() == 7 && move.position().row() == 6));
    }

    @Test
    void testWinOverBlockInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException {
        Board periodicBoard = new Board(10, true);
        periodicBoard.set(4, 0, Mark.CROSS);
        periodicBoard.set(5, 9, Mark.CROSS);
        periodicBoard.set(6, 8, Mark.CROSS);

        periodicBoard.set(7, 5, Mark.NOUGHT);
        periodicBoard.set(7, 6, Mark.NOUGHT);
        periodicBoard.set(7, 4, Mark.NOUGHT);


        MoveAnalyzer periodicAnalyzer = new MoveAnalyzer();
        AnalysisResult res = periodicAnalyzer.analyze(periodicBoard, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 7 && move.position().row() == 7));
    }

    // === Punkt 13: niepoprawny ruch oznaczony ? nie prowadzi do zwycięstwa ===
    @Test
    void testRejectIncorrectMoveMarkedAsQuestion() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(3, 3, Mark.NOUGHT);
        board.set(0, 5, Mark.NOUGHT);
        board.set(1, 4, Mark.NOUGHT);
        board.set(2, 3, Mark.NOUGHT);
        board.set(2, 6, Mark.NOUGHT);
        board.set(2, 8, Mark.NOUGHT);
        board.set(3, 7, Mark.NOUGHT);
        board.set(4, 4, Mark.NOUGHT);
        board.set(4, 8, Mark.NOUGHT);
        board.set(5, 9, Mark.NOUGHT);
        board.set(8, 8, Mark.NOUGHT);
        board.set(0, 1, Mark.CROSS);
        board.set(1, 1, Mark.CROSS);
        board.set(1, 5, Mark.CROSS);
        board.set(2, 1, Mark.CROSS);
        board.set(2, 7, Mark.CROSS);
        board.set(2, 9, Mark.CROSS);
        board.set(4, 5, Mark.CROSS);
        board.set(5, 5, Mark.CROSS);
        board.set(6, 6, Mark.CROSS);
        board.set(6, 7, Mark.CROSS);
        board.set(7, 7, Mark.CROSS);

        MoveAnalyzer periodicAnalyzer = new MoveAnalyzer();
        AnalysisResult res = periodicAnalyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        assertNotNull(move);
        // wybierany powinien być skuteczny ruch, nie przypadkowy błędny
        assertNotEquals(3, move.position().col());
        assertNotEquals(1, move.position().row());
    }

    @Test
    void testNoVictoryWithoutSpace() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(0, 1, Mark.CROSS);
        board.set(0, 2, Mark.NOUGHT);
        board.set(0, 3, Mark.NOUGHT);
        board.set(0, 4, Mark.NOUGHT);
        board.set(0, 5, Mark.NOUGHT);
        board.set(1, 2, Mark.CROSS);
        board.set(1, 3, Mark.CROSS);
        board.set(1, 4, Mark.CROSS);
        board.set(1, 5, Mark.CROSS);

        MoveAnalyzer periodicAnalyzer = new MoveAnalyzer();
        AnalysisResult res = periodicAnalyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        // mimo 5 'x', „kółko” wygrało – sprawdzamy, czy poprawnie odrzucone
        assertNotNull(move);
        assertTrue((move.position().col() == 6 && move.position().row() == 1) || (move.position().col() == 1 && move.position().row() == 1));
    }

    // === Punkt 6: gra nie współpracuje – należy wybrać najlepszy atak zamiast błędnego ===
    @Test
    void testAttackOverridesIncorrectBlock() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);
        board.set(2, 2, Mark.CROSS);
        board.set(3, 3, Mark.CROSS);
        board.set(4, 4, Mark.CROSS);
        board.set(5, 5, Mark.CROSS);

        board.set(2, 6, Mark.CROSS);
        board.set(3, 6, Mark.CROSS);
        board.set(4, 6, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;
        // powinien wybrać dokończenie zwycięskiej linii (nie blokować innych)
        assertNotNull(move);
        assertTrue((move.position().col() == 6 && move.position().row() == 6) || (move.position().col() == 1 && move.position().row() == 1));
    }

    //
    // === Punkt 7: plansza już zawiera zwycięstwo – powinien być wyjątek ===
    @Test
    void testWinnerAlreadyExistsThrowsException() {
        board = new Board(10, false);
        for (int i = 0; i < 5; i++) {
            board.set(5, i, Mark.NOUGHT);
        }
        MoveAnalyzer analyzer = new MoveAnalyzer();
        assertThrows(TheWinnerIsException.class, () -> analyzer.analyze(board, Mark.CROSS));
    }

    @Test
    void testLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(0, 2, Mark.NOUGHT);
        board.set(0, 4, Mark.CROSS);
        board.set(0, 5, Mark.CROSS);
        board.set(0, 7, Mark.CROSS);
        board.set(0, 9, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue((move.position().col() == 6 && move.position().row() == 0));
    }

    @Test
    void testFastestLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(1, 4, Mark.NOUGHT);
        board.set(1, 5, Mark.NOUGHT);
        board.set(1, 6, Mark.NOUGHT);
        board.set(3, 7, Mark.NOUGHT);
        board.set(4, 7, Mark.NOUGHT);
        board.set(5, 5, Mark.NOUGHT);
        board.set(5, 6, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 7 && move.position().row() == 1)
                        || (move.position().col() == 3 && move.position().row() == 1)
        );
    }

    @Test
    void testBlockingLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(1, 4, Mark.NOUGHT);
        board.set(1, 5, Mark.NOUGHT);
        board.set(1, 6, Mark.NOUGHT);
        board.set(3, 7, Mark.CROSS);
        board.set(4, 7, Mark.CROSS);
        board.set(5, 5, Mark.CROSS);
        board.set(5, 6, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 7 && move.position().row() == 1)
                        || (move.position().col() == 3 && move.position().row() == 1)
        );
    }

    @Test
    void testBlockingLeadingToWinningComplex() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(0, 7, Mark.CROSS);

        board.set(2, 2, Mark.NOUGHT);
        board.set(2, 3, Mark.NOUGHT);
        board.set(2, 4, Mark.CROSS);
        board.set(2, 5, Mark.NOUGHT);
        board.set(2, 6, Mark.NOUGHT);

        board.set(3, 3, Mark.CROSS);
        board.set(3, 5, Mark.CROSS);
        board.set(3, 6, Mark.NOUGHT);
        board.set(3, 9, Mark.NOUGHT);

        board.set(4, 2, Mark.CROSS);
        board.set(4, 3, Mark.CROSS);
        board.set(4, 4, Mark.NOUGHT);
        board.set(4, 6, Mark.NOUGHT);
        board.set(4, 8, Mark.CROSS);

        board.set(5, 3, Mark.CROSS);
        board.set(5, 7, Mark.CROSS);
        board.set(5, 8, Mark.NOUGHT);

        board.set(6, 0, Mark.CROSS);
        board.set(6, 2, Mark.CROSS);
        board.set(6, 3, Mark.NOUGHT);
        board.set(6, 8, Mark.CROSS);

        board.set(7, 9, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(move.position().col() == 1 && move.position().row() == 5);
    }

    @Test
    void testCorrectMove() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(0, 1, Mark.CROSS);
        board.set(0, 5, Mark.NOUGHT);
        board.set(1, 1, Mark.CROSS);
        board.set(2, 1, Mark.CROSS);

        board.set(4, 4, Mark.NOUGHT);
        board.set(4, 5, Mark.CROSS);
        board.set(5, 5, Mark.CROSS);
        board.set(6, 6, Mark.CROSS);
        board.set(6, 7, Mark.CROSS);
        board.set(7, 7, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(move.position().col() == 5 && move.position().row() == 6);
    }

    //
    @Test
    void testIncorrectMove() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(1, 0, Mark.NOUGHT);
        board.set(1, 1, Mark.NOUGHT);
        board.set(1, 2, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 2)
                        || (move.position().col() == 0 && move.position().row() == 2)
                        || (move.position().col() == 2 && move.position().row() == 2)
        );
    }
    @Test
    void testCorrectDoubleFour() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(2, 2, Mark.NOUGHT);
        board.set(2, 3, Mark.NOUGHT);
        board.set(2, 5, Mark.NOUGHT);

        board.set(5, 9, Mark.NOUGHT);
        board.set(5, 8, Mark.NOUGHT);
        board.set(5, 7, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 4 && move.position().row() == 2)
        );
    }
    @Test
    void testPriorityWinning() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(2, 2, Mark.NOUGHT);
        board.set(2, 3, Mark.NOUGHT);
        board.set(2, 5, Mark.NOUGHT);
        board.set(2, 6, Mark.NOUGHT);

        board.set(4, 6, Mark.CROSS);
        board.set(5, 6, Mark.CROSS);
        board.set(7, 6, Mark.CROSS);
        board.set(8, 6, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 4 && move.position().row() == 2)
        );
    }
    @Test
    void testPriority2() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(0, 5, Mark.NOUGHT);
        board.set(0, 6, Mark.NOUGHT);
        board.set(0, 7, Mark.NOUGHT);

        board.set(2, 4, Mark.CROSS);
        board.set(3, 5, Mark.CROSS);
        board.set(4, 6, Mark.CROSS);
        board.set(5, 7, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 1)
                || (move.position().col() == 8 && move.position().row() == 6)
        );

//        MoveAnalyzer analyzer2 = new MoveAnalyzer();
//
//        assertThrows(ResignException.class, () -> analyzer2.analyze(board, Mark.NOUGHT));

    }
    @Test
    void testPriority3() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(0, 6, Mark.NOUGHT);
        board.set(0, 7, Mark.NOUGHT);
        board.set(0, 8, Mark.NOUGHT);

        board.set(2, 3, Mark.CROSS);
        board.set(3, 4, Mark.CROSS);
        board.set(5, 7, Mark.CROSS);
        board.set(5, 8, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 5 && move.position().row() == 0)
//                        || (move.position().col() == 8 && move.position().row() == 6)
        );

    }
    @Test
    void testPriority4() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(0, 5, Mark.CROSS);
        board.set(0, 7, Mark.NOUGHT);
        board.set(0, 8, Mark.NOUGHT);

        board.set(2, 3, Mark.CROSS);
        board.set(3, 4, Mark.CROSS);
        board.set(4, 7, Mark.CROSS);
        board.set(4, 8, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 5 && move.position().row() == 4)
                        || (move.position().col() == 6 && move.position().row() == 4)
        );

    }
    @Test
    void testPriority5() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(1, 3, Mark.NOUGHT);
        board.set(2, 3, Mark.NOUGHT);
        board.set(3, 3, Mark.NOUGHT);

        board.set(1, 8, Mark.CROSS);
        board.set(2, 8, Mark.CROSS);
        board.set(3, 8, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 8 && move.position().row() == 4)
        );

    }
    @Test
    void testPriority6() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(1, 3, Mark.NOUGHT);
        board.set(2, 3, Mark.NOUGHT);
        board.set(3, 3, Mark.NOUGHT);

        board.set(0, 8, Mark.CROSS);
        board.set(1, 8, Mark.CROSS);
        board.set(2, 8, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 4)
        );

    }
    @Test
    void testPriority15_2_AisCross_BisNought_blockColumnEnd() throws TheWinnerIsException, WrongBoardStateException {
        Board board = new Board(10, false);

        // A = CROSS
        board.set(1, 4, Mark.CROSS); // "..AA......"  -> row=1, col=2
        board.set(1, 5, Mark.CROSS); //                row=1, col=3
        board.set(2, 7, Mark.CROSS); // ".......A.."  -> row=2, col=7
        board.set(3, 8, Mark.CROSS); // ".B.....A.."  -> row=3, col=7

        // B = NOUGHT (vertical four with two open ends at (1,2) and (1,7))
        board.set(3, 2, Mark.NOUGHT);
        board.set(4, 2, Mark.NOUGHT);
        board.set(5, 3, Mark.NOUGHT);
        board.set(6, 4, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 3)     // bottom end of B-run
        );
    }

    @Test
    void testPriority15_2_AisNought_BisCross_blockColumnEnd() throws TheWinnerIsException, WrongBoardStateException {
        Board board = new Board(10, false);

        // A = NOUGHT
        board.set(1, 4, Mark.CROSS); // "..AA......"  -> row=1, col=2
        board.set(1, 5, Mark.CROSS); //                row=1, col=3
        board.set(2, 7, Mark.CROSS); // ".......A.."  -> row=2, col=7
        board.set(3, 8, Mark.CROSS); // ".B.....A.."  -> row=3, col=7

        // B = NOUGHT (vertical four with two open ends at (1,2) and (1,7))
        board.set(3, 2, Mark.NOUGHT);
        board.set(4, 2, Mark.NOUGHT);
        board.set(5, 3, Mark.NOUGHT);
        board.set(6, 4, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 3)     // bottom end of B-run
                || (move.position().col() == 5 && move.position().row() == 7)
        );
    }
    @Test
    void testNoSpaceNoWin() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(1, 1, Mark.NOUGHT);
        board.set(2, 1, Mark.NOUGHT);
        board.set(3, 1, Mark.NOUGHT);
        board.set(5, 1, Mark.CROSS);


        board.set(2, 6, Mark.NOUGHT);
        board.set(2, 7, Mark.NOUGHT);
        board.set(3, 5, Mark.NOUGHT);
        board.set(4, 5, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 5 && move.position().row() == 2)
        );

    }
    @Test
    void testNoSpaceNoWin2() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(0, 1, Mark.NOUGHT);
        board.set(1, 1, Mark.NOUGHT);
        board.set(3, 1, Mark.NOUGHT);

        board.set(2, 6, Mark.NOUGHT);
        board.set(2, 7, Mark.NOUGHT);
        board.set(3, 5, Mark.NOUGHT);
        board.set(4, 5, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.NOUGHT);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 5 && move.position().row() == 2)
        );

    }
    @Test
    void testNoSpaceNoWin2BlocDoubleThree() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(0, 1, Mark.NOUGHT);
        board.set(1, 1, Mark.NOUGHT);
        board.set(3, 1, Mark.NOUGHT);

        board.set(2, 6, Mark.NOUGHT);
        board.set(2, 7, Mark.NOUGHT);
        board.set(3, 5, Mark.NOUGHT);
        board.set(4, 5, Mark.NOUGHT);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 5 && move.position().row() == 2)
        );

    }
    @Test
    void testNoSurrender() throws TheWinnerIsException, WrongBoardStateException {
        board = new Board(10, false);

        board.set(2, 9, Mark.CROSS);
        board.set(2, 7, Mark.NOUGHT);

        board.set(3, 7, Mark.NOUGHT);
        board.set(3, 8, Mark.CROSS);
        board.set(3, 9, Mark.NOUGHT);

        board.set(4, 7, Mark.NOUGHT);
        board.set(4, 8, Mark.NOUGHT);

        board.set(9, 6, Mark.CROSS);

        MoveAnalyzer analyzer = new MoveAnalyzer();
        AnalysisResult res = analyzer.analyze(board, Mark.CROSS);
        Move move = res.move;

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 7 && move.position().row() == 5)
        );

    }
}



