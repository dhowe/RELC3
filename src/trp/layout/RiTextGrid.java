package trp.layout;

import java.awt.Point;
import java.util.*;

import processing.core.*;
import rita.*;
import rita.render.PageLayout;
import trp.reader.MachineReader;
import trp.util.Readers;

public class RiTextGrid
{
  public static float[] DEFAULT_COLOR = { 0, 0, 0, 255 };

  private static List instances = new ArrayList();
  private static boolean memoizeNeighbors;
  private static Map neighborCache;

  // -------------------- members ---------------------
  protected String origWords[][], rawText, name = "";
  protected RiText template, cells[][];

  protected PageLayout layout;
  protected int id, titleY = 40;

  public RiTextGrid prev, next;
  public RiText title, footer;
  public float x, y, width, footerMargin = 10, scale = 1;

  public PApplet _pApplet;

  // -------------------- constructors ----------------

  public RiTextGrid(PApplet p)
  {
    this(p, (RiText[]) null);
  }

  public RiTextGrid(PApplet pApplet, String fileName, int startX, int startY, int NOTUSED)
  {
    this(pApplet, RiText.createLines(pApplet, RiTa.loadString(/*pApplet, */fileName), startX, startY, pApplet.width
        - startX * 2, pApplet.height - startY * 2));
  }

  public RiTextGrid(PApplet pApplet, String fileName, int startX, int startY, int width, int height)
  {
    this(pApplet, RiText.createLines(pApplet, RiTa.loadString(/*pApplet,*/ fileName), startX, startY, width, height));
  }

  /* This uses 'lines' as is, without doing any auto-breaks */
  public RiTextGrid(PApplet pApplet, String[] lines, int startX, int startY)
  {
    this(pApplet, RiText.createLines(pApplet, lines, startX, startY, -1));
  }

  private RiTextGrid(PApplet pApplet, RiText[] lines)
  {
    this._pApplet = pApplet;
    if (lines != null)
      buildGrid(lines);
    this.width = pApplet.width;
  }

  // Note: this is a separate constructor from the one directly above
  public RiTextGrid(PApplet pApplet, PageLayout layout)
  {
    this._pApplet = pApplet;
    if (layout != null)
    {
      this.x = layout.textRectangle.x;
      this.y = layout.textRectangle.y;
      this.width = layout.pageWidth;
      RiText[] lines = (RiText[]) layout.getLines();

      if (lines == null || lines.length < 1)
        Readers.error("Cannot create a grid with no text!");

      buildGrid(lines);
      setFooter((RiText) layout.footer);
    }
  }

  // ----------------------- methods -----------------------

  /** returns max number of characters in a word on all grids */
  public static int computeMaxWordLength()
  {
    int maxWordLength = 0;
    for (Iterator it1 = instances.iterator(); it1.hasNext();)
    {
      RiTextGrid grid = (RiTextGrid) it1.next();
      for (Iterator it = grid.iterator(); it.hasNext();)
      {
        RiText rt = (RiText) it.next();
        int rtl = rt.length();
        if (rtl > maxWordLength)
          maxWordLength = rtl;
      }
    }
    return maxWordLength;
  }

  public MachineReader[] getReaders(boolean ignorePausedReaders)
  {
    List l = new ArrayList();

    for (Iterator it = MachineReader.instances.iterator(); it.hasNext();)
    {
      MachineReader mr = (MachineReader) it.next();
      if (ignorePausedReaders && mr.isPaused())
      {
        // Readers.info("Skipping paused reader: "+mr);
        continue;
      }
      if (mr.getGrid() == this)
      {
        l.add(mr);
      }
    }
    return (MachineReader[]) l.toArray(new MachineReader[l.size()]);
  }

  public void setFooter(RiText footer)
  {
    this.footer = footer;
  }

  public void draw()
  {

    this.draw(_pApplet.g);
  }

  public void draw(PGraphics g)
  {
    if (Readers.NO_VISUALS)
      return;

    drawCells(g);
    drawNonCells(g);
  }

  public void drawCells(PGraphics g)
  {
    g.pushMatrix();
    g.scale(scale);
    for (Iterator it = iterator(); it.hasNext();)
      ((RiText) it.next()).draw(g);
    g.popMatrix();
  }

  public void drawCells(PGraphics g, float tx)
  {
    g.scale(scale);
    for (Iterator it = iterator(); it.hasNext();)
    {
      RiText rt = (RiText) it.next();
      g.pushMatrix();
      float[] ctr = rt.center();
      float distFromCenterX = width / 2f - ctr[0];
      float currentTrans = (2 * distFromCenterX) * tx;
      g.translate(rt.x + (rt.textWidth() / 2f) + currentTrans, rt.y, 0);
      g.rotateY((float) (tx * Math.PI));
      g.translate(-rt.x - (rt.textWidth() / 2f), -rt.y, 0);
      rt.draw(g);
      g.popMatrix();
    }
  }

  public void drawCellsX(PGraphics g, float tx)
  {
    // Readers.info("tx="+tx);
    g.scale(scale);
    for (Iterator it = iterator(); it.hasNext();)
    {
      g.pushMatrix();
      float distFromCenterX = 0;
      g.translate((2 * -distFromCenterX) * tx, 0, 0);
      ((RiText) it.next()).draw(g);
      g.popMatrix();
    }
  }

