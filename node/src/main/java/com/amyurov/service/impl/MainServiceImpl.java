package com.amyurov.service.impl;

import com.amyurov.entity.AppDocument;
import com.amyurov.entity.AppPhoto;
import com.amyurov.entity.AppUser;
import com.amyurov.entity.RawData;
import com.amyurov.entity.enums.UserState;
import com.amyurov.exception.UploadFileException;
import com.amyurov.repository.AppUserRepository;
import com.amyurov.repository.RawDataRepository;
import com.amyurov.service.AppUserService;
import com.amyurov.service.FileService;
import com.amyurov.service.MainService;
import com.amyurov.service.ProducerService;
import com.amyurov.service.enums.LinkType;
import com.amyurov.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static com.amyurov.entity.enums.UserState.BASIC_STATE;
import static com.amyurov.entity.enums.UserState.WAIT_FOR_EMAIL_ACTIVE;
import static com.amyurov.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataRepository rawDataRepository;
    private final ProducerService producerService;
    private final AppUserRepository appUserRepository;
    private final FileService fileService;
    private final AppUserService appUserService;


    public MainServiceImpl(RawDataRepository rawDataRepository, ProducerService producerService,
            AppUserRepository appUserRepository, FileService fileService, AppUserService appUserService) {
        this.rawDataRepository = rawDataRepository;
        this.producerService = producerService;
        this.appUserRepository = appUserRepository;
        this.fileService = fileService;
        this.appUserService = appUserService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getUserState();
        String messageText = update.getMessage().getText();
        String output = " ";

        ServiceCommands serviceCommands = ServiceCommands.fromValue(messageText);
        if (CANCEL.equals(serviceCommands)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, messageText);
        } else if (WAIT_FOR_EMAIL_ACTIVE.equals(userState)) {
            output = appUserService.setEmail(appUser, messageText);
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

        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            String answer = "Документ успешно загружен. Ссылка для скачивания: \n" + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException e) {
            log.error(e);
            String error = "К сожалению ошибка при ... Повторите попытку";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();

        if (isNotAllowedToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            String answer = "Фото успешно загружено. Ссылка для скачивания: \n" + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException e) {
            log.error(e);
            String error = "К сожалению ошибка при ... Повторите попытку";
            sendAnswer(error, chatId);
        }
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
        ServiceCommands serviceCommands = fromValue(text);
        if (REGISTRATION.equals(serviceCommands)) {
            // TODO
            return "Временно недоступно";
        } else if (HELP.equals(serviceCommands)) {
            return help();
        } else if (START.equals(serviceCommands)) {
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
        Optional<AppUser> persistentAppUser = appUserRepository.findByTelegramUserId(tlgUser.getId());
        if (persistentAppUser.isEmpty()) {
            AppUser transientAppuser = AppUser.builder()
                    .telegramUserId(tlgUser.getId())
                    .userName(tlgUser.getUserName())
                    .lastName(tlgUser.getLastName())
                    .isActive(false)
                    .userState(BASIC_STATE)
                    .build();
            return appUserRepository.save(transientAppuser);
        }
        return persistentAppUser.get();
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder().update(update).build();
        rawDataRepository.save(rawData);
    }
}
