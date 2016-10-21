package trp.util;

public interface ReaderConstants
{

  public static String VERSION = "v41";

  String[] CLOSED_CLASS_WORDS = { ".", ",", "THE", "AND", "A", "OF", "\"", "IN", "I", ":", "YOU", "IS", "TO", "THAT", ")", "(", "IT", "FOR", "ON", "!", "HAVE", "WITH", "?", "THIS", "BE", "...", "NOT", "ARE", "AS", "WAS", "BUT", "OR",
      "FROM", "MY", "AT", "IF", "THEY", "YOUR", "ALL", "HE", "BY", "ONE", "ME", "WHAT", "SO", "CAN", "WILL", "DO", "AN", "ABOUT", "WE", "JUST", "WOULD", "THERE", "NO", "LIKE", "OUT", "HIS", "HAS", "UP", "MORE", "WHO", "WHEN", "DON'T",
      "SOME", "HAD", "THEM", "ANY", "THEIR", "IT'S", "ONLY", ";", "WHICH", "I'M", "BEEN", "OTHER", "WERE", "HOW", "THEN", "NOW", "HER", "THAN", "SHE", "WELL", "ALSO", "US", "VERY", "BECAUSE", "AM", "HERE", "COULD", "EVEN", "HIM", "INTO",
      "OUR", "MUCH", "TOO", "DID", "SHOULD", "OVER", "WANT", "THESE", "MAY", "WHERE", "MOST", "MANY", "THOSE", "DOES", "WHY", "PLEASE", "OFF", "GOING", "ITS", "I'VE", "DOWN", "THAT'S", "CAN'T", "YOU'RE", "DIDN'T", "ANOTHER", "AROUND",
      "MUST", "FEW", "DOESN'T", "EVERY", "YES", "EACH", "MAYBE", "I'LL", "AWAY", "DOING", "OH", "ELSE", "ISN'T", "HE'S", "THERE'S", "HI", "WON'T", "OK", "THEY'RE", "YEAH", "MINE", "WE'RE", "WHAT'S", "SHALL", "SHE'S", "HELLO", "OKAY",
      "HERE'S", "-", "LESS" };

