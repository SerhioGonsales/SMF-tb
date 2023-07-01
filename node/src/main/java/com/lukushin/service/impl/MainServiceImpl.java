package com.lukushin.service.impl;

import com.lukushin.dao.RawDataDAO;
import com.lukushin.entity.AppDocument;
import com.lukushin.entity.AppPhoto;
import com.lukushin.entity.AppUser;
import com.lukushin.entity.RawData;
import com.lukushin.dao.AppUserDAO;
import com.lukushin.enums.LinkType;
import com.lukushin.enums.ServiceCommands;
import com.lukushin.exceptions.UploadFileException;
import com.lukushin.service.AppUserService;
import com.lukushin.service.FileService;
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
    private final FileService fileService;
    private final AppUserService appUserService;

    public MainServiceImpl(RawDataDAO rawDataDAO,
                           ProducerService producerService,
                           AppUserDAO appUserDAO,
                           FileService fileService,
                           AppUserService appUserService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
        this.appUserService = appUserService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var text = update.getMessage().getText();
        var userState = appUser.getState();
        var out ="";

        var serviceCommand = ServiceCommands.fromValue(text);
        if(CANCEL.equals(serviceCommand)) {
            out = processCancelCmd(appUser);
        } else if(BASIC_STATE.equals(userState)){
            out = processServiceCommand(appUser, text);
        } else if(WAIT_FOR_EMAIL_STATE.equals(userState)){
            out = appUserService.setEmail(appUser, text);
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

        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            sendAnswer(chatId, "Документ успешно загружен.\n" +
                    "Ваша ссылка для скачивания: " + link);
        } catch (UploadFileException e){
            log.error(e);
            String answer = "К сожалению, загрузка документа не удалась, попробуйте позже...";
            sendAnswer(chatId, answer);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var chatId = update.getMessage().getChatId();
        var appUser = findOrSaveAppUser(update);
        if(isNotAllowToSendContent(appUser, chatId)){
            return;
        }

        try{
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            sendAnswer(chatId, "Фото успешно загружено.\n" +
                    "Ваша ссылка для скачивания: " + link);
        } catch (UploadFileException e){
            log.error(e);
            String answer = "К сожалению, загрузка фотографии не удалась, попробуйте позже...";
            sendAnswer(chatId, answer);
    }
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = ServiceCommands.fromValue(cmd);
        if(REGISTRATION.equals(serviceCommand)){
            return appUserService.registerUser(appUser);
        } else if(HELP.equals(serviceCommand)){
            return help();
        } else if(START.equals(serviceCommand)){
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
        var optional = appUserDAO.findByTelegramUserId(telegramUserId);
        if(optional.isEmpty()){
            var transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUserId)
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .userName(telegramUser.getUserName())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
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
