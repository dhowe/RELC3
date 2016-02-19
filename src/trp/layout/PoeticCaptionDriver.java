package trp.layout;

import processing.core.PApplet;

public class PoeticCaptionDriver
{
  public static void main(String[] args)
  {
    System.out.println("[INFO] Java version: "+System.getProperty("java.version"));
    System.out.println("[INFO] JLP: "+System.getProperty("java.library.path"));
    
    PoeticCaptionMulti.APP_ID = "PoeticCaption";
    
    String[] options = new String[] { "--present", "--hide-stop", PoeticCaptionMulti.class.getName() };
    PApplet.main(options);
  }
}
