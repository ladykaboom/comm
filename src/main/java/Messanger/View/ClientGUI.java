package Messanger.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import Messanger.Service.ClientMessageQueueService;

public class ClientGUI {

	private JFrame frame = new JFrame("Messanger");
	private JTextField textFieldWho = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 40);
	private PrintWriter out;
	private final JPanel panel = new JPanel();
	private final JLabel lblNewLabel = new JLabel("Adresat");
	private final JLabel lblNewLabel_1 = new JLabel("Wiadomość");
	private final JPanel panel_1 = new JPanel();
	private final JButton btnNewButton = new JButton("Wyślij");
	private final JTextArea textArea = new JTextArea(8, 40);
	
	//keep message until the bus comes
	private ClientMessageQueueService clientMessageQueueService;

	public ClientGUI(String nickName) {
		messageArea.setEditable(false);
		frame.setTitle(nickName);
		getFrame().getContentPane().add(new JScrollPane(messageArea), "South");
		panel.setDoubleBuffered(false);
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));

		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(lblNewLabel_1, BorderLayout.NORTH);
		textArea.setLineWrap(true);
		panel.add(textArea, BorderLayout.SOUTH);
		textArea.setBorder(UIManager.getBorder("CheckBoxMenuItem.border"));
		textArea.setEditable(false);

		frame.getContentPane().add(panel_1, BorderLayout.NORTH);
		lblNewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_1.add(lblNewLabel);
		panel_1.add(textFieldWho);
		textFieldWho.setEditable(false);
		panel_1.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textArea.getText() != null && !textArea.getText().equals("")) {
					clientMessageQueueService.addMessageToQueue(textFieldWho.getText(), textArea.getText());
					clientMessageQueueService.displayMessageQueue();
					//out.println(textFieldWho.getText() + ";" + textArea.getText());
					textArea.setText("");
				}
			}
		});
		getFrame().pack();
	}

	public void initOut(OutputStream output) {
		this.out = new PrintWriter(output, true);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public JTextArea getMessageArea() {
		return messageArea;
	}

	public void setMessageArea(JTextArea messageArea) {
		this.messageArea = messageArea;
	}

	public PrintWriter getOut() {
		return out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public JTextField getTextFieldWho() {
		return textFieldWho;
	}

	public void setTextFieldWho(JTextField textFieldWho) {
		this.textFieldWho = textFieldWho;
	}

	public JTextArea getTextArea() {
		return textArea;
	}

	public ClientMessageQueueService getClientMessageQueueService() {
		return clientMessageQueueService;
	}

	public void setClientMessageQueueService(ClientMessageQueueService clientMessageQueueService) {
		this.clientMessageQueueService = clientMessageQueueService;
	}

	
}
