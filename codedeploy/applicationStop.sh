#!/bin/bash
# Stop the Spring boot app running
sudo pkill -f 'java -jar'
sudo rm -rf target/
sudo rm -rf codedeploy/
sudo rm -f appspec.yml
sudo rm -rf logs/