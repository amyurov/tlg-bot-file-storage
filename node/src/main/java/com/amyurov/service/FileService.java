package com.amyurov.service;

import com.amyurov.entity.AppDocument;
import com.amyurov.entity.AppPhoto;
import com.amyurov.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType type);
}
