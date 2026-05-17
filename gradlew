#!/bin/sh

APP_PATH="$0"
while
    APP_HOME=${APP_PATH%"${APP_PATH##*/}"}
    [ -h "$APP_PATH" ]
do
    APP_PATH=$(readlink "$APP_PATH")
    case $APP_PATH in
      /*) ;;
       *) APP_PATH=$APP_HOME$APP_PATH ;;
    esac
done

APP_HOME=$(cd "${APP_HOME:-./}" && pwd -P) || exit
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

set -- \
        "-Dorg.gradle.appname=gradlew" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

exec java $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "$@"
