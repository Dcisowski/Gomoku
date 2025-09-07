// NEW: Patterns.java
import fais.zti.oramus.gomoku.Mark;

/** Katalog wzorców z dokumentu (wybrane kluczowe).
 *  Uwaga: wzorce „otwartości” bazują na pustych polach po bokach w tym samym oknie.
 */
public final class Patterns {
    private Patterns(){}

    private static final Pattern EMPTY = new PEmpty();
    private static final Pattern MINE  = new PIsMine();
    private static final Pattern OPP   = new PIsOpp();

    /** OPEN FOUR: E MMMM E */
    public static Pattern openFour(){
        return new SeqPattern(new Pattern[]{
                EMPTY,
                new RepeatPattern(MINE, 4),
                EMPTY
        });
    }

    /** OPEN THREE (prosta): E MMM E */
    public static Pattern openThree(){
        return new SeqPattern(new Pattern[]{
                EMPTY,
                new RepeatPattern(MINE, 3),
                EMPTY
        });
    }

    /** BROKEN FOUR (przykładowy wariant): E MM E MM E */
    public static Pattern brokenFour(){
        return new SeqPattern(new Pattern[]{
                EMPTY,
                new RepeatPattern(MINE, 2),
                EMPTY,
                new RepeatPattern(MINE, 2),
                EMPTY
        });
    }
}
