import discord
import asyncio
import MySQLdb
from terminaltables import AsciiTable

def get_db_connection():
    return MySQLdb.connect(host="localhost", user="root", passwd="", db="swgoh")

toons = ""
toons_list= []
def populate_toons():
    global toons, toons_list

    db = get_db_connection()
    cur = db.cursor()
    cur.execute("SELECT name FROM Toons")
    for row in cur.fetchall():
        toons_list.append(row[0])
        toons = toons + row[0] + "\n"

    cur.close()

populate_toons()

members_table = None
members_list = []
def populate_members():
    global members_table, members_list

    db = get_db_connection()
    cur = db.cursor()
    cur.execute("SELECT userId,name FROM Users ORDER by name")
    table_data = [['UserID', 'Name']]
    for row in cur.fetchall():
        members_list.append(row[1])
        table_data.append([row[0], row[1]])

    members_table = AsciiTable(table_data)
    cur.close()

populate_members()

def strip_spaces_and_join(args):
    output = ""
    for arg in args:
        arg = arg.strip()
        if len(arg) != 0:
            if len(output) == 0:
                output = arg
            else:
                output = output + " " + arg
    return output

def find_closest_match(name, list):
    for l in list:
        if name.lower() in l.lower():
            return l

    return None

# Returns None or List
def toons_for_member(name):
    sql = "SELECT @rn:=@rn+1 AS no, name, star, galacticPower, speed FROM " \
          + "(SELECT t.name, gt.star, gt.galacticPower, if(gt.speed = 0, 'N/A', speed) as speed FROM Users u " \
          + "INNER JOIN GuildToons gt ON u.id = gt.userId AND u.name='%s' " \
          + "INNER JOIN Toons t ON gt.toonId = t.id ORDER BY gt.star DESC, gt.galacticPower DESC) t1, " \
          + " (SELECT @rn:=0) t2"

    db = get_db_connection()
    cur = db.cursor()
    cur.execute(sql % (name,))

    table_data=[]
    for row in cur.fetchall():
        table_data.append(list(row))

    cur.close()

    return table_data

def fn_players_with_toon(name):
    sql = "SELECT @rn:=@rn+1 AS no, name, star, galacticPower FROM " \
          + "(SELECT u.name, gt.star, gt.galacticPower FROM Users u " \
          + "INNER JOIN GuildToons gt ON u.id = gt.userId " \
          + "INNER JOIN Toons t ON gt.toonId = t.id AND t.name = '%s' " \
          + "ORDER BY gt.star DESC, gt.galacticPower DESC) t1, " \
          + " (SELECT @rn:=0) t2"

    db = get_db_connection()
    cur = db.cursor()
    cur.execute(sql % (name,))

    table_data=[]
    for row in cur.fetchall():
        table_data.append(list(row))

    cur.close()

    return table_data

@asyncio.coroutine
def send_as_table(data, headers, batch_size, channel):
    chunk = []
    for row in data:
        chunk.append(row)
        if len(chunk) == batch_size:
            chunk.insert(0, headers)
            ascii = AsciiTable(chunk)
            yield from client.send_message(channel, '```' + ascii.table + '```')
            chunk = []

    if len(chunk) > 0:
        chunk.insert(0, headers)
        ascii = AsciiTable(chunk)
        yield from client.send_message(channel, '```' + ascii.table + '```')

client = discord.Client()

@client.event
@asyncio.coroutine
def on_ready():
    print('Logged in as')
    print(client.user.name)
    print(client.user.id)
    print('------')

@client.event
@asyncio.coroutine
def on_message(message):
    bot_handle = '<@' + client.user.id + '>'
    if not message.content.startswith(bot_handle):
        return

    msg = message.content.replace(bot_handle, '').strip()
    words = msg.split(' ')
    if len(words) == 0:
        return

    cmd = words[0].lower()
    if cmd == 'help':
        output = "Command \n" \
                " **help** - Displays all the commands\n" \
                " **ping** - Responds with a Pong\n" \
                " **hello** - Greets you\n" \
                " **toons** - Lists all the toons\n" \
                " **members** - Lists all the Guild members\n" \
                " **guild-member <name>** - Lists the toons for the member\n" \
                " **member-toon <toon name>** - Lists the members with the given toon\n"
        yield from client.send_message(message.channel, output)
    elif cmd == 'ping':
        yield from client.send_message(message.channel, "Pong!")
    elif cmd == 'hello':
        yield from client.send_message(message.channel, 'Hello <@' + str(message.author.id) + '>')
    elif cmd == 'toons':
        yield from client.send_message(message.channel, '```' + toons + '```')
    elif cmd == 'members':
        yield from client.send_message(message.channel, '```' + members_table.table + '```')
    elif cmd == 'guild-member':
        member_name = strip_spaces_and_join(words[1:])
        if len(member_name) == 0:
            yield from client.send_message(message.channel, "Must supply a name. Usage: @greeter guild-member celessa")
            return

        closest_match = find_closest_match(member_name, members_list)
        if closest_match is None:
            yield from client.send_message(message.channel, 'No results found for ' + member_name + '. Check the supplied name. Use @greeter members to get correct name')
            return
        else:
            yield from client.send_message(message.channel, 'Closest match is ' + closest_match + '.')

        toons_for_player = toons_for_member(closest_match)
        if toons_for_player is None or len(toons_for_player) == 0:
            yield from client.send_message(message.channel, 'Toons information is not available for ' + member_name + '.')
            return
        else:
            yield from send_as_table(toons_for_player, ['No', 'Toon', 'Star', 'GP', 'Speed'], 30, message.channel)
    elif cmd == 'member-toon':
        toon_name = strip_spaces_and_join(words[1:])
        if len(toon_name) == 0:
            yield from client.send_message(message.channel, "Must supply a toon name. Usage: @greeter member-toon lobot")
            return

        closest_match = find_closest_match(toon_name, toons_list)
        if closest_match is None:
            yield from client.send_message(message.channel, 'No results found for ' + toon_name + '. Check the supplied name. Use @greeter toons to get correct name')
            return
        else:
            yield from client.send_message(message.channel, 'Closest match is ' + closest_match + '.')

        players_with_toon = fn_players_with_toon(closest_match)
        if players_with_toon is None or len(players_with_toon) == 0:
            yield from client.send_message(message.channel, 'Shame!! There is no player in the guild with ' + toon_name + '.')
            return
        else:
            yield from send_as_table(players_with_toon, ['No', 'Member', 'Star', 'GP'], 30, message.channel)

client.run('MzYwNTA3NTM5OTc4MzIxOTIw.DKWkWA.yx-yMLKqkEegLnthB3wYkNmvXS8')