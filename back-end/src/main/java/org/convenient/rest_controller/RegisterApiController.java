package org.convenient.rest_controller;

import org.convenient.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RegisterApiController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/register")
    public ResponseEntity<String> registerUser(@RequestParam("email") String email,
                                               @RequestParam("password") String password,
                                               @RequestParam("full_name") String fullName) {

        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            return ResponseEntity.badRequest().body("Please complete all fields");
        }

        // Check if email is already registered
        if (userService.isEmailTaken(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("used");
        }

        // Hash password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Register user
        int result = userService.registerUser(email, hashedPassword, fullName);

        if (result == 1) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed");
        }
    }
}