  public void drawNonCells(PGraphics g)
  {
    g.pushMatrix();
    g.scale(scale);
    if (title != null)
      title.draw(g);
    if (footer != null)
      footer.draw(g);
    g.popMatrix();
  }

  public void buildGrid(RiText[] lines)
  {
    if (lines == null || lines.length == 0)
      return;

    /*
     * Code to dispose any lines that are off the screen
     * 
     * Note: this may cause problems with multi-page layouts where there is code
     * to do the same thing, page-by-page
     */
    int numLinesOnScreen = 0;
    float height = _pApplet.height;
    for (int y = 0; y < lines.length; y++)
    {
      if (lines[y].y >= (height - footerMargin))
      {
        Readers.warn("Text beyond grid end, ignoring line: " + lines[y]);
        RiText.dispose(lines[y]);
        continue;
      }
      numLinesOnScreen++;
    }

    cells = new RiText[numLinesOnScreen][];
    for (int y = 0; y < numLinesOnScreen; y++)
    {
      cells[y] = lines[y].splitWords();
      RiText.dispose(lines[y]);
    }

    for (Iterator it = iterator(); it.hasNext();)
      ((RiText) it.next()).fill(DEFAULT_COLOR);

    setId((int) (Math.random() * Integer.MAX_VALUE));

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < numLinesOnScreen; i++)
      sb.append(lines[i].text() + " ");
    rawText = sb.toString().trim();

