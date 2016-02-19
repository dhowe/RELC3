package trp.layout;

import processing.core.PApplet;
import rita.RiText;
import trp.reader.MachineReader;
import trp.util.*;

// TODO: JHC removed audio handling for ELC3
// import trp.util.RiSample;

public class ReadersPApplet extends PApplet implements ReaderConstants
{
  static
  {
    info("Java.Version=" + System.getProperty("java.version"));

    RiText.defaults.paragraphLeading = 12;
    RiText.defaults.paragraphIndent = 0;
  }

  public static boolean USE_AUDIO = false;
  public static float SCALE = 1f;
  public static int FONT_SZ = 14;
  public static boolean FULL_SCREEN = false;
  public static String FONT = "Baskerville";
  public static String FONT_VLW = "Baskerville-" + FONT_SZ + ".vlw";
  public static String TITLE = "The Readers Project";
  public static int LAYOUT_BACKGROUND_COLOR = 255;
  public static int GRID_TEXT_COLOR = 0;
  public static int GRID_ALPHA = 40;
  public static String GRID_NAME = "test";
  protected static final Direction SW = Direction.SW; // keep

  protected boolean pause;
  protected RiTextGrid grid;
  // TODO JHC removed audio handlers for ELC3
  // private RiSample[] samples;

  public static void enableServer(String serverHost)
  {
    Readers.enableServer(serverHost);
  }

  public static void enableServer()
  {
    enableServer(null);
  }

  public static void info(String msg)
  {
    Readers.info(msg);
  }

  public void keyPressed()
  {
    if (MachineReader.PRODUCTION_MODE)
      return;
    
    if (key == 'v') {
      Readers.NO_VISUALS = !Readers.NO_VISUALS;
      //System.out.println("ReadersPApplet.keyPressed("+key+")");
    }

    if (keyCode == 157)
      return; // ignore alt-tabs

    if (keyCode != 39 && keyCode != 32)
      return;

    if (keyCode == 32)
      pause = !pause;

    if (grid != null)
    {
      clickOnReaders(grid.getReaders(false), keyCode);
    }
    else
    {
      PageManager pm = PageManager.getInstance();
      if (pm != null)
      {
        clickOnReaders(pm.getVerso().getReaders(false), keyCode);
        clickOnReaders(pm.getRecto().getReaders(false), keyCode);
      }
    }
  }

// TODO: JHC removed audio handling for ELC3
  
/*  protected void addAudioToRiTexts(String dir, RiText[] words, String wordSet)
  {
    long ts = System.currentTimeMillis();
    if (!wordSet.equals(""))
      wordSet = wordSet + "/";
    samples = new RiSample[words.length];
    for (int i = 0; i < samples.length; i++)
    {
      samples[i] = Readers.loadSample(words[i].pApplet, dir + wordSet + "word_" + i + ".mp3");
    }
    Readers.info("Loaded audio samples in " + Readers.elapsed(ts));
  }

  protected void addAudioToRiTexts(String dir, RiText[] words, Map sampleMap, String[] audioSets)
  {
    long ts = System.currentTimeMillis();
    //System.out.println("Total words: " + words.length);
    // iterate through words of text
    for (int j = 0; j < words.length; j++)
    {
      // creating an array of sample for each word
      // with length of the set of names of the various sets available
      RiSample[] samples = new RiSample[audioSets.length];
      for (int i = 0; i < audioSets.length; i++)
      {
        String wordSet = audioSets[i];
        if (!wordSet.equals(""))
          wordSet = wordSet + "/";
        // the RiTa method seems to have problems loading so many samples
        // needs huge memory at the moment
        samples[i] = Readers.loadSample(this, dir + wordSet + "word_" + j + ".wav");
      }
      sampleMap.put(words[j], samples);
      // just to see when and if it chokes
      //System.out.print(j + " ");
      //if ((j + 1) % 30 == 0)
        //System.out.println();
    }
    //System.out.println();
    Readers.info("Loaded audio samples in " + Readers.elapsed(ts));
  }

  protected void loadAudioFileNames(String dir, RiText[] words, Map sampleMap, String[] audioSets, boolean loadSamples)
  {
    if (loadSamples)
      addAudioToRiTexts(dir, words, sampleMap, audioSets);

    for (int j = 0; j < words.length; j++)
    {
      // creating an array of sample for each word
      // with length of the set of names of the various sets available
      String[] samples = new String[audioSets.length];
      for (int i = 0; i < audioSets.length; i++)
      {
        String wordSet = audioSets[i];
        if (!wordSet.equals(""))
          wordSet = wordSet + "/";
        samples[i] = dir + wordSet + "word_" + j + ".wav";
      }
      sampleMap.put(words[j], samples);
    }
  }

*/  private void clickOnReaders(MachineReader[] readers, int keyCode)
  {
    if (keyCode == 39)
    { // arrow
      if (pause)
        stepReaders(readers);
    }
    else if (keyCode == 32)
      togglePause(readers, pause);
  }

  private void togglePause(MachineReader[] readers, boolean pause)
  {
    for (int i = 0; i < readers.length; i++)
      readers[i].pause(pause);
  }

  private void stepReaders(MachineReader[] readers)
  {
    for (int i = 0; i < readers.length; i++)
      readers[i].stepForward();
  }

  /*
   * public void mouseClicked() // disabled for now... { // need to handle the case (single-page) when we don't have page-manager PageManager pm = PageManager.getInstance();
   * 
   * RiText rt = pm.getVerso().contains(mouseX, mouseY); if (rt == null) rt = pm.getRecto().contains(mouseX, mouseY); if (rt == null) return;
   * 
   * RiTextGrid grid = RiTextGrid.getGridFor(rt); MachineReader[] mrs = grid.getReaders(false); if (mrs.length != 1) // doesnt really make sense for multiples System.out.println("[WARN] Using first reader, ignoring others...");
   * 
   * MachineReader mr = mrs[0]; mr.pause(true); mr.runExitWordBehaviors(rt); mr.setCurrentCell(rt); mr.runEnterWordBehaviors(rt); }
   */

}
