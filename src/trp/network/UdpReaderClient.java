package trp.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import trp.layout.PageManager;
import trp.util.Readers;

public class UdpReaderClient implements ReaderClient
{
  private static String appId;
  
  DatagramSocket clientSocket;
  InetAddress hostIpAddress;
  byte[] sendData = new byte[1024];  
  String id, lastAttemptedMsg;
  int port;
  
  public UdpReaderClient(String hostStr, int port, String readerId) throws SocketException, UnknownHostException {
    this.id = readerId;
    PageManager pm = PageManager.getInstance();
    if (appId == null) 
      this.setAppId(pm != null ? pm.getApplicationId() : "NoPageManager");
    reopen(hostStr, port);
    //Readers.info("[INFO] Created "+getClass().getName()+" to "+hostStr+":"+port);
  }

  private void reopen(String host, int port) throws SocketException, UnknownHostException
  {
    this.port = port;
    this.clientSocket = new DatagramSocket();
    try
    {
      hostIpAddress = InetAddress.getByName(host);
    }
    catch (Exception e)
    {
      System.out.println("[WARN] Unable to get hostIp");
    }
    //Readers.info("hostIpAddress: "+hostIpAddress);
  }
  
  public boolean sendData(String data)
  {
    try
    {
      data = URLEncoder.encode(data, ENCODING);
      
      lastAttemptedMsg = data;
      
      //Readers.info("UDP-SEND: '"+data+"'"); // DEBUG
      
      sendData = data.getBytes();
      
      if (hostIpAddress != null) {
        DatagramPacket sendPacket = new DatagramPacket
          (sendData, sendData.length, hostIpAddress, port);
      
        if (clientSocket !=  null)
          clientSocket.send(sendPacket);
      }
      return true;
    }
    catch (Exception e)
    {
      Readers.warn("[ERROR] Unable to send msg: "+data);
      return false;
    }
  }
  
  public void close() {
    clientSocket.close();
  }

  public String getHostString()
  {
    return hostIpAddress+":"+port;
  }

  public String getLastAttemptedUrl()
  {
    return "Udp://"+hostIpAddress+"?"+lastAttemptedMsg;
  }
  
  public boolean updateServer(String gid, String word, int x, int y)
  {
    String paramStr = "cmd=" + INSERT + "&appId="+getAppId()+"&rid=" + id + "&gid=" + // use constants
      gid + "&data=" + word + "&gx=" + x + "&gy=" + y + "&tms=" + ms();

    //if (paramStr.contains("PhrasingReader"))System.out.println("UdpReaderClient.updateServer(): "+paramStr);
    
    return sendData(paramStr);
  }
  
  private long ms()
  {
    return System.currentTimeMillis();
  }
  
  public static void setAppId(String appId)
  {
    UdpReaderClient.appId = appId;
  }

  public static String getAppId()
  {
    return appId;
  }

  public static void main(String args[]) throws Exception
  {
    System.out.println(System.getProperties());
    if (1==1) return;
    UdpReaderClient x = new UdpReaderClient("localhost", 8001, "1");
    for (int i = 0; i < 10; i++) {
      Readers.info("sending msg #"+i);
      x.updateServer("2341234"+i, "word"+i, i, i+1);
    }
    x.close();
  }

}