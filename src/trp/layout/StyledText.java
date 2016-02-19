package trp.layout;

import processing.core.PApplet;
import processing.core.PFont;
import trp.util.ReaderConstants;

public class StyledText implements ReaderConstants
{
  protected PApplet p;
  protected String textFile;
  
  protected PFont font;
  // protected float[] textColor; // re-add?
  protected float leading = -1;
  protected float paragraphLeading = -1;
  
  protected boolean indentFirstParagraph;
  protected int indents = -1;
   
  public StyledText(PApplet p, String textFile)
  {
    this(p, textFile, (PFont)null);
  }
  
  public StyledText(PApplet p, String textFile, String vlwFontName)
  {
    this(p, textFile, p.loadFont(vlwFontName));
  }
  
  public StyledText(PApplet p, String textFile, float fontSize, String sysFontName)
  {
    this(p, textFile, p.createFont(sysFontName, fontSize));
  }
  
  public StyledText(PApplet p, String textFile, float fontSize, String sysFontName, float leading)
  {
    this(p, textFile, p.createFont(sysFontName, fontSize), leading);
  }
  
  public StyledText(PApplet p, String textFile, PFont font)
  {
    this(p, textFile, font, -1);
  }
  
  public StyledText(PApplet p, String textFile, PFont font, float leading)
  {
    this.p = p;
    this.font = font;
    this.leading = leading;
    this.textFile = textFile;
  }
  
  public PFont getFont()
  {
    return font;
  }

  public void setFont(PFont font)
  {
    this.font = font;
  }

/*  public float[] getTextColor() {
    return textColor;
  }
  public void setTextColor(float[] textColor) {
    this.textColor = textColor;
  }*/

  public float getLeading()
  {
    return leading;
  }

  public void setLeading(float leading)
  {
    this.leading = leading;
  }

  public float getParagraphLeading()
  {
    return paragraphLeading;
  }

  public void setParagraphLeading(float paragraphLeading)
  {
    this.paragraphLeading = paragraphLeading;
  }

  public boolean isIndentFirstParagraph()
  {
    return indentFirstParagraph;
  }

  public void setIndentFirstParagraph(boolean indentFirstParagraph)
  {
    this.indentFirstParagraph = indentFirstParagraph;
  }

  public int getIndents()
  {
    return indents;
  }

  public void setIndents(int indents)
  {
    this.indents = indents;
  }
  
}// end
