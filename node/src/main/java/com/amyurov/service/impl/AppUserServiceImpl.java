package com.amyurov.service.impl;

import com.amyurov.dto.MailParamsDto;
import com.amyurov.entity.AppUser;
import com.amyurov.entity.enums.UserState;
import com.amyurov.repository.AppUserRepository;
import com.amyurov.service.AppUserService;
import com.amyurov.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Optional;

@Service
@Log4j
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserRepository appUserRepository, CryptoTool cryptoTool) {
        this.appUserRepository = appUserRepository;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()) {
            return "Already register";
        } else if (appUser.getEmail() == null) {
            return "Check link on email";
        }
        appUser.setUserState(UserState.WAIT_FOR_EMAIL_ACTIVE);
        appUserRepository.save(appUser);
        return "Enter your email, please";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException e) {
            return "Email is incorrect. type /cancel";
        }
        Optional<AppUser> byEmail = appUserRepository.findByEmail(email);
        if (byEmail.isEmpty()) {
            appUser.setEmail(email);
            appUser.setUserState(UserState.BASIC_STATE);
            AppUser save = appUserRepository.save(appUser);

            String cryptoUserId = cryptoTool.hashOf(appUser.getId());
            ResponseEntity<String> response = sendRequestToUserService(cryptoUserId, email);
            if (response.getStatusCode() != HttpStatus.OK) {
                String msg = String.format("Sending error on: " + email);
                log.error(msg);
                appUser.setEmail(null);
                appUserRepository.save(appUser);
                return msg;
            }
            return "Activate your account via email link";
        }
        return "This email is used already. Try another one";
    }

    private ResponseEntity<String> sendRequestToUserService(String cryptoUserId, String email) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        MailParamsDto mailParams = MailParamsDto.builder()
                .id(cryptoUserId)
                .mailTo(email)
                .build();
        HttpEntity request = new HttpEntity<>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri, HttpMethod.POST, request, String.class);
    }
}
