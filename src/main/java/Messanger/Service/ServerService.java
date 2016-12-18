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

	private static final int MAX_USER_IN_ONE_RING = 100;

	private static final int PORT = 9000;
	private final static Logger log = LoggerFactory.getLogger(ServerService.class);

	private static HashSet<String> names = new HashSet<String>();
	private static HashMap<String, PrintWriter> outs = new HashMap<String, PrintWriter>();
	private static UsersRepository usersRepository;
	private static LinkedList<User> userList;

	private static AdvancedTokenRingAlgorithm advancedTokenRingAlgorithm;

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
		userList = (LinkedList<User>) (Collections.synchronizedList(new LinkedList<User>()));

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

								/* Send previous and next to the prev user */
								if (userList.size() > 1) {
									String prevUserName = userList.get(userList.size() - 2).getName();
									log.debug(prevUserName);
									PrintWriter prevUserOut = outs.get(prevUserName);
									prevUserOut.println("NEW_NEXT " + socket.getSession().getPeerHost());

									/* Send NEXT to current user */
									out.println("NEW_NEXT " + userList.get(0).getIp());
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
					out.println("CREATE_BUS");
				}

				sleep(2000);

				while (true) {

					String input = in.readLine();
					if (input == null) {
						return;
					}
					String userWithBus = "";
					if(input.startsWith("HAVE_BUS")) {
						userWithBus = input.substring(9);
					}
					/* check if ring is not overloaded */
					if (userList.size() > MAX_USER_IN_ONE_RING) {
						sleep(1000);
					} else {
						/*
						 * ring is overloaded! Time to devide it into two
						 * smaller
						 */
						advancedTokenRingAlgorithm = new AdvancedTokenRingAlgorithm(userList);
						advancedTokenRingAlgorithm.createNewRing(Integer.valueOf(userWithBus));
						LinkedList<User> firstRing = advancedTokenRingAlgorithm.getListOfRings().get(0);
						LinkedList<User> secondRing = advancedTokenRingAlgorithm.getListOfRings().get(1);

						/**
						 * Find 'shared node' and send signal to it
						 */
						String sharedNodeName = firstRing.get(firstRing.size()-1).getName();
						PrintWriter sharedNodeOut = outs.get(sharedNodeName);
						sharedNodeOut.println("SHARED");
								
						/**
						 * first ring: send new addresses to first and last user
						 * in this ring
						 */
						String firstUserInFirstRingIP = firstRing.get(0).getIp();
						String secondUserInFirstRingIP = firstRing.get(firstRing.size() - 1).getIp();

						String firstUserInFirstRingName = firstRing.get(0).getName();
						PrintWriter firstUserInFirstRingOut = outs.get(firstUserInFirstRingName);
						firstUserInFirstRingOut.println("NEW_NEXT " + secondUserInFirstRingIP);

						String secondUserInFirstRingName = firstRing.get(firstRing.size() - 1).getName();
						PrintWriter secondUserInFirstRingOut = outs.get(secondUserInFirstRingName);
						secondUserInFirstRingOut.println("NEW_NEXT " + firstUserInFirstRingIP);

						/**
						 * second ring: send new addresses to first and last
						 * user in this ring
						 */
						String firstUserInSecondRingIP = secondRing.get(0).getIp();
						String secondUserInSecondRingIP = secondRing.get(secondRing.size() - 1).getIp();

						String firstUserInSecondRingName = secondRing.get(0).getName();
						PrintWriter firstUserInSecondRingOut = outs.get(firstUserInSecondRingName);
						firstUserInSecondRingOut.println("NEW_NEXT " + secondUserInSecondRingIP);

						String secondUserInSecondRingName = secondRing.get(secondRing.size() - 1).getName();
						PrintWriter secondUserInSecondRingOut = outs.get(secondUserInSecondRingName);
						secondUserInSecondRingOut.println("NEW_NEXT " + firstUserInSecondRingIP);


				
						/*
						 * send signal to middle user in the second ring to
						 * create new bus
						 */
						String middleUserInSecondRingName = secondRing.get(secondRing.size()/2).getName();
						PrintWriter middleUserInSecondRingOut = outs.get(middleUserInSecondRingName);
						middleUserInSecondRingOut.println("CREATE_BUS");
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

	public static LinkedList<User> getList() {
		return userList;
	}

	public static void setList(LinkedList<User> list) {
		ServerService.userList = list;
	}

}