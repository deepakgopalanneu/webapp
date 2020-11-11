package com.deepak.project.service;

import com.amazonaws.services.dynamodbv2.xspec.S;
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
import com.deepak.project.util.CustomStrings;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FileService {


    @Autowired
    AnswerRepository answerRepo;
    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private QuestionRepository questionRepo;
    @Autowired
    private FileRepository fileRepo;
    @Value("cloud.aws.s3.bucketname")
    private String bucketName ;

    public void checkForFileNameConflict(String s3ObjectName) throws FileException {
        Optional<File> Optionalfile= fileRepo.findByS3ObjectName(s3ObjectName);
        if(Optionalfile.isPresent()){
            throw new FileException(CustomStrings.file_exists);
        }
    }


    public File saveFileToS3(MultipartFile uploadedFile, String userId, String questionId) throws FileException, QuestionException {

        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Question question = fetchedQuestion.get();
            if (userId.equals(question.getUserId())) {
                try {
                    ObjectMetadata data = new ObjectMetadata();
                    data.setContentType(uploadedFile.getContentType());
                    data.setContentLength(uploadedFile.getSize());
                    Tika tika = new Tika();
                    String detectedType = tika.detect(uploadedFile.getBytes()).split("/")[1];
                    String s3ObjectName = questionId + uploadedFile.getOriginalFilename();
                    if (detectedType.equals("png") || detectedType.equals("jpg") || detectedType.equals("jpeg")) {
                        checkForFileNameConflict(s3ObjectName);
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
                        throw new FileException(CustomStrings.typeUnsupported);
                    }
                } catch (Exception e) {
                    throw new FileException("Unable to save file", e.getLocalizedMessage());
                }
            } else {
                throw new QuestionException(CustomStrings.forbidden);
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
        }
    }


    public File saveFileToAnswer(MultipartFile uploadedFile, String userId, String questionId, String answerId) throws QuestionException, FileException {
        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Optional<Answer> fetchedAnswer = answerRepo.findById(answerId);
            if (fetchedAnswer.isPresent()) {
                Answer answer = fetchedAnswer.get();
                if (userId.equals(answer.getUserId())) {
                    try {
                        ObjectMetadata data = new ObjectMetadata();
                        data.setContentType(uploadedFile.getContentType());
                        data.setContentLength(uploadedFile.getSize());
                        Tika tika = new Tika();
                        String detectedType = tika.detect(uploadedFile.getBytes()).split("/")[1];
                        String s3ObjectName = answerId + uploadedFile.getOriginalFilename();
                        if (detectedType.equals("png") || detectedType.equals("jpg") || detectedType.equals("jpeg")) {
                            checkForFileNameConflict(s3ObjectName);
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
                            throw new FileException(CustomStrings.typeUnsupported);
                        }
                    } catch (Exception e) {
                        throw new FileException(e.getMessage());
                    }
                } else {
                    throw new QuestionException(CustomStrings.forbidden);
                }
            } else {
                throw new QuestionException(CustomStrings.answer_notfound);
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
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
                    throw new FileException(CustomStrings.file_notfound);
                }
            } else {
                throw new QuestionException(CustomStrings.forbidden);
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
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
                        throw new FileException(CustomStrings.file_notfound);
                    }
                } else {
                    throw new QuestionException(CustomStrings.forbidden);
                }
            } else {
                throw new QuestionException(CustomStrings.answer_notfound);
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
        }
    }

    public void deleteImageBys3ObjectName(String s3ObjectName) throws FileException {
        try{
            amazonS3.deleteObject(bucketName,s3ObjectName);
        }catch (Exception e){
            throw new FileException(e.getMessage());
        }
    }
}
