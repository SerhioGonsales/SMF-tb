package com.lukushin.service;

import com.lukushin.entity.AppDocument;
import com.lukushin.entity.AppPhoto;
import com.lukushin.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long id, LinkType linkType);
}