  String[] STOP_WORDS = { "A", "ABOUT", "ABOVE", "ACROSS", "AFTER", "AFTERWARDS", "AGAIN", "AGAINST", "ALL", "ALMOST", "ALONE", "ALONG", "ALREADY", "ALSO", "ALTHOUGH", "ALWAYS", "AM", "AMONG", "AMONGST", "AMOUNT", "AN", "AND", "ANOTHER",
      "ANY", "ANYHOW", "ANYONE", "ANYTHING", "ANYWAY", "ANYWHERE", "ARE", "AROUND", "AS", "AT", "BACK", "BE", "BECAME", "BECAUSE", "BECOME", "BECOMES", "BECOMING", "BEEN", "BEFORE", "BEFOREHAND", "BEHIND", "BEING", "BELOW", "BESIDE",
      "BESIDES", "BETWEEN", "BEYOND", "BILL", "BOTH", "BOTTOM", "BUT", "BY", "CALL", "CAN", "CANNOT", "CANT", "CO", "COMPUTER", "CON", "COULD", "COULDN’T", "CRY", "DE", "DESCRIBE", "DETAIL", "DO", "DONE", "DOES", "DOWN", "DUE", "DURING",
      "EACH", "EG", "EIGHT", "EITHER", "ELEVEN", "ELSE", "ELSEWHERE", "EMPTY", "ENOUGH", "ETC", "EVEN", "EVER", "EVERY", "EVERYONE", "EVERYTHING", "EVERYWHERE", "EXCEPT", "FEW", "FIFTEEN", "FIFTY", "FILL", "FIND", "FIRE", "FIRST", "FIVE",
      "FOR", "FORMER", "FORMERLY", "FORTY", "FOUND", "FOUR", "FROM", "FRONT", "FULL", "FURTHER", "GET", "GIVE", "GO", "HAD", "HAS", "HASN’T", "HAVE", "HE", "HENCE", "HER", "HERE", "HEREAFTER", "HEREBY", "HEREIN", "HEREUPON", "HERS",
      "HERSELF", "HIM", "HIMSELF", "HIS", "HOW", "HOWEVER", "HUNDRED", "I", "IE", "IF", "IN", "INC", "INDEED", "INTEREST", "INTO", "IS", "IT", "ITS", "ITSELF", "KEEP", "LAST", "LATTER", "LATTERLY", "LEAST", "LESS", "LTD", "MADE", "MANY",
      "MAY", "ME", "MEANWHILE", "MIGHT", "MILL", "MINE", "MORE", "MOREOVER", "MOST", "MOSTLY", "MOVE", "MUCH", "MUST", "MY", "MYSELF", "NAME", "NAMELY", "NEITHER", "NEVER", "NEVERTHELESS", "NEXT", "NINE", "NO", "NOBODY", "NONE", "NOONE",
      "NOR", "NOT", "NOTHING", "NOW", "NOWHERE", "OF", "OFF", "OFTEN", "ON", "ONCE", "ONE", "ONLY", "ONTO", "OR", "OTHER", "OTHERS", "OTHERWISE", "OUR", "OURS", "OURSELVES", "OUT", "OVER", "OWN", "PART", "PER", "PERHAPS", "PLEASE", "PUT",
      "RATHER", "RE", "SAME", "SEE", "SEEM", "SEEMED", "SEEMING", "SEEMS", "SERIOUS", "SEVERAL", "SHE", "SHOULD", "SHOW", "SIDE", "SINCE", "SINCERE", "SIX", "SIXTY", "SO", "SOME", "SOMEHOW", "SOMEONE", "SOMETHING", "SOMETIME", "SOMETIMES",
      "SOMEWHERE", "STILL", "SUCH", "SYSTEM", "TAKE", "TEN", "THAN", "THAT", "THE", "THEIR", "THEM", "THEMSELVES", "THEN", "THENCE", "THERE", "THEREAFTER", "THEREBY", "THEREFORE", "THEREIN", "THEREUPON", "THESE", "THEY", "THICK", "THIN",
      "THIRD", "THIS", "THOSE", "THOUGH", "THREE", "THROUGH", "THROUGHOUT", "THRU", "THUS", "TO", "TOGETHER", "TOO", "TOP", "TOWARD", "TOWARDS", "TWELVE", "TWENTY", "TWO", "UN", "UNDER", "UNTIL", "UP", "UPON", "US", "VERY", "VIA", "WAS",
      "WE", "WELL", "WERE", "WHAT", "WHATEVER", "WHEN", "WHENCE", "WHENEVER", "WHERE", "WHEREAFTER", "WHEREAS", "WHEREBY", "WHEREIN", "WHEREUPON", "WHEREVER", "WHETHER", "WHICH", "WHILE", "WHITHER", "WHO", "WHOEVER", "WHOLE", "WHOM",
      "WHOSE", "WHY", "WILL", "WITH", "WITHIN", "WITHOUT", "WOULD", "YET", "YOU", "YOUR", "YOURS", "YOURSELF", "YOURSELVES" };

  // COLORS
  // M = Megan
  float[] M_LEMON = { 255, 176, 28, 255 };
  float[] M_OCEAN = { 0, 149, 255, 255 };
  float[] M_CAYENNE = { 250, 0, 7, 255 };
  float[] M_FERN = { 0, 209, 7, 255 };

  float[] M_LEMONDARK = { 215, 141, 48, 255 };
  float[] M_OCEANDARK = { 28, 118, 214, 255 };
  float[] M_CAYENNEDARK = { 199, 31, 36, 255 };
  float[] M_FERNDARK = { 63, 163, 41, 255 };

  // M = Megan
  float[] MOCHRE = { 216, 129, 0, 255 };
  float[] MBLUE = { 0, 149, 255, 255 };
  float[] MGREEN = { 0, 209, 7, 255 };
  float[] MRED = { 255, 27, 47, 255 };

