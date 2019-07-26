package de.incentergy.architecture.entities;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Todo {
	// generating uuids might be expensive
	// use with hibernate:
	// @GeneratedValue(generator = "uuid2")
    // @GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Id
	private String id = UUID.randomUUID().toString();
	private String note;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
}
