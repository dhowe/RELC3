package trp.layout;

import static trp.util.Direction.*;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import rita.RiText;
import trp.behavior.*;
import trp.reader.*;
import trp.util.PerigramLookup;
import trp.util.Readers;

public class ELC3Multi extends MultiPageApplet
{
  static final String[] TEXTNAMES = { "Poetic Caption", "Misspelt Landings", "The Image" };
  static final String[] READERNAMES = { "Perigram", "Simple Spawner", "Perigram Spawner", "Mesostic Jumper" };
  static final String[] SPEEDNAMES = { "Fast", "Per-second", "Slow", "Slower", "Slowest", "Very fast" };
  static final String[] VISUALNAMES = { "Default visuals", "Haloed" };
  static final String[] COLORNAMES = { "Oatmeal", "Ochre", "Brown", "Yellow" };

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

  ButtonSelect textSelect, readerSelect, speedSelect, visualSelect, colorSelect;
  RiTextGrid verso, recto;
  float[] readerColor = OATMEAL;
  float readerSpeed = 0.5f;
  RiText currentCell;

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
    colorSelect = new ButtonSelect(this, 1100, BUTTONS_Y, "Color", COLORNAMES);

    // grid color setup
    LAYOUT_BACKGROUND_COLOR = BLACK_INT; // CHANGE THIS TO INVERT; > 127 dark on light
    int gridcol = (LAYOUT_BACKGROUND_COLOR > 127) ? 0 : 255;
    READER_MONOCOLOR = (gridcol == 0) ? BLACK : WHITE;
    GRID_ALPHA = 40; // EDIT could also be set from preferences in production
    RiTextGrid.defaultColor(gridcol, gridcol, gridcol - GRID_ALPHA, GRID_ALPHA);

