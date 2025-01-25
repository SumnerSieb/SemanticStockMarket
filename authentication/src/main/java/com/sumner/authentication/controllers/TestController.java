package com.sumner.authentication.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
  @GetMapping("/all")
  public String allAccess() {
    return "Public Content.";
  }

  @GetMapping("/user")
  @PreAuthorize("hasRole('USER') or hasRole('COMMISSIONER') or hasRole('ADMIN') or hasRole('PREMIUM')")
  public String userAccess() {
    return "User Content.";
  }

  @GetMapping("/com")
  @PreAuthorize("hasRole('COMMISSIONER')")
  public String moderatorAccess() {
    return "Comissioner Board.";
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminAccess() {
    return "Admin Board.";
  }

  @GetMapping("/premium")
  @PreAuthorize("hasRole('PREMIUM')")
  public String premiumAccess() {
    return "Premium Board.";
  }
}
