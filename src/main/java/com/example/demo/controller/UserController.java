
package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private String extractEmailFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Not authenticated");
        }
        String token = authorization.substring(7);
        byte[] decoded = Base64.getDecoder().decode(token);
        return new String(decoded);
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(
        @RequestHeader("Authorization") String authorization) {
        String email = extractEmailFromToken(authorization);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
        @RequestHeader("Authorization") String authorization,
        @RequestBody User user) {
        String email = extractEmailFromToken(authorization);
        return ResponseEntity.ok(userService.updateProfile(email, user));
    }
}