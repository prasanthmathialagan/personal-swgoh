package org.prasanth.swgoh.dao;

import java.util.List;

import org.prasanth.swgoh.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
public class DAOUsers extends BaseDAO{

	public List<User> getAllUsers() {
		return (List<User>) getHibernateTemplate().find("From User");
	}

	public void saveUsers(List<User> users) {
		for ( User user : users ) {
			getHibernateTemplate().save(user);
		}
	}

	public void updateUsers(List<User> users) {
		for ( User user : users ) {
			getHibernateTemplate().update(user);
		}
	}

	public void deleteUsers(List<User> users) {
		getHibernateTemplate().deleteAll(users);
	}
}
