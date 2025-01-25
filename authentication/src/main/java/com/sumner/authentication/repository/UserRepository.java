package com.sumner.authentication.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sumner.authentication.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
  
    Boolean existsByUsername(String username);
  
    Boolean existsByEmail(String email);
  }
