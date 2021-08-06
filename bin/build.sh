#!/usr/bin/env bash
set -euo pipefail

echo "Building uberjar: "
clj -X:uberjar
echo "done."

echo "Building executable: "
native-image --report-unsupported-elements-at-runtime \
             --initialize-at-build-time \
             --no-server \
             --enable-url-protocols=https \
             --no-fallback \
             -jar gitodo.jar \
             -H:Name=gitodo
echo "done."

echo "Copying to /usr/local/Cellar which is on path. "
rm -rf /usr/local/Cellar/gitodo
mkdir /usr/local/Cellar/gitodo
mv gitodo /usr/local/Cellar/gitodo/gitodo
echo "done."
 
