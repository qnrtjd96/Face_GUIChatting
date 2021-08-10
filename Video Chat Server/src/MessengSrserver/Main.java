package MessengSrserver;

public class Main
{
	//메인메소드
    public static void main(String[] args)
    {
        ServerManager serverManager=new ServerManager(); //서버매니저 메소드 실행
        ServerMonitor monitor=new ServerMonitor(serverManager); //서버모니터링 메소드 실행

        monitor.setVisible(true);
    }
}
