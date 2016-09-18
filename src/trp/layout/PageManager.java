package trp.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import rita.RiTa;
import rita.RiText;
import rita.render.*;
import rita.support.BehaviorListener;
import trp.network.HttpFocusThread;
import trp.reader.MachineReader;
import trp.util.ReaderConstants;
import trp.util.Readers;

/*
 * New graphics approach? (b/c most pixels stay the same)
 *   -- draw each page once to off-screen buffer,
 *   -- only do incremental writes when a reader changes something
 *   -- redraw the buffer (w' spotlight) each frame 
 */
public class PageManager implements BehaviorListener, ReaderConstants, PConstants
{
  private static final String PAGE_BREAK = "<pb>";

  public static final float PAGE_FLIP_TIME = 5f;

  // singleton instance
  private static PageManager instance;

  // ---------------------- members --------------------------

  protected RiTextGrid left, right, next;
  private String versoHeader, rectoHeader;
  private PageLayout pageLayout;
  private MachineReader focusedReader;
  private RiLerpBehavior lerp;
  protected PApplet _pApplet;
  private List pages;
  private PFont font;
  private RiText[] allWords;

  @SuppressWarnings("unused")
  private boolean showPageNumbers = true, disablePageFlips, drawMidLine = false;
  protected boolean flipping, is3D, useGLGraphics, doServerFocus = false;

  private float[] textColor;
  private int pageCounter, headerY = 40;
  private float nextVisibleAt = 1.9f, rotateY = 2, scale = 1;
  public float gutterSubtraction = 0; // hack: redo
  private String appId = "test";

  // spotlight vars (remove)
  long[] spotlightTimers;
  float spotlightRadius = 200;
  Point2D.Float[] spotLightTargets, lastSpotTargets;
  public float minSpotlightBrightness = .2f, maxSpotlightBrightness = .8f;
  public static int spotlightMode = SPOTLIGHT_NONE; // ALL, NONE or SPOTLIGHT_FOCUS

  private HttpFocusThread focusThread;
  private PFont headerFont;

  public float leading;

  // ------------------- constructors ------------------------

  public static PageManager getInstance()
  {
    return instance;
  }

  public static PageManager create(PApplet pApplet, int leftMargin, int topMargin, int rightMargin, int bottomMargin)
  {
    if (instance == null)
    {
      RiText.defaults.indentFirstParagraph = false;

      /*
       * if (spotlightMode != SPOTLIGHT_NONE) {
       * instance = new ShadingPageManager(pApplet, leftMargin, topMargin,
       * rightMargin, bottomMargin, pApplet.width/2, pApplet.height);
       * }
       * else {
       */
      instance = new PageManager(pApplet, leftMargin, topMargin, rightMargin, bottomMargin, pApplet.width
          / 2, pApplet.height);
      // }
      System.out.println("[INFO] Created " + instance.getClass().getName());
    }
    return instance;
  }

  protected PageManager(PApplet pApplet, int leftMargin, int topMargin, int rightMargin, int bottomMargin, int pageWidth, int pageHeight)
  {
    this(pApplet, new Rect(leftMargin, topMargin, pageWidth - (leftMargin + rightMargin), pageHeight
        - (topMargin + bottomMargin)), pageWidth, pageHeight);
  }

  private PageManager(PApplet pApplet, Rect rect, int pageWidth, int pageHeight)
  {
    this(pApplet, new PageLayout(pApplet, rect, pageWidth, pageHeight));
  }

  private PageManager(PApplet p, PageLayout rpl)
  {
    if (p == null)
      Readers.error("Null PApplet");
    this._pApplet = p;
    this.pageLayout = rpl;
    is3D = false; // ??
    useGLGraphics = false;// is3D && p.ginstanceof codeanticode.glgraphics.GLGraphics;
    nextVisibleAt = is3D ? 1.9f : 1.7f;

  }

  // --------------------------- methods ------------------------------
  public void draw(PGraphics g)
  {
    // g.background(ReadersPApplet.LAYOUT_BACKGROUND_COLOR);

    g.pushMatrix();
    g.scale(scale);

    g.noStroke();
    left.draw(g);
    g.translate(g.width / 2f - gutterSubtraction, 0);
    if (rotateY < nextVisibleAt)
      next.draw(g);

    if (is3D)
    {
      if (rotateY < 2) // DH: hack to fix bug with bg image
        g.rotateY(rotateY * PI);
      float tx = (1 - (rotateY - 1));
      right.drawCells(g, tx);
      right.drawNonCells(g);
    }
    else if (right != null)
    {
      float tx = (g.width / 2f * (1 - (rotateY - 1)));
      g.translate(-tx, 0);
      right.draw(g);
    }

    if (flipping)
    {
      rotateY = lerp.getValue();
    }
    g.popMatrix();

    // checks focus selection
    if (Readers.serverEnabled() && doServerFocus && focusThread == null)
    {
      focusThread = new HttpFocusThread(this);
      focusThread.start();
    }
  }

