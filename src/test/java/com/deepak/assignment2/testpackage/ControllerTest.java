package com.deepak.assignment2.testpackage;

import com.deepak.assignment2.model.User;
import com.deepak.assignment2.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private UserService userService;

    private final String id = "ff80818174d7e84e0174d7eaf6c50";
    private final String fname = "someFirstName";
    private final String lname = "somelastName";
    private final String email = "csye6225@northeastern.edu";
    private final String password = "somecrazypassword";

    @Test
    public void createUserMethodShouldCreateUser() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        User user = new User();
        user.setId(id);
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setEmail(email);
        user.setPassword(password);
        user.setAccount_created(LocalDateTime.now().toString());
        user.setAccount_updated(LocalDateTime.now().toString());

        when(userService.createUser(user)).thenReturn(user);

        this.mockMvc.perform(post("/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.first_name").value(user.getFirst_name()))
                .andExpect(jsonPath("$.last_name").value(user.getLast_name()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.account_created").value(user.getAccount_created()))
                .andExpect(jsonPath("$.account_updated").value(user.getAccount_updated()));
    }

    @Test
    public void getUserShouldGetCorrespondingUser() throws Exception {

        User user = new User();
        user.setId(id);
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setEmail(email);
        user.setAccount_created(LocalDateTime.now().toString());
        user.setAccount_updated(LocalDateTime.now().toString());
        when(userService.getUser(email)).thenReturn(user);

        this.mockMvc.perform(get("/v1/user/self")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.first_name").value(user.getFirst_name()))
                .andExpect(jsonPath("$.last_name").value(user.getLast_name()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.account_created").value(user.getAccount_created()))
                .andExpect(jsonPath("$.account_updated").value(user.getAccount_updated()));
    }

    @Test
    public void putUserShouldUpdateCorrespondingUser() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        User user = new User();
        user.setId(id);
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setEmail(email);
        user.setAccount_created(LocalDateTime.now().toString());
        user.setAccount_updated(LocalDateTime.now().toString());
        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setFirst_name("updatedfname");
        updatedUser.setLast_name(lname);
        updatedUser.setEmail(email);
        updatedUser.setAccount_created(user.getAccount_created());
        updatedUser.setAccount_updated(LocalDateTime.now().toString());
        when(userService.putUser(email, user)).thenReturn(updatedUser);

        this.mockMvc.perform(get("/v1/user/self")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.first_name").value(updatedUser.getFirst_name()))
                .andExpect(jsonPath("$.last_name").value(updatedUser.getLast_name()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.account_created").value(user.getAccount_created()))
                .andExpect(jsonPath("$.account_updated").value(updatedUser.getAccount_updated()));
    }
}
