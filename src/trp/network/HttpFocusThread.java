package trp.network;

import java.net.URL;

import rita.RiTa;
import trp.layout.PageManager;
import trp.reader.MachineReader;
import trp.util.ReaderConstants;
import trp.util.Readers;

public class HttpFocusThread extends Thread implements ReaderConstants
{
  static final boolean DBUG = false;
  
  protected PageManager pm;
  protected int sleepInterval;
  protected long startTime;
  
  public HttpFocusThread(PageManager pageManager)
  {
    this.pm = pageManager;
    this.sleepInterval = FOCUS_CHECK_INTERVAL;

  }

  public void run()
  {
    Readers.info("HttpFocusThread runnning...");
    
    this.startTime = System.currentTimeMillis();
    
    while (true) {
      
      // check for enabled server here : 
      if (Readers.serverEnabled())
        getServerFocus();
      
      try
      {
        //System.out.println("SLEEPING at "+RiTa.elapsedStr());
        sleep(sleepInterval);
      }
      catch (InterruptedException e) {}
    }
  }
  
  //
  // *** NOTE: THIS MECH. MAY NOT WORK WITH MULTIPLE READERS OF THE SAME TYPE ***
  // 
  String sendData(String name) // send null for getFocus()
  {
    //System.out.println("PageManager.doFocusCheck() "+RiTa.elapsedStr());
    String response = null;
    try
    { 
      String theUrl = "http://"+MachineReader.SERVER_HOST+":"+HTTP_PORT+"/readers?appId="+pm.getApplicationId();
      String append = "&cmd=getFocus";
      if (name != null) {
        append = "&cmd=setFocus&name="+name;
        //Readers.info("sendData() -> "+theUrl+append);
      }
      
      URL url = new URL(theUrl+append);
      return Readers.httpGet(url);
    }
    catch (Exception e)
    {
      this.sleepInterval = FOCUS_CHECK_INTERVAL * 10;
      String msg = "Focus error: "+e.getMessage()+" at "+RiTa.elapsed(startTime)
        + "ms, trying again in "+sleepInterval;
      Readers.warn(msg);
      return msg;
    }
    
  }
  
  public void setServerFocus(MachineReader reader)
  { 
    String response = sendData(reader.getName());
    if (!response.trim().equals(JSON_OK))
      Readers.warn("Unexpected message from focus-server: "+response);
  }
  
  public void getServerFocus()
  { 
    //Readers.info(" got: '"+response.trim()+"'");
    
    String response = sendData(null);
    
    int focusedReaderId = -1;
    try
    {
      String rid = response.split(":")[1].replaceAll("[{}\\n]", "");
      focusedReaderId = Integer.parseInt(rid);
      //Readers.info("FocusThread: focusedReaderId: '"+focusedReaderId+"'");
    }
    catch (NumberFormatException e)
    {
      // catch and ignore, handle later...
    }
    
    if (focusedReaderId >= 0) {
      MachineReader mr = MachineReader.getReaderById(focusedReaderId);
      
      if (mr == null) {
        if (!MachineReader.PRODUCTION_MODE && DBUG) 
          Readers.warn("Focus-check: No reader with id# ("+focusedReaderId+") found...");
        return;
      }

      
      if (pm.getFocusedReader() != mr) {
        //Readers.info("HttpFocusThread.focusSwitch: "+mr.getName());
        pm.onUpdateFocusedReader(mr);
        this.sleepInterval = FOCUS_CHECK_INTERVAL; // reset interval
      }
    }    
  }

}
