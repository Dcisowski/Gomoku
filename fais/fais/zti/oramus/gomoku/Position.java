package fais.zti.oramus.gomoku;

/**
 * Pozycja na planszy do gry. Dozwolone położenia dla kolumn i wierszy są z
 * zakresu od 0 do (rozmiaru planszy - 1). Dla ustalenia uwagi pozycja (0,0)
 * odpowiada lewemu-górnemu rogowi planszy (tak jak we współrzędnych
 * ekranowych).
 */
public record Position(int col, int row) {
}
