#!/bin/bash
# Run the Candy Crush game
# Usage: ./run.sh

set -e

JFX_LIB="$HOME/.m2/repository/org/openjfx"
MODULE_PATH="$JFX_LIB/javafx-base/25.0.2:$JFX_LIB/javafx-graphics/25.0.2:$JFX_LIB/javafx-controls/25.0.2"

mvn compile -q 2>&1

java \
  --module-path "$MODULE_PATH" \
  --add-modules javafx.controls \
  --enable-native-access=javafx.graphics \
  --add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
  -cp target/classes \
  com.ooplab.candycrush.Main
