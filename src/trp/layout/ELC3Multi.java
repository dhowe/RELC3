package trp.layout;

import static trp.util.Direction.*;
import rita.*;

import java.util.*;

import processing.core.*;
import trp.behavior.*;
import trp.reader.*;
import trp.util.*;

public class ELC3Multi extends MultiPageApplet {

	static final String[] TEXTS = { "textual/poeticCaption.txt", "textual/misspeltLandings.txt", "textual/image.txt" };
	static final String[] READER_NAMES = { "Perigram", "Less Directed Perigram", "Simple Spawner", "Perigram Spawner", "Less Directed Spawner", "Mesostic Jumper" };
	static final String[] TEXT_NAMES = { "POETIC CAPTION", "MISSPELT LANDINGS", "THE IMAGE" };
	static final String[] MESOSTICS = { "reading as writing through", "reaching out falling through circling over landing on turning within spelling as", "comes in is over goes out is done lolls in stays there is had no more", "reading as writing through" };

	static Map SPEED_MAP, COLOR_MAP;
	static ReaderBehavior[] BEHAVIORS, TRAILS, HALOING;
	static PerigramLookup[] PERIGRAMS;
	static PFont[] FONTS;

	ReaderBehavior neighborFading, defaultVisuals, tendrilsDGray, neighborFadingNT, haloing, mesostic, mesoHaloing;
	SpawnDirectionalPRs spawningSE, spawningNE, spawningVB;
	ButtonSelect titleSelect, textSelect, readerSelect, speedSelect, spawnSelect, visualSelect, colorSelect;
	float readerColor[] = OATMEAL, readerSpeed = FLUENT;
	int gridFillInt = WHITE_INT, gridAlpha = 40;
	RiTextGrid verso, recto;
	PFont info;

	private String[] textContents;

	public void settings() {

		// fullScreen();
		size(1280, 720);
	}

	public void setup() {

		fontSetup();
		colorSetup();
		buttonSetup();
		loadTextData();
		resetButtons();
		doLayout(0); // 0: Poetic Caption
		constructReadersFor(PERIGRAMS[0], 0);
		spawnSelect.disabled = !isSpawner(currentReaderIdx);
	}

	public void draw() {

		background(LAYOUT_BACKGROUND_COLOR);

		// bit of a KLUDGE: + 14 is for labels:
		if ((mouseY < height) && (mouseY > (height - (titleSelect.height + 14)))) {
			ButtonSelect.drawAll(mouseX, mouseY);

			String word = MachineReader.stripPunctuation(getCurrentReader(currentReaderIdx).getCurrentCell().text());
			showCurrentWord(word);

			if (PRESENTATION_MODE) cursor();
		}
		else {
			if (PRESENTATION_MODE) noCursor();

			drawDog_ear();
		}

		pManager.draw(g);
	}

	protected void drawDog_ear() {

		strokeWeight(2);
		stroke(0);
		fill(191);
		triangle(width - 16, height, width, height - 16, width, height);
	}

	private void loadTextData() {

		long ts = System.currentTimeMillis();
		textContents = Readers.loadFiles(TEXTS);
		PERIGRAMS = new PerigramLookup[TEXTS.length];
		String[][] trigramData = Readers.loadTrigramsFiles(Readers.guessFileNames(TEXTS));
		for (int i = 0; i < trigramData.length; i++) {
			PERIGRAMS[i] = new PerigramLookup(textContents[i], trigramData[i]);
		}
		Readers.info("Load texts/metadata in " + RiTa.elapsed(ts) + " s");
	}

	private void doLayout(int textIndex) {

		if (pManager == null) {
			pManager = PageManager.create(this, 40, 40, 38, 38);
			// bottom marg was 30, adjust for Beckett
			pManager.showPageNumbers(false);
			pManager.setApplicationId("elc3");
			pManager.decreaseGutterBy(20);
		}

		pManager.clear();
		pManager.setLeading(30);
		pManager.setFont(FONTS[textIndex]);
		pManager.addTextFromFile(TEXTS[textIndex]);
		pManager.doLayout();

	}

