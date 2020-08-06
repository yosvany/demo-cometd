package com.demo.cometd.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TestDAO {

	@Id
	@GeneratedValue
	private Long id;
	private String message;
	
	public TestDAO(){}

	public TestDAO(String message){
		this.message = message;
	}

	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getMessage() {
		return this.message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
