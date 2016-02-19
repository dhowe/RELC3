package trp.reader;

import java.awt.geom.Point2D;

import rita.RiTa;
import rita.RiText;
import trp.layout.RiTextGrid;
import trp.util.PerigramLookup;
import trp.util.Readers;

/*
 * TODO:
 *   up the prob when many sequential words have occurred 
 *   
 *   after replacing a noun, make sure the next word is a 
 *     perigram (need flag to remember this) [if not, do what?]
 */
public class LookaheadPosReader extends SimpleReader
{
  private static final boolean DBUG = false;
  private static final int MAX_LINE_JUMP = 5;
  
  private PerigramLookup perigrams;
  
  private String allowedSubstitutions = "nn nns vb vbg vbn jj jjr jjs "; // end with space
  
  private String[] phrases;
  
  public LookaheadPosReader(RiTextGrid grid, PerigramLookup perigrams)
  {
    super(grid);
    this.perigrams = perigrams;
  }
  
  public RiText selectNext()
  {
    int tries = 0, maxLookahead = 200;
    
    RiText next = grid.nextCell(currentCell);
    RiText toReplace = next; 

    String pos = (String) next.features().get(RiTa.POS);
    if (pos == null) return print(next);
    
    if (!allowedSubstitutions.contains(pos+" ")) 
      return print(next);
    
    if(DBUG) System.out.println("  Found pos("+pos+") "+next);
 
    RiTextGrid tmp = RiTextGrid.getGridFor(next);

    while (tries++ < maxLookahead) 
    {
      next = tmp.nextCell(next);
      
      // check the length ==================================
      if (next.length() < 2) continue;
      
      // match the part-of-speech ==========================
      String newPos = (String) next.features().get(RiTa.POS);
      if(DBUG) System.out.println("    Checking: "+next+"='"+newPos+"'");     
      if (newPos == null || !newPos.equals(pos)) 
        continue; 
      
      // check the history =================================
      if (inHistory(next.text())) {
        if(DBUG) System.out.println("    Skipping (already in history):  "+next.text());
        continue; 
      }
      
      // check the distance ================================
      int dist = RiTextGrid.yDistance(toReplace, next);
      if (dist > MAX_LINE_JUMP)
      { 
        if(DBUG) System.out.println("    Bailing (too far to jump): "+next.text());
        next = toReplace; // too far already, give up
        break;
      }

      // check the perigrams ===============================
      RiText last = getLastReadCell();
      if (last != null)       // check perigrammer
      {
        if (!perigrams.isPerigram(last, currentCell, next)) {
          if(DBUG) System.out.println("    Skipping (not a perigram: '"+last+" "+currentCell+" "+next+"')");
          continue;
        }
      }
      
      // check the probability ============================
      float prob = .3f + (.5f - (dist/(float)(2*MAX_LINE_JUMP))); //  .3-.8
      if (Math.random() > prob) {
        if(DBUG) System.out.println("    Skipping (missed on prob): "+next+" [dist="+dist+" prob="+prob+"]");
        continue;
      }
        
      break; // return
    }
    
    if (tries == maxLookahead)
      Readers.warn(getClass().getName()+" failed for "+currentCell+" "+getLastReadCell());
    
    return print(next);
  }

  private RiText print(RiText next)
  {
    // DCH: should use a ResetWordBehavior instead!
    RiTextGrid.resetTextFor(next); // added: dch 9/27 (to repair capitalizations) 
    
    if (printToConsole) 
      System.out.println(next.text().toUpperCase());//+"      ("+getLastReadCell()+" "+currentCell+" "+next+")");
    
    // check whether we add a blank line
    if (phrases != null) {
 
      if (currentCell != null) {
        String toCheck = currentCell.text()+" "+next.text();
        
        //System.out.println("Checking: "+toCheck);
        
        for (int i = 0; i < phrases.length; i++)
        {
          if (phrases[i].endsWith(toCheck)) {
            
           // System.out.println("LINE_BREAK, found: '"+toCheck+"' in '"+phrases[i]+"'");
            
            sendLineBreak(); // add a space between words
            if (printToConsole)
              System.out.println();
            break;
          }
        }
      }
      else
        System.out.println("LAST = null!");
    }

    return next;
  }

  public String getTextForServer(RiText selected) 
  {
    int maxChars = 20;
    float[] c = selected.center();
    Point2D pt = new Point2D.Float(c[0],c[1]); 
    float relPos = (float)pt.getX() / (float)selected.getPApplet().width;
    int numSpaces = (int)(relPos * maxChars);
    StringBuilder spacing = new StringBuilder();  
    for (int i = 0; i < numSpaces; i++)
      spacing.append(' ');
    String send = spacing + super.getTextForServer(selected).toUpperCase();
    //System.out.println("LAReader.sending: "+send);
    return send;
  }

  /** phrases to check for line breaks */
  public void addPhrases(String[] phrases)
  {
    this.phrases = phrases;
  }
  
}
