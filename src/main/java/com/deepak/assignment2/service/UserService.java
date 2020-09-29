package com.deepak.assignment2.service;

import com.deepak.assignment2.Exception.UserException;
import com.deepak.assignment2.model.User;
import com.deepak.assignment2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
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

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccount_created(LocalDateTime.now().toString());
        user.setAccount_updated(LocalDateTime.now().toString());

        try {
            User userExistsForUpdatedEmail = getUser(user.getEmail());
            if(null != userExistsForUpdatedEmail)
                throw new UserException("Conflict - Email address already in use");
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
            User user = userRepo.findByEmail(email);
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
     * @return
     * @throws UserException
     */
    public User putUser(String email, User user) throws UserException {
        User userExistsForUpdatedEmail =null;
        if (!email.equals(user.getEmail())) {
            try{
                userExistsForUpdatedEmail = getUser(user.getEmail());
                if(null != userExistsForUpdatedEmail)
                    throw new UserException("Conflict - Email address already in use");

            }
            catch (Exception ex){
                throw new UserException(ex.getMessage());
            }
        }
        try{
            userExistsForUpdatedEmail = getUser(email);
            if(null!= userExistsForUpdatedEmail) {
                userExistsForUpdatedEmail.setPassword(passwordEncoder.encode(user.getPassword()));
                userExistsForUpdatedEmail.setAccount_updated(LocalDateTime.now().toString());
                userExistsForUpdatedEmail.setLast_name(user.getLast_name());
                userExistsForUpdatedEmail.setFirst_name(user.getFirst_name());
                userExistsForUpdatedEmail.setEmail(user.getEmail());
            }else{
                userExistsForUpdatedEmail = user;
            }
            return userRepo.save(userExistsForUpdatedEmail);
            } catch (Exception ex){
                throw new UserException(ex.getMessage());
            }
    }
}
