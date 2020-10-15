package com.deepak.project.service;

import com.deepak.project.Exception.UserException;
import com.deepak.project.model.User;
import com.deepak.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    final String user_not_found = "User Not found";
    @Autowired
    UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * This method encodes the password, assigns timestamps and calls Repository to persist the User
     *
     * @param user - Valid User Object
     * @return Saved User Object
     * @throws UserException
     */
    public User createUser(User user) throws UserException {

        try {
            User userExistsForUpdatedEmail = getUser(user.getUsername());
            if (null != userExistsForUpdatedEmail)
                throw new UserException("Conflict - Email address already in use");

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAccount_created(LocalDateTime.now().toString());
            user.setAccount_updated(LocalDateTime.now().toString());
            return userRepo.save(user);
        } catch (Exception e) {
            throw new UserException(e.getMessage());
        }
    }

    /**
     * this method finds the user with the given email address
     *
     * @param email Valid Email address
     * @return User Object returned by the Repository
     * @throws UserException
     */
    public User getUser(String email) throws UserException {

        try {
            User user = userRepo.findByUsername(email);
            if (null != user)
                return user;
            else
                return null;
        } catch (Exception e) {
            throw new UserException(e.getMessage());
        }
    }

    /**
     * @param email Valid Email address
     * @param user  Value to be updated
     * @return updated user object
     * @throws UserException
     */
    public User putUser(String email, User user) throws UserException {
        if (null != user.getAccount_updated() || null != user.getAccount_created()) {
            throw new UserException("Put Request should not contain account_updated or account_created fields");
        }
        User userExistsForUpdatedEmail = null;
        if (!email.equals(user.getUsername())) {
            throw new UserException("Not allowed to update Email field");
        }
        try {
            userExistsForUpdatedEmail = getUser(email);
            if (null != userExistsForUpdatedEmail) {
                userExistsForUpdatedEmail.setPassword(passwordEncoder.encode(user.getPassword()));
                userExistsForUpdatedEmail.setAccount_updated(LocalDateTime.now().toString());
                userExistsForUpdatedEmail.setLast_name(user.getLast_name());
                userExistsForUpdatedEmail.setFirst_name(user.getFirst_name());
            } else {
                userExistsForUpdatedEmail = user;
            }
            return userRepo.save(userExistsForUpdatedEmail);
        } catch (Exception ex) {
            throw new UserException(ex.getMessage());
        }
    }

    public User getUnknownUser(String id) throws UserException {
        try {
            Optional<User> optional = userRepo.findById(id);
            if (optional.isPresent())
                return optional.get();
            else
                throw new UserException(user_not_found);
        } catch (Exception e) {
            throw new UserException(e.getMessage());
        }
    }
}
