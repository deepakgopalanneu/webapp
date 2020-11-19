package com.deepak.project.model;

import com.deepak.project.repository.UserRepository;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    UserRepository userRepo;
    StatsDClient statsd;
    private final static Logger logger = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Autowired
    public MyUserDetailsService(UserRepository userRepo, StatsDClient statsd) {
        this.userRepo = userRepo;
        this.statsd = statsd;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Authenticating User");
        long startTime = System.currentTimeMillis();
        User user = userRepo.findByUsername(email);
        statsd.recordExecutionTime("DB ResponseTime - USER AUTHENTICATION", System.currentTimeMillis() - startTime);
        if (null == user) {
            throw new UsernameNotFoundException("User not found for the given email address");
        } else {
            return new UserPrincipal(user.getId(), user.getFirst_name(),
                    user.getLast_name(), user.getPassword(), user.getUsername());
        }
    }
}
