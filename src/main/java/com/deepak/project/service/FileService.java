package com.deepak.project.service;

import com.amazonaws.SdkClientException;
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
import com.timgroup.statsd.StatsDClient;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FileService {


    private final AnswerRepository answerRepo;
    private final AmazonS3 amazonS3;
    private final QuestionRepository questionRepo;
    private final FileRepository fileRepo;
    @Value("${cloud.aws.s3.bucketname}")
    private String bucketName;
    private final static Logger logger = LoggerFactory.getLogger(QuestionService.class);
    StatsDClient statsd;

    @Autowired
    public FileService(AnswerRepository answerRepo, AmazonS3 amazonS3, QuestionRepository questionRepo, FileRepository fileRepo, StatsDClient statsd) {
        this.answerRepo = answerRepo;
        this.amazonS3 = amazonS3;
        this.questionRepo = questionRepo;
        this.fileRepo = fileRepo;
        this.statsd = statsd;
    }

    public File saveFileToQuestion(MultipartFile uploadedFile, String userId, String questionId) throws FileException, QuestionException {
        logger.info("Entering POST FILE_TO_QUESTION service method");
        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Question question = fetchedQuestion.get();
            verifyUserHasPermissionToModifyResource(userId, question.getUserId());
            try {
                String s3ObjectName = saveFileToS3(questionId,uploadedFile);
                return saveFileToDB(s3ObjectName, questionId, uploadedFile.getOriginalFilename(), "Q");
            } catch (SdkClientException e) {
                throw new FileException("Unable to save file to S3", e.getLocalizedMessage());
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
        }
    }



    public File saveFileToAnswer(MultipartFile uploadedFile, String userId, String questionId, String answerId) throws QuestionException, FileException {
        logger.info("Entering SAVE FILE_TO_ANSWER service method");
        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Optional<Answer> fetchedAnswer = answerRepo.findById(answerId);
            if (fetchedAnswer.isPresent()) {
                Answer answer = fetchedAnswer.get();
                verifyUserHasPermissionToModifyResource(userId, answer.getUserId());
                try {
                    String s3ObjectName = saveFileToS3(answerId,uploadedFile);
                    return saveFileToDB(s3ObjectName, answerId, uploadedFile.getOriginalFilename(), "A");
                } catch (SdkClientException e) {
                    throw new FileException("Unable to save file to S3", e.getLocalizedMessage());
                }
            } else {
                throw new QuestionException(CustomStrings.answer_notfound);
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
        }
    }

    public void deleteFromQuestion(String userId, String questionId, String fileId) throws QuestionException, FileException {
        logger.info("Entering DELETE FILE_FROM_QUESTION service method");
        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Question question = fetchedQuestion.get();
            verifyUserHasPermissionToModifyResource(userId, question.getUserId());
            Optional<File> optionalFile = question.getAttachments().stream().filter((f) -> f.getFileId().equals(fileId)).findFirst();
            if (optionalFile.isPresent()) {
                File file = optionalFile.get();
                deleteImageBys3ObjectName(file.getS3ObjectName());
                deleteFileFromDB(fileId);
            } else {
                throw new FileException(CustomStrings.file_notfound);
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
        }
    }

    public void deleteFromAnswer(String userId, String questionId, String answerId, String fileId) throws QuestionException, FileException {
        logger.info("Entering DELETE FILE_FROM_ANSWER service method");
        Optional<Question> fetchedQuestion = questionRepo.findById(questionId);
        if (fetchedQuestion.isPresent()) {
            Optional<Answer> fetchedAnswer = answerRepo.findById(answerId);
            if (fetchedAnswer.isPresent()) {
                Answer answer = fetchedAnswer.get();
                verifyUserHasPermissionToModifyResource(userId, answer.getUserId());
                Optional<File> optionalFile = answer.getAttachments().stream().filter((f) -> f.getFileId().equals(fileId)).findFirst();
                if (optionalFile.isPresent()) {
                    File file = optionalFile.get();
                    deleteImageBys3ObjectName(file.getS3ObjectName());
                    deleteFileFromDB(fileId);
                } else {
                    throw new FileException(CustomStrings.file_notfound);
                }
            } else {
                throw new QuestionException(CustomStrings.answer_notfound);
            }
        } else {
            throw new QuestionException(CustomStrings.not_found);
        }
    }
    public File saveFileToDB(String s3ObjectName, String questionOrAnswerId, String fileName, String QorAIdentifier) throws FileException {
        File file = new File();
        file.setCreated_date(LocalDateTime.now().toString());
        file.setFileName(fileName);
        file.setS3ObjectName(s3ObjectName);
        if (QorAIdentifier.equals("Q"))
            file.setQuestion_id(questionOrAnswerId);
        else
            file.setAnswer_id(questionOrAnswerId);
        try {
            long startTime = System.currentTimeMillis();
            File f = fileRepo.save(file);
            statsd.recordExecutionTime("DB ResponseTime - SAVE FILE", System.currentTimeMillis() - startTime);
            return f;
        } catch (Exception e) {
            deleteImageBys3ObjectName(s3ObjectName);
            throw new FileException(CustomStrings.rds_save_error , e.getLocalizedMessage());
        }
    }

    public void deleteFileFromDB(String fileId) throws FileException {
        try {
            long startTime = System.currentTimeMillis();
            fileRepo.deleteById(fileId);
            statsd.recordExecutionTime("DB ResponseTime - DELETE FILE", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            throw new FileException(CustomStrings.rds_delete_error, e.getLocalizedMessage());
        }
    }

    public String saveFileToS3( String questionOrAnswerId, MultipartFile uploadedFile) throws FileException {
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentType(uploadedFile.getContentType());
        metaData.setContentLength(uploadedFile.getSize());
        String s3ObjectName = validateFileExtensionAndGenerateS3ObjectName(uploadedFile, questionOrAnswerId);
        checkForFileNameConflict(s3ObjectName);
        try {
            long startTime = System.currentTimeMillis();
            amazonS3.putObject(new PutObjectRequest(bucketName, s3ObjectName, uploadedFile.getInputStream(), metaData)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            statsd.recordExecutionTime("S3 ResponseTime - SAVE FILE to S3", System.currentTimeMillis() - startTime);
        }catch (SdkClientException | IOException e) {
            throw new FileException(CustomStrings.s3_save_error, e.getLocalizedMessage());
        }
        return s3ObjectName;
    }
    public void deleteImageBys3ObjectName(String s3ObjectName) throws FileException {
        try {
            long startTime = System.currentTimeMillis();
            amazonS3.deleteObject(bucketName, s3ObjectName);
            statsd.recordExecutionTime("S3 ResponseTime - DELETE FILE FROM S3", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            throw new FileException(CustomStrings.s3_delete_error, e.getMessage());
        }
    }


    public void checkForFileNameConflict(String s3ObjectName) throws FileException {
        Optional<File> Optionalfile = fileRepo.findByS3ObjectName(s3ObjectName);
        if (Optionalfile.isPresent()) {
            throw new FileException(CustomStrings.file_exists);
        }
    }

    public void verifyUserHasPermissionToModifyResource(String userId, String actualUser) throws QuestionException {

        if (!userId.equals(actualUser))
            throw new QuestionException(CustomStrings.forbidden);

    }

    public String validateFileExtensionAndGenerateS3ObjectName(MultipartFile uploadedFile, String questionId) throws FileException {
        Tika tika = new Tika();
        try {
            String detectedType = tika.detect(uploadedFile.getBytes()).split("/")[1];
            if (detectedType.equals("png") || detectedType.equals("jpg") || detectedType.equals("jpeg"))
                return questionId + uploadedFile.getOriginalFilename();
            else
                throw new FileException(CustomStrings.typeUnsupported);
        } catch (IOException e) {
            throw new FileException(CustomStrings.typeUnsupported, e.getLocalizedMessage());
        }
    }

}