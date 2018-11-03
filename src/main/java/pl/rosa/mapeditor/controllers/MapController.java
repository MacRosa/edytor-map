package pl.rosa.mapeditor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import pl.rosa.mapeditor.exceptions.AppUserNotLoggedInException;
import pl.rosa.mapeditor.services.MapService;
import pl.rosa.mapeditor.viewmodels.MapViewModel;

import javax.validation.Valid;

/**
 * Created by Maciej on 2018-11-02 11:30
 */
@Controller
public class MapController {

    private MapService mapService;

    @Autowired
    public MapController(MapService mapService) {
        this.mapService = mapService;
    }


    @GetMapping("/map/list")
    public ModelAndView listMaps(){
        return new ModelAndView("maplist");
    }

    @GetMapping("/map/add")
    public ModelAndView addMap() {
        return new ModelAndView("addmap","model", new MapViewModel());
    }

    @PostMapping("/map/add")
    public ModelAndView addingMap(@Valid @ModelAttribute("model")MapViewModel model,final BindingResult result){
        if(result.hasErrors()){
            return new ModelAndView("addmap","model",model);
        }
        try{
            mapService.addMap(model);
        }catch(AppUserNotLoggedInException ex){
            ex.printStackTrace();
            return new ModelAndView("redirect:/login");
        }

        return new ModelAndView("redirect:/map/list");
    }
}
