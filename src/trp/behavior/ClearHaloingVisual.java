package trp.behavior;

import java.util.ArrayList;

import rita.RiText;
import rita.support.HistoryQueue;
import trp.layout.RiTextGrid;
import trp.reader.MachineReader;

public class ClearHaloingVisual extends DefaultVisuals
{
  // private members
  private float[] innerFadeToColor, outerFadeToColor, gridColor;
  private float readerSpeed;
  private ArrayList innerFadingNeighbors = new ArrayList();
  private ArrayList outerFadingNeighbors = new ArrayList();
  private ArrayList recentlyReadCells = new ArrayList();
  private boolean fadeLeadingNeighbors = true, fadeTrailingNeigbors = true;

  // we're going to get all the parameters from the
  // MachineReader that is passed in
  // except color which is handled by VisualBehaviors
  // DON'T USE THIS CONSTRUCTOR:
//  public ClearHaloingVisual(MachineReader mr, float[] mrColor)
//  {
//    readerColor = mrColor;
//    setReaderColor(readerColor); // so that DefaultVisuals knows the reader color
//    readerSpeed = mr.getSpeed();
//    fadeInTime = readerSpeed * .75f; // must be less than step time
//    fadeOutTime = readerSpeed * 2f;
//    delayBeforeFadeBack = fadeOutTime;
//    RiTextGrid rtg = mr.getGrid();
//    gridColor = rtg.template().getColor();/*mr.getGrid().DEFAULT_COLOR.clone();*/
//    innerFadeToColor = gridColor.clone();
//    innerFadeToColor[3] = (innerFadeToColor[3] / 2); // half alpha
//    outerFadeToColor = gridColor.clone(); // gridColor;
//    outerFadeToColor[3] = 0; // defaults to completely faded
//  }

  public ClearHaloingVisual(float[] rColor, float[] gColor, float rSpeed)
  {
    readerColor = rColor;
    readerSpeed = rSpeed;
    fadeInTime = readerSpeed * .75f; // must be less than step time
    fadeOutTime = readerSpeed * 2f;
    delayBeforeFadeBack = fadeOutTime;
    gridColor = gColor;
    innerFadeToColor = gridColor.clone();
    innerFadeToColor[3] = (innerFadeToColor[3] / 2); // half alpha
    outerFadeToColor = gridColor.clone(); // gridColor;
    outerFadeToColor[3] = 0; // defaults to completely faded
  }

  public void enterWord(MachineReader mr, RiText rt)
  {
    // handle word being read
    fadeCell(rt, readerColor, fadeInTime);

    // get the last few cells read
    // if you get two these will appear in the
    // reader color preceded by a faded out cell
    recentlyReadCells = mr.getRecentlyReadCells(2);
    RiTextGrid rtg = mr.getGrid();
    RiText[] neighbors = rtg.neighborhood(rt);
    ArrayList neighborList = getNeighborList(neighbors);

    if (fadeLeadingNeighbors)
    {
      innerFadingNeighbors = getInnerNeighborsToFade(neighborList, recentlyReadCells);
      fadeCells(innerFadingNeighbors, innerFadeToColor, fadeInTime);
    }
    if (fadeTrailingNeigbors)
    {
      outerFadingNeighbors = getOuterNeighborsToFade(rtg, neighbors, innerFadingNeighbors, recentlyReadCells);
      fadeCells(outerFadingNeighbors, outerFadeToColor, fadeInTime);
    }
    rt.showBounds(mr.testMode);
  }

  public void exitWord(MachineReader mr, RiText rt)
  {
    fadeCell(rt, gridColor, (delayBeforeFadeBack + fadeInTime), fadeOutTime);
    fadeCells(innerFadingNeighbors, gridColor, delayBeforeFadeBack, fadeOutTime);
    fadeCells(outerFadingNeighbors, gridColor, delayBeforeFadeBack, fadeOutTime);
    rt.showBounds(false);
  }

  protected ArrayList getOuterNeighborsToFade(RiTextGrid rtg, RiText[] neighbors, ArrayList innerFadingNeighbors, ArrayList lastThree)
  {
    ArrayList resultList = new ArrayList();
    RiTextGrid gridForNeighbors = null;
    for (int i = 0; i < neighbors.length; i++)
    {
      if (neighbors[i] == null)
        continue;
      // always check current grid first but ...
      gridForNeighbors = rtg;
      // ... if neighbors are not on current grid we need next or prev
      if (!rtg.contains(neighbors[i]))
        gridForNeighbors = rtg.next;
      RiText[] outerNeighbors = gridForNeighbors.neighborhood(neighbors[i]);
      for (int j = 0; j < outerNeighbors.length; j++)
      {
        if (outerNeighbors[j] == null || innerFadingNeighbors.contains(outerNeighbors[j]) || lastThree.contains(outerNeighbors[j]))
          continue;
        // only collect for outer fade if not recently read
        // and not inner neighbor
        resultList.add(outerNeighbors[j]);
      }
    }
    return resultList;
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

  protected ArrayList getInnerNeighborsToFade(ArrayList neighborList, ArrayList lastFewRead)
  {
    ArrayList resultList = new ArrayList();
    for (int i = 0; i < neighborList.size(); i++)
    {
      if (!lastFewRead.contains(neighborList.get(i)))
        resultList.add(neighborList.get(i));
    }
    // System.out.println("resultList.size(): " + resultList.size());
    return resultList;
  }

  protected ArrayList getNeighborList(MachineReader mr, RiText rt)
  {
    RiTextGrid rtg = mr.getGrid();
    RiText[] neighbors = rtg.neighborhood(rt);
    return getNeighborList(neighbors);
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

  public boolean isFadeTrailingNeigbors()
  {
    return fadeTrailingNeigbors;
  }

  public void setFadeTrailingNeigbors(boolean fadeTrailingNeigbors)
  {
    this.fadeTrailingNeigbors = fadeTrailingNeigbors;
  }

}
