package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Server implements Runnable {
  
  public static final int PORT = 6969;
  public static final int PACKAGESIZE = 256;

  public boolean initFlag = false;
  private DatagramSocket socket;
  private File file;
  private FileOutputStream out;
  private byte[] buffer = new byte[PACKAGESIZE+3];//this needs to match up with with sender
  public boolean eotFlag = false;
  public Server() {
    try {
      socket = new DatagramSocket(Server.PORT, InetAddress.getByName("127.0.0.1"));
    } catch (SocketException e) {
      
    } catch (UnknownHostException e) {
      
    }
    //file = new File("output.png"); //XXX
    file = new File("out.txt");
    try {
      out = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      
    }
    initFlag = true;
  }
  public static void main(String[] args) {
    Server srvr = new Server();
    Thread sThread = new Thread(srvr);
    while (!srvr.initFlag) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        
      }
    }
    
    sThread.start();
  }

  private void writeToFile(byte[] bytes) {
    byte[] data = new byte[bytes.length-3];
    short a = (short) ((bytes[1] << 8) | (bytes[2] & 0xff));
    //System.out.println(a + " ~~~~~~~~~~~~~~~~~");

    data = Arrays.copyOfRange(bytes, 3, a+3);
      try {
        out.write(data);
      } catch (IOException e) {
        
      }
      if (a < PACKAGESIZE) {
        eotFlag = true;
      }
    }
    
  

  @Override
  public void run() {

    while(!eotFlag) {

      DatagramPacket p = new DatagramPacket(buffer, buffer.length);
      try {
        socket.receive(p);
      } catch (IOException e) {

      }
      writeToFile(p.getData());
    }
    try {
      out.close();
    } catch (IOException e) {

    }
  }
  
}
