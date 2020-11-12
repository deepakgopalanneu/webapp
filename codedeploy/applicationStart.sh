#!/bin/bash
# Start the Spring boot app running
#sudo mkdir /home/ubuntu/logs
#sudo touch /home/ubuntu/logs/csye6225.log
#sudo chmod 666 /home/ubuntu/logs/csye6225.log
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/config.json -s
sudo java -jar /home/ubuntu/target/project-0.0.1-SNAPSHOT.jar > csye6225.log 2> csye6225.log < csye6225.log &
