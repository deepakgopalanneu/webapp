package com.deepak.project.service;


import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
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

@Service
public class SNSService {

    @Value("${cloud.aws.region}")
    private String region;
    private AmazonSNS sns;
    private CreateTopicResult topic;
    private final static Logger logger = LoggerFactory.getLogger(SNSService.class);

    @Autowired
    public SNSService(){
        InstanceProfileCredentialsProvider provider = new InstanceProfileCredentialsProvider(true);
        this.sns =  AmazonSNSClientBuilder.standard().withCredentials(provider).withRegion(region).build();
        this.topic = sns.createTopic("TOPIC_EMAIL");
    }

    public boolean postToSNSTopic(String questionId, String answerId, String destinationEmail ){
        try {
            PublishRequest request = new PublishRequest(topic.getTopicArn(), formatMessageBody(questionId,answerId,destinationEmail));
            PublishResult result = sns.publish(request);
            logger.info("Published to SNS Topic! The Message ID was : "+ result.getMessageId() + "| Status was " + result.getSdkHttpMetadata().getHttpStatusCode());
            return true;
        } catch (AmazonSNSException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
    }

    public String formatMessageBody(String questionId, String answerId, String destinationEmail) {
        StringBuilder message = new StringBuilder();
        message.append("destination :");
        message.append(destinationEmail);
        message.append("|");
        message.append("QuestionId :");
        message.append(questionId);
        message.append("|");
        message.append("AnswerId :");
        message.append(answerId);
        message.append("|");
        message.append("Question Link : http://prod.deepakgopalan.me/v1/question/");
        message.append(questionId);
        message.append("|");
        message.append("Answer Link : http://prod.deepakgopalan.me/v1/question/");
        message.append(questionId);
        message.append("/answer/");
        message.append(answerId);
        return message.toString();
    }
}
