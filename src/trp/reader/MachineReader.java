package trp.reader;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;

import processing.core.PApplet;

import rita.*;
import rita.support.HistoryQueue;

import trp.behavior.ReaderBehavior;
import trp.layout.*;
import trp.network.*;
import trp.util.*;

public abstract class MachineReader implements ReaderConstants
{
  public static boolean PRODUCTION_MODE = false, USE_SERVER = false;
  public static String SERVER_HOST = "lab-lamp.scm.cityu.edu.hk";// "rednoise.org"//"localhost";

  private static final int DEFAULT_HISTORY_SIZE = 10;

  // Statics --------------------------------------

  public static List instances = new ArrayList();
  private static boolean firstUpdate = true;

  // Members --------------------------------------

  protected List behaviors;
  protected RiText currentCell;
  protected RiTextGrid grid;
  protected HistoryQueue history; // last read cells
  protected MachineReader parent; // reader that spawned this one
  private ReaderBehavior defaultVisualBehavior;
  protected Direction lastDirection;
  protected String readerOutputFile;
  protected String[] readerOutput;

  protected PageManager pMan;

  public boolean testMode, printToConsole;
  private boolean stepping, dead, hasMoved, paused = true;
  protected boolean updatesDisabled, reverse;
  protected float readerColor[] = BRIGHT_RED, lifeSpan = Float.MAX_VALUE;
  protected long delay, birthTime, stepTimeMs = 1000;
  protected long originalStepTimeMs = stepTimeMs;
  private long triggerTime, pauseNetworkUntil;

  protected ReaderClient network;
  private PApplet _pApplet;
  private int id;

  // Constructors ---------------------------------

  public MachineReader(RiTextGrid grid)
  {
    this(grid, 0, 0);
  }

  public MachineReader(RiTextGrid grid, int startX, int startY)
  {
    this(grid, startX, startY, -1);
  }

  public MachineReader(RiTextGrid grid, int startX, int startY, float secondsBetweenSteps)
  {
    if (grid == null)
      throw new RuntimeException("Null grid passed to reader!");

    this._pApplet = grid._pApplet;

    this.grid = grid;

    // random id for server...
    this.id = (int) (Math.random() * Integer.MAX_VALUE);
    this.readerOutputFile = "reader" + this.id + "Output.txt";

    this.setGridPosition(startX, startY);
    this.setCurrentCell(grid.cellAt(startX, startY));

    if (secondsBetweenSteps > 0)
    {
      this.stepTimeMs = (long) (secondsBetweenSteps * 1000);
      this.originalStepTimeMs = this.stepTimeMs; // save
    }

    this.birthTime = System.currentTimeMillis();
    this.triggerTime = birthTime + stepTimeMs;

    this.history = new HistoryQueue(DEFAULT_HISTORY_SIZE);
    history.allowDuplicates();
    history.setGrowable(false);

    this.registerInstance(_pApplet);

    this.pMan = PageManager.getInstance();
  }

  // --------------------------- methods --------------------------

  public PApplet getPApplet()
  {
    return this._pApplet;
  }

  /**
   * Should return the next RiText that the reader will enter. If the returned RiText is null, the reader will remain where it is.
   */
  public abstract RiText selectNext();

  public boolean isDead()
  {
    return dead;
  }

  public boolean wasSpawned()
  {
    return (parent != null);
  }

  public void resetSpeed()
  {
    stepTimeMs = originalStepTimeMs;
  }

  public void adjustSpeed(float factor) // 1=same-speed
  {
    stepTimeMs = (long) (originalStepTimeMs * factor);
  }

  // dch said SHOULD REMOVE THIS METHOD -- only adjust !!
  // but from the code it seems to me that:
  // - originalStepTimeMs is the interval/speed when a MachineReader start()'s
  // - if the reader's setSpeed(ms) is changed originalStepTimeMs says the same
  // - adjustSpeed is currently relative to originalStepTimeMs
  // - the method below is a way to change the speed of the reader after it has started
  // and this is what we should do in sketches where speeds are user-configurable

