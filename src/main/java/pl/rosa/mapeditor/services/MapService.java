package pl.rosa.mapeditor.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.rosa.mapeditor.exceptions.AppUserNotLoggedInException;
import pl.rosa.mapeditor.login.LoggedUser;
import pl.rosa.mapeditor.models.AppUser;
import pl.rosa.mapeditor.models.Map;
import pl.rosa.mapeditor.repositories.MapRepository;
import pl.rosa.mapeditor.viewmodels.MapViewModel;

/**
 * Created by Maciej on 2018-11-03 10:20
 */
@Service
public class MapService {


    private MapRepository mapRepository;
    private AppUserService userService;
    private LoggedUser loggedUser;

    @Autowired
    public MapService(MapRepository mapRepository, AppUserService userService, LoggedUser loggedUser) {
        this.mapRepository = mapRepository;
        this.userService = userService;
        this.loggedUser = loggedUser;
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
}
