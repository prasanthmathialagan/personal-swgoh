package org.prasanth.swgoh.dao;

import java.util.List;

import org.prasanth.swgoh.dto.GuildToon;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
public class DAOGuildToons extends BaseDAO {

	public List<GuildToon> getAllGuildToons() {
		return (List<GuildToon>) getHibernateTemplate().find("From GuildToon");
	}

	public void save(List<GuildToon> guildToons) {
		for ( GuildToon guildToon : guildToons ) {
			getHibernateTemplate().save(guildToon);
		}
	}

	public void update(List<GuildToon> guildToons) {
		for ( GuildToon guildToon : guildToons ) {
			getHibernateTemplate().update(guildToon);
		}
	}

	public void delete(List<GuildToon> guildToons) {
		getHibernateTemplate().deleteAll(guildToons);
	}

}
