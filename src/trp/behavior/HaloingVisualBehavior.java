package trp.behavior;

import java.util.HashSet;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.reader.MachineReader;
import trp.util.Direction;

public class HaloingVisualBehavior extends DefaultVisuals
{

  static final boolean FADE_LEADING_NEIGHBORS = true;
  static final boolean FADE_TRAILING_NEIGHBORS = true;
  static final boolean RESET_INNER_HALO_WORDS = false;
  static final boolean RESET_OUTER_HALO_WORDS = false;
  
  long timeBetween;

  RiText cellBeingRead, cellLastRead, cellBeforeLast;

  public HaloingVisualBehavior()
  {
    this(null);
  }

  public HaloingVisualBehavior(float[] theColor, float stepTime)
  {
    this.readerColor = (theColor != null) ? theColor : BRIGHT_RED;
    this.fadeInTime = stepTime * .6f; // should be less than step time
    this.fadeOutTime = stepTime * 4f;
  }

  public HaloingVisualBehavior(float[] theColor)
  {
    this.readerColor = (theColor != null) ? theColor : BRIGHT_RED;
    this.fadeInTime = 2f; // should be less than step time
    this.fadeOutTime = 10f;
  }

  public void enterWord(MachineReader mr, RiText rt)
  {
    //
    // System.out.println("Haloing.enterWord("+mr+","+rt+")");
    // timeBetween = System.currentTimeMillis();
    //
    RiTextGrid grid = mr.getGrid();
    RiText[] neighbors = grid.neighborhood(rt);

    cellBeingRead = rt;
    this.fadeInTime = mr.getSpeed() * .75f;
    cellLastRead = mr.getLastReadCell(); // only used in this method??
    // System.out.println();
    // System.out.println(cellLastRead.getText() + "--");
    // cellBeforeLast = mr.getCellBeforeLast(); // ??? not-used?

    RiTextGrid.resetTextFor(rt);

    doHalo(grid, neighbors, getNeighborStates(grid, neighbors, cellLastRead), getReaderColor());

    rt.showBounds(mr.testMode);
  }

  protected void doHalo(RiTextGrid grid, RiText[] neighbors, int[] neighborStates, float[] readerColor)
  {
    float[] fadeColor = grid.template().fill();
    float[] outerColor = grid.template().fill();

    fadeColor[3] = fadeColor[3] / 2; // fade immediate neighbors to half of
    // default alpha
    outerColor[3] = 0; // fade outer neighbors to alpha=0

    RiText[] outerNeighbors;
    int[] outerNeighborStates;
    for (int i = 0; neighbors != null && i < neighbors.length; i++)
    {
      if (neighbors[i] == null)
        continue;

      if (RESET_INNER_HALO_WORDS) // DCH: use ResetWordBehavior instead!
      {
        // resetWord(neighbors[i]);
        RiTextGrid.resetTextFor(neighbors[i]);
      }

      if (i == Direction.C.toInt())
      {
        // System.out.println("CURRENT: "+neighbors[i]);
        fadeCell(neighbors[i], readerColor, fadeInTime);
        // neighbors[i].fadeColor(readerColor, fadeInTime);
        continue;
      }
      else
      {
        // System.out.println();
        // System.out.print("Fading i: " + i);
        fadeNeighbor(i, fadeColor, neighbors, neighborStates, fadeInTime);
        // .out.println();
      }
      // must test to see these haloing neighbors are on the current grid
      if (grid.contains(neighbors[i]))
      {
        outerNeighbors = grid.neighborhood(neighbors[i]);
        outerNeighborStates = getNeighborStates(grid, outerNeighbors, cellBeingRead); // should
        // be
        // cellBeingRead
        // (more
        // UNTOUCHED)
        // /Color[3] = 0;
        for (int j = 0; outerNeighbors != null && j < outerNeighbors.length; j++)
        {
          if (outerNeighbors[j] == null || j == Direction.C.toInt())
            continue;

          if (RESET_OUTER_HALO_WORDS)
          {
            // resetWord(outerNeighbors[j]);
            RiTextGrid.resetTextFor(outerNeighbors[j]);
          }

          // System.out.print("Fading j: " + j + " ");
          fadeNeighbor(j, outerColor, outerNeighbors, outerNeighborStates, fadeInTime / 2);
        }
      }
    }
  }