	public void mouseClicked() {

		if (pManager.isFlipping()) return;

		ButtonSelect clicked = ButtonSelect.click(mouseX, mouseY, isShiftDown());
		if (clicked != null) {

			// TITLE
			if (clicked == titleSelect) {
				resetText(textIdxFromName(clicked.value()));
			}

			// TEXT (fill)
			else if (clicked == textSelect) {
				switch (textSelect.value()) {
					case "Gray":
						gridAlpha = 80;
						break;

					case "Dark":
						gridAlpha = 0;
						break;

					default:
						gridAlpha = 40;
						break;
				}
				setGridFill(gridAlpha);
				pManager.showAll();
				resetText(textIdxFromName(titleSelect.value()));
//				verso = getCurrentReader(currentReaderIdx).getGrid();
//				verso.reset();
//				constructBehaviorsFor(PERIGRAMS[textIdxFromName(titleSelect.value())]);
//				setVisuals(visualSelect.value(), readerColor, isSpawner(currentReaderIdx));
			}

			// READER
			else if (clicked == readerSelect) {

				MachineReader rd = getCurrentReader(currentReaderIdx);
				RiText rt = rd.getCurrentCell();
				rd.pause(true);

				currentReaderIdx = readerIdxFromName(clicked.value());
				rd = getCurrentReader(currentReaderIdx);
				rd.getGrid().reset(); // rest the current grid on reader change

				// System.out.println("click: "+clicked.value()+", "+currentReaderIdx);
				readerSpeed = (float) SPEED_MAP.get(speedSelect.value());
				rd.setSpeed(readerSpeed, true); // alsoResetOriginalSpeed
				rd.setCurrentCell(rt);
				rd.pause(false);

				setVisuals(visualSelect.value(), readerColor, isSpawner(currentReaderIdx));
				spawnSelect.disabled = !isSpawner(currentReaderIdx);

				pManager.onUpdateFocusedReader(rd);
			}

			// COLOR - of reader
			else if (clicked == colorSelect) {
				readerColor = (float[]) COLOR_MAP.get(clicked.value());
				BEHAVIORS[currentReaderIdx].setReaderColor(readerColor);
			}

			// SPEED
			else if (clicked == speedSelect) {
				readerSpeed = (float) SPEED_MAP.get(clicked.value());
				getCurrentReader(currentReaderIdx).setSpeed(readerSpeed, true); // alsoResetOriginalSpeed
				BEHAVIORS[currentReaderIdx].adjustForReaderSpeed(readerSpeed);
				if (isSpawner(currentReaderIdx)) {
					switch (spawnSelect.value()) {
						case "South-East":
							spawningSE.adjustForReaderSpeed(readerSpeed);
							break;

						case "North-East":
							spawningNE.adjustForReaderSpeed(readerSpeed);
							break;

						default:
							spawningVB.adjustForReaderSpeed(readerSpeed);
							break;
					}
					if (readerSpeed == FAST) tendrilsDGray.adjustForReaderSpeed(FAST / 2);
					if (readerSpeed == FLUENT)
						tendrilsDGray.adjustForReaderSpeed(FAST);
					else
						tendrilsDGray.adjustForReaderSpeed(FLUENT);
				}
			}

			// VISUALS
			else if (clicked == spawnSelect || clicked == visualSelect) {
				setVisuals(clicked.value(), readerColor, isSpawner(currentReaderIdx));
			}

		}
	}

	protected void resetText(int titleIndex) {

		pauseReaders();
		doLayout(titleIndex);
		constructReadersFor(PERIGRAMS[titleIndex], titleIndex);
		setVisuals(visualSelect.value(), readerColor, isSpawner(currentReaderIdx));
		spawnSelect.disabled = !isSpawner(currentReaderIdx);
		readerSpeed = (float) SPEED_MAP.get(speedSelect.value());
		getCurrentReader(currentReaderIdx).setSpeed(readerSpeed, true); // alsoResetOriginalSpeed
		BEHAVIORS[currentReaderIdx].adjustForReaderSpeed(readerSpeed);
	}

	private boolean isSpawner(int readerIndex) {

		return READER_NAMES[readerIndex].endsWith("Spawner");
	}

	public void fontSetup() {

		PFont bask = loadFont("Baskerville-25.vlw");
		PFont gill = loadFont("GillSansMT-24.vlw");
		PFont info = loadFont("StoneSans-Semi-14.vlw");
		FONTS = new PFont[] { bask, gill, bask };
		this.info = info;
	}

	private void colorSetup() {

		COLOR_MAP = new LinkedHashMap();
		COLOR_MAP.put("White", OATMEAL);
		COLOR_MAP.put("Yellow", YELLOW);
		COLOR_MAP.put("Orange", MYELLOW);
		COLOR_MAP.put("Ochre", MOCHRE);
		COLOR_MAP.put("Brown", MBROWN);

		// grid color setup
		LAYOUT_BACKGROUND_COLOR = BLACK_INT; // this sketch always has a black
																					// background
		gridFillInt = (LAYOUT_BACKGROUND_COLOR > 127) ? BLACK_INT : WHITE_INT;
		setGridFill(gridAlpha);
	}

	private void setGridFill(int gridAlpha) {

		RiTextGrid.defaultColor(gridFillInt, gridFillInt, gridFillInt, gridAlpha);
	}

