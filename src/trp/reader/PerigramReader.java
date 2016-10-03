package trp.reader;

import static trp.util.Direction.C;
import static trp.util.Direction.E;
import static trp.util.Direction.NE;
import static trp.util.Direction.SE;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.util.Direction;
import trp.util.PerigramLookup;

public class PerigramReader extends DigramReader
{
  protected PerigramLookup perigrams;
  public int serverOutputType = 0;

  // make a ReadersConstant for this? 0=SIMPLE, 1=PERMUTATIONS, etc.

  public PerigramReader(RiTextGrid grid, PerigramLookup perigrams)
  {
    super(grid, perigrams);
    this.perigrams = perigrams;
  }

  protected boolean isViableDirection(RiText current, RiText[] neighbors, Direction direction)
  {
    if (getLastReadCell() == null || neighbors[direction.toInt()] == null)
      return false;
    boolean result = perigrams.isPerigram(0, getLastReadCell(), current, neighbors[direction.toInt()]);
    if (result && printToConsole)
      consoleString = consoleString + " pFound: "
          + makePerigram(getLastReadCell(), current, neighbors[direction.toInt()]) + " ("
          + direction + ") ";
    return result;
  }

  protected String makePerigram(RiText lastReadCell, RiText current, RiText riText)
  {
    return lowerStrip(lastReadCell) + " " + makeDigram(current, riText);
  }

  protected void buildConTextForServer(Direction wayToGo, RiText[] neighbors)
  {
    switch (serverOutputType)
    {
      case 1:
        String SEContext = "",
        NEContext = "",
        EContext = "";

        if (neighbors[E.toInt()] != null)
          EContext = neighbors[C.toInt()].text() + " "
              + neighbors[E.toInt()].text();

        if (NEViable)
          NEContext = neighbors[C.toInt()].text() + " "
              + neighbors[NE.toInt()].text();

        if (SEViable)
          SEContext = neighbors[C.toInt()].text() + " "
              + neighbors[SE.toInt()].text();

        conText = EContext;
        switch (wayToGo)
        {
          case NE:
            if (!SEContext.equals(""))
              conText = conText + "<br>" + SEContext;
            conText = /* currentCell */neighbors[NE.toInt()].text().toUpperCase()
                + "<br>" + conText;
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
        //if (0 == 1/* printToConsole */)
          //System.out.println("Context for server: " + conText);
        conText = conText + "<br>";
        break;
      default:
        conText = neighbors[wayToGo.toInt()].text().replace("—", "-")/* + " "*/; // TEMP!
        // MOVED TO CLIENT WHERE IT BELONGS: conText = conText.replace("o", "0")/* + " "*/;
        break;
    }
  }

  public synchronized void jumpToPage(RiTextGrid right)
  {
    this.grid = right;
    RiText cellToTry = right.getRandomCell();
    while (!perigrams.isPerigram(0, getLastReadCell(), currentCell, cellToTry))
    {
      cellToTry = right.nextCell(cellToTry);
      RiTextGrid g = RiTextGrid.getGridFor(cellToTry);
      if (g != right)
        cellToTry = right.cellAt(0, 0);
    }
    if (printToConsole == true)
      System.out.println("Jumping with: " + getLastReadCell().text() + " "
          + currentCell.text() + " " + cellToTry.text());
    currentCell = cellToTry;
  }

}
