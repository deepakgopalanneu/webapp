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

    @Autowired
    QuestionRepository questionRepo;
    @Autowired
    CategoryRepository categoryRepo;
    @Autowired
    AnswerRepository answerRepo;
    @Autowired
    FileService fileService;

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public Question createQuestion(Question question, String userId) throws QuestionException {
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
            return questionRepo.save(question);
        } catch (Exception ex) {
            throw new QuestionException(ex.getMessage());
        }
    }

    public Answer answerQuestion(String question_id, String userId, String answer_text) throws QuestionException {
        Answer answer = new Answer();
        answer.setAnswer_text(answer_text);
        answer.setUserId(userId);
        answer.setQuestion_id(question_id);
        answer.setCreated_timestamp(LocalDateTime.now().toString());
        answer.setUpdated_timestamp(LocalDateTime.now().toString());
        answer.setAttachments(new ArrayList<>());
        try {
            Optional<Question> q = questionRepo.findById(question_id);
            if (q.isPresent())
                return answerRepo.save(answer);
            else
                throw new QuestionException(CustomStrings.not_found);
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    public void editQuestion(Question question, String question_id, String userId) throws QuestionException {

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
                    questionRepo.save(q);
                }
            } else {
                throw new QuestionException(CustomStrings.not_found);
            }
        } catch (Exception e) {
            throw new QuestionException(e.getMessage());
        }
    }

    private void deleteImagesFromS3(List<File> attachments) {
        attachments.stream().forEach(f -> {
            try {
                fileService.deleteImageBys3ObjectName(f.getS3ObjectName());
            } catch (FileException e) {
                throw new RuntimeException("Unable to Delete Images attached to this Question");
            }
        });
    }

    public void deleteQuestion(String question_id, String userId) throws QuestionException {
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
                    questionRepo.deleteById(question_id);
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
                        answerRepo.save(ans);
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
                        answerRepo.deleteById(ans.getAnswer_id());
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
        try {
            Optional<Question> foundQuestion = questionRepo.findById(question_id);
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
        try {
            List<Question> foundQuestions = (List<Question>) questionRepo.findAll();
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
        try {
            Optional<Answer> foundAnswer = answerRepo.findById(answer_id);
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
