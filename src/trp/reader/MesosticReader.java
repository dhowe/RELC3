package trp.reader;

import rita.RiTa;
import rita.RiText;
import trp.layout.RiTextGrid;
import trp.util.Readers;

public class MesosticReader extends SimpleReader
{

  private static final String PAD = " ";

  // in the default testMode, the reader should find an 'm' word and
  // then an 'o' word etc. until it spells out the phrase below
  protected String lastWord = "", toBeSpelt = "mesostic reader";

  protected boolean checkHistory = true, noRepeatedWords = true;
  protected boolean alignOutput = true, variableSpeed = true;
  protected boolean titleIsUpperCase = false, uppercaseSelectedLetter = true;
  protected boolean caseSensitive, updateTitleLetters, allowsOffScreenJumps;

  protected float maxWordLength = 1;// originalSpeed;
  protected int characterPos = 0, titlePos = 0, lastCharacterPos = 0;
  protected String theLetter;
  protected String lastLetter;
  protected RiText lastCellRead = null;

  private String overrideTextForServer;

  // long adjustableSpeedMs = -1;

  public MesosticReader(RiTextGrid grid, String _toBeSpelt)
  {
    this(grid, _toBeSpelt, false, true);
  }

  public MesosticReader(RiTextGrid grid, String _toBeSpelt, boolean _caseSensitive, boolean _uppercaseSelectedLetter)
  {
    super(grid);
    toBeSpelt = _toBeSpelt.toLowerCase();
    caseSensitive = _caseSensitive;
    uppercaseSelectedLetter = _uppercaseSelectedLetter;
    maxWordLength = RiTextGrid.computeMaxWordLength();
  }

  // -------------------- methods --------------------------

  public boolean isUppercasingSelectedLetter()
  {
    return uppercaseSelectedLetter;
  }

  public void setUppercaseSelectedLetter(boolean uppercaseSelectedLetter)
  {
    this.uppercaseSelectedLetter = uppercaseSelectedLetter;
  }

  public void doTitleUpdate(boolean toLowercase)
  {
    String titleStr = grid.getTitleStr();

    if (titleStr.length() < 1)
      return;

    char c = titleStr.charAt(titlePos);
    while (!((c + "").equalsIgnoreCase(getTheLetter())))
    {
      if (++titlePos == titleStr.length())
        titlePos = 0;
      c = titleStr.charAt(titlePos);
    }
    String pre = titleStr.substring(0, titlePos);
    String post = titleStr.substring(titlePos + 1);

    String updated = toLowercase ? pre.toUpperCase() + Character.toLowerCase(c) + post.toUpperCase()
        : pre.toLowerCase() + Character.toUpperCase(c) + post.toLowerCase();

    grid.setTitle(updated);

    if (++titlePos == titleStr.length())
      titlePos = 0;
  }

  /** Line up the mesostics if 'alignOutput' is true */
  public String getTextForServer(RiText selected)
  {
    // System.out.println("MesosticReader.getTextForServer("+selected+")");

    if (overrideTextForServer != null)
    {
      String s = overrideTextForServer;
      overrideTextForServer = null;
      return s;
    }

    String raw = RiTa.trimPunctuation(selected.text());

    if (alignOutput)
    {
      char ch = getTheLetter().charAt(0);

      if (uppercaseSelectedLetter)
        ch = Character.toUpperCase(ch);

      int idx = raw.indexOf(ch);

      if (idx >= 0)
      {
        String orig = RiTextGrid.originalTextFor(selected);
        orig = RiTa.trimPunctuation(orig);

        String pre = orig.substring(0, idx);
        String post = orig.substring(idx + 1);
        raw = padMesostic(pre + ch + post, ch, idx);
      }
      else
        Readers.error("Mesostic letter (" + ch + ") not found in '" + raw + "'");
    }

    if (printToConsole)
      System.out.println(raw);

    return raw;
  }

  // methods -----------------------------

  private String padMesostic(String raw, char c, int idx)
  {
    String pre = raw.substring(0, idx);
    String padStr = "";
    for (int i = 0; i < maxWordLength - pre.length() - 1; i++)
      padStr += PAD;
    return padStr + raw;// pre + c + pos;
  }

