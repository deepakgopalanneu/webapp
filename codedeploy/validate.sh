#!/usr/bin/env bash

while [ true ]
do
    sleep 3s
    if [ "$(curl -s http://localhost:8080/actuator/health)" = '{"status":"UP"}' ]
    then
        exit 0
    else
        echo "check server is running?"
        sleep 3s
    fi
done