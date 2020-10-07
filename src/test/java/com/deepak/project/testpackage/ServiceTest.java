package com.deepak.project.testpackage;

import com.deepak.project.Exception.UserException;
import com.deepak.project.model.User;
import com.deepak.project.repository.UserRepository;
import com.deepak.project.service.UserService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

//@ExtendWith(MockitoExtension.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class ServiceTest {

    private final String id = "ff80818174d7e84e0174d7eaf6c50";
    private final String fname = "someFirstName";
    private final String lname = "somelastName";
    private final String email = "csye6225@northeastern.edu";
    private final String password = "somecrazypassword";

    @Mock
    private UserRepository userRepo;
    @Mock
    private PasswordEncoder encoder;
    @InjectMocks
    private UserService userService;

//    @BeforeAll
//    public void init() {
//        user = new User();
//        user.setFirst_name(fname);
//        user.setLast_name(lname);
//        user.setEmail(email);
//        user.setPassword(password);
//    }

    //    @Test
    public void createUserServiceShouldReturnUser() throws UserException {
        User returnedUser = new User();
        User user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        returnedUser.setId(id);
        returnedUser.setFirst_name(user.getFirst_name());
        returnedUser.setLast_name(user.getLast_name());
        returnedUser.setUsername(user.getUsername());
        returnedUser.setPassword(encoder.encode(user.getPassword()));
        returnedUser.setAccount_created(LocalDateTime.now().toString());
        returnedUser.setAccount_updated(LocalDateTime.now().toString());

        when(userRepo.save(user)).thenReturn(returnedUser);

        User u = userService.createUser(user);
        assertThat(u).isNotNull();
//        we can add more assertions here
    }

    //    @Test
//    To be fixed
    public void createUserServiceShouldThrowUserExceptionForEmailConflict() throws UserException {
        User user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
//        UserException ex = new UserException("Conflict - Email address already in use");
        when(userService.getUser(user.getUsername())).thenThrow(UserException.class);
        assertThrows(UserException.class, () -> userService.createUser(user));
    }

    //    @Test
    public void getUserServiceShouldReturnUser() throws UserException {
        User user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        when(userRepo.findByUsername(user.getUsername())).thenReturn(user);
        User u = userService.getUser(user.getUsername());
        assertThat(u).isNotNull();
    }

    //    @Test
//    To be fixed
    public void getUserServiceShouldThrowExceptionIfUserDoesntExist() throws UserException {
        User user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        when(userRepo.findByUsername(user.getUsername())).thenThrow(UserException.class);
        User u = userService.getUser(user.getUsername());
        assertThrows(UserException.class, () -> userService.getUser(user.getUsername()));
    }

    //    @Test
//    To be fixed
    public void putUserServiceShouldReturnUser() throws UserException {
        User user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        when(userRepo.findByUsername(user.getUsername())).thenReturn(user);
        User u = userService.putUser(user.getUsername(), user);
        assertThat(u).isNotNull();
    }

    //    @Test
    public void putUserServiceShouldThrowExceptionForEmailUpdate() {
        User user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(email);
        user.setPassword(password);
        assertThrows(UserException.class, () -> userService.putUser("random@mail.com", user));
    }


}
