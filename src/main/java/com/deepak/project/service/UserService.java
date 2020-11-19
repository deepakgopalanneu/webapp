package com.deepak.project.service;

import com.deepak.project.Exception.UserException;
import com.deepak.project.model.User;
import com.deepak.project.repository.UserRepository;
import com.deepak.project.util.CustomStrings;
import com.timgroup.statsd.StatsDClient;
import org.hibernate.NonUniqueObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    UserRepository userRepo;
    private PasswordEncoder passwordEncoder;
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);
    StatsDClient statsd;

    @Autowired
    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder, StatsDClient statsd) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.statsd = statsd;
    }

    /**
     * This method encodes the password, assigns timestamps and calls Repository to persist the User
     *
     * @param user - Valid User Object
     * @return Saved User Object
     * @throws UserException
     */
    public User createUser(User user) throws UserException {
        logger.info("Entering POST USER service method");
        try {
            User userExistsForUpdatedEmail = getUser(user.getUsername());
            if (null != userExistsForUpdatedEmail)
                throw new UserException("Conflict - Email address already in use");

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAccount_created(LocalDateTime.now().toString());
            user.setAccount_updated(LocalDateTime.now().toString());
            long startTime = System.currentTimeMillis();
            User u = null;
            try {
                u = userRepo.save(user);
            }catch (ConstraintViolationException e){
                throw new UserException(CustomStrings.user_conflict);
            }
            statsd.recordExecutionTime("DB ResponseTime - POST USER", System.currentTimeMillis() - startTime);
            return u;
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
        logger.info("Entering GET USER service method");
        try {
            long startTime = System.currentTimeMillis();
            User user = userRepo.findByUsername(email);
            statsd.recordExecutionTime("DB ResponseTime - GET USER", System.currentTimeMillis() - startTime);
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
        logger.info("Entering PUT USER service method");
        if (null != user.getAccount_updated() || null != user.getAccount_created()) {
            throw new UserException("Put Request should not contain account_updated or account_created fields");
        }
        User savedUser = null;
        if (!email.equals(user.getUsername())) {
            throw new UserException("Not allowed to update Email field");
        }
        try {
            savedUser = getUser(email);
            if(null!=savedUser) {
                savedUser.setPassword(passwordEncoder.encode(user.getPassword()));
                savedUser.setAccount_updated(LocalDateTime.now().toString());
                savedUser.setLast_name(user.getLast_name());
                savedUser.setFirst_name(user.getFirst_name());
                long startTime = System.currentTimeMillis();
                User u = userRepo.save(savedUser);
                statsd.recordExecutionTime("DB ResponseTime - PUT USER", System.currentTimeMillis() - startTime);
                return u;
            }else{
                throw new UserException(CustomStrings.user_not_found);
            }
        } catch (Exception ex) {
            throw new UserException(ex.getMessage());
        }
    }

    /**
     * This method fetches user form DB for given Id
     *
     * @param id
     * @return
     * @throws UserException
     */
    public User getUnknownUser(String id) throws UserException {
        logger.info("Logging from GET USER_BY_ID service method");
        try {
            long startTime = System.currentTimeMillis();
            Optional<User> optional = userRepo.findById(id);
            statsd.recordExecutionTime("DB ResponseTime - GET USER_BY_ID", System.currentTimeMillis() - startTime);
            if (optional.isPresent())
                return optional.get();
            else
                throw new UserException(CustomStrings.user_not_found);
        } catch (Exception e) {
            throw new UserException(e.getMessage());
        }
    }
}
