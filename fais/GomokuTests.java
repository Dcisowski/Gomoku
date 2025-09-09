import fais.zti.oramus.gomoku.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class GomokuTests {

    @BeforeEach
    void setup() {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Set<Move> boardState = new HashSet<Move>();
    }

    @Test
    void testWinningMoveDetection() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(15);
        Mark playerMark = Mark.CROSS;
        
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 4; i++) {
            boardState.add(new Move(new Position(5, i), Mark.CROSS));
        }

        for (int i = 0; i < 4; i++) {
            boardState.add(new Move(new Position(7, i+1), Mark.NOUGHT));
        }
        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertEquals(5, move.position().col());
        assertEquals(4, move.position().row());
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testBlockingMoveDetection() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 4; i++) {
            boardState.add(new Move(new Position(i, 0), Mark.NOUGHT));
        }

        for (int i = 0; i < 3; i++) {
            boardState.add(new Move(new Position(i, 5), Mark.CROSS));
        }

        boardState.add(new Move(new Position(8, 8), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertEquals(4, move.position().col());
        assertEquals(0, move.position().row());
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testOpenFourMoveDetection() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<>();

        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));

        boardState.add(new Move(new Position(6, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 8), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && (move.position().row() == 4 || move.position().row() == 2))
                || (move.position().col() == 4 && (move.position().row() == 5 || move.position().row() == 3))
                || (move.position().col() == 5 && (move.position().row() == 6 || move.position().row() == 4))
                );
        assertEquals(move.mark(), playerMark);
        
    }

    @Test
    void testDoubleThreatMoveDetection() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(1, 1), Mark.CROSS));
        boardState.add(new Move(new Position(1, 2), Mark.CROSS));
        boardState.add(new Move(new Position(1, 3), Mark.CROSS));
        boardState.add(new Move(new Position(2, 1), Mark.CROSS));
        boardState.add(new Move(new Position(3, 1), Mark.CROSS));

        boardState.add(new Move(new Position(5, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testWinnerIsExceptionThrown() {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(0, i), Mark.CROSS));
        }
        for (int i = 0; i < 2; i++) {
            boardState.add(new Move(new Position(3, i), Mark.NOUGHT));
        }
        for (int i = 0; i < 3; i++) {
            boardState.add(new Move(new Position(5, i), Mark.NOUGHT));
        }
        assertThrows(TheWinnerIsException.class, () -> gomoku.nextMove(boardState, playerMark));
    }
    @Test
    void testWinnerIsExceptionThrown2() {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(0, i), Mark.CROSS));
        }
        for (int i = 0; i < 3; i++) {
            boardState.add(new Move(new Position(3, i), Mark.NOUGHT));
        }
        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 8), Mark.NOUGHT));

        assertThrows(TheWinnerIsException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testWinnerIsExceptionThrown3() {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);

        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(0, i), Mark.CROSS));
        }
        for (int i = 0; i < 4; i++) {
            boardState.add(new Move(new Position(3, i), Mark.NOUGHT));
        }

        boardState.add(new Move(new Position(7, 0), Mark.NOUGHT));
        assertThrows(TheWinnerIsException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testNoMoveWhenBoardIsEmpty() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertEquals(move.mark(), playerMark);
        // ??????
    }

    @Test
    void testAvoidWrongThreatInterpretation() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);

        Set<Move> boardState = new HashSet<Move>();
        
        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));

        boardState.add(new Move(new Position(9, 1), Mark.CROSS));
        boardState.add(new Move(new Position(9, 3), Mark.CROSS));
        boardState.add(new Move(new Position(9, 6), Mark.CROSS));
        boardState.add(new Move(new Position(9, 9), Mark.CROSS));
        boardState.add(new Move(new Position(7, 3), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertEquals(move.mark(), playerMark);
    }

    // === Test z pkt. 12 – obrona przed natychmiastową wygraną przeciwnika ===
    @Test
    void testDefendAgainstImmediateLoss() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 3), Mark.CROSS));
        boardState.add(new Move(new Position(2, 3), Mark.CROSS));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 3), Mark.NOUGHT));

        boardState.add(new Move(new Position(3, 7), Mark.CROSS));
        boardState.add(new Move(new Position(4, 7), Mark.CROSS));
        boardState.add(new Move(new Position(6, 7), Mark.CROSS));
        boardState.add(new Move(new Position(8, 7), Mark.CROSS));
        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertTrue((move.position().col() == 7 && move.position().row() == 3));
        assertEquals(move.mark(), playerMark);
    }

    // === Test z pkt. 13 – błąd: błędny ruch nie zapewnia wygranej ===
    @Test
    void testAvoidWrongMoveChoice() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(0, 1), Mark.CROSS));
        boardState.add(new Move(new Position(1, 1), Mark.CROSS));
        boardState.add(new Move(new Position(2, 1), Mark.CROSS));
        boardState.add(new Move(new Position(3, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 3), Mark.CROSS));

        boardState.add(new Move(new Position(6, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 2)
                || (move.position().col() == 2 && move.position().row() == 2)
        );
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testChooseFastestWinningPath() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(2, 2), Mark.CROSS));
        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));

        boardState.add(new Move(new Position(2, 5), Mark.CROSS));
        boardState.add(new Move(new Position(3, 5), Mark.CROSS));
        boardState.add(new Move(new Position(4, 5), Mark.CROSS));

        boardState.add(new Move(new Position(6, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(6, 8), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 5), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        // powinna być kontynuowana ścieżka prowadząca do natychmiastowego zwycięstwa
        assertTrue((
                move.position().col() == 6 && move.position().row() == 6)
                || (move.position().col() == 1 && move.position().row() == 1)
                || (move.position().col() == 6 && move.position().row() == 5)
                || (move.position().col() == 1 && move.position().row() == 5)
        );
        assertEquals(move.mark(), playerMark);
    }

    // === Punkt 9: gracz x powinien zablokować kółko (które dąży do 4) ===
    @Test
    void testBlockOpponentWinFromThree() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        Set<Move> boardState = new HashSet<>();
        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 4), Mark.NOUGHT));

        boardState.add(new Move(new Position(4, 7), Mark.CROSS));
        boardState.add(new Move(new Position(8, 1), Mark.CROSS));
        boardState.add(new Move(new Position(9, 9), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);
        assertNotNull(move);
        assertTrue((move.position().col() == 1 && move.position().row() == 1) || (move.position().col() == 5 && move.position().row() == 5));
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testDefendAgainstImmediateWinInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);
        gomoku.periodicBoundaryConditionsInUse();

        Set<Move> periodicboardState = new HashSet<Move>();

        periodicboardState.add(new Move(new Position(0, 9), Mark.CROSS));
        periodicboardState.add(new Move(new Position(2, 1), Mark.CROSS));
        periodicboardState.add(new Move(new Position(3, 2), Mark.CROSS));
        periodicboardState.add(new Move(new Position(4, 3), Mark.CROSS));

        periodicboardState.add(new Move(new Position(6, 9), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(7, 4), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(8, 6), Mark.NOUGHT));

        periodicboardState.add(new Move(new Position(7, 5), Mark.NOUGHT));


        Move move = gomoku.nextMove(periodicboardState, playerMark);

        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 1 && move.position().row() == 0));
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testWinInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        gomoku.periodicBoundaryConditionsInUse();

        Set<Move> periodicboardState = new HashSet<Move>();

        periodicboardState.add(new Move(new Position(2, 1), Mark.CROSS));
        periodicboardState.add(new Move(new Position(3, 0), Mark.CROSS));
        periodicboardState.add(new Move(new Position(4, 9), Mark.CROSS));
        periodicboardState.add(new Move(new Position(5, 8), Mark.CROSS));

        periodicboardState.add(new Move(new Position(7, 5), Mark.CROSS));

        periodicboardState.add(new Move(new Position(8, 9), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(7, 4), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(7, 7), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(8, 6), Mark.NOUGHT));

        periodicboardState.add(new Move(new Position(9, 3), Mark.NOUGHT));


        Move move = gomoku.nextMove(periodicboardState, playerMark);

        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 1 && move.position().row() == 2) || (move.position().col() == 6 && move.position().row() == 7));
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testWinOverBlockInPeriodicMode() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        gomoku.periodicBoundaryConditionsInUse();

        Set<Move> periodicboardState = new HashSet<Move>();

        periodicboardState.add(new Move(new Position(4, 0), Mark.CROSS));
        periodicboardState.add(new Move(new Position(5, 9), Mark.CROSS));
        periodicboardState.add(new Move(new Position(6, 8), Mark.CROSS));

        periodicboardState.add(new Move(new Position(7, 5), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(7, 6), Mark.NOUGHT));
        periodicboardState.add(new Move(new Position(7, 4), Mark.NOUGHT));


        Move move = gomoku.nextMove(periodicboardState, playerMark);

        assertNotNull(move);
        // blokuje wygraną x w trybie periodycznym
        assertTrue((move.position().col() == 7 && move.position().row() == 7));
        assertEquals(move.mark(), playerMark);
    }

    // === Punkt 13: niepoprawny ruch oznaczony ? nie prowadzi do zwycięstwa ===
    @Test
    void testRejectIncorrectMoveMarkedAsQuestion() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
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

        Move move = gomoku.nextMove(boardState, playerMark);
        
        assertNotNull(move);
        // wybierany powinien być skuteczny ruch, nie przypadkowy błędny
        assertNotEquals(3, move.position().col());
        assertNotEquals(1, move.position().row());
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testNoVictoryWithoutSpace() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
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

        boardState.add(new Move(new Position(8, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);
        // mimo 5 'x', „kółko” wygrało – sprawdzamy, czy poprawnie odrzucone
        assertNotNull(move);
        assertTrue((move.position().col() == 1 && move.position().row() == 6) || (move.position().col() == 1 && move.position().row() == 1));
        assertEquals(move.mark(), playerMark);
    }

    // === Punkt 6: gra nie współpracuje – należy wybrać najlepszy atak zamiast błędnego ===
    @Test
    void testAttackOverridesIncorrectBlock() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        Set<Move> boardState = new HashSet<Move>();
        
        boardState.add(new Move(new Position(2, 2), Mark.CROSS));
        boardState.add(new Move(new Position(3, 3), Mark.CROSS));
        boardState.add(new Move(new Position(4, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));

        boardState.add(new Move(new Position(2, 6), Mark.CROSS));
        boardState.add(new Move(new Position(3, 6), Mark.CROSS));
        boardState.add(new Move(new Position(4, 6), Mark.CROSS));

        boardState.add(new Move(new Position(9, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));

        boardState.add(new Move(new Position(7, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);
        
        // powinien wybrać dokończenie zwycięskiej linii (nie blokować innych)
        assertNotNull(move);
        assertTrue((move.position().col() == 6 && move.position().row() == 6) || (move.position().col() == 1 && move.position().row() == 1));
        assertEquals(move.mark(), playerMark);
    }

    //
    // === Punkt 7: plansza już zawiera zwycięstwo – powinien być wyjątek ===
    @Test
    void testWinnerAlreadyExistsThrowsException() {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        Set<Move> boardState = new HashSet<Move>();
        
        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(5, i), Mark.NOUGHT));
        }

        for (int i = 0; i < 3; i++) {
            boardState.add(new Move(new Position(3, i), Mark.CROSS));
        }

        for (int i = 0; i < 2; i++) {
            boardState.add(new Move(new Position(9, i), Mark.CROSS));
        }

        assertThrows(TheWinnerIsException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        Set<Move> boardState = new HashSet<Move>();
        
        boardState.add(new Move(new Position(0, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 4), Mark.CROSS));
        boardState.add(new Move(new Position(0, 5), Mark.CROSS));
        boardState.add(new Move(new Position(0, 7), Mark.CROSS));
        boardState.add(new Move(new Position(0, 9), Mark.NOUGHT));

        boardState.add(new Move(new Position(8, 1), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue((move.position().col() == 0 && move.position().row() == 6));
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testFastestLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        gomoku.size(10);
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);

        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(1, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 6), Mark.NOUGHT));

        boardState.add(new Move(new Position(9, 3), Mark.CROSS));
        boardState.add(new Move(new Position(9, 6), Mark.CROSS));
        boardState.add(new Move(new Position(7, 6), Mark.CROSS));
        boardState.add(new Move(new Position(7, 9), Mark.CROSS));
        boardState.add(new Move(new Position(8, 1), Mark.CROSS));
        boardState.add(new Move(new Position(8, 8), Mark.CROSS));
        boardState.add(new Move(new Position(0, 0), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 7)
                        || (move.position().col() == 1 && move.position().row() == 3)
        );
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testBlockingLeadingToWinning() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        gomoku.size(10);

        Set<Move> boardState = new HashSet<>();
        boardState.add(new Move(new Position(1, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 7), Mark.CROSS));
        boardState.add(new Move(new Position(4, 7), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));
        boardState.add(new Move(new Position(5, 6), Mark.CROSS));

        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 7)
                        || (move.position().col() == 1 && move.position().row() == 3)
        );
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testBlockingLeadingToWinningComplex() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);
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
        boardState.add(new Move(new Position(5, 0), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(move.position().col() == 5 && move.position().row() == 1);
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testCorrectMove() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
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

        boardState.add(new Move(new Position(0, 9), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 2), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(move.position().col() == 6 && move.position().row() == 5);
        assertEquals(move.mark(), playerMark);
    }

    //
    @Test
    void testIncorrectMove() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);
        gomoku.size(10);

        Set<Move> boardState = new HashSet<>();
        boardState.add(new Move(new Position(1, 0), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 2), Mark.NOUGHT));

        boardState.add(new Move(new Position(5, 0), Mark.CROSS));
        boardState.add(new Move(new Position(7, 9), Mark.CROSS));
        boardState.add(new Move(new Position(4, 5), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 1)
                        || (move.position().col() == 2 && move.position().row() == 0)
                        || (move.position().col() == 2 && move.position().row() == 2)
        );
        assertEquals(move.mark(), playerMark);
    }
    @Test
    void testCorrectDoubleFour() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);
        gomoku.size(10);

        Set<Move> boardState = new HashSet<>();
        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 5), Mark.NOUGHT));

        boardState.add(new Move(new Position(5, 9), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 8), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 7), Mark.NOUGHT));

        boardState.add(new Move(new Position(9, 0), Mark.CROSS));
        boardState.add(new Move(new Position(9, 9), Mark.CROSS));
        boardState.add(new Move(new Position(9, 5), Mark.CROSS));

        boardState.add(new Move(new Position(0, 9), Mark.CROSS));
        boardState.add(new Move(new Position(0, 4), Mark.CROSS));
        boardState.add(new Move(new Position(0, 0), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 4)
        );
        assertEquals(move.mark(), playerMark);
    }
    @Test
    void testPriorityWinning() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(2, 2), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 6), Mark.NOUGHT));

        boardState.add(new Move(new Position(4, 6), Mark.CROSS));
        boardState.add(new Move(new Position(5, 6), Mark.CROSS));
        boardState.add(new Move(new Position(7, 6), Mark.CROSS));
        boardState.add(new Move(new Position(8, 6), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 4)
        );
        assertEquals(move.mark(), playerMark);
    }
    @Test
    void testPriority2() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 4), Mark.CROSS));
        boardState.add(new Move(new Position(3, 5), Mark.CROSS));
        boardState.add(new Move(new Position(4, 6), Mark.CROSS));
        boardState.add(new Move(new Position(5, 7), Mark.CROSS));

        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 1 && move.position().row() == 3)
                || (move.position().col() == 6 && move.position().row() == 8)
        );
        assertEquals(move.mark(), playerMark);

