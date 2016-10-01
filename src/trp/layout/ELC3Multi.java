package trp.layout;

import static trp.util.Direction.*;

import rita.*;

import java.util.*;

import processing.core.*;
import trp.behavior.*;
import trp.reader.*;
import trp.util.PerigramLookup;

public class ELC3Multi extends MultiPageApplet {

	static final String[] TEXTS = { "textual/poeticCaption.txt", "textual/misspeltLandings.txt", "textual/image.txt" };
	static final String[] READER_NAMES = { "Perigram", "Simple Spawner", "Perigram Spawner", "Mesostic Jumper" };
	static final String[] TEXT_NAMES = { "POETIC CAPTION", "MISSPELT LANDINGS", "THE IMAGE" };
	static final String[] VISUAL_NAMES = { "Traces", "Haloes" };
	static final String[] MESOSTICS = { 
		"reading as writing through", 
		"reaching out falling through circling over landing on turning within spelling as",
		"comes in is over goes out is done lolls in stays there is had no more", 
		"reading as writing through"
	};

	static Map SPEED_MAP, COLOR_MAP;
	static ReaderBehavior[] BEHAVIORS;
	static PFont[] FONTS;

	ReaderBehavior neighborFading, spawningVB, defaultVisuals, tendrilsDGray, neighborFadingNT, haloing, mesostic;
	ButtonSelect textSelect, wordMonitor, readerSelect, speedSelect, visualSelect, colorSelect;
	float readerColor[] = OATMEAL, readerSpeed = 0.5f;
	RiTextGrid verso, recto;
  PFont info;

	public void settings() {

		// fullScreen();
		size(1280, 720);
	}

	public void setup() {

		fontSetup();
		colorSetup();
		buttonSetup();
		createPageManager();
		doLayout(0/* Poetic Caption */);
	}

	private void createPageManager() {

		pManager = PageManager.create(this, 40, 40, 38, 30);
		pManager.showPageNumbers(false);
		pManager.setApplicationId("elc3");
		pManager.decreaseGutterBy(20);
	}

	private void doLayout(int textIndex) {

		pauseReaders();
		resetButtons();

		pManager.clear();
		pManager.setLeading(30);
		pManager.setFont(FONTS[textIndex]);
		pManager.addTextFromFile(TEXTS[textIndex]);
		pManager.doLayout();

		constructReadersFor(new PerigramLookup(this, TEXTS[textIndex]), textIndex);
	}

	public void mouseClicked() {

		if (pManager.isFlipping()) return;

		ButtonSelect clicked = ButtonSelect.click(mouseX, mouseY);
		if (clicked != null) {

			// TEXT
			if (clicked == textSelect) {
				doLayout(textIdxFromName(clicked.value()));
			}

			// READER
			else if (clicked == readerSelect) {

				MachineReader rd = currentReader();
				RiText rt = rd.getCurrentCell();
				rd.pause(true);

				currentReaderIdx = readerIdxFromName(clicked.value());

				rd = currentReader();
				// System.out.println("click: "+clicked.value()+", "+currentReaderIdx);
				rd.setSpeed((float) SPEED_MAP.get(speedSelect.value()), true); // alsoResetOriginalSpeed
				rd.currentCell(rt);
				rd.pause(false);

				setVisuals(visualSelect.value(), readerColor, isSpawner(currentReaderIdx));

				pManager.onUpdateFocusedReader(rd);
			}

			// SPEED
			else if (clicked == speedSelect) {
				readerSpeed = (float) SPEED_MAP.get(clicked.value());
				currentReader().setSpeed(readerSpeed, true); // alsoResetOriginalSpeed
			}

			// VISUALS
			else if (clicked == visualSelect) {
				setVisuals(clicked.value(), readerColor, isSpawner(currentReaderIdx));
			}

			// COLOR - of reader
			else if (clicked == colorSelect) {
				readerColor = (float[]) COLOR_MAP.get(clicked.value());

				if (visualSelect.value().equals("Haloes")) {
					setVisuals("Haloes", readerColor, isSpawner(currentReaderIdx));
				}
				else {
					BEHAVIORS[currentReaderIdx].setReaderColor(readerColor);
				}
			}
		}
	}

