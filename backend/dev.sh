#!/bin/bash
set -e

while getopts "r" flag; do
    case $flag in
        r)
            onlyRun=true
        ;;
        \?)
        # Handle invalid options
        exit 1
        ;;
    esac
done

if [ "$onlyRun" != true ] ; then
    mvn clean install -DskipTests
fi

cd server
mvn spring-boot:run
#mvn spring-boot:run -Dspring-boot.run.arguments=--logging.level.de.uni.passau.server=OFF
