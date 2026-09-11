package com.hospital.controller;

import com.hospital.model.User;
import com.hospital.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Returns the currently authenticated user's own profile - lets the
    // Angular app know who's logged in and what role/department they have.
    @GetMapping("/me")
    public ResponseEntity<User> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    // Powers the "select a doctor" dropdown on the booking form.
    @GetMapping("/doctors")
    public ResponseEntity<List<User>> doctors(@RequestParam String department) {
        return ResponseEntity.ok(userRepository.findByRoleAndDepartment(User.Role.DOCTOR, department));
    }
}
