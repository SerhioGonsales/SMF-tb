package com.lukushin.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MessageUtils {
    public SendMessage generateSendMessageWithText(Update update, String text){
        var sendMessage = new SendMessage();
        var chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }
}