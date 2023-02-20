#!/bin/sh
VERSION=$(cat src/org/nschmidt/ldparteditor/i18n/Version.properties | grep 'VERSION' | sed 's/^.*= //')
NEXT_VERSION=$(echo $VERSION | awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')
echo "current version $VERSION"
echo "next    version $NEXT_VERSION"
sed -i "s/$VERSION/$NEXT_VERSION/g" "build-linux.xml"
sed -i "s/$VERSION/$NEXT_VERSION/g" "build-macos.xml"
sed -i "s/$VERSION/$NEXT_VERSION/g" "build-windows.xml"
sed -i "s/$VERSION/$NEXT_VERSION/g" "src/org/nschmidt/ldparteditor/i18n/Version.properties"
git add build-linux.xml
git add build-macos.xml
git add build-windows.xml
git add src/org/nschmidt/ldparteditor/i18n/Version.properties
git commit -m "Updated version number to $NEXT_VERSION"
echo "Updated version number to $NEXT_VERSION"
