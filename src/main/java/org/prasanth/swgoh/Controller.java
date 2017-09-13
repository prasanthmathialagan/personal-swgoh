package org.prasanth.swgoh;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.prasanth.swgoh.dao.DAOGuildToons;
import org.prasanth.swgoh.dao.DAOToons;
import org.prasanth.swgoh.dao.DAOUsers;
import org.prasanth.swgoh.dto.GuildToon;
import org.prasanth.swgoh.dto.GuildToon.GuildToonData;
import org.prasanth.swgoh.dto.Toon;
import org.prasanth.swgoh.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
public class Controller {

	private static final Logger LOGGER = Logger.getLogger(Controller.class);

	public static final String GUILD_MEMBERS_FILE = "guildmembers";
	public static final String ALL_TOONS_FILE = "alltoons";
	public static final String GUILD_TOONS_FILE = "guildtoons";

	@Autowired
	private DAOUsers daoUsers;

	@Autowired
	private DAOToons daoToons;

	@Autowired
	private DAOGuildToons daoGuildToons;

	@Value("${dataDir}")
	private String dataDir;

	public String getDataDir() {
		return dataDir;
	}

	@Transactional
	public void reconcileUsers() throws Exception {
		List<User> usersFromDB = daoUsers.getAllUsers();
		List<User> usersFromCSV = parseUsers();

		Map<String, User> dbMap = usersFromDB.stream().collect(Collectors.toMap(User::getUserId, user -> user));
		Map<String, User> csvMap = usersFromCSV.stream().collect(Collectors.toMap(User::getUserId, user -> user));

		List<User> newUsers = new ArrayList<>();
		List<User> deletedUsers = new ArrayList<>();
		List<User> updatedUsers = new ArrayList<>();

		// NewUsers
		csvMap.forEach((userId, user) -> {
			if (!dbMap.containsKey(userId)) {
				newUsers.add(user);
				LOGGER.info("New user = " + user);
			}
		});

		// DeletedUsers
		dbMap.forEach((userId, user) -> {
			if (!csvMap.containsKey(userId)) {
				deletedUsers.add(user);
			}
		});

		// UpdatedUsers
		dbMap.forEach((userId, oldUser) -> {
			if (csvMap.containsKey(userId)) {
				User newUser = csvMap.get(userId);

				// Check for the equality of names
				// TODO Move to an interface based reconciliation
				if (!Objects.equals(newUser.getName(), oldUser.getName())) {
					oldUser.setName(newUser.getName());
					updatedUsers.add(oldUser);
					LOGGER.info("Updated user = " + newUser);
				}
			}
		});

		daoUsers.saveUsers(newUsers);
		daoUsers.deleteUsers(deletedUsers); // TODO: Delete the data from GuildToons
		daoUsers.updateUsers(updatedUsers);

		LOGGER.info("Users reconciliation summary: New=" + newUsers.size() + ", Deleted=" + deletedUsers.size() + ", Updated=" + updatedUsers.size());
	}

	private List<User> parseUsers() throws IOException {
		File file = new File(dataDir + File.separatorChar + GUILD_MEMBERS_FILE);
		CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
		List<User> users = new ArrayList<>();
		for ( CSVRecord record: parser) {
			User user = new User();
			user.setUserId(record.get(0).trim().replace("%20", " ")); // TODO : Find a clean way to handle the spaces in the username
			user.setName(record.get(1).trim());
			users.add(user);
		}
		return users;
	}

	@Transactional
	public void reconcileAllToons() throws Exception {
		List<Toon> toonsFromDB = daoToons.getAllToons();
		List<Toon> toonsFromCSV = parseToons();

		// It is unlikely that the toons be removed from SWGOH. So it is enough to just add the new toons
		Set<String> allToonsFromDB = new HashSet<>(Lists.transform(toonsFromDB, Toon::getName));

		List<Toon> newToons = new ArrayList<>();
		toonsFromCSV.forEach((toon -> {
			if (!allToonsFromDB.contains(toon.getName())) {
				newToons.add(toon);
				LOGGER.info("New toon = " + toon);
			}
		}));

		daoToons.saveToons(newToons);

		LOGGER.info("Toons reconciliation summary: New=" + newToons.size());
	}

	private List<Toon> parseToons() throws IOException {
		File file = new File(dataDir + File.separatorChar + ALL_TOONS_FILE);
		CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
		List<Toon> toons = new ArrayList<>();
		for ( CSVRecord record: parser) {
			Toon toon = new Toon();
			toon.setName(record.get(0).trim());
			toons.add(toon);
		}
		return toons;
	}


