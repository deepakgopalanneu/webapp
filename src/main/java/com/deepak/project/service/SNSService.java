package com.deepak.project.service;


import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import  com.amazonaws.services.sns.model.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class SNSService {

    @Value("${cloud.aws.region}")
    private String region;
    private AmazonSNS sns;
    private CreateTopicResult topic;
    private final static Logger logger = LoggerFactory.getLogger(SNSService.class);
    String topicArn;
    enum operation{
        POST,
        PUT,
        DELETE
    }
    @Autowired
    public SNSService(){
        InstanceProfileCredentialsProvider provider = new InstanceProfileCredentialsProvider(true);
        this.sns =  AmazonSNSClientBuilder.standard().withCredentials(provider).withRegion(region).build();
        this.topic = sns.createTopic("TOPIC_EMAIL");
        topicArn=topic.getTopicArn();
    }

    public boolean postToSNSTopic(String questionId, String answerId, String destinationEmail , String answerBody , operation scenario){
        try {
            PublishRequest request = new PublishRequest(topicArn, formatMessageBody(questionId,answerId,destinationEmail,answerBody, scenario));
            PublishResult result = sns.publish(request);
            logger.info("Published to SNS Topic! The Message ID was : "+ result.getMessageId() + "| Status was " + result.getSdkHttpMetadata().getHttpStatusCode());
            return true;
        } catch (AmazonSNSException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
    }

    public String formatMessageBody(String questionId, String answerId, String destinationEmail, String answerBody, operation scenario) {
        switch(scenario) {
            case PUT:
                String itemKey = Base64.getEncoder().encodeToString((questionId+destinationEmail+answerBody).getBytes());
                return ("PUT,"+destinationEmail +","+questionId+","+answerId+","+"Question Link : http://prod.deepakgopalan.me/v1/question/"+questionId+","+
                        "Answer Link : http://prod.deepakgopalan.me/v1/question/"+questionId+"/answer/"+answerId + ","+ answerBody+ ","+itemKey);
            case POST:
                String itemKey1 = Base64.getEncoder().encodeToString((questionId+destinationEmail+answerBody).getBytes());
                return ("POST,"+destinationEmail +","+questionId+","+answerId+","+"Question Link : http://prod.deepakgopalan.me/v1/question/"+questionId+","+
                        "Answer Link : http://prod.deepakgopalan.me/v1/question/"+questionId+"/answer/"+answerId + ","+ answerBody+ ","+itemKey1);

            case DELETE:
                String itemKey2 = Base64.getEncoder().encodeToString((questionId+destinationEmail).getBytes());
                return ("DELETE,"+destinationEmail +","+questionId+","+answerId+","+"Question Link : http://prod.deepakgopalan.me/v1/question/"+questionId+","+itemKey2);

            default:
                return " ";
        }


    }
}
