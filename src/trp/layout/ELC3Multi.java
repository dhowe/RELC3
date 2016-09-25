package trp.layout;

import static trp.util.Direction.*;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PFont;
import rita.RiTa;
import rita.RiText;
import trp.behavior.*;
import trp.reader.*;
import trp.util.PerigramLookup;

public class ELC3Multi extends MultiPageApplet {
	
	static final String[] TEXTS = { "textual/poeticCaption.txt", "textual/misspeltLandings.txt", "textual/image.txt" };
	static final String[] TEXTNAMES = { "POETIC CAPTION", "MISSPELT LANDINGS", "THE IMAGE" };
	static final String[] READERNAMES = { "Perigram", "Simple Spawner", "Perigram Spawner", "Mesostic Jumper" };
	static final String[] SPEEDNAMES = { "Fast", "Per-second", "Slow", "Slower", "Slowest", "Very fast" };
	static final String[] VISUALNAMES = { "Default visuals", "Haloed" };
	//static final String[] COLORNAMES = { "Oatmeal", "Ochre", "Brown", "Yellow" };
	static final PFont[] FONTS = new PFont[TEXTNAMES.length];

	//static Map READER_MAP = new HashMap();
	static Map SPEED_MAP = new HashMap();
	static Map COLOR_MAP = new HashMap();
	//static ButtonSelect[] BUTTONS;

	static {
		SPEED_MAP.put("Fast", 0.5f);
		SPEED_MAP.put("Per-second", 1.0f);
		SPEED_MAP.put("Slow", 1.5f);
		SPEED_MAP.put("Slower", 2.0f);
		SPEED_MAP.put("Slowest", 2.5f);
		SPEED_MAP.put("Very fast", 0.25f);

		COLOR_MAP.put("Oatmeal", OATMEAL);
		COLOR_MAP.put("Ochre", MOCHRE);
		COLOR_MAP.put("Brown", MBROWN);
		COLOR_MAP.put("Yellow", MYELLOW);
	}

	protected MachineReader perigramReader, simpleReadingSpawner, perigramReadingSpawner, mesosticJumper;
	protected ReaderBehavior neighborFading, spawningVB, defaultVisuals, tendrilsDGray,
		neighborFadingNoTrails, haloing, mesostic;
	protected PerigramLookup perigrams;
	protected static String TEXT = "textual/poeticCaption.txt";
	protected static final String MESOSTIC = "reading as writing through";
	protected static String APP_ID = "elc3";
	protected static int BUTTONS_Y = 691;

	float readerColor[] = OATMEAL, readerSpeed = 0.5f;
	ButtonSelect textSelect, wordMonitor, readerSelect, speedSelect, visualSelect, colorSelect;
	String currentWord = "", lastWord = "";
	RiTextGrid verso, recto;
	RiText currentCell;

	public void settings() {
		
		// fullScreen();
		size(1280, 720);
	}

	public void setup() {
		
		fontSetup();
		buttonSetup();
		colorSetup();

		// do layout
		pManager = PageManager.create(this, 40, 40, 38, 30);
		pManager.showPageNumbers(false);
		pManager.setApplicationId(APP_ID);
		pManager.decreaseGutterBy(20);
		pManager.setVersoHeader("");
		pManager.setRectoHeader("");

		doLayout(0);

		if (PRESENTATION_MODE)
			noCursor(); // only hide cursor in a configurable PRESENTATION mode
	}
	
	private void doLayout(int i) {
		
		pauseReaders();
		
		speedSelect.advanceTo("Fast");
		readerSelect.advanceTo("Perigram");
		visualSelect.advanceTo("Default visuals");
		
		pManager.clear();
		pManager.setLeading(30);
		RiText.defaultFont(FONTS[i]);
		pManager.addTextFromFile(TEXTS[i]);
		pManager.doLayout();

		currentCell = pManager.getVerso().cellAt(0, 0);
		readerSpeed = (float) SPEED_MAP.get("Fast");

		constructReadersFor(TEXTS[i]);
	}
	
	public void mouseClicked() {
		
		if (pManager.flipping) return;
		
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
				//System.out.println("click: "+clicked.value()+", "+currentReaderIdx);
				rd.setSpeed((float) SPEED_MAP.get(speedSelect.value()));
				rd.setCurrentCell(rt);
				rd.pause(false);

				setVisuals(visualSelect.value(), readerColor);

				pManager.onUpdateFocusedReader(rd);
			}
			
			// SPEED
			else if (clicked == speedSelect) {
				readerSpeed = (float) SPEED_MAP.get(clicked.value());
				currentReader().setSpeed(readerSpeed);
			}
			
			// VISUALS
			else if (clicked == visualSelect) {
				setVisuals(clicked.value(), readerColor);
			}
			
