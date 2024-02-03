package com.amyurov.controller;

import com.amyurov.service.UserActivationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class ActivationController {
    private UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }

    public ResponseEntity<?> activation(@RequestParam("id") String id) {
        boolean activation = userActivationService.activation(id);
        if (activation) {
            return ResponseEntity.ok().body("Your account is activated");
        }
        return ResponseEntity.internalServerError().build();
    }
}
