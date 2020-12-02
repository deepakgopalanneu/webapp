package com.deepak.project.service;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
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
    SNSService snsService;
    @Autowired
    public QuestionService(QuestionRepository questionRepo, CategoryRepository categoryRepo, AnswerRepository answerRepo, FileService fileService, StatsDClient statsd,SNSService snsService) {
        this.questionRepo = questionRepo;
        this.categoryRepo = categoryRepo;
        this.answerRepo = answerRepo;
        this.fileService = fileService;
        this.statsd = statsd;
        this.snsService=snsService;
    }

    /**
     * This predicate helps to filter out unique items in a list
     * in this case, we use this to filter repeated categories from questions
     *
     * @param keyExtractor - pass the attribute based on which you want to filter the stream
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * this method checks all the categories on the questions to be saved.
     * removes duplicate categories and saves new categories
     * then saves the question to DB
     *
     * @param question
     * @param userId
     * @return
     * @throws QuestionException
     */
    public Question createQuestion(Question question, String userId) throws QuestionException {
        logger.info("Entering CREATE QUESTION service method");
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
                    try {
                        categoryList.add( categoryRepo.save(addCategory));
                    }catch (Exception e){}

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

    /**
     * This method checks if a Question exists for given questionId
     * If yes, saves a new Answer to that Question
     *
     * @param question_id
     * @param userId
     * @param answer_text
     * @return
     * @throws QuestionException
     */
    public Answer answerQuestion(String question_id, String userId, String answer_text, String userEmail) throws QuestionException {
        logger.info("Entering POST ANSWER service method");
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
                Answer ans = answerRepo.save(answer);
                statsd.recordExecutionTime("DB ResponseTime - POST ANSWER", System.currentTimeMillis() - startTime);
                snsService.postToSNSTopic(question_id,ans.getAnswer_id(),userId);
                return ans;
            } else
                throw new QuestionException(CustomStrings.not_found);
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    /**
     * This method checks if question exists for given questionID
     * removes duplicates for list of categories given, checks if the filtered categories already exists
     * if not, saves the categories
     * then updates the Question
     *
     * @param question
     * @param question_id
     * @param userId
     * @throws QuestionException
     */
    public void editQuestion(Question question, String question_id, String userId) throws QuestionException {
        logger.info("Entering PUT QUESTION service method");
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
                            try {
                                categoryList.add( categoryRepo.save(addCategory));
                            }catch (Exception e){}
                        }
                    });
                    q.setCategories(categoryList);
                    q.setQuestion_text(question.getQuestion_text());
                    q.setUpdated_timestamp(LocalDateTime.now().toString());
                    long startTime = System.currentTimeMillis();
                    questionRepo.save(q);
                    statsd.recordExecutionTime("DB ResponseTime - PUT QUESTION", System.currentTimeMillis() - startTime);
                }
            } else {
                throw new QuestionException(CustomStrings.not_found);
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    /**
     * this method deletes each of the file in the given list from S3
     *
     * @param attachments - List of Files
     */
    private void deleteImagesFromS3(List<File> attachments) {
        logger.info("Entering DELETE FILE FROM S3 when question is deleted - service method");

        attachments.stream().forEach(f -> {
            try {
                fileService.deleteImageBys3ObjectName(f.getS3ObjectName());
            } catch (FileException e) {
                throw new RuntimeException("Unable to Delete Images attached to this Question");
            }
        });


    }

    /**
     * ensures given question exists
     * ensures user is authorized to delete the question
     * ensures question does not have any answers attached to it
     * deletes all the files atatched to question, if any
     * then deletes the question from DB
     *
     * @param question_id
     * @param userId
     * @throws QuestionException
     */
    public void deleteQuestion(String question_id, String userId) throws QuestionException {
        logger.info("Entering DELETE QUESTION service method");
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
                    statsd.recordExecutionTime("DB ResponseTime - DELETE QUESTION", System.currentTimeMillis() - startTime);

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

    /**
     * ensures Question exists for given questionId
     * ensures Answer exists for given answerId
     * ensure User is authorized to update answer
     *
     * @param question_id
     * @param answer_id
     * @param answer_text
     * @param userId
     * @throws QuestionException
     */
    public void updateAnswer(String question_id, String answer_id, String answer_text, String userId, String userEmail) throws QuestionException {
        logger.info("Entering PUT ANSWER service method");
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
                        snsService.postToSNSTopic(question_id,ans.getAnswer_id(),userId);
                        statsd.recordExecutionTime("DB ResponseTime - PUT ANSWER", System.currentTimeMillis() - startTime);

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

    /**
     * ensures question exists for given questionId
     * ensures answer exists for given answerId
     * ensures User is authorized to delete the answer
     * deletes all files attached to the answer
     * deletes the answer from DB
     * @param answer_id
     * @param question_id
     * @param userId
     * @throws QuestionException
     */
    public void deleteAnswer(String answer_id, String question_id, String userId, String userEmail) throws QuestionException {
        logger.info("Entering DELETE ANSWER service method");
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
                        snsService.postToSNSTopic(question_id,ans.getAnswer_id(),userId);
                        statsd.recordExecutionTime("DB ResponseTime - DELETE ANSWER", System.currentTimeMillis() - startTime);
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

    /**
     * Fetches Question form DB for the given questionId
     * @param question_id
     * @return
     * @throws QuestionException
     */
    public Question getQuestion(String question_id) throws QuestionException {
        logger.info("Entering GET QUESTION service method");
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

    /**
     * Fetches all the questions stored in the DB
     * @return
     * @throws QuestionException
     */
    public List<Question> getAllQuestions() throws QuestionException {
        logger.info("Entering GET ALL QUESTIONS service method");
        try {
            long startTime = System.currentTimeMillis();
            List<Question> foundQuestions = (List<Question>) questionRepo.findAll();
            statsd.recordExecutionTime("DB ResponseTime - GET ALL QUESTIONS", System.currentTimeMillis() - startTime);
            if (foundQuestions.isEmpty()) {
                throw new QuestionException(CustomStrings.not_found);
            } else {
                return foundQuestions;
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    /**
     * Fetches the Answer from DB for given answerId
     * @param answer_id
     * @return
     * @throws QuestionException
     */
    public Answer getAnswer(String answer_id) throws QuestionException {
        logger.info("Entering GET ANSWER service method");
        try {
            long startTime = System.currentTimeMillis();
            Optional<Answer> foundAnswer = answerRepo.findById(answer_id);
            statsd.recordExecutionTime("DB ResponseTime - GET ANSWER", System.currentTimeMillis() - startTime);
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