  protected int[] getNeighborStates(RiTextGrid grid, RiText[] neighbors, RiText lastCellRead)
  {
    int[] neighborStates = new int[9];
    HashSet rtSet = null;
    // check that lastCellRead is not null and it's on current grid!
    if (lastCellRead != null && grid.contains(lastCellRead))
    {
      // RiText[] lastNeighbors = null;
      RiText[] lastNeighbors = grid.neighborhood(lastCellRead);
      rtSet = new HashSet();
      for (int i = 0; lastNeighbors != null && i < lastNeighbors.length; i++)
      {
        if (lastNeighbors[i] == null)
          continue;
        rtSet.add(lastNeighbors[i]);
      }
    }
    for (int i = 0; i < neighborStates.length; i++)
    {
      neighborStates[i] = UNTOUCHED;
      if (neighbors[i] == null || rtSet == null)
        continue;

      if (rtSet.contains(neighbors[i]))
        neighborStates[i] = WAS_NEIGHBOR;
      if (neighbors[i] == lastCellRead)
        neighborStates[i] = WAS_READ;
    }
    return neighborStates;
  }

  private void fadeNeighbor(int i, float[] fadeColor, RiText[] neighbors, int[] neighborStates, float fadeInSeconds)
  {
    // leading neighbors
    if (i == Direction.NE.toInt() || i == Direction.E.toInt()
        || i == Direction.SE.toInt())
    {
      // System.out.println("LEADING: "+neighbors[i]);
      if (FADE_LEADING_NEIGHBORS && (neighborStates[i] == UNTOUCHED))
      {
        // neighbors[i].fadeColor(fadeColor, fadeInSeconds);
        // System.out.print(" leading, ");
        fadeCell(neighbors[i], fadeColor, fadeInSeconds);
      }
    }
    // trailing neighbors
    else if (FADE_TRAILING_NEIGHBORS && (neighborStates[i] == UNTOUCHED))
    {
      // System.out.println("TRAILING: "+neighbors[i]);
      // neighbors[i].fadeColor(fadeColor, fadeInSeconds);
      // System.out.print(" trailing, ");
      fadeCell(neighbors[i], fadeColor, fadeInSeconds);
    }
  }

  public void exitWord(MachineReader mr, RiText rt)
  {
    //
    // System.out.println("Haloing.exitWord("+mr+","+rt+") " + (System.currentTimeMillis() - timeBetween)/1000);
    //
   RiTextGrid grid = mr.getGrid();
    RiText[] neighbors = grid.neighborhood(rt);
    for (int i = 0; neighbors != null && i < neighbors.length; i++)
    {
      if (neighbors[i] == null)
        continue;

      // neighbors[i].fadeColor(grid.template().fill(), 10,
      // fadeOutTime);
      fadeCell(neighbors[i], grid.template().fill(), delayBeforeFadeBack, fadeOutTime);

      // must test to see these haloing neighbors are on the current grid
      if (grid.contains(neighbors[i]))
      {
        RiText[] outerNeighbors = grid.neighborhood(neighbors[i]);
        for (int j = 0; outerNeighbors != null && j < outerNeighbors.length; j++)
        {
          if (outerNeighbors[j] == null)
            continue;
          fadeCell(outerNeighbors[j], grid.template().fill(), delayBeforeFadeBack, fadeOutTime);
          // / outerNeighbors[j].fadeColor(grid.template().fill(),
          // 10,
          // fadeOutTime);
        }
      }
    }
    rt.showBounds(false);
  }

}
