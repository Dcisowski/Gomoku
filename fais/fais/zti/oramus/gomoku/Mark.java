package fais.zti.oramus.gomoku;

/**
 * Zestaw znaków/symboli używanych w grze wraz z obiektem specjalnego
 * przeznaczenia (NULL). Znak specjalnego przeznaczenie pozwala uniknąć
 * problemu referencji null dla pól, które nie są zajęte.
 */
public enum Mark {
	NOUGHT("o"), CROSS("x"), NULL(".");

	private final String shape;

	private Mark(String shape) {
		this.shape = shape;
	}

	@Override
	public String toString() {
		return shape;
	}
}
