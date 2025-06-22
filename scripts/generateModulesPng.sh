#!/bin/sh

SCRIPT_PATH=$(readlink -f "$0")
cd $SCRIPT_PATH/../..

./gradlew \
  dumpProjectLinks \
  dumpModuleType \
  :collateProjectLinks \
  :collateModuleTypes \
  calculateProjectTree \
  generateModulesDotFile \
  generateModulesPng
