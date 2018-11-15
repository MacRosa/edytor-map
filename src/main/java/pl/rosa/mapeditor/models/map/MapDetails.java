package pl.rosa.mapeditor.models.map;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.List;

/**
 * Created by Maciej on 2018-11-15 09:29
 */
@Document
public class MapDetails {

    @Id
    private String id;

    private Double width, height;

    private List<Point> points;

    private List<Line> lines;

    private List<Area> areas;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }
}
