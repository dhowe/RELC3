package trp.layout;

import static trp.util.Direction.*;

import rita.*;

import java.util.*;

import processing.core.*;
import trp.behavior.*;
import trp.reader.*;
import trp.util.PerigramLookup;

public class ELC3Multi extends MultiPageApplet
{

  static final String[] TEXTS = { "textual/poeticCaption.txt", "textual/misspeltLandings.txt",
      "textual/image.txt" };
  static final String[] READER_NAMES = { "Perigram", "Simple Spawner", "Perigram Spawner",
      "Mesostic Jumper" };
  static final String[] TEXT_NAMES = { "POETIC CAPTION", "MISSPELT LANDINGS", "THE IMAGE" };
  static final String[] VISUAL_NAMES = { "Default visuals", "Haloed" };

  static Map SPEED_MAP, COLOR_MAP;
  static ReaderBehavior[] BEHAVIORS;
  static PFont[] FONTS;

  ReaderBehavior neighborFading, spawningVB, defaultVisuals, tendrilsDGray, neighborFadingNoTrails, haloing,
      mesostic;
  ButtonSelect textSelect, wordMonitor, readerSelect, speedSelect, visualSelect, colorSelect;
  float readerColor[] = OATMEAL, readerSpeed = 0.5f;
  String currentWord = "", lastWord = "";
  RiTextGrid verso, recto;
  RiText currentCell;

  public void settings()
  {

    // fullScreen();
    size(1280, 720);
  }

  public void setup()
  {

    fontSetup();
    colorSetup();
    buttonSetup();

    pManager = PageManager.create(this, 40, 40, 38, 30);
    pManager.showPageNumbers(false);
    pManager.setApplicationId("elc3");
    pManager.decreaseGutterBy(20);
    pManager.setVersoHeader("");
    pManager.setRectoHeader("");

    doLayout(0);

    if (PRESENTATION_MODE)
      noCursor(); // only hide cursor in a configurable PRESENTATION mode
  }

  private void doLayout(int i)
  {

    pauseReaders();
    resetButtons();

    pManager.clear();
    pManager.setLeading(30);
    pManager.setFont(FONTS[i]);
    pManager.addTextFromFile(TEXTS[i]);
    pManager.doLayout();

    constructReadersFor(new PerigramLookup(this, TEXTS[i]));
  }

  public void mouseClicked()
  {

    if (pManager.flipping)
      return;

    ButtonSelect clicked = ButtonSelect.click(mouseX, mouseY);
    if (clicked != null)
    {

      // TEXT
      if (clicked == textSelect)
      {

        doLayout(textIdxFromName(clicked.value()));
      }

      // READER
      else if (clicked == readerSelect)
      {

        MachineReader rd = currentReader();
        RiText rt = rd.getCurrentCell();
        rd.pause(true);

        currentReaderIdx = readerIdxFromName(clicked.value());

        rd = currentReader();
        // System.out.println("click: "+clicked.value()+", "+currentReaderIdx);
        rd.setSpeed((float) SPEED_MAP.get(speedSelect.value()));
        rd.setCurrentCell(rt);
        rd.pause(false);

        setVisuals(visualSelect.value(), readerColor, isSpawner(currentReaderIdx));

        pManager.onUpdateFocusedReader(rd);
      }

      // SPEED
      else if (clicked == speedSelect)
      {

        readerSpeed = (float) SPEED_MAP.get(clicked.value());
        currentReader().setSpeed(readerSpeed);
      }

      // VISUALS
      else if (clicked == visualSelect)
      {
        setVisuals(clicked.value(), readerColor, isSpawner(currentReaderIdx));
      }

      // COLOR - of reader
      else if (clicked == colorSelect)
      {

        readerColor = (float[]) COLOR_MAP.get(clicked.value());

        if (visualSelect.value().equals("Haloed"))
        {
          setVisuals("Haloed", readerColor, isSpawner(currentReaderIdx));
        }
        else
        {
          BEHAVIORS[currentReaderIdx].setReaderColor(readerColor);
        }
      }
    }
  }

  private boolean isSpawner(int currentReaderIdx)
  {
    return currentReaderIdx == 1 || currentReaderIdx == 2;
  }

