package com.lukushin.controller;

import com.lukushin.service.UpdateProducer;
import com.lukushin.utils.MessageUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.lukushin.model.RabbitQueue.*;

@Component
@Log4j
public class UpdateProcessor {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateProcessor(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update){
        if (update == null){
            log.error("Received update is null");
        } else if (update.getMessage() != null){
            distributeMessageByType(update);
        } else {
            log.error("Unsupported type message: " + update.getMessage());
        }
    }

    private void distributeMessageByType(Update update) {
        var message = update.getMessage();
        if(message.hasText()){
            processTextMessage(update);
        } else if(message.hasDocument()){
            processDocMessage(update);
        } else if(message.hasPhoto()){
            processPhotoMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void processTextMessage(Update update) {
        log.debug(update.getMessage().getText());
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }

    private void processPhotoMessage(Update update) {
        log.debug(update.getMessage().getText());
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processDocMessage(Update update) {
        log.debug(update.getMessage().getText());
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Неподдерживаемый тип сообщения");
        setView(sendMessage);
    }

    private void setFileIsReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Файл получен, идет генерация ссылки для скачивания...");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

}
