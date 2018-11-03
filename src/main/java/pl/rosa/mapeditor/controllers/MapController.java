package pl.rosa.mapeditor.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Maciej on 2018-11-02 11:30
 */
@Controller
public class MapController {

    @GetMapping("/map/list")
    public ModelAndView listMaps(){
        return new ModelAndView("maplist");
    }

    @GetMapping("/map/add")
    @ResponseBody
    public String addMap() {
        return "Work in progress.";
    }
}
