package com.lukushin.service.impl;

import com.lukushin.dao.RawDataDAO;
import com.lukushin.entity.AppUser;
import com.lukushin.entity.RawData;
import com.lukushin.dao.AppUserDAO;
import com.lukushin.service.MainService;
import com.lukushin.service.ProducerService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.lukushin.enums.UserState.*;
import static com.lukushin.enums.ServiceCommands.*;

@Service
@Log4j
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
        var cmd = update.getMessage().getText();
        var userState = appUser.getState();
        var out ="";

        if(CANCEL.equals(cmd)) {
            out = processCancelCmd(appUser);
        } else if(BASIC_STATE.equals(userState)){
            out = processServiceCommand(cmd);
        } else if(WAIT_FOR_EMAIL_STATE.equals(userState)){
            // TODO добавить обработку email
        } else {
            log.error("Неизвестный статус пользователя: " + userState);
            out = "Неизвестная ошибка. Отмените действие командой /cancel.";
        }
        var chatId = update.getMessage().getChatId();
        sendAnswer(chatId, out);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var chatId = update.getMessage().getChatId();
        var appUser = findOrSaveAppUser(update);
        if(isNotAllowToSendContent(appUser, chatId)){
            return;
        }
        // TODO реализовать сохранение контента
        sendAnswer(chatId, "Документ успешно загружен.\n" +
                "Ваша ссылка для скачивания: http://test.ru/get-doc/777.doc");
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var chatId = update.getMessage().getChatId();
        var appUser = findOrSaveAppUser(update);
        if(isNotAllowToSendContent(appUser, chatId)){
            return;
        }
        // TODO реализовать сохранение контента
        sendAnswer(chatId, "Фото успешно загружено.\n" +
                "Ваша ссылка для скачивания: http://test.ru/get-photo/777.jpg");
    }

    private String processServiceCommand(String cmd) {
        if(REGISTRATION.equals(cmd)){
            // TODO добавит генерацию письма
            return "Функция временно недоступна.";
//            return "На вашу почту было выслано письмо для регистрации.\n" +
//                    "Пройдите по ссылке в этом письме.";
        } else if(HELP.equals(cmd)){
            return help();
        } else if(START.equals(cmd)){
            return "Приветствую!\n" +
                    "Список доступных команд ниже:\n" +
                    "/cancel - отмена предыдущей команды\n" +
                    "/registration - запрос на регистрацию\n" +
                    "/help - список доступных команд";
        } else{
            return "Введена неверная команда.\n" +
                    "Список доступных команд:\n" +
                    "/cancel - отмена предыдущей команды\n" +
                    "/registration - запрос на регистрацию\n" +
                    "/help - список доступных команд";

        }
    }

    private String help() {
        return "Список доступных команд:\n" +
                "/cancel - отмена предыдущей команды\n" +
                "/registration - запрос на регистрацию";
    }

    private String processCancelCmd(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена.";
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

    private boolean isNotAllowToSendContent(AppUser appUser, Long chatId) {
        var userState = appUser.getState();
        if (!appUser.isActive()){
            var error = "Зарегистрируйтесь или активируйте учетную запись для загрузки контента.";
            sendAnswer(chatId, error);
            return true;
        } else if(WAIT_FOR_EMAIL_STATE.equals(userState)){
            var error = "Активируйте вашу учетную запись, пройдя по ссылке в письме у вас на почте.";
            sendAnswer(chatId, error);
            return true;
        }
        return false;
    }

    private void saveRawData(Update update) {
        var rawData = RawData
                .builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }

    private void sendAnswer(Long chatId, String answer){
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(answer);
        producerService.produceAnswer(sendMessage);
    }
}
