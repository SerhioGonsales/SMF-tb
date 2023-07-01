package com.lukushin.controller;

import com.lukushin.service.UserActivationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class ActivationController {
    private final UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }

    @GetMapping("/activation")
    public ResponseEntity<?> activation(@RequestParam("id") String hashId){
        var res = userActivationService.activate(hashId);
        //TODO добавить отбивку в чат бота
        if(res){
            return ResponseEntity.ok().body("Регистрация прошла успешно!");
        }
        //TODO собрать все исключения по коду в ControllerAdvice
        return ResponseEntity.internalServerError().build();
    }
}
