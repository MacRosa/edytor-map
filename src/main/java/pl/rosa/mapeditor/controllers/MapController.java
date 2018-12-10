package pl.rosa.mapeditor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import pl.rosa.mapeditor.models.map.MapDetails;
import pl.rosa.mapeditor.repositories.MapDetailsRepository;
import pl.rosa.mapeditor.services.AppUserService;
import pl.rosa.mapeditor.services.MapService;
import pl.rosa.mapeditor.utils.MapConverter;
import pl.rosa.mapeditor.viewmodels.MapViewModel;

import javax.validation.Valid;
import java.io.IOException;

/**
 * Created by Maciej on 2018-11-02 11:30
 */
@Controller
public class MapController {

    private MapService mapService;
    private LoggedUser loggedUser;
    private AppUserService appUserService;
    private final MapConverter mapConverter;
    @SuppressWarnings("FieldCanBeLocal")
    private ObjectMapper objectMapper;
    private MapDetailsRepository mapDetailsRepository;

    @Autowired
    public MapController(MapService mapService, LoggedUser loggedUser, AppUserService appUserService, MapConverter mapConverter, MapDetailsRepository mapDetailsRepository) {
        this.mapService = mapService;
        this.loggedUser = loggedUser;
        this.appUserService = appUserService;
        this.mapConverter = mapConverter;
        this.mapDetailsRepository = mapDetailsRepository;
        this.objectMapper = new ObjectMapper();
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

    private Map addMapModelAndValidate(ModelAndView modelAndView,Long id){
        modelAndView.addObject("notfound",false);
        modelAndView.addObject("noaccess",false);
        try{
            Map map = mapService.getMap(id);
            modelAndView.addObject("model",map);
            return map;
        }catch(MapNotFoundException ex){
            modelAndView.addObject("notfound",true);
        }catch(NoAccessToMapException ex){
            modelAndView.addObject("noaccess",true);
        }
        return null;
    }


    @GetMapping("/map/show/{id}")
    public ModelAndView getMap(@PathVariable("id")Long id){
        ModelAndView modelAndView = new ModelAndView("showmap");
        addMapModelAndValidate(modelAndView,id);
        return modelAndView;
    }

    @GetMapping("/map/edit/{id}")
    public ModelAndView editMap(@PathVariable("id")Long id){
        ModelAndView modelAndView = new ModelAndView("map_editor");
        Map map = addMapModelAndValidate(modelAndView,id);
        if(map != null){
            modelAndView.addObject("cantedit",!(mapService.currentUserCanEdit(map)));
        }else{
            modelAndView.addObject("cantedit",true);
        }
        return modelAndView;
    }

    @PostMapping("/map/edit/{id}")
    @ResponseBody
    @Transactional
    public String sendEditedMap(@PathVariable("id")Long id, @RequestParam("mapData") String mapData) throws IOException {
        try {
            Map map = mapService.getMap(id);
            if(!mapService.currentUserCanEdit(map)){
                return "cantEdit";
            }
            MapDetails mapDetails = mapConverter.getMapFromJson(objectMapper.readTree(mapData));
            if(map.getDocumentId() == null) {
                mapDetailsRepository.save(mapDetails);
                map.setDocumentId(mapDetails.getId());
            }else{
                MapDetails oldDetails = mapDetailsRepository.findById(map.getDocumentId()).orElse(null);
                if(oldDetails == null){
                    mapDetailsRepository.save(mapDetails);
                    map.setDocumentId(mapDetails.getId());
                }else{
                    mapDetails.setId(oldDetails.getId());
                    mapDetailsRepository.save(mapDetails);
                }
            }
            return objectMapper.writeValueAsString(mapDetails);

        } catch (MapNotFoundException e) {
            return "mapNotFound";
        } catch (NoAccessToMapException e) {
            return "noAccess";
        }
    }

    @GetMapping("/map/details/{id}")
    @ResponseBody
    public String getMapDetails(@PathVariable("id")Long id) throws IOException {
        try {
            Map map = mapService.getMap(id);
            if(!mapService.currentUserCanEdit(map)){
                return "Not allowed to edit";
            }
            if(map.getDocumentId() == null){
                return "No map details";
            }

            MapDetails mapDetails = mapDetailsRepository.findById(map.getDocumentId()).orElse(null);
            if(mapDetails == null){
                return "Map details are null";
            }
            return objectMapper.writeValueAsString(mapDetails);
        } catch (MapNotFoundException e) {
            return "Map not found";
        } catch (NoAccessToMapException e) {
            return "No access to map.";
        }
    }

}
