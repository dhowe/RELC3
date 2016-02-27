package trp.layout;

import processing.core.PApplet;

public class ButtonDemo extends PApplet {

	ButtonSelect textSelect, readerSelect;
	
	public void settings() {
		
		size(500, 100);
	}

	public void setup() {
		
		textSelect = new ButtonSelect(this, 100, 40, "Text",
				new String[]{ "Misspelt Landings", "Poetic Caption", "The Image" });

		readerSelect = new ButtonSelect(this, 300, 40, "Reader",
				new String[]{ "Perigram", "Mesostic", "Lookahead" });
	}

	public void draw() {
		
		background(0);
		ButtonSelect.drawAll(mouseX, mouseY);
	}
	
	public void mouseClicked() {
		
		ButtonSelect clicked = ButtonSelect.click(mouseX, mouseY);
		if (clicked != null)  {
			System.out.println(clicked.label+ "="+clicked.value());
		}
	}
	
	public static void main(String[] args) {
    
    PApplet.main(new String[] { ButtonDemo.class.getName() });
	}
}
