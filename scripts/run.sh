#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
sh scripts/build.sh
java -cp build/main programmodel.ProgramModel "$@"
