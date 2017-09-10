package org.prasanth.swgoh.dao;

import java.util.List;

import org.prasanth.swgoh.dto.Toon;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
public class DAOToons extends BaseDAO {

	public List<Toon> getAllToons() {
		return (List<Toon>) getHibernateTemplate().find("From Toon");
	}

	public void saveToons(List<Toon> toons) {
		for ( Toon toon : toons ) {
			getHibernateTemplate().save(toon);
		}
	}
}
