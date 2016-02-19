package trp.util;

import java.io.*;
import java.net.*;
import java.util.*;

import processing.core.PApplet;

import rita.*;
import rita.render.RiLerpBehavior;
import trp.reader.MachineReader;

public class Readers implements ReaderConstants 
{
	public static final String PREFERENCE_URL = "http://rednoise.org/readers/readers-prefs.txt";

	public static Map PREFERENCES;
	
  public static boolean NO_VISUALS = false;

	static {
		System.out.println("[INFO] Readers.version [" + VERSION + "]");
		//if (PRODUCTION_MODE) PREFERENCES = loadPrefs(PREFERENCE_URL);
	}

	// METHODS ==================================================s

  public static RiLerpBehavior createLerp(RiText cellAt, int start, int target, float durationSec)
  {
      // System.out.println("RiText.createLerp("+start+","+target+","+startOffsetSec+","+durationSec+")");
    RiLerpBehavior rlb = new RiLerpBehavior(cellAt, start, target, 0, durationSec);
    cellAt.behaviors.add(rlb);
    return rlb;
  }
  
	public static Map<String, Integer> hashDigrams(PApplet p,
			String[] rawTrigramsFile) {
	  
		// if (digramsHashed == null)
		long ts = System.currentTimeMillis();
		Map<String, Integer> digramMap = new HashMap<String, Integer>();

		// the rawTrigrams file is output from the ngrams
		// dbase and is thus already processed (lowercase etc)
		for (int j = 0; j < rawTrigramsFile.length; j++) {
			String[] rows = p.loadStrings(rawTrigramsFile[j]);
			for (int i = 0; i < rows.length; i++) {
				String words[] = rows[i].split(" ");

				// map: String (the proposed digram) -> Integer (count)
				digramMap.put(words[1] + " " + words[2], Integer
						.parseInt(words[3]));
			}
		}
		Readers.info("Loaded digram Map in " + (System.currentTimeMillis()-ts));

		return digramMap;
	}

	public static Map<String, Integer> hashPerigrams(PApplet p,
			String[] rawTrigramsFile) {
		// if (digramsHashed == null)
		long ts = System.currentTimeMillis();
		Map<String, Integer> pMap = new HashMap<String, Integer>();

		// the rawTrigrams file is output from the ngrams
		// dbase and is thus already processed (lowercased etc)
		for (int j = 0; j < rawTrigramsFile.length; j++) 
		{
			String[] rows = p.loadStrings(rawTrigramsFile[j]);
			for (int i = 0; i < rows.length; i++) {
				String words[] = rows[i].split(" ");

				// map: String (the proposed digram) -> Integer (count)
				pMap.put(words[0] + " " + words[1] + " " + words[2], 
				    Integer.parseInt(words[3]));
			}
		}
		Readers.info("Loaded perigram Map in " +  (System.currentTimeMillis()-ts));
		return pMap;
	}

	/**
	 * Returns a list of words from a text (one per grid cell)
	 */
	public static List parseWordsFromFile(PApplet p, String fileName) {
		System.out.println("[INFO] Loading: " + fileName);
		String s = RiTa.loadString(/*p, */fileName);
		return parseWords(p, s);
	}

	public static List parseWords(PApplet p, String contents) {
		String[] sentences = RiTa.splitSentences(contents);
		List words = new ArrayList();
		for (int i = 0; i < sentences.length; i++) {
			RiText[] rts = new RiText(p, sentences[i]).splitWords();
			for (int j = 0; j < rts.length; j++) {
				if (!rts[j].equals("—"))
					words.add(rts[j].text());
			}
		}
		System.out.println("[INFO] Found " + words.size() + " words...");
		return words;
	}

	public static boolean serverEnabled() {
	  return MachineReader.USE_SERVER;
	}
	
	public static void enableServer(String serverHost) {
		MachineReader.USE_SERVER = true;
		if (serverHost != null)
			MachineReader.SERVER_HOST = serverHost;
	}

	public static void enableServer() {
		enableServer(null);
	}

	public static void error(final String message) {
		error(message, null, false);
	}

	public static void error(final String message, Throwable cause) {
		error(message, cause, false);
	}
	
	 public static void error(Throwable cause) {
	    error("[no message]", cause, false);
	  }

	public static void error(final String message, Throwable cause, boolean alwaysFatal) {
		String msg = message;
		if (cause != null) {
			msg += "\n\t\tCAUSE: " + stackToString(cause);
		} else {
			try {
				throw new RuntimeException();
			} catch (Exception e) {
				cause = e;
			}
		}

		//Logger.getLogger(LOG_ID).error(message, cause);
		System.err.println("[ERROR] "+message+"\n"+cause);

		if (alwaysFatal || !MachineReader.PRODUCTION_MODE) {
			System.err.println("[FATAL] "+msg+"\n");
			System.exit(0);
		}
	}
	
