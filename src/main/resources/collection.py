from bs4 import BeautifulSoup
import requests

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

    for toon in toons:
        toon_obj = {}
        toon_obj['code'] = toon.find("a")['href'].split("/")[4]
        toon_obj['name'] = toon.find("a").find("img")['alt']

        if not lite:
            toon_obj['star'] = 7 - len(toon.find_all(class_="star-inactive"))

            parent_div = toon.find_parent("div")
            heavytoon = parent_div.find("div", class_="collection-char-gp")
            toon_obj['galacticPower'] = int(heavytoon['title'].replace("Power","").split("/")[0].strip().replace(",",""))

        toons_list.append(toon_obj)

    return toons_list;