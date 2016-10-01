package trp.behavior;

import rita.RiText;
import trp.layout.RiTextGrid;
import trp.reader.MachineReader;
import trp.reader.PerigramDirectionalReader;
import trp.util.Direction;
import trp.util.PerigramLookup;
import trp.util.Readers;

public class SpawnDirectionalPRs extends Spawning
{
  static boolean spawnInThread = true;

  private PerigramLookup spawningPerigrams; // was static with dch remark: yuk

  private Direction[] spawningDirections;
  private RiText prevCell, currCell, spurCell;
  private ReaderBehavior spawnedVB;

  public SpawnDirectionalPRs(PerigramLookup pl, ReaderBehavior vb, Direction... d)
  {
    this.spawningDirections = d;
    if (pl != null)
      spawningPerigrams = pl;
    if (vb != null)
    {
      this.spawnedVB = vb;
    }
  }

  public void enterWord(/* final */MachineReader mr, /* final */RiText rt)
  {
    // System.out.println("SpawnOnDirection.enterWord(lastDir="+mr.getLastDirection()+")");

    currCell = rt;
    for (int i = 0; i < spawningDirections.length; i++)
    {
      RiTextGrid rtg = mr.getGrid();
      RiText[] neighbors = rtg.neighborhood(currCell);
      spurCell = neighbors[spawningDirections[i].toInt()];
      if (spurCell == null)
        continue;
      prevCell = mr.getPreviouslyReadCell(1);
      if (spawningPerigrams.isPerigram(-1, prevCell, currCell, spurCell))
        spawnReader(mr, currCell, spawningDirections[i]);
    }
  }

  public void spawnReader(MachineReader spawner, RiText spawningCell, Direction dir)
  {
    Readers.verify(spawner != null);
    Readers.verify(spawningCell != null);
    Readers.verify(dir != null);

    MachineReader spawned = null;
    RiTextGrid rtg = spawner.getGrid();
    float spawnSpeed = spawner.getSpeed();

    // info("Spawning PerigramDirectionalReader " + dir + " at cell [" + spawningCell.getText() + "]\rbased on perigram: " + prevCell.getText() + " " + spawningCell.getText() + " " + spurCell.getText());
    spawned = new PerigramDirectionalReader(rtg, spawningPerigrams, dir);

    if (spawned != null)
    {
      spawned.setSpeed(spawnSpeed/* * 1.7f*/); // SET speed of spawned reader here
//      ReaderBehavior vb1 = new NeighborFadingVisual(MRED, rtg.template().getColor(), spawned.getSpeed());
//      ((NeighborFadingVisual) vb1).setFadeLeadingNeighbors(false);
//      ((NeighborFadingVisual) vb1).setFadeTrailingNeighbors(false);
      spawned.setBehavior(spawnedVB);
      spawned.currentCell(spawningCell);
      spawned.setHasMoved(true);
      spawned.setUpdatesDisabled(true);
      spawned.setParent(spawner);
      spawned.setTestMode(false);
      spawned.start();
    }
  }

}// end
