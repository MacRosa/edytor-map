package pl.rosa.mapeditor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.rosa.mapeditor.exceptions.AppUserNotLoggedInException;
import pl.rosa.mapeditor.exceptions.MapNotFoundException;
import pl.rosa.mapeditor.exceptions.NoAccessToMapException;
import pl.rosa.mapeditor.login.LoggedUser;
import pl.rosa.mapeditor.models.AppUser;
import pl.rosa.mapeditor.models.Map;
import pl.rosa.mapeditor.services.AppUserService;
import pl.rosa.mapeditor.services.MapService;
import pl.rosa.mapeditor.viewmodels.MapViewModel;

import javax.validation.Valid;

/**
 * Created by Maciej on 2018-11-02 11:30
 */
@Controller
public class MapController {

    private MapService mapService;
    private LoggedUser loggedUser;
    private AppUserService appUserService;

    @Autowired
    public MapController(MapService mapService, LoggedUser loggedUser, AppUserService appUserService) {
        this.mapService = mapService;
        this.loggedUser = loggedUser;
        this.appUserService = appUserService;
    }


    @GetMapping("/map/list")
    public ModelAndView listMaps(@ModelAttribute("addmap")String addmap){
        ModelAndView modelAndView = new ModelAndView("maplist");

        AppUser user = appUserService.getAppUser(loggedUser.getLoggedUser());
        modelAndView.addObject("ownedmaps",user.getOwnedMaps());

        if(addmap.equals("mapadded")){
            modelAndView.addObject("mapadded",true);
        }
        return modelAndView;
    }

    @GetMapping("/map/add")
    public ModelAndView addMap() {
        return new ModelAndView("addmap","model", new MapViewModel());
    }

    @PostMapping("/map/add")
    public ModelAndView addingMap(@Valid @ModelAttribute("model")MapViewModel model,final BindingResult result,
                                    final RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            return new ModelAndView("addmap","model",model);
        }
        try{
            mapService.addMap(model);
        }catch(AppUserNotLoggedInException ex){
            ex.printStackTrace();
            return new ModelAndView("redirect:/login");
        }

        redirectAttributes.addFlashAttribute("addmap","mapadded");

        return new ModelAndView("redirect:/map/list");
    }

    private void addMapModelAndValidate(ModelAndView modelAndView,Long id){
        modelAndView.addObject("notfound",false);
        modelAndView.addObject("noaccess",false);
        try{
            Map map = mapService.getMap(id);
            modelAndView.addObject("model",map);
        }catch(MapNotFoundException ex){
            modelAndView.addObject("notfound",true);
        }catch(NoAccessToMapException ex){
            modelAndView.addObject("noaccess",true);
        }
    }


    @GetMapping("/map/show/{id}")
    public ModelAndView getMap(@PathVariable("id")Long id){
        ModelAndView modelAndView = new ModelAndView("showmap");
        addMapModelAndValidate(modelAndView,id);
        return modelAndView;
    }

    @GetMapping("/map/edit/{id}")
    @ResponseBody
    public String editMap(@PathVariable("id")Long id){
        return "Edit map " + id + ". Work in progress.";
    }
}
