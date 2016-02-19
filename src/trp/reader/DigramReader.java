package trp.reader;

import static trp.util.Direction.C;
import static trp.util.Direction.E;
import static trp.util.Direction.NE;
import static trp.util.Direction.SE;

import java.util.Map;
import java.util.Random;

import rita.RiTaException;
import rita.RiText;
import trp.behavior.NeighborFadingVisual;
import trp.layout.RiTextGrid;
import trp.util.Direction;
import trp.util.PerigramLookup;
import trp.util.Readers;

public class DigramReader extends SimpleReader// implements Digrammable
{
  protected String conText, digramsText, consoleString;
  protected double upWeighting = .12, downWeighting = .6;
  protected Map<String, Integer> digramsHashed;
  protected Random random;
  protected boolean NEViable = false, SEViable = false;

  // constructors -----------------------------

  public DigramReader(RiTextGrid grid, String rawDigramsFile)
  {
    this(grid, new String[] { rawDigramsFile });
  }

  public DigramReader(RiTextGrid grid, String[] digramsFiles)
  {
    this(grid, Readers.hashDigrams(grid._pApplet, digramsFiles));
  }

//  public DigramReader(RiTextGrid grid, Digrammable digrammable)
//  {
//    this(grid, digrammable.getDigrams());
//  }

  public DigramReader(RiTextGrid grid, PerigramLookup perigrams)
  {
    this(grid, perigrams.getHashedPerigrams());
  }

  public DigramReader(RiTextGrid grid, Map<String, Integer> digrams)
  {
    super(grid);
    this.digramsHashed = digrams;
    this.random = new Random(System.currentTimeMillis());
    setBehavior(new NeighborFadingVisual(BLUE, grid.template().fill(), getSpeed()));
  }

  // this gets sent to server
  public String getTextForServer(RiText selected)
  {
    return conText;
  }

  // methods -----------------------------

  public RiText selectNext()
  {

    consoleString = "";

    // System.out.println("DigramReader.selectNext() on grid: "+grid);

    RiText[] neighbors = grid.neighborhood(currentCell);

    if (neighbors == null)
      return null;

    RiText rt = determineReadingPath(neighbors);

    return rt;
  }

  protected RiText determineReadingPath(RiText[] neighbors)
  {

    NEViable = false;
    SEViable = false;
    String NEConText = null, SEConText = null;
    Direction wayToGo = E;

    // only go NE if it is viable
    if (isViableDirection(currentCell, neighbors, NE))
    {
      wayToGo = NE;
      NEViable = true;
    }

    if (wayToGo == NE)
    {
      // collect the context in any case
      NEConText = neighbors[C.toInt()].text() + " " + neighbors[NE.toInt()].text();
      // but only actually go NE rarely
      wayToGo = (random.nextDouble() < upWeighting) ? NE : E;
    }

    // only go SE if it is viable
    if (isViableDirection(currentCell, neighbors, SE))
      SEViable = true;

    if (SEViable && (wayToGo == E))
      wayToGo = SE;

    if (wayToGo == SE)
    {
      // collect the context in any case
      SEConText = neighbors[C.toInt()].text() + " " + neighbors[SE.toInt()].text();
      // but only actually go SE occasionally
      wayToGo = (random.nextDouble() < downWeighting) ? SE : E;
    }

    // build the context based on where we are going
    // if (OLD_CONTEXT)
    // buildConTextForServer(wayToGo, NEConText, SEConText, neighbors);
    // else
    buildConTextForServer(wayToGo, neighbors);

    if (printToConsole)
    {
      if (neighbors[wayToGo.toInt()] != null)
        Readers.info(neighbors[wayToGo.toInt()].text() + " (" + wayToGo.toString()
            + ") - " + consoleString);
    }

    setLastDirection(wayToGo);

    switch (wayToGo)
    {
      case NE:
        return neighbors[NE.toInt()];
      case SE:
        return neighbors[SE.toInt()];
      default:
        if (neighbors[E.toInt()] != null)
          return neighbors[E.toInt()];
        else
          return neighbors[C.toInt()];
    }
  }

