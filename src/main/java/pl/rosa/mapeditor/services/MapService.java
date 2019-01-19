package pl.rosa.mapeditor.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.rosa.mapeditor.exceptions.AppUserNotLoggedInException;
import pl.rosa.mapeditor.exceptions.MapNotFoundException;
import pl.rosa.mapeditor.exceptions.NoAccessToMapException;
import pl.rosa.mapeditor.exceptions.UserNotFoundException;
import pl.rosa.mapeditor.login.LoggedUser;
import pl.rosa.mapeditor.models.AppUser;
import pl.rosa.mapeditor.models.Map;
import pl.rosa.mapeditor.models.MapAccess;
import pl.rosa.mapeditor.repositories.MapAccessRepository;
import pl.rosa.mapeditor.repositories.MapRepository;
import pl.rosa.mapeditor.viewmodels.MapViewModel;

import java.util.List;
import java.util.Optional;

/**
 * Created by Maciej on 2018-11-03 10:20
 */
@Service
public class MapService {


    private MapRepository mapRepository;
    private AppUserService userService;
    private LoggedUser loggedUser;
    private MapAccessRepository mapAccessRepository;

    @Autowired
    public MapService(MapRepository mapRepository, AppUserService userService, LoggedUser loggedUser, MapAccessRepository mapAccessRepository) {
        this.mapRepository = mapRepository;
        this.userService = userService;
        this.loggedUser = loggedUser;
        this.mapAccessRepository = mapAccessRepository;
    }

    public void addMap(MapViewModel mapViewModel) throws AppUserNotLoggedInException {
        if(!userService.isUserLoggedIn()){
            throw new AppUserNotLoggedInException();
        }
        AppUser user = loggedUser.getLoggedUser().getAppUser();
        Map map = new Map();
        map.setName(mapViewModel.getName());
        map.setDescription(mapViewModel.getDescription());
        map.setVisibility(mapViewModel.getVisibility());
        map.setOwner(user);
        mapRepository.save(map);
    }

    private boolean isOwner(AppUser user,Map map){
        return user.getId().equals(map.getOwner().getId());
    }

    public boolean userCanRead(Map map,AppUser user){
        if(isOwner(user,map)){
            return true;
        }
        Optional<MapAccess> mapAccess = mapAccessRepository.findByMapIdAndAppUserId(map.getId(),user.getId());
        return mapAccess.map(mapAccess1 -> mapAccess1.getAccessType().matches("[rw]")).orElse(false);
    }

    public Map getMap(Long id) throws MapNotFoundException,NoAccessToMapException {
        Map map = mapRepository.findById(id).orElseThrow(MapNotFoundException::new);
        if(!map.getVisibility().matches("nonpublic|public")){
            if(!userService.isUserLoggedIn())
                throw new NoAccessToMapException();
            AppUser user = loggedUser.getLoggedUser().getAppUser();
            if(!userCanRead(map,user))
                throw new NoAccessToMapException();
        }
        return map;
    }

    public boolean userCanEdit(AppUser user, Map map) {
        if(isOwner(user,map)){
            return true;
        }
        Optional<MapAccess> mapAccessOption = mapAccessRepository.findByMapIdAndAppUserId(map.getId(),user.getId());

        return mapAccessOption.map(mapAccess -> mapAccess.getAccessType().equals("w")).orElse(false);
    }

    public boolean currentUserCanEdit(Map map){
        if(!loggedUser.isLogged())
            return false;
        return userCanEdit(loggedUser.getLoggedUser().getAppUser(),map);
    }

    public boolean currentUserIsOwner(Map map){
        if(!loggedUser.isLogged())
            return false;
        return isOwner(loggedUser.getLoggedUser().getAppUser(),map);
    }

    public void addContributor(String name,Map map,String accessType) throws UserNotFoundException, NoAccessToMapException {
        if(!currentUserIsOwner(map))
            throw new NoAccessToMapException();
        AppUser user = userService.getUserByName(name);
        Optional<MapAccess> mapAccessOptional = mapAccessRepository.findByMapIdAndAppUserId(map.getId(), user.getId());
        MapAccess mapAccess;
        if(mapAccessOptional.isPresent()){
            mapAccess = mapAccessOptional.get();
            mapAccess.setAccessType(accessType);
        }else{
            mapAccess = new MapAccess();
            mapAccess.setMap(map);
            mapAccess.setAppUser(user);
            mapAccess.setAccessType(accessType);
        }
        mapAccessRepository.save(mapAccess);
    }

    public List<Map> getPublicMapsByUser(Long userId){
        return mapRepository.findByOwnerIdAndVisibility(userId,"public");
    }

    public Map getMapToEditInfo(Long mapId) throws MapNotFoundException, NoAccessToMapException {
        Map map = mapRepository.findById(mapId).orElseThrow(MapNotFoundException::new);
        if(!currentUserIsOwner(map))
            throw new NoAccessToMapException();
        return map;
    }

    public MapViewModel getModelFromMap(Map map){
        MapViewModel mapViewModel = new MapViewModel();
        mapViewModel.setName(map.getName());
        mapViewModel.setDescription(map.getDescription());
        mapViewModel.setVisibility(map.getVisibility());
        return mapViewModel;
    }

    public void editMapInfo(Long mapId,MapViewModel mapViewModel) throws MapNotFoundException, NoAccessToMapException {
        Map map = getMapToEditInfo(mapId);
        map.setName(mapViewModel.getName());
        map.setDescription(mapViewModel.getDescription());
        map.setVisibility(mapViewModel.getVisibility());
        mapRepository.save(map);
    }
}
