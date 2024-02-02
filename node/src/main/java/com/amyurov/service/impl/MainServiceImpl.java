package com.amyurov.service.impl;

import com.amyurov.entity.AppUser;
import com.amyurov.entity.RawData;
import com.amyurov.entity.enums.UserState;
import com.amyurov.repository.AppUserRepository;
import com.amyurov.repository.RawDataRepository;
import com.amyurov.service.MainService;
import com.amyurov.service.ProducerService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.amyurov.entity.enums.UserState.BASIC_STATE;
import static com.amyurov.entity.enums.UserState.WAIT_FOR_EMAIL_ACTIVE;
import static com.amyurov.service.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataRepository rawDataRepository;
    private final ProducerService producerService;
    private final AppUserRepository appUserRepository;

    public MainServiceImpl(RawDataRepository rawDataRepository, ProducerService producerService,
            AppUserRepository appUserRepository) {
        this.rawDataRepository = rawDataRepository;
        this.producerService = producerService;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getUserState();
        String text = update.getMessage().getText();
        String output = " ";
        if (CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_ACTIVE.equals(userState)) {
            //TODO wait for registration impl
        } else {
            log.error("Unknown userState: " + userState);
            output = "Неизветсная ошибка! Введите /cancel и попробуйте снова!";
        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);

    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowedToSendContent(chatId, appUser)) {
            return;
        }

        String answer = "Файл загружен. Ссылка для скачивания: ";
        sendAnswer(answer, chatId);
    }


    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowedToSendContent(chatId, appUser)) {
            return;
        }

        String answer = "Фото загружен. Ссылка для скачивания: ";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowedToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getUserState();
        if (!appUser.getIsActive()) {
            String error = "Зарегестрируйтесь или подтвердите уч. запись";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            String error = "Отмените текущую команду /cancel и повторите";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String text) {
        if (REGISTRATION.equals(text)) {
            // TODO
            return "Временно недоступно";
        } else if (HELP.equals(text)) {
            return help();
        } else if (START.equals(text)) {
            return "Приветствую, чтобы просмотреть список команд введите /help";
        } else {
            return "Неизвестная команда, чтобы просмотреть список команд введите /help";
        }
    }

    private String help() {
        return "Список доступных комманд: \n" + "/cancel - отмена \n" + "/registration - регистрация \n";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserRepository.save(appUser);
        return "Команда отменена";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User tlgUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserRepository.findAppUserByTelegramUserId(tlgUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppuser = AppUser.builder()
                    .telegramUserId(tlgUser.getId())
                    .userName(tlgUser.getUserName())
                    .lastName(tlgUser.getLastName())
                    // TODO change default val after registration impl
                    .isActive(true)
                    .userState(BASIC_STATE)
                    .build();
            return appUserRepository.save(transientAppuser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder().update(update).build();
        rawDataRepository.save(rawData);
    }
}
