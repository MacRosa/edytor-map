package pl.rosa.mapeditor.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Maciej on 2018-11-02 11:34
 */
@Entity
@Table
public class Map {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String description;
    private String visibility;
    private String documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private AppUser owner;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @OneToMany(mappedBy = "map",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonIgnore
    private List<MapAccess> mapAccessList;

    public List<MapAccess> getMapAccessList() {
        return mapAccessList;
    }

    public void setMapAccessList(List<MapAccess> mapAccessList) {
        this.mapAccessList = mapAccessList;
    }
}
