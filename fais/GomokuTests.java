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
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        analyzer = new MoveAnalyzer();
    }

    @Test
    void testWinningMoveDetection() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 4; i++) {
            boardState.add(new Move(new Position(5, i), Mark.CROSS));
        }
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
        assertEquals(5, move.position().col());
        assertEquals(4, move.position().row());
    }

    @Test
    void testBlockingMoveDetection() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 4; i++) {
            boardState.add(new Move(new Position(i, 0), Mark.NOUGHT));
        }
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
        assertEquals(4, move.position().col());
        assertEquals(0, move.position().row());
    }

    @Test
    void testOpenFourMoveDetection() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
        assertTrue(move.position().col() == 2 || move.position().col() == 6);
        assertTrue(move.position().row() == 2 || move.position().row() == 6);
    }

    @Test
    void testDoubleThreatMoveDetection() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(1, 1), Mark.CROSS));
        boardState.add(new Move(new Position(1, 2), Mark.CROSS));
        boardState.add(new Move(new Position(1, 3), Mark.CROSS));
        boardState.add(new Move(new Position(2, 1), Mark.CROSS));
        boardState.add(new Move(new Position(3, 1), Mark.CROSS));
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
    }

    @Test
    void testWinnerIsExceptionThrown() {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(0, i), Mark.CROSS));
        }
        assertThrows(TheWinnerIsException.class, () -> gomoku.nextMove(boardState, Mark.CROSS));
    }

    @Test
    void testNoMoveWhenBoardIsEmpty() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
        // ??????
    }

    @Test
    void testAvoidWrongThreatInterpretation() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        
        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));
        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);
        assertNotNull(move);
    }

    // === Test z pkt. 12 – obrona przed natychmiastową wygraną przeciwnika ===
    @Test
    void testDefendAgainstImmediateLoss() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 3), Mark.CROSS));
        boardState.add(new Move(new Position(2, 3), Mark.CROSS));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 3), Mark.NOUGHT));
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
        assertTrue((move.position().col() == 7 && move.position().row() == 3));
    }

    // === Test z pkt. 13 – błąd: błędny ruch nie zapewnia wygranej ===
    @Test
    void testAvoidWrongMoveChoice() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 1), Mark.CROSS));
        boardState.add(new Move(new Position(1, 1), Mark.CROSS));
        boardState.add(new Move(new Position(2, 1), Mark.CROSS));
        boardState.add(new Move(new Position(3, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 3), Mark.CROSS));
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
    }

    @Test
    void testChooseFastestWinningPath() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(2, 2), Mark.CROSS));
        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));

        boardState.add(new Move(new Position(2, 5), Mark.CROSS));
        boardState.add(new Move(new Position(3, 5), Mark.CROSS));
        boardState.add(new Move(new Position(4, 5), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
        // powinna być kontynuowana ścieżka prowadząca do natychmiastowego zwycięstwa
        assertTrue((
                move.position().col() == 6 && move.position().row() == 6)
                || (move.position().col() == 1 && move.position().row() == 1)
                || (move.position().col() == 6 && move.position().row() == 5)
                || (move.position().col() == 1 && move.position().row() == 5)
        );
    }

    // === Punkt 9: gracz x powinien zablokować kółko (które dąży do 4) ===
    @Test
    void testBlockOpponentWinFromThree() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 4), Mark.NOUGHT));
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
        assertTrue((move.position().col() == 1 && move.position().row() == 1) || (move.position().col() == 5 && move.position().row() == 5));
    }

    // === Punkt 10: gracz x musi zablokować 3 kółka (przeciwnik o) ===
    @Test
    void testBlockThreeOpponentCircle() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 2), Mark.NOUGHT));
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move);
        assertTrue((move.position().col() == 1 && move.position().row() == 2) || (move.position().col() == 5 && move.position().row() == 2));
    }

    // === Punkt 11: unikamy błędnego poddania się przy pojedynczym zagrożeniu ===
    @Test
    void testAvoidUnnecessarySurrender() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 5), Mark.NOUGHT));
        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        assertNotNull(move); // x powinien próbować blokować, nie poddać się
    }

    @Test
    void testDefendAgainstImmediateWinInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        gomoku.periodicBoundaryConditionsInUse();

        Set<Move> periodicboardState = new HashSet<Move>();

        periodicboardState.add(new Move(new Position(0, 9), Mark.CROSS));
        periodicboardState.add(new Move(new Position(2, 1), Mark.CROSS));
        periodicboardState.add(new Move(new Position(3, 2), Mark.CROSS));
        periodicboardState.add(new Move(new Position(4, 3), Mark.CROSS));

        periodicboardState.add(new Move(new Position(7, 5), Mark.NOUGHT));


        Move move = gomoku.nextMove(periodicboardState, Mark.NOUGHT);

        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 1 && move.position().row() == 0));
    }

    @Test
    void testWinInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        gomoku.periodicBoundaryConditionsInUse();

        Set<Move> periodicboardState = new HashSet<Move>();

        periodicboardState.add(new Move(new Position(2, 1), Mark.CROSS));
        periodicboardState.add(new Move(new Position(3, 0), Mark.CROSS));
        periodicboardState.add(new Move(new Position(4, 9), Mark.CROSS));
        periodicboardState.add(new Move(new Position(5, 8), Mark.CROSS));

        periodicboardState.add(new Move(new Position(7, 5), Mark.CROSS));


        Move move = gomoku.nextMove(periodicboardState, Mark.CROSS);

        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 1 && move.position().row() == 2) || (move.position().col() == 6 && move.position().row() == 7));
    }

    @Test
    void testWinOverBlockInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        gomoku.periodicBoundaryConditionsInUse();

        Set<Move> periodicboardState = new HashSet<Move>();

        periodicboardState.add(new Move(new Position(4, 0), Mark.CROSS));
        periodicboardState.add(new Move(new Position(5, 9), Mark.CROSS));
        periodicboardState.add(new Move(new Position(6, 8), Mark.CROSS));

        periodicboardState.add(new Move(new Position(7, 5), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(7, 6), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(7, 4), Mark.NOUGHT));


        Move move = gomoku.nextMove(periodicboardState, Mark.CROSS);

        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 7 && move.position().row() == 7));
    }

    // === Punkt 13: niepoprawny ruch oznaczony ? nie prowadzi do zwycięstwa ===
    @Test
    void testRejectIncorrectMoveMarkedAsQuestion() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 8), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 8), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 9), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 8), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 1), Mark.CROSS));
        boardState.add(new Move(new Position(1, 1), Mark.CROSS));
        boardState.add(new Move(new Position(1, 5), Mark.CROSS));
        boardState.add(new Move(new Position(2, 1), Mark.CROSS));
        boardState.add(new Move(new Position(2, 7), Mark.CROSS));
        boardState.add(new Move(new Position(2, 9), Mark.CROSS));
        boardState.add(new Move(new Position(4, 5), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));
        boardState.add(new Move(new Position(6, 6), Mark.CROSS));
        boardState.add(new Move(new Position(6, 7), Mark.CROSS));
        boardState.add(new Move(new Position(7, 7), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        
        assertNotNull(move);
        // wybierany powinien być skuteczny ruch, nie przypadkowy błędny
        assertNotEquals(3, move.position().col());
        assertNotEquals(1, move.position().row());
    }

    @Test
    void testNoVictoryWithoutSpace() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        
        boardState.add(new Move(new Position(0, 1), Mark.CROSS));
        boardState.add(new Move(new Position(0, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 2), Mark.CROSS));
        boardState.add(new Move(new Position(1, 3), Mark.CROSS));
        boardState.add(new Move(new Position(1, 4), Mark.CROSS));
        boardState.add(new Move(new Position(1, 5), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        // mimo 5 'x', „kółko” wygrało – sprawdzamy, czy poprawnie odrzucone
        assertNotNull(move);
        assertTrue((move.position().col() == 1 && move.position().row() == 6) || (move.position().col() == 1 && move.position().row() == 1));
    }

    // === Punkt 6: gra nie współpracuje – należy wybrać najlepszy atak zamiast błędnego ===
    @Test
    void testAttackOverridesIncorrectBlock() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        
        boardState.add(new Move(new Position(2, 2), Mark.CROSS));
        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));

        boardState.add(new Move(new Position(2, 6), Mark.CROSS));
        boardState.add(new Move(new Position(3, 6), Mark.CROSS));
        boardState.add(new Move(new Position(4, 6), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);
        
        // powinien wybrać dokończenie zwycięskiej linii (nie blokować innych)
        assertNotNull(move);
        assertTrue((move.position().col() == 6 && move.position().row() == 6) || (move.position().col() == 1 && move.position().row() == 1));
    }

    //
    // === Punkt 7: plansza już zawiera zwycięstwo – powinien być wyjątek ===
    @Test
    void testWinnerAlreadyExistsThrowsException() {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        
        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(5, i), Mark.NOUGHT));
        }

        assertThrows(TheWinnerIsException.class, () -> gomoku.nextMove(boardState, Mark.CROSS));
    }

    @Test
    void testLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
        
        boardState.add(new Move(new Position(0, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 4), Mark.CROSS));
        boardState.add(new Move(new Position(0, 5), Mark.CROSS));
        boardState.add(new Move(new Position(0, 7), Mark.CROSS));
        boardState.add(new Move(new Position(0, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue((move.position().col() == 0 && move.position().row() == 6));
    }

    @Test
    void testFastestLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(1, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 6), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 7)
                        || (move.position().col() == 1 && move.position().row() == 3)
        );
    }

    @Test
    void testBlockingLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 7), Mark.CROSS));
        boardState.add(new Move(new Position(4, 7), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));
        boardState.add(new Move(new Position(5, 6), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 7)
                        || (move.position().col() == 1 && move.position().row() == 3)
        );
    }

    @Test
    void testBlockingLeadingToWinningComplex() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 7), Mark.CROSS));

        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 4), Mark.CROSS));
        boardState.add(new Move(new Position(2, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 6), Mark.NOUGHT));

        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(3, 5), Mark.CROSS));
        boardState.add(new Move(new Position(3, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 9), Mark.NOUGHT));

        boardState.add(new Move(new Position(4, 2), Mark.CROSS));
        boardState.add(new Move(new Position(4, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 8), Mark.CROSS));

        boardState.add(new Move(new Position(5, 3), Mark.CROSS));
        boardState.add(new Move(new Position(5, 7), Mark.CROSS));
        boardState.add(new Move(new Position(5, 8), Mark.NOUGHT));

        boardState.add(new Move(new Position(6, 0), Mark.CROSS));
        boardState.add(new Move(new Position(6, 2), Mark.CROSS));
        boardState.add(new Move(new Position(6, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 8), Mark.CROSS));

        boardState.add(new Move(new Position(7, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);

        assertNotNull(move);
        assertTrue(move.position().col() == 5 && move.position().row() == 1);
    }

    @Test
    void testCorrectMove() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 1), Mark.CROSS));
        boardState.add(new Move(new Position(2, 1), Mark.CROSS));

        boardState.add(new Move(new Position(4, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 5), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));
        boardState.add(new Move(new Position(6, 6), Mark.CROSS));
        boardState.add(new Move(new Position(6, 7), Mark.CROSS));
        boardState.add(new Move(new Position(7, 7), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(move.position().col() == 6 && move.position().row() == 5);
    }

    //
    @Test
    void testIncorrectMove() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 0), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 2), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 1)
                        || (move.position().col() == 2 && move.position().row() == 0)
                        || (move.position().col() == 2 && move.position().row() == 2)
        );
    }
    @Test
    void testCorrectDoubleFour() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 5), Mark.NOUGHT));

        boardState.add(new Move(new Position(5, 9), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 8), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 7), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 4)
        );
    }
    @Test
    void testPriorityWinning() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 6), Mark.NOUGHT));

        boardState.add(new Move(new Position(4, 6), Mark.CROSS));
        boardState.add(new Move(new Position(5, 6), Mark.CROSS));
        boardState.add(new Move(new Position(7, 6), Mark.CROSS));
        boardState.add(new Move(new Position(8, 6), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 4)
        );
    }
    @Test
    void testPriority2() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 4), Mark.CROSS));
        boardState.add(new Move(new Position(3, 5), Mark.CROSS));
        boardState.add(new Move(new Position(4, 6), Mark.CROSS));
        boardState.add(new Move(new Position(5, 7), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 3)
                || (move.position().col() == 6 && move.position().row() == 8)
        );

