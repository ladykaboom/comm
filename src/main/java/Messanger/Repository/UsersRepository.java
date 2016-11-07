package Messanger.Repository;

import java.util.ArrayList;
import java.util.List;

import Messanger.Model.User;

public class UsersRepository {

	private static List<User> users;
	
	public UsersRepository() {
		// init repository
		users = new ArrayList<User>();
		
		User user1 = new User();
		User user2 = new User();
		
		user1.setName("Marcin");
		user2.setName("Alek");
		
		users.add(user1);
		users.add(user2);

	}

	public static List<User> getUsers() {
		return users;
	}

	public static void setUsers(List<User> users) {
		UsersRepository.users = users;
	}
	
	public int isUserInSet(String name) {
		for(int i = 0 ; i < users.size() ; i++) {
			if(users.get(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public int getUserByName(String name) {
		for(int i = 0 ; i < users.size() ; i++) {
			if(users.get(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
}
