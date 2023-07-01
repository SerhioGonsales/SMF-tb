package com.lukushin.service.impl;

import com.lukushin.dao.AppUserDAO;
import com.lukushin.dto.MailParam;
import com.lukushin.entity.AppUser;
import com.lukushin.service.AppUserService;
import com.lukushin.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Pattern;

import static com.lukushin.enums.UserState.BASIC_STATE;
import static com.lukushin.enums.UserState.WAIT_FOR_EMAIL_STATE;

@Service
@Log4j
public class AppUserServiceImpl implements AppUserService {
    @Value("${service.mail.uri}")
    private String mailServiceUrl;
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    public AppUserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if(appUser.isActive()){
            return "Вы уже зарегистрированы.";
        } else if(appUser.getEmail() != null){
            return "На вашу почту уже было выслано письмо для регистрации \n" +
                    "Пройдите по ссылке в письме";
        }
        appUser.setState(WAIT_FOR_EMAIL_STATE);
        appUserDAO.save(appUser);
        return "Введите, пожалуйста, ваш email для регистрации.";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        var emailIsCorrect =
                Pattern.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", email);
        if(!emailIsCorrect){
            return "Введен некорректный email :( \n" +
                    "Пожалуйста, введите email повторно.\n" +
                    "Для отмены команды введите /cancel";
        }
        var optional = appUserDAO.findByEmail(email);
        if(optional.isEmpty()){
            appUser.setEmail(email);
            appUser.setState(BASIC_STATE);
            appUserDAO.save(appUser);

            var cryptoHashId = cryptoTool.hashOf(appUser.getId());
            var response = sendRequestToMailService(cryptoHashId, email);
            if(response.getStatusCode() == HttpStatus.OK){
                var msg = String.format("Отправка эл. письма на почту %s не удалась", email);
                log.error(msg);
                appUser.setEmail(null);
                appUserDAO.save(appUser);
                return msg;
            }
            return String.format("На почту %s было отправлено письмо. \n" +
                    "Перейдите по ссылке в письме для регистрации", email);
        }
        return "Такая почта уже зарегистрирована. \n" +
                "Для отмены команды введите /cancel";
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoHashId, String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParam = MailParam.builder()
                .id(cryptoHashId)
                .emailTo(email)
                .build();
        var request = new HttpEntity<>(mailParam, headers);
        return restTemplate.exchange(mailServiceUrl,
                HttpMethod.POST,
                request,
                String.class);
    }
}
