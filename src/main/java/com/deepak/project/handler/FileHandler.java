package com.deepak.project.handler;

import com.deepak.project.Exception.FileException;
import com.deepak.project.Exception.QuestionException;
import com.deepak.project.model.File;
import com.deepak.project.model.UserPrincipal;
import com.deepak.project.service.FileService;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.security.Principal;

@RestController
public class FileHandler {


    private FileService fileService;
    private final static Logger logger = LoggerFactory.getLogger(FileHandler.class);
    StatsDClient statsd;

    @Autowired
    public FileHandler(FileService fileService, StatsDClient statsd) {
        this.fileService = fileService;
        this.statsd = statsd;
    }

    /**
     * This method saves file to question, uploads to S3 and saves File info in DB
     * @param uploadedFile
     * @param questionId
     * @param principal
     * @return
     * @throws FileException
     * @throws QuestionException
     */
    @PostMapping("/v1/question/{question_id}/file")
    public ResponseEntity<File> postImageToQuestion(@RequestPart @NotNull MultipartFile uploadedFile,
                                                    @PathVariable("question_id") @NotNull String questionId, Principal principal) throws FileException, QuestionException {
        logger.info("Entering POST FILE_TO_QUESTION controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - POST FILE_TO_QUESTION");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        File f = fileService.saveFileToQuestion(uploadedFile, userId, questionId);
        statsd.recordExecutionTime("Total ResponseTime - POST FILE_TO_QUESTION",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(f);
    }

    /**
     * This method deletes the file with the given fileId from the question with given questionId
     * @param questionId
     * @param fileId
     * @param principal
     * @return
     * @throws FileException
     * @throws QuestionException
     */
    @DeleteMapping("/v1/question/{question_id}/file/{file_id}")
    public ResponseEntity deleteImageFromQuestion(@PathVariable("question_id") @NotNull String questionId,
                                                  @PathVariable("file_id") @NotNull String fileId, Principal principal) throws FileException, QuestionException {
        logger.info("Entering DELETE FILE_FROM_QUESTION controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - DELETE FILE_FROM_QUESTION");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        fileService.deleteFromQuestion(userId, questionId, fileId);
        statsd.recordExecutionTime("Total ResponseTime - DELETE FILE_FROM_QUESTION",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     *  This method saves file to Answer, uploads to S3 and saves File info in DB
     * @param uploadedFile
     * @param answerId
     * @param questionId
     * @param principal
     * @return
     * @throws FileException
     * @throws QuestionException
     */
    @PostMapping("/v1/question/{question_id}/answer/{answer_id}/file")
    public ResponseEntity<File> postImageToAnswer(@RequestPart @NotNull MultipartFile uploadedFile, @PathVariable("answer_id") @NotNull String answerId,
                                                  @PathVariable("question_id") @NotNull String questionId, Principal principal) throws FileException, QuestionException {
        logger.info("Entering POST FILE_TO_ANSWER controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - POST FILE_TO_ANSWER");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        File f = fileService.saveFileToAnswer(uploadedFile, userId, questionId, answerId);
        statsd.recordExecutionTime("Total ResponseTime - POST FILE_TO_ANSWER",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(f);
    }

    /**
     * This method deletes the file with the given fileId from the Answer with given AnswerId
     * @param questionId
     * @param answerId
     * @param fileId
     * @param principal
     * @return
     * @throws FileException
     * @throws QuestionException
     */
    @DeleteMapping("/v1/question/{question_id}/answer/{answer_id}/file/{file_id}")
    public ResponseEntity deleteImageFromAnswer(@PathVariable("question_id") @NotNull String questionId, @PathVariable("answer_id") @NotNull String answerId,
                                                @PathVariable("file_id") @NotNull String fileId, Principal principal) throws FileException, QuestionException {
        logger.info("Entering DELETE FILE_FROM_ANSWER controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - DELETE FILE_FROM_ANSWER");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        fileService.deleteFromAnswer(userId, questionId, answerId, fileId);
        statsd.recordExecutionTime("Total ResponseTime - DELETE FILE_FROM_ANSWER",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
