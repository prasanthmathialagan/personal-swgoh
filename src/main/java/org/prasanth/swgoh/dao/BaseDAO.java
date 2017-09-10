package org.prasanth.swgoh.dao;

import org.hibernate.SessionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
public abstract class BaseDAO extends HibernateDaoSupport{

	@Autowired
	public void autowireSessionFactory(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
}
