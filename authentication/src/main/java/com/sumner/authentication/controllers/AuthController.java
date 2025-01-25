package com.sumner.authentication.controllers;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.sumner.authentication.events.OnRegistrationCompleteEvent;
import com.sumner.authentication.exception.TokenRefreshException;
import com.sumner.authentication.models.ERole;
import com.sumner.authentication.models.Role;
import com.sumner.authentication.models.User;
import com.sumner.authentication.models.VerificationToken;
import com.sumner.authentication.models.RefreshToken;
import com.sumner.authentication.payload.request.LoginRequest;
import com.sumner.authentication.payload.request.SignupRequest;
import com.sumner.authentication.payload.request.TokenRefreshRequest;
import com.sumner.authentication.payload.response.JwtResponse;
import com.sumner.authentication.payload.response.MessageResponse;
import com.sumner.authentication.payload.response.TokenRefreshResponse;
import com.sumner.authentication.repository.RoleRepository;
import com.sumner.authentication.repository.UserRepository;
import com.sumner.authentication.security.jwt.JwtUtils;
import com.sumner.authentication.security.services.UserDetailsImpl;
import com.sumner.authentication.security.services.IUserService;
import com.sumner.authentication.security.services.RefreshTokenService;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messages;

    @Autowired
    private IUserService service;

    @Value("${frontend.host}")
    private String frontendHost;

    @Value("${frontend.port}")
    private String frontendPort;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      Authentication authentication = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtUtils.generateJwtToken(authentication);

      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
          .collect(Collectors.toList());


      RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

      return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
    }
    catch (DisabledException ex) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Error: Account not verified."));}
    catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: Bad credentials."));
    }  
  }

  @PostMapping("/refreshtoken")
  public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
    String requestRefreshToken = request.getRefreshToken();

    return refreshTokenService.findByToken(requestRefreshToken)
        .map(refreshTokenService::verifyExpiration)
        .map(RefreshToken::getUser)
        .map(user -> {
          String token = jwtUtils.generateTokenFromUsername(user.getUsername());
          return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
        })
        .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
            "Refresh token is not in database!"));
  }

  
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, HttpServletRequest request) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
        encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "admin":
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(adminRole);

          break;
        case "premium":
          Role modRole = roleRepository.findByName(ERole.ROLE_PREMIUM)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(modRole);

          break;

          case "com":
          Role comRole = roleRepository.findByName(ERole.ROLE_COMMISSIONER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(comRole);

          break;
        default:
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    String appUrl = request.getContextPath();
    eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }


  @GetMapping("/registrationconfirm")
  public String confirmRegistration(WebRequest request, @RequestParam("token") String token) {
    Locale locale = request.getLocale();

    VerificationToken verificationToken = service.getVerificationToken(token);
    if (verificationToken == null) {
        // Redirect to React route for invalid token
        return "redirect:" + frontendHost + ":" + frontendPort + "/confirm?status=invalid";
    }

    User user = verificationToken.getUser();
    Calendar cal = Calendar.getInstance();

    if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
        // Redirect to React route for expired token
        return "redirect:" + frontendHost + ":" + frontendPort + "/confirm?status=expired";
    }

    // Enable the user
    user.setEnabled(true);
    service.saveRegisteredUser(user);

    // Redirect to React route for successful confirmation
    return "redirect:" + frontendHost + ":" + frontendPort + "/confirm?status=success";
}
}

