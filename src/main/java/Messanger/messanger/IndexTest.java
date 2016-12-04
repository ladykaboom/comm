package Messanger.messanger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class IndexTest {

	private static LinkedList<String> users;
	private static LinkedList<String> rows;
	private static LinkedList<LinkedList<String>> bus;

	public static void main(String[] args) {

		users = new LinkedList<String>();
		rows = new LinkedList<String>();
		bus = new LinkedList<LinkedList<String>>();

		users.add("Ala");
		users.add("Marcin");
		users.add("Momo");

		rows = (LinkedList<String>) users.clone();

		for (int i = 0; i < rows.size(); i++) {
			bus.add(rows);
		}

		String user = "Marcin";

		for (int i = 0; i < rows.size(); i++) {
			if (users.get(i).equals(user)) {
				LinkedList<String> row = bus.get(i);
				for (int j = 0; j < rows.size(); j++) {
					row.set(j, " X" + j );
				}
				bus.set(i, row);
				break;
			}
		}

		for (int i = 0; i < rows.size(); i++) {
			System.out.print(i + ": ");
			for (int j = 0; j < rows.size(); j++) {
				System.out.print(bus.get(i).get(j));
			}
			System.out.println("");

		}

	}

}
