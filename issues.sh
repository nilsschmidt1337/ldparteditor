#!/bin/sh
#Delete the fine grained token after it was used!
read -p "Token: " token
bearer="Authorization: Bearer $token"
echo -n "[" > issues.json
for i in {1..11}
do
    curl -L \
      -H "Accept: application/vnd.github+json" \
      -H "$bearer" \
      -H "X-GitHub-Api-Version: 2022-11-28" \
      "https://api.github.com/repos/nilsschmidt1337/ldparteditor/issues?state=all&per_page=100&page=$i" >> issues.json
    echo -n "," >> issues.json
    echo "Loaded page $i of 11."
    sleep 1
done
echo -n "[]]" >> issues.json
git add issues.json
git commit -m "Backup for issue information."
echo "Did a backup of all issues."
