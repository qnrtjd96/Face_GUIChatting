/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package messengerclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientManager
{
    ExecutorService clientExecutor; //병렬작업 시 여러개의 작업을 효율적으로 처리하기 위해 제공되는 JAVA의 라이브러리
    Socket clientSocket;
    boolean isConnected=false;

    ObjectInputStream input;
    ObjectOutputStream output;
    MessageRecever messageRecever;

    
    public ClientManager()
    {
        clientExecutor=Executors.newCachedThreadPool(); ///????????????
    }

    //connect메소드        인터페이스										서버주소				 포트
    public void connect(ClientStatusListener clientStatus, String sServerAddress, String sPort)
    {
        try
        {
            if(isConnected)//기본 false
                return;
            else
            {
                clientSocket=new Socket(sServerAddress, Integer.parseInt(sPort));
                clientStatus.loginStatus("연결된 서버 주소는 :"+sServerAddress+ " 입니다.");
                isConnected=true;
            }
        }
        catch (UnknownHostException ex)
        {
            clientStatus.loginStatus("No Server found");
        }
        catch (IOException ex)
        {
            clientStatus.loginStatus("No Server found");
        }
    }
    
    //접속끊김
    public void disconnect(ClientStatusListener clientStatus)
    {
        messageRecever.stopListening();
        try
        {
            clientStatus.loginStatus("You are no longer connected to Server");
            clientSocket.close();
        }
        catch (IOException ex)
        {
        }
    }
    
    //보낸 메세지
    public void sendMessage(String message)
    {
        clientExecutor.execute(new MessageSender(message));
    }

    //보낸 파일 반출?
    public void sendFile(String fileName)
    {
        clientExecutor.execute(new FileSender(fileName));
    }

    //메세지보낸메소드
    boolean flageoutput=true;
    class MessageSender implements Runnable
    {
        String message;
        public MessageSender(String getMessage)
        {
            if(flageoutput)
            {
                try
                {
                    output = new ObjectOutputStream(clientSocket.getOutputStream());
                    output.flush();
                    flageoutput=false;
                }
                catch (IOException ex){}
            }
            message=getMessage;
            System.out.println("user is sending   "+ message);
        }
        public void run()
        {
            try
            {
                output.writeObject(message);
                output.flush();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    //메세지 받기                  
    public void receiveMessage(ClientListListener getClientListListener ,ClientWindowListener getClientWindowListener)
    {
    	System.out.println("this === " + this);
        messageRecever = new MessageRecever(clientSocket, getClientListListener, getClientWindowListener, this);
        clientExecutor.execute(messageRecever);
    }
}
