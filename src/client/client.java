
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
public class client implements Runnable {
  public static final int PACKAGESIZE = 256;
  public static final int PORT = 6969;

  public client() {

  }

  @Override
  public void run() {

  }
}

/**
 * Innerclient
 */
class fileReader implements Runnable {
  FileInputStream in;
  byte[] byteFile;
  DatagramSocket socket;
  DatagramPacket packet;
  int loc = 0;

  public fileReader(String filePath) {
    File file = new File(filePath);
    try {
      in = new FileInputStream(file);

    } catch (FileNotFoundException e) {
      System.out.println("The file you want to send is not available");
    }
    try {
      byteFile = in.readAllBytes();
    } catch (IOException e) {
      System.out.println("The filereader was unsuccessfull");
    }
    try {
      socket = new DatagramSocket(client.PORT);
    } catch (SocketException e) {
      System.out.println("Failed to make a connection");
    }
  }

  private byte[] getBytes () {
    byte[] out = new byte[client.PACKAGESIZE];
    try {
      if((loc = in.read(out)) != -1) {
        return out;
      }
    } catch (IOException e) {
      System.out.println("The thing broke");
    }
    return null;
  }

  @Override
  public void run() {

  }

}