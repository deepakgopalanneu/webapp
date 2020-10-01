package com.deepak.assignment2.testpackage;

import com.deepak.assignment2.handler.UserHandler;
import com.deepak.assignment2.model.User;
import com.deepak.assignment2.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ExtendWith(SpringExtension.class)
//@DataJpaTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        classes = SpringSecurityWebAuxTestConfig.class)
//@AutoConfigureMockMvc
//@ContextConfiguration(classes = {UserHandler.class, UserService.class})
//@WebMvcTest
public class IntegrationTest {

    private final String id = "ff80818174d7e84e0174d7eaf6c50";
    private final String fname = "someFirstName";
    private final String lname = "somelastName";
    private final String email = "csye6225@northeastern.edu";
    private final String password = "somecrazypassword";
    private final String header = "Basic Y3N5ZTYyMjVAbm9ydGhlYXN0ZXJuLmVkdTpzb21lY3JhenlwYXNzd29yZA==";
    @Autowired
    private MockMvc mockMvc;

    //    @Test
    public void createUserIntegrationTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        User user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setEmail(email);
        user.setPassword(password);
        this.mockMvc.perform(post("/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.first_name").value(user.getFirst_name()))
                .andExpect(jsonPath("$.last_name").value(user.getLast_name()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.account_created").exists())
                .andExpect(jsonPath("$.account_updated").exists());
    }

}