  public void fontSetup()
  {

    PFont bask = loadFont("Baskerville-25.vlw");
    PFont gill = loadFont("GillSansMT-24.vlw");
    FONTS = new PFont[] { bask, gill, bask };
  }

  private void colorSetup()
  {

    COLOR_MAP = new LinkedHashMap();
    COLOR_MAP.put("Oatmeal", OATMEAL);
    COLOR_MAP.put("Ochre", MOCHRE);
    COLOR_MAP.put("Brown", MBROWN);
    COLOR_MAP.put("Yellow", MYELLOW);

    // grid color setup
    LAYOUT_BACKGROUND_COLOR = BLACK_INT; // CHANGE THIS TO INVERT; > 127 dark on light
    int gridcol = (LAYOUT_BACKGROUND_COLOR > 127) ? 0 : 255;
    GRID_ALPHA = 40; // EDIT could also be set from preferences in production
    RiTextGrid.defaultColor(gridcol, gridcol, gridcol - GRID_ALPHA, GRID_ALPHA);
  }

  private void buttonSetup()
  {

    SPEED_MAP = new LinkedHashMap(); // must be LinkedHashMap to preserve keySet() orders below
    SPEED_MAP.put("Fast", 0.5f);
    SPEED_MAP.put("Per-second", 1.0f);
    SPEED_MAP.put("Slow", 1.5f);
    SPEED_MAP.put("Slower", 2.0f);
    SPEED_MAP.put("Slowest", 2.5f);
    SPEED_MAP.put("Very fast", 0.25f);

    ButtonSelect.TEXT_FILL = BLACK;
    ButtonSelect.STROKE_WEIGHT = 0;

    int buttonY = 691;
    textSelect = new ButtonSelect(this, 0, buttonY, "Text", TEXT_NAMES);
    wordMonitor = new ButtonSelect(this, 0, buttonY, "Monitor", new String[] { "monitors the current word" }); // the string passed here determines the width of the button
    readerSelect = new ButtonSelect(this, 0, buttonY, "Reader", READER_NAMES);
    speedSelect = new ButtonSelect(this, 0, buttonY, "Speed", (String[]) SPEED_MAP.keySet().toArray(new String[0]));
    visualSelect = new ButtonSelect(this, 0, buttonY, "Visual", VISUAL_NAMES);
    colorSelect = new ButtonSelect(this, 0, buttonY, "Color", (String[]) COLOR_MAP.keySet().toArray(new String[0]));

    int totalWidth = 0;
    for (int i = 0; i < ButtonSelect.instances.size(); i++)
    {
      totalWidth += ButtonSelect.instances.get(i).getWidth();
    }

    int nextX = (width - totalWidth) / 2;
    for (int i = 0; i < ButtonSelect.instances.size(); i++)
    {
      ButtonSelect bs = ButtonSelect.instances.get(i);
      bs.x = nextX;
      nextX += bs.getWidth();
    }
  }

  public void constructReadersFor(PerigramLookup perigrams)
  {

    currentReaderIdx = 0; // reset back to first reader
    currentCell = pManager.getVerso().cellAt(0, 0);
    readerSpeed = (float) SPEED_MAP.get("Fast");
    verso = pManager.getVerso();
    recto = pManager.getRecto();

    constructBehaviorsFor(perigrams);

    if (READERS == null)
      READERS = new MachineReader[READER_NAMES.length];

    // PERIGRAM
    MachineReader.delete(READERS[0]);
    READERS[0] = new PerigramReader(verso, perigrams);
    READERS[0].setSpeed(readerSpeed); // was 0.5f
    READERS[0].setBehavior(neighborFading);

    // SIMPLE READING SPAWNER - nb: has different default speed
    MachineReader.delete(READERS[1]);
    READERS[1] = new SimpleReader(verso);
    READERS[1].setSpeed((float) SPEED_MAP.get("Slow")); // was 1.7f
    READERS[1].setBehavior(defaultVisuals);
    READERS[1].addBehavior(spawningVB);

    // PERIGRAM SPAWNER
    MachineReader.delete(READERS[2]);

    READERS[2] = new PerigramReader(verso, perigrams);
    READERS[2].setSpeed(readerSpeed); // was 0.6f
    READERS[2].setBehavior(neighborFadingNoTrails);
    READERS[2].addBehavior(spawningVB);

    // MESOSTIC JUMPER - nb: has different default speed
    MachineReader.delete(READERS[3]);
    READERS[3] = new MesoPerigramJumper(verso, "reading as writing through", perigrams);
    READERS[3].setSpeed((float) SPEED_MAP.get("Slow"), true);
    READERS[3].setBehavior(mesostic);

    for (int i = 0; i < READERS.length; i++)
    {
      READERS[i].start();
      READERS[i].pause(currentReaderIdx != i);
    }

    // TODO: workaround! see draw method where this is done:
    // pManager.onUpdateFocusedReader(currentReader());
  }

