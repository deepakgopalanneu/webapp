package com.deepak.project.repository;

import com.deepak.project.model.File;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FileRepository extends CrudRepository<File, String> {
    Optional<File> findByS3ObjectName(String s3ObjectName);
}
