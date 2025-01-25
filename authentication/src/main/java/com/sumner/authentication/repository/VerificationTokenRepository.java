package com.sumner.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sumner.authentication.models.User;
import com.sumner.authentication.models.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);
}
