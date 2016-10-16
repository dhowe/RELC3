package trp.behavior;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.reader.MachineReader;
import trp.reader.MesosticReader;

public class MesosticDefault extends DefaultVisuals
{

  public MesosticDefault()
  {
    this(10f, MBLUE);
  }

  public MesosticDefault(float fadeTime, float[] color)
  {
    setFadeOutTime(fadeTime);
    setReaderColor(color);
  }

  public void enterWord(MachineReader mr, RiText rt)
  {
    MesosticReader meso = (MesosticReader) mr;

    RiTextGrid rtg = mr.getGrid();

    // Reset text of direct neighbors
    RiTextGrid.resetTextFor(rtg.previousCell(rt));
    RiTextGrid.resetTextFor(rtg.nextCell(rt));

    if (meso.isUppercasingSelectedLetter())
    {
    	String lett = meso.getTheLetter();
    	if (lett != null) {
	      String newText = RiTextGrid.originalTextFor(rt).replaceFirst(lett, lett.toUpperCase());
	      rtg.textFor(rt, newText);
    	}
    }

    super.enterWord(mr, rt);

    if (meso.isUpdatingTitleLetters())
      meso.doTitleUpdate(meso.isTitleUpperCase());
  }
  
  public void adjustForReaderSpeed(float readerSpeed)
  {
    // adjust fade time: minimum of 4 plus twice the reader speed
    setFadeOutTime(4 + (2 * readerSpeed));
  }
}