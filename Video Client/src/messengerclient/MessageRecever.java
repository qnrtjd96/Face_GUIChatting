/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package messengerclient;

import static messengerclient.ClientConstant.DISCONNECT_STRING;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MessageRecever implements Runnable
{
    ObjectInputStream input;
    boolean keepListening=true;
    ClientListListener clientListListener;// ClientListListener 메소드
    ClientWindowListener clientWindowListener;//ClientWindowListener 메소드
    ClientManager clientManager; //ClientManager 메소드
    Socket clientSocket;
    ExecutorService clientExecutor; //병렬작업 시 여러개의 작업을 효율적으로 처리하기 위해 제공되는 JAVA의 라이브러리
   
    //메세지 받기
    MessageRecever(Socket getClientSocket, ClientListListener getClientListListener, 
    			   ClientWindowListener getClientWindowListener, ClientManager getClientManager)
    {
        clientExecutor=Executors.newCachedThreadPool(); //쓰레드 생성
        clientManager=getClientManager;
        clientSocket=getClientSocket;
        
        try
        {
            input = new ObjectInputStream(getClientSocket.getInputStream());
        }
        catch (IOException ex)
        {}
        clientListListener=getClientListListener;
        clientWindowListener=getClientWindowListener;
    }
    
    //Runnable떄문에 도는거
    public void run()
    {
        String message,name="",ips = "";
        while(keepListening) // 위에 true로 선언해줌
        {
            try
            {
                message = (String) input.readObject(); //Object로 받아옴
                System.out.println("user is receiving "+ message);
                
                StringTokenizer tokens=new StringTokenizer(message); //StringTokenizer

                String header=tokens.nextToken(); //nextToken 다음 토큰을 반환
                if(tokens.hasMoreTokens()) //StringTokenizer 클래스 객체에서 다음에 읽어 들일 token이 있으면 true, 없으면 false를 return한다.
                    name=tokens.nextToken();
     
                if(header.equalsIgnoreCase("login")) //equalsIgnoreCase = equals랑 같음 둘이 다른점은 equal는 대소문자 비교함 / equalsIgnoreCase는 대소문자를 비교안함
                {
                    clientListListener.addToList(name);
 
                }
                else if(header.equalsIgnoreCase(DISCONNECT_STRING)) // 끊겼을때
                {
                    clientListListener.removeFromList(name);
                }
                else if(header.equalsIgnoreCase("server")) 
                {
                    clientWindowListener.closeWindow(message);
                }
                
                // Video 
                
                else if(name.equalsIgnoreCase("video") || name.equalsIgnoreCase("video1")) //비디오일때
                {
                	 VideoConference videoConference = new VideoConference(message); //비디오메소드 시작!
                	  clientExecutor.execute(videoConference);
                	 System.out.println("VIDEO CHAT Thread started :)");
                }
                
                else
                {
                    clientWindowListener.openWindow(message);
                }
            }
            catch (IOException ex)
            {
                clientListListener.removeFromList(name);
            }
            catch (ClassNotFoundException ex)
            {

            }
        }
    }

    void stopListening()
    {
        keepListening=false;
    }
}