  /** sets time for each step in seconds */
  public void setSpeed(float secondsBtwnSteps, boolean alsoResetOriginalSpeed)
  {
    this.stepTimeMs = (long) (secondsBtwnSteps * 1000f);
    if (alsoResetOriginalSpeed)
      this.originalStepTimeMs = stepTimeMs;
  }

  public void setSpeed(float secondsBtwnSteps)
  {
    this.stepTimeMs = (long) (secondsBtwnSteps * 1000f);
  }

  /** returns time for each step in seconds */
  public float getSpeed()
  {
    return stepTimeMs / 1000f;
  }

  /**
   * Called immediately as the reader enters the next cell, delegates to current behavior list.
   */
  public final void runEnterWordBehaviors(RiText rt)
  {
    // Readers.info("runEnterWordBehaviors("+this+","+currentCell+")");
    if (behaviors != null && !Readers.NO_VISUALS)
    {
      for (Iterator it = behaviors.iterator(); it.hasNext();)
      {
        ReaderBehavior rb = (ReaderBehavior) it.next();
        rb.enterWord(this, currentCell);
      }
    }
    if (this.stepping)
      this.paused = true;
  }

  /**
   * Called just before the reader exits the current cell, delegates to current behavior list.
   */
  public final void runExitWordBehaviors(RiText rt)
  {
    if (rt == null)
      return;

    // Readers.info("MachineReader.onExitWord()");
    if (behaviors != null && !Readers.NO_VISUALS)
    {
      for (Iterator it = behaviors.iterator(); it.hasNext();)
        ((ReaderBehavior) it.next()).exitWord(this, currentCell);
    }
  }

  // Methods -------------------------------------

  public Direction getLastDirection()
  {
    return lastDirection;
  }

  public void setLastDirection(Direction lastDirection)
  {
    this.lastDirection = lastDirection;
  }

  public void start()
  {
    this.originalStepTimeMs = stepTimeMs;
    this.paused = false;
  }

  /*
   * SPEC: enterWord behaviors should only be run after the word/cell as been selected by the reader (such a selectable is a word/cell that the reader really is entering; other word/cells it is offered are just skimmed over, although in the
   * case of some (simple) readers they accept/select any first word they're offered)
   * 
   * history should be added at the point of selection
   * 
   * So: hasMoved = false * point to first (possible) word * is this word selectable by the reader (run its selectNext() - but ?rename to selectWord or selectCell) * ? no: loop to select another * hasMoved ? then run exitWord behaviors (on
   * the word/cell just left - this is not run the first time) * set hasMoved = true * run enterWord behaviors and add history
   */
  public void draw()
  {
    if (dead)
      return;

    long now = System.currentTimeMillis();

    checkLifeSpan(now);

    if (!this.paused && now >= triggerTime) // time to fire
    {
      RiText wordBeingRead = null;

      runExitWordBehaviors(currentCell);

      verify(currentCell != null, "MachineReader.currentCell=null for: " + this);

      wordBeingRead = selectNext();

      // Following test required because spawned (PerigramDirectiona) readers
      // may 'die' and return null from their selectNext()
      if (wordBeingRead == null)
      {
        // DEBUG Readers.warn("wordBeingRead=null in "+this+".draw()");
        return;
      }

      // adjust grids if the wordBeingRead isn't on the current one
      if (!grid.contains(wordBeingRead))
      {
        // DEBUG -remove if (this == pMan.getFocusedReader()) Readers.info("Grid adjustment needed by: " + this + " at: " + this.getCurrentCell().text() + " next: " + wordBeingRead.text());

        RiTextGrid gridForNextWord = RiTextGrid.getGridFor(wordBeingRead);

        if (gridForNextWord == null)
          Readers.error("There is no grid for the next word: " + wordBeingRead.text());

        adjustGrids(gridForNextWord);

        if (this.paused)
        {
          // Readers.info("we're paused"); DEBUG -remove
          // we're flipping so return with currentCell as is
          return;
        }
        // boolean changeWasRejected = (grid == lastGrid);
        // if (changeWasRejected)
        // {
        // // pause the page-flipper if its trying to change grids during a flip
        // // Readers.info("Grid change rejected, pausing on "+currentCell);
        // return;
        // }
      }

      adjustSpeedForWord(wordBeingRead);

      history.add(wordBeingRead); // store last cell

      currentCell = wordBeingRead; // keep track of current word
      grid = RiTextGrid.getGridFor(currentCell);

      if (!dead) // enterWord
      {
        runEnterWordBehaviors(currentCell);

        // update the server
        if (USE_SERVER && !updatesDisabled)
        {
          String txt = getTextForServer(currentCell);
          sendUpdate(convertQuotes(txt));
        }
      }

      hasMoved = true; // not the 1st cell anymore

      triggerTime = now + stepTimeMs + delay; // reset start-time

      delay = 0; // reset the delay time, used in pause()
    }
  }

