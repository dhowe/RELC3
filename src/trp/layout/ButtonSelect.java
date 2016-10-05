package trp.layout;

import java.util.ArrayList;
import java.util.Iterator;

import processing.core.*;
import trp.util.ReaderConstants;

public class ButtonSelect implements ReaderConstants
{

  protected static ArrayList<ButtonSelect> instances = new ArrayList<ButtonSelect>();

  // these can be set directly (before creation) to manipulate appearance of all buttons
  public static float[] STROKE = BLACK, TEXT_FILL = WHITE, FILL, HOVERFILL = { 255, 255, 255, 64f },
      LABELFILL = OATMEAL;
  public static int PADDING = 6, STROKE_WEIGHT = 0, TEXT_SIZE;

  // these can be set directly (after creation) to manipulate appearance of one button
  public float[] stroke, textFill = WHITE;

  float[] fill;

  public float[] hoverFill = { 255, 255, 255, 64f };
  public int x, y, width, height, padding = 6, strokeWeight = 0, textSize, selectedIndex;
  public String options[], label;
  public boolean hidden;
  public PFont font, labelFont;

  protected PApplet p;
  protected int setableWidth = 0;

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
    defaults();
    this.p = p;
    this.x = x;
    this.y = y;
    this.label = label;
    this.options = options;
    this.selectedIndex = selectedIndex;
    this.font = (font == null) ? p.loadFont("StoneSans-Semi-14.vlw") : font;
    this.labelFont = p.loadFont("GillSans-Light-14.vlw");
    instances.add(this);
  }

  private void defaults()
  {
    this.stroke = STROKE;
    this.fill = FILL;
    this.textFill = TEXT_FILL;
    this.hoverFill = HOVERFILL;
    this.padding = PADDING;
    this.strokeWeight = STROKE_WEIGHT;
    this.textSize = TEXT_SIZE;
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
    drawLabel(LABELFILL);
    drawText();
    drawRects(mx, my);
  }

  private void drawLabel(float[] labelFill)
  {
    p.textFont(labelFont);
    p.fill(labelFill[0], labelFill[1], labelFill[2], labelFill[3]);
    p.text(label(), x + width / 2f, (y - height / 2f) + 2); // KLUDGE: + 2
  }

  private void drawText()
  {
    if (textSize > 0)
      p.textFont(font, textSize);
    else
      p.textFont(font);
    
    textFill = WHITE; // KLUDGE
    
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
    if (setableWidth != 0)
    {
      width = setableWidth;
    }
    else if (width == 0)
    {
      for (int i = 0; i < options.length; i++)
      {
        width = (int) Math.max(width, p.textWidth(options[i]) + (padding * 2));
      }
    }

    if (height == 0)
    {
      height = (int) (p.textAscent() + p.textDescent() + ((padding - 2) * 2)); // KLUDGE: - 2
    }
  }

  public int getWidth()
  {
    setProps();
    return width;
  }

  public void setWidth(int width)
  {
    this.setableWidth = width;
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
    else if (getFill() != null)
    {
      p.fill(getFill()[0], getFill()[1], getFill()[2], getFill()[3]);
    }

    p.noStroke();
    p.rect(x - strokeWeight, y - strokeWeight, width + strokeWeight * 2, height + strokeWeight * 2);
  }

  public ButtonSelect advance()
  {

    selectedIndex = (selectedIndex + 1) % options.length;
    return this;
  }

  public ButtonSelect advanceTo(String s) // returns null if s is not an option
  {
    boolean foundIt = false;
    for (int i = 0; i < options.length; i++)
    {
      if (options[selectedIndex].equals(s))
      {
        foundIt = true;
        break;
      }
      else
        advance();
    }
    return foundIt ? this : null;
  }

  public String label()
  {
    return label;
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

  public ButtonSelect setValue(String valueString)
  // kludge to make a button act as a monitor
  {
    options[0] = valueString;
    this.selectedIndex = 0;
    return this;
  }

  public float[] getFill()
  {
    return this.fill;
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
