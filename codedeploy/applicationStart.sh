#!/bin/bash
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/config.json -s
sudo java -jar ~/target/project-0.0.1-SNAPSHOT.jar > /home/ubuntu/csye6225.log 2> /home/ubuntu/csye6225.log < /home/ubuntu/csye6225.log &
