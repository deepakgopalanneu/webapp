#!/bin/bash
# Stop the Spring boot app running
pkill -f 'java -jar'
sudo rm -rf target/
sudo rm -rf codedeploy/
sudo rm -f appspec.yml