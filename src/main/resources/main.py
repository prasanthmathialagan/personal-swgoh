import csv

# Map of username to description

username_to_desc = {}
with open("data/guildmembers", "rb") as csvfile:
	guildmembers_reader = csv.reader(csvfile, delimiter=',')
	for row in guildmembers_reader:
		username_to_desc[row[0].strip()] = row[1].strip();

# All toons set

all_toons = []
with open("data/alltoons", "rb") as csvfile:
	alltoons_reader = csv.reader(csvfile, delimiter=',')
	for row in alltoons_reader:
		all_toons.append(row[0].strip())
all_toons.sort()

header = "Member"
for toon in all_toons:
	header = header + ", " + toon + ", Galactic Power" 
print header

def enumerate2(xs, start=0, step=1):
    for x in xrange(start, len(xs), step):
        yield (start, xs[x])
        start += step

with open("data/guildtoons", "rb") as csvfile:
	csv_reader = csv.reader(csvfile, delimiter=',')
	for row in csv_reader:
		guild_member=row[0]
		guild_member_toons={}
		guild_member_toons_gp={}
		for index, elem in enumerate2(row, 1, 3):
			toon = elem.strip()
			guild_member_toons[toon] = row[index+1]
			guild_member_toons_gp[toon] = int(row[index+2])

		result = username_to_desc.get(guild_member, guild_member)

		for toon in all_toons:
			result = result + ", " + guild_member_toons.get(toon, "-") + ", " + str(guild_member_toons_gp.get(toon, "-"))

		print result