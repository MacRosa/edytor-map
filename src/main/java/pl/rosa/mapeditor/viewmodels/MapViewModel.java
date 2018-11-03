package pl.rosa.mapeditor.viewmodels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Created by Maciej on 2018-11-03 09:23
 */
public class MapViewModel {

    @NotEmpty(message = "{NotEmpty.message}")
    private String name;

    private String description;


    @Pattern(regexp = "private|nonpublic|public",flags = Pattern.Flag.CASE_INSENSITIVE,message="{InvalidValue.message}")
    private String visibility;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