  protected void adjustSpeedForWord(RiText wordBeingRead)
  {

    // adjust speed relative to the number of syllables in the word being read
    // TODO: add a flag or option to keep speeds regular, optionally
    String wordString = stripPunctuation(wordBeingRead.text());
    this.adjustSpeed(1 + (countSyllables(RiTa.getSyllables(wordString)) - 1) * .3f);
    // System.out.println(wordString + ": "
    // + (1 + (countSyllables(RiTa.getSyllables(wordString)) - 1) * .3f));
  }

  private void checkLifeSpan(long now)
  {
    if (!dead && (lifeSpan >= 0 && getAge(now) >= lifeSpan))
      delete(this);
  }

  public void setTestMode(boolean b)
  {
    this.testMode = b;
  }

  protected String convertQuotes(String s)
  {
    if (s == null)
      return null;
    return s.replaceAll("[‘’”“`]", "'");
  }

  public String getTextForServer(RiText selected)
  {
    return selected == null ? "" : selected.text();
  }

  public static String stripPunctuation(String s)
  {
    return RiTa.stripPunctuation(s, ALLOWABLE_PUNCTUATION);
  }

  public static int countSyllables(String syllables)
  {
    return syllables.split("/").length;
  }

  protected void sendLineBreak()
  {
    if (USE_SERVER && !updatesDisabled)
      sendUpdate(" ");
  }

  protected boolean sendUpdate(String text)
  {
    if (text == null || updatesDisabled || System.currentTimeMillis() < pauseNetworkUntil)
      return false;

    if (network == null)
    {
      try
      {
        if (firstUpdate)
        {
          Readers.info("Updating " + SERVER_HOST + ":" + UDP_PORT);
          firstUpdate = false;
        }
        network = new UdpReaderClient(SERVER_HOST, UDP_PORT, getIdTag());
      }
      catch (Exception e)
      {
        Readers.error("Unable to create UdpReaderClient " + "(for reader# " + id + ") to " + SERVER_HOST + ":"
            + UDP_PORT);

        warnAndWait(PAUSE_BETWEEN_NETWORK_FAILURES_SEC);
      }
    }

    if (network != null)
    {
      Point p = currentCoords();
      if (network.updateServer(grid.getIdTag(), text, p.x, p.y))
      {
        pauseNetworkUntil = 0;
        return true;
      }
    }

    return false;
  }

  public Point currentCoords()
  {
    if (grid == null)
      Readers.error("Null grid for reader: " + this + "!");
    return grid.coordsFor(currentCell);
  }

  private String getIdTag()
  {
    return RiTa.shortName(this) + "#" + id;
  }

  public String getName()
  {
    return RiTa.shortName(this);
  }

  private void warnAndWait(float waitSecs)
  {
    String lastUrl = network != null ? network.getLastAttemptedUrl() : "unknown";
    Readers.warn("Reader # " + id + " unable to" + " contact server at:\n" + "       " + lastUrl
        + ", trying again in " + waitSecs + "s");
    pauseNetworkUntil = System.currentTimeMillis() + ((int) (waitSecs * 1000));
  }

  public float getAge(long _now)
  {
    return ((_now - birthTime) / 1000f);
  }

  public void pause(boolean b)
  {
    // System.out.println(this+".paused("+b+")");
    this.paused = b;
    if (!this.paused)
      this.stepping = false;
  }

  public static void delete(MachineReader reader)
  {
    // Readers.info(RiTa.shortName(reader)+" expired at: " +RiTa.elapsed());
    if (reader != null)
      reader.dead = true;
    instances.remove(reader);
    if (reader != null)
    {
      RiTextGrid rtg = reader.grid;
      if (rtg != null)
      {
        rtg._pApplet.unregisterMethod("draw", reader);
        // rtg._pApplet.unregisterDraw(reader);
      }
    }
  }

