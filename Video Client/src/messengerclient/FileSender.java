/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package messengerclient;

import java.net.*;
import java.io.*;

public class FileSender implements Runnable
{
    String fileName;
  FileSender(String file)
  {
      fileName=file;
  }

  public void run()
  {
        try {
            ServerSocket servsock = new ServerSocket(13267);
            System.out.println("Waiting...");
            Socket sock = servsock.accept();
            System.out.println("Accepted connection : " + sock);
            // 파일보내기
            File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            OutputStream os = sock.getOutputStream();
            System.out.println("Sending...");
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
            sock.close();
        } 
        catch (IOException ex)
        {
        }
      }
}
