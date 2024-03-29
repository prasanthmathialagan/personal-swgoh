package org.prasanth.swgoh.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
@Entity(name = "User")
@Table(name = "Users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String userId;

	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		User user = (User) o;

		return userId != null ? userId.equals(user.userId) : user.userId == null;
	}

	@Override
	public int hashCode() {
		return userId != null ? userId.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "User{" +
				"userId='" + userId + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
