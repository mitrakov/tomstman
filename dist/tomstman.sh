#!/bin/bash

if ! [ `which java` ]; then
    echo "Please install java"
    exit 1
fi

if [ ! -f settings.ini ]; then
  cat > settings.ini <<EOF
[requests]
item = GET example.com\u0e4fhttps\://example.com\u0e4fGET\u0e4f{\n\n}
item = POST example.com\u0e4fhttps\://example.com\u0e4fPOST\u0e4f{\n\n}
item = GET google.com\u0e4fhttps\://google.com\u0e4fGET\u0e4f{\n\n}
EOF
fi

DIR="$(dirname "$(readlink "$0")")"
java -jar $DIR/tomstman.jar
