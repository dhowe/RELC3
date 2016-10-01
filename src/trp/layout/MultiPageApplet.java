package trp.layout;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import rita.RiText;
import trp.behavior.DefaultVisuals;
import trp.behavior.HaloingVisualBehavior;
import trp.behavior.MesosticDefault;
import trp.behavior.NeighborFadingVisual;
import trp.reader.LookaheadPosReader;
import trp.reader.MachineReader;
import trp.reader.MesoPerigramJumper;
import trp.reader.PerigramReader;
import trp.reader.UnconPerigramReader;
import trp.util.PerigramLookup;
import trp.util.Readers;

public class MultiPageApplet extends ReadersPApplet
{
  public static boolean PRESENTATION_MODE = false;
  public static String MESOSTIC = "its over its done ive had the image";
  public static int[] COLORS = { 0xFA0027, 0x0095FF, 0xFFB01C, 0x00D107 };
  public static boolean TURN_PAGES = false, LOADSAMPLES = false;
  public static String[] TEXTS = { "beckett/image.txt" };
  public static String[] DIGRAMS = { "beckett/imagePerigrams.txt" };
  protected static int PR_VOICE = 0, MPJ_VOICE = 1, UPR_VOICE = 2, LPR_VOICE = 3;
  protected static String[] AUDIOSETS = { "up15", "down05", "up20", "root" };
  
  protected MachineReader[] readers;
  protected int currentReaderIdx = 0;

  protected Map sampleMap = new HashMap();

  protected PageManager pManager;
  protected String[] phrases;

  public void setup()
  {
    USE_AUDIO = false;

    size(1200, 720);

    enableServer(MachineReader.SERVER_HOST);

    RiText.defaultFont("Georgia", 14); // 24
    RiText.defaults.paragraphLeading = 26;
    RiText.defaults.paragraphIndent = 0;

    RiTextGrid.defaultColor(0, 0, 0, PageManager.spotlightMode
        == SPOTLIGHT_NONE ? 255: 255);

    pManager = PageManager.create(this, 200, 100, 120, 100);
    pManager.setHeaders("The Image", TITLE);
    pManager.addTextsFromFile(TEXTS);
    pManager.setApplicationId("test");
    pManager.decreaseGutterBy(140);

    RiText[] words = pManager.doLayout();

    // Add RiTa features to all the RiTexts
    phrases = loadStrings("beckett/imagePhrases.txt");
    Readers.addRiTaFeatures(this, phrases, words, loadStrings("beckett/imagePos.txt"));

    // TODO: JHC removed audio handling for ELC3
//    if (USE_AUDIO)
//      loadAudioFileNames("beckett/audio/", words, sampleMap, AUDIOSETS, false);

    addReaders();
  }

  public MachineReader currentReader() {
  	return readers[currentReaderIdx];
  }
  
  public void addReaders()
  {
    PerigramLookup perigrams = PerigramLookup.getInstance(this, TEXTS);

    RiTextGrid verso = pManager.getVerso();
    RiTextGrid recto = pManager.getRecto();
    
    MachineReader reader0 = new PerigramReader(verso, perigrams);
    reader0.setSpeed(1.07f, true);
    reader0.setBehavior(new NeighborFadingVisual(Readers.unhex(COLORS[0]), verso.template().fill(), reader0.getSpeed()));
    //reader0.addBehavior(new MappedAudioBehavior(this, sampleMap, PR_VOICE, LOADSAMPLES));
    reader0.setPrintToConsole(false);
    reader0.setGridPosition(0, 5);
    reader0.start();

    MachineReader reader1 = new MesoPerigramJumper(verso, MESOSTIC, perigrams);
    reader1.setBehavior(new MesosticDefault(10f, Readers.unhex(COLORS[1])));
   // reader1.addBehavior(new MappedAudioBehavior(this, sampleMap, MPJ_VOICE, LOADSAMPLES));
    reader1.setGridPosition(0, 0);
    reader1.setSpeed(1.5f, true);
    reader1.start();
    
    if (1==1) return;
    
    MachineReader reader2 = new UnconPerigramReader(recto, perigrams);
    // rdr4.setBehavior(new SpawnOnDirection(perigrams, S, SE, N, NW));
    reader2.setSpeed(1.5f, true);
    reader2.setBehavior(new HaloingVisualBehavior(Readers.unhex(COLORS[2]), reader2.getSpeed()));
    //reader2.addBehavior(new MappedAudioBehavior(this, sampleMap, UPR_VOICE, LOADSAMPLES));
    reader2.setPrintToConsole(false);
    reader2.setTestMode(false);
    reader2.start();

    LookaheadPosReader reader3 = new LookaheadPosReader(recto, perigrams);
    reader3.setBehavior(new DefaultVisuals(Readers.unhex(COLORS[3])));
    //reader3.addBehavior(new MappedAudioBehavior(this, sampleMap, LPR_VOICE, LOADSAMPLES));
    reader3.addPhrases(phrases);
    reader3.setPrintToConsole(false);
    reader3.setGridPosition(0, 7);
    reader3.setSpeed(1.05f, true);
    reader3.start();

    pManager.onUpdateFocusedReader(reader1);
  }


  public void keyPressed()
  {
    if (!MachineReader.PRODUCTION_MODE)
    {
      if (keyCode < 58 && keyCode > 47)
      {
        // number keys (0-9)
        // COMMENTED OUT FOR ELC3
        // int idx = keyCode - 48;
        // if (idx < MachineReader.instances.size())
        // {
        // pManager.onUpdateFocusedReader((MachineReader) (MachineReader.instances.get(idx)));
        // }
        // else
        // {
        // Readers.warn("No reader corresponding to key #" + idx);
        // }
      }
      else if ((key == 'n' || key == 'N' || keyCode == 39) && !pManager.isFlipping())
      {
        pManager.nextPage();
      }
      else if (key == 'v')
      {
        Readers.NO_VISUALS = !Readers.NO_VISUALS;
      }  
      else if (key == ' ')
      {
        // assuming only one 'current' reader for ELC3 ...
        pause = !pause;
        currentReader().pause(pause);
        return; // ... super.keyPressed() will not be called (because it would pause or start all readers)
      }
    }
    
    super.keyPressed();
  }

  public void draw()
  {
    background(LAYOUT_BACKGROUND_COLOR);
    pManager.draw(g);
    
    //float offset = width/2f - PageManager.getInstance().gutterSubtraction;
    // if (frameCount%10==9) println("fps="+frameRate);
  }

  public static void main(String[] args)
  {
    String[] options = { MultiPageApplet.class.getName() };
    if (FULL_SCREEN)
      options = new String[] { "--present", "--hide-stop",
          MultiPageApplet.class.getName() };
    PApplet.main(options);
  }

}// end
