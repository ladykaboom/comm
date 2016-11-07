package Messanger.Service;

import java.util.HashSet;

public class ServerObservator {

	private HashSet<String> availableUsers = new HashSet<String>();

	public HashSet<String> getAvailableUsers() {
		return availableUsers;
	}

	public void setAvailableUsers(HashSet<String> availableUsers) {
		this.availableUsers = availableUsers;
	}
	
	public void addUser(String name) {
		availableUsers.add(name);
	}
}