	private boolean isSpawner(int readerIndex) {

		return READER_NAMES[readerIndex].contains("awn");
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
		SPEED_MAP.put("Fluent", 0.4f);
		SPEED_MAP.put("Steady", 0.8f);
		SPEED_MAP.put("Slow", 1.2f);
		SPEED_MAP.put("Slower", 1.6f);
		SPEED_MAP.put("Slowest", 2.0f);
		SPEED_MAP.put("Fast", 0.2f);

		ButtonSelect.TEXT_FILL = BLACK;
		ButtonSelect.STROKE_WEIGHT = 0;

		int buttonY = 691;
		textSelect = new ButtonSelect(this, 0, buttonY, "Text", TEXT_NAMES);
		// wordMonitor = new ButtonSelect(this, 0, buttonY, "Monitor", new String[]
		// { "monitors the current word" }); // the string passed here determines
		// the width of the button
		readerSelect = new ButtonSelect(this, 0, buttonY, "Reader", READER_NAMES);
		speedSelect = new ButtonSelect(this, 0, buttonY, "Speed", (String[]) SPEED_MAP.keySet().toArray(new String[0]));
		visualSelect = new ButtonSelect(this, 0, buttonY, "Visual", VISUAL_NAMES);
		colorSelect = new ButtonSelect(this, 0, buttonY, "Color", (String[]) COLOR_MAP.keySet().toArray(new String[0]));

		int totalWidth = 0;
		for (int i = 0; i < ButtonSelect.instances.size(); i++) {
			totalWidth += ButtonSelect.instances.get(i).getWidth();
		}

		int nextX = (width - totalWidth) / 2;
		for (int i = 0; i < ButtonSelect.instances.size(); i++) {
			ButtonSelect bs = ButtonSelect.instances.get(i);
			bs.x = nextX;
			nextX += bs.getWidth();
		}
	}

	public void constructReadersFor(PerigramLookup perigrams, int textIndex) {

		currentReaderIdx = 0; // reset back to first reader

		readerSpeed = (float) SPEED_MAP.get("Fluent");
		verso = pManager.getVerso();
		recto = pManager.getRecto();

		constructBehaviorsFor(perigrams);

		if (readers == null) readers = new MachineReader[READER_NAMES.length];

		// PERIGRAM
		if (readers.length > 0) {

			MachineReader.delete(readers[0]);
			readers[0] = new PerigramReader(verso, perigrams);
			readers[0].setSpeed(readerSpeed); // was 0.5f
			readers[0].setBehavior(neighborFading);
		}

		if (readers.length > 1) {

			// SIMPLE READING SPAWNER - nb: has different default speed
			MachineReader.delete(readers[1]);
			readers[1] = new SimpleReader(verso);
			readers[1].setSpeed((float) SPEED_MAP.get("Slow")); // was 1.7f
			readers[1].setBehavior(defaultVisuals);
			readers[1].addBehavior(spawningVB);
		}

		if (readers.length > 2) {

			// PERIGRAM SPAWNER
			MachineReader.delete(readers[2]);
			readers[2] = new PerigramReader(verso, perigrams);
			readers[2].setSpeed(readerSpeed); // was 0.6f
			readers[2].setBehavior(neighborFadingNT);
			readers[2].addBehavior(spawningVB);
		}

		if (readers.length > 3) {

			// MESOSTIC JUMPER - nb: has different default speed
			MachineReader.delete(readers[3]);
			readers[3] = new MesoPerigramJumper(verso, MESOSTICS[textIndex], perigrams);
			readers[3].setSpeed((float) SPEED_MAP.get("Slow"));
			readers[3].setBehavior(mesostic);
		}

		for (int i = 0; i < readers.length; i++) {
			readers[i].currentCell(pManager.getVerso().cellAt(0, 0));
			readers[i].start(); // original speeds are set when the readers start
			readers[i].pause(currentReaderIdx != i);
		}

		pManager.onUpdateFocusedReader(currentReader());
	}

