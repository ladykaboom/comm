package Messanger.Model;

import java.io.Serializable;

/*
 * Communicats sending by server to clients
 * 
 */
public class ServerMsg implements Serializable{

	private String message;
	private Bus bus;
	
	public void clean() {
		message = "";
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public Bus getBus() {
		return bus;
	}
	public void setBus(Bus bus) {
		this.bus = bus;
	}
	
	
}
