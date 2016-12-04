package Messanger.messanger;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;
import javax.swing.JFrame;

import Messanger.Service.ClientService;


public class App {
	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		System.setProperty("javax.net.ssl.trustStore", "C:/mySrvKeystore");
		System.setProperty("javax.net.ssl.trustStorePassword","123456");
		System.setProperty("javax.net.ssl.keyStore", "C:/mySrvKeystore");
		System.setProperty("javax.net.ssl.keyStorePassword","123456");
		System.setProperty("java.net.useSystemProxies", "true");
		//System.setProperty("javax.net.debug", "all");

		ClientService clientService = new ClientService();
		clientService.getClientGUI().getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clientService.getClientGUI().getFrame().setLocation(50,30);
		clientService.getClientGUI().getFrame().setSize(400,400);
		clientService.getClientGUI().getFrame().setVisible(true);

		clientService.run();
	}
}