  public void constructBehaviorsFor(PerigramLookup perigrams)
  {
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

    // these appear to be the default behaviors, at least when not haloing?
    // (not sure if we need to keep recreating them over and over)
    BEHAVIORS = new ReaderBehavior[] { neighborFading, defaultVisuals, neighborFadingNoTrails, mesostic };
  }

  protected void setVisuals(String visuals, float[] color, boolean isSpawner)
  {

    if (visuals.equals("Haloed"))
    {

      if (currentReaderIdx == 3) // mesostic reader
      {
        haloing = new MesosticHaloingVisual(color, verso.template().fill(), readerSpeed);
      }
      else
      {
        haloing = new ClearHaloingVisual(color, verso.template().fill(), readerSpeed);
      }
      currentReader().setBehavior(haloing);
      if (isSpawner)
      {
        currentReader().addBehavior(spawningVB);
      }

    }
    else
    {

      readerColor = color;

      switch (currentReaderIdx)
      {

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

  public void draw()
  {

    background(LAYOUT_BACKGROUND_COLOR);

    // BUTTONS drawn only if page is not flipping
    if (!pManager.flipping)
    {

      if (mouseY >= textSelect.y && mouseY < (textSelect.y + textSelect.height))
      {

        for (int i = 0; i < ButtonSelect.instances.size(); i++)
          ButtonSelect.instances.get(i).textFill = WHITE;

        if (PRESENTATION_MODE)
          cursor();

      }
      else
      {

        for (int i = 0; i < ButtonSelect.instances.size(); i++)
          ButtonSelect.instances.get(i).textFill = BLACK;

        if (PRESENTATION_MODE)
          noCursor();
      }

      currentWord = currentReader().getCurrentCell().text();
      currentWord = RiTa.stripPunctuation(currentWord);
      if (!currentWord.equals(lastWord))
      {

        int numOfSyllables = countSyllables(RiTa.getSyllables(currentWord));

        // currentReader().adjustSpeed(1f + (numOfSyllables * 2f)); TODO: how to
        // adjust speed per-step properly??

        // DH: what behavior do you want? adjustSpeed takes a multiplier,
        // so you can pass 1.1 to speed up by 10% or .9 to slow down by 10%

        wordMonitor.setValue(currentWord);
        lastWord = currentWord;
      }

      ButtonSelect.drawAll(mouseX, mouseY);
    }

    pManager.draw(g);
  }

  public int readerIdxFromName(String name)
  {

    for (int i = 0; i < READER_NAMES.length; i++)
      if (READER_NAMES[i].equals(name))
        return i;
    return -1;
  }

  public int textIdxFromName(String name)
  {

    for (int i = 0; i < TEXT_NAMES.length; i++)
      if (TEXT_NAMES[i].equals(name))
        return i;
    return -1;
  }

  private void resetButtons()
  {

    speedSelect.advanceTo("Fast");
    readerSelect.advanceTo("Perigram");
    visualSelect.advanceTo("Default visuals");
  }

  private static void pauseReaders()
  {

    for (int i = 0; READERS != null && i < READERS.length; i++)
    {
      READERS[i].pause(true);
    }
  }

  private static int countSyllables(String syllables)
  {

    return syllables.split("/").length;
  }

  public static void main(String[] args)
  {

    info("Running " + ELC3Multi.class.getName());
    // String[] options = { "--present", "--hide-stop","--bgcolor=#000000",
    String[] options = { "--hide-stop", "--bgcolor=#000000", ELC3Multi.class.getName() };
    PApplet.main(options);
  }

}// end
