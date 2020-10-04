package com.deepak.project.repository;

import com.deepak.project.model.Question;
import org.springframework.data.repository.CrudRepository;

public interface QuestionRepository extends CrudRepository<Question, String> {
}