  public void decreaseGutterBy(float gutterSubtraction)
  {
    this.gutterSubtraction = gutterSubtraction;
  }

  public void nextPage()
  {
    // Readers.info("PageManager.nextPage("+new Date()+")"); // DBUG-remove

    if (disablePageFlips)
      return;

    if (flipping == true)
    {
      Readers.info("Already flipping -- finish()");
      lerp.finish();
    }

    flipping = true;

    // fade verso grid during flip
    left.fadeOut(PAGE_FLIP_TIME);

    // this controls page rotation/slide
    lerp = Readers.createLerp(left.cellAt(0, 0), 2, 1, PAGE_FLIP_TIME);
    lerp.addListener(this);

    right.title.fadeOut(PAGE_FLIP_TIME);

    next.reset(true);
    // next.title.visible(true);
    next.title.alpha(255);
  }

  /**
   * Returns true if the word is on the verso page as a new page is flipping over it
   */
  public boolean isFadingOnFlip(final RiText word)
  {
    return flipping && (left == RiTextGrid.getGridFor(word));
  }

  private RiTextGrid buildPage(StringBuilder text)
  {
    // System.out.println("\n\nPageManager.buildPage()\n"+text);

    PageLayout nextLayout = pageLayout.copy();
    pageLayout.textColor = textColor;

    if (font == null)
      font = RiText.defaultFont(this._pApplet);

    if (leading == -1)
      leading = font.getSize() * RiText.defaults.leadingFactor;

    int idx = text.indexOf(PAGE_BREAK);

    if (idx > -1) // deal with page break
    {
      String part1 = Readers.trimEnds(text.substring(0, idx));
      String part3 = Readers.trimEnds(text.substring(idx));

      // System.out.println("FORCED PAGE BREAK!");// chunk:\n"+todo+"\n\n"+remaining.length()+") '" +remaining+ "'");

      if (part1.length() > 0)
      {
        pageLayout.layout(font, part1, this.leading);
        resetBuffer(text, Readers.trimEnds(pageLayout.remainingText()) + ' ' + part3);
      }
      else
        idx = -1; // do cond. below
    }

    if (idx == -1 && text.length() > 0)
    {

      RiText[] lines = pageLayout.layout(font, text.toString(), this.leading);

      String remainingText = pageLayout.remainingText();

      /*
       * for (int i = 0; i < lines.length; i++)
       * System.out.println(lines[i].text());
       * System.out.println("remainingText:"+remainingText);
       */
      resetBuffer(text, remainingText);
    }

    PageLayout tmp = pageLayout;
    pageLayout = nextLayout;

    /*
     * yuk -- need uniform way of dealing w' headers/footers,
     * they are currently in both PageLayout and Grid
     */
    if (showPageNumbers)
      tmp.footer(++pageCounter + "");
    else
      tmp.footer("");

    RiTextGrid grid = new RiTextGrid(_pApplet, tmp);
    grid.footer.fill(grid.template.fill());

    return grid;
  }

  // resets the contents of the StringBuilder
  private void resetBuffer(StringBuilder sb, String string)
  {
    sb.delete(0, sb.length());
    sb.append(string);
  }

  private boolean hasNext(StringBuilder remaining)
  {
    return remaining != null && remaining.length() > 0;
  }

  private void showAll()
  {
    if (left != null)
      left.visible(true);
    if (right != null)
    {
      right.visible(true);
      if (next != null)
        next.visible(true);
    }
    showTitles();
  }

  public RiTextGrid getLast()
  {
    if (left == null)
      return null;
    RiTextGrid g = left.next;
    if (g == null)
      return left;
    while (g.next != left)
      g = g.next;
    return g;
  }

