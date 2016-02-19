package trp.behavior;

import java.util.Timer;
import java.util.TimerTask;

import rita.RiText;
import rita.render.RiTextBehavior;
import trp.layout.PageManager;
import trp.reader.MachineReader;
import trp.util.ReaderConstants;
import trp.util.Readers;

public abstract class ReaderBehavior implements ReaderConstants
{
  protected Timer timer;
  protected float fadeInTime = 1f;
  protected float fadeOutTime = 2f;
  protected float delayBeforeFadeBack = fadeInTime * 2f;
  protected float delayBeforeFadeIn = 0;
  protected float[] readerColor = BRIGHT_RED;
  protected boolean fadingOnPageTurn;
  protected boolean disableFades;

  public void fadeCell(RiText word, float[] color, float durationSec)
  {
    fadeCell(word, color, 0, durationSec);
  }
  
  public void fadeCell(final RiText word, final float[] color, final float startOffsetSec, final float durationSec)
  {
    if (disableFades ) { // for testing only
      word.fill(color);
      return;
    }
    
    // handle readers trying to fade-in on a fading-out grid
    PageManager pm = PageManager.getInstance();
    if (pm != null && pm.isFadingOnFlip(word)) return; 
    
    if (startOffsetSec > 0)
    {
      if (timer == null)
      {
        timer = new Timer();
      }
      timer.schedule(new TimerTask()
      {
        public void run()
        {
          RiTextBehavior.deleteAllFades(word);
          word.colorTo(color, durationSec);
          
        }
      }, (long) (startOffsetSec * 1000));
    }
    else
    {
      RiTextBehavior.deleteAllFades(word);
      word.colorTo(color, durationSec);
    }
  }
  
  public static void info(String msg)
  {
    Readers.info(msg);
  }

  public void enterWord(MachineReader mr, RiText word)
  {
    // do nothing
  }

  public void exitWord(MachineReader mr, RiText word)
  {
    // do nothing
  }

  public float[] getReaderColor()
  {
    return readerColor;
  }

  public void setReaderColor(float[] color)
  {
    this.readerColor = color;
  }

  public float getFadeInTime()
  {
    return fadeInTime;
  }

  public void setFadeInTime(float fadeInTime)
  {
    this.fadeInTime = fadeInTime;
  }

  public float getFadeOutTime()
  {
    return fadeOutTime;
  }

  public void setFadeOutTime(float fadeOutTime)
  {
    this.fadeOutTime = fadeOutTime;
  }

  public float getDelayBeforeFadeBack()
  {
    return delayBeforeFadeBack;
  }

  public void setDelayBeforeFadeBack(float delayBeforeFadeBack)
  {
    this.delayBeforeFadeBack = delayBeforeFadeBack;
  }

  public float getDelayBeforeFadeIn()
  {
    return delayBeforeFadeIn;
  }

  public void setDelayBeforeFadeIn(float delayBeforeFadeIn)
  {
    this.delayBeforeFadeIn = delayBeforeFadeIn;
  }

  public void verify(boolean b, String msg)
  {
    Readers.verify(b, msg);
  }

  public void verify(boolean b)
  {
    Readers.verify(b);
  }

}// end
