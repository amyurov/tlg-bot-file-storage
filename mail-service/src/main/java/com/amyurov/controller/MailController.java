package com.amyurov.controller;

import com.amyurov.dto.MailParamsDto;
import com.amyurov.service.MailSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {
    private final MailSender mailSenderService;

    public MailController(MailSender mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendActivationMail(@RequestBody MailParamsDto mailParamsDto) {
        mailSenderService.sendMail(mailParamsDto);
        return ResponseEntity.ok().build();
    }
}
