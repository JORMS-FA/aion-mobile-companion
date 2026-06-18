#!/bin/sh
# Gradle wrapper script
app_path=$(dirname "$0")
APP_HOME=$(cd "$app_path" && pwd)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
