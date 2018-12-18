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
import pl.rosa.mapeditor.utils.JSONToMapConverter;
import pl.rosa.mapeditor.utils.MapDetailsRequest;
import pl.rosa.mapeditor.utils.MapToSVGConverter;
import pl.rosa.mapeditor.viewmodels.MapViewModel;

import javax.validation.Valid;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Created by Maciej on 2018-11-02 11:30
 */
@Controller
public class MapController {

    private MapService mapService;
    private LoggedUser loggedUser;
    private AppUserService appUserService;
    private final JSONToMapConverter mapConverter;
    private ObjectMapper objectMapper;
    private MapDetailsRepository mapDetailsRepository;
    private MapToSVGConverter mapToSVGConverter;

    @Autowired
    public MapController(MapService mapService, LoggedUser loggedUser, AppUserService appUserService, JSONToMapConverter mapConverter, MapDetailsRepository mapDetailsRepository, MapToSVGConverter mapToSVGConverter) {
        this.mapService = mapService;
        this.loggedUser = loggedUser;
        this.appUserService = appUserService;
        this.mapConverter = mapConverter;
        this.mapDetailsRepository = mapDetailsRepository;
        this.mapToSVGConverter = mapToSVGConverter;
        this.objectMapper = new ObjectMapper();
    }


    @GetMapping("/map/list")
    public ModelAndView listMaps(@ModelAttribute("addmap") String addmap) {
        ModelAndView modelAndView = new ModelAndView("maplist");

        AppUser user = appUserService.getAppUser(loggedUser.getLoggedUser());
        modelAndView.addObject("ownedmaps", user.getOwnedMaps());
        modelAndView.addObject("sharedmaps",user.getMapAccessList());

        if (addmap.equals("mapadded")) {
            modelAndView.addObject("mapadded", true);
        }
        return modelAndView;
    }

    @GetMapping("/map/add")
    public ModelAndView addMap() {
        return new ModelAndView("addmap", "model", new MapViewModel());
    }

    @PostMapping("/map/add")
    public ModelAndView addingMap(@Valid @ModelAttribute("model") MapViewModel model, final BindingResult result,
                                  final RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return new ModelAndView("addmap", "model", model);
        }
        try {
            mapService.addMap(model);
        } catch (AppUserNotLoggedInException ex) {
            ex.printStackTrace();
            return new ModelAndView("redirect:/login");
        }

        redirectAttributes.addFlashAttribute("addmap", "mapadded");

        return new ModelAndView("redirect:/map/list");
    }

    private Map addMapModelAndValidate(ModelAndView modelAndView, Long id) {
        modelAndView.addObject("notfound", false);
        modelAndView.addObject("noaccess", false);
        try {
            Map map = mapService.getMap(id);
            modelAndView.addObject("model", map);
            return map;
        } catch (MapNotFoundException ex) {
            modelAndView.addObject("notfound", true);
        } catch (NoAccessToMapException ex) {
            modelAndView.addObject("noaccess", true);
        }
        return null;
    }


    @GetMapping("/map/show/{id}")
    public ModelAndView getMap(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("showmap");
        addMapModelAndValidate(modelAndView, id);
        return modelAndView;
    }

    @GetMapping("/map/edit/{id}")
    public ModelAndView editMap(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("map_editor");
        Map map = addMapModelAndValidate(modelAndView, id);
        if (map != null) {
            modelAndView.addObject("cantedit", !(mapService.currentUserCanEdit(map)));
        } else {
            modelAndView.addObject("cantedit", true);
        }
        return modelAndView;
    }

    @PostMapping("/map/edit/{id}")
    @Transactional
    public ModelAndView sendEditedMap(@PathVariable("id") Long id, @RequestParam("mapData") String mapData) throws IOException {
        try {
            ModelAndView modelAndView = new ModelAndView("redirect:/map/show/" + id);
            Map map = mapService.getMap(id);
            if (!mapService.currentUserCanEdit(map)) {
                return new ModelAndView("error");
            }
            MapDetails mapDetails = mapConverter.getMapFromJson(objectMapper.readTree(mapData));
            if (map.getDocumentId() == null) {
                mapDetailsRepository.save(mapDetails);
                map.setDocumentId(mapDetails.getId());
            } else {
                MapDetails oldDetails = mapDetailsRepository.findById(map.getDocumentId()).orElse(null);
                if (oldDetails == null) {
                    mapDetailsRepository.save(mapDetails);
                    map.setDocumentId(mapDetails.getId());
                } else {
                    mapDetails.setId(oldDetails.getId());
                    mapDetailsRepository.save(mapDetails);
                }
            }
            return modelAndView;
        } catch (MapNotFoundException e) {
            return new ModelAndView("error");
        } catch (@SuppressWarnings({"TryWithIdenticalCatches", "RedundantSuppression"}) NoAccessToMapException e) {
            //other
            return new ModelAndView("error");
        }
    }

    @GetMapping("/map/details/{id}")
    @ResponseBody
    public MapDetailsRequest getMapDetails(@PathVariable("id") Long id) {
        try {
            Map map = mapService.getMap(id);
            if (!mapService.currentUserCanEdit(map)) {
                return new MapDetailsRequest(false, "No permissions to edit", null);
            }
            if (map.getDocumentId() == null) {
                return new MapDetailsRequest(true, "No document", null);
            }

            MapDetails mapDetails = mapDetailsRepository.findById(map.getDocumentId()).orElse(null);
            if (mapDetails == null) {
                return new MapDetailsRequest(true, "No details", null);
            }
            return new MapDetailsRequest(true, "success", mapDetails);
        } catch (MapNotFoundException e) {
            return new MapDetailsRequest(false, "Map not found", null);
        } catch (NoAccessToMapException e) {
            return new MapDetailsRequest(false, "No access to map", null);
        }
    }

    @GetMapping(value = "/map/svg/{id}",
            produces = "image/svg+xml")
    @ResponseBody
    public byte[] getMapSVGImage(@PathVariable("id") Long id) {
        try {
            Map map = mapService.getMap(id);
            if (map.getDocumentId() == null) {
                return new byte[0];
            }
            MapDetails mapDetails = mapDetailsRepository.findById(map.getDocumentId()).orElse(null);
            if (mapDetails == null) {
                return new byte[0];
            }
            return mapToSVGConverter.toByteSVGImage(mapDetails);
        } catch (MapNotFoundException | NoAccessToMapException | TransformerException e) {
            return new byte[0];
        }
    }
}