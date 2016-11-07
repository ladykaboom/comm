package Messanger.messanger;

import java.io.IOException;

import javax.swing.JFrame;

import Messanger.Service.ClientService;


public class App {
	public static void main(String[] args) throws IOException {
		System.setProperty("javax.net.ssl.trustStore", "C:/mySrvKeystore");
		System.setProperty("javax.net.ssl.trustStorePassword","123456");
		System.setProperty("javax.net.ssl.keyStore", "C:/mySrvKeystore");
		System.setProperty("javax.net.ssl.keyStorePassword","123456");

		
		ClientService clientService = new ClientService();
		clientService.getMainGUI().getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clientService.getMainGUI().getFrame().setLocation(50,30);
		clientService.getMainGUI().getFrame().setSize(200,300);
		clientService.getMainGUI().getFrame().setVisible(true);

		clientService.run();
	}
}
