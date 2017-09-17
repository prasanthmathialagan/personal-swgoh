#! /bin/sh

echo "Remvoving old data.."
rm -rf data/*

echo "Collecting all toons.."
node alltoons.js > data/alltoons

echo "Collecting members of the guild.."
node members.js > data/guildmembers

echo "Collecting toons from the guild.."
node guildtoons.js > data/guildtoons

echo "Consolidating data.."
python main.py > output.csv

echo "Processing completed!!"