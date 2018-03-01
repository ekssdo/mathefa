public class Parser {
	/**
	 * Die Inputzeile als Zeichenkette
	 */
	private char[] characters;

	/**
	 * Die derzeitige Position des Parsers
	 */
	private int cursor;

	/**
	 *
	 * @param input Die Eingangszeile
	 */
	public Parser(String input) {
		// Eingangszeile in Zeichenkette teilen
		this.characters = input.toCharArray();

		// Es wird bei 0 angefangen zu parsen
		this.cursor = 0;
	}

	/**
	 * Gibt zurück ob weitere Zeichen zum parsen bereitstehen
	 * @return
	 */
	public boolean hasMoreCharacters() {
		return this.cursor < this.characters.length;
	}

	/**
	 * Prüft den nächsten Character
	 * @param character
	 * @return
	 */
	public boolean checkNext(char character) {
		trimInput();

		return hasMoreCharacters() && this.characters[cursor] == character;
	}

	/**
	 * Fordert einen bestimmten Charakter - ansonsten: Fehler werfen.
	 *
	 * @param character
	 * @return
	 */
	public boolean demand(char character) {
		char found = hasMoreCharacters() ? this.characters[cursor++] : ' ';

		if(found != character) throw new RuntimeException("Erwartetes Zeichen: '" + character + "' stattdessen wurde '" + found + "' gefunden an Stelle " + cursor);

		return true;
	}

	/**
	 * Einsprungsfunktion des Parsers
	 *
	 * @return Die Wurzel des Syntaxbaumes
	 */
	public Node parse() {
		// Kein Term vorhanden: Funktion ist undefiniert
		if(!hasMoreCharacters()) return context -> Double.NaN;

		Node root = parseAddition();
		if(hasMoreCharacters()) throw new RuntimeException("Unerwartetes Zeichen: '" + this.characters[cursor] + "' an der Stelle " + cursor);

		return root;
	}

	/**
	 * Überspringt alle Leerzeichen und beginnt bei dem nächsten Zeichen.
	 */
	public void trimInput() {
		while(this.cursor < this.characters.length && this.characters[cursor] == ' ') cursor++;
	}

	/**
	 * 1. Funktion der Funktionskette
	 *
	 * Addition und Subtraktion behandeln
	 * @return
	 */
	public Node parseAddition() {
		// Linke seite
		Node left = parseFactor();

		if(checkNext('+')) {
			demand('+'); // + Zeichen überspringen

			Node right = parseAddition();

			// Addition von "left" und "rechts"
			return context -> left.evaluate(context) + right.evaluate(context);
		} else if(checkNext('-')) {
			demand('-'); // - Zeichen überspringen

			Node right = parseAddition();

			// Subtraktion von "left" und "rechts"
			return context -> left.evaluate(context) - right.evaluate(context);
		}

		return left;
	}

	/**
	 * 2. Funktion der Funktionskette
	 *
	 * Multiplikationen & Divisionen behandeln
	 * @return
	 */
	public Node parseFactor() {
		// Linke seite
		Node left = parseSign();

		if(checkNext('*')) {
			demand('*'); // * Zeichen überspringen

			Node right = parseFactor();

			// Multiplikation zwischen "left" und "rechts"
			return context -> left.evaluate(context) * right.evaluate(context);
		} else if(checkNext('/')) {
			demand('/'); // / Zeichen überspringen

			Node right = parseFactor();

			// Division von "left" mit "rechts"
			return context -> {
				double leftValue = left.evaluate(context);
				double rightValue = right.evaluate(context);

				if(rightValue == 0) return Double.MAX_VALUE; // Dies verhindert, dass man ins Mathe Gefängniss kommt.

				return leftValue / rightValue;
			};
		}

		return left;
	}

	/**
	 * 3. Funktion der Funktionskette
	 *
	 * Vorzeichenwechsel / Negativierung
	 * @return
	 */
	public Node parseSign() {
		if(checkNext('-')) {
			demand('-'); // - Zeichen überspringen

			Node node = parseSign();

			return context -> -node.evaluate(context);
		}

		return parsePower();
	}

	/**
	 * 4. Funktion der Funktionskette
	 *
	 * Potenzen behandeln
	 * @return
	 */
	public Node parsePower() {
		// Linke seite
		Node left = parseValue();

		if(checkNext('^')) {
			demand('^'); // ^ Zeichen überspringen

			Node right = parsePower();

			// Potenzierung von "left" mit "rechts"
			return context -> Math.pow(left.evaluate(context), right.evaluate(context));
		}

		return left;
	}

	/**
	 * 5. Funktion der Funktionskette
	 *
	 * Werte behandeln
	 * Ein Wert kann ein Funktionsaufruf, eine Variable oder ein konstanter Wert sein.
	 * @return
	 */
	public Node parseValue() {
		trimInput();

		if(hasMoreCharacters() && Character.isDigit(characters[cursor])) {
			// Es handelt sich um eine Zahl.

			// Speicherort (Puffer) um die Zahl zu parsen
			StringBuilder buffer = new StringBuilder();

			// Sollange Ziffern folgen, an den Buffer anhängen
			while(hasMoreCharacters() && Character.isDigit(characters[cursor])) buffer.append(characters[cursor++]);

			// Prüfen ob eine Kommazahl vorhanden ist
			if(checkNext('.')) {

				// Das Komma an den Speicher Puffer anhängen
				buffer.append(characters[cursor++]);

				// Sollange Ziffern folgen, an den Buffer anhängen (Nachkommastellen)
				while(hasMoreCharacters() && Character.isDigit(characters[cursor])) buffer.append(characters[cursor++]);
			}

			return context -> Double.parseDouble(buffer.toString());
		} else if(hasMoreCharacters() && Character.isAlphabetic(characters[cursor])) {
			// Es handelt sich um einen Ausdruck (Variable etc.).

			// Speicherort (Puffer) um den Ausdruck zu parsen
			StringBuilder buffer = new StringBuilder();

			// Sollange Buchstaben folgen, an den Buffer anhängen
			while(hasMoreCharacters() && Character.isAlphabetic(characters[cursor])) buffer.append(characters[cursor++]);

			if(hasMoreCharacters() && checkNext('(')) {
				// Es handelt sich um einen Funktionsaufruf
				demand('('); // ( Zeichen überspringen

				// Funktionsparameter auslesen
				Node param = parseSign();

				demand(')'); // ) Zeichen überspringen

				return context -> context.callFunction(buffer.toString(), param.evaluate(context));
			}

			return context -> context.getVariable(buffer.toString());
		}

		return parseParen();
	}

	/**
	 * 6. Funktion der Funktionskette
	 *
	 * Klammern behandeln
	 * @return
	 */
	public Node parseParen() {
		if(checkNext('(')) {
			demand('('); // ( Zeichen überspringen

			Node inner = parseAddition();

			demand(')'); // ) Zeichen überspringen

			return inner;
		}

		trimInput();

		if(hasMoreCharacters()) {
			throw new RuntimeException("Unerwartetes Zeichen '" + characters[cursor] + "' an der Stelle " + cursor);
		} else {
			throw new RuntimeException("Unerwartetes Ende des Terms");
		}
	}
}