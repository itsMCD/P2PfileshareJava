
package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * client
 */
public class Client implements Runnable {
  public static final boolean DEBUG = true;
  public static final int PACKAGESIZE = 256;
  public static final int PORT = 6968;

  private ConcurrentLinkedQueue<DatagramPacket> buffer = new ConcurrentLinkedQueue<DatagramPacket>();
  private LinkedList<DatagramPacket> sentPackets = new LinkedList<DatagramPacket>();
  DatagramSocket socket;
  public boolean initFlag = false;
  public boolean EOT = false;

  public Client(String file) {
    if (DEBUG)
      System.out.println(Thread.currentThread().getName() + "\t| client started");
    fileReader fr = new fileReader(file, this);
    Thread filereaderThread = new Thread(fr);
    filereaderThread.start();
    try {
      socket = new DatagramSocket(Client.PORT);
    } catch (SocketException e) {
      System.out.println("Failed to make a connection");
    }
    initFlag = true;
  }

  public Client(String file, String IP) {
    if (DEBUG)
      System.out.println(Thread.currentThread().getName() + "\t| client started");
    fileReader fr = new fileReader(file, this);
    Thread filereaderThread = new Thread(fr);
    filereaderThread.start();
    try {
      socket = new DatagramSocket(Client.PORT, InetAddress.getByName(IP));
      socket.connect(InetAddress.getByName(IP), 6969);
      if (DEBUG) System.out.println(Thread.currentThread().getName() + "| The connection was made");
    } catch (SocketException e) {
      System.out.println("Failed to make a connection");
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    initFlag = true;
  }

  public void addToQueue(DatagramPacket packet) {
    if (DEBUG)
      System.out.println(Thread.currentThread().getName() + "| added to queue");
    
    buffer.add(packet);
  }

  @Override
  public void run() {
    if (DEBUG)
      System.out.println(Thread.currentThread().getName() + "| Run method of client started");
    while (!buffer.isEmpty() || !EOT) {
      try {
        DatagramPacket temp = buffer.peek();
        if (temp != null) {
          socket.send(temp);
          sentPackets.add(buffer.remove());
        }

      } catch (IOException e) {

      }

    }
    socket.close();
  }

  public static void main(String[] args) throws InterruptedException {
    Client cl = new Client("test1.png", "127.0.0.1");
    //Client cl = new Client("in.txt", "127.0.0.1");
    while (!cl.initFlag) {
      Thread.sleep(50);
    }
    Thread clThread = new Thread(cl);
    clThread.start();
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

  AtomicInteger loc = new AtomicInteger(0);
  byte counter = 0;
  Client parent;
  short multiplier = -1;

  public fileReader(String filePath, Client parent) {
    this.parent = parent;
    this.filename = filePath;
    this.getTextFile();
    if (DEBUG)
      System.out.println(Thread.currentThread().getName() + "\t| Filereader init");
    File file = new File(filePath);
    try {
      in = new FileInputStream(file);

    } catch (FileNotFoundException e) {
      System.out.println("The file you want to send is not available");
    }

  }

  private byte[] getBytes() {
    if (DEBUG)
      System.out.println(Thread.currentThread().getName() + "| getbytes called");
    byte[] out = new byte[Client.PACKAGESIZE];
    try {
      if (loc.get() != -1) {
        int tmp = in.read(out);
        loc.getAndSet(tmp);
        return out;
      }
    } catch (IOException e) {
      System.out.println("The thing broke");
    }
    return out;
  }

  private void getTextFile() {
    FileInputStream getPacket = null;
    byte[] all = null;
    try {
      getPacket = new FileInputStream(new File(filename));
    } catch (FileNotFoundException e) {
      
    }
    try {
      all = getPacket.readAllBytes();
    } catch (IOException e) {
      
    }
    try {
      FileOutputStream out = new FileOutputStream(new File("TextOut.txt"));
      out.write(all);
      out.close();
    } catch (IOException e) {
      
    }
      
    }

  

  @Override
  public void run() {
    short prevloc = 0;
    if (DEBUG)
      System.out.println(Thread.currentThread().getName() + "| filereader run init");
    while (loc.get() != -1) {
      prevloc = (short) loc.get();
      if (counter == 0)
        multiplier++;
      byte[] information = getBytes();
      byte[] addCounter = new byte[information.length + 3];
      addCounter[0] = counter;
      addCounter[1] = (byte) (((short)loc.get()) >> 8);
      addCounter[2] = (byte) loc.get();
      counter++;
      for (int i = 3; i < addCounter.length; i++) {
        addCounter[i] = information[i - 3];
      }
        parent.addToQueue(new DatagramPacket(addCounter, addCounter.length));
        System.out.println("filename");
    }
    parent.EOT = true;
    // byte[] fin = new byte[Client.PACKAGESIZE+1];
    // fin[0] = counter;
    // fin[1] = (byte)prevloc;
    // parent.addToQueue(new DatagramPacket(fin, fin.length));
    // parent.EOT = true;
    // System.out.println("EOT " + counter);

  }

}