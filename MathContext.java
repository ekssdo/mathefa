import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Enthält Variablen / Funktionen die für die Evaluierung der Nodes relevant sind.
 */
public class MathContext {
	/**
	 * Map mit Variablen
	 * Der Variablename ist der Schlüssel und der Wert der Variable der Wert in der Map
	 */
	private Map<String, Object> variableMap = new HashMap<>();

	/**
	 * Einsprungsfunktion eines mathematischen Kontext
	 */
	public MathContext() {
		// Konstanten
		setVariable("PI", Math.PI);
		setVariable("e", Math.E);

		// Systemfunktionen
		setNativeFunction("sin",   Math::sin);
		setNativeFunction("cos",   Math::cos);
		setNativeFunction("tan",   Math::tan);
		setNativeFunction("abs",   Math::abs);
		setNativeFunction("sqrt",  Math::sqrt);
		setNativeFunction("log",   Math::log);
		setNativeFunction("log10", Math::log10);
	}

	/**
	 * Definiert eine mathematische Funktion mit dessen Name, Parameter, sowie einen Einsprungsknotenpunkt
	 *
	 * also aus f(x) = x² + 5x wird createFunction("f", "x", "x^2 + 5x");
	 *
	 * @param name Name der Funktion
	 * @param parameter Parametername
	 * @param term Funktionszeile als Zeichenkette
	 */
	public void setFunction(String name, String parameter, String term) {
		// Der Term muss zuerst von dem Parser in ein Syntaxbaum umgerechnet werden.
		Node rootNode = new Parser(term).parse();

		variableMap.put(name, (MathFunction) (context, parameterValue) -> {
			// Jede Funktion läuft in einem eigenen mathematischen Kontext.
			// In der Informatik nennt man dies Scope

			MathContext subContext = new MathContext();

			// Parameter definieren bei f(x) ist dies "x"
			subContext.setVariable(parameter, parameterValue);

			return rootNode.evaluate(subContext);
		});
	}

	/**
	 * Definiert eine Java System Funktion als mathematische Funktion in diesem Context.
	 *
	 * @param name
	 * @param function
	 */
	public void setNativeFunction(String name, Function<Double, Double> function) {
		variableMap.put(name, (MathFunction) (context, parameterValue) -> (double) function.apply(parameterValue));
	}

	/**
	 * Setzt den Wert einer Variable.
	 *
	 * @param name
	 * @param value
	 */
	public void setVariable(String name, double value) {
		variableMap.put(name, value);
	}

	/**
	 * Lese den Wert einer Variable aus.
	 *
	 * @param name
	 * @return
	 */
	public double getVariable(String name) {
		Object value = variableMap.get(name);

		if(value == null) throw new RuntimeException("Die Variable '" + name + "' ist nicht definiert!");
		if(!(value instanceof Double)) throw new RuntimeException("Die Variable '" + name + "' hat keinen Zahlenwert!");

		return (double) value;
	}

	/**
	 * Ruft eine Funktion mit dessen Namen und einem Parameter auf.
	 *
	 * @param name Der Name der Funktion
	 * @param parameterValue Der Parameterwert
	 * @return
	 */
	public double callFunction(String name, double parameterValue) {
		Object value = variableMap.get(name);

		if(value == null) throw new RuntimeException("Die Funktion '" + name + "' ist nicht definiert!");
		if(!(value instanceof MathFunction)) throw new RuntimeException("Die Variable '" + name + "' ist keine Funktion!");

		return ((MathFunction) value).call(this, parameterValue);
	}
}