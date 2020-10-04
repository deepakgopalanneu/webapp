package com.deepak.assignment2;

import com.deepak.assignment2.Exception.UserException;
import com.deepak.assignment2.handler.UserHandler;
import com.deepak.assignment2.model.User;
import com.deepak.assignment2.service.UserService;
import com.deepak.assignment2.testpackage.SpringSecurityWebAuxTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testable
class Assignment2ApplicationTests {

    private final String id = "ff80818174d7e84e0174d7eaf6c50";
    private final String fname = "someFirstName";
    private final String lname = "somelastName";
    private final String email = "csye6225@northeastern.edu";
    private final String password = "somecrazypassword";
    private final String header = "Basic Y3N5ZTYyMjVAbm9ydGhlYXN0ZXJuLmVkdTpzb21lY3JhenlwYXNzd29yZA==";
//    @Autowired
    private MockMvc mockMvc;
//    @MockBean
    private UserService userService;
    private User user;

//    @Test
    void contextLoads() {
    }

//    @BeforeAll
//    public void init() {
//        user = new User();
//        user.setFirst_name(fname);
//        user.setLast_name(lname);
//        user.setEmail(email);
//        user.setPassword(password);
//    }


//    @Test
    public void createUserMethodShouldCreateUser() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        User returnedUser = new User();
        returnedUser.setId(id);
        returnedUser.setFirst_name(user.getFirst_name());
        returnedUser.setUsername(user.getUsername());
        returnedUser.setPassword(user.getPassword());
        returnedUser.setLast_name(user.getLast_name());
        returnedUser.setAccount_created(LocalDateTime.now().toString());
        returnedUser.setAccount_updated(LocalDateTime.now().toString());

        when(userService.createUser(any(User.class))).thenReturn(returnedUser);

        this.mockMvc.perform(post("/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.first_name").value(user.getFirst_name()))
                .andExpect(jsonPath("$.last_name").value(user.getLast_name()))
                .andExpect(jsonPath("$.id").value(returnedUser.getId()))
                .andExpect(jsonPath("$.email").value(user.getUsername()))
                .andExpect(jsonPath("$.account_created").value(returnedUser.getAccount_created()))
                .andExpect(jsonPath("$.account_updated").value(returnedUser.getAccount_updated()));
    }

//    @Test
//    @WithUserDetails("csye6225@northeastern.edu")
    public void getUserShouldGetCorrespondingUser() throws Exception {

        user.setAccount_created(LocalDateTime.now().toString());
        user.setAccount_updated(LocalDateTime.now().toString());
        when(userService.getUser(email)).thenReturn(user);

        this.mockMvc.perform(get("/v1/user/self")
                .header("Authorization", header)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.first_name").value(user.getFirst_name()))
                .andExpect(jsonPath("$.last_name").value(user.getLast_name()))
                .andExpect(jsonPath("$.email").value(user.getUsername()))
                .andExpect(jsonPath("$.account_created").value(user.getAccount_created()))
                .andExpect(jsonPath("$.account_updated").value(user.getAccount_updated()));
    }

//    @Test
//    @WithUserDetails("csye6225@northeastern.edu")
    public void putUserShouldUpdateCorrespondingUser() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        when(userService.putUser(email, user)).thenReturn(user);

        this.mockMvc.perform(put("/v1/user/self")
                .header("Authorization", header)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }


    @Test
    public void extractEmailFromHeaderShouldDecodeCredentials() {

        assertTrue(UserHandler.extractEmailFromHeader(header).equals(email));

    }

    @Test
    public void validatedUserShouldValidateFirstName() throws UserException {
        user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        user.setFirst_name("");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

        user.setFirst_name("abc123");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

        user.setFirst_name("abc!@");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));
    }

    @Test
    public void validatedUserShouldValidateLastName() {
        user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        user.setLast_name("");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

        user.setLast_name("abc123");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

        user.setLast_name("abc!@");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

    }

    @Test
    public void validatedUserShouldValidateEmail() {
                user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        user.setUsername("email");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

        user.setUsername("deepak.com");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

        user.setUsername("deepak@com");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));


    }

    @Test
    public void validatedUserShouldValidatePassword() {
        user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        user.setPassword("");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

        user.setPassword("1234567");
        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

//        byte[] array = new byte[256]; // length is bounded by 7
//        new Random().nextBytes(array);
//        user.setPassword(new String(array, Charset.forName("UTF-8")));
//        assertThrows(UserException.class, () -> UserHandler.validatedUser(user));

    }


}

