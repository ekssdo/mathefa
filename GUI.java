import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.image.BufferedImage;

/**
 * Die ist das Graphical User Interface, welches die Benutzeroberfläche des Funktionsplotters bildet.
 *
 * Da dieser Teil für den eigentlichen Funktionsplotter nicht
 * relevant ist und lediglich eine möglichkeit der bedienbarkeit ist,
 * wird er in der Facharbeit nicht erklärt.
 */
public class GUI extends JFrame {
	private final JLabel inputPrefix;
	private final JTextField inputField;
	private final JLabel infoLine;
	private final JLabel graphCanvas;

	private final JLabel startLabel;
	private final JSpinner startX;
	private final JSpinner startY;
	private final JLabel endLabel;
	private final JSpinner endX;
	private final JSpinner endY;

	// Der mathematische Kontext in dem die Benutzeroberfläche agiert
	private final MathContext context;

	public GUI() {
		this.inputPrefix = new JLabel("f(x) = ");
		this.inputField = new JTextField();
		this.infoLine = new JLabel();
		this.graphCanvas = new JLabel();
		this.startLabel = new JLabel("Minimalpunkt: ");
		this.endLabel = new JLabel("Maximalpunkt: ");

		this.startX = new JSpinner();
		this.startY = new JSpinner();
		this.endX = new JSpinner();
		this.endY = new JSpinner();

		this.startX.addChangeListener(event -> drawGraph());
		this.startY.addChangeListener(event -> drawGraph());
		this.endX.addChangeListener(event -> drawGraph());
		this.endY.addChangeListener(event -> drawGraph());

		this.startX.setValue(-100);
		this.startY.setValue(-100);
		this.endX.setValue(100);
		this.endY.setValue(100);

		// Den mathematischen Kontext erstellen.
		this.context = new MathContext();

		this.inputField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				drawGraph();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				drawGraph();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				drawGraph();
			}
		});

		add(this.inputField);
		add(this.inputPrefix);
		add(this.infoLine);
		add(this.graphCanvas);
		add(this.startLabel);
		add(this.startX);
		add(this.startY);
		add(this.endLabel);
		add(this.endX);
		add(this.endY);

		setSize(720, 480);

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setLayout(null);
		updateLayout();

		setVisible(true);
	}

	/**
	 * Zeichnet den Graphen auf die Benutzeroberfläche
	 */
	public void drawGraph() {
		try {
			// Definiere f(x) mit dem Term der im InputField definiert ist.
			context.setFunction("f", "x", inputField.getText());

			// Zeichne den Graphen in ein Bild
			BufferedImage graph = GraphRenderer.drawGraph(graphCanvas.getWidth(), graphCanvas.getHeight(), context, "f", (int) startX.getValue(), (int) startY.getValue(), (int) endX.getValue(), (int) endY.getValue());

			// Fügt dieses Bild der Benutzeroberfläche hinzu.
			graphCanvas.setIcon(new ImageIcon(graph));

			if(inputField.getText().isEmpty()) {
				infoLine.setText("Warte auf eingabe...");
			} else {
				infoLine.setText("f(x) = " + inputField.getText());
			}
		} catch(Throwable exception) {
			// Es gab beim Parsen einen Fehler.
			// Ausgabe über Infozeile

			infoLine.setText(exception.getMessage());
		}
	}

	@Override
	public void validate() {
		super.validate();

		SwingUtilities.invokeLater(this::updateLayout);
	}

	/**
	 * Baut das Layout des Funktionsplotters zusammen
	 */
	public void updateLayout() {
		// Komponenten sind Vertical angerichtet.
		int y = 0;

		// Eingangsterm
		inputPrefix.setBounds(2, 0, 32, 25);
		inputField.setBounds(32, y, getWidth(), 25);
		y += 30;

		// Minimal und Maximalpunkte
		int spinnerWidth = 75;
		int spinnerBorder = 6;

		int x = 2;

		startLabel.setBounds(x, y, 90, 30);
		x += 90;

		startX.setBounds(x, y, spinnerWidth, 30);
		x += spinnerWidth + spinnerBorder;
		startY.setBounds(x, y, spinnerWidth, 30);
		x += spinnerWidth + spinnerBorder;

		x += 20;

		endLabel.setBounds(x, y, 90, 30);
		x += 90;

		endX.setBounds(x, y, spinnerWidth, 30);
		x += spinnerWidth + spinnerBorder;
		endY.setBounds(x, y, spinnerWidth, 30);

		y += 30;

		// Textausgabe
		infoLine.setBounds(2, y, getWidth(), 30);
		y += 30;

		graphCanvas.setBounds(0, y, getWidth(), getHeight() - y);

		drawGraph();
	}
}