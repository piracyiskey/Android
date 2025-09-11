package org.convenient.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset");
        message.setText("Your OTP code is: " + otp + "\nIt will expire in 5 minutes.");
        mailSender.send(message);
    }

    // Store OTP in cache
    @CachePut(value = "otpCache", key = "#email")
    public String storeOtp(String email, String otp) {
        return otp;
    }

    // Get OTP from cache
    @Cacheable(value = "otpCache", key = "#email")
    public String getOtp(String email) {
        return null; // Will only hit if OTP is not cached (i.e., expired)
    }

    // Remove OTP from cache
    @CacheEvict(value = "otpCache", key = "#email")
    public void clearOtp(String email) {
        // Automatically removed
    }

}
