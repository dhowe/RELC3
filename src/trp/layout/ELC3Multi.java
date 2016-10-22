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
	ButtonSelect textSelect, readerSelect, speedSelect, spawnSelect, visualSelect, colorSelect;
	float readerColor[] = OATMEAL, readerSpeed = 0.5f;
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
		doLayout(0); // 0: Poetic Caption
	}

	public void draw() {

		background(LAYOUT_BACKGROUND_COLOR);

		// draw buttons only if not flipping
		// if (!pManager.isFlipping())
		// {
		if ((mouseY < height) && (mouseY > (height - (textSelect.height + 14)))) // KLUDGE:
																																							// +
																																							// 14
																																							// for
																																							// label
		{
			ButtonSelect.drawAll(mouseX, mouseY);

			String word = MachineReader.stripPunctuation(getCurrentReader(currentReaderIdx).getCurrentCell().text());
			showCurrentWord(word);

			if (PRESENTATION_MODE) cursor();
		}
		else {
			if (PRESENTATION_MODE) noCursor();

			drawDog_ear();
		}

		// }
		// else
		// drawDog_ear();

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

		pauseReaders();
		resetButtons();

		if (pManager == null) {
			pManager = PageManager.create(this, 40, 40, 38, 38); // bottom marg was
																														// 30, adjust for
																														// Beckett
			pManager.showPageNumbers(false);
			pManager.setApplicationId("elc3");
			pManager.decreaseGutterBy(20);
		}

		pManager.clear();
		pManager.setLeading(30);
		pManager.setFont(FONTS[textIndex]);
		pManager.addTextFromFile(TEXTS[textIndex]);
		pManager.doLayout();

		constructReadersFor(PERIGRAMS[textIndex], textIndex);
	}

	public void mouseClicked() {

		if (pManager.isFlipping()) return;

		ButtonSelect clicked = ButtonSelect.click(mouseX, mouseY, isShiftDown());
		if (clicked != null) {

			// TEXT
			if (clicked == textSelect) {
				// MachineReader.destroyAll(); TODO: I thought this would do it but it
				// doesn't
				// look at the output from this. Why don't the instances get deleted??
				for (int i = 0; i < MachineReader.instances.size(); i++) {
					Readers.info(MachineReader.instances.get(i).toString());
					((MachineReader) MachineReader.instances.get(i)).pause(true);
					MachineReader.delete((MachineReader) MachineReader.instances.get(i));
				}
				Readers.info("no instances? " + (MachineReader.instances.size() == 0));
				for (int i = 0; i < MachineReader.instances.size(); i++) {
					Readers.info(MachineReader.instances.get(i).toString());
				}
				doLayout(textIdxFromName(clicked.value()));
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
			}

			// VISUALS
			else if (clicked == spawnSelect || clicked == visualSelect) {
				setVisuals(clicked.value(), readerColor, isSpawner(currentReaderIdx));
			}

		}
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
		COLOR_MAP.put("Oatmeal", OATMEAL);
		COLOR_MAP.put("Ochre", MOCHRE);
		COLOR_MAP.put("Brown", MBROWN);
		COLOR_MAP.put("Yellow", MYELLOW);

		// grid color setup
		LAYOUT_BACKGROUND_COLOR = BLACK_INT; // CHANGE THIS TO INVERT; > 127 dark on
																					// light
		int gridcol = (LAYOUT_BACKGROUND_COLOR > 127) ? 0 : 255;
		GRID_ALPHA = 40; // EDIT could also be set from preferences in production
		RiTextGrid.defaultColor(gridcol, gridcol, gridcol - GRID_ALPHA, GRID_ALPHA);
	}

	private void buttonSetup() {

		SPEED_MAP = new LinkedHashMap(); // must be LinkedHashMap to preserve
																			// keySet() orders below
		SPEED_MAP.put("Fluent", FLUENT);
		SPEED_MAP.put("Steady", STEADY);
		SPEED_MAP.put("Slow", SLOW);
		SPEED_MAP.put("Slower", SLOWER);
		SPEED_MAP.put("Slowest", SLOWEST);
		SPEED_MAP.put("Fast", FAST);

		ButtonSelect.TEXT_FILL = BLACK;
		ButtonSelect.STROKE_WEIGHT = 0;

		int buttonY = 697;
		textSelect = new ButtonSelect(this, 0, buttonY, "Text", TEXT_NAMES);
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

		int nextX = width / 2 + 20;
		for (int i = 0; i < ButtonSelect.instances.size(); i++) {
			ButtonSelect bs = ButtonSelect.instances.get(i);
			bs.x = nextX;
			nextX = nextX + bs.getWidth();
		}
	}

	public void constructReadersFor(PerigramLookup perigrams, int textIndex) {

		currentReaderIdx = 0; // reset back to first reader

		readerSpeed = FLUENT;
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

		pManager.onUpdateFocusedReader(getCurrentReader(currentReaderIdx));

		for (int i = 0; i < readers.length; i++) {
			readers[i].setCurrentCell(pManager.getVerso().cellAt(0, 0));
			readers[i].start(); // original speeds are set when the readers start
			readers[i].pause(currentReaderIdx != i);
		}
	}

	public void constructBehaviorsFor(PerigramLookup perigrams) {

		neighborFading = new NeighborFadingVisual(readerColor, verso.template().fill(), readerSpeed);
		((NeighborFadingVisual) neighborFading).setFadeLeadingNeighbors(true);
		((NeighborFadingVisual) neighborFading).setFadeTrailingNeighbors(true);

		neighborFadingNT = new NeighborFadingVisual(MOCHRE, verso.template().fill(), readerSpeed);
		((NeighborFadingVisual) neighborFadingNT).setFadeLeadingNeighbors(false);
		((NeighborFadingVisual) neighborFadingNT).setFadeTrailingNeighbors(false);

		defaultVisuals = new DefaultVisuals(MOCHRE, readerSpeed);

		tendrilsDGray = new DefaultVisuals(DGRAY, FAST, FLUENT); // FAST is a delay *before fadein*

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
		// NB (not brilliant): number of behaviors in this array must match number
		// of READER_NAMES
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

		// what about showing the last x words (perhaps with a fade for older ones)
		// ?
		// would be nice for mesostic at least, but would also give a better sense
		// of the generation
		//
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
