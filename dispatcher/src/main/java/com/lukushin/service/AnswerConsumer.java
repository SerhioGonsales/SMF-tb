package com.lukushin.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface AnswerConsumer {
    void answerConsume(SendMessage sendMessage);
}
