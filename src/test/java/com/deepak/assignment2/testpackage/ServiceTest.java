package com.deepak.assignment2.testpackage;

import com.deepak.assignment2.repository.UserRepository;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

//@ExtendWith(MockitoExtension.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceTest {

//    @MockBean
    private UserRepository userRepo;

    private final String id = "ff80818174d7e84e0174d7eaf6c50";
    private final String fname = "someFirstName";
    private final String lname = "somelastName";
    private final String email = "csye6225@northeastern.edu";
    private final String password = "somecrazypassword";

    public void createUserShouldReturnUser(){

    }

    public void createUserShouldThrowUserExceptionForEmailConflict(){

    }

    public void getUserShouldReturnUser(){

    }

    public void getUserShouldThrowExceptionIfUserDoesntExist(){

    }

    public void putUserShouldReturnUser(){

    }

    public void putUserShouldThrowExceptionForEmailUpdate(){

    }



}
