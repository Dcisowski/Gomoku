package fais.zti.oramus.gomoku;

/**
 * Wyjątek zgłaszany, gdy na przedstawionym stanie planszy znajduje się pozycja,
 * w której jeden z graczy wygrał.
 */
public class TheWinnerIsException extends Exception {
	private static final long serialVersionUID = -3405920612853571432L;
	public final Mark mark;

	/**
	 * Konstruktor pozwalający na ustalenie znaku, którym wygrano partię.
	 * 
	 * @param mark Symbol wygrywającego gracza.
	 */
	public TheWinnerIsException(Mark mark) {
		this.mark = mark;
	}
}
