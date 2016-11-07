package Messanger.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Messanger.View.ClientGUI;
import Messanger.View.MainGUI;
import Messanger.View.NewMsgGUI;

public class ClientService {

	private final static Logger log = LoggerFactory.getLogger(ClientService.class);
	
	private final static String HOST = "192.168.1.2";
	private final static int PORT = 9001;

	private String name;
	private BufferedReader in;
	private ClientGUI clientGUI;
	private NewMsgGUI newMsgGUI;
	private MainGUI mainGUI;

	public ClientService() {
		mainGUI = new MainGUI();
		clientGUI = mainGUI.getClientGUI();
	}

	public void run() throws IOException {

		log.info("Client is starting...");
		
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(HOST, PORT);

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		mainGUI.initOut(socket.getOutputStream());

		while (true) {
			String line = in.readLine();
			if (line.startsWith("LOGOWANIE")) {
				this.name = getName();
				mainGUI.getFrame().setTitle("Hello " + name);
				mainGUI.getOut().println(name);

			} else if (line.startsWith("SEND")) {
				if (mainGUI.getClientGUI() != null) {
					mainGUI.getClientGUI().getMessageArea().append(line.substring(5) + "\n");
				} else {
					log.info("[C]:---Nie ma otwartego okna---");
					if (newMsgGUI == null)
						newMsgGUI = new NewMsgGUI("Nowa wiadomość");
				}
			} else if (line.startsWith("USERS")) {
				String usersStr = line.substring(7, line.length() - 1);
				String[] users = usersStr.split(",");
				log.info("[C]: " + users);
				for (String user : users) {
					if (!user.equals(name))
						mainGUI.addElement(user);
				}
			} else if (line.startsWith("NEW_USER")) {
				String newUser = line.substring(9);

				log.info("Newuser =" + newUser);
				if (!newUser.equals(name))
					mainGUI.addElement(newUser);

			}
		}
	}

	private String getName() {
		return JOptionPane.showInputDialog(mainGUI.getFrame(), "Wpisz imię", "Zaloguj się", JOptionPane.PLAIN_MESSAGE);
	}

	public ClientGUI getClientGUI() {
		return clientGUI;
	}

	public void setClientGUI(ClientGUI clientGUI) {
		this.clientGUI = clientGUI;
	}

	public MainGUI getMainGUI() {
		return mainGUI;
	}

	public void setMainGUI(MainGUI mainGUI) {
		this.mainGUI = mainGUI;
	}

	public NewMsgGUI getNewMsgGUI() {
		return newMsgGUI;
	}

	public void setNewMsgGUI(NewMsgGUI newMsgGUI) {
		this.newMsgGUI = newMsgGUI;
	}

}