package trp.layout;

import static trp.util.Direction.*;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import rita.RiText;
import trp.behavior.*;
import trp.reader.*;
import trp.util.PerigramLookup;

public class ELC3Multi extends MultiPageApplet
{
  static final String[] TEXTNAMES = { "Poetic Caption", "Misspelt Landings", "The Image" };
  static final String[] READERNAMES = { "Perigram", "Simple Spawner", "Perigram Spawner" };
  static final String[] SPEEDNAMES = { "Fast", "Per-second", "Slow", "Slower", "Slowest", "Very fast" };
  static final String[] VISUALNAMES = { "Default visuals", "Haloed" };
  static final String[] COLORNAMES = { "White", "Ochre", "Brown", "Yellow" };

  static Map READER_MAP = new HashMap();
  static Map SPEED_MAP = new HashMap();
  static Map COLOR_MAP = new HashMap();

  static
  {
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

  public static float[] READER_MONOCOLOR = BLACK;

  protected static String TEXT = "textual/poeticCaption.txt";
  protected static final String MESOSTIC = "reading as writing through";
  protected static String APP_ID = "elc3";
  protected static int BUTTONS_Y = 691;

  ButtonSelect textSelect, readerSelect, speedSelect, visualSelect;
  RiTextGrid verso, recto;

  public void settings()
  {
    // fullScreen();
    size(1280, 720);
  }

  public void setup()
  {
    fontSetup();

    // TODO: buttons code
    ButtonSelect.TEXT_FILL = BLACK;
    ButtonSelect.STROKE_WEIGHT = 0;
    textSelect = new ButtonSelect(this, 200, BUTTONS_Y, "Text", TEXTNAMES);
    readerSelect = new ButtonSelect(this, 500, BUTTONS_Y, "Reader", READERNAMES);
    speedSelect = new ButtonSelect(this, 700, BUTTONS_Y, "Speed", SPEEDNAMES);
    visualSelect = new ButtonSelect(this, 900, BUTTONS_Y, "Visual", VISUALNAMES);

    // grid color setup
    LAYOUT_BACKGROUND_COLOR = BLACK_INT; // CHANGE THIS TO INVERT; > 127 dark on light
    int gridcol = (LAYOUT_BACKGROUND_COLOR > 127) ? 0 : 255;
    READER_MONOCOLOR = (gridcol == 0) ? BLACK : WHITE;
    GRID_ALPHA = 40; // EDIT could also be set from preferences in production
    RiTextGrid.defaultColor(gridcol, gridcol, gridcol - GRID_ALPHA, GRID_ALPHA);

    for (int i = 0; i < READERNAMES.length; i++) {
      READER_MAP.put(READERNAMES[i], i);
    }
    
    // do layout
    pManager = PageManager.create(this, 40, 40, 38, 30);
    pManager.showPageNumbers(false);
    pManager.setApplicationId(APP_ID);
    pManager.decreaseGutterBy(20);
    pManager.setVersoHeader("");
    pManager.setRectoHeader("");
    
    doLayout(TEXT);

    if (PRESENTATION_MODE)
      noCursor(); // only hide cursor in a configurable PRESENTATION mode
  }

  public void fontSetup()
  {
    FONT = "Baskerville"; // cf: Baskerville (22), Perpetua (24), MinionPro-Regular(20);
    FONT_VLW = FONT + "-25" + ".vlw"; // was 26
    RiText.defaultFont(loadFont(FONT_VLW));
  }

	private void doLayout(String fileName) {
		///System.out.println("ELC3Multi.doLayout("+text+")");
  	pauseReaders();
		pManager.clear();
		pManager.setLeading(30);
		//pManager.setFont(loadFont(FONT_VLW));
		pManager.addTextFromFile(fileName);
    pManager.doLayout();
    addReadersFor(fileName);
	}

	private void pauseReaders() {
		for (int i = 0; READERS != null && i < READERS.length; i++) {
			READERS[i].pause(true);
		}
	}

  protected MachineReader rdr1, rdr2, rdr3, rdr4, rdr5;
  protected ReaderBehavior nbFadeOatmeal, tendrilsDGray, nbFadeMBrown, haloYellow, vb5;

  public void addReadersFor(String text)
  {
    verso = pManager.getVerso();
    recto = pManager.getRecto();

    PerigramLookup allPerigrams = new PerigramLookup(this, new String[] { text });

    // (1) PERIGRAM THAT KNOWS BOTH NEIGHBORHOODS
    if (rdr1 != null) MachineReader.delete(rdr1);
    rdr1 = new PerigramReader(verso, allPerigrams);
    rdr1.setGridPosition(1, 0); // was 1, 0 because of page turn!
    rdr1.setSpeed((float) SPEED_MAP.get("Fast")); // was 0.5f
    nbFadeOatmeal = new NeighborFadingVisual(OATMEAL, verso.template().fill(), rdr1.getSpeed());
    ((NeighborFadingVisual) nbFadeOatmeal).setFadeLeadingNeighbors(true);
    ((NeighborFadingVisual) nbFadeOatmeal).setFadeTrailingNeighbors(true);
    rdr1.setBehavior(nbFadeOatmeal);

    // (2) SIMPLE SPAWNER
    if (rdr2 != null) MachineReader.delete(rdr2);
    rdr2 = new SimpleReader(verso);
    rdr2.setGridPosition(1, 0); // was 5, 20
    rdr2.setSpeed((float) SPEED_MAP.get("Slow")); // was 1.7f
    rdr2.setBehavior(new DefaultVisuals(MOCHRE, rdr2.getSpeed()));
    tendrilsDGray = new DefaultVisuals(DGRAY, .5f, rdr2.getSpeed()/* * 1.7f */);
    rdr2.addBehavior(new SpawnDirectionalPRs(allPerigrams, tendrilsDGray, NE, N, NW, SW, S, SE));
    rdr2.setTestMode(false);

    // (3) PERIGRAM SPAWNER
    if (rdr3 != null) MachineReader.delete(rdr3);
    rdr3 = new PerigramReader(verso, allPerigrams);
    rdr3.setGridPosition(1, 0); // was 1, 0 because of page turn!
    rdr3.setSpeed((float) SPEED_MAP.get("Fast")); // was 0.6f
    nbFadeMBrown = new NeighborFadingVisual(MBROWN, verso.template().fill(), rdr3.getSpeed());
    ((NeighborFadingVisual) nbFadeMBrown).setFadeLeadingNeighbors(false);
    ((NeighborFadingVisual) nbFadeMBrown).setFadeTrailingNeighbors(false);
    rdr3.setBehavior(nbFadeMBrown);
    rdr3.addBehavior(new SpawnDirectionalPRs(allPerigrams, tendrilsDGray, NE, N, NW, SW, S, SE));
    
    READERS = new MachineReader[] { rdr1, rdr2, rdr3 };
    for (int i = 0; i < READERS.length; i++) {
    	READERS[i].start();
    	READERS[i].pause(currentReaderIdx!=i);
		}
    
    pManager.onUpdateFocusedReader(currentReader());
  }

  
  public void mouseClicked()
  {
    ButtonSelect clicked = ButtonSelect.click(mouseX, mouseY);
    if (clicked != null)
    {
      //System.out.println(clicked.label + "=" + clicked.value());
      if (clicked == textSelect)
      {
        switch (clicked.value())
        {
          case "Poetic Caption":
            doLayout("textual/poeticCaption.txt");
            break;

          case "Misspelt Landings":
            doLayout("textual/misspeltLandings.txt");            
            break;
            
          case "The Image":
            doLayout("textual/image.txt");            
            break;

          default:
            break;
        }
      }
      else if (clicked == readerSelect)
      {
      	MachineReader current = currentReader();
      	current.pause(true);
        RiText rt = current.getCurrentCell();
        currentReaderIdx = (int) READER_MAP.get(clicked.value());
        current.setSpeed((float) SPEED_MAP.get(speedSelect.value()));
        current.setCurrentCell(rt);
        current.pause(false);
        
        // pManager.onUpdateFocusedReader(currentReader); TODO: problem with focus!
      }
      else if (clicked == speedSelect)
      {
        currentReader().setSpeed((float) SPEED_MAP.get(clicked.value()));
      }
      else if (clicked == visualSelect)
      {
        if ((clicked.value()).equals("Haloed"))
        {
        	MachineReader current = currentReader();
          haloYellow = new ClearHaloingVisual(MYELLOW, verso.template().fill(), current.getSpeed());
          current.setBehavior(haloYellow);
        }
      }
    }
  }
  
  public void draw()
  {
    background(LAYOUT_BACKGROUND_COLOR);

    // TODO: buttons drawing
    if (mouseY >= BUTTONS_Y && mouseY < (BUTTONS_Y + textSelect.height))
    {
      textSelect.textFill = WHITE;
      readerSelect.textFill = WHITE;
      speedSelect.textFill = WHITE;
      visualSelect.textFill = WHITE;
      if (PRESENTATION_MODE)
        cursor();
    }
    else
    {
      textSelect.textFill = BLACK;
      readerSelect.textFill = BLACK;
      speedSelect.textFill = BLACK;
      visualSelect.textFill = BLACK;
      if (PRESENTATION_MODE)
        noCursor();
    }
    ButtonSelect.drawAll(mouseX, mouseY);

    pManager.draw(g);

  }

  public static void main(String[] args)
  {
    info("Running " + ELC3Multi.class.getName());
    // String[] options = { "--present", "--hide-stop","--bgcolor=#000000",
    String[] options = { "--hide-stop", "--bgcolor=#000000", ELC3Multi.class.getName() };
    PApplet.main(options);
  }

}// end
