package pl.rosa.mapeditor.models.map;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maciej on 2018-11-21 10:09
 */
public class Style {

    private Map<String,String> styleMap = new HashMap<>();

    public Map<String, String> getStyleMap() {
        return styleMap;
    }

    public void setStyleMap(Map<String, String> styleMap) {
        this.styleMap = styleMap;
    }

    public String get(String key) {
        return styleMap.getOrDefault(key,"");
    }

    public void set(String key, String value){
        styleMap.put(key,value);
    }
}
