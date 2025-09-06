package fais.zti.oramus.gomoku;

/**
 * Reprezentacja pojedynczego ruchu w grze. Ruch to połączona pozycja i znak na
 * niej umieszczony.
 */
public record Move(Position position, Mark mark) {
}
