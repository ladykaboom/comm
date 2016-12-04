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

	private final static Logger log = LoggerFactory.getLogger(ClientService.class);

	private final static String HOST = "192.168.1.2";
	private final static int PORT = 9000;
	private final static int CLIENT_PORT = 9001;
	private final static int nextClientPort = 8002;

	private static PublicKey publicKey;
	private static PrivateKey privateKey;
	private static EncryptionService encryptionService;

	private static String name;
	private static ClientGUI clientGUI;
	/* keep all messages that user want to send */
	private static ClientMessageQueueService clientMessageQueueService;

	private BufferedReader inServer;
	private static ObjectOutputStream outNextClient;

	private static Bus bus;

	/* 0 - no bus, 1 - bus just created, 2 - bus has been created */
	private static int bus_flag = 0;

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

		/* prepare server socket for new client */
		SSLServerSocketFactory sslserversocketfactoryPrevUser;
		SSLServerSocket sslserversocketPrevUser;
		sslserversocketfactoryPrevUser = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		sslserversocketPrevUser = (SSLServerSocket) sslserversocketfactoryPrevUser.createServerSocket(CLIENT_PORT);

		ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(sslserversocketPrevUser);
		clientConnectionHandler.start();

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
				bus = new Bus();
				bus.addNewClientToBus(name, publicKey);
				bus_flag = 1;
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

		public ClientConnectionHandler(SSLServerSocket sslserversocketPrevUser) {
			this.sslserversocketPrevUser = sslserversocketPrevUser;
		}

		public void run() {
			while (true) {
				try {
					new ConnectionHandler((SSLSocket) this.sslserversocketPrevUser.accept()).start();
				} catch (IOException e) {
					e.printStackTrace();
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

		public ConnectionHandler(SSLSocket socket) throws IOException {
			log.debug("run....constr...");
			this.socketConHand = socket;
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

					/* wait for connection with next client */
					while (outNextClient == null) {
						log.debug("outnext null...waiting...");
						sleep(1000);
					}

					/* get bus from previous client */
					if (bus == null) {
						bus = (Bus) in.readObject();
						if (bus == null) {
							return;
						}

						log.debug("\nDostalam userList size = " + bus.getUsers().size());
						bus.displayBus();
						sleep(2000);
					}

					/* get user header */
					String myHeader = prepareHeader(name, bus.getLoop());

					/* get current user index */
					int user_in_bus_index = -1;
					for (int i = 0; i < bus.getUsers().size(); i++) {
						if (bus.getUsers().get(i).equals(name)) {
							user_in_bus_index = i;
							break;
						}
					}

					/* add user to the bus */
					if (user_in_bus_index == -1) {
						bus.addNewClientToBus(name, publicKey);
						user_in_bus_index = bus.getUsers().size() - 1;
					} else {
						/* read message from the bus */
						for (int i = 0; i < bus.getUsers().size(); i++) {
							String userName = bus.getUsers().get(i);

							if (!userName.equals(name)) {
								LinkedList<byte[]> row_reading = bus.getBus().get(i);

								if (row_reading.get(user_in_bus_index) != null) {

									/* get message from bus */
									byte[] msgByte = row_reading.get(user_in_bus_index);

									/* decrypt message */
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

					log.debug("\nBus size = " + bus.getBus().size());

					/*
					 * If current user is the first user in the ring, increment
					 * bus loop
					 */
					if (user_in_bus_index == 0) {
						bus.incrementLoop();
					}

					log.debug("\n Bus loop = " + bus.getLoop());

					/* create row to put messages to it */
					LinkedList<byte[]> my_row_write = new LinkedList<byte[]>();

					for (int i = 0; i < bus.getUsers().size(); i++) {
						String userName = bus.getUsers().get(i);
						String msg = clientMessageQueueService.getMessageFromQueue(userName);
						/*
						 * is there are any message for the 'userName' user in
						 * the queue, encrypt them and put into bus.
						 */
						if (msg != null) {
							/* prepare header */
							String adrHeader = prepareHeader(bus.getUsers().get(i), bus.getLoop());

							/* get addrs public key and encrypt the msg */
							byte[] cipherMsg = encryptionService.encrypt(adrHeader + msg,
									bus.getUsersPublicKyes().get(i));

							my_row_write.add(cipherMsg);

							/* delete msg from queue */
							clientMessageQueueService.deleteMessage(bus.getUsers().get(i));
						} else {
							/* w.p.p. set garbage */
							byte[] garbage = "grb".getBytes();
							my_row_write.add(garbage);
						}
						log.debug("\nWriting... msg: " + msg + " for user: " + userName + "\n");
					}

					// set row to the bus
					bus.getBus().set(user_in_bus_index, my_row_write);

					/* send bus */
					outNextClient.writeObject(bus);

					/* set bus to null */
					bus = null;

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
	 * Prepare header which will be put before messages.
	 * Get userName + bus loop and hash it.
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

	public static Bus getBus() {
		return bus;
	}

	public static void setBus(Bus bus) {
		ClientService.bus = bus;
	}

}