    this.buildTemplateCell(cells);
    this.prev = this.next = this;
    this.origWords = toStringArray();
    if (memoizeNeighbors)
      neighborCache = new HashMap();
    instances.add(this);
    // Readers.info("[INFO] Creating grid #" + getId());
  }

  private void setId(int i)
  {
    this.id = i;
  }

  protected void buildTemplateCell(RiText[][] rts)
  {
    try
    {
      template = rts[0][0].copy();
    }
    catch (Exception e)
    {
      template = new RiText(_pApplet);
    }
    template.fill(DEFAULT_COLOR);
    template.position(Float.MAX_VALUE, Float.MAX_VALUE); // this makes it invisible
    // template.alpha(0); // don't do this: messes with fades back to DEFAULT_COLOUR
  }

  public void showBoundingBoxes(boolean b)
  {
    for (Iterator it = iterator(); it.hasNext();)
    {
      ((RiText) it.next()).showBounds(b);
    }
  }

  public String toString()
  {
    String name = "*";
    PageManager pm = PageManager.getInstance();
    if (pm != null)
    {
      if (this == pm.getNext())
        name = "next";
      else if (this == pm.getVerso())
        name = "verso";
      else if (this == pm.getRecto())
        name = "recto";
    }
    if (1 == 1)
      return "grid(" + name + ") #" + id;
    else
      return asString();
  }

  public String asString()
  {
    String s = "\n";
    for (int i = 0; i < cells.length; i++)
    {
      for (int j = 0; j < cells[i].length; j++)
      {
        if (j == 0)
          s += i + "] ";
        s += cells[i][j].text() + " ";
      }
      s += "\n";
    }
    return s;
  }

  /**
   * Returns the cell (a RiText) that contains (mx,my) or null if none does
   */
  public RiText contains(float mx, float my)
  {
    RiText[] rts = RiText.picked(mx, my);
    return (rts == null) ? null : rts[0];
  }

  /**
   * @invisible public void mouseEvent(MouseEvent e) { float mx = e.getX();
   *            float my = e.getY(); switch (e.getID()) { case
   *            MouseEvent.MOUSE_CLICKED: lastClicked = null; RiText[] rts =
   *            RiText.getPicked(mx, my); if (rts != null) lastClicked = rts[0];
   *            break; } }
   */

  /** @invisible */
  public void dispose()
  {
    instances.remove(this);
    if (layout != null)
      layout = null;
    for (Iterator it = iterator(); it.hasNext();) {
    	RiText rt = (RiText) it.next();
    	rt.text("");
      RiText.dispose(rt);
    }
    RiText.dispose(template);
  }

  public void visible(boolean b)
  {
  	float alphaVal = b ? ReadersPApplet.GRID_ALPHA : 0;
  	
    // Readers.info("RiTextGrid.visible("+b+")");
    for (Iterator it = iterator(); it.hasNext();) {
      ((RiText) it.next()).alpha(alphaVal);
    }
    
    if (title != null)
      title.alpha(alphaVal);
    if (footer != null)
      footer.alpha(alphaVal);
  }

  /** returns an Iterator over all the RiTexts in the grid */
  public Iterator iterator()
  {
    return getRiTexts().iterator();
  }

  /** returns a List of all the RiTexts in the grid */
  public List getRiTexts()
  {
    List l = new ArrayList();
    getRiTexts(l);
    return l;
  }

  public void getRiTexts(List l)
  {
    for (int i = 0; cells != null && i < cells.length; i++)
    {
      for (int j = 0; j < cells[i].length; j++)
        l.add(cells[i][j]);
    }
  }

  /** Returns all the coords for RiTexts that exactly match the given String */
  public Point[] coordsMatching(String word)
  {
    RiText[] rts = wordsMatching(word);
    Point[] pts = new Point[rts.length];
    for (int i = 0; i < pts.length; i++)
      pts[i] = coordsFor(rts[i]);
    return pts;
  }

  /** Returns all the RiTexts exactly matching the given String */
  public RiText[] wordsMatching(String word)
  {
    List l = new ArrayList();
    for (Iterator it = iterator(); it.hasNext();)
    {
      RiText rt = (RiText) it.next();
      if (word.equals(rt.text()))
        l.add(rt);
    }
    return toRiTextArr(l);
  }

  public static RiText[] toRiTextArr(List l)
  {
    return (RiText[]) l.toArray(new RiText[l.size()]);
  }

  /**
   * Locates the grid on which this RiText exists, then returns the grid (x,y)
   * coordinates for it
   */
  public static Point getGridCoordsFor(RiText rt)
  {
    return getGridFor(rt).coordsFor(rt);
  }

  public static RiText nextCellAfter(RiText rt)
  {
    return getGridFor(rt).nextCell(rt);
  }

  /**
   * Returns the grid on which the RiText exists
   */
  public static RiTextGrid getGridFor(RiText rt)
  {
    if (rt == null)
    {
      Readers.warn("Null RiText passed to RiTextGrid.getGridFor()");
      return null;
    }

    for (Iterator it = instances.iterator(); it.hasNext();)
    {
      RiTextGrid g = (RiTextGrid) it.next();
      if (g.contains(rt))
        return g;
    }

    Readers.warn("Illegal state in RiTextGrid.getGridFor(), no grid for RiText: " + rt);
    //throw new RuntimeException("Illegal state in RiTextGrid.getGridFor(), no grid for RiText: " + rt);

    return null;
  }

  /** Returns true if the RiText exists on this grid */
  public boolean contains(RiText rt)
  {
    if (cells == null)
      return false;
    for (int i = 0; i < cells.length; i++)
    {
      for (int j = 0; j < cells[i].length; j++)
      {
        if (cells[i][j] == rt)
          return true;
      }
    }
    return false;
  }

  /* Returns the grid coords for the given RiTexts on this grid only */
  private Point coords(RiText rt)
  {
    if (rt == null)
      return null;

    for (int i = 0; i < cells.length; i++)
    {
      for (int j = 0; j < cells[i].length; j++)
      {
        if (cells[i][j] == rt)
          return new Point(j, i);
      }
    }
    return null;
  }

  /**
   * Returns the grid coords for the given RiText, checking each grid in turn if
   * it does not exist on the current grid
   */
  public Point coordsFor(RiText rt)
  {
    if (rt == null)
      return null;

    Point p = coords(rt);
    if (p != null)
      return p;

    for (Iterator it = instances.iterator(); it.hasNext();)
    {
      RiTextGrid g = (RiTextGrid) it.next();
      if (g == this)
        continue;
      p = g.coords(rt);
      if (p != null)
        return p;
    }

    Readers.error(rt + " not found on any grid! " + rt);

    return null;
  }

  /**
   * Returns the next word according to typical reading patterns, right->left,
   * top->bottom. The returned cell may be on a different grid than this one.
   */
  public RiText nextCell(RiText rt)
  {
    Point pt = coordsFor(rt);
    if (pt == null)
    {
      Readers.error("No coords for " + rt + "!");
      return null;
    }
    return nextCell(pt.x, pt.y);
  }

  /**
   * Returns the previous word according to typical reading patterns,
   * right->left, top-bottom. The returned cell may be on a different grid than
   * this one.
   */
  public RiText previousCell(RiText rt)
  {
    if (rt == null)
    {
      Readers.warn("Null RiText passed to previousCell()");
      return null;
    }
    Point pt = coordsFor(rt);
    if (pt == null)
    {
      Readers.error("RiTextGrid.previousCell():  No coords for RiText(" + rt
          + ") on grid: " + this);
      return null;
    }
    return previousCell(pt.x, pt.y);
  }

  /**
   * Returns the next word according to typical reading patterns, right->left,
   * top-bottom. The returned cell may be on a different grid than this one.
   */
  public RiText nextCell(Point p)
  {
    return nextCell(p.x, p.y);
  }

  /**
   * Returns the next word according to typical reading patterns, right->left,
   * top-bottom. The returned cell may be on a different grid than this one.
   */
  private RiText nextCell(int x, int y)
  {
    RiTextGrid g = this;

    // try next word on the line
    int newY = y, newX = x + 1;

    // are we at end of line?
    RiText[] rts = g.lineAt(newY);
    if (rts == null)
      return null;

    if (newX >= rts.length)
    {
      // if so, move to 1st word of next line
      newX = 0;
      newY++;
    }

    // is it the last line? if so, move to top of the next
    if (newY >= numLines())
    {
      newY = 0;
      g = getNext();
    }

    // find word at those coords
    RiText next = g.cellAt(newX, newY);

    // double-check
    if (next == null)
    {
      boolean recto = (g == PageManager.getInstance().getRecto());
      boolean verso = (g == PageManager.getInstance().getVerso());
      Readers.error("RiTextGrid.nextCell(" + x + "," + y + ")->" + cellAt(x, y)
          + " returned " + "null for " + this + " [verso=" + verso + " recto=" + recto
          + " newX=" + newX + " newY=" + newY + "]");
    }

    return next;
  }

  /**
   * Returns the previous word according to typical reading patterns,
   * right->left, top-bottom.
   */
  public RiText previousCell(Point p)
  {
    return previousCell(p.x, p.y);
  }

  /**
   * Returns the previous word according to typical reading patterns,
   * right->left, top->bottom.
   */
  private RiText previousCell(int x, int y)
  {
    RiTextGrid g = this;

    Readers.verify(cellAt(x, y) != null, "previousCell(" + x + "," + y
        + ") -> no cell at x=" + x + ", y=" + y + " for grid:\n" + g);

    // try prev word on the line
    int newY = y, newX = x - 1;

    // are we at beginning of line?
    if (newX < 0)
      newY--; // if so, first reduce Y

    // was it the first line?
    if (newY < 0)
    {
      // move back to prev grid
      RiTextGrid pg = g.getPrevious();

      if (pg == null)
        Readers.error("Null previous grid in previousCell(" + x + "," + y + ")" + g);

      // WHY DOES THIS HAPPEN???
      if (g == pg)
        ;// Readers.warn("previousCell() -> Previous grid is identical to grid...");

      // if so, set to prev last line
      newY = pg.numLines() - 1;

      g = pg; // grid is now previous
    }

    // set newX to end of the new line
    if (newX < 0)
    {
      if (g.lineAt(newY) == null)
      {

        Readers.error("No line #" + newY + " on " + g);

        g = PageManager.getInstance().getVerso();
        Readers.warn("Resetting reader to verso(0,0)");

        newX = 0;
        newY = 0;
      }
      else
        newX = g.lineAt(newY).length - 1;
    }

    RiText rt = g.cellAt(newX, newY);

    if (rt == null)
    {
      boolean recto = (g == PageManager.getInstance().getRecto());
      boolean verso = (g == PageManager.getInstance().getVerso());
      Readers.error("RiTextGrid.previousCell(" + x + "," + y + ")->" + cellAt(x, y)
          + " returned " + "null for " + this + " [verso=" + verso + " recto=" + recto
          + " newX=" + newX + " newY=" + newY + "]");
    }

    return rt;
  }

  /**
   * Returns the RiText on the grid cell with the specified coords.
   */
  public RiText cellAt(Point p)
  {
    if (p == null)
      return null;
    return cellAt(p.x, p.y);
  }

  /**
   * Returns the RiText on the grid cell with the specified coords.
   */
  public RiText cellAt(int _x, int _y)
  {
    RiText[] line = lineAt(_y);
    if (line == null)
      return null;
    return (_x >= 0 && _x < line.length) ? line[_x] : null;
  }

  /**
   * @see #neighborhood(int, int)
   */
  public RiText[] neighborhood(Point p)
  {
    return neighborhood(cellAt(p));
  }

  /**
   * @see #neighborhood(int, int)
   */
  public RiText[] neighborhood(int x, int y)
  {
    return neighborhood(cellAt(x, y));
  }

  /**
   * Returns the 9-cell neighborhood for the specified cell ([4]), as follows:<br>
   * 
   * <pre>
   * [0] [1] [2]
   * [3] [4] [5]
   * [6] [7] [8]
   * </pre>
   * 
   * One or more of these cells may be null if they are off the edge of the
   * grid.
   * <p>
   * Positions (for layout above) are specified as follows:
   * 
   * <pre>
   *         [4] = the current word
   *         [3] = the preceding word
   *         [5] = the next word
   *         
   *         [2] = rightmost overlapping word on line above
   *               (if first line return null)
   *               but if current is last word in line AND
   *               boundingBox.x + width &gt;= that of the
   *               last word on line above
   *               then = first word of current line
   *         [8] = rightmost overlapping word on line below
   *               (including the first line)
   *               but if current is last word on line AND
   *               boundingBox.x + width &gt;= that of the
   *               last word on line below
   *               then = first word 2 lines below wrapping
   *               to first line
   *         
   *         [1] = overlapping word preceding 2, otherwise null
   *         [7] = overlapping word preceding 8, otherwise null
   *         
   *         [0] = overlapping word preceding 1, otherwise null
   *         [6] = overlapping word preceding 7, otherwise null
   * </pre>
   */
  public RiText[] neighborhood(final RiText center)
  {
    if (center == null)
    {
      Readers.warn("Null RiText passed to RiTextGrid.neighborhood()");
      return new RiText[9];
    }

    RiTextGrid rtg = getGridFor(center);

    if (rtg == null)
    {
      Readers.warn("Null grid in RiTextGrid.neighborhood()");
      return new RiText[9];
    }
    
    Readers.verify(rtg.contains(center), "Grid does not contain center: " + center
        + "\n\ncoords(" + center + ") returns " + rtg.coords(center) + "\n" + this);

    RiText[] rts = null;
    if (memoizeNeighbors)
    {
      rts = checkNeighborCache(center);
      if (rts != null)
        return rts;
    }

    RiText over = null, under = null;
    Point pOver = null, pUnder = null;
    Point cOver = rtg.coords(center);

    if (cOver == null)
    {
      System.err.println("[WARN] No coords " + "for center: " + center
          + ", returning empty array!!!");
      return new RiText[9];
    }

    int lineY = cOver.y, lineX = cOver.x;
    rts = new RiText[9];

    // get cell directly above if not 1st line
    // we do not allow the first line to wrap back to end of text
    if (lineY > 0)
    {
      over = rtg.bestAbove(center, 3);

      if (over == null) // nothing directly above
      {
        // use 1st word of current line
        over = rtg.cellAt(0, lineY);
      }
      else
      {
        // if lastWord and its bbx-end < current.bb-end
        // set it to first word of current line;
        if (rtg.isLineEnd(center) && rtg.rightOutdentIsGreaterOrEqual(center, over))
        {
          over = rtg.cellAt(0, lineY);
        }
      }
      // now get the point for 'over' = rts[2]
      pOver = rtg.coordsFor(over);
    }
    else
    {
      // J: on first line of grid - what should we do here?
    }

    // get cell directly below
    if (lineY < rtg.numLines() - 1) // not the last line
    {
      under = rtg.bestBelow(center, 3);
      if (under == null) // nothing directly under
      {
        // general case
        if (lineY < rtg.numLines() - 2)
        {
          under = rtg.cellAt(0, lineY + 2);
        }

        // select first line of next grid
        else if (lineY == rtg.numLines() - 2)
        {
          under = rtg.next.cellAt(0, 0);
        }
      }
      else
      // (under != null)
      {
        // if lastWord and its bbx-end < current.bb-end
        // set it to first word of current line;
        if (rtg.isLineEnd(center) && rtg.rightOutdentIsGreaterOrEqual(center, under))
        {
          under = (lineY < rtg.numLines() - 2) ? rtg.cellAt(0, lineY + 2)
              : rtg.cellAt(0, lineY + 1);
        }
      }
      pUnder = rtg.coordsFor(under);
    }
    else // last line, so wrap
    {
      RiTextGrid gnext = rtg.getNext();

      RiText[] firstLine = gnext.lineAt(0);
      for (int i = firstLine.length - 1; i >= 0; i--)
      {
        if (gnext.intersectsOnX(center, firstLine[i], 3))
          under = firstLine[i];
      }
      if (under == null)
        Readers.warn("Illegal state, " + "nothing under: " + center + " (on next grid)");
      pUnder = gnext.coordsFor(under);
    }

    // center row (3,4,5)
    if (lineX > 0)
      rts[3] = rtg.previousCell(center);
    rts[4] = center;
    // always wrap, to defer test: if (x < lineAt(y).length - 1)
    rts[5] = rtg.nextCell(center);

    // top row (2,1,0)
    if (over != null)
    {
      rts[2] = over;

      RiTextGrid top = rtg;
      // always wrap or test: if (pOver.x > 0) {
      rts[1] = top.previousCell(pOver);
      if (rts[1] != null)
      {
        if (!top.intersectsOnX(rts[1], rts[4], 0))
          rts[1] = null;
        else
          top = RiTextGrid.getGridFor(rts[1]);
      }

      // always wrap or: }
      if (rts[1] != null)
      {
        pOver = top.coordsFor(rts[1]);
        // always wrap or test: if (pOver.x > 0) {
        rts[0] = top.previousCell(pOver);

        // what if rts[0] is on a diff grid?

        if (rts[0] != null && !top.intersectsOnX(rts[0], rts[4], 0))
          rts[0] = null;
        // always wrap or: }
      }
    }

    // bottom row (8,7,6)
    if (under != null)
    {
      rts[8] = under;

      // what if rts[8] is on a the next grid?
      RiTextGrid next = RiTextGrid.getGridFor(rts[8]);

      // always wrap or test: if (pUnder.x > 0)
      rts[7] = next.previousCell(pUnder);

      if (rts[7] != null)
      {

        next = RiTextGrid.getGridFor(rts[8]); // need this?

        if (!next.intersectsOnX(rts[7], rts[4], 0))
          rts[7] = null;
        else
          // what if rts[7] is on a differnt grid?
          next = RiTextGrid.getGridFor(rts[7]);
      }

      if (rts[7] != null)
      {
        pUnder = next.coordsFor(rts[7]);
        // always wrap or test: if (pUnder.x > 0) {
        rts[6] = next.previousCell(pUnder);

        // what if rts[6] is on a diff grid?

        if (rts[6] != null && !next.intersectsOnX(rts[6], rts[4], 0))
          rts[6] = null;
      }
    }

    if (memoizeNeighbors)
      neighborCache.put(center, rts);

    return rts;
  }

  private static RiText[] checkNeighborCache(RiText key)
  {
    Object o = neighborCache.get(key);
    return o == null ? null : (RiText[]) o;
  }

  // if its bbx-end < current.bb-end
  private boolean rightOutdentIsGreaterOrEqual(RiText center, RiText over)
  {
    float[] centerbb = center.boundingBox(), overbb = over.boundingBox();
    return (centerbb[0] + centerbb[2] >= overbb[0] + overbb[2]);
  }

  /** returns true if 'rt' is the last word on its line */
  public boolean isLineEnd(RiText rt)
  {
    Point p = coordsFor(rt);
    int lineLength = lineAt(p.y).length;
    return p.x == lineLength - 1;
  }

  /**
   * Returns all words whose bounding boxes contain the x position of the RiText
   * specified, plus or minus the amount of 'slop' specified.
   */
  public List bestBelows(RiText rt, float slop)
  {
    Point p1 = coordsFor(rt);
    Point p2 = null;
    List l = new ArrayList();
    if (p1.y < numLines() - 1)
    {
      Iterator it = iterator();
      while (it.hasNext())
      {
        RiText test = (RiText) it.next();
        p2 = coordsFor(test);
        if (p1.y == p2.y - 1 && intersectsOnX(rt, test, slop))
          l.add(test);
      }
    }
    return l;
  }

  private List bestAboves(RiText rt, float slop)
  {
    List l = new ArrayList();
    if (rt == null)
      return l;
    Point p1 = coordsFor(rt);
    Point p2 = null;
    if (p1.y > 0)
    {
      Iterator it = iterator();
      while (it.hasNext())
      {
        RiText test = (RiText) it.next();
        p2 = coordsFor(test);
        if (p1.y == p2.y + 1 && intersectsOnX(rt, test, slop))
          l.add(test);
      }
    }
    return l;
  }

  public RiText bestBelow(RiText rt, float slop)
  {
    RiText best = null;
    float bestY = 0;
    List l = bestBelows(rt, slop);
    for (Iterator it = l.iterator(); it.hasNext();)
    {
      RiText cand = (RiText) it.next();
      if (cand.x > bestY)
      {
        bestY = cand.x;
        best = cand;
      }
    }
    return best;
  }

  public RiText bestAbove(RiText rt, float slop)
  {
    RiText best = null;
    float bestY = 0;
    List l = bestAboves(rt, slop);
    for (Iterator it = l.iterator(); it.hasNext();)
    {
      RiText cand = (RiText) it.next();
      if (cand.x > bestY)
      {
        bestY = cand.x;
        best = cand;
      }
    }
    return best;
  }

  public boolean intersectsOnX(RiText rt1, RiText rt2, float slop)
  {
    if (rt1 == null || rt2 == null)
      return false;
    return oneWayOverlapX(rt1, rt2, slop) || oneWayOverlapX(rt2, rt1, slop);
  }

  private boolean oneWayOverlapX(RiText rt1, RiText rt2, float slop)
  {

    RiTextGrid g1 = RiTextGrid.getGridFor(rt1);
    RiTextGrid g2 = RiTextGrid.getGridFor(rt2);
    float[] bb = rt2.boundingBox();

    float rt1x = rt1.x - g1.x;
    float bb2x = bb[0] - g2.x;
    float pad = 1;

    return (rt1x + pad > (bb2x + pad - slop) && rt1x - pad < (bb2x + bb[2] - pad + slop));
  }

  public RiText[] lineAt(int _y)
  {
    if (cells == null || _y < 0 || _y > cells.length - 1)
      return null;
    return cells[_y];
  }

  /**
   * Sets the new text for a given RiText and attempts to adjust its position on
   * the line by balancing space on either side.
   * 
   * TODO: should reset the line in those cases where it doesn't fit
   * 
   * @see #resetLine(int)
   */
  public void textFor(RiText rt, String newText)
  {
    if (rt != null)
    {

      float oldWidth = rt.textWidth();

      rt.text(newText);

      float widthDiff = oldWidth - rt.textWidth();

      rt.x += widthDiff / 2f;
    }
    // return rt;
  }

  /**
   * Resets word spacing for the entire line
   */
  public void resetLine(int lineIdx)
  {
    if (cells == null || lineIdx < 0 || lineIdx > cells.length - 1)
      return;

    String sofar = "";
    RiText[] line = lineAt(lineIdx);

    relineate(sofar, line);
  }

  private void relineate(String sofar, RiText[] line)
  {
    if (line == null || line.length < 1)
      return;

    float xStart = line[0].x;
    for (int i = 0; i < line.length; i++)
    {
      if (i > 0)
      {
        line[i].x = xStart + _pApplet.textWidth(sofar);
      }
      sofar += (line[i].text() + " ");
    }
  }

  public int numLines()
  {
    if (cells == null || cells.length < 1)
      return 0;
    return cells.length;
  }

  public int numCells()
  {
    if (cells == null)
      return 0;
    int numWords = 0;
    for (int i = 0; i < cells.length; i++)
    {
      numWords += cells[i].length;
    }
    return numWords;
  }

  /**
   * Sets the current color for the entire grid (not including the template).
   */
  public void setColor(float gray)
  {
    this.setColor(gray, gray, gray, template.alpha(), false);
  }

  /**
   * Sets the current color for the entire grid (not including the template).
   */
  public void setColor(float r, float g, float b)
  {
    this.setColor(r, g, b, template.alpha(), false);
  }

  /**
   * Sets the current color for the entire grid (not including the template).
   */
  public void setColor(float r, float g, float b, float a)
  {
    this.setColor(r, g, b, a, false);
  }

  /**
   * Sets the current color for the entire grid (not including the template).
   */
  public void setColor(float[] color)
  {
    float r = color[0], g = 0, b = 0, a = 255;
    switch (color.length)
    {
      case 4:
        g = color[1];
        b = color[2];
        a = color[3];
        break;
      case 3:
        g = color[1];
        b = color[2];
        break;
      case 2:
        g = color[0];
        b = color[0];
        a = color[1];
        break;
    }
    this.setColor(r, g, b, a);
  }

  /**
   * Sets the color for the entire grid, updating the template only if
   * 'updateTemplate' is set to true
   */
  public void setColor(float r, float g, float b, float a, boolean updateTemplate)
  {
    for (Iterator it = iterator(); it.hasNext();)
      ((RiText) it.next()).fill(r, g, b, a);
    if (updateTemplate)
      template.fill(r, g, b, a);
  }

  public void textFont(PFont pf)
  {
    for (Iterator it = iterator(); it.hasNext();)
      ((RiText) it.next()).font(pf);
  }

  public void fadeOut(float seconds)
  {
    for (Iterator it = iterator(); it.hasNext();)
      ((RiText) it.next()).fadeOut(seconds);
  }

  public void textFont(String fontName, float fontSize)
  {
    textFont(_pApplet.createFont(fontName, fontSize));
  }

  /**
   * Returns the template for the grid -- use this to access the original
   * properties of a grid cell, so that, for example, one or more can be
   * restored.
   */
  public RiText template()
  {
    return template;
  }

  /**
   * Sets the RiText template for the grid which is generally used to access the
   * original properties of a grid cell, so that, for example, one or more can
   * be restored.
   */
  public void setTemplate(RiText template)
  {
    this.template = template;
  }

  /**
   * Returns the coordinates of the last cell in the grid.
   */
  public Point lastCellCoords()
  {
    int newY = numLines() - 1;
    return new Point(lineAt(newY).length - 1, newY);
  }

  /**
   * Returns the RiText for last cell in the grid.
   */
  public RiText lastCell()
  {
    return cellAt(lastCellCoords());
  }

  /**
   * Returns a cell at a random point on the grid.
   */
  public RiText getRandomCell()
  {
    int randY = (int) (Math.random() * numLines());
    RiText[] line = lineAt(randY);
    return line[(int) (Math.random() * line.length)];
  }

  /**
   * Returns the raw text used to create the grid.
   * <p>
   * Note: does not update with changes to individual cell
   */
  public String getRawText()
  {
    return rawText;
  }

  public void scale(float scl)
  {
    for (Iterator it = iterator(); it.hasNext();)
      ((RiText) it.next()).scale(scl);
  }

  public void shiftText(float xOffset)
  {
    for (Iterator it = iterator(); it.hasNext();)
      ((RiText) it.next()).x += xOffset;
    if (title != null)
      title.x += xOffset;
    if (footer != null)
      footer.x += xOffset;
  }

  public String[][] toStringArray()
  {
    return toStringArray(false);
  }

  public String[][] toStringArray(boolean updateWithCurrentGridWord)
  {
    if (origWords == null || updateWithCurrentGridWord)
      origWords = createStringArray();
    return origWords;
  }

  public String[][] createStringArray()
  {
    int gridLen = cells.length;
    // Readers.info("gridLen: " + gridLen);
    String[][] gWords = new String[gridLen][];
    for (int i = 0; i < gridLen; i++)
    {
      int lineLen = cells[i].length;
      gWords[i] = new String[lineLen];
      // Readers.info("lineLen: " + lineLen);
      for (int j = 0; j < lineLen; j++)
      {
        gWords[i][j] = cellAt(j, i).text();
      }
    }
    return gWords;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getIdTag()
  {
    String nm = "";
    if (name != null && name.length() > 0)
      nm = "%20(" + name + ")";
    return "#" + gridId + nm;
  }

  static int gridId = 100 + ((int) (Math.random() * 900));

  public static void linkGrids(RiTextGrid... g)
  {
    for (int i = 1; i < g.length; i++)
    {
      g[i - 1].next = g[i];
      g[1].prev = g[i - 1];
    }
    g[g.length - 1].next = g[0];
    g[0].prev = g[g.length - 1];
  }

  public String getTitleStr()
  {
    String titleStr = "";
    if (title != null)
      titleStr = title.text();
    return titleStr;
  }

  public void setTitle(RiText riText)
  {
    this.title = riText;
  }

  public void setTitle(String s)
  {
    this.setTitle(s, _pApplet.width / 2, titleY);
  }

  public void setTitle(String s, int x, int y)
  {
    this.setTitle(s, x, y, null);
  }

  public void setTitle(String s, int x, int y, PFont pf)
  {
    // Readers.info("RiTextGrid.setTitle("+s+")");
    if (title == null)
      title = new RiText(_pApplet, x, y);
    if (pf != null)
      title.font(pf);
    title.textAlign(_pApplet.CENTER);
    title.text(s);
  }

  public void setFooter(String s)
  {
    this.setFooter(s, _pApplet.width / 2, 40);
  }

  public void setFooter(String s, int x, int y)
  {
    // Readers.info("RiTextGrid.setFooter("+s+")");
    if (footer == null)
      footer = new RiText(_pApplet);
    footer.textAlign(_pApplet.CENTER);
    footer.text(s);
    footer.position(x, y);
  }

  public RiTextGrid getNext()
  {
    return next;
  }

  public RiTextGrid getPrevious()
  {
    return prev;
  }

  public static void defaultColor(int r, int g, int b, int a)
  {
    DEFAULT_COLOR = new float[] { r, g, b, a };
  }

  /**
   * Returns the original word as set when the grid was created; subsequent
   * changes to grid cells are not reflected here.
   */
  private String originalWordAt(int _x, int _y)
  {
    try
    {
      return origWords[_y][_x];
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      Readers.error("origWords.length=" + origWords.length + " but y=" + _y);
    }
    return null;
  }

  /**
   * Restores the text in the specified RiText to its initial value when the
   * grid (whichever one it is on) was created.
   * 
   * @return the original (now current) text for this cell
   */
  public static String resetTextFor(RiText rt)
  {
    return resetTextFor(rt, -1);
  }

  /**
   * Called in this form this method: Restores the text in the specified RiText
   * to its initial value when the grid (whichever one it is on) was created,
   * but fades in over the specified float seconds and does not adjust the
   * position of the RiText.
   * 
   * @return the original (now current) text for this cell
   */
  public static String resetTextFor(RiText rt, float fadeInSecs)
  {
    if (rt == null)
      return "";
    RiTextGrid rtg = getGridFor(rt);
    Point p = rtg.coordsFor(rt);
    String newWord = rtg.originalWordAt(p.x, p.y);
    String oldWord = rt.text();
    if (oldWord == null)
      return "";
    if (!oldWord.equals(newWord))
    {
      if (fadeInSecs == -1)
        rtg.textFor(rt, newWord);
      else
      {
        // System.out.println("is doing a fadeToText");
        rt.textTo(newWord, fadeInSecs);
      }
    }
    return rt.text();
  }

  public static String originalTextFor(RiText rt)
  {
    RiTextGrid rtg = getGridFor(rt);
    Point p = rtg.coordsFor(rt);
    return rtg.originalWordAt(p.x, p.y);
  }

  @SuppressWarnings("unused")
  private RiText getClosestCell(RiText rt)
  {
    if (rt == null)
      throw new RuntimeException("Bad Cell: " + rt);
    RiText best = null;
    float bestDist = Integer.MAX_VALUE;
    for (Iterator it = iterator(); it.hasNext();)
    {
      RiText cand = (RiText) it.next();
      if (cand == null)
        continue;
      float d = rt.distanceTo(cand);
      if (d < bestDist)
      {
        best = cand;
        bestDist = d;
      }
    }
    return best;
  }

  /**
   * Should reset all cells in the grid to their original (templated) visual
   * state. Pass 'true' if all behaviors (fades, etc) should also stop
   * immediately.
   */
  public void reset(boolean stopBehaviors)
  {
    for (Iterator it = iterator(); it.hasNext();)
    {
      RiText rt = (RiText) it.next();
      RiTextGrid.resetTextFor(rt);
      // if (stopBehaviors) rt.completeBehaviors(); // not sure??
      rt.fill(template.fill());
      rt.boundingFill(template.boundingFill());
      rt.showBounds(template.showBounds());
    }
  }

  public float getScale()
  {
    return scale;
  }

  public void setScale(float scale)
  {
    this.scale = scale;
  }

  public boolean onLastLine(RiText rt)
  {
    Readers.verify(contains(rt));
    Point p = coords(rt);
    return (p.y == numLines() - 1);
  }

  public boolean onFirstLine(RiText rt)
  {
    Readers.verify(contains(rt));
    Point p = coords(rt);
    return (p.y == 0);
  }

  public static boolean isMultiPage()
  {
    return instances.size() > 1;
  }

  /**
   * Returns the number of lines between the RiTexts assuming 2 conditions: 1)
   * that the first comes before the second and 2) that they are on subsequent
   * grids in the text. IF either conditiond is false, will return
   * Integer.MAX_VALUE.
   */
  public static int yDistance(RiText rt1, RiText rt2)
  {
    RiTextGrid grid1 = getGridFor(rt1);
    RiTextGrid grid2 = getGridFor(rt2);
    int y1 = (int) grid1.coordsFor(rt1).getY();
    int y2 = (int) grid2.coordsFor(rt2).getY();

    if (grid1 == grid2)
      return (y2 < y1) ? Integer.MAX_VALUE : (y2 - y1);

    if (grid1.next != grid2)
      return Integer.MAX_VALUE;

    int distToEnd = grid1.numLines() - y1;
    return y2 + distToEnd;
  }

  public PApplet getPApplet()
  {
    return _pApplet;
  }

}// end
