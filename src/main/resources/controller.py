import users, collection
import json

# locations
toons_output = "data/alltoons.json"
guild_members_output = "data/guildmembers.json"
guild_toons_output = "data/guildtoons.json"

# -----------------------------------------------------------------------
# Get the list of all toons
# mrrush --> This guys is a top arena player and has all the toons.
top_player = "mrrush"
toons = collection.toons(top_player)
print "Collecting all toons..."
with open(toons_output, 'w+') as outfile:
    json.dump(toons, outfile)

# -----------------------------------------------------------------------
# Get the list of members of the guild
guild_leader = users.parseUser("prasanthswgoh")
guild_url = guild_leader['guildurl']

print "Collecting members of the guild.."
guild_members = users.getGuildUsers(guild_url)
with open(guild_members_output, 'w+') as outfile:
    json.dump(guild_members, outfile)

# -----------------------------------------------------------------------

# Get the toons of all the guild members
print "Collecting toons from the guild.."

guild_toons = {}
for member in guild_members:
    username = member['username']
    print("Fetching data for " + username)
    gt = collection.toons(username, False)
    guild_toons[username] = gt

with open(guild_toons_output, 'w+') as outfile:
    json.dump(guild_toons, outfile)