package com.deepak.project.repository;

import com.deepak.project.model.File;
import org.springframework.data.repository.CrudRepository;

public interface FileRepository extends CrudRepository<File, String> {
}
