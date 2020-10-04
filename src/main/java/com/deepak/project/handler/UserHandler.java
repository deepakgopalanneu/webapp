package com.deepak.project.handler;

import com.deepak.project.Exception.UserException;
import com.deepak.project.model.User;
import com.deepak.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Base64;
import java.util.regex.Pattern;

@RestController
public class UserHandler {

    UserService userService;

    @Autowired
    public UserHandler(UserService service) {
        this.userService = service;
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
     * @param value
     * @return String email address decoded from the Authorization header
     */
    public static String extractEmailFromHeader(String value) {

        String credentials = value.split(" ")[1];
        String decodedCredentials = new String(Base64.getDecoder().decode(credentials));
        return decodedCredentials.split(":")[0];
    }

    /**
     * This method validates the input and persists in the Database
     *
     * @param user RequestBody should contain a valid User Object
     * @return ResponseEntity with Body of type User
     * @throws UserException
     */
    @PostMapping("/v1/user")
    public ResponseEntity<User> createUser(@RequestBody @NotNull @Valid User user ) throws UserException {

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(validatedUser(user)));
    }

    /**
     * This method analyzes the user and returns corresponding resource as response
     *
     * @param value is the Basic Authorization String sent by Consumer
     * @return ResponseEntity with Body of type User
     * @throws UserException
     */
    @GetMapping("/v1/user/self")
    public ResponseEntity<User> getUser(@RequestHeader("Authorization") @NotNull @Valid String value ) throws UserException {

        User u = userService.getUser(extractEmailFromHeader(value));
        if (null != u)
            return ResponseEntity.ok(u);
        else
            throw new UserException("User Not Found");
    }

    /**
     * @param value is the Basic Authorization String sent by Consumer
     * @param user  RequestBody should contain a valid User Object
     * @return ResponseEntity with Body of type User
     * @throws UserException
     */
    @PutMapping("/v1/user/self")
    public ResponseEntity putUser(@RequestHeader("Authorization") String value, @RequestBody  @NotNull @Valid User user) throws UserException {

        userService.putUser(extractEmailFromHeader(value), validatedUser(user));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/v1/user/{id}")
    public ResponseEntity<User> getUnknownUser(@PathVariable("id") @NotNull String id ) throws  UserException{
        return ResponseEntity.ok(userService.getUnknownUser(id));
    }
}
