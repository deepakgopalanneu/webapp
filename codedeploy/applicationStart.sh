#!/bin/bash
# Start the Spring boot app running
sudo mkdir logs
sudo touch logs/csye6225.log
java -jar ~/target/project-0.0.1-SNAPSHOT.jar > /dev/null 2> /dev/null < /dev/null &
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/config.json -s