//        MoveAnalyzer analyzer2 = new MoveAnalyzer();
//
//        assertThrows(ResignException.class, () -> analyzer2.analyze(board, Mark.NOUGHT));

    }
    @Test
    void testPriority3() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 8), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 3), Mark.CROSS));
        boardState.add(new Move(new Position(3, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 7), Mark.CROSS));
        boardState.add(new Move(new Position(5, 8), Mark.CROSS));

        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 0 && move.position().row() == 5)
//                        || (move.position().col() == 8 && move.position().row() == 6)
        );
        assertEquals(move.mark(), playerMark);

    }
    @Test
    void testPriority4() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);

        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 5), Mark.CROSS));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 8), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 3), Mark.CROSS));
        boardState.add(new Move(new Position(3, 4), Mark.CROSS));
        boardState.add(new Move(new Position(4, 7), Mark.CROSS));
        boardState.add(new Move(new Position(4, 8), Mark.CROSS));

        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 0), Mark.NOUGHT));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 4 && move.position().row() == 5)
                        || (move.position().col() == 4 && move.position().row() == 6)
        );
        assertEquals(move.mark(), playerMark);

    }
    @Test
    void testPriority5() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));

        boardState.add(new Move(new Position(1, 8), Mark.CROSS));
        boardState.add(new Move(new Position(2, 8), Mark.CROSS));
        boardState.add(new Move(new Position(3, 8), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 4 && move.position().row() == 8)
        );
        assertEquals(move.mark(), playerMark);

    }
    @Test
    void testPriority6() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        gomoku.size(10);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(1, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 3), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 3), Mark.NOUGHT));

        boardState.add(new Move(new Position(0, 8), Mark.CROSS));
        boardState.add(new Move(new Position(1, 8), Mark.CROSS));
        boardState.add(new Move(new Position(2, 8), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 4 && move.position().row() == 3)
        );
        assertEquals(move.mark(), playerMark);

    }
    @Test
    void testPriority15_2_AisCross_BisNought_blockColumnEnd() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
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

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 1)
                || (move.position().col() == 7 && move.position().row() == 5)
        );
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testBlockDoubleFourAndThree() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
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

        boardState.add(new Move(new Position(9, 9), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 1)
        );
        assertEquals(move.mark(), playerMark);
    }

    @Test
    void testPriority15_2_AisNought_BisCross_blockColumnEnd() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);
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

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 3 && move.position().row() == 1)     // bottom end of B-run
                || (move.position().col() == 7 && move.position().row() == 5)
        );
        assertEquals(move.mark(), playerMark);
    }
    @Test
    void testNoSpaceNoWin() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);
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

        boardState.add(new Move(new Position(9, 9), Mark.CROSS));
        boardState.add(new Move(new Position(9, 7), Mark.CROSS));
        boardState.add(new Move(new Position(9, 4), Mark.CROSS));
        boardState.add(new Move(new Position(9, 1), Mark.CROSS));
        boardState.add(new Move(new Position(7, 0), Mark.CROSS));
        boardState.add(new Move(new Position(7, 8), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 5)
        );
        assertEquals(move.mark(), playerMark);

    }
    @Test
    void testNoSpaceNoWin2() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.firstMark(Mark.NOUGHT);
        gomoku.size(10);

        Set<Move> boardState = new HashSet<>();
        boardState.add(new Move(new Position(0, 1), Mark.NOUGHT));

        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 1), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(4, 5), Mark.NOUGHT));

        boardState.add(new Move(new Position(9, 9), Mark.CROSS));
        boardState.add(new Move(new Position(9, 7), Mark.CROSS));
        boardState.add(new Move(new Position(9, 4), Mark.CROSS));
        boardState.add(new Move(new Position(9, 1), Mark.CROSS));
        boardState.add(new Move(new Position(7, 0), Mark.CROSS));
        boardState.add(new Move(new Position(7, 8), Mark.CROSS));
        boardState.add(new Move(new Position(7, 5), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 2 && move.position().row() == 5)
        );
        assertEquals(move.mark(), playerMark);

    }
    @Test
    void testNoSpaceNoWin2BlocDoubleThree() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
        gomoku.size(15);

        Set<Move> boardState = new HashSet<Move>();
        boardState.add(new Move(new Position(0, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(1, 1), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 1), Mark.NOUGHT));

        boardState.add(new Move(new Position(7, 11), Mark.NOUGHT));
        boardState.add(new Move(new Position(7, 12), Mark.NOUGHT));
        boardState.add(new Move(new Position(8, 10), Mark.NOUGHT));
        boardState.add(new Move(new Position(9, 10), Mark.NOUGHT));

        boardState.add(new Move(new Position(14, 14), Mark.CROSS));
        boardState.add(new Move(new Position(14, 12), Mark.CROSS));
        boardState.add(new Move(new Position(14, 9), Mark.CROSS));
        boardState.add(new Move(new Position(14, 6), Mark.CROSS));
        boardState.add(new Move(new Position(12, 5), Mark.CROSS));
        boardState.add(new Move(new Position(12, 13), Mark.CROSS));
        boardState.add(new Move(new Position(12, 10), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 7 && move.position().row() == 10)
        );
        assertEquals(move.mark(), playerMark);

    }
    @Test
    void testNoSurrender() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
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

        boardState.add(new Move(new Position(0, 0), Mark.CROSS));
        boardState.add(new Move(new Position(0, 5), Mark.CROSS));

        Move move = gomoku.nextMove(boardState, playerMark);

        assertNotNull(move);
        assertTrue(
                (move.position().col() == 7 && move.position().row() == 5)
        );
        assertEquals(move.mark(), playerMark);

    }
    @Test
    void testResign() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.firstMark(Mark.CROSS);
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

        boardState.add(new Move(new Position(5, 0), Mark.CROSS));
        boardState.add(new Move(new Position(5, 2), Mark.CROSS));
        boardState.add(new Move(new Position(5, 4), Mark.CROSS));
        boardState.add(new Move(new Position(5, 6), Mark.CROSS));

        boardState.add(new Move(new Position(5, 8), Mark.CROSS));
        boardState.add(new Move(new Position(0, 0), Mark.CROSS));
        boardState.add(new Move(new Position(0, 2), Mark.CROSS));
        boardState.add(new Move(new Position(0, 4), Mark.CROSS));

        assertThrows(ResignException.class, () -> gomoku.nextMove(boardState, playerMark));
    }
    @Test
    void testResign2() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.size(10);
        gomoku.firstMark(Mark.CROSS);

        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 7), Mark.NOUGHT));

        boardState.add(new Move(new Position(9, 9), Mark.CROSS));
        boardState.add(new Move(new Position(7, 6), Mark.CROSS));
        boardState.add(new Move(new Position(9, 5), Mark.CROSS));
        boardState.add(new Move(new Position(9, 3), Mark.CROSS));
        boardState.add(new Move(new Position(5, 5), Mark.CROSS));
        boardState.add(new Move(new Position(7, 3), Mark.CROSS));

        assertThrows(ResignException.class, () -> gomoku.nextMove(boardState, playerMark));
    }
    @Test
    void testResign3() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.size(13);
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<>();

        boardState.add(new Move(new Position(1, 4), Mark.NOUGHT));
        boardState.add(new Move(new Position(2, 4), Mark.NOUGHT));

        boardState.add(new Move(new Position(3, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(3, 10), Mark.NOUGHT));

        boardState.add(new Move(new Position(4, 9), Mark.NOUGHT));

        boardState.add(new Move(new Position(5, 7), Mark.NOUGHT));
        boardState.add(new Move(new Position(5, 6), Mark.NOUGHT));


        boardState.add(new Move(new Position(0, 1), Mark.CROSS));
        boardState.add(new Move(new Position(3, 1), Mark.CROSS));
        boardState.add(new Move(new Position(5, 1), Mark.CROSS));
        boardState.add(new Move(new Position(8, 1), Mark.CROSS));
        boardState.add(new Move(new Position(10, 1), Mark.CROSS));
        boardState.add(new Move(new Position(12, 2), Mark.CROSS));
        boardState.add(new Move(new Position(12, 5), Mark.CROSS));
        boardState.add(new Move(new Position(12, 8), Mark.CROSS));

        assertThrows(ResignException.class, () -> gomoku.nextMove(boardState, playerMark));
    }
    @Test
    void testResign4() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.size(10);
        gomoku.firstMark(Mark.NOUGHT);
        Set<Move> boardState = new HashSet<Move>();

        boardState.add(new Move(new Position(0, 5), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 6), Mark.NOUGHT));
        boardState.add(new Move(new Position(0, 7), Mark.NOUGHT));

        boardState.add(new Move(new Position(2, 4), Mark.CROSS));
        boardState.add(new Move(new Position(3, 5), Mark.CROSS));
        boardState.add(new Move(new Position(4, 6), Mark.CROSS));
        boardState.add(new Move(new Position(5, 7), Mark.CROSS));

        boardState.add(new Move(new Position(9, 9), Mark.NOUGHT));

        assertThrows(ResignException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testWrongBoardState1() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.size(10);
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(i, 5), Mark.CROSS));
        }

        assertThrows(WrongBoardStateException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testWrongBoardState2() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.size(10);
        gomoku.firstMark(Mark.NOUGHT);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(i, 5), Mark.CROSS));
        }

        assertThrows(WrongBoardStateException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testWrongBoardState3() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.size(10);
        gomoku.firstMark(Mark.NOUGHT);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 11; i++) {
            boardState.add(new Move(new Position(i, 5), Mark.CROSS));
        }


        assertThrows(WrongBoardStateException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testWrongBoardState4() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.size(10);
        gomoku.firstMark(Mark.NOUGHT);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(i, 5), Mark.CROSS));
        }

        boardState.add(new Move(new Position(3, 5), Mark.CROSS));

        assertThrows(WrongBoardStateException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testWrongBoardState5() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.NOUGHT;
        gomoku.size(10);
        gomoku.firstMark(Mark.NOUGHT);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 5; i++) {
            boardState.add(new Move(new Position(i, 5), Mark.CROSS));
        }

        boardState.add(new Move(new Position(3, 5), Mark.NOUGHT));

        assertThrows(WrongBoardStateException.class, () -> gomoku.nextMove(boardState, playerMark));
    }
    @Test
    void testNoWrongBoardState() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.size(10);
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 4; i++) {
            boardState.add(new Move(new Position(i, 0), Mark.CROSS));
        }

        for (int i = 0; i < 3; i++) {
            boardState.add(new Move(new Position(i, 5), Mark.NOUGHT));
        }

        assertThrows(WrongBoardStateException.class, () -> gomoku.nextMove(boardState, playerMark));
    }

    @Test
    void testNoWrongBoardState2() throws TheWinnerIsException, WrongBoardStateException, ResignException {
        Gomoku gomoku = new Gomoku();
        Mark playerMark = Mark.CROSS;
        gomoku.size(10);
        gomoku.firstMark(Mark.CROSS);
        Set<Move> boardState = new HashSet<Move>();

        for (int i = 0; i < 3; i++) {
            boardState.add(new Move(new Position(i, 0), Mark.CROSS));
        }

        for (int i = 0; i < 2; i++) {
            boardState.add(new Move(new Position(i, 5), Mark.NOUGHT));
        }

        assertThrows(WrongBoardStateException.class, () -> gomoku.nextMove(boardState, playerMark));

    }


}



