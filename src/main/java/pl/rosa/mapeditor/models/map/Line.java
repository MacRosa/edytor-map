package pl.rosa.mapeditor.models.map;


import java.util.List;

/**
 * Created by Maciej on 2018-11-15 17:38
 */
public class Line extends Element{

    private List<PathSegment> path;

    public List<PathSegment> getPath() {
        return path;
    }

    public void setPath(List<PathSegment> path) {
        this.path = path;
    }
}
