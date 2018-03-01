/**
 * Ein Node ist ein Knotenpunkt des Syntaxbaums.
 *
 * Er besitzt die Evaluate Funktion, welche einen Context benötigt.
 * Diese Berechnet den Wert des Knotenpunkts anhand der untergeordneten Knotenpunkte oder des Eigenwertes (Variablen, Konstanten)
 */
public interface Node {
	double evaluate(MathContext context);
}