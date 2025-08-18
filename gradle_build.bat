@echo off
echo "Starting Gradle build with automatic Java toolchain..."
echo "This will download Java 17 if needed..."

set JAVA_OPTS=-Dorg.gradle.java.installations.auto-download=true
./gradlew clean build --info