package com.deepak.project.repository;

import com.deepak.project.model.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, String> {
    public Category findByCategory(String category);
}
