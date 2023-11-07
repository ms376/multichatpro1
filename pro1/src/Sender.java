import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Sender extends Thread
{
	Socket socket;
	PrintWriter out = null;
	String name;
	
	//생성자
	public Sender(Socket socket)
	{
		this.socket = socket;
		try {
			out = new PrintWriter(this.socket.getOutputStream(), true);
			
		}catch(Exception e) {
			System.out.println("예외S3: " + e);
		}
	}
	
	//run()메소드 재정의
	@Override
	public void run() {
		
		Scanner sc = new Scanner(System.in);
		
		try {
			//입력한 사용자이름을 서버에 보내준다
			//out.println(name);
			
			while(out != null)
			{
				try {
					String s = sc.nextLine();
					out.println(s);
					
					if(s.equals("q") || s.equals("Q")){
						break;
					}
				}catch(Exception e) {
					System.out.println("예외S1:" + e);
				}
			}
			
			out.close();
			
			socket.close();
		}catch(Exception e) {
			System.out.println("예외S2: " + e);
		}
		sc.close();
	}

}
/*
메인에서 보내기용 받기용 쓰레드 분리
이번 예제에서 보내기용 쓰레드 분리 추가
*/