  public static String stackToString(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

	public static void warn(String msg) {
		//Logger.getLogger(LOG_ID).warn("[WARN] " + msg);
	  System.err.println("[WARN] " + msg);
	}

	// to console only...
	public static void info(String msg) {
		System.out.println("[INFO] " + msg);
	}

	public static void info() {
	  System.out.println();
	}

	public static String getPref(String key, String defaultValue) {
		return getPref(PREFERENCES, key, defaultValue);
	}

	private static String getPref(Map m, String key, String defaultValue) {
		if (m == null)
			return defaultValue;
		String value = (String) m.get(key);
		return value == null ? defaultValue : value;
	}

	public static boolean getPref(String key, boolean defaultValue) {
		return getPref(PREFERENCES, key, defaultValue);
	}

	private static boolean getPref(Map m, String key, boolean defaultValue) {
		if (m == null)
			return defaultValue;
		String value = (String) m.get(key);
		boolean result = defaultValue;
		if (value != null)
			result = value.equals("1") || value.equalsIgnoreCase("t")
					|| value.equalsIgnoreCase("true");
		return result;
	}

	public static float getPref(String key, float defaultValue) {
		return getPref(PREFERENCES, key, defaultValue);
	}

	private static float getPref(Map m, String key, float defaultValue) {
		if (m == null)
			return defaultValue;
		String value = (String) m.get(key);
		float result = defaultValue;
		if (value != null)
			result = Float.parseFloat(value);
		return result;
	}

	public static int getPref(String key, int defaultValue) {
		return getPref(PREFERENCES, key, defaultValue);
	}

	private static int getPref(Map m, String key, int defaultValue) {
		if (m == null)
			return defaultValue;
		String value = (String) m.get(key);
		int result = defaultValue;
		if (value != null)
			result = Integer.parseInt(value);
		return result;
	}

	private static Map loadPrefs(String url) {
		Map m = new HashMap();
		try {
			URL destURL = new URL(url);
			URLConnection connection = destURL.openConnection();
			InputStream is = connection.getInputStream();
			String[] lines = PApplet.loadStrings(is);
			for (int i = 0; i < lines.length; i++) {
				if (lines[i] == null || lines[i].length() < 1
						|| lines[i].startsWith("#"))
					continue;
				String[] s = lines[i].split("=");
				if (s.length != 2)
					System.out.println("\n[WARN] Bad preference: " + lines[i]
							+ "\n");
				m.put(s[0], s[1].trim());
			}
		} catch (Exception e) {
			String msg = "Unable to load properties file from: " + url;
			if (!MachineReader.PRODUCTION_MODE)
				Readers.error(msg, e);
			else
				warn("[WARN] " + msg);
		}
		if (m.size() > 0)
			info("Prefs=" + m);

		return m;
	}

	/** an assertion: throws an exception if boolean is false */
	public static void verify(boolean b) {
		verify(b, "");
	}

	/** an assertion: throws error (and prints msg) if boolean exp is false */
	public static void verify(boolean exp, String msg) {
		if (exp)
			return; // ok
		try {
			throw new RuntimeException("Verify check failed: " + msg);
		} catch (Exception e) {
			error(msg, e);
		}
	}
	
	/**
	 *  Add RiTa features (pos, phonemes, syllables, etc) to each RiText word
	 */
  public static void addRiTaFeatures(PApplet p, String[] sents, RiText[] words, String[] correctedPos) 
  {
    if (correctedPos == null)
      Readers.warn("[WARN] No POS-tags found, tagging from scratch...");
    
    RiLexicon dict = new RiLexicon();
    
    int wordCount = 0;
    Map lex = dict.lexicalData();
    for (int i = 0; i < sents.length; i++)
    {
      if (sents[i].length()<1) continue;
      
      String[] tokens = sents[i].split(" ");
      
      //System.out.println("Found "+tokens.length+" tokens");
      
      String[] tags = correctedPos != null ? 
          new String[words.length] : getPosTags(words);
          
      //System.out.println("Found "+tags.length+" tags\n");
          
      for (int j = 0; j < tokens.length; j++)
      {
        /*String word = RiTa.stripPunctuation(tokens[j]);
        String allPos = "_"; // unknown 
        String tmp = (String)lex.get(word);
        
        if (tmp != null) {
          allPos = tmp.split("\\|")[1].trim();
        }*/
        
        //System.out.println(words[wordCount]+":"+tags[j]);
        if (correctedPos != null) {
          String[] posData = correctedPos[wordCount].split("=");
          if (posData.length!=2)
            Readers.error("Bad pos entry: "+correctedPos[wordCount]);
          tags[j] = posData[1];
        }
        
        words[wordCount].features().put(RiTa.POS, tags[j]);
        
        wordCount++;
      }
    }
    
    if (wordCount != words.length)
      Readers.error("Mismatched sizes: wordCount="+wordCount+" != words.length="+words.length);
    
    if (correctedPos != null && wordCount != correctedPos.length)
      Readers.error("Mismatched sizes: wordCount="+wordCount+" != correctedPos.length)="+correctedPos.length);
  }
  
  private static String[] getPosTags(RiText[] words)
  {
    String[] swords = new String[words.length];
    for (int i = 0; i < swords.length; i++)
      swords[i] = words[i].text();
    return RiTa.getPosTags(swords);
  }

  /** @invisible */
  public static final float[] unhex(int hexColor){
    // note: not handling alphas...
    int r = hexColor >> 16;
    int temp = hexColor ^ r << 16;
    int g = temp >> 8;
    int b = temp ^ g << 8;
    return new float[]{r,g,b,255};
  }
  
  /**
   * Fetches page from specified Http URL
   * @param url Remote host & page to connect to.
   *   ex. "http://192.168.3.20:650/index.html".
   * @return Contents of page
   */
  public static String httpGet(URL url) throws IOException
  {
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setConnectTimeout(5000);
    conn.setRequestProperty("User-Agent", "Mozilla/4.05 [en] (WinNT; I)");
    conn.setRequestProperty("Accept-Language", "en");
    conn.setRequestProperty("Accept-Charset", "iso-8859-1,*,utf-8");
    conn.setUseCaches(false); // added: dch
    conn.connect();

    int rc = conn.getResponseCode();

    //  System.out.println("HTML="+getHtmlResponse(conn));

    String response /*= RESPONSE_OK;
    if (rc == 200) 
      response */= getHtmlResponse(conn);
    
    conn.disconnect();
    
    return response;
  }

  public static String trimEnds(String token)
  {
    boolean gotChar = false;
    token = token.trim();
    char[] c = token.toCharArray();
    token = "";
    for (int i = 0; i < c.length; i++)
    {
      if (gotChar || !Character.isSpaceChar(c[i])) {
        token += c[i];
        gotChar = true;
      }
    }
    return token;
  }
  
  // Sample loading methods ====================================

  // TODO ?? JHC removed sample handling for ELC deliverables
  
/*  public static RiSample loadSample(PApplet p, String sampleFileName) {
    return loadSample(p, sampleFileName, false);
  }    
  
  public static RiSample loadSample(PApplet p, String sampleFileName, boolean setLooping) 
  {
    RiSample sample = RiSample.create(p, RiSample.MINIM_SAMPLE_PLAYER);
    try {
      if (setLooping)
        sample.loop(sampleFileName);
      else
        sample.load(sampleFileName);
    }
    catch (Exception e) {
      throw new RiTaException("Unable to load sample: "
        + sampleFileName + " from " + RiTa.cwd());
    }    
    return sample;
  }

  public static RiSample loopSample(PApplet p, String sampleFileName) {
    return loadSample(p, sampleFileName, true);
  }
  

  public static RiSample playSample(PApplet p, String sampleFileName) {
    RiSample rs = loadSample(p, sampleFileName, false);
    rs.play();
    return rs;
  }
    
*/  
  public static float elapsed(long start) {
    return ((System.currentTimeMillis()-start)/1000f);
  } 
  
  private static String getHtmlResponse(HttpURLConnection conn) throws IOException
  {
    String line = "";
    StringBuilder buffer = new StringBuilder();
    BufferedReader reader = new BufferedReader
      (new InputStreamReader(conn.getInputStream()));
    while ((line = reader.readLine()) != null)
      buffer.append(line + "\n");
    reader.close();
    return buffer.toString();
  }
  
  public static boolean isClosedClass(String word)
  {
    for (int i = 0; i < CLOSED_CLASS_WORDS.length; i++)
      if (word.equalsIgnoreCase(CLOSED_CLASS_WORDS[i]))
        return true;
    return false;    
  }

  /*
   * Accepts any type of Objects in a List and tried to do the 'right' thing
   */
  public static String join(Object[] input, String delim)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length; i++) {
      if (input[i] instanceof RiText)
        sb.append(((RiText) input[i]).text());
      else
        sb.append(input[i]);
      if (i < input.length-1) 
        sb.append(delim);
    }
    return sb.toString();
  }

  /*
   * Accepts any type of Objects in a List and tried to do the 'right' thing
   */
  public static String join(List input, String delim)
  {    
    StringBuilder sb = new StringBuilder();
    if (input != null) {
      for (Iterator i = input.iterator(); i.hasNext();) {
        Object next = i.next();
        if (next instanceof RiText)
          sb.append(((RiText)next).text());
        else
          sb.append(next);
        if (i.hasNext())
          sb.append(delim);
      }
    }
    return sb.toString();
  }
  
  public static String join(Object[] input)
  {
    return join(input, SP);
  }
  
  public static String join(List input)
  {
    return join(input, SP);
  }

}// end