  // bumps the characterPos and puts the next letter into 'theLetter'
  protected void nextLetterToBeSpelt()
  {

    lastCellRead = this.getCurrentCell();
    setLastLetter(getTheLetter()); // save the last
    lastCharacterPos = characterPos; // also save pos

    if (characterPos >= toBeSpelt.length())
    {
      // add a space after last word
      sendLineBreak();
      if (printToConsole)
        System.out.println();
      characterPos = 0;
    }

    setTheLetter(toBeSpelt.substring(characterPos, ++characterPos));

    while (!getTheLetter().toString().matches("[\\p{InBasic_Latin}\\p{InCyrillic}_]")
        || getTheLetter().toString().matches("\\s")) // was: [A-Za-z_]; trying with
    {
      setTheLetter(toBeSpelt.substring(characterPos, ++characterPos));
      sendLineBreak(); // add a space between words
      if (printToConsole)
        System.out.println();
    }
  }

  public RiText selectNext()
  {
    RiText rt = getNextCellWithLetter();

    lastWord = rt.text().toLowerCase();

    return rt;
  }

  protected RiText getNextCellWithLetter()
  {
    nextLetterToBeSpelt();

    RiTextGrid g = grid;

    RiText rt = g.nextCell(currentCell);

    RiText startWord = rt;

    while (!conditionsMet(rt))
    {
      rt = g.nextCell(rt);

      g = RiTextGrid.getGridFor(rt); // check other grids (?)
      if (rt == startWord)
      {
        System.out.println("[WARN] No words containing '" + getTheLetter() + "' found in the text!");

        if (wasSpawned())
        {
          System.out.println("      Deleting spawned reader: " + this);
          delete(this);
        }
        else
        {
          overrideTextForServer = getTheLetter();
          System.out.println("      Picking random next word...");
          rt = g.nextCell(rt); // send correct letter then just go to next word (TODO: jump down a line or 2)
        }
      }
    }
    return rt;
  }

  /*
   * public void start()
   * {
   * super.start();
   * adjustableSpeedMs = originalStepTimeMs;
   * }
   * 
   * public void adjustSpeed(float factor)
   * {
   * adjustableSpeedMs *= factor;
   * }
   */

  /*
   * protected void adjustSpeed(RiText result)
   * {
   * if (varySpeedOnWordLength())
   * {
   * // scale varies from .3 - 3.3
   * float scale = .3f + (3 * result.length() / maxWordLength);
   * 
   * float nextSpeed = (adjustableSpeedMs/1000f * scale);
   * 
   * // then multiply speed by scale
   * setSpeed(nextSpeed);
   * }
   * }
   */

  public void setTitleIsUpperCase(boolean b)
  {
    titleIsUpperCase = b;
  }

  public boolean conditionsMet(RiText rt)
  {

    // JC: added to disallow match of special punctuation
    // surrounded by whitespace (in the clock)
    if ((rt == null) || rt.text().matches("[•|]"))
      return false;

    // no repeated words
    if (rt.text().toLowerCase().equals(lastWord))
      return false;

    if (checkHistory)
    {
      // tests to see if the proposed next word has 'history'
      MachineReader[] mrs = grid.getReaders(false);
      for (int i = 0; i < mrs.length; i++)
      {
        // if the new word is in any reader's history
        if (mrs[i].getHistory().contains(rt))
        {
          // System.out.println("found history for " + rt.text());
          return false;
        }
      }
    }

    // do letter-related tests
    // JC: added to allow wild card next word for 0's in timeItems
    if (getTheLetter().equals("_"))
      return true;

    if (caseSensitive)
      return rt.contains(getTheLetter());
    else
      return rt.text().toLowerCase().contains(getTheLetter());
  }

  public String getCurrentLetter()
  {
    return getTheLetter();
  }

  public void setVariableSpeed(boolean variableSpeed)
  {
    this.variableSpeed = variableSpeed;
  }

  public boolean varySpeedOnWordLength()
  {
    return variableSpeed;
  }

  public boolean isUpdatingTitleLetters()
  {
    return updateTitleLetters;
  }

  public void setUpdateTitleLetters(boolean updateTitleLetters)
  {
    this.updateTitleLetters = updateTitleLetters;
  }

  public boolean isTitleUpperCase()
  {
    return titleIsUpperCase;
  }

  public String getTheLetter()
  {
    return theLetter;
  }

  public void setTheLetter(String theLetter)
  {
    this.theLetter = theLetter;
  }

  public String getLastLetter()
  {
    return lastLetter;
  }

  public void setLastLetter(String lastLetter)
  {
    this.lastLetter = lastLetter;
  }
}
