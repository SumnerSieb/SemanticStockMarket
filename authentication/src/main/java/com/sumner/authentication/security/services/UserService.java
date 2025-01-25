package com.sumner.authentication.security.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;


import com.sumner.authentication.exception.UserAlreadyExistsException;
import com.sumner.authentication.models.Role;
import com.sumner.authentication.models.User;
import com.sumner.authentication.models.VerificationToken;
import com.sumner.authentication.repository.UserRepository;
import com.sumner.authentication.repository.VerificationTokenRepository;

@Service
@Transactional
public class UserService implements IUserService {
    
    @Autowired
    private UserRepository repository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Override
    public User registerNewUserAccount(User userIn) 
      throws UserAlreadyExistsException {
        
        if (emailExist(userIn.getEmail())) {
            throw new UserAlreadyExistsException(
              "There is an account with that email adress: " 
              + userIn.getEmail());
        }
        
        User user = new User();
        user.setUsername(userIn.getUsername());
        user.setPassword(userIn.getPassword());
        user.setEmail(userIn.getEmail());
        user.setRoles(userIn.getRoles());
        return repository.save(user);
    }

    private boolean emailExist(String email) {
        return repository.existsByEmail(email);
    }
    
    @Override
    public User getUser(String verificationToken) {
        User user = tokenRepository.findByToken(verificationToken).getUser();
        return user;
    }
    
    @Override
    public VerificationToken getVerificationToken(String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }
    
    @Override
    public void saveRegisteredUser(User user) {
        repository.save(user);
    }
    
    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }
}
