#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
sh scripts/build.sh
mkdir -p build/test
find src/test/java -name '*.java' | sort > build/test-sources.txt
javac --release 17 -cp build/main -d build/test @build/test-sources.txt
java -cp build/main:build/test programmodel.tests.AllTests
