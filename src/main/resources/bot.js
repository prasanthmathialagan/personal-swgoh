var Discord = require('discord.io');
var logger = require('winston');
var auth = require('./auth.json');
var fs = require('fs')

// Configure logger settings
logger.remove(logger.transports.Console);
logger.add(logger.transports.Console, {
    colorize: true
});
logger.level = 'debug';
// Initialize Discord Bot
var bot = new Discord.Client({
   token: auth.token,
   autorun: true
});

var mysql = require('mysql');

function get_db_connection() {
    var con = mysql.createConnection({
      host: "localhost",
      user: "root",
      password: "",
      database: "swgoh"
    });
    return con;
}

toons = "";
toons_list = [];
var toons_con = get_db_connection();
toons_con.connect(function(err) {
    if (err) {
        throw err;
    } 

    toons_con.query("SELECT name FROM Toons", function (err, result, fields) {
        if (err) {
            throw err;
        }

        for (i in result) {
            var toon = result[i];
            toons_list.push(toon['name']);
            toons = toons + toon['name'] + "\n";
        }
    });

    toons_con.end();
});

var members_con = get_db_connection();
members = "";
members_con.connect(function(err) {
    if (err) {
        throw err;
    }

    members_con.query("SELECT userId,name FROM Users ORDER by name", function (err, result, fields) {
        if (err) {
            throw err;
        }

        members="UserID, name\n----------------\n";
        for (i in result) {
            var member = result[i];
            members = members + member['userId'] + ", " + member['name'] + "\n";
        }
    });

    members_con.end();
});

bot.on('ready', function (evt) {
    logger.info('Connected');
    logger.info('Logged in as: ');
    logger.info(bot.username + ' - (' + bot.id + ')');
});
bot.on('message', function (user, userID, channelID, message, evt) {
    // Our bot needs to know if it will execute a command
    // It will listen for messages that are addressed to it using @greeter
    if (message.indexOf('@' + bot.id) > -1) {
        message = message.trim().replace("<@" + bot.id + "> ", '');
        var args = message.trim().split(' ');
        var cmd = args[0];
       
        args = args.splice(1);
        switch(cmd) {
            case 'help':
                msg = "Commands \n" + 
                      " **help** - Displays all the commands\n" +
                      " **ping** - Responds with a Pong\n" +
                      " **hello** - Greets you\n" +
                      " **toons** - Lists all the toons\n" + 
                      " **members** - Lists all the Guild members\n" +
                      " **guild-member <name>** - Lists the toons for the member\n" +
                      " **member-toon <toon name>** - Lists the members with the given toon\n";
                bot.sendMessage({
                    to: channelID,
                    message: msg
                });
            break;
            // !ping
            case 'ping':
                bot.sendMessage({
                    to: channelID,
                    message: 'Pong!'
                });
            break;
            case 'hello':
                bot.sendMessage({
                    to: channelID,
                    message: 'Hello @' + user + '!'
                });
            break;
            case 'toons':
                bot.sendMessage({
                    to: channelID,
                    message: toons
                }); 
            break;
            case 'members':
                bot.sendMessage({
                    to: channelID,
                    message: members
                });
            break;
            case 'guild-member':
                name = "";
                for (i in args) {
                    var n = args[i];
                    if (n.length != 0) {
                        if (name.length == 0) {
                            name = n;
                        } else {
                            name = name + " " + n; 
                        }
                    }
                }
                if (name.length == 0) {
                    bot.sendMessage({
                        to: channelID,
                        message: 'Must supply a name. Usage: @greeter guild-member Celessa Laike'
                    });
                } else {
                    var con = get_db_connection();
                    con.connect(function(err) {
                      if (err) {
                        console.log(err);
                        bot.sendMessage({
                            to: channelID,
                            message: 'Internal error. Try again after some time!'
                        });
                      } else {
                            console.log(name);
                            con.query("select t.name, gt.star, gt.galacticPower, if(gt.speed = 0, 'N/A', speed) as speed from Users u INNER JOIN GuildToons gt ON u.id = gt.userId AND u.name=? INNER JOIN Toons t ON gt.toonId = t.id ORDER BY gt.star DESC, gt.galacticPower DESC", [name], function (err, result, fields) {
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
                                            message: 'No results found for ' + name + '. Check the supplied name. Use !members to get correct name'
                                        });
                                    } else {
                                        output = "Toon, star, GP, Speed\n----------------------\n"
                                        for (i in result) {
                                            var o = result[i];
                                            var toon = o['name'] + ", " + o['star'] + ", " + o['galacticPower'] + ", " + o['speed'] + "\n";
                                            if ((toon.length + output.length) > 1990) {
                                                bot.sendMessage ({
                                                    to: channelID,
                                                    message: output
                                                })
                                                output = "";
                                            }
                                            output = output + toon;
                                        }
                                        if (output.length > 0) {
                                            bot.sendMessage ({
                                                to: channelID,
                                                message: output
                                            })
                                        }
                                    }
                                }
                            });
                            con.end();
                        }
                    });
                }
            break;
            case 'member-toon':
            //
                name = "";
                for (i in args) {
                    var n = args[i];
                    if (n.length != 0) {
                        if (name.length == 0) {
                            name = n;
                        } else {
                            name = name + " " + n; 
                        }
                    }
                }

				console.log(name);
			   	name_found = 1;
			   	closest_match = 0;
               	if (name.length == 0) {
               		name_found = 0;
                    bot.sendMessage({
                        to: channelID,
                        message: 'Must supply a toon name. Usage: @greeter member-toon Ewok Scout'
                    });    
                } else if (toons.indexOf(name) < 0) {
					// Find the closest match
					for (i in toons_list) {
						var t = toons_list[i];
						if (t.toLowerCase().indexOf(name.toLowerCase()) >= 0) {
							console.log("Closest match to " + name + " is " + t);
							name=t;
							name_found = 1;
							closest_match = 1;
							break;
						}
					}

					if (closest_match == 0) {
						name_found == 0;
						bot.sendMessage({
							to: channelID,
							message: 'No results found for ' + name + '. Check the supplied toon name. Use @greeter toons to get correct name'
						});
					}
				}

				if (name_found == 1) {
                    var con = get_db_connection();
                    con.connect(function(err) {
                      if (err) {
                        console.log(err);
                        bot.sendMessage({
                            to: channelID,
                            message: 'Internal error. Try again after some time!'
                        });
                      } else {
                            con.query("select u.name, gt.star, gt.galacticPower from Users u INNER JOIN GuildToons gt ON u.id = gt.userId INNER JOIN Toons t ON gt.toonId = t.id AND t.name = ? ORDER BY gt.star DESC, gt.galacticPower DESC", [name], function (err, result, fields) {
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
                                            message: 'Shame!! There is no player in the guild with ' + name + '.'
                                        });
                                    } else {
                                        output = "Member, star, GP\n-------------------\n"
                                        for (i in result) {
                                            var o = result[i];
                                            var toon = o['name'] + ", " + o['star'] + ", " + o['galacticPower'] + "\n";
                                            if ((toon.length + output.length) > 1990) {
                                                bot.sendMessage ({
                                                    to: channelID,
                                                    message: output
                                                })
                                                output = "";
                                            }
                                            output = output + toon;
                                        }
                                        if (output.length > 0) {
                                            bot.sendMessage ({
                                                to: channelID,
                                                message: output
                                            })
                                        }
                                    }
                                }
                            });
                            con.end();
                        }
                    });
                }
            //
            break;
            // Just add any case commands if you want to..
         }
     }
});
