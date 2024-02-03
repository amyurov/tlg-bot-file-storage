package com.amyurov.service.impl;

import com.amyurov.entity.AppUser;
import com.amyurov.repository.AppUserRepository;
import com.amyurov.service.UserActivationService;
import com.amyurov.utils.CryptoTool;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserRepository appUserRepository;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserRepository appUserRepository, CryptoTool cryptoTool) {
        this.appUserRepository = appUserRepository;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activation(String cryptoUserId) {
        Long userId = cryptoTool.idOf(cryptoUserId);
        Optional<AppUser> user = appUserRepository.findById(userId);
        if (user.isPresent()) {
            AppUser appUser = user.get();
            appUser.setIsActive(true);
            appUserRepository.save(appUser);
            return true;
        }
        return false;
    }
}
