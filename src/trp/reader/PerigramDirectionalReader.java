package trp.reader;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.util.Direction;
import trp.util.PerigramLookup;

public class PerigramDirectionalReader extends PerigramReader
{
  private Direction wayToGo;

  public PerigramDirectionalReader(RiTextGrid grid, PerigramLookup perigrams, Direction dir)
  {
    super(grid, perigrams);
    wayToGo = dir;
  }

  protected RiText determineReadingPath(RiText[] neighbors)
  {

    // can only go in dir if it is viable
    RiText lastReadCell = getLastReadCell(), nextCell = neighbors[wayToGo.toInt()];

    if (nextCell == null)
    {
      // System.out.println("Died with null cell ahead to the " + wayToGo + " from: " + neighbors[4]);
      delete(this);
      return null;
    }

    if (lastReadCell != null) // first word with this spawned reader
    {
      if (!perigrams.isPerigram(-1, lastReadCell, currentCell, nextCell))
      {
        // System.out.println("Died looking for: " + lastReadCell.getText() + " " + currentCell.getText() + " " + nextCell.getText());
        delete(this);
        return null;
      }
    }

    return neighbors[wayToGo.toInt()];

  }
}
