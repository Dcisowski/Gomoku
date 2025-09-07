import fais.zti.oramus.gomoku.Mark;

public interface Pattern {
    /** Długość okna (liczba komórek), po którym przesuwamy wzorzec. */
    int span();

    /** Czy wzorzec pasuje do okna linii zaczynającego się od offsetu 'start'. */
    boolean matches(Mark[] line, int start, Mark mine, Mark opp);

    /** Opcjonalne podpowiedzi ruchów wynikające z dopasowania wzorca.
     *  Zgłasza kandydatów do obserwatora (np. końce otwartej czwórki).
     */
    void suggest(LineSlice ctx, int start, Mark mine, Mark opp, MoveObserver obs, MoveType asType);
}