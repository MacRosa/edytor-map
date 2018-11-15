package pl.rosa.mapeditor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.rosa.mapeditor.models.AppUser;
import pl.rosa.mapeditor.models.map.MapDetails;
import pl.rosa.mapeditor.repositories.AppUserRepository;
import pl.rosa.mapeditor.repositories.MapDetailsRepository;

import java.util.Optional;
import java.util.Random;

@Controller
public class TestController {

    final private AppUserRepository appUserRepository;
    private MapDetailsRepository mapDetailsRepository;

    @Autowired
    public TestController(AppUserRepository appUserRepository, MapDetailsRepository mapDetailsRepository) {
        this.appUserRepository = appUserRepository;
        this.mapDetailsRepository = mapDetailsRepository;
    }

    private String userToString(AppUser user){
        return user.getId() + ": " + user.getEmail() + " " + user.getName() + ".";
    }

    @ResponseBody
    @RequestMapping("/test/user/{id}")
    public String returnUser(@PathVariable Long id){
        Optional<AppUser> userOp = appUserRepository.findById(id);
        if(userOp.isPresent()){
            AppUser user = userOp.get();
            return userToString(user);
        }
        return "not found";
    }

    @RequestMapping("/test/username/{name}")
    @ResponseBody
    public String returnUser(@PathVariable String name){
        AppUser user = appUserRepository.findByName(name);
        if(user == null)
            return "User not found";
        return userToString(user);
    }

    @RequestMapping("/test/mapdetails/create")
    @ResponseBody
    public String createMapDetails(){
        MapDetails details = new MapDetails();
        Random random = new Random();
        details.setHeight(random.nextDouble());
        details.setWidth(random.nextDouble());
        mapDetailsRepository.save(details);
        return "Create";
    }


}
