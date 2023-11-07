import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

//import MultiServer.MultiServer2;


public class MultiServer
{
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	static Connection con;
	Map<String, String> listw; // c목록
	Map<String, String> inviteM; // 초대
	Map<String, PrintWriter> waitUser; // 입장전 유저
	Map<String, PrintWriter> clientMap; // 입장전 출력/hashM
	Map<String, Integer> capa; // 최대 입장수
	Map<String, Map<String, PrintWriter>> roomN; // 공개방 hashM
	Map<String, Map<String, PrintWriter>> roomP; // 비밀방
	Map<String, String> pwd; // 비밀방 pw

	public MultiServer()
	{
		inviteM = new HashMap<String, String>();
		waitUser = new HashMap<String, PrintWriter>();
		clientMap = new HashMap<String, PrintWriter>();
		roomN = new HashMap<String, Map<String, PrintWriter>>();
		capa = new HashMap<String, Integer>();
		pwd = new HashMap<String, String>();
		listw = new HashMap<String, String>();
		Collections.synchronizedMap(clientMap);

		listw.put("/list", "모든 사용자 리스트");
		listw.put("/waituser", "대기방 유저 리스트");
		listw.put("/rlist", "채팅방 리스트 출력");
		listw.put("/adword", "개인 금칙어 설정 추가");
		listw.put("/delword", "개인 설정 금칙어 삭제");
		listw.put("/agree", "채팅방 초대 수락 명령어");

		// 해쉬맵 동기화 설정.
		Collections.synchronizedMap(inviteM);
		Collections.synchronizedMap(waitUser);
		Collections.synchronizedMap(roomN);
		Collections.synchronizedMap(clientMap);
		Collections.synchronizedMap(capa);
		Collections.synchronizedMap(pwd);

	}