  /**
   * Returns a list of all the RiTexts in the set of pages managed by this PageManager.
   */
  public RiText[] doLayout()
  {
    Readers.verify(pages != null || pages.size() > 0);

    if (pages.size() < 3)
      disablePageFlips = true;

    left = (RiTextGrid) pages.get(0);
    if (pages.size() > 1)
      right = (RiTextGrid) pages.get(1);
    else
      System.err.println("[WARN] PageManager: only 1 page created!");

    if (pages.size() > 2)
      next = (RiTextGrid) pages.get(2);

    RiTextGrid last = (RiTextGrid) pages.get(pages.size() - 1);
    for (Iterator it = pages.iterator(); it.hasNext();)
    {
      RiTextGrid rtg = (RiTextGrid) it.next();
      rtg.setName(appId);

      if (last != null)
      {
        rtg.prev = last;
        last.next = rtg;
      }
      rtg.setName(appId);
      last = rtg;
    }
    last.next = left;

    Readers.info("Created " + pages.size() + " grids/pages");

    List allRiTexts = new ArrayList();
    for (Iterator it = pages.iterator(); it.hasNext();)
    {
      RiTextGrid page = (RiTextGrid) it.next();
      page.getRiTexts(allRiTexts);
    }

    showAll();

    this.allWords = (RiText[]) allRiTexts.toArray(RiText.EMPTY_ARRAY);

    return this.allWords;
  }

  public RiText[] getAllWords()
  {
    if (allWords == null)
      throw new RuntimeException("Must call doLayout() before getAllWords()...");
    return allWords;
  }

  private void showTitles()
  {
    if (versoHeader != null)
    {
      setHeader(left, versoHeader);
    }
    if (rectoHeader != null)
    {
      if (right != null)
      {
        setHeader(right, rectoHeader);
      }
      if (next != null)
      {
        setHeader(next, rectoHeader);
        // next.title.visible(false);
        next.title.alpha(0);
      }
    }
  }

  public void setHeaders(String rectoHead, String versoHead)
  {
    setRectoHeader(rectoHead);
    setVersoHeader(versoHead);
  }

  public void setHeaderFont(PFont font)
  {
    this.headerFont = font;
  }

  public void setHeaderY(int y)
  {
    this.headerY = y;
  }

  private void setHeader(RiTextGrid page, String header)
  {
    page.setTitle(header, -1, headerY, headerFont);
    // page.title.visible(true);
    page.title.alpha(255);
    page.title.fill(page.template().fill());
    page.title.x = _pApplet.width / 4f;
  }

  private void addPages(String txt)
  {
    if (pages == null)
      pages = new ArrayList();

    StringBuilder text = new StringBuilder(txt);
    while (hasNext(text))
    {// create the grid layouts
      RiTextGrid rtg = buildPage(text);
      if (rtg != null)
        pages.add(rtg);
    }
  }

  public void behaviorCompleted(RiTextBehavior behavior)
  {
    if (flipping && behavior == lerp)
      onNextPageComplete();
  }

  public boolean isFlipping()
  {
    return flipping;
  }

  /*
   * private void renderPageToBuffer(PApplet p, GLGraphicsOffScreen page, RiTextGrid rtg, boolean transparent)
   * {
   * // draw to the buffer (3D)
   * if (page != null) {
   * page.beginDraw();
   * page.background(255);
   * rtg.draw(page);
   * page.endDraw();
   * 
   * int sx=200, sy=200; //spotlight center
   * 
   * GLTexture tex = page.getTexture();
   * tex.loadPixels(); // Spotlight Calcs ***
   * for (int i = 0; i < tex.pixels.length; i++) {
   * tex.pixels[i] = 0xffff0000;
   * }
   * tex.loadTexture();
   * 
   * p.beginShape();
   * p.texture(tex);
   * p.vertex(0, 0, 0, 0);
   * p.vertex(page.width, 0, page.width, 0);
   * p.vertex(page.width, page.height, page.width, page.height);
   * p.vertex(0, page.height, 0, page.height);
   * p.endShape(PApplet.CLOSE);
   * }
   * }
   */

  private void onNextPageComplete()
  {
    flipping = false;
    left.visible(false);

    left = left.next;
    right = right.next;
    next = next.next;

    rotateY = 2;
    showAll();
  }

  public void onGridChange(MachineReader changed, RiTextGrid changedFrom, RiTextGrid changedTo)
  {
    // System.out.println("PageManager.onGridChange()");
    if (changedFrom == right && changedTo == next || changedFrom == next)
    {
      // dont' flip if we are a page-turner and already flipping
      /* if (!(isFlipping() && changed == pageTurner)) */
      nextPage();
    }
    else
    {

      // Should this ever happen or is this a problem?
      // Yes, on change from left->right! ???
      if (!(changedFrom == left && changedTo == right))
        Readers.warn("Ignoring flip(reader=" + changed + ")\n " + "changedFrom=" + changedFrom + " to="
            + changedTo + " (this shouldn't happen on a text with more than 2 pages!)");
    }
  }

  public RiTextGrid getRecto()
  {
    return right;
  }

