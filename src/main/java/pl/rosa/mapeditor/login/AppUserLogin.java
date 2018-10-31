package pl.rosa.mapeditor.login;

import org.springframework.security.core.userdetails.User;
import pl.rosa.mapeditor.models.AppUser;

import java.util.HashSet;

/**
 * Created by Maciej on 2018-10-31 18:23
 */
public class AppUserLogin extends User {

    private AppUser appUser;


    public AppUserLogin(AppUser appUser) {
        super(appUser.getEmail(), appUser.getPassword(), true, true, true, true, new HashSet<>());
        this.appUser = appUser;
    }

    public AppUser getAppUser() {
        return appUser;
    }
}
