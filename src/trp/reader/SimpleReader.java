package trp.reader;


import java.awt.Point;

import rita.RiText;
import trp.behavior.DefaultVisuals;
import trp.layout.RiTextGrid;
import trp.util.Direction;


public class SimpleReader extends MachineReader 
{
	// constructors -----------------------------

	public SimpleReader(RiTextGrid grid) {
		this(grid, false);
	}

	public SimpleReader(RiTextGrid grid, Point startCell) {
		this(grid, startCell, false);
	}

	public SimpleReader(RiTextGrid grid, boolean test) {
		this(grid, new Point(1,0), test);
	}
	
  public SimpleReader(RiTextGrid grid, Point startCell, boolean test)
  {
    super(grid, startCell.x, startCell.y);
    this.testMode = test; 
    setDefaultVisualBehavior(new DefaultVisuals());
  }

	// methods -----------------------------

  /**
	 * Returns the next RiText that the reader will enter; in this case, the next word
	 * in the text. <p> 
	 * 
	 * Note: If the returned RiText is null, the reader will remain where it is. 
	 */
	public RiText selectNext() 
	{
	  RiText next = reverse ? grid.previousCell(currentCell) : grid.nextCell(currentCell);
		lastDirection = reverse ? Direction.W : Direction.E;
		//System.out.println(next);
		return next;
	}

}// end

