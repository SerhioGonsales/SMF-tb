package com.lukushin.service.impl;

import com.lukushin.dao.RawDataDAO;
import com.lukushin.entity.AppUser;
import com.lukushin.entity.RawData;
import com.lukushin.entity.dao.AppUserDAO;
import com.lukushin.service.MainService;
import com.lukushin.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.lukushin.entity.enums.UserState.*;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;

    public MainServiceImpl(RawDataDAO rawDataDAO,
                           ProducerService producerService,
                           AppUserDAO appUserDAO) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        
        var appUser = findOrSaveAppUser(update);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Hello from NODE");
        producerService.produceAnswer(sendMessage);
    }

    private AppUser findOrSaveAppUser(Update update) {
        var telegramUser = update.getMessage().getFrom();
        var telegramUserId = telegramUser.getId();
        var persistenceAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUserId);
        if(persistenceAppUser == null){
            var transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUserId)
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .userName(telegramUser.getUserName())
                    // TODO изменить на false когда будет регистрация и активация по email
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistenceAppUser;
    }

    private void saveRawData(Update update) {
        var rawData = RawData
                .builder()
                .update(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
