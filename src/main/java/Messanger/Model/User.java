package Messanger.Model;

import java.io.PrintWriter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {

	@Id
	private int id;

	@Column
	private String name;

	@Column
	private String number;

	@Column
	private String nick;

	@Column
	private String status;

	@Column
	private PrintWriter out;

	public User() {
		// this.name = name;
		this.status = "NIEDOSTĘPNY";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PrintWriter getOut() {
		return out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public User withName(final String name) {
		this.name = name;
		return this;
	}
	
	public User withNumber(final String number) {
		this.number = number;
		return this;
	}
	
	public User withNick(final String nick) {
		this.nick = nick;
		return this;
	}
	
	public User withStatus(final String status) {
		this.status = status;
		return this;
	}
	
	public User withId(final int id) {
		this.id = id;
		return this;
	}
	
}
