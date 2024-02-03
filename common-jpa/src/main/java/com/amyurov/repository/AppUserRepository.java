package com.amyurov.repository;

import com.amyurov.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByTelegramUserId(Long telegramId);
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByEmail(String id);

}
