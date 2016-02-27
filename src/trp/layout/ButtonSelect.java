package trp.layout;

import java.util.ArrayList;
import java.util.Iterator;

import processing.core.*;
import trp.util.ReaderConstants;

public class ButtonSelect implements ReaderConstants
{

  protected static ArrayList<ButtonSelect> instances = new ArrayList<ButtonSelect>();

  // these can be set directly to manipulate appearance
  public float[] stroke = WHITE, textFill = WHITE, fill, hoverFill = { 255, 255, 255, 64f };
  public int x, y, width, height, padding = 4, strokeWeight = 2, textSize, selectedIndex;
  public String options[], label;
  public boolean hidden;
  public PFont font;

  protected PApplet p;

  public ButtonSelect(PApplet p, int x, int y, String label, String[] options)
  {

    this(p, x, y, label, options, 0);
  }

  public ButtonSelect(PApplet p, int x, int y, String label, String[] options, int selectedIndex)
  {

    this(p, x, y, null, label, options, selectedIndex);
  }

  public ButtonSelect(PApplet p, int x, int y, PFont font, String label, String[] options, int selectedIndex)
  {

    this.p = p;
    this.x = x;
    this.y = y;
    this.label = label;
    this.options = options;
    this.selectedIndex = selectedIndex;
    this.font = (font == null) ? p.loadFont("StoneSans-Semi-14.vlw") : font;
    instances.add(this);
  }

  public boolean contains(float mx, float my)
  {
    return (mx >= this.x && my >= this.y && mx < this.x + this.width && my < this.y + this.height);
  }

  public void draw(int mx, int my)
  {

    if (hidden)
      return;

    setProps();
    drawText();
    drawRects(mx, my);
  }

  private void drawText()
  {

    p.fill(textFill[0], textFill[1], textFill[2], textFill[3]);
    p.text(value(), x + width / 2f, y + height / 2f);
  }

  private void setProps()
  {

    // text-properties
    p.textAlign(PConstants.CENTER, PConstants.CENTER);
    if (font != null)
    {
      if (textSize > 0)
        p.textFont(font, textSize);
      else
        p.textFont(font);
    }

    // width/height
    if (width == 0)
    {
      for (int i = 0; i < options.length; i++)
      {
        width = (int) Math.max(width, p.textWidth(options[i]) + (padding * 2));
      }
    }
    if (height == 0)
    {
      height = (int) (p.textAscent() + p.textDescent() + (padding * 2));
    }
  }

  private void drawRects(int mx, int my)
  {

    // draw stroke-rect
    p.noFill();
    p.strokeWeight(strokeWeight);
    p.stroke(stroke[0], stroke[1], stroke[2], stroke[3]);
    p.rect(x, y, width, height);

    // draw fill-rect
    if (contains(mx, my))
    {
      p.fill(hoverFill[0], hoverFill[1], hoverFill[2], hoverFill[3]);
    }
    else if (fill != null)
    {
      p.fill(fill[0], fill[1], fill[2], fill[3]);
    }

    p.noStroke();
    p.rect(x - strokeWeight, y - strokeWeight, width + strokeWeight * 2, height + strokeWeight * 2);
  }

  public ButtonSelect advance()
  {

    selectedIndex = (selectedIndex + 1) % options.length;
    return this;
  }

  public String value()
  {

    return options[selectedIndex];
  }

  public ButtonSelect value(int idx)
  {

    this.selectedIndex = idx;
    return this;
  }

  //////////////////////////////////// statics //////////////////////////////////////////

  public static ButtonSelect click(int mx, int my)
  { // assumes no overlap between buttons

    for (Iterator it = instances.iterator(); it.hasNext();)
    {
      ButtonSelect button = (ButtonSelect) it.next();
      if (button.contains(mx, my))
        return button.advance();
    }
    return null;
  }

  public static void drawAll(int mx, int my)
  {

    for (Iterator it = instances.iterator(); it.hasNext();)
      ((ButtonSelect) it.next()).draw(mx, my);
  }
}
