
package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * client
 */
public class Client implements Runnable {
  public static final boolean DEBUG = true;
  public static final int PACKAGESIZE = 16;
  public static final int PORT = 6969;

  public Client(String file) {
    if (DEBUG) System.out.println(Thread.currentThread().getName() + "\t| client started");
    fileReader fr = new fileReader(file, this);
    Thread filereaderThread = new Thread(fr);
    filereaderThread.start();
  }

  public void addToQueue(DatagramPacket packet) {
    byte[] pack = packet.getData();
    System.out.println(pack[0]);
    for (byte b : pack) {
      System.out.println((char)b);
    }
    //TODO: Add to the queue to send
  }

  @Override
  public void run() {

  }
  public static void main(String[] args) {
    Client cl = new Client("in.txt");
  }
}

/**
 * Innerclient
 */
class fileReader implements Runnable {
  public static final boolean DEBUG = true;
  FileInputStream in;
  String filename;
  byte[] byteFile;
  DatagramSocket socket;
  int loc = 0;
  byte counter = 0;
  Client parent;
  short multiplier = -1;

  public fileReader(String filePath, Client parent) {
    this.parent = parent;
    this.filename = filePath;
    if(DEBUG) System.out.println(Thread.currentThread().getName() + "\t| Filereader init");
    File file = new File(filePath);
    try {
      in = new FileInputStream(file);

    } catch (FileNotFoundException e) {
      System.out.println("The file you want to send is not available");
    }
    try {
      socket = new DatagramSocket(Client.PORT);
    } catch (SocketException e) {
      System.out.println("Failed to make a connection");
    }
  }

  private byte[] getBytes () {
    if (DEBUG) System.out.println(Thread.currentThread().getName() + "| getbytes called");
    byte[] out = new byte[Client.PACKAGESIZE];
    try { 
      if( true) {
        loc = in.read(out);
        if (DEBUG) System.out.println(Thread.currentThread().getName() + "| the current package was at location " + loc);
        return out;
      }
    } catch (IOException e) {
      System.out.println("The thing broke");
    }
    return out;
  }
  private byte[] getBytes (int locatoin) {
    FileInputStream getPacket;
    byte[] out = new byte[Client.PACKAGESIZE];
    try {
      getPacket = new FileInputStream(new File(filename));
      getPacket.skip(locatoin*Client.PACKAGESIZE);
      getPacket.read(out);
    } catch (IOException e) {
      
    }
    
    
    
    
    return out;
    
  }

  @Override
  public void run() {
    if (DEBUG) System.out.println(Thread.currentThread().getName() + "| filereader run init");
    int test = 0;
    while (test < 9) { //TODO: the thing needs to be stopable
      if (counter == 0) multiplier++;
      byte[] information = getBytes();
      byte[] addCounter = new byte[information.length + 1];
      addCounter[0] = counter;
      counter++;
      for (int i = 1; i < addCounter.length; i++) {
        addCounter[i] = information[i-1];
      }
      parent.addToQueue(new DatagramPacket(addCounter, addCounter.length));
      test++;
    }
    //TEST IF WE CAN GET THE THING\
    System.out.println("aassa");
    parent.addToQueue(new DatagramPacket(getBytes(5), Client.PACKAGESIZE));

  }

}