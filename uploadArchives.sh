#!/usr/bin/env bash

./gradlew clean

./gradlew -p hyena-core uploadArchives
if [ $? -ne 0 ]; then
    echo "build hyena-core failed!!!"
    exit 99
fi

./gradlew -p hyena-spring-boot-autoconfigure uploadArchives
if [ $? -ne 0 ]; then
    echo "build hyena-spring-boot-autoconfigure failed!!!"
    exit 99
fi

./gradlew -p hyena-spring-boot-starter uploadArchives
if [ $? -ne 0 ]; then
    echo "build hyena-spring-boot-starter failed!!!"
    exit 99
fi
echo "build succeed"