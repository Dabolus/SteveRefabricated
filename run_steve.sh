#!/bin/bash

# Steve AI Refabricated Mod - Launch Script
# This script sets up Java and runs Minecraft with the mod

cd "$(dirname "$0")"

echo "🎮 Steve AI Refabricated Mod - Launcher"
echo "================================"
echo ""

# Set up Java
export JAVA_HOME="$PWD/jdk-21.0.4/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "✅ Java 21 ready"
echo ""
echo "Starting Minecraft..."
echo "⏳ First launch will download assets (~1-2 minutes)"
echo ""

# Run Minecraft
./gradlew runClient --no-daemon

echo ""
echo "================================"
echo "Minecraft closed. Thanks for testing!"
