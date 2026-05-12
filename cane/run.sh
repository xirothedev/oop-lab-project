#!/bin/bash
# Run the Candy Crush game.
# Usage: ./run.sh

set -e

JFX_VERSION="25.0.2"
JFX_LIB="$HOME/.m2/repository/org/openjfx"

# Detect platform classifier so we pick the right native jar.
case "$(uname -s)-$(uname -m)" in
    Darwin-arm64) CLASSIFIER="mac-aarch64" ;;
    Darwin-x86_64) CLASSIFIER="mac" ;;
    Linux-x86_64) CLASSIFIER="linux" ;;
    Linux-aarch64) CLASSIFIER="linux-aarch64" ;;
    *) CLASSIFIER="" ;;
esac

modules=(javafx-base javafx-graphics javafx-controls javafx-media)
MODULE_PATH=""
for m in "${modules[@]}"; do
    api_jar="$JFX_LIB/$m/$JFX_VERSION/$m-$JFX_VERSION.jar"
    native_jar="$JFX_LIB/$m/$JFX_VERSION/$m-$JFX_VERSION-$CLASSIFIER.jar"
    if [ -n "$CLASSIFIER" ] && [ -f "$native_jar" ]; then
        MODULE_PATH="$MODULE_PATH:$native_jar"
    elif [ -f "$api_jar" ]; then
        MODULE_PATH="$MODULE_PATH:$api_jar"
    else
        echo "Missing JavaFX module jar: $m $JFX_VERSION" >&2
        echo "Run 'mvn dependency:resolve' first." >&2
        exit 1
    fi
done
MODULE_PATH="${MODULE_PATH#:}"

mvn compile -q

java \
  --module-path "$MODULE_PATH" \
  --add-modules javafx.controls,javafx.media \
  --enable-native-access=javafx.graphics,javafx.media \
  --add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
  -cp target/classes \
  com.ooplab.candycrush.Main
