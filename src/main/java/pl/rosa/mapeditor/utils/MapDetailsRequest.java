package pl.rosa.mapeditor.utils;

import pl.rosa.mapeditor.models.map.MapDetails;

/**
 * Created by Maciej on 2018-12-10 19:07
 */
public class MapDetailsRequest {

    private boolean success;
    private String message;
    private MapDetails mapDetails;

    public MapDetailsRequest(boolean success, String message, MapDetails mapDetails) {
        this.success = success;
        this.message = message;
        this.mapDetails = mapDetails;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MapDetails getMapDetails() {
        return mapDetails;
    }

    public void setMapDetails(MapDetails mapDetails) {
        this.mapDetails = mapDetails;
    }
}
