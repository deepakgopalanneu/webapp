package com.deepak.project.repository;

import com.deepak.project.model.Answer;
import org.springframework.data.repository.CrudRepository;

public interface AnswerRepository extends CrudRepository<Answer, String> {
}
