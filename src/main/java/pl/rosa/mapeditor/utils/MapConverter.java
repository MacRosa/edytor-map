package pl.rosa.mapeditor.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pl.rosa.mapeditor.models.map.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maciej on 2018-12-08 17:45
 */
@Component
public class MapConverter {

    private Text getText(JsonNode node){
        Text text = new Text();
        text.setX(node.get("tx").asDouble());
        text.setY(node.get("ty").asDouble());
        text.setValue(node.get("name").asText());
        return text;
    }

    private List<PathSegment> getPath(JsonNode node){
        List<PathSegment> path = new ArrayList<>();
        JsonNode pathSegmentsNode = node.get("path");
        pathSegmentsNode.elements().forEachRemaining(
                jsonNode ->
                    path.add(new PathSegment(
                            jsonNode.get(0).asText().charAt(0),
                            jsonNode.get(1).asDouble(),
                            jsonNode.get(2).asDouble())));
        return path;
    }

    public MapDetails getMapFromJson(JsonNode node){
        MapDetails mapDetails = new MapDetails();
        mapDetails.setWidth(node.get("height").asDouble());
        mapDetails.setHeight(node.get("height").asDouble());
        JsonNode pointsNode = node.get("points");
        List<Point> points = new ArrayList<>();
        pointsNode.elements().forEachRemaining(
                s -> {
                    Point point = new Point();
                    point.setX( s.get("px").asDouble());
                    point.setY( s.get("py").asDouble());
                    point.setName( getText(s));
                    points.add(point);
                }
        );
        mapDetails.setPoints(points);

        List<Line> lines = new ArrayList<>();

        JsonNode linesNode = node.get("lines");
        linesNode.elements().forEachRemaining(
                s -> {
                    Line line = new Line();
                    line.setName(getText(s));
                    line.setPath(getPath(s));
                    lines.add(line);
                }
        );

        mapDetails.setLines(lines);

        List<Area> areas = new ArrayList<>();

        node.get("areas").elements().forEachRemaining(
                s -> {
                    Area area = new Area();
                    area.setName(getText(s));
                    area.setPath(getPath(s));
                    areas.add(area);
                }

        );
        mapDetails.setAreas(areas);

        return mapDetails;
    }
}
