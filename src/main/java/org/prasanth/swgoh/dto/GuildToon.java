package org.prasanth.swgoh.dto;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
@Entity
@Table(name = "GuildToons")
public class GuildToon {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private long userId;

	private long toonId;

	@Embedded
	private GuildToonData guildToonData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getToonId() {
		return toonId;
	}

	public void setToonId(long toonId) {
		this.toonId = toonId;
	}

	public GuildToonData getGuildToonData() {
		return guildToonData;
	}

	public void setGuildToonData(GuildToonData guildToonData) {
		this.guildToonData = guildToonData;
	}

	@Embeddable
	public static class GuildToonData {

		private int star;

		private long galacticPower;

		private int speed;

		public long getGalacticPower() {
			return galacticPower;
		}

		public void setGalacticPower(long galacticPower) {
			this.galacticPower = galacticPower;
		}

		public int getStar() {
			return star;
		}

		public void setStar(int star) {
			this.star = star;
		}

		public int getSpeed() {
			return speed;
		}

		public void setSpeed(int speed) {
			this.speed = speed;
		}
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		GuildToon guildToon = (GuildToon) o;

		if ( userId != guildToon.userId ) {
			return false;
		}
		return toonId == guildToon.toonId;
	}

	@Override
	public int hashCode() {
		int result = (int) ( userId ^ ( userId >>> 32 ) );
		result = 31 * result + (int) ( toonId ^ ( toonId >>> 32 ) );
		return result;
	}
}
