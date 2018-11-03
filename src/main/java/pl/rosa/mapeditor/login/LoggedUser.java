package pl.rosa.mapeditor.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.rosa.mapeditor.services.AppUserService;

/**
 * Created by Maciej on 2018-10-31 18:51
 */
@Component
public class LoggedUser {

    private AppUserService appUserService;

    @Autowired
    public LoggedUser(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public AppUserLogin getLoggedUser(){
        return (AppUserLogin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getUserName(){
        if(appUserService.isUserLoggedIn()){
            return getLoggedUser().getAppUser().getName();
        }
        return "";
    }
}
