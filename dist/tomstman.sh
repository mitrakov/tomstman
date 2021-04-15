#!/bin/bash

if ! [ `which java` ]; then
    echo "Please install java"
    exit 1
fi

DIR="$(dirname "$(readlink "$0")")"
java -jar $DIR/tomstman.jar
