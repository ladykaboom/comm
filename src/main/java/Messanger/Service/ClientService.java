package Messanger.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;

import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Messanger.Model.Bus;
import Messanger.View.ClientGUI;

public class ClientService {

	/* if ring is too large, this flag will be set to true */
	private static boolean stopSending = false;
	private static boolean sharedNode = false;

	private final static Logger log = LoggerFactory.getLogger(ClientService.class);

	private final static String HOST = "192.168.1.2";
	private final static int PORT = 9000;
	private final static int CLIENT_PORT = 9001;
	private final static int nextClientPort = 8002;

	/* only if client is shared by two rings! */
	private final static int SECOND_CLIENT_PORT = 8004;
	private final static int secondNxtClientPort = 8005;

	private static PublicKey publicKey;
	private static PrivateKey privateKey;
	private static EncryptionService encryptionService;

	private static String name;
	private static ClientGUI clientGUI;
	/* keep all messages that user want to send */
	private static ClientMessageQueueService clientMessageQueueService;

	private BufferedReader inServer;
	private PrintWriter outServer;
	private static ObjectOutputStream outNextClient;

	private static Bus globalBus;
	private static int user_in_bus_index = -1;

	/* only for 'shared client' */
	private static Bus secondBus;
	private static volatile boolean sendBusSemafor = true;

	public ClientService()
			throws IOException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		clientMessageQueueService = new ClientMessageQueueService();
		clientGUI = new ClientGUI("");
		clientGUI.setClientMessageQueueService(clientMessageQueueService);