			// COLOR - of reader
			else if (clicked == colorSelect) {
				readerColor = (float[]) COLOR_MAP.get(clicked.value());
				if (visualSelect.value().equals("Haloed")) {
					haloing = new ClearHaloingVisual(readerColor, verso.template().fill(), readerSpeed);
					currentReader().setBehavior(haloing);
				} else {
					switch (currentReaderIdx) {
					case 0: // perigram
						neighborFading.setReaderColor(readerColor);
						break;
					case 1: // simple spawner
						defaultVisuals.setReaderColor(readerColor);
						break;
					case 2: // perigram spawner
						neighborFadingNoTrails.setReaderColor(readerColor);
						break;
					case 3: // mesostic
						mesostic.setReaderColor(readerColor);
						break;

					default:
						break;
					}

				}
			}
		}
	}
	public void fontSetup() {
		PFont bask = loadFont("Baskerville-25.vlw");
		PFont bask = loadFont("Baskerville-25.vlw");
	}
	
	private void colorSetup() {
		
		// grid color setup
		LAYOUT_BACKGROUND_COLOR = BLACK_INT; // CHANGE THIS TO INVERT; > 127 dark on light
		int gridcol = (LAYOUT_BACKGROUND_COLOR > 127) ? 0 : 255;
		GRID_ALPHA = 40; // EDIT could also be set from preferences in production
		RiTextGrid.defaultColor(gridcol, gridcol, gridcol - GRID_ALPHA, GRID_ALPHA);
	}

	private void buttonSetup() {

		ButtonSelect.TEXT_FILL = BLACK;
		ButtonSelect.STROKE_WEIGHT = 0;

		textSelect = new ButtonSelect(this, 0, BUTTONS_Y, "Text", TEXTNAMES);
		wordMonitor = new ButtonSelect(this, 0, BUTTONS_Y, "Monitor", new String[] { "This monitors the current word" });
		readerSelect = new ButtonSelect(this, 0, BUTTONS_Y, "Reader", READERNAMES);
		speedSelect = new ButtonSelect(this, 0, BUTTONS_Y, "Speed", SPEEDNAMES);
		visualSelect = new ButtonSelect(this, 0, BUTTONS_Y, "Visual", VISUALNAMES);
		colorSelect = new ButtonSelect(this, 0, BUTTONS_Y, "Color", (String[]) COLOR_MAP.keySet().toArray(new String[0]));

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

	private void pauseReaders() {
		for (int i = 0; READERS != null && i < READERS.length; i++) {
			READERS[i].pause(true);
		}
	}

	public void constructReadersFor(String text) {
		currentReaderIdx = 0;

		verso = pManager.getVerso();
		recto = pManager.getRecto();

		constructVBsFor(text);

		// PERIGRAM
		if (perigramReader != null)
			MachineReader.delete(perigramReader);
		perigramReader = new PerigramReader(verso, perigrams);
		perigramReader.setGridPosition(0, 0);
		//perigramReader.setCurrentCell(currentCell); // was setGridPosition(1, 0)
																								// because of page turn!
		perigramReader.setSpeed(readerSpeed); // was 0.5f
		perigramReader.setBehavior(neighborFading);

		// SIMPLE READING SPAWNER - nb: has different default speed
		if (simpleReadingSpawner != null)
			MachineReader.delete(simpleReadingSpawner);
		simpleReadingSpawner = new SimpleReader(verso);
		//simpleReadingSpawner.setCurrentCell(currentCell);
		simpleReadingSpawner.setGridPosition(0, 0);
		simpleReadingSpawner.setSpeed((float) SPEED_MAP.get("Slow")); // was 1.7f
		simpleReadingSpawner.setBehavior(defaultVisuals);
		simpleReadingSpawner.addBehavior(spawningVB);
		simpleReadingSpawner.setTestMode(false);

		// PERIGRAM SPAWNER
		if (perigramReadingSpawner != null)
			MachineReader.delete(perigramReadingSpawner);
		perigramReadingSpawner = new PerigramReader(verso, perigrams);
		//perigramReadingSpawner.setCurrentCell(currentCell);
		perigramReadingSpawner.setGridPosition(0, 0);
		perigramReadingSpawner.setSpeed(readerSpeed); // was 0.6f
		perigramReadingSpawner.setBehavior(neighborFadingNoTrails);
		perigramReadingSpawner.addBehavior(spawningVB);

		// MESOSTIC JUMPER - nb: has different default speed
		if (mesosticJumper != null)
			MachineReader.delete(mesosticJumper);
		mesosticJumper = new MesoPerigramJumper(verso, MESOSTIC, perigrams);
		//mesosticJumper.setCurrentCell(currentCell);
		mesosticJumper.setGridPosition(0, 0);
		mesosticJumper.setSpeed((float) SPEED_MAP.get("Slow"), true);
		mesosticJumper.setBehavior(mesostic);

		READERS = new MachineReader[] { perigramReader, simpleReadingSpawner, perigramReadingSpawner, mesosticJumper };
		for (int i = 0; i < READERS.length; i++) {
			READERS[i].start();
			READERS[i].pause(currentReaderIdx != i);
		}

		// TODO: workaround! see draw method where this is done:
		// pManager.onUpdateFocusedReader(currentReader());
	}

	public void constructVBsFor(String text) // call by constructReaders()
	{
		perigrams = new PerigramLookup(this, new String[] { text });

		neighborFading = new NeighborFadingVisual(readerColor, verso.template().fill(), readerSpeed);
		((NeighborFadingVisual) neighborFading).setFadeLeadingNeighbors(true);
		((NeighborFadingVisual) neighborFading).setFadeTrailingNeighbors(true);

		neighborFadingNoTrails = new NeighborFadingVisual(MOCHRE, verso.template().fill(), readerSpeed);
		((NeighborFadingVisual) neighborFadingNoTrails).setFadeLeadingNeighbors(false);
		((NeighborFadingVisual) neighborFadingNoTrails).setFadeTrailingNeighbors(false);

		defaultVisuals = new DefaultVisuals(MOCHRE, (float) SPEED_MAP.get("Slow"));

		tendrilsDGray = new DefaultVisuals(DGRAY, .5f, (float) SPEED_MAP.get("Slow"));
		// earlier failed? attempt to make tendrils faster by multiplying speed by 1.7

		// NB ugh: tendrilsDGray has to be non-null at this point:
		spawningVB = new SpawnDirectionalPRs(perigrams, tendrilsDGray, NE, N, NW, SW, S, SE);

		mesostic = new MesosticDefault(10f, MYELLOW);

	}

	public int readerIdxFromName(String name) {
		for (int i = 0; i < READERNAMES.length; i++)
				if (READERNAMES[i].equals(name))
						return i;
		return -1;
	}
	public int textIdxFromName(String name) {
		for (int i = 0; i < TEXTNAMES.length; i++)
				if (TEXTNAMES[i].equals(name))
						return i;
		return -1;
	}

	protected void setVisuals(String visuals, float[] color) {
		if (visuals.equals("Haloed")) {
			haloing = new ClearHaloingVisual(color, verso.template().fill(), readerSpeed);
			currentReader().setBehavior(haloing);
		} else {
			readerColor = color;
			switch (currentReaderIdx) {
			case 0: // perigram
				currentReader().setBehavior(neighborFading);
				neighborFading.setReaderColor(color);
				break;
			case 1: // simple spawner
				currentReader().setSpeed((float) SPEED_MAP.get("Slow"));
				currentReader().setBehavior(defaultVisuals);
				currentReader().addBehavior(spawningVB);
				defaultVisuals.setReaderColor(color);
				speedSelect.advanceTo("Slow");
				break;
			case 2: // perigram spawner
				currentReader().setBehavior(neighborFadingNoTrails);
				currentReader().addBehavior(spawningVB);
				neighborFadingNoTrails.setReaderColor(color);
				break;
			case 3: // mesostic
				currentReader().setSpeed((float) SPEED_MAP.get("Slow"), true);
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

		// BUTTONS drawn only if page is not flipping
		if (!pManager.flipping) {
			
			if (mouseY >= BUTTONS_Y && mouseY < (BUTTONS_Y + textSelect.height)) {
				
				for (int i = 0; i < ButtonSelect.instances.size(); i++)
					ButtonSelect.instances.get(i).textFill = WHITE;

				if (PRESENTATION_MODE)
					cursor();
				
			} else {
				
				for (int i = 0; i < ButtonSelect.instances.size(); i++)
					ButtonSelect.instances.get(i).textFill = BLACK;
					
				if (PRESENTATION_MODE)
					noCursor();
			}

			currentWord = currentReader().getCurrentCell().text();
			currentWord = RiTa.stripPunctuation(currentWord);
			if (!currentWord.equals(lastWord)) {
				
				int numOfSyllables = countSyllables(RiTa.getSyllables(currentWord));

				// currentReader().adjustSpeed(1f + (numOfSyllables * 2f)); TODO: how to
				// adjust speed per-step properly??

				wordMonitor.setValue(currentWord);
				lastWord = currentWord;
			}

			ButtonSelect.drawAll(mouseX, mouseY);
		}
		pManager.draw(g);

		// TODO: part of ugly workaround to allow new readers to begin at 0,0 on
		// verso without page flip/turn
//		if (MachineReader.OK_TO_FOCUS) {
//			pManager.onUpdateFocusedReader(currentReader());
//			MachineReader.OK_TO_FOCUS = false;
//		}

	}

	private int countSyllables(String syllables) {
		return syllables.length() - syllables.replace("/", "").length() + 1;
	}

	public static void main(String[] args) {
		info("Running " + ELC3Multi.class.getName());
		// String[] options = { "--present", "--hide-stop","--bgcolor=#000000",
		String[] options = { "--hide-stop", "--bgcolor=#000000", ELC3Multi.class.getName() };
		PApplet.main(options);
	}

}// end
