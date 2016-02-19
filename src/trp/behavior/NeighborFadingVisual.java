package trp.behavior;

import static trp.util.Direction.E;
import static trp.util.Direction.N;
import static trp.util.Direction.NE;
import static trp.util.Direction.NW;
import static trp.util.Direction.S;
import static trp.util.Direction.SE;
import static trp.util.Direction.SW;
import static trp.util.Direction.W;

import java.util.ArrayList;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.reader.MachineReader;

public class NeighborFadingVisual extends DefaultVisuals
{

  // private members
  private float[] leadingFadeToColor, trailingFadeToColor, gridColor;
  private float readerSpeed; // for convenience
  private ArrayList leadingFadingNeighbors = new ArrayList();
  private ArrayList trailingFadingNeighbors = new ArrayList();
  private ArrayList recentlyReadCells = new ArrayList();
  private boolean fadeLeadingNeighbors = true, fadeTrailingNeighbors = true;

  public NeighborFadingVisual(float[] theColor, float[] gCol, float rSpeed)
  {
    if (theColor != null)
      readerColor = theColor;
    readerSpeed = rSpeed;
    fadeInTime = readerSpeed * .8f; // shld be less that step speed
    fadeOutTime = readerSpeed * 10f;
    delayBeforeFadeBack = readerSpeed * 2.5f;
    gridColor = gCol;

    leadingFadeToColor = gridColor.clone();
    leadingFadeToColor[3] = leadingFadeToColor[3] + (255 - leadingFadeToColor[3]) / 4;

    trailingFadeToColor = gridColor.clone(); // gridColor;
    trailingFadeToColor[3] = leadingFadeToColor[3] + (255 - leadingFadeToColor[3]) / 6;

  }

  public void enterWord(MachineReader mr, RiText rt)
  {
    rt.showBounds(mr.testMode);

    RiTextGrid.resetTextFor(rt);
    
    // is it the first cell?
    // if (mr.getHistory().size() < 1)
    // return;

    // RiText lastRead = mr.getLastReadCell();
    // verify(lastRead != null, "lastRead is null for: " + rt);

    // get the last few cells read
    // if you get two these will appear in the
    // reader color preceded by a faded out cell
    recentlyReadCells = mr.getRecentlyReadCells(2);
    RiTextGrid rtg = mr.getGrid();
    RiText[] neighbors = rtg.neighborhood(rt);
    ArrayList neighborList = getNeighborList(neighbors);

    // handle word being read
    fadeCell(rt, readerColor, fadeInTime);

    if (fadeLeadingNeighbors)
    {
      leadingFadingNeighbors = getLeadingNeighborsToFade(neighbors, recentlyReadCells);
      fadeCells(leadingFadingNeighbors, leadingFadeToColor, fadeInTime);
    }
    if (fadeTrailingNeighbors)
    {
      trailingFadingNeighbors = getTrailingNeighborsToFade(neighbors, recentlyReadCells);
      fadeCells(trailingFadingNeighbors, trailingFadeToColor, fadeInTime);
    }

  }

  public void exitWord(MachineReader mr, RiText rt)
  {
    fadeCell(rt, gridColor, (delayBeforeFadeBack + fadeInTime), fadeOutTime);
    fadeCells(leadingFadingNeighbors, gridColor, delayBeforeFadeBack, fadeOutTime);
    fadeCells(trailingFadingNeighbors, gridColor, delayBeforeFadeBack, fadeOutTime);
    rt.showBounds(false);
  }

  protected void fadeCells(ArrayList cellList, float[] fadeColor, float fadeTime)
  {
    if (cellList == null)
      return;
    for (int i = 0; i < cellList.size(); i++)
    {
      fadeCell((RiText) cellList.get(i), fadeColor, fadeTime);
    }

  }

  protected void fadeCells(ArrayList cellList, float[] fadeColor, float delay, float fadeTime)
  {
    if (cellList == null)
      return;
    for (int i = 0; i < cellList.size(); i++)
    {
      fadeCell((RiText) cellList.get(i), fadeColor, delay, fadeTime);
    }

  }

  protected ArrayList getLeadingNeighborsToFade(RiText[] neighbors, ArrayList lastFewRead)
  {
    ArrayList resultList = new ArrayList();
    for (int i = 0; i < neighbors.length; i++)
    {
      if (neighbors[i] == null)
        continue;
      if (i == NE.toInt() || i == E.toInt() || i == SE.toInt())
      {
        if (!lastFewRead.contains(neighbors[i]))
          resultList.add(neighbors[i]);
      }
    }
    // System.out.println("resultList.size(): " + resultList.size());
    return resultList;
  }

  protected ArrayList getTrailingNeighborsToFade(RiText[] neighbors, ArrayList lastFewRead)
  {
    ArrayList resultList = new ArrayList();
    for (int i = 0; i < neighbors.length; i++)
    {
      if (neighbors[i] == null)
        continue;
      if (i == N.toInt() || i == NW.toInt() || i == W.toInt() || i == SW.toInt()
          || i == S.toInt())
      {
        if (!lastFewRead.contains(neighbors[i]))
          resultList.add(neighbors[i]);
      }
    }
    // System.out.println("resultList.size(): " + resultList.size());
    return resultList;
  }

  protected ArrayList getNeighborList(RiText[] neighbors)
  {
    ArrayList resultList = new ArrayList();
    for (int i = 0; i < neighbors.length; i++)
    {
      if (neighbors[i] == null)
        continue;
      resultList.add(neighbors[i]);
    }
    return resultList;
  }

  public boolean isFadeLeadingNeighbors()
  {
    return fadeLeadingNeighbors;
  }

  public void setFadeLeadingNeighbors(boolean fadeLeadingNeighbors)
  {
    this.fadeLeadingNeighbors = fadeLeadingNeighbors;
  }

  public boolean isFadeTrailingNeighbors()
  {
    return fadeTrailingNeighbors;
  }

  public void setFadeTrailingNeighbors(boolean fadeTrailingNeighbors)
  {
    this.fadeTrailingNeighbors = fadeTrailingNeighbors;
  }

}
