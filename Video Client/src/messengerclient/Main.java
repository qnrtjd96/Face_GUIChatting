package messengerclient;

public class Main {
	
	//메인메소드 실행
    public static void main(String[] args)
    {
      	//해당 클래스 실행
        ClientManager clientManager=new ClientManager();
        LoginFrame loginFrame=new LoginFrame(clientManager);
        loginFrame.setLocation(400,100); //위치
        loginFrame.setVisible(true); //보여주기 여부
    }

}
