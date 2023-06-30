package com.lukushin.service.impl;

import com.lukushin.dto.MailParam;
import com.lukushin.service.MailSenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSenderServiceImpl implements MailSenderService {
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.uri}")
    private String activationUri;
    private final JavaMailSender javaMailSender;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(MailParam mailParam) {
        var subject = "Регистрация учетной записи в SMF_tb";
        var text = generateMessageBody(mailParam.getId());
        var emailTo = mailParam.getEmailTo();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        mailMessage.setTo(emailTo);
        javaMailSender.send(mailMessage);
    }

    private String generateMessageBody(String id) {
        var text = "Здравствуйте!\n" +
                "Для регистрации профиля пройдите по ссылке " + activationUri;
        return text.replace("{id}", id);
    }
}
