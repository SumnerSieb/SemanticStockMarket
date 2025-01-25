package com.sumner.authentication.listeners;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.sumner.authentication.events.OnRegistrationCompleteEvent;
import com.sumner.authentication.models.User;
import com.sumner.authentication.security.services.IUserService;
import com.sumner.authentication.security.services.UserDetailsImpl;

import java.io.File;
import java.util.UUID;

@Component
public class RegistrationListener implements 
  ApplicationListener<OnRegistrationCompleteEvent> {
 
    @Autowired
    private IUserService service;
 
    @Autowired
    private MessageSource messages;
 
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        service.createVerificationToken(user, token);
        
        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";
        String confirmationUrl 
          = event.getAppUrl() + "/regitrationConfirm?token=" + token;
        String devMessage = "Please use the following token to complete your registration:\n\n" + token;
        
        String message = messages.getMessage("message.regSucc", null, event.getLocale());
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
    
            String htmlMsg = "<h3>" + message + "</h3>"
                    + "<p>To confirm your registration, please click the following link:</p>"
                    + "<a href=\"" + confirmationUrl + "\">Confirm Registration</a>" +
                    "<p> href=\"" + confirmationUrl + "\">Confirm Registration</a>";
            
            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(devMessage, false);  // `true` enables HTML
    
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Error sending confirmation email: " + e.getMessage());
        }
    }
    
}
