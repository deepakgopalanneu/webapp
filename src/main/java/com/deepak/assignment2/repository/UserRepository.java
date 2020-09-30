package com.deepak.assignment2.repository;

import com.deepak.assignment2.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

    User findByEmail(String email);
}
