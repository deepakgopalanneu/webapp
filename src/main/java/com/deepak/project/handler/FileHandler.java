package com.deepak.project.handler;

import com.deepak.project.Exception.FileException;
import com.deepak.project.Exception.QuestionException;
import com.deepak.project.model.File;
import com.deepak.project.model.UserPrincipal;
import com.deepak.project.service.FileService;
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

    @Autowired
    private FileService fileService;

    @PostMapping("/v1/question/{question_id}/file")
    public ResponseEntity<File> postImageToQuestion(@RequestPart @NotNull MultipartFile uploadedFile,
                                                    @PathVariable("question_id") @NotNull String questionId, Principal principal) throws FileException, QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();

        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.saveFileToQuestion(uploadedFile, userId, questionId));
    }

    @DeleteMapping("/v1/question/{question_id}/file/{file_id}")
    public ResponseEntity deleteImageFromQuestion(@PathVariable("question_id") @NotNull String questionId,
                                                  @PathVariable("file_id") @NotNull String fileId, Principal principal) throws FileException, QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        fileService.deleteFromQuestion(userId, questionId, fileId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/v1/question/{question_id}/answer/{answer_id}/file")
    public ResponseEntity<File> postImageToAnswer(@RequestPart @NotNull MultipartFile uploadedFile, @PathVariable("answer_id") @NotNull String answerId,
                                                  @PathVariable("question_id") @NotNull String questionId, Principal principal) throws FileException, QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.saveFileToAnswer(uploadedFile, userId, questionId, answerId));
    }

    @DeleteMapping("/v1/question/{question_id}/answer/{answer_id}/file/{file_id}")
    public ResponseEntity deleteImageFromAnswer(@PathVariable("question_id") @NotNull String questionId, @PathVariable("answer_id") @NotNull String answerId,
                                                @PathVariable("file_id") @NotNull String fileId, Principal principal) throws FileException, QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        fileService.deleteFromAnswer(userId, questionId, answerId, fileId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
