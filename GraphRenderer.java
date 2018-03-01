import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Zeichnet den Graphen anhand einer Funktion
 */
public class GraphRenderer {
	/**
	 * Zeichnet einen Graphen mit gegebener Funktion und Zeichnungsparametern in eine Zeichnung
	 *
	 * @param canvasWidth Die breite der Zeichnung in Pixel
	 * @param canvasHeight Die höhe der Zeichnung in Pixel
	 * @param context Der mathematische Kontext
	 * @param function Der Name der Funktion die gezeichnet werden soll
	 * @param startX Die am weitesten Linke X - Koordinate
	 * @param startY Die unterste Y - Koordinate
	 * @param endX Die am weitesten Rechte X - Koordinate
	 * @param endY Die oberste Y - Koordinate
	 * @return
	 */
	public static BufferedImage drawGraph(int canvasWidth, int canvasHeight, MathContext context, String function, int startX, int startY, int endX, int endY) {
		if(startX > endX) throw new RuntimeException("Die angegebenen X-Werte für die Zeichnung sind ungültig.");
		if(startY > endY) throw new RuntimeException("Die angegebenen Y-Werte für die Zeichnung sind ungültig.");

		// Erstelle die Zeichnung
		BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);

		// Zeichenhelfer
		Graphics graphics = canvas.getGraphics();

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, canvasWidth, canvasHeight);

		// Der Wertebereich
		int lastPixelY = Integer.MAX_VALUE;

		// Werte Tabelle für Pixelkoordinaten
		int[] yValues = new int[canvasWidth];

		for(int pixelX = 0; pixelX < canvasWidth; pixelX++) {
			double progress = ((pixelX * 1D) / (canvasWidth * 1D));

			double xValue = startX + progress * (endX - startX);
			double yValue = context.callFunction(function, xValue);

			// Dreisatz zur Berechnung der Y - Position im Verhältnis zu den Parametern
			double relativeY = (yValue - startY) / (endY - startY);

			int pixelY = (int) Math.round(canvasHeight * relativeY);

			// Werte in die Wertetabelle eintragen. (JavaAWT beginnt oben mit 0, also muss alles gespiegelt werden)
			yValues[pixelX] = canvasHeight - pixelY;
		}

		graphics.setColor(Color.BLACK);

		//Hilfslinien
		graphics.drawRect(0, canvasHeight - (int) (canvasHeight * ((0.0 - startY) / (endY - startY))), canvasWidth, 1);
		graphics.drawRect((int) (canvasWidth * (-startX / (endX * 1.0 - startX * 1.0))), 0, 1, canvasHeight);

		graphics.setColor(Color.RED);
		for(int pixelX = 0; pixelX < canvasWidth - 1; pixelX++) {
			// Der Pixel Wert ist pixelX im Intervall [0, canvasWidth]
			// Der X Wert muss im Intervall [startX, endX] sein
			// D.h. er beginnt bei startX. Wenn PixelX canvasWidth entspricht, muss er bei endX sein.
			// (Da es sich bei pixelX und canvasWidth um Ints handelt ist das * 1D nötig um sie zu einer Gleitkommazahl division zu zwingen)

			// Stellt den Vortschritt der Wertetabelle als Wert im Intervall von [0, 1] da.

			int pixelY1 = yValues[pixelX];
			int pixelY2 = yValues[pixelX + 1];

			// Prüfen ob der Wert im Wertebereich ist
			if((pixelY1 >= 0 && pixelY1 < canvasHeight) || (pixelY2 >= 0 && pixelY2 < canvasHeight)) {

				graphics.drawPolyline(new int[] {pixelX, pixelX + 1}, new int[] {pixelY1, pixelY2}, 2);
			}
		}

		return canvas;
	}
}