  /** Returns false if the reader has not yet moved, else true. */
  public boolean hasMoved()
  {
    return hasMoved;
  }

  public void setHasMoved(boolean b)
  {
    hasMoved = b;
  }

  /** Pause the reader (on the current cell) for the specified # of seconds. */
  public void pause(float delaySec)
  {
    this.delay = (long) delaySec * 1000;
  }

  public MachineReader setCurrentCell(RiText rt/* , boolean notifyListeners */)
  {
    // System.out.println(this+".currentCell: "+rt);

    this.currentCell = rt; // changed: 2/18
    this.grid = RiTextGrid.getGridFor(rt);

    if (currentCell == null)
      Readers.error("null cell: " + rt);

    if (grid == null)
      Readers.error("null grid for cell: " + rt);

    return this;
  }

  /*
   * Will reject the grid change if we are a page-turner and this would cause a page-flip,
   * when pages are already flipping; in such cases 'grid' remains unchanged.
   */
  protected void adjustGrids(RiTextGrid newGrid)
  {
    // Should we tell the PageManager to do a flip?
    if (pMan == null)
    {
      Readers.error("Attempt to change grid on a single page layout!");
      return;
    }

    if (this == pMan.getFocusedReader()) // only if we're a page-turner
    {
      // if (pMan.isFlipping()) // but reject if we're already flipping!
      // return grid;
      pMan.onGridChange(this, this.grid, newGrid);
    }
  }

  public void setGridPosition(int x, int y)
  {
    RiText rt = grid.cellAt(x, y);
    if (rt == null)
    {
      Readers.warn("Position (" + x + "," + y + ") does not exist on " + grid + ", using 0,0!");
      rt = grid.cellAt(0, 0);
    }
    this.currentCell = rt;
  }

  @SuppressWarnings("unused")
  private static void getReadersOnCell(List result, RiTextGrid theGrid, int xVal, int yVal)
  {
    for (Iterator it = instances.iterator(); it.hasNext();)
    {
      MachineReader mr = (MachineReader) it.next();
      RiTextGrid rtg = mr.getGrid();
      Point p = rtg.coordsFor(mr.currentCell);
      if (rtg == theGrid && p.x == xVal && p.y == yVal)
        result.add(mr);
    }
  }

  public static MachineReader getReaderById(int id)
  {
    for (Iterator it = instances.iterator(); it.hasNext();)
    {
      MachineReader mr = (MachineReader) it.next();
      if (mr.id == id)
        return mr;
    }
    return null;
  }

  private static void getReadersOnCell(List l, RiText cell)
  {
    for (Iterator it = instances.iterator(); it.hasNext();)
    {
      MachineReader mr = (MachineReader) it.next();
      if (mr.getCurrentCell() == cell)
        l.add(mr);
    }
  }

  public static MachineReader[] getReadersOnCell(RiText cell)
  {
    List l = new ArrayList();
    getReadersOnCell(l, cell);
    return (MachineReader[]) l.toArray(new MachineReader[l.size()]);
  }

  public static int numReadersOnCell(RiText cell)
  {
    List l = new ArrayList();
    getReadersOnCell(cell);
    return l.size();
  }

  private void registerInstance(PApplet p)
  {
    instances.add(this);
    if (p != null)
    {
      p.registerMethod("draw", this);
      // p.registerDraw(this);
    }
  }

  public void setPrintToConsole(boolean printToConsole)
  {
    this.printToConsole = printToConsole;
  }

  public float getLifeSpan()
  {
    return lifeSpan;
  }

  /**
   * Sets the lifespan for the reader in seconds
   * <p>
   * Note: set to -1 for infinity
   */
  public void setLifeSpan(float _lifeSpan)
  {
    this.lifeSpan = _lifeSpan;
  }

  /**
   * This is a way to decouple behaviors (on enter/exit words) from the specific readers. Note that a reader can have multiple behaviors, which are called in the order they were added.
   */
  public void addBehavior(ReaderBehavior behavior)
  {
    if (behaviors == null)
      behaviors = new ArrayList();
    behaviors.add(behavior);
  }

