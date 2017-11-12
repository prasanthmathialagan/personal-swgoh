from bs4 import BeautifulSoup
import requests
import time
import math

# switch on this flag to fetch speed information (very expensive)
compute_speed=True
speed_for_members=['starwarsgeek', 'frostdragon04', 'hollowgrey']

def toons(username, lite=True):
    global compute_speed, speed_for_members

    url = "https://swgoh.gg/u/" +username + "/collection"
    r = requests.get(url)
    html_doc = r.text
    soup = BeautifulSoup(html_doc, 'html.parser')

    base = "body > div.container.p-t-md > div.content-container > div.content-container-primary.character-list > ul \
    > li.media.list-group-item.p-a.collection-char-list > div > div > div > div.player-char-portrait"

    # toons
    toons = soup.select(base)
    toons_list=[]

    i=0
    for toon in toons:
        i = i + 1
        print("Processing " + str(i) + "/" + str(len(toons)))
        toon_obj = {}
        code = toon.find("a")['href'].split("/")[5]
        toon_obj['code'] = code
        toon_obj['name'] = toon.find("a").find("img")['alt']

        if not lite:
            toon_obj['star'] = 7 - len(toon.find_all(class_="star-inactive"))

            parent_div = toon.find_parent("div")
            heavytoon = parent_div.find("div", class_="collection-char-gp")
            toon_obj['galacticPower'] = int(heavytoon['title'].replace("Power","").split("/")[0].strip().replace(",",""))

            toon_obj['health'] = -1;
            toon_obj['protection'] = -1;
            toon_obj['speed'] = -1;
            toon_obj['tenacity'] = -1;
            toon_obj['potency'] = -1;
            toon_obj['criticalChance'] = -1;
            toon_obj['criticalDamage'] = -1;

            # Speed - Expensive to compute. So, don't fetch these often
            if compute_speed and username in speed_for_members: # and toon_obj['galacticPower'] > 12000:
                print ("--------> Collecting speed data for " + code)
                toonInfoUrl = url + "/" + code
                r2 = requests.get(toonInfoUrl)
                html_doc2 = r2.text
                soup2 = BeautifulSoup(html_doc2, 'html.parser')
                stats = soup2.select("body > div.container.p-t-sm > div.content-container > div.content-container-primary-aside \
                                     > ul > li.media.list-group-item.p-sm > div.media-body > div.media-body > div.pc-stat")
                for stat in stats:
                    label = stat.find("span", class_="pc-stat-label").text
                    if label == "Speed":
                        toon_obj['speed'] = int(stat.find("span", class_="pc-stat-value").text)
                        print toon_obj['speed']
                    if label == "Health":
                        toon_obj['health'] = int(stat.find("span", class_="pc-stat-value").text)
                    if label == "Protection":
                        toon_obj['protection'] = int(stat.find("span", class_="pc-stat-value").text)
                    if label == "Potency":
                        toon_obj['potency'] = math.floor(float(stat.find("span", class_="pc-stat-value").text.replace("%", "")))
                    if label == "Tenacity":
                        toon_obj['tenacity'] = math.floor(float(stat.find("span", class_="pc-stat-value").text.replace("%", "")))
                    if label == "Physical Critical Chance":
                        toon_obj['criticalChance'] = math.floor(float(stat.find("span", class_="pc-stat-value").text.replace("%", "")))
                    if label == "Critical Damage":
                        toon_obj['criticalDamage'] = math.floor(float(stat.find("span", class_="pc-stat-value").text.replace("%", "")))
                # print ("Sleeping for 2 seconds to cool down...")
                time.sleep(5)

        toons_list.append(toon_obj)

    return toons_list;

# print(str(toons("hollowgrey", False)));