package com.sumner.authentication.security.services;

import com.sumner.authentication.exception.UserAlreadyExistsException;
import com.sumner.authentication.models.User;
import com.sumner.authentication.models.VerificationToken;

public interface IUserService {
    
    User registerNewUserAccount(User user) 
      throws UserAlreadyExistsException;

    User getUser(String verificationToken);

    void saveRegisteredUser(User user);

    void createVerificationToken(User user, String token);

    VerificationToken getVerificationToken(String VerificationToken);
}
