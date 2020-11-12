package com.deepak.project.handler;

import com.deepak.project.Exception.QuestionException;
import com.deepak.project.model.Answer;
import com.deepak.project.model.Question;
import com.deepak.project.model.UserPrincipal;
import com.deepak.project.service.QuestionService;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger logger = LoggerFactory.getLogger(QuestionHandler.class);
    StatsDClient statsd;

    @Autowired
    public QuestionHandler(QuestionService questionService, StatsDClient statsd) {
        this.questionService = questionService;
        this.statsd = statsd;
    }

    /**
     * This method posts the given Question
     *
     * @param question
     * @param principal
     * @return
     * @throws QuestionException
     */
    @PostMapping("/v1/question")
    public ResponseEntity<Question> postQuestion(@RequestBody @Valid @NotNull Question question,
                                                 Principal principal) throws QuestionException {
        logger.info("Logging from POST /v1/question controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - POST /v1/question");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        Question q = questionService.createQuestion(question, userId);
        statsd.recordExecutionTime("ResponseTime - POST /v1/question",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(q);
    }

    /**
     * This method edits the question with the given questionId
     * @param question
     * @param question_id
     * @param principal
     * @return
     * @throws QuestionException
     */
    @PutMapping("/v1/question/{question_id}")
    public ResponseEntity editAQuestion(@RequestBody @Valid @NotNull Question question, @PathVariable("question_id") String question_id,
                                        Principal principal) throws QuestionException {
        logger.info("Logging from PUT /v1/question/{question_id} controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - PUT /v1/question/{question_id}");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        questionService.editQuestion(validateQuestion(question), question_id, userId);
        statsd.recordExecutionTime("ResponseTime - PUT /v1/question/{question_id}",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * This method deletes the question with the given QuestionId
     * @param question_id
     * @param principal
     * @return
     * @throws QuestionException
     */
    @DeleteMapping("/v1/question/{question_id}")
    public ResponseEntity deleteAQuestion(@PathVariable("question_id") @NotNull String question_id,
                                          Principal principal) throws QuestionException {
        logger.info("Logging from DELETE /v1/question/{question_id} controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - DELETE /v1/question/{question_id}");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        questionService.deleteQuestion(question_id, userId);
        statsd.recordExecutionTime("ResponseTime - DELETE /v1/question/{question_id}",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * this method posts a Answer for the given QuestionId
     * @param answer
     * @param question_id
     * @param principal
     * @return
     * @throws QuestionException
     */
    @PostMapping("v1/question/{question_id}")
    public ResponseEntity<Answer> answerAQuestion(@RequestBody @Valid @NotNull Answer answer,
                                                  @PathVariable("question_id") @NotNull String question_id,
                                                  Principal principal) throws QuestionException {
        logger.info("Logging from POST /v1/question/{question_id} controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - POST /v1/question/{question_id}");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        String answer_text = answer.getAnswer_text();
        if (null != answer_text && !answer_text.isEmpty()) {
            Answer ans = questionService.answerQuestion(question_id, userId, answer_text);
            statsd.recordExecutionTime("ResponseTime - POST /v1/question/{question_id}",System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.CREATED).body(ans);
        }
        else throw new QuestionException("Invalid Answer");
    }

    /**
     * this method updates the Answer within the given questionId & answerId
     * @param answer
     * @param answer_id
     * @param question_id
     * @param principal
     * @return
     * @throws QuestionException
     */
    @PutMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity<Answer> updateAnAnswer(@RequestBody @Valid @NotNull Answer answer,
                                                 @PathVariable("answer_id") @NotNull String answer_id,
                                                 @PathVariable("question_id") @NotNull String question_id,
                                                 Principal principal) throws QuestionException {
        logger.info("Logging from PUT /v1/question/{id}/answer/{id} controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - PUT /v1/question/{id}/answer/{id}");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        String answer_text = answer.getAnswer_text();
        questionService.updateAnswer(question_id, answer_id, answer_text, userId);
        statsd.recordExecutionTime("ResponseTime - PUT /v1/question/{id}/answer/{id}",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * this method deletes the Answer with the given AnswerId and QuestionId
     * @param answer_id
     * @param question_id
     * @param principal
     * @return
     * @throws QuestionException
     */
    @DeleteMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity deleteAnAnswer(@PathVariable("answer_id") @NotNull String answer_id,
                                         @PathVariable("question_id") @NotNull String question_id,
                                         Principal principal) throws QuestionException {
        logger.info("Logging from DELETE /v1/question/{id}/answer/{id} controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - DELETE /v1/question/{id}/answer/{id}");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        String userId = userPrincipal.getId();
        questionService.deleteAnswer(answer_id, question_id, userId);
        statsd.recordExecutionTime("ResponseTime - DELETE /v1/question/{id}/answer/{id}",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * this method fetches the question with the given questionId
     * @param question_id
     * @return
     * @throws QuestionException
     */
    @GetMapping("/v1/question/{question_id}")
    public ResponseEntity<Question> getAQuestion(@PathVariable("question_id") @NotNull String question_id) throws QuestionException {
        logger.info("Logging from GET /v1/question/{id} controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - GET /v1/question/{id}");
        Question q = questionService.getQuestion(question_id);
        statsd.recordExecutionTime("ResponseTime - GET /v1/question/{id}",System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(q);
    }

    /**
     * This method fetches all the questions in the DB
     * @return
     * @throws QuestionException
     */
    @GetMapping("/v1/questions")
    public ResponseEntity<List<Question>> getAllQuestions() throws QuestionException {
        logger.info("Logging from GET /v1/questions controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - GET /v1/questions");
        statsd.recordExecutionTime("ResponseTime - GET /v1/questions",System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    /**
     * this method fetches the answer with the given AnswerId
     * @param answer_id
     * @param question_id
     * @return
     * @throws QuestionException
     */
    @GetMapping("/v1/question/{question_id}/answer/{answer_id}")
    public ResponseEntity<Answer> getAnAnswer(@PathVariable("answer_id") @NotNull String answer_id,
                                              @PathVariable("question_id") @NotNull String question_id) throws QuestionException {
        logger.info("Logging from GET /v1/question/{id}/answer/{id} controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - GET /v1/question/{id}/answer/{id}");
        statsd.recordExecutionTime("ResponseTime - GET /v1/question/{id}/answer/{id}",System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(questionService.getAnswer(answer_id));
    }

    /**
     * This method is a helper method used to validate the question in the request body
     * @param question
     * @return
     * @throws QuestionException
     */
    public Question validateQuestion(Question question) throws QuestionException {

        if (null == question.getCategories()) {
            question.setCategories(new ArrayList<>());
        }
        if (null == question.getAnswers()) {
            question.setAnswers(new ArrayList<>());
        }
        if (null == question.getQuestion_text() || question.getQuestion_text().equals("")) {
            throw new QuestionException("Question Text cannot be empty");
        }
        return question;
    }

}
