package com.deepak.assignment2.repository;

import com.deepak.assignment2.model.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, String> {
    public Category findByCategory(String category);
}
