/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package MessengSrserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static MessengSrserver.ServerConstant.*;

public class ServerManager implements MessageListener
{
    ExecutorService serverExeCutor;
    ServerSocket server;
    Socket clientSocket;
    Clients[] client;
    int clientNumber=0;
    static String[] clientTracker;
    String users="";

    public ServerManager()
    {
        client=new Clients[CLIENT_NUMBER];
        clientTracker=new String [CLIENT_NUMBER];
        serverExeCutor=Executors.newCachedThreadPool();
    }

    public void startServer(ServerStatusListener statusListener,ClientListener clientListener)
    {
        try
        {
            statusListener.status("server start : "+SERVER_PORT);

            server=new ServerSocket(SERVER_PORT, BACKLOG); //소켓열어주기  BACKLOG=100 MessageListener에 100으로 저장해놈
            serverExeCutor.execute(new ConnectionController(statusListener,clientListener)); //conectionController에 연결
        }
        catch(IOException ioe)
        {
            statusListener.status("IOException occured When Server start");
        }
    }
    
    //서버 정지
    public void stopServer(ServerStatusListener statusListener)
    {
        try 
        {
            server.close();
            statusListener.status("server stop");
        }
        catch(SocketException ex)
        {
            //ex.printStackTrace();
            statusListener.status("SocketException Occured When Server is going to stoped");
        }
        catch (IOException ioe)
        {
            //ioe.printStackTrace();
            statusListener.status("IOException Occured When Server is going to stoped");
        }
    }

    //서버연결
    public void controllConnection(ServerStatusListener statusListener,ClientListener clientListener)
    {
        while(clientNumber<CLIENT_NUMBER) // 최대 인원수 100명, CLIENT_NUMBER=100
        { 
            try
            {
                clientSocket= server.accept();
                client[clientNumber]=new Clients(clientListener,clientSocket,this,clientNumber);
                serverExeCutor.execute(client[clientNumber]);
                clientNumber++;
                //System.out.println(clientNumber);
            }
            catch(SocketException ex)
            {
                ex.printStackTrace();
                break;
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
                statusListener.status("Some Problem Occured When connection received");
                break;
            }
        }
    }

    //UI에 띄어주기
    public void sendInfo(String message)
    {
        StringTokenizer tokens=new StringTokenizer(message);
        String to=tokens.nextToken();

        for(int i=0;i<clientNumber;i++)
        {
            if(clientTracker[i].equalsIgnoreCase(to))
            {
                try
                {
                    client[i].output.writeObject(message);
                    client[i].output.flush();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    //파일보내기
    public void sendFile(String sendTo,int file)
    {
        for(int i=0;i<clientNumber;i++)
        {
            if(clientTracker[i].equalsIgnoreCase(sendTo))
            {
                try
                {
                    client[i].output.writeInt(file);
                    client[i].output.flush();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    
    // send all;
    public void sendNameToAll(String message)
    {
        for(int i=0;i<clientNumber;i++)
        {
            try
            {
                System.out.println("서버에서 매세지보내기   "+ message);
                client[i].output.writeObject(message);
                client[i].output.flush();
            }
            catch (IOException ex)
            {}
        }
    }
    
    //연결 컨트롤러
    class ConnectionController implements Runnable
    {
        ServerStatusListener statusListener;
        ClientListener clientListener;

        ConnectionController(ServerStatusListener getStatusListener,ClientListener getClientListener)
        {
            statusListener=getStatusListener;
            clientListener=getClientListener;
        }

        public void run()
        {
            controllConnection(statusListener,clientListener);
        }
    }
}