    for (int i = 0; i < READERNAMES.length; i++)
    {
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

  private void doLayout(String fileName)
  {
    /// System.out.println("ELC3Multi.doLayout("+text+")");
    pauseReaders();
    pManager.clear();
    pManager.setLeading(30);
    // pManager.setFont(loadFont(FONT_VLW));
    pManager.addTextFromFile(fileName);
    pManager.doLayout();

    currentCell = pManager.getVerso().cellAt(0, 0);
    readerSpeed = (float) SPEED_MAP.get("Fast");

    // TODO: we should reset buttons and labels at this point

    constructReadersFor(fileName);
  }

  private void pauseReaders()
  {
    for (int i = 0; READERS != null && i < READERS.length; i++)
    {
      READERS[i].pause(true);
    }
  }

  protected MachineReader perigramReader, simpleReadingSpawner, perigramReadingSpawner, mesosticJumper;
  protected ReaderBehavior neighborFading, spawningVB, defaultVisuals, tendrilsDGray, neighborFadingNoTrails,
      haloing, mesostic;
  protected PerigramLookup perigrams;

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

  public void constructReadersFor(String text)
  {
    currentReaderIdx = 0;
    
    verso = pManager.getVerso();
    recto = pManager.getRecto();

    constructVBsFor(text);

    // PERIGRAM
    if (perigramReader != null)
      MachineReader.delete(perigramReader);
    perigramReader = new PerigramReader(verso, perigrams);
    perigramReader.setCurrentCell(currentCell); // was setGridPosition(1, 0) because of page turn!
    perigramReader.setSpeed(readerSpeed); // was 0.5f
    perigramReader.setBehavior(neighborFading);

    // SIMPLE READING SPAWNER - TODO: has different default speed
    if (simpleReadingSpawner != null)
      MachineReader.delete(simpleReadingSpawner);
    simpleReadingSpawner = new SimpleReader(verso);
    simpleReadingSpawner.setCurrentCell(currentCell);
    simpleReadingSpawner.setSpeed((float) SPEED_MAP.get("Slow")); // was 1.7f
    simpleReadingSpawner.setBehavior(defaultVisuals);
    simpleReadingSpawner.addBehavior(spawningVB);
    simpleReadingSpawner.setTestMode(false);

    // PERIGRAM SPAWNER
    if (perigramReadingSpawner != null)
      MachineReader.delete(perigramReadingSpawner);
    perigramReadingSpawner = new PerigramReader(verso, perigrams);
    perigramReadingSpawner.setCurrentCell(currentCell);
    perigramReadingSpawner.setSpeed(readerSpeed); // was 0.6f
    perigramReadingSpawner.setBehavior(neighborFadingNoTrails);
    perigramReadingSpawner.addBehavior(spawningVB);

    // MESOSTIC JUMPER - TODO: has different default speed
    if (mesosticJumper != null)
      MachineReader.delete(mesosticJumper);
    mesosticJumper = new MesoPerigramJumper(verso, MESOSTIC, perigrams);
    mesosticJumper.setCurrentCell(currentCell);
    mesosticJumper.setSpeed((float) SPEED_MAP.get("Slow"), true);
    mesosticJumper.setBehavior(mesostic);

    READERS = new MachineReader[] { perigramReader, simpleReadingSpawner, perigramReadingSpawner, mesosticJumper };
    for (int i = 0; i < READERS.length; i++)
    {
      READERS[i].start();
      READERS[i].pause(currentReaderIdx != i);
    }

    // TODO: workaround, see draw method: pManager.onUpdateFocusedReader(currentReader());
  }

  public void mouseClicked()
  {
    ButtonSelect clicked = ButtonSelect.click(mouseX, mouseY);
    if (clicked != null)
    {
      // System.out.println(clicked.label + "=" + clicked.value());
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
        readerSelect.advanceTo("Perigram");
        speedSelect.advanceTo("Fast");
        visualSelect.advanceTo("Default visuals");
        colorSelect.advanceTo("Oatmeal");
      }
      else if (clicked == readerSelect)
      {
        MachineReader current = currentReader();
        current.pause(true);
        RiText rt = current.getCurrentCell();
        currentReaderIdx = (int) READER_MAP.get(clicked.value());
        current = READERS[currentReaderIdx];
        current.setSpeed((float) SPEED_MAP.get(speedSelect.value()));
        current.setCurrentCell(rt);
        current.pause(false);

        pManager.onUpdateFocusedReader(current); // TODO: ? problem with focus!
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
          haloing = new ClearHaloingVisual(readerColor, verso.template().fill(), readerSpeed);
          current.setBehavior(haloing);
        }
        else
        {
          switch (currentReaderIdx)
          {
            case 0: // perigram
              currentReader().setBehavior(neighborFading);
              colorSelect.advanceTo("Oatmeal");
              speedSelect.advanceTo("Fast");
              break;
            case 1: // simple spawner
              currentReader().setSpeed((float) SPEED_MAP.get("Slow"));
              currentReader().setBehavior(defaultVisuals);
              currentReader().addBehavior(spawningVB);
              colorSelect.advanceTo("Ochre");
              speedSelect.advanceTo("Slow");
              break;
            case 2: // perigram spawner
              currentReader().setSpeed((float) SPEED_MAP.get("Fast"));
              currentReader().setBehavior(neighborFadingNoTrails);
              currentReader().addBehavior(spawningVB);
              colorSelect.advanceTo("Ochre");
              speedSelect.advanceTo("Fast");
              break;
            case 3: // mesostic
              currentReader().setSpeed((float) SPEED_MAP.get("Slow"), true);
              currentReader().setBehavior(mesostic);
              colorSelect.advanceTo("Yellow");
              speedSelect.advanceTo("Slow");
              break;

            default:
              break;
          }
        }
      }
      else if (clicked == colorSelect)
      {
        readerColor = (float[]) COLOR_MAP.get(clicked.value());
        // currentReader().setC
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
      colorSelect.textFill = WHITE;
      if (PRESENTATION_MODE)
        cursor();
    }
    else
    {
      textSelect.textFill = BLACK;
      readerSelect.textFill = BLACK;
      speedSelect.textFill = BLACK;
      visualSelect.textFill = BLACK;
      colorSelect.textFill = BLACK;
      if (PRESENTATION_MODE)
        noCursor();
    }
    ButtonSelect.drawAll(mouseX, mouseY);

    pManager.draw(g);

    // TODO: ugly workaround
    if (MachineReader.OK_TO_FOCUS)
    {
      pManager.onUpdateFocusedReader(currentReader());
      MachineReader.OK_TO_FOCUS = false;
    }

  }

  public static void main(String[] args)
  {
    info("Running " + ELC3Multi.class.getName());
    // String[] options = { "--present", "--hide-stop","--bgcolor=#000000",
    String[] options = { "--hide-stop", "--bgcolor=#000000", ELC3Multi.class.getName() };
    PApplet.main(options);
  }

}// end
