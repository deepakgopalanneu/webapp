package com.deepak.project.model;

import com.deepak.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepo.findByUsername(email);

        if (null == user) {
            throw new UsernameNotFoundException("User not found for the given email address");
        } else {
            return new UserPrincipal(user.getId(), user.getFirst_name(),
                    user.getLast_name(), user.getPassword(), user.getUsername());
        }
    }
}