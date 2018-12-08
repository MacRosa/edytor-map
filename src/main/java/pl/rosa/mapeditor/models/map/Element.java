package pl.rosa.mapeditor.models.map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Maciej on 2018-11-15 17:34
 */
public class Element {

    @JsonIgnore
    @Field("_id")
    private ObjectId _id;

    private Text name;
    private String type;

    private Style style;

    public Element() {
        this._id = new ObjectId();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public Text getName() {
        return name;
    }

    public void setName(Text name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }
}