	public void constructBehaviorsFor(PerigramLookup perigrams) {

		neighborFading = new NeighborFadingVisual(readerColor, verso.template().fill(), readerSpeed);
		((NeighborFadingVisual) neighborFading).setFadeLeadingNeighbors(true);
		((NeighborFadingVisual) neighborFading).setFadeTrailingNeighbors(true);

		neighborFadingNT = new NeighborFadingVisual(MOCHRE, verso.template().fill(), readerSpeed);
		((NeighborFadingVisual) neighborFadingNT).setFadeLeadingNeighbors(false);
		((NeighborFadingVisual) neighborFadingNT).setFadeTrailingNeighbors(false);

		defaultVisuals = new DefaultVisuals(MOCHRE, (float) SPEED_MAP.get("Slow"));

		tendrilsDGray = new DefaultVisuals(DGRAY, .5f, (float) SPEED_MAP.get("Slow"));
		// earlier failed? attempt to make tendrils faster by multiplying speed by
		// 1.7

		// NB ugh: tendrilsDGray has to be non-null at this point:
		spawningVB = new SpawnDirectionalPRs(perigrams, tendrilsDGray, NE, N, NW, SW, S, SE);

		mesostic = new MesosticDefault(10f, MYELLOW);

		// these appear to be the default behaviors, at least when not haloing?
		// (not sure if we need to keep recreating them over and over)
		BEHAVIORS = new ReaderBehavior[] { neighborFading, defaultVisuals, neighborFadingNT, mesostic };
	}

	protected void setVisuals(String visuals, float[] color, boolean isSpawner) {

		if (visuals.equals("Haloes")) {
			haloing = (currentReaderIdx == 3) ? new MesosticHaloingVisual(color, verso.template().fill(), readerSpeed) : new ClearHaloingVisual(color, verso.template().fill(), readerSpeed);

			currentReader().setBehavior(haloing);

			if (isSpawner) {
				currentReader().addBehavior(spawningVB);
			}
		}
		else {

			readerColor = color;

			switch (currentReaderIdx) {

				case 0: // perigram
					currentReader().setBehavior(neighborFading);
					neighborFading.setReaderColor(color);
					break;

				case 1: // simple spawner
					currentReader().setSpeed((float) SPEED_MAP.get("Slow"), true); // alsoResetOriginalSpeed
					currentReader().setBehavior(defaultVisuals);
					currentReader().addBehavior(spawningVB);
					defaultVisuals.setReaderColor(color);
					speedSelect.advanceTo("Slow");
					break;

				case 2: // perigram spawner
					currentReader().setBehavior(neighborFadingNT);
					currentReader().addBehavior(spawningVB);
					neighborFadingNT.setReaderColor(color);
					break;

				case 3: // mesostic
					currentReader().setSpeed((float) SPEED_MAP.get("Slow"), true); // alsoResetOriginalSpeed
					currentReader().setBehavior(mesostic);
					mesostic.setReaderColor(color);
					speedSelect.advanceTo("Slow");
					break;

				default:
					break;
			}
		}
	}

	public void draw() {

		background(LAYOUT_BACKGROUND_COLOR);

		// draw buttons only if not flipping
		if (!pManager.isFlipping()) {
			if ((mouseY >= textSelect.y && mouseY < (textSelect.y + textSelect.height)) || (mouseY > 0 && mouseY < 44)) {
				for (int i = 0; i < ButtonSelect.instances.size(); i++)
					ButtonSelect.instances.get(i).textFill = WHITE;

				String word = MachineReader.stripPunctuation(currentReader().getCurrentCell().text());
	      showCurrentWord(word);

				if (PRESENTATION_MODE) cursor();
			}
			else {
				for (int i = 0; i < ButtonSelect.instances.size(); i++)
					ButtonSelect.instances.get(i).textFill = BLACK;

				if (PRESENTATION_MODE) noCursor();
			}

			ButtonSelect.drawAll(mouseX, mouseY);
		}

		pManager.draw(g);
	}
	
	private void showCurrentWord(String word) {
		
		// what about showing the last x words (perhaps with a fade for older ones) ?
		// would be nice for mesostic at least, but would also give a better sense of the generation
	  //
	  // idea above def worth exploring but I think we should continue to allow the human reader
	  // to have a 'clean' view of the sketch without buttons or word monitor
		fill(255);
		textFont(info);
    // textSize(18);
		textAlign(RIGHT);
		text(word, width - 16, 30);
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