	public void init()
	{

		try
		{
			serverSocket = new ServerSocket(9999);
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // 서버 부팅시간
			System.out.println(formatter.format(date)+" 서버 부팅");
			while (true)
			{
				socket = serverSocket.accept();

				Thread msr = new MultiServerR(socket);
				msr.start();
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (serverSocket != null)
				{
					serverSocket.close();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void waitU(PrintWriter out) // 입장전 유저 목록
	{
		Iterator<String> it = waitUser.keySet().iterator();
		String msg = "대기중인 유저L [";
		while (it.hasNext())
		{
			msg += (String) it.next() + ",";
		}
		msg = msg.substring(0, msg.length() - 1) + "]";

		try
		{
			out.println(msg); // 목록
		} catch (Exception e)
		{
		}
	}

	public void list(PrintWriter out) // userL
	{
		Iterator<String> it = clientMap.keySet().iterator(); // 순차적 메세지 출력
		String msg = "사용자 L [";
		while (it.hasNext())
		{
			msg += (String) it.next() + ",";
		}
		msg = msg.substring(0, msg.length() - 1) + "]";

		try
		{
			out.println(msg); // 리스트 출력
		} catch (Exception e)
		{
		}
	}

	public void roomList(PrintWriter out) // 대화방L
	
	{
		Iterator<String> it = roomN.keySet().iterator();
		String msg = "(대화방 목록) \n[";
		while (it.hasNext())
		{
			msg += (String) it.next() + ",";
		}
		msg = msg.substring(0, msg.length() - 1) + "]";

		try
		{
			out.println(msg); // L
		} catch (Exception e)
		{
		}
	}

	public void userR(String title, PrintWriter out) // 대화방 이용 U
	{
		Iterator<String> it = roomN.get(title).keySet().iterator();
		String msg = title + "방 유저 L [";
		while (it.hasNext())
		{
			msg += (String) it.next() + ",";
		}
		msg = msg.substring(0, msg.length() - 1) + "]";

		try
		{
			out.println(msg); // L
		} catch (Exception e)
		{
		}
	}

	public void sendAllMsg(String msg, String name) // 전체(클라) 메세지
	{

		PreparedStatement pstmt2 = null;
		String sql = null;
		ResultSet rs = null;
		String key = null;
		String word = null;
		String query = null;
		int chk = 0;
		int id2 = 0;
		int id1 = 0;

		Iterator<String> it = clientMap.keySet().iterator(); // 순차적 메세지 출력

		while (it.hasNext())
		{
			chk = 0;
			id2 = 0;
			try
			{
				key = it.next();
				PrintWriter it_out = (PrintWriter) clientMap.get(key);
				sql = "select * from blocked_users";
				pstmt2 = con.prepareStatement(sql);
				rs = pstmt2.executeQuery();
				while (rs.next())
				{
					word = rs.getString(1);
					if (msg.contains(word))
					{
						chk = 1;
						for (int i = 0; i < msg.length(); i++)
						{
							it_out.print("*");
						}
						it_out.println();
						break;
					}

				}

				sql = "select count(*) from $tablename";
				query = sql.replace("$tableName", key);
				pstmt2 = con.prepareStatement(query);
				rs = pstmt2.executeQuery();
				while (rs.next())
				{
					id1 = Integer.parseInt(rs.getString(1));
				}

				if (id1 > 0)
				{
					sql = "select * from $tablename";
					query = sql.replace("$tableName", key);
					pstmt2 = con.prepareStatement(query);
					rs = pstmt2.executeQuery();

					while (rs.next())
					{
						word = rs.getString(1);
						if (msg.equals(word))
						{
							id2 = 1;
							for (int i = 0; i < msg.length(); i++)
							{
								it_out.print("*");
							}
							it_out.println();
						}
					}

					id1 = 0;
				}
				if (id2 == 0)
				{
					if (name.equals(""))
						it_out.println(msg); // 받은 메세지 각 방에 출력
					else
						it_out.println(name + " > " + msg);
				}
			} catch (Exception e)
			{
				System.out.println("예외:" + e);
			}

		}
	}

	public void sendMsg(String msg, String name, String i) //대화방 메세지 
	{
		Iterator<String> it = roomN.get(i).keySet().iterator();
		String sql = null;
		ResultSet rs = null;
		String word = null;
		String query = "";
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		String next = null;
		int chk = 0;
		int id2 = 0;
		int id1 = 0;
		while (it.hasNext())
		{
			chk = 0;
			id2 = 0;
			try
			{
				next = it.next();
				if (!next.equals(name))
				{
					PrintWriter it_out = (PrintWriter) roomN.get(i).get(next);
					sql = "select * from BlockWord";
					pstmt2 = con.prepareStatement(sql);
					rs = pstmt2.executeQuery();
					while (rs.next())
					{
						word = rs.getString(1);
						if (msg.equals(word))
						{
							chk = 1;
							for (int j = 0; j < word.length(); j++)
							{
								it_out.print("*");
							}
							it_out.println();
							break;
						}
					}
					if (chk == 1)
						continue;
					sql = "select * from " + next;
					pstmt2 = con.prepareStatement(sql);
					rs = pstmt2.executeQuery();
					while (rs.next())
					{
						word = rs.getString(1);
						if (msg.equals(word))
						{
							id2 = 1;
							for (int j = 0; j < msg.length(); j++)
							{
								it_out.print("*");
							}
							it_out.println();
							break;
						}
					}
					if (id2 == 1)
						continue;
					if (name.equals(""))
						it_out.println(msg);
					else
						it_out.println(name + " > " + msg);
				}
			} catch (Exception e)
			{
				System.out.println("예외:" + e);
			}
		}
	}
	
	public void adlogin(BufferedReader in, PrintWriter out) throws IOException, SQLException // administrator
	{

		String menu = "";
		String sql = null;
		PreparedStatement pstmt4 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		String id = null;
		String word = null;

		while (true)
		{
			out.println("관리자 메뉴");
			out.println("1) Black_user List");
			out.println("2) Black_user Add");
			out.println("3) Black_user Clear");
			out.println("4) 방폭ㄱㄱ");
			out.println("5) BlockWord List");
			out.println("6) BlockWord Add");
			out.println("7) BlockWord Delete");
			out.println("8) 나가기");
			out.println();
			menu = in.readLine();
			if (menu.equals("1")) // Black_user List
			{
				sql = "select id from chatuser where utype = ?";
				pstmt2 = con.prepareStatement(sql);
				pstmt2.setString(1, "1");
				rs = pstmt2.executeQuery();
				out.println("====Black_user List====");
				while (rs.next())
				{
					word = rs.getString(1);
					out.println(word);
				}
				out.println();
			}
			else if (menu.equals("2")) // Black_user Add
			{
				sql = "select id from chatuser";
				pstmt2 = con.prepareStatement(sql);
				rs = pstmt2.executeQuery();
				out.println("====User_Id_List====");
				while (rs.next())
				{
					word = rs.getString(1);
					out.println(word);
				}
				out.println("누구를 블랙시킬까요?");
				id = in.readLine();

				sql = "update chatuser set utype = '1' where id = ?";
				pstmt4 = con.prepareStatement(sql);
				pstmt4.setString(1, id);
				rs = pstmt4.executeQuery();
				out.println("블랙리스트 등록이 완료되었습니다.");
				out.println();
			}
			else if (menu.equals("3")) // Black_user Clear
			{
				out.println("누구를 블랙리스트 해제 할까요?.");
				id = in.readLine();
				sql = "update chatuser set utype = '0' where id = ?";
				pstmt4 = con.prepareStatement(sql);
				pstmt4.setString(1, id);
				rs = pstmt4.executeQuery();
				out.println(id+"(을)를 블랙리스트에서 해제 했습니다.");
				out.println();
			}
			else if (menu.equals("4")) // RoomBoom
			{
				roomList(out);
				out.println("방폭 시키고싶은 방이름을 입력해주세요.");
				id = in.readLine();
				roomN.remove(id);
				out.println(id + "방이 폭파되었습니다.");
				out.println();
			}
			else if (menu.equals("5")) // Black_word List
			{
				out.println("====BlockWord_List====");
				sql = "select * from BlockWord";
				pstmt2 = con.prepareStatement(sql);
				rs = pstmt2.executeQuery();
				while (rs.next())
				{
					out.println("> " + rs.getString(1));
				}
				out.println();
			}
			else if (menu.equals("6")) // Black_word Add
			{
				out.println("====BlockWord_Add====");
				out.println("BlockWord : ");
				id = in.readLine();
				sql = "insert into BlockWord values (?)";
				pstmt4 = con.prepareStatement(sql);
				pstmt4.setString(1, id);
				rs = pstmt4.executeQuery();
				out.println("\'" + id + "\'가 금칙어로 추가되었습니다. ");
				out.println();
			}
			else if (menu.equals("7")) // BlockWord_Delete
			{
				out.println("BlockWord Delete");
				out.println("BlockWord_L : ");
				id = in.readLine();
				sql = "delete from blockword where blockword = ?";
				pstmt2 = con.prepareStatement(sql);
				pstmt2.setString(1, id);
				rs = pstmt2.executeQuery();
				out.println("금칙어를 삭제했습니다.");
				out.println();
			}

			else if (menu.equals("8")) // page down
			{
				out.println("..................");
				break;
			} else
			{
				out.println("잘못눌렀어.");
			}
		}
	}

	public void secretMsg(String msg, String name, String nameto, String i) //귓
	{

		PrintWriter p = null;
		try
		{
			p = (PrintWriter) roomN.get(i).get(nameto);
			if (name.equals(""))
				p.println(msg); // 받은메세지 방출력
			else
				p.println(name + "(귓속말) > " + msg);
		} catch (Exception e)
		{

		}

	}

	public void invite(String name, String nameto, String i) // 초대 (/inv)
	{
		PrintWriter p = null;
		inviteM.put(nameto, i);
		try
		{
			p = (PrintWriter) clientMap.get(nameto);
			p.println(name + "님께서 " + i + "방으로 초대하셨습니다.");
			p.println("ㄱㄱ? ( /ㄱㄱ )");
			p.println("ㄴㄴ? ( /거부 )");

		} catch (Exception e)
		{
		}
	}

	public void cutUser(String title, String name, PrintWriter out) // 강퇴 (/kick)
	{

		roomN.get(title).remove(name);
	}

	public static void main(String[] args) // 메인
	{

		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
			System.out.println("오라클 연결 드라이버 제대로 입력해");
		}

		try
		{
			con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", // localhost = 사용위치(현재는 내컴퓨터)
					"chatc", "123");
		} catch (SQLException sqle)
		{
			System.out.println("SQL 오류떴다. \n" + sqle.getMessage());
		}

		MultiServer ms = new MultiServer(); // 서버 inst 
		ms.init();

	}

	class MultiServerR extends Thread // 여기서부터 입장전 대기방 관련 쓰레드
	{
		String sql1 = null;
		String sql = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		PreparedStatement pstmt4 = null;
		ResultSet rs = null;

		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		String name = "";
		String title = null;

		public MultiServerR(Socket socket) // 그냥 생성자
		{
			this.socket = socket;

			try
			{
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (Exception e)
			{

			}
		}

		// 쓰레드를 사용하기 위해 run()메서드 재정의
		@Override
		public void run()
		{
			int type = 0;
			String s = "";
			String id = null; // 아이디
			String pw = null; // 비밀번호
			int th = 0;
			Iterator<String> iter = null;
			String ss = "";
			String key = null;
			String getMenu = "";

			out.println("_-_-_-_-입장전 대기방_-_-_-_-");
			out.println();

			try
			{
				String pw1 = null; // 비밀번호 비교에 사용
				int ld = 0;
				while (true)
				{

					ld = 0;
					out.println("메뉴 Set.");
					out.println("로그인 → 1  회원가입 → 2  계정삭제 → 3");
					out.println("관리자 로그인 → /adlogin");
					out.println("번호를 입력하세요. \n");
					
					try
					{
						getMenu = in.readLine();

					} catch (IOException e)
					{

					}

					if (getMenu.equals("/adlogin"))
					{
						out.println("administrator");
						out.println("Password :");
						pw = in.readLine();
						sql = "select pw from chatuser where id = ?";
						pstmt1 = con.prepareStatement(sql);
						pstmt1.setString(1, "head");
						rs = pstmt1.executeQuery();

						while (rs.next())
						{
							pw1 = rs.getString(1);
							if (pw.equals(pw1))
							{
								out.println("Admin login");
								adlogin(in, out);
							} else
							{
								out.println("pw 오입력.");
							}
							break;
						}
					} else
					{

						type = Integer.parseInt(getMenu);
						// 로그인
						if (type == 1)
						{

							int id1 = 0;
							String id11 = null;

							out.println("로그인\n");
							out.println("아이디를 입력하세요.");
							out.println("ID : ");
							try
							{
								id = in.readLine();

								if (id.equals("back"))
									continue;
								sql = "select count(*) from chatuser where id = ?";
								pstmt2 = con.prepareStatement(sql);
								pstmt2.setString(1, id);
								rs = pstmt2.executeQuery();
								while (rs.next())
								{
									id1 = Integer.parseInt(rs.getString(1));
								}
								if (id1 == 0)
								{
									out.println("로그인 정보가 없습니다.");
									continue;
								}

								sql = "select utype from chatuser where id = ?";
								pstmt1 = con.prepareStatement(sql);
								pstmt1.setString(1, id);
								rs = pstmt1.executeQuery();

								while (rs.next())
								{
									int utype = Integer.parseInt(rs.getString(1));
									if (utype == 1)
									{
										out.println("블랙리스트에 추가된 아이디입니다.");
										ld = 1;
									}
								}

							} catch (IOException e1)
							{
								e1.printStackTrace();
							}
							if (ld == 1)
								continue;

							id1 = 0;
							Iterator<String> at = clientMap.keySet().iterator();
							while (at.hasNext())
							{
								id11 = (String) at.next();
								if (id.equals(id11))
								{
									id1 = 1;
									break;
								}
							}
							if (id1 == 1)
							{
								out.println("현재 접속중인 아이디입니다..");
							} else
							{
								out.println("비밀번호를 입력하세요.");
								out.println("password : ");
								try
								{
									pw = in.readLine();
								} catch (IOException e1)
								{

								}

								try
								{
									try
									{
										sql = "select pw from chatuser where id = ?";
										pstmt1 = con.prepareStatement(sql);
										pstmt1.setString(1, id);
										rs = pstmt1.executeQuery();

										while (rs.next())
										{
											pw1 = rs.getString(1);
										}
									} catch (SQLException sqle)
									{

										System.out.println("SQL 테이블 오류");
									}

									if (pw.equals(pw1))
									{
										clientMap.put(id, out);
										waitUser.put(id, out);
										out.println("로그인 되었습니다\n.");
										Thread mst = new MultiServer3(socket, id, title); // 쓰레드 생성(대화방 입장 알림, 대화)
										th = 1;
										mst.start();
										mst.join();
										continue;
									} else
									{
										out.println("로그인 실패");
									}
								} finally
								{
									try
									{
										if (rs != null)
											rs.close();
										if (pstmt1 != null)
											pstmt1.close();
										if (pstmt2 != null)
											pstmt2.close();
									} catch (SQLException sqle)
									{
									}
								}
							}
						}
						else if (type == 2) // 회원가입
						{
							try
							{
								out.println("-회원가입");
								int id1 = 0;

								while (true)
								{

									out.println("아이디를 입력하세요.");
									out.println("ID :");
									id = in.readLine();

									try
									{
										if (id.equals("back"))
											break;
										sql = "select count(*) from chatuser where id = ?";
										pstmt2 = con.prepareStatement(sql);
										pstmt2.setString(1, id);
										rs = pstmt2.executeQuery();
										while (rs.next())
										{
											id1 = Integer.parseInt(rs.getString(1));
										}
										if (id1 == 0)
										{
											out.println("pw: ");
											pw = id = in.readLine();
											sql = "insert into chatuser(id, pw, utype) values(?, ?, 0)";
											pstmt3 = con.prepareStatement(sql);
											pstmt3.setString(1, id);
											pstmt3.setString(2, pw);
											rs = pstmt3.executeQuery();
											
											String strQuery = "create table $tableName (wordd varchar(10) )"; // 개인 blockword
											String query = strQuery.replace("$tableName", id);
											pstmt4 = con.prepareStatement(query);
											rs = pstmt4.executeQuery();

											out.println("가입 성공.");
											break;

										} else
										{
											out.println("중복된 아이디명 입니다.");
											break;
										}

									} catch (SQLException sqle)
									{
										sqle.printStackTrace();
									} finally
									{
										try
										{
											if (rs != null)
												rs.close();
											if (pstmt2 != null)
												pstmt2.close();
											if (pstmt3 != null)
												pstmt3.close();
										} catch (SQLException sqle)
										{
										}

									}
								}
							} catch (IOException e) // break;
							{
							}
						}
						else if (type == 3) // 3 - 계정 삭제
						{
							int id1 = 0;
							out.println("계정삭제");
							out.println("삭제할 계정 ID를 입력하세요.");
							out.println("ID :");
							id = in.readLine();

							if (id.equals("back"))
								continue;

							sql = "select count(*) from chatuser where id = ?";
							pstmt2 = con.prepareStatement(sql);
							pstmt2.setString(1, id);
							rs = pstmt2.executeQuery();
							while (rs.next())
							{
								id1 = Integer.parseInt(rs.getString(1));
							}
							if (id1 == 0)
							{
								out.println("로그인 정보가 없는 ID입니다.");
								continue;
							}
							out.println("삭제할 계정 password를 입력하세요.");
							out.println("password :");
							pw = in.readLine();

							sql = "select pw from chatuser where id = ?";
							pstmt1 = con.prepareStatement(sql);
							pstmt1.setString(1, id);
							rs = pstmt1.executeQuery();

							while (rs.next())
							{
								pw1 = rs.getString(1);
							}

							if (pw.equals(pw1))
							{

								out.println("계정의 모든 정보가 삭제됩니다.");
								out.println("계정을 삭제 하시겠습니까? Y / N");
								String o = in.readLine();
								if (o.equals("Y") || o.equals("y"))
								{
									sql = "delete from chatuser where id = ?";
									pstmt3 = con.prepareStatement(sql);
									pstmt3.setString(1, id);
									rs = pstmt3.executeQuery();

									sql = "drop table " + id;
									pstmt4 = con.prepareStatement(sql);

									rs = pstmt4.executeQuery();
									out.println("계정이 삭제되었습니다.");
									continue;
								}
							}
						} else
						{
							System.out.println("메뉴를 다시 선택하세요");
						}
					}
				}
			} catch (Exception e)
			{
				System.out.println("예외1:" + e);
			} finally
			{
				// 예외가 발생할때 퇴장. 해쉬맵에서 해당 데이터 제거.
				// 보통 종료하거나 나가면 java.net.SocketException: 예외발생
				if (th == 0)
				{
					clientMap.remove(name);
					sendAllMsg(name + "님이 도망치셨어요.", "");
					System.out.println("현재 접속자 수는 " + clientMap.size() + "명 입니다.");
				}

				try
				{
					serverSocket.close();
					in.close();
					out.close();
				} catch (Exception e)
				{

				}
			}
		}

	}

	class MultiServer3 extends Thread // 여기서부터 입장이후 입장방 관련 쓰레드
	{

		Socket socket;
		String title = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String id = "";
		String pw = null;

		public MultiServer3(Socket socket, String name, String title) // 생성자
		{
			this.socket = socket;
			this.id = name;
			this.title = title;
			try
			{
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (Exception e)
			{

			}
		}

		// 쓰레드를 사용하기 위해 run()메서드 재정의
		@Override
		public void run()
		{
			String sql1 = null;
			String sql = null;
			PreparedStatement pstmt1 = null;
			PreparedStatement pstmt2 = null;
			PreparedStatement pstmt3 = null;
			PreparedStatement pstmt4 = null;
			ResultSet rs = null;

			Iterator<String> iter = null;
			String ss = "";
			String menu = null;
			int count = 0;
			int th = 0;
			String type = null;

			try
			{

				System.out.println("	" + id + " 등장.	");

				System.out.println("	방 총원은 " + clientMap.size() + "명 입니다.");
				System.out.println();

				while (true)
				{

					out.println("명령어 보기 → /h");
					out.println("원하는 메뉴 번호를 입력하세요.\n" + 
					"1. 공개방 만들기\n" + 
					"2. 비밀방 만들기\n" + 
					"3. 공개방 리스트\n"+
					"4. 방 입장하기\n" + 
					"5. 로그아웃");
					menu = in.readLine();
					type = menu;

					if (menu.startsWith("/"))
					{
						if (menu.equals("/h"))
						{
							iter = listw.keySet().iterator();
							out.println("====명령어====");
							while (iter.hasNext())
							{
								String key = iter.next();
								out.println(">> " + key + " :		" + listw.get(key));
							}
							out.println(">> /ulist :		방 참여자 리스트");

						} else if (menu.equals("/ㄱㄱ"))
						{
							title = inviteM.get(id);
							try
							{

								if (id.equals(""))
									sendMsg("수락하셨습니다", id, title);
								else
									sendMsg("수락", id, title);
								Thread mst = new MultiServerT(socket, id, title); // 쓰레드 생성(대화방 입장 알림, 대화)
								mst.start();
								mst.join();
								// continue;

							} catch (Exception e)
							{

							}
						} else if (menu.equals("/거부"))
						{
							try
							{
								title = inviteM.get(id);
								inviteM.remove(id);

								if (id.equals(""))
									sendMsg("거부하셨습니다", id, title); // 받은 메세지 각 방에 출력
								else
									sendMsg("거부", id, title);

							} catch (Exception e)
							{

							}

						} else if (menu.equals("/list"))
						{
							list(out);

						}

						else if (menu.equals("/rlist")) // 방리스트
						{
							roomList(out);

						}
						else if (menu.equals("/waituser")) // 대기실 사용자 리스트
						{
							waitU(out);
						}

						else if (menu.equals("/adword")) // 금칙어 추가
						{
							out.println("====금칙어 추가====");
							out.println("금칙어 : ");
							ss = in.readLine();
							sql = "insert into $tableName values (?)";
							String query = sql.replace("$tableName", id);
							pstmt1 = con.prepareStatement(query);
							pstmt1.setString(1, ss);
							rs = pstmt1.executeQuery();
							out.println("\'" + ss + "\'가 금칙어로 추가되었습니다. ");

						}
						else if (menu.equals("/delword")) // 금칙어 삭제
						{
							sql = "select * from $tableName";
							String query = sql.replace("$tableName", id);
							pstmt2 = con.prepareStatement(query);
							rs = pstmt2.executeQuery();
							while (rs.next())
							{
								out.println("> " + rs.getString(1));
							}

							out.println("삭제할 금칙어 : ");
							ss = in.readLine();
							sql = "delete from $tableName where wordd = ?";
							String query1 = sql.replace("$tableName", id);
							pstmt3 = con.prepareStatement(query1);
							pstmt3.setString(1, ss);
							rs = pstmt3.executeQuery();
							out.println("금칙어가 삭제되었습니다.");

						}

						else if (menu.equals("/ulist"))
						{
							userR(title, out);
						}
					}
					else if (type.equals("1")) // 1번 선택시 방추가
					{

						out.println(" 공개방을 생성합니다");
						out.println("========================");
						out.println("방이름을 설정해 주세요: ");
						title = in.readLine();

						out.println("방의 정원을 설정하세요: ");
						count = Integer.parseInt(in.readLine());

						roomN.put(title, new HashMap<String, PrintWriter>(count)); // 방을 저장해둔 해시맵에 참여자 해시맵 추가
						capa.put(title, count);
						waitUser.remove(id);

						th = 1;
						Thread mst = new MultiServerT(socket, id, title); // 쓰레드 생성(대화방 입장 알림, 대화)
						mst.start();
						mst.join();

					}
					else if (type.equals("2")) // 2번 선택시 비밀방추가
					{

						out.println("  비밀방을 생성합니다");
						out.println("========================");
						out.println("방이름을 설정해 주세요: ");
						title = in.readLine();

						out.println("방의 정원을 설정하세요: ");
						count = Integer.parseInt(in.readLine());

						out.println("비밀번호를 입력하세요: ");
						pw = in.readLine();

						roomN.put(title, new HashMap<String, PrintWriter>(count));
						capa.put(title, count);
						pwd.put(title, pw);
						waitUser.remove(id);

						th = 1;
						Thread mst = new MultiServerT(socket, id, title); // 쓰레드 생성(대화방 입장 알림, 대화)
						mst.start();
						mst.join();

					}

					else if (type.equals("3")) // 3번 선택시 전체 방 리스트
					{
						roomList(out);
					}
					else if (type.equals("4")) // 4번 선택시 선택한 채팅방 입장하기
					{
						menu = null;
						out.println("입장을 원하는 방 이름을 입력해 주세요: ");
						title = in.readLine();
						out.println();

						if (!(roomN.containsKey(title))) //입력오류
						{
							out.println("존재하지 않는 방입니다.");
						}
						else if (roomN.get(title).size() == capa.get(title)) //정원초과 
						{
							out.println("정원초과로 입장하실 수 없습니다.");
						}
						else if (pwd.containsKey(title)) // 비밀방 입장
						{

							out.println("비밀번호를 입력해주세요: ");
							pw = in.readLine();

							if (pwd.get(title).equals(pw)) // 비밀번호 eq
							{
								System.out.println("\'" + title + "\' 방에 입장하셨습니다.");

								Thread mst = new MultiServerT(socket, id, title); // 쓰레드 생성(대화방 입장 알림, 대화)
								th = 1;
								waitUser.remove(id);
								mst.start();
								mst.join();
							} else // 비밀번호 err
							{
								out.println("비밀번호가 틀렸습니다");
							}
						} else // 일반방 입장
						{
							Thread mst = new MultiServerT(socket, id, title); // 쓰레드 생성(대화방 입장 알림, 대화)
							th = 1;
							waitUser.remove(id);
							mst.start();
							mst.join();
						}
					} else if (type.equals("5"))
					{
						clientMap.remove(id);
						out.println("로그아웃 되었습니다.");
						out.println();
						break;
					}

				}
			} catch (Exception e)
			{

			} finally
			{
				// 예외가 발생할때 퇴장. 해쉬맵에서 해당 데이터 제거.
				// 보통 종료하거나 나가면 java.net.SocketException: 예외발생
				if (th == 0)
				{
					roomN.get(title).remove(id);
					sendMsg(id + "님이 퇴장하셨습니다.", "", title);
					System.out.println("현재 접속자 수는 " + roomN.get(title).size() + "명 입니다.");
				}
				try
				{
				} catch (Exception e)
				{
					out.println("방입장 문제");
				}
			}
		}
	}

	class MultiServerT extends Thread // 내부클레스, 읽은 메세지를 다른소켓(클라)에 보내는 메서드
	{
		Socket socket;
		String title = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String name = "";

		public MultiServerT(Socket socket, String name, String title) // 생성자
		{
			this.socket = socket;
			this.title = title;
			this.name = name;
			try
			{
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (Exception e)
			{

			}

		}

		public void run() //오버라이딩 , 쓰레드 redefine
		{
			int th = 0;
			roomN.get(title).put(name, out);
			String s = "";
			String sss = "";

			String ss;
			String sql;
			PreparedStatement pstmt1;
			ResultSet rs;

			try // 이 클라제외 다른모든 클라들에 접속메세지 출력
			{
				sendMsg("====" + name + "님이 입장하셨습니다.====", "", title);
				
				out.println("【" + title + "】 에 입장하셨습니다. ");
				out.println("【" + title + "】 의 현재 접속자 수는 " + roomN.get(title).size()+"/"+capa.get(title)+"명");
				out.println("방나가기 → /out,/ex");
				out.println("명령어 목록 보기 → /h ");

				while (in != null) 
				{

					s = in.readLine();
					if (!roomN.keySet().contains(title)) // 방폭
					{
						waitUser.put(name, out);
						out.println("퇴장.");
						th = 1;
						break;
					}
					if (!roomN.get(title).keySet().contains(name)) //kick
					{
						waitUser.put(name, out);
						out.println("퇴장.");
						th = 1;
						break;
					} else
					{

						if (s.startsWith("/"))
						{
							String s2 = null;
							String n = null;
							String[] s1 = s.split(" ");
							if (s1.length > 1)
							{
								s2 = s1[0];
								if (s2.equals("/to")) // 귓
								{
									n = s1[1];
									out.println(n + "에게 귓속말>>");
									while (in != null)
									{
										sss = in.readLine();
										if (sss.equals("//stop"))
										{
											break;
										} else
										{
											secretMsg(sss, name, n, title);
										}
									}
									continue;
								}
								else if (s2.equals("/kick")) // 강퇴 명령어
								{
									for (int j = 1; j < s1.length; j++)
									{
										n = s1[j];
										roomN.get(title).remove(n);
									}
									continue;
								}
								else if (s2.equals("/inv")) //inv 명령어
								{
									n = s1[1];
									invite(name, n, title);
								}
							}

							else if (s.equals("/h")) // 명령어 리스트
							{
								Iterator<String> iter = listw.keySet().iterator();
								out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ명령어ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
								while (iter.hasNext())
								{
									String key = iter.next();
									out.println("→  " + key + " :		" + listw.get(key));
								}
								out.println("→  /to (사용자):	귓속말");
								out.println("→  /inv (사용자):       사용자 초대하기");
								out.println("→  /kick (사용자):	강퇴");
								out.println("→  /ulist :		방 참여자 리스트");
								out.println("→  /out,/ex :		방 나가기");
								out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
							}
							else if (s.equals("/list"))
							{
								list(out);
							}
							else if (s.equals("/rlist")) // Room_L
							{
								roomList(out);
							}
							else if (s.equals("/waituser")) // 입장전 user_L
							{
								waitU(out);

							}
							else if (s.equals("/adword")) // BlockWord
							{
								out.println("====BlockWord Add====");
								out.println("BlockWord : ");
								ss = in.readLine();
								sql = "insert into $tableName values (?)";
								String query = sql.replace("$tableName", name);
								pstmt1 = con.prepareStatement(query);
								pstmt1.setString(1, ss);
								rs = pstmt1.executeQuery();
								out.println("\'" + ss + "\'(을)를 BlockWord로 설정했습니다. ");

							}

							else if (s.equals("/delword")) // BlockWord Del
							{
								sql = "select * from $tableName";
								String query = sql.replace("$tableName", name);
								PreparedStatement pstmt2 = con.prepareStatement(query);
								rs = pstmt2.executeQuery();
								while (rs.next())
								{
									out.println("> " + rs.getString(1));
								}

								out.println("해제할 BlockWord를 입력하세요 : ");
								ss = in.readLine();
								sql = "delete from $tableName where wordd = ?";
								String query1 = sql.replace("$tableName", name);
								pstmt1 = con.prepareStatement(query1);
								pstmt1.setString(1, ss);
								rs = pstmt1.executeQuery();
								out.println("Delete_word Complete !");

							} else if (s.equals("/ulist"))
							{
								userR(title, out);
							}

							else if (s.equals("/out") || s.equals("/ex"))
							{
								roomN.get(title).remove(name);
								waitUser.put(name, out);
								if (roomN.get(title).isEmpty())
								{
									roomN.remove(title);
								}
								th = 1;
								break;
							} else
							{
								out.println("명령어 잘못입력.");
							}

						} else
						{
							sendMsg(s, name, title);
						}
					}

				}

			} catch (Exception e)
			{

			} finally
			{
				if (th != 1)
				{
					roomN.get(title).remove(name);
				}
				if (!roomN.get(title).isEmpty())
					sendMsg(name + "님이 퇴장하셨습니다.", "", title);
				try
				{
				} catch (Exception e)
				{
				}
			}

		}

	}
}