	private void buttonSetup() {

		SPEED_MAP = new LinkedHashMap();
		// must be LinkedHashMap to preserve keySet() orders below
		SPEED_MAP.put("Fluent", FLUENT);
		SPEED_MAP.put("Steady", STEADY);
		SPEED_MAP.put("Slow", SLOW);
		SPEED_MAP.put("Slower", SLOWER);
		SPEED_MAP.put("Slowest", SLOWEST);
		SPEED_MAP.put("Fast", FAST);

		ButtonSelect.TEXT_FILL = BLACK;
		ButtonSelect.STROKE_WEIGHT = 0;

		int buttonY = 697;
		titleSelect = new ButtonSelect(this, 0, buttonY, "Title", TEXT_NAMES);
		textSelect = new ButtonSelect(this, 0, buttonY, "Text", new String[] { "Faint", "Dark", "Gray" });
		readerSelect = new ButtonSelect(this, 0, buttonY, "Reader", READER_NAMES);
		colorSelect = new ButtonSelect(this, 0, buttonY, "Color", (String[]) COLOR_MAP.keySet().toArray(new String[0]));
		speedSelect = new ButtonSelect(this, 0, buttonY, "Speed", (String[]) SPEED_MAP.keySet().toArray(new String[0]));
		visualSelect = new ButtonSelect(this, 0, buttonY, "Visual", new String[] { "Traces", "Haloes" });
		spawnSelect = new ButtonSelect(this, 0, buttonY, "Spawning", new String[] { "NE & SE", "South-East", "North-East" });

		int widestButton = 0;
		for (int i = 0; i < ButtonSelect.instances.size(); i++) {
			int buttonWidth = ButtonSelect.instances.get(i).getWidth();
			widestButton = buttonWidth > widestButton ? buttonWidth : widestButton;
		}

		int nextX = width / 2 + 10;
		for (int i = 0; i < ButtonSelect.instances.size(); i++) {
			ButtonSelect bs = ButtonSelect.instances.get(i);
			bs.x = nextX;
			nextX = nextX + bs.getWidth();
		}
	}

	public void constructReadersFor(PerigramLookup perigrams, int textIndex) {

		verso = pManager.getVerso();
		recto = pManager.getRecto();

		constructBehaviorsFor(perigrams);

		if (readers == null) readers = new MachineReader[READER_NAMES.length];

		// PERIGRAM
		if (readers.length > 0) {

			MachineReader.delete(readers[0]);
			readers[0] = new PerigramReader(verso, perigrams);
			readers[0].setSpeed(readerSpeed);
			readers[0].setBehavior(neighborFading);
		}

		// UNCONSTRAINED PERIGRAM
		if (readers.length > 1) {

			MachineReader.delete(readers[1]);
			readers[1] = new UnconPerigramReader(verso, perigrams);
			readers[1].setSpeed(readerSpeed);
			readers[1].setBehavior(neighborFadingNT);
		}

		// SIMPLE READING SPAWNER
		if (readers.length > 2) {

			MachineReader.delete(readers[2]);
			readers[2] = new SimpleReader(verso);
			readers[2].setSpeed(readerSpeed);
			readers[2].setBehavior(defaultVisuals);
			readers[2].addBehavior(spawningVB);
		}

		// PERIGRAM SPAWNER
		if (readers.length > 3) {
			MachineReader.delete(readers[3]);
			readers[3] = new PerigramReader(verso, perigrams);
			readers[3].setSpeed(readerSpeed);
			readers[3].setBehavior(neighborFadingNT);
			readers[3].addBehavior(spawningVB);
		}

		// LESS DIRECTED SPAWNER
		if (readers.length > 4) {

			MachineReader.delete(readers[4]);
			readers[4] = new UnconPerigramReader(verso, perigrams);
			readers[4].setSpeed((float) SPEED_MAP.get("Steady"));
			readers[4].setBehavior(neighborFadingNT);
			readers[4].addBehavior(spawningVB);
		}

		// MESOSTIC JUMPER
		if (readers.length > 5) {

			MachineReader.delete(readers[5]);
			readers[5] = new MesoPerigramJumper(verso, MESOSTICS[textIndex], perigrams);
			readers[5].setSpeed(readerSpeed);
			readers[5].setBehavior(mesostic);
		}

		// currentReaderIdx = 6; // Mesostic default

		for (int i = 0; i < readers.length; i++) {
			readers[i].setCurrentCell(pManager.getVerso().cellAt(0, 0));
			readers[i].start(); // original speeds are set when the readers start
			readers[i].pause(currentReaderIdx != i);
		}

		pManager.onUpdateFocusedReader(getCurrentReader(currentReaderIdx));

	}

