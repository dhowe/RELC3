package trp.layout;

import static trp.util.Direction.N;
import static trp.util.Direction.NE;
import static trp.util.Direction.NW;
import static trp.util.Direction.S;
import static trp.util.Direction.SE;

import processing.core.PApplet;
import rita.RiText;
import trp.behavior.DefaultVisuals;
import trp.behavior.NeighborFadingVisual;
import trp.behavior.ReaderBehavior;
import trp.behavior.SpawnDirectionalPRs;
import trp.reader.MachineReader;
import trp.reader.PerigramReader;
import trp.reader.SimpleReader;
import trp.util.PerigramLookup;

public class PoeticCaptionMulti extends MultiPageApplet
{
  public static int[] COLORS = { 0xFA0027, 0x0095FF, 0xFFB01C, 0x00D107 };
  public static float[] READER_MONOCOLOR = BLACK;

  protected static String TEXT = "textual/poeticCaption.txt";
  protected static final String MESOSTIC = "reading through writing through"; // not
  protected static String APP_ID = "pc4elc3";

  public void settings()
  {
    size(1280, 720/*, GLConstants.GLGRAPHICS*/);
  }
  
  public void setup() 
  {
    //enableServer("rednoise.org");

    FONT = "Baskerville"; // Baskerville (22), Perpetua (24); MinionPro-Regular(20)
    FONT_VLW = FONT + "-26" + ".vlw";
    RiText.defaultFont(loadFont(FONT_VLW));

    // grid color setup
    LAYOUT_BACKGROUND_COLOR = 0; // CHANGE THIS TO INVERT; > 127 dark on light
    int gridcol = (LAYOUT_BACKGROUND_COLOR > 127) ? 0 : 255;
    READER_MONOCOLOR = (gridcol == 0) ? BLACK : WHITE;
    GRID_ALPHA = 16; // could be set from preferences in production
    RiTextGrid.defaultColor(gridcol, gridcol, gridcol -16, 16);

    pManager = PageManager.create(this, 40, 40, 38, 30);
    pManager.showPageNumbers(false);
    pManager.addTextsFromFile(new String[] { TEXT });
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

  protected MachineReader rdr1, rdr2;
  protected ReaderBehavior vb1, vb2;

  public void addReaders()
  {
    RiTextGrid verso = pManager.getVerso();
    RiTextGrid recto = pManager.getRecto();

    PerigramLookup perigrams = PerigramLookup.getInstance(this, new String[] { TEXT });

    rdr1 = new PerigramReader(verso, perigrams);
    rdr1.setGridPosition(1, 0); // was 1, 0 because of page turn!
    rdr1.setSpeed(1.4f);
    vb1 = new NeighborFadingVisual(OATMEAL, verso.template().fill(), rdr1.getSpeed());
    ((NeighborFadingVisual) vb1).setFadeLeadingNeighbors(true);
    ((NeighborFadingVisual) vb1).setFadeTrailingNeighbors(true);
    rdr1.setBehavior(vb1);
    rdr1.start();

    rdr2 = new SimpleReader(recto);
    rdr2.setGridPosition(5, 10); // was 5, 20
    rdr2.setSpeed(1.4f);
    rdr2.setBehavior(new DefaultVisuals(SNOW, rdr2.getSpeed()));
    vb2 = new DefaultVisuals(DGRAY, .5f, rdr2.getSpeed()/* * 1.7f*/);
    rdr2.addBehavior(new SpawnDirectionalPRs(perigrams, vb2, NE, N, NW, SW, S, SE));
    rdr2.setTestMode(false);
    rdr2.start();

    pManager.onUpdateFocusedReader(rdr1);
  }

  public static void main(String[] args)
  {
    info("Running " + PoeticCaptionMulti.class.getName());
    String[] options = { "--present", "--hide-stop", PoeticCaptionMulti.class.getName() };
    PApplet.main(options);
  }

}// end
