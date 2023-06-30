package com.lukushin.service;

import com.lukushin.dto.MailParam;

public interface MailSenderService {
    void send(MailParam mailParam);
}
