package trp.behavior;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.reader.MachineReader;
import trp.reader.MesosticReader;

public class MesosticHaloingVisual extends ClearHaloingVisual
{

  public MesosticHaloingVisual(float[] rColor, float[] gColor, float rSpeed)
  {
    super(rColor, gColor, rSpeed);
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
      String newText = RiTextGrid.originalTextFor(rt).replaceFirst(meso.theLetter, meso.theLetter.toUpperCase());

      rtg.textFor(rt, newText);
    }

    super.enterWord(mr, rt);
  }

}
