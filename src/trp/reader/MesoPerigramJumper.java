package trp.reader;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import rita.RiText;
import trp.behavior.MesosticDefault;
import trp.layout.RiTextGrid;
import trp.util.PerigramLookup;
import trp.util.Readers;

public class MesoPerigramJumper extends MesosticReader
{
  private static boolean DBUG = true; // TODO:

  private static final int CHECK_PERIGRAMS = 2;
  private static final int CHECK_DIGRAMS = 1;
  private static final int CHECK_NONE = 0;

  private PerigramLookup perigrams;
  
  protected MesoPerigramJumper(RiTextGrid grid, String toBeSpelt)
  {
    super(grid, toBeSpelt);
  }

  public MesoPerigramJumper(RiTextGrid grid, String toBeSpelt, PerigramLookup perigrams)
  {
    super(grid, toBeSpelt);
    this.caseSensitive = false;
    this.perigrams = perigrams;
    this.uppercaseSelectedLetter = true;
    this.toBeSpelt = toBeSpelt.toLowerCase();
    setDefaultVisualBehavior(new MesosticDefault());
  }

  /**
   * Look first for words on subsequent lines with n-grams,
   * then subsequent lines without n-grams, then default to
   * MesosticReader.selectNext(), if something good has not yet been found...
   */
  public RiText selectNext()
  {
    // System.out.println("MesoDigramJumper.selectNext("+theLetter+")");

    if (this.getCurrentCell() != lastCellRead) // TODO: quick fix for page-turning
      nextLetterToBeSpelt();

    int[] ngramLineIdxs = { 1, 2, 3, 4, 5, 6 };

    RiText result = checkLines(ngramLineIdxs, CHECK_PERIGRAMS);

    // got nothing, now retry, with digrams
    if (result == null)
    {
      if (printToConsole && DBUG)
        Readers.info("MesoPerigramJumper: no perigrams for '" + getTheLetter() + "',  trying digrams...");
      result = checkLines(ngramLineIdxs, CHECK_DIGRAMS);
    }

    // got nothing, now retry, ignoring ngrams
    if (result == null)
    {
      if (printToConsole && DBUG)
        Readers.info("MesoPerigramJumper: no digrams for '" + getTheLetter() + "',  trying w'out ngrams...");
      int[] letterLineIdxs = { 1, 2, 3, 4 }; // less lines
      result = checkLines(letterLineIdxs, CHECK_NONE);
    }
    else
    {
      // Readers.info("MesoPerigramJumper got digram!!!!!! for '" + theLetter+" -> "+result.text());
    }

    // still got nothing, use normal mesostic method
    if (result == null)
    {
      if (DBUG)
        Readers.info("MesoPerigramJumper giving up on '" + getTheLetter()
            + "', defaulting to MesosticReader...");
      this.setTheLetter(getLastLetter()); // hack
      characterPos = lastCharacterPos; // hack
      result = super.selectNext();
    }

    if (result == null)
      Readers.error("Unable to find word for '" + getTheLetter() + "'");

    // DISABLED FOR NOW

    // adjust speed/fade-time according to word-length (?)
    // adjustSpeed(result);

    return result;
  }

  private RiText checkLines(int[] targetLines, int mode)
  {
    Point p = currentCoords();

    RiText result = null;

    OUTER: for (int j = 0; j < targetLines.length; j++)
    {
      RiTextGrid rtg = grid;

      // use targets above
      int lineIdx = p.y + targetLines[j];

      // but last line wraps
      if (p.y == rtg.numLines() - 1)
      { // changed: 2/1, was this.grid
        lineIdx = j;
        rtg = rtg.getNext();
      }

      // don't go off the grid
      if (lineIdx > rtg.numLines() - 1)
        continue;

      // get matching words, ordered by x-distance
      List matches = (mode != CHECK_NONE) ? searchLineForLetterUsingNgrams(getTheLetter(), rtg, lineIdx, mode)
          : searchLineForLetter(getTheLetter(), rtg, lineIdx);

      if (!matches.isEmpty())
      {
        result = (RiText) matches.get(0);

        if (result != null && mode != 0 && result.distanceTo(currentCell) > 500)
        {
          // System.out.println("skipping big dist");
          result = null;
        }
        else
          break OUTER;
      }
    }
    return result;
  }

  private List searchLineForLetterUsingNgrams(String theLetter, RiTextGrid rtg, int lineIdx, int mode)
  {
    verify(mode == CHECK_DIGRAMS || mode == CHECK_PERIGRAMS);

    RiText last = getLastReadCell();
    if (last == null)
      mode = CHECK_DIGRAMS;
    List result = searchLineForLetter(theLetter, rtg, lineIdx);

    for (Iterator it = result.iterator(); it.hasNext();)
    {
      RiText rt = (RiText) it.next();

      if (mode == CHECK_PERIGRAMS)
      {
        if (!isTrigram(last, currentCell, rt))
          it.remove();
      }
      else if (mode == CHECK_PERIGRAMS)
      {
        if (!isBigram(currentCell, rt))
          it.remove();
      }
    }

    return result;
  }

  public boolean isBigram(RiText rt1, RiText rt2)
  {
    return perigrams.isBigram(rt1, rt2);
  }

  public boolean isTrigram(RiText... phrase)
  {
    return perigrams.isPerigram(phrase);
  }

  List searchLineForLetter(String theLetter, RiTextGrid rtg, int lineIdx)
  {
    if (theLetter == null)
      throw new IllegalArgumentException("Bad letter: " + theLetter);
    if (lineIdx > rtg.numLines() - 1)
      throw new IllegalArgumentException("Bad line index: " + lineIdx);
    List result = new ArrayList();
    RiText[] words = rtg.lineAt(lineIdx); // try a line
    try
    {
      for (int i = 0; i < words.length; i++)
      {
        if (words[i] == null)
          continue;
        if (words[i].contains(theLetter))
          result.add(words[i]);
      }
    }
    catch (Exception e)
    {
      Readers.warn("searchLineForLetter() errored on exit...");
    }

    Collections.sort(result, new Comparator()
    {
      public int compare(Object o1, Object o2)
      {
        float d1 = currentCell.distanceTo((RiText) o1);
        float d2 = currentCell.distanceTo((RiText) o2);
        return d2 > d1 ? -1 : 1;
      }
    });

    return result;
  }

}// end
