package Messanger.Model;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.LinkedList;

public class Bus implements Serializable {

	/* bus - each row is connected only with one user,
	 * for example, row '1' is editable only for user number 1.
	 * Each time, the user is modyfing all of position in its row. 
	 * 	 | 0 1 2 3 4 5
	 * --|-------------
	 * 0 | X _ _ _ _ _
	 * 1 | _ X _ _ _ _
	 * 2 | _ _ X _ _ _
	 * 3 | _ _ _ X _ _
	 * 4 | _ _ _ _ X _
	 * 5 | _ _ _ _ _ X
	 * */
	private LinkedList<String> users;
	private LinkedList<PublicKey> usersPublicKyes;
	//rivate static LinkedList<String> bus_cols;
	private LinkedList<LinkedList<byte[]>> bus;
	private int loop;
	
	public Bus() {
		loop = 0;
		this.users = new LinkedList<String>();
		this.usersPublicKyes = new LinkedList<PublicKey>();
		//bus_cols = new LinkedList<String>();
		this.bus = new LinkedList<LinkedList<byte[]>>();
	}

	public void addNewClientToBus(String name, PublicKey publicKey) {
		users.add(name);
		usersPublicKyes.add(publicKey);
		System.out.println("user size " + users.size());
		
		LinkedList<byte[]> new_bus_cols = new LinkedList<byte[]>();
		byte[] e = "".getBytes();
		for(int i = 0 ; i < users.size() ; i++) {
			new_bus_cols.add(e);
		}
		this.bus.add(new_bus_cols);

		System.out.println("[Bus] User " + name + " added to the bus.");
		System.out.println("[Bus] bus size = " + bus.size() + " new col size = " + new_bus_cols.size());
	}
	
	public void deleteClientFromBus(User user) {
		//int index = bus_cols.indexOf(user);
		//bus_cols.remove(index);
		//bus.remove(index);
	}
	
	public void displayBus() {
		
		System.out.println("---------BUS-------- loop: " + getLoop());
		System.out.println("user size " + users.size());

		System.out.println(bus.toString() + " ");
		System.out.println("Hereee....");

		
	}
	
	public void incrementLoop() {
		this.loop += 1;
	}
	
	public LinkedList<LinkedList<byte[]>> getBus() {
		return bus;
	}

	public void setBus(LinkedList<LinkedList<byte[]>> bus) {
		this.bus = bus;
	}

	public int getLoop() {
		return loop;
	}

	public void setLoop(int loop) {
		this.loop = loop;
	}

//	public LinkedList<String> getBus_cols() {
//		return bus_cols;
//	}
//
//	public void setBus_cols(LinkedList<String> bus_cols) {
//		this.bus_cols = bus_cols;
//	}

	public LinkedList<String> getUsers() {
		return users;
	}

	public void setUsers(LinkedList<String> users) {
		this.users = users;
	}

	public LinkedList<PublicKey> getUsersPublicKyes() {
		return usersPublicKyes;
	}

	public void setUsersPublicKyes(LinkedList<PublicKey> usersPublicKyes) {
		this.usersPublicKyes = usersPublicKyes;
	}
	
	
	
	
}