  public RiTextGrid getVerso()
  {
    return left;
  }

  public RiTextGrid getNext()
  {
    return next;
  }

  public void showPageNumbers(boolean pageNumbers)
  {
    this.showPageNumbers = pageNumbers;
  }

  public void addTextsFromFile(String... fileNames)
  {
    for (int i = 0; i < fileNames.length; i++)
      addTextFromFile(fileNames[i]);
  }

  public void addTextFromFile(String fileName)
  {
    Readers.info("Loading: " + fileName);
    String txt = RiTa.loadString(/* _pApplet, */ fileName);
    addPages(txt.replaceAll("[\\r\\n]", " ")); // +PAGE_BREAK);
  }

  public void setRectoHeader(String rectoHeader)
  {
    this.rectoHeader = rectoHeader;
  }

  public String getRectoHeader()
  {
    return rectoHeader;
  }

  public void setVersoHeader(String versoHeader)
  {
    this.versoHeader = versoHeader;
  }

  public String getVersoHeader()
  {
    return versoHeader;
  }

  public PApplet getPApplet()
  {
    return _pApplet;
  }

  public float screenX(RiTextGrid grid, RiText cell)
  {
    float x = cell.x; // yuk
    if (grid == right || grid == next)
      x += cell.getPApplet().width / 2f;
    return x;
  }

  public void setApplicationId(String id)
  {
    this.appId = id;
  }

  public String getApplicationId()
  {
    return this.appId;
  }

  public void setScale(float scale)
  {
    this.scale = scale;
  }

  public float getScale()
  {
    return this.scale;
  }

  public void setParagraphIndents(int numPixels)
  {
    pageLayout.paragraphIndent = numPixels;
  }

  public void setParagraphSpacing(float additionalLeading)
  {
    pageLayout.paragraphLeading = (additionalLeading);
  }

  public void setIndentFirstParagraph(boolean indentFirst)
  {
    pageLayout.indentFirstParagraph = (indentFirst);
  }

  public void addStyledTexts(StyledText... sts)
  {
    for (int i = 0; i < sts.length; i++)
    {
      StyledText st = sts[i];
      applyStyle(st);
      addTextFromFile(st.textFile);
    }
  }

  private void applyStyle(StyledText st)
  {
    if (st.leading > -1)
      setLeading(st.leading);
    if (st.paragraphLeading > -1)
      setParagraphSpacing(st.paragraphLeading);
    if (st.indents > -1)
      setParagraphSpacing(st.indents);
    setIndentFirstParagraph(st.indentFirstParagraph);
    // setColor(st.textColor);
    setFont(st.font);
  }

  public void setLeading(float leading)
  {
    this.leading = leading;
  }

  public void setColor(float[] color)
  {
    if (color != null)
      this.textColor = color;
  }

  public void setFont(PFont pf)
  {
    if (pf != null)
      this.font = pf;
  }

  public MachineReader getFocusedReader()
  {
    return focusedReader;
  }

  public void onUpdateFocusedReader(MachineReader reader)
  {
    Readers.info("PageManager.focusSwitch: " + reader.getName());

    boolean newFocusedReader = (reader != this.focusedReader);

    if (newFocusedReader)
    {
      // JC commented out: DEBUGging? speeded up readers - eventually all - after focus change
      // reader.adjustSpeed(.5f);
      //
      // if (focusedReader != null) // previous-focus
      // focusedReader.resetSpeed();

      this.focusedReader = reader;
    }

    // if page-turner is not visible, move it to recto

    RiTextGrid rtg = focusedReader.getGrid();
    if (rtg == next)
      nextPage();

    if (rtg != left && rtg != right)
    {
      focusedReader.jumpToPage(right);
    }

    // move all non-visible readers to the verso page

    for (Iterator it = MachineReader.instances.iterator(); it.hasNext();)
    {
      MachineReader mr = (MachineReader) it.next();

      if (mr == focusedReader)
        continue;

      rtg = mr.getGrid();

      if (rtg != left && rtg != right)
        mr.jumpToPage(left);
    }
  }

  // ===========================================================================

  /**
   * returns true if there is a valid focus-thread to make the update
   */
  public boolean sendFocusUpdate(MachineReader reader)
  {
    if (MachineReader.SERVER_HOST != null && reader != null)
    {

      if (focusThread != null)
      {

        focusThread.setServerFocus(reader);

        return true;
      }
    }

    return false;
  }

