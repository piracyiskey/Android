package org.convenient.rest_controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserApiController {

    @GetMapping("/test")
    public String testEndpoint() {
        return "Test is working";
    }

    @GetMapping("/hi")
    public ResponseEntity<String> hiEndpoint() {
        return ResponseEntity.ok("Hello Daisy!");
    }
}
