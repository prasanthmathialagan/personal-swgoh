function find_closest_match_toon(all_toons, input) {
	input = input.replace("-", " ");
	for (i in all_toons) {
		var t = all_toons[i];
		if (t.toLowerCase().indexOf(input.toLowerCase()) >= 0) {
			return t;
		}
	}
	return "";
}

function find_closest_match_member(all_members, input) {
	input = input.replace("'s", "");
	for (i in all_members) {
		var t = all_members[i];
		if (t.toLowerCase().indexOf(input.toLowerCase()) >= 0) {
			return t;
		}
	}
	return "";
}

function send_speed(member, toon, bot, channelID) {
	var con = get_db_connection();
	con.connect(function(err) {
		var speed = -1;
	  	if (err) {
			console.log(err);
			bot.sendMessage({
				to: channelID,
				message: 'Internal error. Try again after some time!'
			});
	  	} else {
			con.query("select gt.speed from Users u INNER JOIN GuildToons gt ON u.id = gt.userId INNER JOIN Toons t ON gt.toonId = t.id AND t.name = ? AND u.name = ?", [toon, member], function (err, result, fields) {
				if (err) {
					console.log(err);
					bot.sendMessage({
						to: channelID,
						message: 'Internal error. Try again after some time!'
					});
				} else {
					if (result.length == 0) {
						bot.sendMessage({
							to: channelID,
							message: 'No results found!'
						});
					} else {
						for (i in result) {
							var o = result[i];
							speed = o['speed'];
							bot.sendMessage({
								to: channelID,
								message: "Speed of " + member + "'s " + toon + " is " + speed
							});
							break;
						}
					}
				}
			});
			con.end();
		}
	});

}

// It is the responsibility of the caller to make sure the message sent in a batch_size does not exceed 2000 characters.
function send_as_table (data, batch_size, bot, channelID) {
	var options = {
      skinny: true,
      intersectionCharacter: "x"
    };

	var chunk=[]
	for (var i=0; i < data.length; i++) {
		chunk.push(data[i]);
		if (chunk.length == batch_size) {
			// Write the data
			var table = asciitable(options, chunk);
			chunk = [];
			bot.sendMessage({
				to: channelID,
				message: "```" + table + "```"
			});
		}
	}

	if (chunk.length > 0) {
		var table = asciitable(options, chunk);
		bot.sendMessage({
			to: channelID,
			message: "```" + table + "```"
		});
	}
}

bot.on('message', function (user, userID, channelID, message, evt) {
    // Our bot needs to know if it will execute a command
    // It will listen for messages that are addressed to it using @greeter
    if (message.indexOf('@' + bot.id) > -1) {
        message = message.trim().replace("<@" + bot.id + "> ", '');
        var args = message.trim().split(' ');
        var cmd = args[0].toLowerCase();
       
        args = args.splice(1);
        switch(cmd) {
            default:
            	player="";
                toon="";
				toon_found=0;
				player_found=0;
                for (i in args) {
                	if (player_found == 1 && toon_found == 1) {
						break;
                	}

					var n = args[i].trim().toLowerCase();
					if (n === "is" || n === "and" || n === "the" || n === "of") {
						continue;
					}

					if (n.length != 0) {
						if (toon_found == 0) {
							var t = find_closest_match_toon(toons_list, n);
							if (t.length != 0) {
								toon = t;
								toon_found = 1;
								continue;
							}
						}

						if (player_found == 0) {
							var t = find_closest_match_member(members_list, n);
							if (t.length != 0) {
								player = t;
								player_found = 1;
								continue;
							}
						}
					}
				}

				msg = message.toLowerCase();

				console.log(player);
                console.log(toon);

            	// Find the speed of a particular member's toon
            	if (msg.indexOf("speed") >= 0) {
            		if (player_found == 1 && toon_found == 1) {
            			// Get the speed from database
            			send_speed(player, toon, bot, channelID);
            		} else if (player_found == 1 && toon_found == 0 ) {
						bot.sendMessage({
							to: channelID,
							message: 'Could not find the specified toon for ' + player + "!!!"
						});
            		} else if (player_found == 0 && toon_found == 1 ) {
						bot.sendMessage({
							to: channelID,
							message: toon + " of who??"
						});
					} else {
						bot.sendMessage({
							to: channelID,
							message: 'Not enough information to find speed!!!'
						});
            		}
            	}

            	if (msg.indexOf("potency") >= 0) {
					bot.sendMessage({
						to: channelID,
						message: 'My master has not taught me how to find potency!!!'
					});
				}

				if (msg.indexOf("tenacity") >= 0) {
					bot.sendMessage({
						to: channelID,
						message: 'My master has not taught me how to find tenacity!!!'
					});
				}

				if (msg.indexOf("health") >= 0) {
					bot.sendMessage({
						to: channelID,
						message: 'My master has not taught me how to find health!!!'
					});
				}

				if (msg.indexOf("protection") >= 0) {
					bot.sendMessage({
						to: channelID,
						message: 'My master has not taught me how to find protection!!!'
					});
				}

				if (msg.indexOf("critical") >= 0 && msg.indexOf("chance") >= 0) {
					bot.sendMessage({
						to: channelID,
						message: 'My master has not taught me how to find Critical Chance!!!'
					});
				}

				if (msg.indexOf("critical") >= 0 && msg.indexOf("damage") >= 0) {
					bot.sendMessage({
						to: channelID,
						message: 'My master has not taught me how to find Critical Damage!!!'
					});
				}
            // Just add any case commands if you want to..
         }
     }
});
