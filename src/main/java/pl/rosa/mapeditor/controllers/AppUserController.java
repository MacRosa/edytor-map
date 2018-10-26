package pl.rosa.mapeditor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView register(){

        if(appUserService.isUserLoggedIn()){
            return new ModelAndView("redirect:/");
        }
        return new ModelAndView("register","model",new RegistrationViewModel());
    }


    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public ModelAndView registration(@Valid @ModelAttribute("model") RegistrationViewModel model, final BindingResult result,
                                     final RedirectAttributes redirectAttr){

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
        redirectAttr.addFlashAttribute("register","register");


        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@ModelAttribute("register")final String register, Model model, String error,String logout){
        if(logout != null){
            model.addAttribute("logout",true);
            return "login";
        }

        if(appUserService.isUserLoggedIn()){
            return "redirect:/";
        }

        if(register.equals("register")){
            model.addAttribute("afterRegister",true);
        }


        if(error != null){
            model.addAttribute("loginError",true);
        }

        return "login";
    }

}
