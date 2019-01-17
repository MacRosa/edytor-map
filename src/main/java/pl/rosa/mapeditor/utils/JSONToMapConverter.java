package pl.rosa.mapeditor.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import pl.rosa.mapeditor.models.map.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Maciej on 2018-12-08 17:45
 */
@Component
public class JSONToMapConverter {

    private Style getTextStyle(JsonNode node){
        Style style = new Style();
        style.set("font-size",node.get("font-size").asText());
        style.set("fill",node.get("fill").asText());
        return style;
    }

    private Text getText(JsonNode node){
        Text text = new Text();
        text.setX(node.get("tx").asDouble());
        text.setY(node.get("ty").asDouble());
        text.setValue(node.get("name").asText());
        text.setStyle(getTextStyle(node.get("style").get("text")));
        return text;
    }

    private PathSegment getPathSegment(JsonNode node){
        PathSegment pathSegment = new PathSegment();
        Iterator<JsonNode> nodeIterator = node.elements();
        pathSegment.setInstruction(nodeIterator.next().asText().charAt(0));
        List<Double> params = new ArrayList<>();
        nodeIterator.forEachRemaining(jsonNode -> params.add(jsonNode.asDouble()));
        pathSegment.setParams(params);
        return pathSegment;
    }

    private List<PathSegment> getPath(JsonNode node){
        List<PathSegment> path = new ArrayList<>();
        JsonNode pathSegmentsNode = node.get("path");
        pathSegmentsNode.elements().forEachRemaining(
                jsonNode ->
                    path.add(getPathSegment(jsonNode)));
        return path;
    }

    private Style getPointStyle(JsonNode node){
        Style style = new Style();
        JsonNode pointNode = node.get("point");
        style.set("stroke",pointNode.get("stroke").asText());
        style.set("fill",pointNode.get("fill").asText());
        style.set("r",pointNode.get("r").asText());
        return style;
    }

    private Style getLineStyle(JsonNode node){
        Style style = new Style();
        JsonNode lineNode = node.get("line");
        style.set("stroke-width",lineNode.get("stroke-width").asText());
        style.set("stroke",lineNode.get("stroke").asText());
        return style;
    }

    private Style getAreaStyle(JsonNode node){
        Style style = new Style();
        JsonNode areaNode = node.get("area");
        style.set("stroke-width",areaNode.get("stroke-width").asText());
        style.set("stroke",areaNode.get("stroke").asText());
        style.set("fill",areaNode.get("fill").asText());
        return style;
    }

    public MapDetails getMapFromJson(JsonNode node){
        MapDetails mapDetails = new MapDetails();
        mapDetails.setWidth(node.get("width").asDouble());
        mapDetails.setHeight(node.get("height").asDouble());
        JsonNode pointsNode = node.get("points");
        List<Point> points = new ArrayList<>();
        pointsNode.elements().forEachRemaining(
                s -> {
                    Point point = new Point();
                    point.setX( s.get("px").asDouble());
                    point.setY( s.get("py").asDouble());
                    point.setName( getText(s));
                    point.setStyle(getPointStyle(s.get("style")));
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
                    line.setStyle(getLineStyle(s.get("style")));
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
                    area.setStyle(getAreaStyle(s.get("style")));
                    areas.add(area);
                }

        );
        mapDetails.setAreas(areas);

        return mapDetails;
    }
}
