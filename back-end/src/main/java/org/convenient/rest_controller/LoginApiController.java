package org.convenient.rest_controller;

import jakarta.servlet.http.HttpSession;
import org.convenient.models.User;
import org.convenient.security.JwtUtil;
import org.convenient.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class LoginApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/user/login")
    public ResponseEntity<?> login(@RequestParam("email") String email,
                                   @RequestParam("password") String password) {
        User user = userService.login(email, password);
        if (user != null) {
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body("invalid");
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isValid(token)) {
                String email = jwtUtil.extractEmail(token);
                User user = userService.findByEmail(email);
                if (user != null) return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(401).body("unauthorized");
    }
}

