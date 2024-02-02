package com.amyurov.service.impl;

import com.amyurov.service.ConsumerService;
import com.amyurov.service.MainService;
import com.amyurov.service.ProducerService;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.amyurov.model.RabbitQueue.*;

@Service
@Log4j
public class ConsumerServiceImpl implements ConsumerService {
    private final ProducerService producerService;
    private final MainService mainService;

    public ConsumerServiceImpl(ProducerService producerService, MainService mainService) {
        this.producerService = producerService;
        this.mainService = mainService;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdate(Update update) {
        log.debug("NODE: Text message is received from rmq");
        mainService.processTextMessage(update);
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocMessageUpdate(Update update) {
        log.debug("NODE: Doc message is received from rmq");
        mainService.processDocMessage(update);
    }

    @Override
    @RabbitListener(queues = PHOTO_MESSAGE_UPDATE)
    public void consumePhotoMessageUpdate(Update update) {
        log.debug("NODE: Photo message is received from rmq");
        mainService.processPhotoMessage(update);
    }
}