  int BLACK_INT = 0, WHITE_INT = 255;
  // ---- Megan's other colors: ----
  float[] MDARKRED = { 131, 0, 18, 255 };
  float[] M_BLUE = { 0, 86, 216, 255 };
  float[] M_GREEN = Readers.unhex(0x00D107);
  float[] MBROWN = { 79, 34, 0, 255 };
  float[] MYELLOW = Readers.unhex(0xFFB01C);
  // ----
  float[] GREEN = Readers.unhex(0x06C700);
  float[] RED = Readers.unhex(0xf80300);
  float[] BLUE = Readers.unhex(0x0093FF);
  float[] CYAN = { 0, 180, 180, 255 };
  float[] YELLOW = Readers.unhex(0xFFCC00);
  float[] BLACK = { 0, 0, 0, 255 };
  float[] DGRAY = Readers.unhex(0x666666);
  float[] GRAY = Readers.unhex(0x999999);
  float[] PURPLE = Readers.unhex(0xCE5AC8);
  float[] WHITE = { 255, 255, 255, 255 };
  float[] SNOW = Readers.unhex(0xFFEEFF);
  float[] DSNOW = Readers.unhex(0x666066);
  float[] CREAM = Readers.unhex(0xFFFF99);
  float[] OATMEAL = Readers.unhex(0xFFFFEE);
  float[] DOATMEAL = Readers.unhex(0x666660);
  float[] BRIGHT_RED = { 255, 0, 0, 255 };
  float[] BRIGHT_BLUE = { 0, 0, 255, 255 };
  float[] BRIGHT_GREEN = { 0, 255, 0, 255 };
  float[] LIGHT_SALMON = Readers.unhex(0xFFc0cA);
  float[] LIGHT_PINK = Readers.unhex(0xFFB6C1);
  float[] PALE_BLUE = Readers.unhex(0x66CCFF);

  // typical reader speeds
  float FLUENT = 0.4f;
  float STEADY = 0.8f;
  float SLOW = 1.2f;
  float  SLOWER = 1.6f;
  float SLOWEST = 2.0f;
  float FAST = 0.2f;

  int FOCUS_CHECK_INTERVAL = 200;
  int CLIENT_MAX_WORD_LIST_LENGTH = 20;
  int HTTP_PORT = 8080;
  int UDP_PORT = 8091;

  // SpotLight modes
  int SPOTLIGHT_NONE = 0, SPOTLIGHT_FOCUS = 1, SPOTLIGHT_ALL = 2;
  
  String HTTP_PRE = "http://";
  // String LOCALHOST = "localhost";
  String ENCODING = "UTF-8";

  String JSON_OK = "{\"Result\":\"OK\"}";
  String CONTEXT_PATH = "/readers";
  String ROOT_DIR = ".";

  String RESPONSE_OK = "OK";

  String TIMESTAMP_MS = "tms";
  String GRID_Y = "gy";
  String GRID_X = "gx";
  String READER_ID = "rid";
  String GRID_ID = "gid";
  String DATA = "data";

  String QUERY = "query";
  String LIST_PHP = "plist";
  String DISPLAY_LIST_PHP = "dlist";
  String DISPLAY_QUERY_PHP = "dquery";
  String QUERY_PHP = "pquery";
  String INSERT = "insert";
  String SET_FOCUS = "setFocus";
  String SUBSCRIBE = "subscribe";
  String BIGRAM_UPDATE = "bigrams";

  String TRIGRAMS = "3grams";
  String BIGRAMS = "2grams";
  String UNIGRAMS = "1grams";

  String GET_PERCENT_COMPLETE = "getPercentComplete";
  String GET_PENDING_ROWS = "getPendingRows";
  String UPDATE_TRIGAMS = "update" + TRIGRAMS;

  float PAUSE_BETWEEN_NETWORK_FAILURES_SEC = 180;

  String AMP = "&", CMD = "cmd", EQUALS = "=", SLASH = "/";
  String QUESTION = "?", DOT = ".", SP = " ";

  char[] ALLOWABLE_PUNCTUATION = new char[] { '’', '-' }; // NO MDASH, BUT APOSTROPHE
  String LOG_ID = "readers-" + VERSION;

  int WAS_READ = 0, WAS_NEIGHBOR = 1, UNTOUCHED = -1;
}
