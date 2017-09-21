from bs4 import BeautifulSoup
import requests

def parseUser(username):
    url = "https://swgoh.gg/u/" +username
    r = requests.get(url)
    html_doc = r.text
    soup = BeautifulSoup(html_doc, 'html.parser')

    # username
    username = soup.select("body > div.container.p-t-md > div.content-container > div.content-container-aside \
                > div.panel.panel-default.panel-profile.m-b-sm > div.panel-body \
                > h5 > a")[0].text

    # level
    level = soup.select("body > div.container.p-t-md > div.content-container > div.content-container-aside \
                > div.panel.panel-default.panel-profile.m-b-sm > div.panel-body \
                > ul > li:nth-of-type(3) > h5")[0].text

    # Guild name
    guildname = soup.select("body > div.container.p-t-md > div.content-container > div.content-container-aside \
                > div.panel.panel-default.panel-profile.m-b-sm > div.panel-body \
                > p:nth-of-type(1) > strong > a")[0].text

    # Guild url
    guildurl = soup.select("body > div.container.p-t-md > div.content-container > div.content-container-aside \
                > div.panel.panel-default.panel-profile.m-b-sm > div.panel-body \
                > p:nth-of-type(1) > strong > a")[0]['href']

    return { \
        "username" : username,
        "level" : int(level),
        "guild" : guildname,
        "guildurl" : guildurl
    }

def getGuildUsers(guildurl):
    url = "https://swgoh.gg" + guildurl
    r = requests.get(url)
    html_doc = r.text
    soup = BeautifulSoup(html_doc, 'html.parser')

    base = "body > div.container.p-t-md > div.content-container > div.content-container-primary.character-list " \
           "> ul > li.media.list-group-item.p-0.b-t-0 > div > table > tbody > tr > td > a"

    members_list=[]

    members = soup.select(base)
    for member in members:
        username = member['href'].split("/")[2]
        name = member.find("strong").text
        members_list.append({"name":name, "username":username})

    return members_list