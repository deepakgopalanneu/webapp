package com.deepak.assignment2.repository;

import com.deepak.assignment2.model.Question;
import org.springframework.data.repository.CrudRepository;

public interface QuestionRepository extends CrudRepository<Question, String> {
}
