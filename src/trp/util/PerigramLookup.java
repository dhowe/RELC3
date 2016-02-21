package trp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import processing.core.PApplet;
import rita.RiTa;
import rita.RiText;
import trp.reader.MachineReader;

public class PerigramLookup implements ReaderConstants
{
  private static PerigramLookup instance;

  private Map<String, Integer> bigramMap;
  private Map<String, Integer> perigramMap;
  private List stopWords = new ArrayList();
  private List closedClassWords = new ArrayList();
  //private boolean hasStopWord = false;

  // CONSTRUCTORS =============================================

  public PerigramLookup(PApplet p, String[] textNames)
  {
    this(p, textNames, guessFileNames(textNames));
  }
  
  private PerigramLookup(PApplet p, String[] textNames, String[] rawPerigramFiles)
  {
    long ts = System.currentTimeMillis();      
    bigramMap = hashBigrams(p, textNames);
    Readers.info("Loaded & hashed bigrams in " + RiTa.elapsed(ts));
    ts = System.currentTimeMillis();
    
    
    perigramMap = hashPerigrams(p, rawPerigramFiles);
    Readers.info("Loaded & hashed perigrams in " + RiTa.elapsed(ts));
    for (int i = 0; i < ReaderConstants.STOP_WORDS.length; i++)
    {
      stopWords.add(MachineReader.stripPunctuation(ReaderConstants.STOP_WORDS[i]).toLowerCase());
    }
    for (int i = 0; i < ReaderConstants.CLOSED_CLASS_WORDS.length; i++)
    {
      closedClassWords.add(MachineReader.stripPunctuation(ReaderConstants.CLOSED_CLASS_WORDS[i]).toLowerCase());
    }
  }

  // STATICS ==================================================

  public static PerigramLookup getInstance(PApplet p, String[] textNames, String[] rawPerigramFiles)
  {
    if (instance == null)
      instance = new PerigramLookup(p, textNames, rawPerigramFiles);
    return instance;
  }

  public static PerigramLookup getInstance(PApplet p, String ... textNames)
  {
    return getInstance(p, textNames, guessFileNames(textNames));
  }

  // METHODS ==================================================

  public boolean isPerigram(int countThreshold, RiText... rts)
  {
    verifyPerigramArray(rts);
    
    if (!checkPeriNulls(rts))
      return false;
    
    String[] words = prepareWordsForLookups(rts);
    
    // if countThreshold is set to a negative number then no adjustment is made
    // and 1 is added to the negative number to allow fixed thresholds of 0 up
    // i.e. -1 = unadjusted count threshold of 0; -11 = unadjusted 10
    if (countThreshold < 0)
      countThreshold = -(++countThreshold);
    else
      countThreshold = adjustForStopWords(countThreshold, words);
    
    return getCount(words) > countThreshold;
  }

  public boolean isPerigram(RiText... rts)
  {
    // countThreshold is set to 0
    // unadjusted (see above) if it's not passed in
    return isPerigram(-1, rts);
  }

  private boolean checkPeriNulls(RiText... rts)
  {
    if (rts[0] == null || rts[1] == null || rts[2] == null)
    {
      String more = "";
      for (int i = 0; i < rts.length; i++)
      {
        if (rts[i] == null)
          more = more + "null ";
        else
          more = more + rts[i].text() + " ";
      }
      
      // Shouldn't ever be the case: move to new grid ISSUE??
      //Readers.warn("Null word in perigram array: " + more); // doesnt cause problem?
      return false;
    }
    else
      return true;
  }

  private String[] prepareWordsForLookups(RiText... rts)
  {
    String[] words = new String[rts.length];
    for (int i = 0; i < words.length; i++)
    {
      words[i] = lowerStrip(rts[i]);
    }
    return words;
  }

  private int adjustForStopWords(int countThreshold, String... words)
  {
    //hasStopWord = false;
    for (int i = 0; i < words.length; i++)
    {
      // order of testing is significant
      // actual stop words first (more of them with more 'semantics'
      // so they don't require as high an initial countThreshold rise)
      // System.out.println("checking '"+words[i]+"'");
      if (stopWords.contains(words[i]))
      {
        //hasStopWord = true;
        if (countThreshold < 5)
          countThreshold += 5;
        else
          countThreshold += 50;
      }
      if (closedClassWords.contains(words[i]))
      {
        //hasStopWord = true;
        if (countThreshold < 10)
          countThreshold += 15;
        else
          countThreshold += 175;
      }
    }
    return countThreshold;
  }

  public int getCount(RiText... rts)
  // this version which is passed the perigram RiText cells
  // can be used by Readers interested in what the counts
  // are for its perigrams whereas isPerigram just returns
  // a boolean: whether or not there is a non-zero count
  {
    int count = 0;
    verifyPerigramArray(rts);
    if (!checkPeriNulls(rts))
      return count; // 0 if any of the cells are null
    String[] words = prepareWordsForLookups(rts);
    int countThreshold = adjustForStopWords(0, words);
    count = getCount(words);
    return count > countThreshold ? count : 0;
  }

