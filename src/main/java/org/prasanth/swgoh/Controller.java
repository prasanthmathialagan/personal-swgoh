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

import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.prasanth.swgoh.dao.DAOToons;
import org.prasanth.swgoh.dao.DAOUsers;
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

	@Autowired
	private DAOUsers daoUsers;

	@Autowired
	private DAOToons daoToons;

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
				}
			}
		});

		daoUsers.saveUsers(newUsers);
		daoUsers.deleteUsers(deletedUsers);
		daoUsers.updateUsers(updatedUsers);

		LOGGER.info("Users reconciliation summary: New=" + newUsers.size() + ", Deleted=" + deletedUsers.size() + ", Updated=" + updatedUsers.size());
	}

	private List<User> parseUsers() throws IOException {
		File file = new File(dataDir + File.separatorChar + GUILD_MEMBERS_FILE);
		CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
		List<User> users = new ArrayList<>();
		for ( CSVRecord record: parser) {
			User user = new User();
			user.setUserId(record.get(0).trim());
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
}
