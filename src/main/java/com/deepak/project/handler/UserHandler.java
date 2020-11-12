package com.deepak.project.handler;

import com.deepak.project.Exception.UserException;
import com.deepak.project.model.User;
import com.deepak.project.model.UserPrincipal;
import com.deepak.project.service.UserService;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.Base64;
import java.util.regex.Pattern;

@RestController
public class UserHandler {

    private UserService userService;
    private final static Logger logger = LoggerFactory.getLogger(UserHandler.class);
    StatsDClient statsd;

    @Autowired
    public UserHandler(UserService userService, StatsDClient statsd) {
        this.userService = userService;
        this.statsd = statsd;
    }

    /**
     * This method verifies the first_name, last_name, email and Password strength
     *
     * @param user
     * @return Valid User Object
     * @throws UserException
     */
    public static User validatedUser(User user) throws UserException {
        if (null != user) {
            String firstName = user.getFirst_name();
            String email = user.getUsername();
            String lastName = user.getLast_name();
            String password = user.getPassword();
            String passwordRegex = "^(?=.*?[a-zA-Z0-9#?!@$%^&*-]).{8,255}$";
            String nameRegex = "^(?=.{1,60}$)[a-zA-Z]+(?:[-'\\s][a-zA-Z]+)*$";
            String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
            if (null != email) {
                if (!Pattern.matches(emailRegex, email)) {
                    throw new UserException("invalid email");
                }
            }
            if (null != password) {
                if (!Pattern.matches(passwordRegex, password)) {
                    throw new UserException("Password should be between 8 & 255 character! may or may not contain numbers and special characters ");
                }
                if (null != firstName) {
                    if (!Pattern.matches(nameRegex, firstName)) {
                        throw new UserException("invalid firstname");
                    }
                }
                if (null != lastName) {
                    if (!Pattern.matches(nameRegex, lastName)) {
                        throw new UserException("invalid lastName");
                    }
                }

            }
            return user;
        } else
            throw new UserException("Invalid User Object Provided");
    }



    /**
     * This method validates the input and persists in the Database
     *
     * @param user RequestBody should contain a valid User Object
     * @return ResponseEntity with Body of type User
     * @throws UserException
     */
    @PostMapping("/v1/user")
    public ResponseEntity<User> createUser(@RequestBody @NotNull @Valid User user) throws UserException {
        logger.info("Logging from POST /v1/user controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - POST /v1/user");
        User savedUser = userService.createUser(validatedUser(user));
        statsd.recordExecutionTime("ResponseTime - GET /v1/user/self",System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    /**
     * This method analyzes the user and returns corresponding resource as response
     *
     * @return ResponseEntity with Body of type User
     * @throws UserException
     */
    @GetMapping("/v1/user/self")
    public ResponseEntity<User> getUser( Principal principal) throws UserException {
        logger.info("Logging from GET /v1/user/self controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - GET /v1/user/self");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        User u = userService.getUser(userPrincipal.getUsername());
        statsd.recordExecutionTime("ResponseTime - GET /v1/user/self", System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(u);

    }

    /**
     * @param user  RequestBody should contain a valid User Object
     * @return ResponseEntity with Body of type User
     * @throws UserException
     */
    @PutMapping("/v1/user/self")
    public ResponseEntity putUser( Principal principal, @RequestBody @NotNull @Valid User user) throws UserException {
        logger.info("Logging from PUT /v1/user/self controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic - PUT /v1/user/self");
        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
        userService.putUser(userPrincipal.getUsername(), validatedUser(user));
        statsd.recordExecutionTime("ResponseTime - PUT /v1/user/self", System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * This method is used to retrieve user info by Id - does not require authentication
     * @param id
     * @return User with the given Id
     * @throws UserException
     */
    @GetMapping("/v1/user/{id}")
    public ResponseEntity<User> getUnknownUser(@PathVariable("id") @NotNull String id) throws UserException {
        logger.info("Logging from GET /v1/user/{id} controller method");
        long startTime = System.currentTimeMillis();
        statsd.increment("Traffic to GET /v1/user/{id}");
        User u = userService.getUnknownUser(id);
        statsd.recordExecutionTime("ResponseTime - GET /v1/user/self", System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(u);
    }
}
