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
        user.setId("1234");
        user.setFirst_name("Deepak");
        user.setLast_name("Gopalan");
        user.setUsername("csye6225@northeastern.edu");
        user.setPassword("somecrazypassword");
        UserPrincipal testUser =  new UserPrincipal(user.getId(),user.getFirst_name(),
                user.getLast_name(),user.getPassword(),user.getUsername());;

        return new InMemoryUserDetailsManager(testUser);
    }
}
