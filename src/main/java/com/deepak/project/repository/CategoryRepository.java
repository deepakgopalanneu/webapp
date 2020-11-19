package com.deepak.project.repository;

import com.deepak.project.model.Category;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoryRepository extends CrudRepository<Category, String> {
    public List<Category> findByCategory(String category);
}
