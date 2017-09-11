package org.prasanth.swgoh.dao;

import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.prasanth.swgoh.dto.User;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
public class DAOUsers extends BaseDAO{

	private final LoadingCache<String, User> usernameToUserCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, User>() {
				@Override
				public User load(String username) throws Exception {
					return (User) getHibernateTemplate().find("From User where userId = ?", username).get(0);
				}
			});

	public User getFromCache(String name) {
		return usernameToUserCache.getUnchecked(name);
	}

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
		usernameToUserCache.invalidateAll();
	}
}