  protected void buildConTextForServer(Direction wayToGo, RiText[] neighbors)
  {
    String SEContext = "", NEContext = "", EContext = "";

    if (neighbors[E.toInt()] != null)
      EContext = neighbors[C.toInt()].text() + " " + neighbors[E.toInt()].text();

    if (NEViable)
      NEContext = neighbors[C.toInt()].text() + " " + neighbors[NE.toInt()].text();

    if (SEViable)
      SEContext = neighbors[C.toInt()].text() + " " + neighbors[SE.toInt()].text();

    conText = EContext;
    switch (wayToGo)
    {
      case NE:
        if (!SEContext.equals(""))
          conText = conText + "<br>" + SEContext;
        conText = /* currentCell */neighbors[NE.toInt()].text().toUpperCase() + "<br>"
            + conText;
        break;
      case SE:
        if (!NEContext.equals(""))
          conText = NEContext + "<br>" + conText;
        conText = conText + "<br>"
            + /* currentCell */neighbors[SE.toInt()].text().toUpperCase();
        break;
      default:
        if (neighbors[E.toInt()] != null)
          conText = /* currentCell */neighbors[E.toInt()].text().toUpperCase();
        if (!NEContext.equals(""))
          conText = NEContext + "<br>" + conText;
        if (!SEContext.equals(""))
          conText = conText + "<br>" + SEContext;
        break;
    }

    // add blank line
    conText = conText + "<br>";
  }

  private void buildConTextForServer(Direction wayToGo, String NEConText, String SEConText, RiText[] neighbors)
  {
    if (neighbors == null || neighbors[C.toInt()] == null || neighbors[E.toInt()] == null)
      return;

    // default context (always relevant) is current + next
    conText = neighbors[C.toInt()].text() + " " + neighbors[E.toInt()].text();
    // conText = lastReadCell.text() + " " + neighbors[NN].text()+" "
    // + neighbors[NX].text();

    // if we are actually going that way, make this upper case
    if (wayToGo == E)
      conText = conText.toUpperCase();

    // the following code will collect the contexts
    // keep them in order top-to-bottom NE, NX, SE
    // with the chosen path set to upperCase
    switch (wayToGo)
    {
      case NE:
        if (SEConText != null)
          conText = conText + "<br>" + SEConText;
        conText = NEConText.toUpperCase() + "<br>" + conText;
        break;
      case SE:
        if (NEConText != null)
          conText = NEConText + "<br>" + conText;
        conText = conText + "<br>" + SEConText.toUpperCase();
        break;
      default:
        if (NEConText != null)
          conText = NEConText + "<br>" + conText;
        if (SEConText != null)
          conText = conText + "<br>" + SEConText;
        break;
    }

    // add blank line
    conText = conText + "<br>";
  }

  protected boolean isViableDirection(RiText current, RiText[] neighbors, Direction direction)
  {
    boolean result = isDigram(current, neighbors[direction.toInt()]);
    if (result && printToConsole)
      consoleString = consoleString + " Found: "
          + makeDigram(current, neighbors[direction.toInt()]) + " (" + direction + ") ";
    return result;
  }

  private boolean isDigram(RiText current, RiText[] neighbors, Direction direction, boolean addToConsoleString)
  {
    boolean result = isDigram(current, neighbors[direction.toInt()]);
    if (result && printToConsole && addToConsoleString)
      consoleString = consoleString + " Found: "
          + makeDigram(current, neighbors[direction.toInt()]) + " (" + direction + ") ";
    return result;
  }

  public boolean isDigram(RiText current, RiText rt)
  {
    if (rt == null)
      return false;
    String digram = makeDigram(current, rt);
    return digramsHashed.containsKey(digram);
  }

  public String makeDigram(RiText current, RiText possibleNext)
  {
    if (current == null || possibleNext == null)
      return "";
    return lowerStrip(current) + " " + lowerStrip(possibleNext);
  }

  public String lowerStrip(RiText rt)
  {
    return stripPunctuation(rt.text()).toLowerCase();
  }

  public double getUpWeighting()
  {
    return upWeighting;
  }

  public void setUpWeighting(double upWeighting)
  {
    this.upWeighting = upWeighting;
  }

  public double getDownWeighting()
  {
    return downWeighting;
  }

  public void setDownWeighting(double downWeighting)
  {
    this.downWeighting = downWeighting;
  }

  public Map<String, Integer> getDigrams()
  {
    return digramsHashed;
  }

  // for Ngrammable interface ================================

  public boolean isNGram(RiText... phrase)
  {
    return isDigram(phrase[0], phrase[1]);
  }

  public int getCount(RiText... phrase)
  {
    throw new RiTaException("getCount() is not implemented for digrams");
  }

} // end
