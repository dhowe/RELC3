import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import trp.behavior.*; 
import trp.layout.*; 
import trp.network.*; 
import trp.reader.*; 
import trp.util.*; 
import rita.*; 
import rita.json.*; 
import rita.render.*; 
import rita.render.examples.*; 
import rita.support.*; 
import rita.wordnet.*; 
import rita.wordnet.jawbone.*; 
import rita.wordnet.jwnl.*; 
import rita.wordnet.jwnl.dictionary.*; 
import rita.wordnet.jwnl.dictionary.database.*; 
import rita.wordnet.jwnl.dictionary.file.*; 
import rita.wordnet.jwnl.dictionary.file_manager.*; 
import rita.wordnet.jwnl.dictionary.morph.*; 
import rita.wordnet.jwnl.princeton.file.*; 
import rita.wordnet.jwnl.princeton.wndata.*; 
import rita.wordnet.jwnl.util.*; 
import rita.wordnet.jwnl.util.cache.*; 
import rita.wordnet.jwnl.util.factory.*; 
import rita.wordnet.jwnl.wndata.*; 
import rita.wordnet.jwnl.wndata.list.*; 
import rita.wordnet.jwnl.wndata.relationship.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Readers2016 extends PApplet {

public static void main(String[] args) {

  System.out.println("class: "+trp.layout.Readers2016.class.getName());
  String[] options = { "--hide-stop", "--window-color=#181818", trp.layout.Readers2016.class.getName() };
  PApplet.main(options);
}
}
