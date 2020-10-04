package com.deepak.project.handler;

import com.deepak.project.Exception.QuestionException;
import com.deepak.project.model.Answer;
import com.deepak.project.model.Question;
import com.deepak.project.model.UserPrincipal;
import com.deepak.project.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
public class QuestionHandler {

    QuestionService questionService;

    @Autowired
    public QuestionHandler(QuestionService service){
        this.questionService=service;
    }

    @PostMapping("/v1/question")
    public ResponseEntity<Question> postQuestion(@RequestBody @Valid @NotNull Question question ,
                                                 Principal principal) throws QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication)principal).getPrincipal();
        String userId = userPrincipal.getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestion(question, userId));
    }

    @PutMapping("/v1/question/{question_id}")
    public ResponseEntity editAQuestion(@RequestBody @Valid @NotNull Question question, @PathVariable("question_id") String question_id,
                                        Principal principal) throws QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication)principal).getPrincipal();
        String userId = userPrincipal.getId();
        questionService.editQuestion(validateQuestion(question), question_id,userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/v1/question/{question_id}")
    public ResponseEntity deleteAQuestion(@PathVariable("question_id") @NotNull String question_id,
                                          Principal principal) throws QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication)principal).getPrincipal();
        String userId = userPrincipal.getId();
        questionService.deleteQuestion(question_id,userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("v1/question/{question_id}")
    public ResponseEntity<Answer> answerAQuestion(@RequestBody @Valid @NotNull Answer answer,
                                                  @PathVariable("question_id") @NotNull String question_id ,
                                                  Principal principal) throws QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication)principal).getPrincipal();
        String userId = userPrincipal.getId();
        String answer_text = answer.getAnswer_text();
        if(null!=answer_text)
            return ResponseEntity.status(HttpStatus.CREATED).body( questionService.answerQuestion(question_id, userId, answer_text));
        else throw new QuestionException("Invalid Answer");
    }

    @PutMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity<Answer> updateAnAnswer(@RequestBody @Valid @NotNull Answer answer,
                                                  @PathVariable("answer_id") @NotNull String answer_id,
                                                  @PathVariable("question_id") @NotNull String question_id ,
                                                  Principal principal) throws QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication)principal).getPrincipal();
        String userId = userPrincipal.getId();
        String answer_text = answer.getAnswer_text();
        questionService.updateAnswer(question_id,answer_id,answer_text,userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity deleteAnAnswer(@PathVariable("answer_id") @NotNull String answer_id,
                                                 @PathVariable("question_id") @NotNull String question_id ,
                                                 Principal principal) throws QuestionException {
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication)principal).getPrincipal();
        String userId = userPrincipal.getId();
        questionService.deleteAnswer(answer_id,question_id,userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/v1/question/{question_id}")
    public ResponseEntity<Question> getAQuestion(@PathVariable("question_id") @NotNull String question_id ) throws QuestionException {
        return ResponseEntity.ok(questionService.getQuestion(question_id));
    }

    @GetMapping("/v1/questions")
    public ResponseEntity<List<Question>> getAllQuestions() throws QuestionException {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity<Answer> getAnAnswer(@PathVariable("answer_id") @NotNull String answer_id,
                                                      @PathVariable("question_id") @NotNull String question_id) throws QuestionException {
        return ResponseEntity.ok(questionService.getAnswer(answer_id));
    }


    public Question validateQuestion(Question question) throws QuestionException {

        if(null==question.getCategories()) {
            question.setCategories(new ArrayList<>());
        }
        if(null==question.getAnswers()) {
            question.setAnswers(new ArrayList<>());
        }
        if(null==question.getQuestion_text() || question.getQuestion_text().equals("")) {
            throw new QuestionException("Question Text cannot be empty");
        }
        return question;
    }

}
