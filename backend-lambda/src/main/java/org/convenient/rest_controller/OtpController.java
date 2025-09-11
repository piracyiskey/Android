package org.convenient.rest_controller;

import org.convenient.services.OtpService;
import org.convenient.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserService userService;

    @PostMapping("/user/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam("email") String email) {
        if (!userService.isEmailTaken(email)) {
            return ResponseEntity.status(404).body("email_not_found");
        }
        String otp = otpService.generateOtp();
        otpService.sendOtpEmail(email, otp);
        otpService.storeOtp(email, otp);
        return ResponseEntity.ok("otp_sent");
    }

    @PostMapping("/user/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam("email") String email,
                                            @RequestParam("otp") String otp) {
        String storedOtp = otpService.getOtp(email);  // <-- Now this uses the caching proxy
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpService.clearOtp(email);
            return ResponseEntity.ok("otp_valid");
        } else {
            return ResponseEntity.status(400).body("invalid_otp");
        }
    }
}