  // not used at moment
  @SuppressWarnings("unused")
  private void addSpotlight(PImage p, MachineReader... readers)
  {
    if (readers == null || readers.length < 1)
      return;

    if (spotlightRadius > 0 && readers != null)
    {
      long millis = System.currentTimeMillis();

      float[] newX = new float[readers.length];
      float[] newY = new float[readers.length];
      Arrays.fill(newX, Float.MAX_VALUE);
      Arrays.fill(newX, Float.MAX_VALUE);

      if (spotLightTargets == null || spotLightTargets.length != readers.length)
      {
        spotLightTargets = new Point2D.Float[readers.length];
        lastSpotTargets = new Point2D.Float[readers.length];
        spotlightTimers = new long[readers.length];
      }

      for (int i = 0; i < readers.length; i++)
      {
        RiTextGrid grid = readers[i].getGrid();

        if (grid != right && grid != left && !(grid == next && flipping))
          continue;

        Point2D.Float nextTarget = readers[i].position();

        if (spotLightTargets[i] == null)
        {
          lastSpotTargets[i] = spotLightTargets[i] = nextTarget;
          continue;
        }

        if (!nextTarget.equals(spotLightTargets[i]))
        {
          lastSpotTargets[i] = spotLightTargets[i];
          spotLightTargets[i] = nextTarget;
          spotlightTimers[i] = millis;
        }

        float lerpTime = (millis - spotlightTimers[i]) / (readers[i].getSpeed() * 1000); // 0 -1
        newX[i] = PApplet.lerp(lastSpotTargets[i].x, spotLightTargets[i].x, lerpTime);
        newY[i] = PApplet.lerp(lastSpotTargets[i].y, spotLightTargets[i].y, lerpTime);

        if (grid == right || grid == next)
          newX[i] += (p.width / 2f - gutterSubtraction);
      }
      renderSpotlight(p, spotlightRadius, newX, newY, 15, 15);
    }
  }

  // not used at moment
  private void renderSpotlight(PImage p, float radius, float[] sx, float[] sy, int xOff, int yOff)
  {
    p.loadPixels();

    // if (p instanceof GLTexture)((GLTexture)p).updateTexture();

    for (int x = 0; x < p.width; x++)
    {
      float xpos = x - xOff;

      for (int y = 0; y < p.height; y++)
      {
        float ypos = y - yOff;

        // Calculate an amount to change brightness based on distance to (sx,sy)
        // The closer the pixel is to (sx,sy), the lower the value of dist
        float minDist = Float.MAX_VALUE;
        for (int i = 0; i < sx.length; i++)
        {
          float dist = (float) Math.sqrt((((sx[i] - xpos)) * ((sx[i] - xpos)))
              + (sy[i] - ypos) * 2 * (sy[i] - ypos));// - centerSpotlightSize;
          if (dist < minDist)
            minDist = dist;
        }

        // 'adjustBrightness' ranges from 0-1 with closest/brightest approaching 1

        // float adjustBrightness = 1 / (1f + minDist);
        float adjustBrightness = Math.min(maxSpotlightBrightness, Math.max(minSpotlightBrightness, (radius
            - minDist) / radius));

        // adjust the r,g,b vals for the pixel
        int loc = x + y * p.width;
        int what = p.pixels[loc];

        int r = (int) ((what >> 16 & 0xff) * adjustBrightness);
        int g = (int) ((what >> 8 & 0xff) * adjustBrightness);
        int b = (int) ((what & 0xff) * adjustBrightness);

        if (r > 255)
          r = 255;
        else if (r < 0)
          r = 0;
        if (g > 255)
          g = 255;
        else if (g < 0)
          g = 0;
        if (b > 255)
          b = 255;
        else if (b < 0)
          b = 0;

        p.pixels[loc] = 0xff000000 | ((int) r << 16) | ((int) g << 8) | (int) b;
      }
    }
    p.updatePixels();
  }

  public void setPages(List pages)
  {
    this.pages = pages;
  }

  public RiText[] getHeaders()
  {
    List l = new ArrayList();
    for (Iterator it = pages.iterator(); it.hasNext();)
    {
      RiTextGrid page = (RiTextGrid) it.next();
      if (page.title != null)
        l.add(page.title);
    }
    return (RiText[]) l.toArray(new RiText[0]);
  }

  /**
   * True if we sending/checking for focus on the server
   */
  public void isUsingServerFocus(boolean sendServerFocus)
  {
    if (sendServerFocus && MachineReader.SERVER_HOST == null)
      Readers.warn("Server=false, FocusThread=true...");
    this.doServerFocus = sendServerFocus;
  }

  /**
   * True if we sending/checking for focus on the server
   */
  public boolean setServerFocus()
  {
    return doServerFocus;
  }

}// end
