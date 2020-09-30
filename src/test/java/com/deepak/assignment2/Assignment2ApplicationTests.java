package com.deepak.assignment2;

import com.deepak.assignment2.model.User;
import com.deepak.assignment2.service.UserService;
import com.deepak.assignment2.testpackage.SpringSecurityWebAuxTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = SpringSecurityWebAuxTestConfig.class
)
@AutoConfigureMockMvc
class Assignment2ApplicationTests {

    @Test
    void contextLoads() {
    }

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
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setEmail(email);
        user.setPassword(password);
        User returnedUser = new User();
        returnedUser.setId(id);
        returnedUser.setFirst_name(user.getFirst_name());
        returnedUser.setEmail(user.getEmail());
        returnedUser.setLast_name(user.getLast_name());
        returnedUser.setAccount_created(LocalDateTime.now().toString());
        returnedUser.setAccount_updated(LocalDateTime.now().toString());

        when(userService.createUser(user)).thenReturn(returnedUser);

        this.mockMvc.perform(post("/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").value(returnedUser.getId()))
                .andExpect(jsonPath("$.first_name").value(returnedUser.getFirst_name()))
                .andExpect(jsonPath("$.last_name").value(returnedUser.getLast_name()))
                .andExpect(jsonPath("$.email").value(returnedUser.getEmail()))
                .andExpect(jsonPath("$.account_created").value(returnedUser.getAccount_created()))
                .andExpect(jsonPath("$.account_updated").value(returnedUser.getAccount_updated()));
    }

    @Test
    @WithUserDetails("csye6225@northeastern.edu")
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
                .header("Authorization", "Basic Y3N5ZTYyMjVAbm9ydGhlYXN0ZXJuLmVkdTpzb21lY3JhenlwYXNzd29yZA==")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
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
    @WithUserDetails("csye6225@northeastern.edu")
    public void putUserShouldUpdateCorrespondingUser() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        User user = new User();
        user.setId(id);
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setEmail(email);
        when(userService.putUser(email, user)).thenReturn(user);

        this.mockMvc.perform(put("/v1/user/self")
                .header("Authorization", "Basic Y3N5ZTYyMjVAbm9ydGhlYXN0ZXJuLmVkdTpzb21lY3JhenlwYXNzd29yZA==")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)))
                .andExpect(status().isNoContent());
    }
}
