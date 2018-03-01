/**
 * Eine Mathematische Funktion
 */
public interface MathFunction {
	/**
	 * Führt die Funktion aus.
	 * @param context
	 * @param parameter
	 * @return
	 */
	double call(MathContext context, double parameter);
}