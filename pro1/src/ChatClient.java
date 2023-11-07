import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient {

	
	public static void main(String[] args) throws UnknownHostException, IOException
	{
		
		
			try {	
				String ServerIP = "localhost";
				if(args.length > 0)
					ServerIP = args[0];
				Socket socket = new Socket(ServerIP, 9999);		//소켓 객체 생성
				System.out.println("서버와 연결이 되었습니다....");
				
				
				//서버에서 보내는 메시지를 사용자의 콘솔에 출력하는 쓰레드.
				Thread receiver = new Receiver(socket);
				receiver.start();
	
				//사용자로부터 얻은 문자열을 서버로 전송해주는 역할을 하는 쓰레드
				Thread sender = new Sender(socket);
				sender.start();
				
			}catch(Exception e) {
				System.out.println("예외[MultiClient class]:"+e);
			}
	//	}

			
	}
	

	

}