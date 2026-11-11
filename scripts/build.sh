#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/main
find src/main/java -name '*.java' | sort > build/main-sources.txt
javac --release 17 -d build/main @build/main-sources.txt
printf 'Built plain Java main classes\n'
