package org.convenient.rest_controller;

import org.convenient.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PasswordRecoveryController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam("email") String email) {
        if (userService.isEmailTaken(email)) {
            return ResponseEntity.ok("exists");
        } else {
            return ResponseEntity.status(404).body("not_found");
        }
    }

    @PostMapping("/user/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam("email") String email,
                                                 @RequestParam("password") String newPassword) {
        boolean success = userService.updatePassword(email, newPassword);
        if (success) {
            return ResponseEntity.ok("updated");
        } else {
            return ResponseEntity.status(404).body("not_found");
        }
    }
}
