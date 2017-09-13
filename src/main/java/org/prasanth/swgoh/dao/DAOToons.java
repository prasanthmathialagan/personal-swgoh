package org.prasanth.swgoh.dao;

import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.prasanth.swgoh.dto.Toon;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
public class DAOToons extends BaseDAO {

	private final LoadingCache<String, Toon> nameToToonCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, Toon>() {
				@Override
				public Toon load(String name) throws Exception {
					return (Toon) getHibernateTemplate().find("From Toon where name = ?", name).get(0);
				}
			});

	private final LoadingCache<Long, Toon> idToToonCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<Long, Toon>() {
				@Override
				public Toon load(Long id) throws Exception {
					return getHibernateTemplate().get(Toon.class, id);
				}
			});

	public List<Toon> getAllToons() {
		return (List<Toon>) getHibernateTemplate().find("From Toon");
	}

	public Toon getFromCache(String name) {
		return nameToToonCache.getUnchecked(name);
	}

	public Toon getFromCache(long id) {
		return idToToonCache.getUnchecked(id);
	}

	public void saveToons(List<Toon> toons) {
		for ( Toon toon : toons ) {
			getHibernateTemplate().save(toon);
		}
		nameToToonCache.invalidateAll();
		idToToonCache.invalidateAll();
	}
}
