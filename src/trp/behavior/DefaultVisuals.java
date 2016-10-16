package trp.behavior;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.reader.MachineReader;

public class DefaultVisuals extends ReaderBehavior
{
  static private final float FADEINFACTOR = .8f, FADEOUTFACTOR = 2f, DELAYFACTOR = 2f;

  public DefaultVisuals() {}
  
  public DefaultVisuals(float[] rColor)
  {
    setReaderColor(rColor);
  }
  
  public DefaultVisuals(float[] rColor, float fadeIn, float fadeOut, float delay)
  {
    this(rColor);
    setFadeInTime(fadeIn);
    setFadeOutTime(fadeOut);
    setDelayBeforeFadeBack(delay);
  }
  
  public DefaultVisuals(float[] rColor, float rSpeed)
  {
    this(rColor, rSpeed * FADEINFACTOR, rSpeed * FADEOUTFACTOR, rSpeed * DELAYFACTOR);
  }

  public DefaultVisuals(float[] rColor, float fadeIn, float fadeOut, float delay, float delayIn)
  {
    this(rColor, fadeIn, fadeOut, delay);
    setDelayBeforeFadeIn(delayIn);
  }
  
  public DefaultVisuals(float[] rColor, float delayIn, float rSpeed)
  {
    this(rColor, rSpeed);
    setDelayBeforeFadeIn(delayIn);
  }
  
  public void enterWord(MachineReader mr, RiText word)
  {
    super.enterWord(mr, word);
    
    // System.out.println("DefaultVisuals.enterWord("+mr+","+word+")");
    RiTextGrid grid = mr.getGrid();

    fadeCell(word, getReaderColor(), getDelayBeforeFadeIn(), getFadeInTime());
    // word.fill(getColor());
    // word.showBounds(mr.testMode);
    
    fadeCell(word, grid.template().fill(), getDelayBeforeFadeBack() + getDelayBeforeFadeIn(), getFadeOutTime());
  }

  public void exitWord(MachineReader mr, RiText word)
  {
    // System.out.println("DefaultVisuals.exitWord("+mr+","+word+")");
    word.showBounds(false);
  }

  public void adjustForReaderSpeed(float readerSpeed)
  {
    setFadeInTime(readerSpeed * FADEINFACTOR);
    setFadeOutTime(readerSpeed * FADEOUTFACTOR);
    setDelayBeforeFadeBack(readerSpeed * DELAYFACTOR);
  }
}