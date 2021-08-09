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
             -jar gitdo.jar \
             -H:Name=gitdo
echo "done."

echo "Copying to /usr/local/Cellar which is on path. "
rm -rf /usr/local/Cellar/gitdo
mkdir /usr/local/Cellar/gitdo
mv gitodo /usr/local/Cellar/gitdo/gitdo
echo "done."
 