	@Transactional
	public void reconcileGuildToons() throws Exception {
		Table<Long, Long, GuildToonData> guildToonsFromCSV = parseGuildToons();

		List<GuildToon> allGuildToons = daoGuildToons.getAllGuildToons();
		Table<Long, Long, GuildToonData> guildToonsFromDB = convertToTable(allGuildToons); // Max = 120*50 = 6000 entries

		// Updated and deleted
		List<GuildToon> updatedGuildToons = new ArrayList<>();
		List<GuildToon> deletedGuildToons = new ArrayList<>();

		allGuildToons.forEach((guildToon -> {
			GuildToonData newGuildToonData = guildToonsFromCSV.get(guildToon.getUserId(), guildToon.getToonId());
			if (newGuildToonData == null) {
				deletedGuildToons.add(guildToon);
			} else {
				GuildToonData oldGuildToonData = guildToon.getGuildToonData();

				boolean updated = false;
				if (newGuildToonData.getStar() != oldGuildToonData.getStar()) {
					updated = true;
					LOGGER.info(
							daoUsers.getFromCache(guildToon.getUserId()).getName() +
									" increased star of " + daoToons.getFromCache(guildToon.getToonId()).getName() +
									" from " + oldGuildToonData.getStar() + " to " + newGuildToonData.getStar());
					oldGuildToonData.setStar(newGuildToonData.getStar());
				}

				if (newGuildToonData.getGalacticPower() != oldGuildToonData.getGalacticPower()) {
					updated = true;
					LOGGER.info(
							daoUsers.getFromCache(guildToon.getUserId()).getName() +
									" increased Galactic Power of " + daoToons.getFromCache(guildToon.getToonId()).getName() +
									" from " + oldGuildToonData.getGalacticPower() + " to " + newGuildToonData.getGalacticPower());
					oldGuildToonData.setGalacticPower(newGuildToonData.getGalacticPower());
				}

				if (updated) {
					updatedGuildToons.add(guildToon);
				}
			}
		}));

		// Newly added
		List<GuildToon> addedGuildToons = new ArrayList<>();
		guildToonsFromCSV.cellSet().forEach((cell) -> {
			// Row key = userid, column key = toonId
			if (guildToonsFromDB.get(cell.getRowKey(), cell.getColumnKey()) == null) {
				GuildToon newToon = new GuildToon();
				newToon.setUserId(cell.getRowKey());
				newToon.setToonId(cell.getColumnKey());
				newToon.setGuildToonData(cell.getValue());

				LOGGER.info(
						daoUsers.getFromCache(newToon.getUserId()).getName() +
								" has activated new toon " + daoToons.getFromCache(newToon.getToonId()).getName() +
								" with star=" + newToon.getGuildToonData().getStar() + " and Galactic Power="
								+ newToon.getGuildToonData().getGalacticPower());

				addedGuildToons.add(newToon);
			}
		});

		daoGuildToons.save(addedGuildToons);
		daoGuildToons.update(updatedGuildToons);
		daoGuildToons.delete(deletedGuildToons);

		LOGGER.info("Guild Toons reconciliation summary: Added=" + addedGuildToons.size() + ", Deleted=" + deletedGuildToons.size() + ", Updated=" + updatedGuildToons.size());
	}

	private Table<Long, Long, GuildToonData> convertToTable(List<GuildToon> allGuildToons) {
		Table<Long, Long, GuildToonData> guildToons = HashBasedTable.create();
		allGuildToons.forEach((guildToon) -> guildToons.put(guildToon.getUserId(), guildToon.getToonId(), guildToon.getGuildToonData()));
		return guildToons;
	}

	private Table<Long, Long, GuildToonData> parseGuildToons() throws IOException {
		File file = new File(dataDir + File.separatorChar + GUILD_TOONS_FILE);
		CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
		Table<Long, Long, GuildToonData> guildToons = HashBasedTable.create();
		for ( CSVRecord record: parser) {
			String username = record.get(0).trim().toLowerCase(); // In some places username is mixed case. In database, it is saved with lowercase.
			long userId = daoUsers.getFromCache(username).getId();

			int size = record.size();
			for (int i = 1; i < size; i += 3) {
				String toon = record.get(i).trim();
				long toonId = daoToons.getFromCache(toon).getId();

				int star = Integer.parseInt(record.get(i + 1).trim());
				long gp = Long.parseLong(record.get(i + 2).trim());

				GuildToonData toonData = new GuildToonData();
				toonData.setStar(star);
				toonData.setGalacticPower(gp);

				guildToons.put(userId, toonId, toonData);
			}
		}
		return guildToons;
	}
}
