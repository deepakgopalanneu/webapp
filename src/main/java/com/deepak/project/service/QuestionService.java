package com.deepak.project.service;

import com.deepak.project.Exception.FileException;
import com.deepak.project.Exception.QuestionException;
import com.deepak.project.model.Answer;
import com.deepak.project.model.Category;
import com.deepak.project.model.File;
import com.deepak.project.model.Question;
import com.deepak.project.repository.AnswerRepository;
import com.deepak.project.repository.CategoryRepository;
import com.deepak.project.repository.QuestionRepository;
import com.deepak.project.util.CustomStrings;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuestionService {

    QuestionRepository questionRepo;
    CategoryRepository categoryRepo;
    AnswerRepository answerRepo;
    FileService fileService;
    private final static Logger logger = LoggerFactory.getLogger(QuestionService.class);
    StatsDClient statsd;

    @Autowired
    public QuestionService(QuestionRepository questionRepo, CategoryRepository categoryRepo, AnswerRepository answerRepo, FileService fileService, StatsDClient statsd) {
        this.questionRepo = questionRepo;
        this.categoryRepo = categoryRepo;
        this.answerRepo = answerRepo;
        this.fileService = fileService;
        this.statsd = statsd;
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public Question createQuestion(Question question, String userId) throws QuestionException {
        logger.info("Logging from CREATE Question service method");
        question.setCreated_timestamp(LocalDateTime.now().toString());
        question.setUpdated_timestamp(LocalDateTime.now().toString());
        question.setUserId(userId);
        List<Category> givenCategories = null;
        List<Category> categoryList = new ArrayList<>();
        if (null != question.getCategories())
            givenCategories = question.getCategories();
        else
            givenCategories = new ArrayList<>();
        question.setCategories(null);
        try {

            givenCategories.stream().filter(distinctByKey(c -> c.getCategory())).forEach((category) -> {
                String categoryName = category.getCategory().toLowerCase().trim();
                if (null == categoryName || categoryName.trim().isEmpty())
                    throw new RuntimeException("Category cannot be empty or null");
                Pattern p = Pattern.compile("[^A-Za-z0-9]");
                Matcher m = p.matcher(categoryName);
                boolean b = m.find();
                if (b)
                    throw new RuntimeException("Category cannot have special characters");
                Category c = categoryRepo.findByCategory(categoryName);
                if (null != c)
                    categoryList.add(c);
                else {
                    Category addCategory = new Category();
                    addCategory.setCategory(categoryName);
                    categoryList.add(categoryRepo.save(addCategory));
                }
            });
            question.setCategories(categoryList);
            question.setAnswers(new ArrayList<>());
            question.setAttachments(new ArrayList<>());
            long startTime = System.currentTimeMillis();
            Question q = questionRepo.save(question);
            statsd.recordExecutionTime("DB ResponseTime - SAVE Question", System.currentTimeMillis() - startTime);
            return q;
        } catch (Exception ex) {
            throw new QuestionException(ex.getMessage());
        }
    }

    public Answer answerQuestion(String question_id, String userId, String answer_text) throws QuestionException {
        logger.info("Logging from CREATE ANSWER service method");
        Answer answer = new Answer();
        answer.setAnswer_text(answer_text);
        answer.setUserId(userId);
        answer.setQuestion_id(question_id);
        answer.setCreated_timestamp(LocalDateTime.now().toString());
        answer.setUpdated_timestamp(LocalDateTime.now().toString());
        answer.setAttachments(new ArrayList<>());
        try {
            long startTime = System.currentTimeMillis();
            Optional<Question> q = questionRepo.findById(question_id);
            if (q.isPresent()) {
                Answer ans =  answerRepo.save(answer);
                statsd.recordExecutionTime("DB ResponseTime - SAVE ANSWER", System.currentTimeMillis() - startTime);
                return ans;
            }else
                throw new QuestionException(CustomStrings.not_found);
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    public void editQuestion(Question question, String question_id, String userId) throws QuestionException {
        logger.info("Logging from EDIT Question service method");
        try {
            Optional<Question> foundQuestion = questionRepo.findById(question_id);
            if (foundQuestion.isPresent()) {
                Question q = foundQuestion.get();
                if (!q.getUserId().equals(userId)) {
                    throw new QuestionException(CustomStrings.forbidden);
                } else {
                    List<Category> givenCategories = null;
                    List<Category> categoryList = new ArrayList<>();
                    if (null != question.getCategories())
                        givenCategories = question.getCategories();
                    else
                        givenCategories = new ArrayList<>();
                    question.setCategories(null);
                    givenCategories.stream().filter(distinctByKey(c -> c.getCategory())).forEach((category) -> {
                        String categoryName = category.getCategory().toLowerCase().trim();
                        if (null == categoryName || categoryName.trim().isEmpty())
                            throw new RuntimeException("Category cannot be empty or null");
                        Pattern p = Pattern.compile("[^A-Za-z0-9]");
                        Matcher m = p.matcher(categoryName);
                        boolean b = m.find();
                        if (b)
                            throw new RuntimeException("Category cannot have special characters");
                        Category c = categoryRepo.findByCategory(categoryName);
                        if (null != c)
                            categoryList.add(c);
                        else {
                            Category addCategory = new Category();
                            addCategory.setCategory(categoryName);
                            categoryList.add(categoryRepo.save(addCategory));
                        }
                    });
                    q.setCategories(categoryList);
                    q.setQuestion_text(question.getQuestion_text());
                    q.setUpdated_timestamp(LocalDateTime.now().toString());
                    long startTime = System.currentTimeMillis();
                    questionRepo.save(q);
                    statsd.recordExecutionTime("DB ResponseTime - EDIT ANSWER", System.currentTimeMillis() - startTime);
                }
            } else {
                throw new QuestionException(CustomStrings.not_found);
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    private void deleteImagesFromS3(List<File> attachments) {
        logger.info("Logging from DELETE IMAGES FROM S3 when question is deleted - service method");
        long startTime = System.currentTimeMillis();
        attachments.stream().forEach(f -> {
            try {
                fileService.deleteImageBys3ObjectName(f.getS3ObjectName());
            } catch (FileException e) {
                throw new RuntimeException("Unable to Delete Images attached to this Question");
            }
        });
        statsd.recordExecutionTime("S3 ResponseTime - DELETE File FROM S3", System.currentTimeMillis() - startTime);

    }

    public void deleteQuestion(String question_id, String userId) throws QuestionException {
        logger.info("Logging from DELETE Question service method");
        try {
            Optional<Question> foundQuestion = questionRepo.findById(question_id);
            if (foundQuestion.isPresent()) {
                Question q = foundQuestion.get();
                if (!q.getUserId().equals(userId)) {
                    throw new QuestionException(CustomStrings.forbidden);
                }
                if (q.getAnswers().size() < 1) {
                    if (q.getAttachments().size() > 0) {
                        deleteImagesFromS3(q.getAttachments());
                    }
                    long startTime = System.currentTimeMillis();
                    questionRepo.deleteById(question_id);
                    statsd.recordExecutionTime("DB ResponseTime - DELETE Question", System.currentTimeMillis() - startTime);

                } else {
                    throw new QuestionException("You cannot delete a Question which has answers");
                }
            } else {
                throw new QuestionException(CustomStrings.not_found);
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    public void updateAnswer(String question_id, String answer_id, String answer_text, String userId) throws QuestionException {
        logger.info("Logging from UPDATE Answer service method");
        try {
            Optional<Question> questOptional = questionRepo.findById(question_id);
            Answer ans = null;
            if (questOptional.isPresent()) {
                Question q = questOptional.get();
                Optional<Answer> answers = q.getAnswers().stream().filter((a) -> a.getAnswer_id().equals(answer_id)).findFirst();
                if (answers.isPresent()) {
                    ans = answers.get();
                    if (!ans.getUserId().equals(userId)) {
                        throw new QuestionException(CustomStrings.forbidden);
                    } else {
                        ans.setAnswer_text(answer_text);
                        ans.setUpdated_timestamp(LocalDateTime.now().toString());
                        long startTime = System.currentTimeMillis();
                        answerRepo.save(ans);
                        statsd.recordExecutionTime("DB ResponseTime - UPDATE Answer", System.currentTimeMillis() - startTime);

                    }
                } else {
                    throw new QuestionException(CustomStrings.answer_notfound);
                }
            } else {
                throw new QuestionException(CustomStrings.not_found);
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    public void deleteAnswer(String answer_id, String question_id, String userId) throws QuestionException {
        logger.info("Logging from DELETE Answer service method");
        try {
            Optional<Question> questOptional = questionRepo.findById(question_id);
            Answer ans;
            if (questOptional.isPresent()) {
                Question q = questOptional.get();
                Optional<Answer> answers = q.getAnswers().stream().filter((a) -> a.getAnswer_id().equals(answer_id)).findFirst();
                if (answers.isPresent()) {
                    ans = answers.get();
                    if (!ans.getUserId().equals(userId)) {
                        throw new QuestionException(CustomStrings.forbidden);
                    } else {
                        if (ans.getAttachments().size() > 0)
                            deleteImagesFromS3(ans.getAttachments());
                        long startTime = System.currentTimeMillis();
                        answerRepo.deleteById(ans.getAnswer_id());
                        statsd.recordExecutionTime("DB ResponseTime - DELETE Answer", System.currentTimeMillis() - startTime);

                    }
                } else {
                    throw new QuestionException(CustomStrings.answer_notfound);
                }
            } else {
                throw new QuestionException(CustomStrings.not_found);
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    public Question getQuestion(String question_id) throws QuestionException {
        logger.info("Logging from GET Question service method");
        try {
            long startTime = System.currentTimeMillis();
            Optional<Question> foundQuestion = questionRepo.findById(question_id);
            statsd.recordExecutionTime("DB ResponseTime - GET QUESTION", System.currentTimeMillis() - startTime);

            if (foundQuestion.isPresent()) {
                return foundQuestion.get();
            } else {
                throw new QuestionException(CustomStrings.not_found);
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    public List<Question> getAllQuestions() throws QuestionException {
        logger.info("Logging from GET ALL Questions service method");
        try {
            long startTime = System.currentTimeMillis();
            List<Question> foundQuestions = (List<Question>) questionRepo.findAll();
            statsd.recordExecutionTime("DB ResponseTime - GET ALL Questions", System.currentTimeMillis() - startTime);
            if (foundQuestions.isEmpty()) {
                throw new QuestionException(CustomStrings.not_found);
            } else {
                return foundQuestions;
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    public Answer getAnswer(String answer_id) throws QuestionException {
        logger.info("Logging from GET Answer service method");
        try {
            long startTime = System.currentTimeMillis();
            Optional<Answer> foundAnswer = answerRepo.findById(answer_id);
            statsd.recordExecutionTime("DB ResponseTime - GET Answer", System.currentTimeMillis() - startTime);
            if (foundAnswer.isPresent()) {
                Answer ans = foundAnswer.get();
                return ans;
            } else {
                throw new QuestionException(CustomStrings.answer_notfound);
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }
}
