package Messanger.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.wb.swing.FocusTraversalOnArray;

public class MainGUI implements ListSelectionListener {
	private JFrame frame = new JFrame("Messanger");
	private JList list;
	private JPanel panel;
	private DefaultListModel listModel;
	private JTextPane txtpnListaUytkownikw;
	private JPanel panel_1;
	private PrintWriter out;
	private ClientGUI clientGUI;

	public MainGUI() {

		listModel = new DefaultListModel();
		// listModel.addElement("Ala");
		// listModel.addElement("Marcin");
		// listModel.addElement("Alek");
		// listModel.addElement("Momo");

		panel_1 = new JPanel();
		frame.getContentPane().add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		txtpnListaUytkownikw = new JTextPane();
		panel_1.add(txtpnListaUytkownikw, BorderLayout.SOUTH);
		txtpnListaUytkownikw.setEditable(false);
		txtpnListaUytkownikw.setText("Lista użytkowników");

		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		list = new JList(listModel);
		panel.add(list, BorderLayout.NORTH);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		frame.getContentPane().setFocusTraversalPolicy(
				new FocusTraversalOnArray(new Component[] { panel_1, txtpnListaUytkownikw, panel, list }));

		getFrame().pack();
	}

	public void addElement(String name) {
		listModel.addElement(name);
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {

			if (list.getSelectedIndex() != -1) {
				// Selection
				System.out.println("Listmodel size =  " + listModel.size());

				for (int i = 0; i < listModel.size(); i++) {
					System.out.println("Listmodel [" + i + ": " + listModel.get(i));
				}
				String toWhom = listModel.get(list.getSelectedIndex()).toString();

				clientGUI = new ClientGUI(toWhom);
				clientGUI.getFrame().setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				clientGUI.getFrame().setLocation(150, 150);
				clientGUI.getFrame().setVisible(true);

				clientGUI.setOut(out);
				clientGUI.getTextFieldWho().setText(toWhom);
				clientGUI.getTextArea().setEditable(true);
				clientGUI.getTextFieldWho().setEditable(true);
			}
		}
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

	public PrintWriter getOut() {
		return out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public ClientGUI getClientGUI() {
		return clientGUI;
	}

	public void setClientGUI(ClientGUI clientGUI) {
		this.clientGUI = clientGUI;
	}

}
