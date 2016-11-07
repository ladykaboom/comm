package Messanger.View;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class NewMsgGUI {

	private JFrame frame;
	private JPanel panel;
	
	public NewMsgGUI(String title) {
		frame = new JFrame(title);
		panel = new JPanel();
		
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		
		JTextPane txtpnUytkownikWysaWiadomo = new JTextPane();
		txtpnUytkownikWysaWiadomo.setText("Użytkownik wysłał wiadomość");
		txtpnUytkownikWysaWiadomo.setEditable(false);
		panel.add(txtpnUytkownikWysaWiadomo);
		
		frame.setBounds(400, 400, 200, 50);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setVisible(true);
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}
	
	
}
