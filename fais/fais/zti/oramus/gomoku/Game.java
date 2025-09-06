package fais.zti.oramus.gomoku;

import java.util.Set;

/**
 * Podstawowy interfejs gry.
 */
public interface Game {
	/**
	 * Odpowiada na pytanie jakim znakiem rozpoczęto grę.
	 * 
	 * @param first pierwszy znak na planszy
	 */
	public void firstMark(Mark first);

	/**
	 * Liczba kolumn/wierszy na planszy.
	 * 
	 * @param size rozmiar liniowy planszy do gry.
	 */
	public void size(int size);

	/**
	 * Włączenie (domyślnie wyłączone) periodycznych warunków brzegowych.
	 */
	public void periodicBoundaryConditionsInUse();

	/**
	 * Zlecenie analizy stanu planszy i przedstawienia następnego ruchu (o ile taki
	 * może zostać zaproponowany).
	 * 
	 * @param boardState   stan plaszny w postaci zbioru wykonanych ruchów.
	 * @param nextMoveMark tym znakiem ma zostać wykonany następny ruch.
	 * @return propozycja ruchu
	 * @throws ResignException          poddanie partii - zwycięstwo przeciwnika
	 *                                  jest nieuniknione.
	 * @throws TheWinnerIsException     plansza zawiera pozycje, którą jeden z
	 *                                  graczy już wygrał
	 * @throws WrongBoardStateException stan planszy nie zgadza się z regułami gry
	 */
	public Move nextMove(Set<Move> boardState, Mark nextMoveMark)
			throws ResignException, TheWinnerIsException, WrongBoardStateException;
}
