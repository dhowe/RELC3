package trp.reader;

import static trp.util.Direction.C;
import static trp.util.Direction.E;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.util.Direction;
import trp.util.PerigramLookup;
import trp.util.Readers;

public class UnconPerigramReader extends PerigramReader
{
  static final boolean USE_PERIGRAMS = true; // remove this once refactoring is
  // complete

  static double[] pathWeighting = { .4, .5, .75, .4, 1, 1, 1, 1, 1 };

  int wordsOut = 0;
  Direction dir;

  public UnconPerigramReader(RiTextGrid grid, PerigramLookup perigrams)
  {
    super(grid, perigrams);
    this.perigrams = perigrams;
  }

  protected RiText determineReadingPath(RiText[] neighbors)
  {

    // default to next word
    int idx = 0;
    int bestScore = 0;
    // defaults:
    Direction nextDir = E;
    RiText nextCell = grid.nextCell(currentCell);
    for (; idx < 9; idx++)
    {
      // only try path if not null and not current or next
      if ((idx != C.toInt()) && (idx != E.toInt()) && (neighbors[idx] != null))
      {
        int newScore;
        if (USE_PERIGRAMS)
          newScore = tryPath(neighbors[idx], pathWeighting[idx], perigrams);
        else
          newScore = tryPath(neighbors[idx], pathWeighting[idx]);

        // make wayToGo the highest scoring neighbor
        if (newScore > bestScore)
        {
          bestScore = newScore;
          nextCell = neighbors[idx];
          nextDir = Direction.fromInt(idx);
        }
      }
    }

    // but always go to the next word 1/7 of the time:
    if (random.nextInt(7) == 0)
    {
      nextCell = grid.nextCell(currentCell);
      nextDir = E;
    }

    if (nextCell == null)
      Readers.error("nextCell= null!");

    // build the context based on where we are going
    buildConTextForServer(nextCell);

    if (printToConsole)
      printDirection(neighbors, nextCell, nextDir.toInt());

    setLastDirection(nextDir);

    return nextCell;
  }

  private int tryPath(RiText cellOnNewPath, double theWeighting, PerigramLookup perigrams)
  {
    // theWeighting (from pathWeighting[]) not yet used
    int theScore = 0;
    if (getLastReadCell() == null || cellOnNewPath == null)
      return theScore;
    if (perigrams.isPerigram(getLastReadCell(), currentCell, cellOnNewPath))
      theScore = (random.nextDouble() < theWeighting ? 1 : 0);
    // theScore will be 0 if not a bigram
    // just give a randomly weighted score to a qualifying direction
    return theScore * (random.nextInt(9) + 1);
  }

  private int tryPath(RiText cellOnNewPath, double theWeighting)
  {
    // theWeighting (from pathWeighting[]) not yet used
    int theScore = 0;
    if (isDigram(currentCell, cellOnNewPath))
      theScore = (random.nextDouble() < theWeighting ? 1 : 0);
    // theScore will be 0 if not a bigram
    // just give a randomly weighted score to a qualifying direction
    return theScore * (random.nextInt(9) + 1);
  }

  protected void buildConTextForServer(RiText wayToGo)
  {
    String spacing = "      ";
    String text = wayToGo == null ? "" : wayToGo.text();
    conText = spacing + text;
  }

  private void printDirection(RiText[] neighbors, RiText result, int idx)
  {
    /*
     * int dirInt = 0; for (int i = 0; i < 9; i++) { if (result == neighbors[i])
     * { dirInt = i; break; }}
     */
    if (printToConsole)
    {
      System.out.print(result.text() + " (" + Direction.fromInt(idx) + ") ");
      if ((++wordsOut) % 5 == 0)
      {
        System.out.println();
        wordsOut = 0;
      }
    }
  }

  public void jumpToPage(RiTextGrid right)
  {
    this.grid = right;
    RiText cellToTry = right.getRandomCell();
    while (!perigrams.isPerigram(getLastReadCell(), currentCell, cellToTry))
    {
      cellToTry = right.nextCell(cellToTry);
      RiTextGrid g = RiTextGrid.getGridFor(cellToTry);
      if (g != right)
        cellToTry = right.cellAt(0, 0);
    }
    if (printToConsole)
      System.out.println("Jumping with: " + getLastReadCell().text() + " "
          + currentCell.text() + " " + cellToTry.text());
    currentCell = cellToTry;
  }


} // end