	public void constructBehaviorsFor(PerigramLookup perigrams) {

		neighborFading = new NeighborFadingVisual(readerColor, verso.template().fill(), readerSpeed);
		((NeighborFadingVisual) neighborFading).setFadeLeadingNeighbors(true);
		((NeighborFadingVisual) neighborFading).setFadeTrailingNeighbors(true);

		neighborFadingNT = new NeighborFadingVisual(MOCHRE, verso.template().fill(), readerSpeed);
		((NeighborFadingVisual) neighborFadingNT).setFadeLeadingNeighbors(false);
		((NeighborFadingVisual) neighborFadingNT).setFadeTrailingNeighbors(false);

		defaultVisuals = new DefaultVisuals(MOCHRE, readerSpeed);

		tendrilsDGray = new DefaultVisuals(DGRAY, FAST, FLUENT);
		// FAST is a delay before fadein

		spawningVB = new SpawnDirectionalPRs(perigrams, tendrilsDGray, SE, NE);
		spawningSE = new SpawnDirectionalPRs(perigrams, tendrilsDGray, SE);
		spawningNE = new SpawnDirectionalPRs(perigrams, tendrilsDGray, NE);

		mesostic = new MesosticDefault(10f, MYELLOW);

		mesoHaloing = new MesosticHaloingVisual(MOCHRE, verso.template().fill(), readerSpeed);

		haloing = new ClearHaloingVisual(MOCHRE, verso.template().fill(), readerSpeed);

		// these appear to be the default behaviors, at least when not haloing?
		// Current (6) readers for reference:
		// { "Perigram", "Less Directed Perigram", "Simple Spawner",
		// "Perigram Spawner", "Less Directed Spawner", "Mesostic Jumper" }
		TRAILS = new ReaderBehavior[] { neighborFading, neighborFading, defaultVisuals, neighborFadingNT, neighborFadingNT, mesostic };
		HALOING = new ReaderBehavior[] { haloing, haloing, haloing, haloing, haloing, mesoHaloing };
		// NB (not brilliant): number of behaviors in this array
		// must match number of READER_NAMES
		BEHAVIORS = TRAILS;
		if (READER_NAMES.length != BEHAVIORS.length) Readers.warn("Number of behaviors does nto match number of readers.");
	}

	protected void setVisuals(String visuals, float[] color, boolean isSpawner) {

		if (visuals.equals("Haloes"))
			BEHAVIORS = HALOING;
		else
			BEHAVIORS = TRAILS;

		getCurrentReader(currentReaderIdx).setBehavior(BEHAVIORS[currentReaderIdx]);
		BEHAVIORS[currentReaderIdx].setReaderColor(color);
		BEHAVIORS[currentReaderIdx].adjustForReaderSpeed(readerSpeed);

		if (isSpawner) {
			switch (spawnSelect.value()) {
				case "South-East":
					getCurrentReader(currentReaderIdx).addBehavior(spawningSE);
					break;

				case "North-East":
					getCurrentReader(currentReaderIdx).addBehavior(spawningNE);
					break;

				default:
					getCurrentReader(currentReaderIdx).addBehavior(spawningVB);
					break;
			}
		}
	}

	private void showCurrentWord(String word) {

		// what about showing the last x words (perhaps with a fade for older ones)?
		// would be nice for mesostic at least, but would also give a better sense
		// of the generation
		// idea above def worth exploring but I think we should continue to allow
		// the human reader
		// to have a 'clean' view of the sketch without buttons or word monitor
		fill(255);
		textFont(info);
		// textSize(18);
		textAlign(LEFT);
		text(word, 56, 708);
	}

	String display, last = ""; // tmp-remove;

	public int readerIdxFromName(String name) {

		for (int i = 0; i < READER_NAMES.length; i++)
			if (READER_NAMES[i].equals(name)) return i;
		return -1;
	}

	public int textIdxFromName(String name) {

		for (int i = 0; i < TEXT_NAMES.length; i++)
			if (TEXT_NAMES[i].equals(name)) return i;
		return -1;
	}

	protected void resetButtons() {

		speedSelect.advanceTo("Fluent");
		readerSelect.advanceTo("Perigram");
		visualSelect.advanceTo("Traces");
	}

	protected void pauseReaders() {

		for (int i = 0; readers != null && i < readers.length; i++)
			readers[i].pause(true);
	}

	public static void main(String[] args) {

		info("Running " + ELC3Multi.class.getName());
		// String[] options = { "--present", "--hide-stop","--bgcolor=#000000",
		String[] options = { "--hide-stop", "--bgcolor=#000000", ELC3Multi.class.getName() };
		PApplet.main(options);
	}

}// end
