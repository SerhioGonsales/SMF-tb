package com.lukushin.service.impl;

import com.lukushin.dao.AppUserDAO;
import com.lukushin.service.UserActivationService;
import com.lukushin.utils.CryptoTool;
import org.springframework.stereotype.Service;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activate(String hashId) {
        var userId = cryptoTool.idOf(hashId);
        var optional = appUserDAO.findById(userId);
        if(optional.isPresent()){
            var appUser = optional.get();
            appUser.setActive(true);
            appUserDAO.save(appUser);
            return true;
        }
        return false;
    }
}
