package com.deepak.assignment2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan(basePackages = "com.deepak.assignment2.*")
public class Assignment2Application {

    public static void main(String[] args) {
        SpringApplication.run(Assignment2Application.class, args);
    }

}