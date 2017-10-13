import users, collection
import json
import os, shutil

print "Removing old data..."
folder = 'data'
for the_file in os.listdir(folder):
    file_path = os.path.join(folder, the_file)
    try:
        if os.path.isfile(file_path):
            os.unlink(file_path)
    except Exception as e:
        print(e)

# locations
toons_output = "data/alltoons.json"
guild_members_output = "data/guildmembers.json"
guild_toons_output = "data/guildtoons.json"

# -----------------------------------------------------------------------
# Get the list of all toons
# mrrush --> This guys is a top arena player and has all the toons.
top_player = "mrrush"
print "Collecting all toons..."
toons = collection.toons(top_player)
with open(toons_output, 'w+') as outfile:
    json.dump(toons, outfile)

# -----------------------------------------------------------------------
# Get the list of members of the guild
guild_leader = users.parseUser("prasanthswgoh")
guild_url = guild_leader['guildurl']

print "Collecting members of the guild..."
guild_members = users.getGuildUsers(guild_url)
with open(guild_members_output, 'w+') as outfile:
    json.dump(guild_members, outfile)

# -----------------------------------------------------------------------

# Get the toons of all the guild members
print "Collecting toons from the guild..."

guild_toons = []
i=0
for member in guild_members:
    i = i + 1
    username = member['username']
    print("Fetching data for " + username + " (" + str(i) + "/" + str(len(guild_members)) + ")")
    gt = collection.toons(username, False)
    guild_toons.append({"username":username, "toons":gt})

with open(guild_toons_output, 'w+') as outfile:
    json.dump(guild_toons, outfile)