package com.deepak.project.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.deepak.project.Exception.FileException;
import com.deepak.project.Exception.QuestionException;
import com.deepak.project.model.Answer;
import com.deepak.project.model.File;
import com.deepak.project.model.Question;
import com.deepak.project.repository.AnswerRepository;
import com.deepak.project.repository.FileRepository;
import com.deepak.project.repository.QuestionRepository;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FileService {

    final String forbidden = "Forbidden! You are not the owner of this question. you cannot delete/modify it";
    final String not_found = "Question not found";
    final String answer_notfound = "Answer not found";
    final String file_notfound = "File not found";
    final String typeUnsupported = "File type Unsupported";
    @Autowired
    AnswerRepository answerRepo;
    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private QuestionRepository questionRepo;
    @Autowired
    private FileRepository fileRepo;
    private String bucketName = "webapp.deepak.gopalan";


    public File saveFileToS3(MultipartFile uploadedFile, String userId, String questionId) throws FileException, QuestionException {

        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Question question = fetchedQuestion.get();
            if (userId.equals(question.getUserId())) {
                String s3ObjectName = questionId + LocalDateTime.now().toString();
                try {
                    ObjectMetadata data = new ObjectMetadata();
                    data.setContentType(uploadedFile.getContentType());
                    data.setContentLength(uploadedFile.getSize());
                    Tika tika = new Tika();
                    String detectedType = tika.detect(uploadedFile.getBytes()).split("/")[1];
                    if (detectedType.equals("png") || detectedType.equals("jpg") || detectedType.equals("jpeg")) {
                        amazonS3.putObject(new PutObjectRequest(bucketName, s3ObjectName, uploadedFile.getInputStream(), data)
                                .withCannedAcl(CannedAccessControlList.PublicRead));
                        File file = new File();
                        file.setCreated_date(LocalDateTime.now().toString());
                        file.setFileName(uploadedFile.getOriginalFilename());
                        file.setS3ObjectName(s3ObjectName);
                        file.setQuestion_id(questionId);
                        try {
                            return fileRepo.save(file);
                        } catch (Exception e) {
                            amazonS3.deleteObject(bucketName, s3ObjectName);
                            throw new FileException("Unable to save file data to RDS - " + e.getLocalizedMessage());
                        }
                    } else {
                        throw new FileException(typeUnsupported);
                    }
                } catch (Exception e) {
                    throw new FileException("Unable to save file", e.getLocalizedMessage());
                }
            } else {
                throw new QuestionException(forbidden);
            }
        } else {
            throw new QuestionException(not_found);
        }
    }


    public File saveFileToAnswer(MultipartFile uploadedFile, String userId, String questionId, String answerId) throws QuestionException, FileException {
        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Optional<Answer> fetchedAnswer = answerRepo.findById(answerId);
            if (fetchedAnswer.isPresent()) {
                Answer answer = fetchedAnswer.get();
                if (userId.equals(answer.getUserId())) {
                    String s3ObjectName = answerId + LocalDateTime.now().toString();
                    try {
                        ObjectMetadata data = new ObjectMetadata();
                        data.setContentType(uploadedFile.getContentType());
                        data.setContentLength(uploadedFile.getSize());
                        Tika tika = new Tika();
                        String detectedType = tika.detect(uploadedFile.getBytes()).split("/")[1];
                        if (detectedType.equals("png") || detectedType.equals("jpg") || detectedType.equals("jpeg")) {
                            amazonS3.putObject(new PutObjectRequest(bucketName, s3ObjectName, uploadedFile.getInputStream(), data)
                                    .withCannedAcl(CannedAccessControlList.PublicRead));
                            File file = new File();
                            file.setCreated_date(LocalDateTime.now().toString());
                            file.setFileName(uploadedFile.getOriginalFilename());
                            file.setS3ObjectName(s3ObjectName);
                            file.setAnswer_id(answerId);
                            try {
                                return fileRepo.save(file);
                            } catch (Exception e) {
                                amazonS3.deleteObject(bucketName, s3ObjectName);
                                throw new FileException("Unable to save file data to RDS - " + e.getLocalizedMessage());
                            }
                        } else {
                            throw new FileException(typeUnsupported);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new FileException("Unable to save file", e.getLocalizedMessage());
                    }
                } else {
                    throw new QuestionException(forbidden);
                }
            } else {
                throw new QuestionException(answer_notfound);
            }
        } else {
            throw new QuestionException(not_found);
        }
    }

    public void deleteFromS3(String userId, String questionId, String fileId) throws QuestionException, FileException {

        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Question question = fetchedQuestion.get();
            if (userId.equals(question.getUserId())) {
                Optional<File> optionalFile = question.getAttachments().stream().filter((f) -> f.getFileId().equals(fileId)).findFirst();
                if (optionalFile.isPresent()) {
                    File file = optionalFile.get();
                    try {
                        amazonS3.deleteObject(bucketName, file.getS3ObjectName());
                    } catch (Exception e) {
                        throw new FileException("Unable to delete File from S3", e.getLocalizedMessage());
                    }
                    try {
                        fileRepo.deleteById(fileId);
                    } catch (Exception e) {
                        throw new FileException("Unable to delete File from RDS", e.getLocalizedMessage());
                    }
                } else {
                    throw new FileException(file_notfound);
                }
            } else {
                throw new QuestionException(forbidden);
            }
        } else {
            throw new QuestionException(not_found);
        }
    }

    public void deleteFromAnswer(String userId, String questionId, String answerId, String fileId) throws QuestionException, FileException {
        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Optional<Answer> fetchedAnswer = answerRepo.findById(answerId);
            if (fetchedAnswer.isPresent()) {
                Answer answer = fetchedAnswer.get();
                if (userId.equals(answer.getUserId())) {
                    Optional<File> optionalFile = answer.getAttachments().stream().filter((f) -> f.getFileId().equals(fileId)).findFirst();
                    if (optionalFile.isPresent()) {
                        File file = optionalFile.get();
                        try {
                            amazonS3.deleteObject(bucketName, file.getS3ObjectName());
                        } catch (Exception e) {
                            throw new FileException("Unable to delete File from S3", e.getLocalizedMessage());
                        }
                        try {
                            fileRepo.deleteById(fileId);
                        } catch (Exception e) {
                            throw new FileException("Unable to delete File from RDS", e.getLocalizedMessage());
                        }
                    } else {
                        throw new FileException(file_notfound);
                    }
                } else {
                    throw new QuestionException(forbidden);
                }
            } else {
                throw new QuestionException(answer_notfound);
            }
        } else {
            throw new QuestionException(not_found);
        }
    }
}
