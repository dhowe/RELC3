package trp.layout;

import static trp.util.Direction.*;
import processing.core.PApplet;
import rita.RiText;
import trp.behavior.*;
import trp.reader.*;
import trp.util.PerigramLookup;

public class ELC3Multi extends MultiPageApplet
{
  // colors are Megan's Red[0], Blue[1], Yellow[2], Green[3]

  public static int[] COLORS = { 0xFA0027, 0x0095FF, 0xFFB01C, 0x00D107 };
  public static float[] READER_MONOCOLOR = BLACK;

  protected static String TEXT = "textual/image.txt";
  protected static String TEXT2 = "textual/image.txt";
  protected static final String MESOSTIC = "reading through writing through"; // not
  protected static String APP_ID = "pbtest";

  public void settings()
  {
    // fullScreen();
    size(1280, 720/*, GLConstants.GLGRAPHICS*/);
  }
  
  public void setup()
  {
    
    // server setup
    if (false && APP_ID.startsWith("PoetryBeyondText"))
    {
      System.out.println("Sending to server: " + APP_ID);
      enableServer("rednoise.org");
    }

    // font setup
    FONT = "Baskerville"; // Baskerville (22), Perpetua (24);
    // MinionPro-Regular(20)
    FONT_VLW = FONT + "-25" + ".vlw"; // was 26
    RiText.defaultFont(loadFont(FONT_VLW));

    // grid color setup
    LAYOUT_BACKGROUND_COLOR = BLACK_INT; // CHANGE THIS TO INVERT; > 127 dark on light
    int gridcol = (LAYOUT_BACKGROUND_COLOR > 127) ? 0 : 255;
    READER_MONOCOLOR = (gridcol == 0) ? BLACK : WHITE;
    GRID_ALPHA = 40; // EDIT could also be set from preferences in production
    RiTextGrid.defaultColor(gridcol, gridcol, gridcol - GRID_ALPHA, GRID_ALPHA);

    pManager = PageManager.create(this, 40, 40, 38, 30);
    pManager.showPageNumbers(false);
    pManager.addTextsFromFile(new String[] { TEXT2, TEXT });
    pManager.setApplicationId(APP_ID);
    pManager.decreaseGutterBy(20);
    pManager.setVersoHeader("");
    pManager.setRectoHeader("");
    pManager.setLeading(12);
    pManager.doLayout();

    // add readers
    addReaders();

    noCursor();
  }

  protected MachineReader rdr1, rdr2, rdr3, rdr4, rdr5;
  protected ReaderBehavior vb1, vb2, vb3, vb4, vb5;

  public void addReaders()
  {
    RiTextGrid verso = pManager.getVerso();
    RiTextGrid recto = pManager.getRecto();

    PerigramLookup allPerigrams = new PerigramLookup(this, new String[] { TEXT, TEXT2 });
    PerigramLookup pcPerigrams = new PerigramLookup(this, new String[] { TEXT });
    PerigramLookup misspeltPerigrams = new PerigramLookup(this, new String[] { TEXT2 });

    // rdr1 = new PerigramDirectionalReader(verso, perigrams, W);
    // rdr1.setGridPosition(5, 12);
    // rdr1.setSpeed(.5f);
    // rdr1.setBehavior(new DefaultVisuals(BRIGHT_RED));
    // rdr1.start();

    // (1) PERIGRAM THAT KNOWS BOTH NEIGHBORHOODS
    rdr1 = new PerigramReader(verso, allPerigrams);
    rdr1.setGridPosition(0, 0); // was 1, 0 because of page turn!
    rdr1.setSpeed(0.5f);
    vb1 = new NeighborFadingVisual(OATMEAL, verso.template().fill(), rdr1.getSpeed());
    ((NeighborFadingVisual) vb1).setFadeLeadingNeighbors(true);
    ((NeighborFadingVisual) vb1).setFadeTrailingNeighbors(true);
    rdr1.setBehavior(vb1);
    rdr1.start();

    // (2) X SIMPLE SPAWNER THAT KNOWS POETIC CAPTION
    rdr2 = new SimpleReader(verso);
    rdr2.setGridPosition(5, 10); // was 5, 20
    rdr2.setSpeed(1.7f);
    rdr2.setBehavior(new DefaultVisuals(MOCHRE, rdr2.getSpeed()));
    vb2 = new DefaultVisuals(DGRAY, .5f, rdr2.getSpeed()/* * 1.7f */);
    rdr2.addBehavior(new SpawnDirectionalPRs(pcPerigrams, vb2, NE, N, NW, SW, S, SE));
    rdr2.setTestMode(false);
    // rdr2.start();

    // (3) SIMPLE SPAWNER THAT KNOWS MISSPELT
    rdr3 = new SimpleReader(recto);
    rdr3.setGridPosition(0, 4); // was 5, 20
    rdr3.setSpeed(0.7f);
    rdr3.setBehavior(new DefaultVisuals(MOCHRE, rdr3.getSpeed()));
    vb3 = new DefaultVisuals(DGRAY, .5f, rdr3.getSpeed()/* * 1.7f */);
    rdr3.addBehavior(new SpawnDirectionalPRs(misspeltPerigrams, vb3, NE, N, NW, SW, S, SE));
    rdr3.setTestMode(false);
    rdr3.start();

    // (4) PERIGRAM SPAWNER THAT KNOWS POETIC CAPTION
    rdr4 = new PerigramReader(recto, pcPerigrams);
    rdr4.setGridPosition(9, 20); // was 1, 0 because of page turn!
    rdr4.setSpeed(0.6f);
    vb4 = new NeighborFadingVisual(MBROWN, verso.template().fill(), rdr4.getSpeed());
    ((NeighborFadingVisual) vb4).setFadeLeadingNeighbors(false);
    ((NeighborFadingVisual) vb4).setFadeTrailingNeighbors(false);
    rdr4.setBehavior(vb4);
    rdr4.addBehavior(new SpawnDirectionalPRs(pcPerigrams, vb3, NE, N, NW, SW, S, SE));
    rdr4.start();

    // (5) X PERIGRAM SPAWNER THAT KNOWS MISSPELT
    rdr5 = new PerigramReader(recto, misspeltPerigrams);
    rdr5.setGridPosition(3, 15); // was 1, 0 because of page turn!
    rdr5.setSpeed(2.7f);
    vb5 = new NeighborFadingVisual(MBROWN, recto.template().fill(), rdr5.getSpeed());
    ((NeighborFadingVisual) vb5).setFadeLeadingNeighbors(false);
    ((NeighborFadingVisual) vb5).setFadeTrailingNeighbors(false);
    rdr5.setBehavior(vb5);
    rdr5.addBehavior(new SpawnDirectionalPRs(misspeltPerigrams, vb3, NE, N, NW, SW, S, SE));
    // rdr5.start();

    pManager.onUpdateFocusedReader(rdr1);
  }

  public static void main(String[] args)
  {
    info("Running " + ELC3Multi.class.getName());
    String[] options = { "--present", "--hide-stop","--bgcolor=#000000",
        ELC3Multi.class.getName() };
    PApplet.main(options);
  }

}// end
