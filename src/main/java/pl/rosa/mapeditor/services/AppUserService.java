package pl.rosa.mapeditor.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.rosa.mapeditor.models.AppUser;
import pl.rosa.mapeditor.repositories.AppUserRepository;
import pl.rosa.mapeditor.viewmodels.RegistrationViewModel;

@Service
public class AppUserService {

    private AppUserRepository appUserRepository;
    private PasswordEncoder passwordEncoder;

    private boolean emailAlreadyInUse = false;
    private boolean nameAlreadyInUse = false;

    @Autowired
    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void checkIfEmailIsInUse(String email){
        emailAlreadyInUse = (appUserRepository.findByEmail(email) != null);
    }

    private void checkIfNameIsInUse(String name){
        nameAlreadyInUse = (appUserRepository.findByName(name) != null);

    }

    public boolean validateUser(RegistrationViewModel model){
        checkIfEmailIsInUse(model.getEmail());
        checkIfNameIsInUse(model.getName());
        return !(emailAlreadyInUse || nameAlreadyInUse);
    }


    public boolean isEmailAlreadyInUse() {
        return emailAlreadyInUse;
    }

    public boolean isNameAlreadyInUse() {
        return nameAlreadyInUse;
    }

    public void registerUser(RegistrationViewModel model){
        AppUser appUser = new AppUser();
        appUser.setEmail(model.getEmail());
        appUser.setName(model.getName());
        appUser.setPassword(passwordEncoder.encode(model.getPassword()));
        appUserRepository.save(appUser);
    }

    public boolean isUserLoggedIn(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return !(auth instanceof AnonymousAuthenticationToken);
    }
}