  /**
   * This is a way to decouple behaviors (on enter/exit words)
   * from the specific readers. Note that this method first clears
   * all current behaviors, then sets this as the sole behavior.
   */
  public void setBehavior(ReaderBehavior behavior)
  {
    if (behaviors == null)
      behaviors = new ArrayList();
    else
      behaviors.clear();
    if (behavior != null)
      behaviors.add(behavior);
  }

  public RiTextGrid getGrid()
  {
    if (grid == null)
      Readers.error("MachineReader(" + this + ") has null grid!");
    return grid;
  }

  public RiText getCurrentCell()
  {
    return currentCell;
  }

  // Note: does not take into account shift for recto pages
  public Point2D.Float position()
  {
    float[] center = currentCell.center();
    return new Point2D.Float(center[0], center[1]);
  }

  public static void destroyAll()
  {
    try
    {
      for (int i = 0; i < instances.size(); i++)
        delete((MachineReader) instances.get(i));
    }
    catch (Exception e)
    {
    }
  }

  public void setReverse(boolean reverseMotion)
  {
    this.reverse = reverseMotion;
  }

  public void stepForward()
  {
    stepping = true;
    paused = false;
  }

  public boolean isPaused()
  {
    return paused;
  }

  public void setUpdatesDisabled(boolean updatesDisabled)
  {
    this.updatesDisabled = updatesDisabled;
  }

  public RiText getLastReadCell()
  {
    return getPreviouslyReadCell(1);
  }

  /**
   * returns a previously read cell, if 'numInPast' = 1, this is equivalent to getLastReadCell() if 'numInPast' = 2, this is the cell read before that, etc.
   */
  public RiText getPreviouslyReadCell(int numInPast)
  {
    return (history.size() > numInPast) ? (RiText) history.get(history.size() - (numInPast + 1)) : null;
  }

  /**
   * returns an ArrayList with howMany of the last cells read
   */
  public ArrayList getRecentlyReadCells(int howMany)
  {
    ArrayList resultList = new ArrayList();
    if (history.size() < 1)
      return null;
    for (int i = 1; i < (howMany + 1); i++)
    {
      if (i > history.size())
        break;
      resultList.add(history.get(history.size() - i));
    }
    return resultList;
  }

  public HistoryQueue getHistory()
  {
    return history;
  }

  /** Returns true if the history contains the String ignoring case */
  public boolean inHistory(String s)
  {
    for (Iterator it = history.iterator(); it.hasNext();)
    {
      RiText rt = (RiText) it.next();
      if (rt == null)
        continue;
      if (rt.text().equalsIgnoreCase(s))
        return true;
    }
    return false;
  }

  /**
   * Creates (and fills) a new history queue of the requested size (for this reader only)
   */
  public void setHistorySize(int sz)
  {
    if (history.size() == sz)
      return;
    HistoryQueue nextHistory = new HistoryQueue(sz);
    for (Iterator it = nextHistory.iterator(); it.hasNext();)
    {
      nextHistory.add(it.next());

    }
    history = nextHistory;
  }

  // remove??? NO: leave this in
  public void setDefaultVisualBehavior(ReaderBehavior defaultVisualsBehavior)
  {
    this.defaultVisualBehavior = defaultVisualsBehavior;
    addBehavior(defaultVisualsBehavior);
  }

  public ReaderBehavior getDefaultVisualBehavior()
  {
    return defaultVisualBehavior;
  }

  public void setParent(MachineReader spawner)
  {
    this.parent = spawner;
  }

  public void verify(boolean b, String msg)
  {
    Readers.verify(b, msg);
  }

  public void verify(boolean b)
  {
    Readers.verify(b);
  }

  public synchronized void jumpToPage(RiTextGrid right)
  {
    this.grid = right;
    currentCell = right.getRandomCell();
  }

  public void clearBehaviors()
  {
    behaviors.clear();
  }

  public ReaderClient getNetworkClient()
  {
    return network;
  }

  public void writeOutput()
  {
    _pApplet.saveStrings(readerOutputFile, readerOutput);
  }

  public String getOutputFile()
  {
    return readerOutputFile;
  }
}// end
