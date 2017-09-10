package org.prasanth.swgoh.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
@Entity(name = "Toon")
@Table(name = "Toons")
public class Toon {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

		Toon toon = (Toon) o;

		return name != null ? name.equals(toon.name) : toon.name == null;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
}
