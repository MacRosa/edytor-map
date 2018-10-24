package pl.rosa.mapeditor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import pl.rosa.mapeditor.services.AppUserService;
import pl.rosa.mapeditor.viewmodels.RegistrationViewModel;

import javax.validation.Valid;

@Controller
public class AppUserController {

    private AppUserService appUserService;

    @Autowired
    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @RequestMapping
    public ModelAndView register(){

        return new ModelAndView("register","model",new RegistrationViewModel());
    }


    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public ModelAndView registration(@Valid @ModelAttribute("model") RegistrationViewModel model, final BindingResult result){

        if(!appUserService.validateUser(model)){
            if(appUserService.isEmailAlreadyInUse()){
                result.reject("Validation.emailInUse","Email is already in use.");
            }
            if(appUserService.isNameAlreadyInUse()){
                result.reject("Validation.nameInUse","Name is already in use.");
            }
        }

        if(result.hasErrors()){
            return new ModelAndView("register","model",model);
        }

        appUserService.registerUser(model);

        return new ModelAndView("redirect:/login");
    }


}
