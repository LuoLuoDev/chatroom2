package com.chat.server;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.chat.util.CharacterUtil;
import com.chat.util.XMLUtil;

public class ServerConnectionThread extends Thread
{
	private ServerSocket serverSocket;

	private Socket socket;

	private JLabel jLabelToChange;

	private JButton jButtonToChange;

	private InputStream is;

	private OutputStream os;

	private Server server;

	public ServerConnectionThread(Server server, String threadName, int hostPort)
	{
		try
		{
			this.server = server;
			this.setName(threadName);

			serverSocket = new ServerSocket(hostPort); // ��ʼ�������socket

			jLabelToChange = server.getjLabel2();
			jLabelToChange.setText("Run");
			jLabelToChange.setForeground(Color.green);

			jButtonToChange = server.getjButton1();
			jButtonToChange.setVisible(false);

		}
		catch (Exception e)
		{
			e.printStackTrace();

			JOptionPane.showMessageDialog(this.server,
					"This port has been used.Please change.", "Warning",
					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				socket = serverSocket.accept();

				is = socket.getInputStream();
				os = socket.getOutputStream();

				byte[] buffer = new byte[1024];
				int len = is.read(buffer);

				// �ͻ��˷�����������Ϣ�������û�����
				String loginXML = new String(buffer, 0, len); // zhoule@@@4567_2345_127.0.0.1
				// �ӿͻ��˷�����������Ϣ�������û���
				String userName = XMLUtil.extractUsername(loginXML);
				//���õ�½�ɹ����ı��
				boolean isLogin = false;
				
				String isLoginResult = null;

				if (this.server.getMap().containsKey(userName))
				{
					isLoginResult = CharacterUtil.FALSE;
				}
				else
				{
					isLoginResult = CharacterUtil.SUCCESS;

					isLogin = true;
				}
				//�����Ƿ��½�ɹ���xml��Ϣ��ʽ �������ַ�������ʽ
				String isLoginXML = XMLUtil
						.constructIsLoginResultXML(isLoginResult);
				//���������ɵ�xml�ַ�����Ϣ�����ͻ���
				os.write(isLoginXML.getBytes());
				//�������߳����������ͻ����������֮�����Ϣ����
				ServerMsgThread serverMsgThread = new ServerMsgThread(server, socket);
				//���û������Լ����û���Ӧ�����̱߳��浽servreά����map����
				server.getMap().put(userName, serverMsgThread);
				//���¿ͻ��˵��û��б�
				serverMsgThread.updateUserList();
				
				serverMsgThread.start();
				
				
				

			}

			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}