  public int getCount(String... words)
  {
    String periKey = makePerikey(words);
    int count = 0;
    if (perigramMap.containsKey(periKey))
      count = perigramMap.get(periKey);
    // System.out.println(periKey +" -> "+count);
    return count;
  }

  private String makePerikey(String... words)
  {
    return words[0] + " " + words[1] + " " + words[2];
  }

  private void verifyPerigramArray(RiText... rts)
  {
    if (rts.length != 3)
      Readers.error("Bad array size=" + rts.length + ", expecting 3");
  }

  public String lowerStrip(RiText rt)
  {
    return MachineReader.stripPunctuation(rt.text()).toLowerCase();
  }

  // never-called
  @SuppressWarnings("unused")
  private boolean isAlsoDigram(RiText r0, RiText r1, RiText r2)
  {
    return isPerigram(r0, r1, r2) && (bigramMap.containsKey(r0.text() + " " + r1.text()));
  }

  public boolean isBigram(RiText r0, RiText r1)
  {
    return bigramMap.containsKey(r0.text() + " " + r1.text());
  }
  public Map<String, Integer> hashBigrams(PApplet p, String[] textNames)
  {
    for (int j = 0; j < textNames.length; j++)
    {
      //System.out.println("loading "+textNames[j]);
      String data = RiTa.loadString(textNames[j]);
      computeBigrams(data);
    }
    //System.out.println(bigramMap.size()+" unique bigrams");//"+bigramMap);
    
    return bigramMap;
  }

  private void computeBigrams(String txt)
  {
    if (bigramMap == null)
      bigramMap = new HashMap<String, Integer>();
    
     String[] words = txt.split("\\s+");
     String last = words[0];

     for (int i = 1; i < words.length; i++)
     {
        if (words[i].matches("<pb?\\/?>")) { // <p/> or <pb/> 
          continue;
        }

        bigramMap.put(last+' '+words[i], 0);
        last = words[i];
    }
  }

  // should write these to a file and just load them?
  public Map<String, Integer> hashBigramsOld(PApplet p, String[] textNames)
  {
    bigramMap = new HashMap<String, Integer>();

    // load all the texts in and then makes a Map with
    // keys of all the syntagmatic bigrams in these texts
    String firstWord = null, lineLast = null;
    String[] bigram = new String[2];
    for (int j = 0; j < textNames.length; j++)
    {
      String[] rows = p.loadStrings(textNames[j]);
      System.out.println("PerigramLookup.hashBigrams() loading "+textNames[j]);
      for (int i = 0; i < rows.length; i++)
      {
        // strip out any tags:
        rows[i] = rows[i].replaceAll("\\<.*?>", "");
        // make sure lowerCase ?? need to strip punctuation ??
        rows[i] = rows[i].toLowerCase();
        String words[] = rows[i].split(" ");
        if (firstWord == null)
          firstWord = words[0];
        if (lineLast != null)
          bigramMap.put(lineLast + " " + words[0], 0);
        lineLast = null;
        for (int k = 0; k < (words.length - 1); k++)
        {
          bigram[0] = words[k];
          bigram[1] = words[k + 1];
        }
        // map: String (the proposed digram) -> Integer (count)
        bigramMap.put(bigram[0] + " " + bigram[1], 0);
//System.out.println(bigram[0] + " " + bigram[1]);
        if (lineLast == null)
          lineLast = bigram[1];
      }
      bigramMap.put(lineLast + " " + firstWord, 0);
      firstWord = null;
    }
    System.out.println(bigramMap.size()+" bigrams");
    
    System.out.println(bigramMap);
    return bigramMap;
  }

  private static String[] guessFileNames(String[] textNames) // util
  {
    String[] periNames = new String[textNames.length];
    for (int i = 0; i < textNames.length; i++)
      periNames[i] = textNames[i].replaceAll("\\.txt", "Perigrams.txt");
    return periNames;
  }
  
  public Map<String, Integer> hashPerigrams(PApplet p, String[] rawTrigramsFile)
  {
    perigramMap = new HashMap<String, Integer>();

    // the rawTrigrams files are output from the ngrams
    // dbase and are thus already processed (lowercase etc)
    for (int j = 0; j < rawTrigramsFile.length; j++)
    {
      String[] rows = p.loadStrings(rawTrigramsFile[j]);
      for (int i = 0; i < rows.length; i++)
      {       
        String words[] = rows[i].split("\\s+");

        // map: String (the proposed perigram) -> Integer (count)
        perigramMap.put(words[0] + " " + words[1] + " " + words[2], Integer.parseInt(words[3]));
      }
    }
    return perigramMap;
  }

  public Map<String, Integer> getHashedPerigrams()
  {
    return perigramMap;
  }
  
  public static void main(String[] args)
  {
    PerigramLookup.getInstance(null, "/Users/dhowe/Documents/javascript-workspace/ReadersJS/data/image.txt");
  }

}
