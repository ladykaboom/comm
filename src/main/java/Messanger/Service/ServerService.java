package Messanger.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;

import Messanger.Model.ServerMsg;
import Messanger.Model.User;
import Messanger.Repository.UserRepository;
import Messanger.Repository.UsersRepository;

@EnableAutoConfiguration
@ComponentScan
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
@Service
public class ServerService {

	private final static Logger log = LoggerFactory.getLogger(ServerService.class);
	private static final int PORT = 9000;

	private static HashSet<String> names = new HashSet<String>();
	private static HashMap<String, PrintWriter> outs = new HashMap<String, PrintWriter>();
	private static UsersRepository usersRepository;
	private static List<User> userList;
	//private static Bus bus;

	public static void main(String[] args) throws Exception {

		log.debug("Server is starting...");

		System.setProperty("javax.net.ssl.trustStore", "C:/mySrvKeystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		System.setProperty("javax.net.ssl.keyStore", "C:/mySrvKeystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		System.setProperty("java.net.useSystemProxies", "true");

		SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(PORT);

		log.debug("Server started, listen on the " + PORT + " port.");

		usersRepository = new UsersRepository();
		userList = (Collections.synchronizedList(new LinkedList<User>()));

		//bus = new Bus();
		//bus.addNewClientToBus("Lek");

		//log.debug("Main bus " + bus);

		try {
			while (true) {
				new ConnectionHandler((SSLSocket) sslserversocket.accept()).start();
			}
		} finally {
			sslserversocket.close();
		}
	}

	private static class ConnectionHandler extends Thread {
		private String name;
		private SSLSocket socket;
		private BufferedReader in;
		private PrintWriter out;
		private static ServerMsg serverMsg;
		private static ObjectOutputStream outToServer;

		public ConnectionHandler(SSLSocket socket) {
			this.socket = socket;
			serverMsg = new ServerMsg();
		}

		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				//outToServer = new ObjectOutputStream(socket.getOutputStream());

				log.debug("\n [Server] Dostalem ip = " + socket.getSession().getPeerHost());

				while (true) {
					out.println("LOGOWANIE");

					name = in.readLine();
					if (name == null) {
						return;
					}

					// FIXME bad idea (userRepository will be read from
					// database)
					synchronized (names) {
						if (!names.contains(name)) {
							log.debug("Dodaje..." + name);
							names.add(name);

							int isUserInSet = usersRepository.isUserInSet(name);
							if (isUserInSet != -1) {
								log.debug("[Server]: " + name + " is in repository.");
								usersRepository.getUsers().get(isUserInSet).setStatus("DOSTĘPNY");
							} else {
								log.debug("[Server]: " + name + " isn't in repository.");
								User user = new User();
								user.setName(name);
								user.setIp(socket.getSession().getPeerHost());
								log.debug("\nUSER " + user.getName() + " IP = " + user.getIp());
								usersRepository.getUsers().add(user);

								usersRepository.getUsers().get((usersRepository.getUsers()).size() - 1)
										.setStatus("DOSTĘPNY");

								userList.add(user);

								// TODO: powiadom poprzedniego uzytkownika o
								// zmianie w liscie
								/* Send previous and next user on the list */
								if (userList.size() > 1) {
									String prevUserName = userList.get(userList.size() - 2).getName();
									log.debug(prevUserName);
									PrintWriter prevUserOut = outs.get(prevUserName);
									out.println("NEW_NEXT " + socket.getSession().getPeerHost());
								
								}
							}

							log.debug("[Server]: " + name + " connected.");

							break;
						}
					}
				}

				outs.put(name, out);
				log.debug("Zalogowany");

				out.println("ZALOGOWANY");

				/* Send create bus */
				if (userList.size() == 2) {
					out.write("CREATE_BUS");
				}

				/* Send previous and next user on the list */
				log.debug("\nUSER " + userList.get(0).getName() + " IP = " + userList.get(0).getIp());
				if (userList.size() > 1) {
					out.println("NEXT " + userList.get(0).getIp());
				}

				sleep(2000);

				while (true) {
					String input = in.readLine();
					if (input == null) {
						return;
					}
				}
			} catch (IOException e) {
				log.debug(e.toString());
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (name != null) {
					names.remove(name);
				}
				if (out != null) {
					outs.remove(out);
				}
				log.debug("[Server]: " + name + " disconnected.");
				try {
					socket.close();
				} catch (IOException e) {
					log.debug("Exception during closing socket..." + e.toString());
				}
			}
		}

	}

	public static List<User> getList() {
		return userList;
	}

	public static void setList(List<User> list) {
		ServerService.userList = list;
	}

}