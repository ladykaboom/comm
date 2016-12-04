package Messanger.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ClientMessageQueueService {

	// <String addressee, String message>
	private HashMap<String, String> messageQueue;

	public ClientMessageQueueService() {
		messageQueue = new HashMap<String, String>();
	}

	public void addMessageToQueue(String addressee, String message) {
		if (messageQueue.get(addressee) == null) {
			messageQueue.put(addressee, message);
		} else {
			String prevMsg = messageQueue.get(addressee);
			messageQueue.put(addressee, prevMsg + "\n" + message);
		}
	}

	public String getMessageFromQueue(String addressee) {
		return messageQueue.get(addressee);
	}

	public void displayMessageQueue() {
		for (String name : messageQueue.keySet()) {

			String value = messageQueue.get(name);
			System.out.println(name + " " + value);

		}
	}

	public Set<String> takeAllKeys() {		
		return messageQueue.keySet();
	}
	public void deleteMessage(String addressee) {
		messageQueue.remove(addressee);
	}

	public HashMap<String, String> getMessageQueue() {
		return messageQueue;
	}

	public void setMessageQueue(HashMap<String, String> messageQueue) {
		this.messageQueue = messageQueue;
	}

}

