package com.deepak.assignment2.testpackage;

import com.deepak.assignment2.model.User;
import com.deepak.assignment2.model.UserPrincipal;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;


@TestConfiguration
public class SpringSecurityWebAuxTestConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        User user = new User();
        user.setEmail("csye6225@northeastern.edu");
        user.setPassword("somecrazypassword");
        UserPrincipal testUser = new UserPrincipal(user);

        return new InMemoryUserDetailsManager( testUser);
    }
}
