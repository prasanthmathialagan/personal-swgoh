from bs4 import BeautifulSoup
import requests
import time

# switch on this flag to fetch speed information (very expensive)
compute_speed=False

def toons(username, lite=True):
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
        i = i + 1;
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

            # Speed - Expensive to compute. So, don't fetch these often
            if compute_speed: # and toon_obj['galacticPower'] > 12000:
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
                        print (str(toon_obj['speed']))

                # print ("Sleeping for 2 seconds to cool down...")
                time.sleep(2)
            else:
                toon_obj['speed'] = -1 #invalid

        toons_list.append(toon_obj)

    return toons_list;

# toons("grommet", False);