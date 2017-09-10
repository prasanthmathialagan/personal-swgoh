package org.prasanth.swgoh;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.prasanth.swgoh.dao.DAOUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by prasanthmathialagan on 9/10/17.
 */
public class Controller {

	private static final Logger LOGGER = Logger.getLogger(Controller.class);

	public static final String GUILD_MEMBERS_FILE = "guildmembers";

	@Autowired
	private DAOUsers daoUsers;

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
					// TODO: Check if data is getting updated automatically
					updatedUsers.add(oldUser);
				}
			}
		});

		daoUsers.saveUsers(newUsers);
		daoUsers.deleteUsers(deletedUsers);
		daoUsers.updateUsers(updatedUsers);

		LOGGER.info("Reconciliation summary: New=" + newUsers.size() + ", Deleted=" + deletedUsers.size() + ", Updated=" + updatedUsers.size());
	}

	private List<User> parseUsers() throws IOException {
		File file = new File(dataDir + File.separatorChar + GUILD_MEMBERS_FILE);
		CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
		List<User> users = new ArrayList<User>();
		for ( CSVRecord record: parser) {
			User user = new User();
			user.setUserId(record.get(0).trim());
			user.setName(record.get(1).trim());
			users.add(user);
		}
		return users;
	}
}