		// Security.addProvider(new
		// org.bouncycastle.jce.provider.BouncyCastleProvider());

	}

	public void run() throws IOException, ClassNotFoundException {

		log.info("Client is starting...");

		/**
		 * Generate public and private keys Used to encrypt messages before
		 * putting them to the bus.
		 */
		encryptionService = new EncryptionService();
		encryptionService.generateKey();

		ObjectInputStream fileStream = new ObjectInputStream(new FileInputStream(encryptionService.getPublicKeyFile()));
		publicKey = (PublicKey) fileStream.readObject();

		fileStream = new ObjectInputStream(new FileInputStream(encryptionService.getPrivateKeyFile()));
		privateKey = (PrivateKey) fileStream.readObject();

		log.debug("\nGET publicKey = " + publicKey.toString());
		log.debug("\nGET privateKey = " + privateKey.toString());

		/* prepare socket and buffer for server */
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(HOST, PORT);
		inServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		outServer = new PrintWriter(socket.getOutputStream(), true);

		/* prepare server socket for new client */
		SSLServerSocketFactory sslserversocketfactoryPrevUser;
		SSLServerSocket sslserversocketPrevUser;
		sslserversocketfactoryPrevUser = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		sslserversocketPrevUser = (SSLServerSocket) sslserversocketfactoryPrevUser.createServerSocket(CLIENT_PORT);

		ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(sslserversocketPrevUser, true);
		clientConnectionHandler.start();

		/* ONLY FOR SHARED NODE! Prepare socket for client from second ring */

		// init gui
		clientGUI.initOut(socket.getOutputStream());

		while (true) {
			// server
			String line = inServer.readLine();
			log.debug("Msg = " + line);

			if (line.startsWith("LOGOWANIE")) {
				name = getName();
				clientGUI.getFrame().setTitle("Hello " + name);
				clientGUI.getOut().println(name);
				clientGUI.getOut().println(InetAddress.getLocalHost().getHostAddress());
				log.debug("IP:" + InetAddress.getLocalHost().getHostAddress());

			} else if (line.startsWith("ZALOGOWANY")) {
				clientGUI.getTextArea().setEditable(true);
				clientGUI.getTextFieldWho().setEditable(true);
			} else if (line.startsWith("NEW_NEXT")) {
				log.debug("[Client " + name + "] get NEW_NEXT");
				SSLSocketFactory sslsocketfactoryNextUser = (SSLSocketFactory) SSLSocketFactory.getDefault();
				SSLSocket socketNextUser = (SSLSocket) sslsocketfactoryNextUser.createSocket("192.168.1.1",
						nextClientPort);
				NextClient nextClient = new NextClient(socketNextUser);
				nextClient.start();
			} else if (line.startsWith("CREATE_BUS")) {
				globalBus = new Bus();
				globalBus.addNewClientToBus(name, publicKey);
			} else if (line.startsWith("STOP_SENDING")) {
				stopSending = true;

				/* if I have bus, send my name to server */
				if (globalBus != null) {
					outServer.println("HAVE_BUS " + user_in_bus_index);
				}
			} else if (line.startsWith("SHARED")) {
				/**
				 * this client will be shared by two rings. It has to merge two
				 * buses from these two rings
				 */
				sharedNode = true;
				sendBusSemafor = false;

				/* prepare server socket for new client */
				SSLServerSocketFactory new_sslserversocketfactoryPrevUser;
				SSLServerSocket new_sslserversocketPrevUser;
				new_sslserversocketfactoryPrevUser = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
				new_sslserversocketPrevUser = (SSLServerSocket) new_sslserversocketfactoryPrevUser
						.createServerSocket(CLIENT_PORT);

				ClientConnectionHandler new_clientConnectionHandler = new ClientConnectionHandler(
						new_sslserversocketPrevUser, false);
				new_clientConnectionHandler.start();

				new SharedNodeHandler().start();

			}
		}
	}

	/**
	 * Method to handle connection from prev user
	 * 
	 * @author RAVEN
	 *
	 */
	private static class ClientConnectionHandler extends Thread {
		private SSLServerSocket sslserversocketPrevUser;
		private boolean isFirstBus;

		public ClientConnectionHandler(SSLServerSocket sslserversocketPrevUser, boolean isFirstBus) {
			this.sslserversocketPrevUser = sslserversocketPrevUser;
			this.isFirstBus = isFirstBus;
		}

		public void run() {
			while (true) {
				try {
					new ConnectionHandler((SSLSocket) this.sslserversocketPrevUser.accept(), isFirstBus).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * synchronized Buses
	 */
	private static class SharedNodeHandler extends Thread {

		public SharedNodeHandler() {
		}

		public void run() {
			while (true) {

				if (sharedNode == true && globalBus != null && secondBus != null) {
					/* merge buses (copy msgs from secondBus to bus */

					secondBus.setBus(globalBus.getBus());
					sendBusSemafor = true;
				}
			}
		}
	}

	/**
	 * init connection to next client (to whom we should send the bus)
	 * 
	 * @author RAVEN
	 *
	 */
	private static class NextClient extends Thread {
		private SSLSocket socketNextUser;
		private PrintWriter outNext;
		private BufferedReader inNext;

		public NextClient(SSLSocket socketNextUser) throws UnknownHostException, IOException {
			this.socketNextUser = socketNextUser;
		}

		public void run() {
			try {
				inNext = new BufferedReader(new InputStreamReader(socketNextUser.getInputStream()));
				outNext = new PrintWriter(socketNextUser.getOutputStream(), true);
				outNextClient = new ObjectOutputStream(socketNextUser.getOutputStream());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (true) {
				// outNext.println(name);
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get the bus, read messages, write to the bus and resend...
	 * 
	 * @author RAVEN
	 *
	 */
	private static class ConnectionHandler extends Thread {
		private SSLSocket socketConHand;
		private ObjectInputStream in;
		private PrintWriter out;
		private boolean firstBus;
		private Bus myBus;

		public ConnectionHandler(SSLSocket socket, boolean firstBus) throws IOException {
			log.debug("run....constr...");
			this.socketConHand = socket;
			this.firstBus = firstBus;
		}

		public void run() {
			try {
				in = new ObjectInputStream(socketConHand.getInputStream());
				out = new PrintWriter(socketConHand.getOutputStream(), true);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				log.debug("ConnectionHandler run....");

				while (true) {

					while (stopSending == true) {
						sleep(1000);
					}
					/* wait for connection with next client */
					while (outNextClient == null) {
						log.debug("outnext null...waiting...");
						sleep(1000);
					}

					/* get bus from previous client */
					if (myBus == null) {
						myBus = (Bus) in.readObject();
						if (myBus == null) {
							return;
						}

						log.debug("\nDostalam userList size = " + myBus.getUsers().size());
						myBus.displayBus();
						sleep(2000);
					}

					/* get user header */
					String myHeader = prepareHeader(name, myBus.getLoop());

					/* get current user index */
					for (int i = 0; i < myBus.getUsers().size(); i++) {
						if (myBus.getUsers().get(i).equals(name)) {
							user_in_bus_index = i;
							break;
						}
					}

					/* add user to the bus */
					if (user_in_bus_index == -1) {
						myBus.addNewClientToBus(name, publicKey);
						user_in_bus_index = myBus.getUsers().size() - 1;
					} else {
						/* read message from the bus */
						for (int i = 0; i < myBus.getUsers().size(); i++) {
							String userName = myBus.getUsers().get(i);

							if (!userName.equals(name)) {
								LinkedList<byte[]> row_reading = myBus.getBus().get(i);

								if (row_reading.get(user_in_bus_index) != null) {

									/* get message from bus */
									byte[] msgByte = row_reading.get(user_in_bus_index);

									/* decrypt message */
									if (msgByte != null) {
										String msg = encryptionService.decrypt(msgByte, privateKey);

										/* check header */
										if (msg.startsWith(myHeader)) {
											System.out.println(
													"\nReading... msg: " + msg + " ; from user: " + userName + "\n");
											clientGUI.getMessageArea().append(userName + ": " + msg + "\n");
										}
									}
								}
							}
						}
					}

					log.debug("\nBus size = " + myBus.getBus().size());

					/*
					 * If current user is the first user in the ring, increment
					 * bus loop
					 */
					if (user_in_bus_index == 0) {
						myBus.incrementLoop();
					}

					log.debug("\n Bus loop = " + myBus.getLoop());

					/* create row to put messages to it */
					LinkedList<byte[]> my_row_write = new LinkedList<byte[]>();

					for (int i = 0; i < myBus.getUsers().size(); i++) {
						String userName = myBus.getUsers().get(i);
						String msg = clientMessageQueueService.getMessageFromQueue(userName);
						/*
						 * is there are any message for the 'userName' user in
						 * the queue, encrypt them and put into bus.
						 */
						if (msg != null) {
							/* prepare header */
							String adrHeader = prepareHeader(myBus.getUsers().get(i), myBus.getLoop());

							/* get addrs public key and encrypt the msg */
							byte[] cipherMsg = encryptionService.encrypt(adrHeader + msg,
									myBus.getUsersPublicKyes().get(i));

							my_row_write.add(cipherMsg);

							/* show msg also on my gui */
							clientGUI.getMessageArea().append(name + ": " + msg + "\n");

							/* delete msg from queue */
							clientMessageQueueService.deleteMessage(myBus.getUsers().get(i));
						} else {
							/* w.p.p. set garbage */
							byte[] garbage = "grb".getBytes();
							my_row_write.add(garbage);
						}
						log.debug("\nWriting... msg: " + msg + " for user: " + userName + "\n");
					}

					// set row to the bus
					myBus.getBus().set(user_in_bus_index, my_row_write);

					/* merge with global bus */
					if (firstBus == true) {
						globalBus = myBus;
					} else {
						secondBus = myBus;
					}

					/* if SHARED NODE, wait until bus will synchronized */
					while (sendBusSemafor == false) {
						sleep(1000);
					}

					if (sharedNode == true) {
						myBus = globalBus;
					}
					
					/* send bus */
					outNextClient.writeObject(myBus);

					/* set bus to null */
					myBus = null;

				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					socketConHand.close();
				} catch (IOException e) {
					log.debug("Exception during closing socket..." + e.toString());
				}
			}
		}

	}

	/**
	 * Prepare header which will be put before messages. Get userName + bus loop
	 * and hash it.
	 * 
	 * @param usrName
	 * @param loop
	 * @return
	 */
	private static String prepareHeader(String usrName, int loop) {

		StringBuilder headerBuilder = new StringBuilder();
		headerBuilder.append(usrName);
		headerBuilder.append(Integer.toString(loop));

		int hashCode = headerBuilder.toString().hashCode();

		log.debug("\n My header = " + headerBuilder + " ,hashCode = " + hashCode);

		return Integer.toString(hashCode);
	}

	/* Get new user name */
	private String getName() {
		return JOptionPane.showInputDialog(clientGUI.getFrame(), "Wpisz imię", "Zaloguj się",
				JOptionPane.PLAIN_MESSAGE);
	}

	public ClientGUI getClientGUI() {
		return clientGUI;
	}

	public void setClientGUI(ClientGUI clientGUI) {
		this.clientGUI = clientGUI;
	}

	public BufferedReader getInServer() {
		return inServer;
	}

	public void setInServer(BufferedReader inServer) {
		this.inServer = inServer;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static ObjectOutputStream getOutNextClient() {
		return outNextClient;
	}

	public static void setOutNextClient(ObjectOutputStream outNextClient) {
		ClientService.outNextClient = outNextClient;
	}

	public static Bus getGlobalBus() {
		return globalBus;
	}

	public static void setGlobalBus(Bus globalBus) {
		ClientService.globalBus = globalBus;
	}

}