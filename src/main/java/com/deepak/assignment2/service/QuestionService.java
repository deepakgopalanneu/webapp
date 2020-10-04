package com.deepak.assignment2.service;

import com.deepak.assignment2.Exception.QuestionException;
import com.deepak.assignment2.model.Answer;
import com.deepak.assignment2.model.Category;
import com.deepak.assignment2.model.Question;
import com.deepak.assignment2.repository.AnswerRepository;
import com.deepak.assignment2.repository.CategoryRepository;
import com.deepak.assignment2.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    QuestionRepository questionRepo;
    @Autowired
    CategoryRepository categoryRepo;
    @Autowired
    AnswerRepository answerRepo;

//    @Transactional
    public Question createQuestion(Question question, String userId) throws QuestionException {
        question.setCreated_timestamp(LocalDateTime.now().toString());
        question.setUpdated_timestamp(LocalDateTime.now().toString());
        question.setUserId(userId);

        List<Category> categoryList = new ArrayList<>();
        try{
        question.getCategories().stream().forEach( (category) ->{
            String categoryName = category.getCategory().toLowerCase();
            Category c = categoryRepo.findByCategory(categoryName);
                if(null!=c)
                    categoryList.add(c);
                else{
                    Category addCategory = new Category();
                    addCategory.setCategory(categoryName);
                    categoryList.add(categoryRepo.save(addCategory));
                } });

            return questionRepo.save(question);
        }catch (Exception ex){
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

        try{
            return answerRepo.save(answer);
        }catch (Exception e){
            throw new QuestionException(e.getMessage());
        }
    }


    public void editQuestion(Question question, String question_id, String userId) throws QuestionException {

        try{
            Optional<Question> foundQuestion = questionRepo.findById(question_id);
                if(foundQuestion.isPresent()){
                    Question q = foundQuestion.get();
                    if(!q.getUserId().equals(userId)){
                        throw new QuestionException("You are not the owner of this question. you cannot delete/modify it");
                    }else{
                        q.setCategories(question.getCategories());
                        q.setQuestion_text(question.getQuestion_text());
                        q.setUpdated_timestamp(LocalDateTime.now().toString());
                        questionRepo.save(q);
                    }
                }else{
                    throw new QuestionException("Question not found");
                }
        }catch (Exception e){
            throw  new QuestionException(e.getMessage());
        }
    }

    public void deleteQuestion(String question_id, String userId) throws QuestionException {
        try{
            Optional<Question> foundQuestion = questionRepo.findById(question_id);
            if(foundQuestion.isPresent()){
                Question q = foundQuestion.get();
                if(!q.getUserId().equals(userId)){
                    throw new QuestionException("You are not the owner of this question. you cannot delete/modify it");
                }else{
                    questionRepo.delete(q);
                }
            }else{
                throw new QuestionException("Question not found");
            }
        }catch (Exception e){
            throw  new QuestionException(e.getMessage());
        }
    }

    public void updateAnswer(String question_id, String answer_id, String answer_text, String userId) throws QuestionException {
        try{
            if(null==answer_text || answer_text.equals("")) {
                throw new QuestionException("Answer Text cannot be empty");
            }
            Optional<Answer> foundAnswer = answerRepo.findById(answer_id);
            if(foundAnswer.isPresent()){
                Answer ans = foundAnswer.get();
                if(!ans.getUserId().equals(userId)){
                    throw new QuestionException("You are not the owner of this Answer. you cannot delete/modify it");
                }else{
                    ans.setAnswer_text(answer_text);
                    ans.setUpdated_timestamp(LocalDateTime.now().toString());
                    answerRepo.save(ans);
                }
            }else{
                throw new QuestionException("Answer not found");
            }
        }catch (Exception e){
            throw  new QuestionException(e.getMessage());
        }
    }

    public void deleteAnswer(String answer_id, String question_id, String userId) throws QuestionException {
        try{
            Optional<Answer> foundAnswer = answerRepo.findById(answer_id);
            if(foundAnswer.isPresent()){
                Answer ans = foundAnswer.get();
                if(!ans.getUserId().equals(userId)){
                    throw new QuestionException("You are not the owner of this Answer. you cannot delete/modify it");
                }else{
                    answerRepo.delete(ans);
                }
            }else{
                throw new QuestionException("Answer not found");
            }
        }catch (Exception e){
            throw  new QuestionException(e.getMessage());
        }
    }

    public Question getQuestion(String question_id) throws QuestionException {
        try{
            Optional<Question> foundQuestion = questionRepo.findById(question_id);
            if(foundQuestion.isPresent()){
                return foundQuestion.get();
            }else{
                throw new QuestionException("Question not found");
            }
        }catch (Exception e){
            throw  new QuestionException(e.getMessage());
        }
    }

    public List<Question> getAllQuestions() throws QuestionException {
        try{
            List<Question> foundQuestions = (List<Question>) questionRepo.findAll();
            if(foundQuestions.isEmpty()){
                throw new QuestionException("Question not found");
            }else{
                return foundQuestions;
            }
        }catch (Exception e){
            throw  new QuestionException(e.getMessage());
        }
    }

    public Answer getAnswer(String answer_id) throws QuestionException {
        try{
            Optional<Answer> foundAnswer = answerRepo.findById(answer_id);
            if(foundAnswer.isPresent()){
                Answer ans = foundAnswer.get();
                return ans;
            }else{
                throw new QuestionException("Answer not found");
            }
        }catch (Exception e){
            throw  new QuestionException(e.getMessage());
        }
    }
}
