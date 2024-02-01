package com.amyurov.service.impl;

import com.amyurov.entity.RawData;
import com.amyurov.repository.RawDataRepository;
import com.amyurov.service.MainService;
import com.amyurov.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataRepository rawDataRepository;
    private final ProducerService producerService;
    public MainServiceImpl(RawDataRepository rawDataRepository, ProducerService producerService) {
        this.rawDataRepository = rawDataRepository;
        this.producerService = producerService;
    }

    @Override
    public void processTextMessage(Update update) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from node-main-service");

        saveRawData(update);
        producerService.produceAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .update(update)
                .build();

        rawDataRepository.save(rawData);
    }
}
