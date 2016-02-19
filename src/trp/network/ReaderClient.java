package trp.network;

import trp.util.ReaderConstants;


public interface ReaderClient extends ReaderConstants
{
  public abstract boolean updateServer(String gid, String word, int x, int y);
  
  public abstract String getHostString();

  public abstract String getLastAttemptedUrl();

}