//        MoveAnalyzer analyzer2 = new MoveAnalyzer();
//
//        assertThrows(ResignException.class, () -> analyzer2.analyze(board, Mark.NOUGHT));

    }
    @Test
    void testPriority3() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 8), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 3), Mark.CROSS));
        boardState.add(new Move(new Position(3, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 7), Mark.CROSS));
        boardState.add(new Move(new Position(5, 8), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 0 && move.position().row() == 5)
//                        || (move.position().col() == 8 && move.position().row() == 6)
        );

    }
    @Test
    void testPriority4() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 5), Mark.CROSS));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 8), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 3), Mark.CROSS));
        boardState.add(new Move(new Position(3, 4), Mark.CROSS));
        boardState.add(new Move(new Position(4, 7), Mark.CROSS));
        boardState.add(new Move(new Position(4, 8), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 4 && move.position().row() == 5)
                        || (move.position().col() == 4 && move.position().row() == 6)
        );

    }
    @Test
    void testPriority5() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));

        boardState.add(new Move(new Position(1, 8), Mark.CROSS));
        boardState.add(new Move(new Position(2, 8), Mark.CROSS));
        boardState.add(new Move(new Position(3, 8), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 4 && move.position().row() == 8)
        );

    }
    @Test
    void testPriority6() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));

        boardState.add(new Move(new Position(0, 8), Mark.CROSS));
        boardState.add(new Move(new Position(1, 8), Mark.CROSS));
        boardState.add(new Move(new Position(2, 8), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 4 && move.position().row() == 3)
        );

    }
    @Test
    void testPriority15_2_AisCross_BisNought_blockColumnEnd() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 4), Mark.CROSS));
        // A = NOUGHT
        boardState.add(new Move(new Position(1, 5), Mark.CROSS)); //                row=1, col=3
        boardState.add(new Move(new Position(2, 7), Mark.CROSS)); // ".......A.."  -> row=2, col=7
        boardState.add(new Move(new Position(3, 8), Mark.CROSS)); // ".B.....A.."  -> row=3, col=7

        // B = NOUGHT (vertical four with two open ends at (1,2) and (1,7))
        boardState.add(new Move(new Position(3, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 4), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 1)
                || (move.position().col() == 7 && move.position().row() == 5)
        );
    }

    @Test
    void testBlockDoubleFourAndThree() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 4), Mark.CROSS));
        // A = NOUGHT
        boardState.add(new Move(new Position(1, 5), Mark.CROSS)); //                row=1, col=3
        boardState.add(new Move(new Position(2, 7), Mark.CROSS)); // ".......A.."  -> row=2, col=7
        boardState.add(new Move(new Position(3, 8), Mark.CROSS)); // ".B.....A.."  -> row=3, col=7

        // B = NOUGHT (vertical four with two open ends at (1,2) and (1,7))
        boardState.add(new Move(new Position(3, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 4), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 1)
        );
    }

    @Test
    void testPriority15_2_AisNought_BisCross_blockColumnEnd() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 4), Mark.CROSS));
        // A = NOUGHT
        boardState.add(new Move(new Position(1, 5), Mark.CROSS)); //                row=1, col=3
        boardState.add(new Move(new Position(2, 7), Mark.CROSS)); // ".......A.."  -> row=2, col=7
        boardState.add(new Move(new Position(3, 8), Mark.CROSS)); // ".B.....A.."  -> row=3, col=7

        // B = NOUGHT (vertical four with two open ends at (1,2) and (1,7))
        boardState.add(new Move(new Position(3, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 4), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 1)     // bottom end of B-run
                || (move.position().col() == 7 && move.position().row() == 5)
        );
    }
    @Test
    void testNoSpaceNoWin() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 1), Mark.CROSS));


        boardState.add(new Move(new Position(2, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 5), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 5)
        );

    }
    @Test
    void testNoSpaceNoWin2() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 1), Mark.NOUGHT));

        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 1), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 5), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.NOUGHT);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 5)
        );

    }
    @Test
    void testNoSpaceNoWin2BlocDoubleThree() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(13);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 1), Mark.NOUGHT));

        boardState.add(new Move(new Position(7, 11), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 12), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 10), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 10), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 7 && move.position().row() == 10)
        );

    }
    @Test
    void testNoSurrender() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(9, 2), Mark.CROSS));
        boardState.add(new Move(new Position(7, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 3), Mark.CROSS));
        boardState.add(new Move(new Position(9, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 9), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, Mark.CROSS);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 7 && move.position().row() == 5)
        );

    }
    @Test
    void testResign() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);

        Set<Move> boardState = new HashSet<>();

        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 6), Mark.NOUGHT));

        boardState.add(new Move(new Position(8, 9), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 8), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 6), Mark.NOUGHT));

        assertThrows(ResignException.class, () -> gomoku.nextMove(boardState, Mark.CROSS));
    }
    @Test
    void testResign2() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 7), Mark.NOUGHT));

        assertThrows(ResignException.class, () -> gomoku.nextMove(boardState, Mark.CROSS));
    }
    @Test
    void testResign3() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(13);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(1, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 4), Mark.NOUGHT));

        boardState.add(new Move(new Position(3, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 10), Mark.NOUGHT));

        boardState.add(new Move(new Position(4, 9), Mark.NOUGHT));

        boardState.add(new Move(new Position(5, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 6), Mark.NOUGHT));

        assertThrows(ResignException.class, () -> gomoku.nextMove(boardState, Mark.CROSS));
    }
    @Test
    void testResign4() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 4), Mark.CROSS));
        boardState.add(new Move(new Position(3, 5), Mark.CROSS));
        boardState.add(new Move(new Position(4, 6), Mark.CROSS));
        boardState.add(new Move(new Position(5, 7), Mark.CROSS));

        assertThrows(ResignException.class, () -> gomoku.nextMove(boardState, Mark.NOUGHT));
    }
}



