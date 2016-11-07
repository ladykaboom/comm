package Messanger.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import Messanger.Model.User;
import Messanger.Repository.UserRepository;
import Messanger.Repository.UsersRepository;

@EnableAutoConfiguration
@ComponentScan
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
@Service
public class ServerService {
	
	private final static Logger log = LoggerFactory.getLogger(ServerService.class);
	private static final int PORT = 9001;

	private static HashSet<String> names = new HashSet<String>();
	private static HashSet<PrintWriter> outs = new HashSet<PrintWriter>();
	private static UsersRepository usersRepository;

	
	public static void main(String[] args) throws Exception {
				
		log.debug("Server is starting...");

		System.setProperty("javax.net.ssl.trustStore", "C:/mySrvKeystore");
		System.setProperty("javax.net.ssl.trustStorePassword","123456");
		System.setProperty("javax.net.ssl.keyStore", "C:/mySrvKeystore");
		System.setProperty("javax.net.ssl.keyStorePassword","123456");

		SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
				.getDefault();
		SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(PORT);
		
		log.debug("Server started, listen on the " + PORT + " port.");
		
		usersRepository = new UsersRepository();
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

		public ConnectionHandler(SSLSocket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				while (true) {
					out.println("LOGOWANIE");
					name = in.readLine();
					if (name == null) {
						return;
					}
					//FIXME bad idea (userRepository will be read from database)
					synchronized (names) {
						if (!names.contains(name)) {
							log.debug("Dodaje..." + name);
							names.add(name);
							
							int isUserInSet = usersRepository.isUserInSet(name);
							if (isUserInSet != -1) {
								log.debug("[Server]: " + name + " is in repository.");
								usersRepository.getUsers().get(isUserInSet).setOut(out);
								usersRepository.getUsers().get(isUserInSet).setStatus("DOSTĘPNY");
							} else {
								log.debug("[Server]: " + name + " isn't in repository.");
								User user = new User();
								user.setName(name);
								usersRepository.getUsers().add(user);
								usersRepository.getUsers().get((usersRepository.getUsers()).size() - 1).setOut(out);
								usersRepository.getUsers().get((usersRepository.getUsers()).size() - 1)
										.setStatus("DOSTĘPNY");
							}
							log.debug("[Server]: " + name + " connected.");
							
							//powiadom innych o nowym użytkowniku
							for (PrintWriter writer : outs) {
								writer.println("NEW_USER " + name);
							}
							
							break;
						}
					}
				}

				out.println("ZALOGOWANY");
				outs.add(out);
				
				out.println("USERS " + names);
				out.println("Users online: " + names);

				while (true) {
					String input = in.readLine();
					if (input == null) {
						return;
					}

					String[] in = input.split(";");
					String toWhom = in[0];
					String msg = in[1];
					int isUserInSet = usersRepository.isUserInSet(toWhom);

					if (isUserInSet != -1) {
						usersRepository.getUsers().get(isUserInSet).getOut().println("SEND " + name + ": " + msg);
						System.out.println("[Serwer] Wysylam do konkretnej osoby");
						usersRepository.getUsers().get(usersRepository.getUserByName(name)).getOut()
								.println("SEND " + name + ": " + msg);

					} else {
						for (PrintWriter writer : outs) {
							writer.println("SEND " + name + ": " + msg);
						}
						log.debug("[Server] Sending msg to all users...");
					}
					log.debug("[Server] " + name + ": " + msg);
				}
			} catch (IOException e) {
				log.debug(e.toString());
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
	
}
