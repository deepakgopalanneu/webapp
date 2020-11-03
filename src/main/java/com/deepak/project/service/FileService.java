package com.deepak.project.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private String bucketName = "webapp.deepak.gopalan";

    public void checkForFileNameConflict(String s3ObjectName) throws FileException {
        Optional<File> Optionalfile= fileRepo.findByS3ObjectName(s3ObjectName);
        if(Optionalfile.isPresent()){
            throw new FileException(CustomStrings.file_exists);
        }
    }

    public java.io.File convertMultipartFileToFile(MultipartFile uploadedFile) throws FileException {
        java.io.File convFile = new java.io.File(uploadedFile.getOriginalFilename());
        try {
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(uploadedFile.getBytes());
            fos.close();
        } catch (Exception e) {
            throw new FileException(CustomStrings.file_conversion_error);
        }
        return convFile;
    }
    public void verifyUserHasPermissionToModifyResource(String userId, String questionId, String answerId, String validateForQorA){

        if(validateForQorA.equals("Q")){

        }else{

        }

    }
    public String validateFileExtensionAndGenerateS3ObjectName(MultipartFile uploadedFile, String questionId) throws FileException {
        Tika tika = new Tika();
        String detectedType = null;
        try {
            detectedType = tika.detect(uploadedFile.getBytes()).split("/")[1];
        } catch (IOException e) {
            throw new FileException(CustomStrings.typeUnsupported, e.getLocalizedMessage());
        }
        if (detectedType.equals("png") || detectedType.equals("jpg") || detectedType.equals("jpeg"))
            return questionId + uploadedFile.getOriginalFilename();
        else
            throw new FileException(CustomStrings.typeUnsupported);

    }
    public File saveFileToS3(MultipartFile uploadedFile, String userId, String questionId) throws FileException, QuestionException {

        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Question question = fetchedQuestion.get();
            if (userId.equals(question.getUserId())) {
                try {
                    ObjectMetadata metaData = new ObjectMetadata();
                    metaData.setContentType(uploadedFile.getContentType());
                    metaData.setContentLength(uploadedFile.getSize());
                    String s3ObjectName = validateFileExtensionAndGenerateS3ObjectName(uploadedFile,questionId);
                    checkForFileNameConflict(s3ObjectName);
                    java.io.File fileToSave = convertMultipartFileToFile(uploadedFile);
                    amazonS3.putObject(new PutObjectRequest(bucketName, s3ObjectName,fileToSave )
                            .withCannedAcl(CannedAccessControlList.PublicRead)
                            .withMetadata(metaData));
                    return saveFileToDB(s3ObjectName,questionId,uploadedFile.getOriginalFilename());
                } catch ( SdkClientException e) {
                    throw new FileException("Unable to save file to S3", e.getLocalizedMessage());
                }
            } else {
                throw new QuestionException(CustomStrings.forbidden);
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
        }
    }

    public File saveFileToDB(String s3ObjectName, String questionId , String fileName) throws FileException {
        File file = new File();
        file.setCreated_date(LocalDateTime.now().toString());
        file.setFileName(fileName);
        file.setS3ObjectName(s3ObjectName);
        file.setQuestion_id(questionId);
        try {
            return fileRepo.save(file);
        } catch (Exception e) {
            amazonS3.deleteObject(bucketName, s3ObjectName);
            throw new FileException("Unable to save file data to RDS - " + e.getLocalizedMessage());
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
