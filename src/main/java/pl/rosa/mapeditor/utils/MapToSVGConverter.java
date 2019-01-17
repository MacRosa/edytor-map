package pl.rosa.mapeditor.utils;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pl.rosa.mapeditor.models.map.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maciej on 2018-12-14 16:38
 */
@Component
public class MapToSVGConverter {

    private final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

    private void buildPathString(StringBuilder stringBuilder,PathSegment pathSegment){
        stringBuilder.append(pathSegment.getInstruction());
        pathSegment.getParams().forEach(param -> stringBuilder.append(param).append(" "));
    }

    private String pathToString(List<PathSegment> path){
        StringBuilder stringBuilder = new StringBuilder();
        path.forEach(pathSegment ->
                buildPathString(stringBuilder,pathSegment));
        return stringBuilder.toString();
    }

    private Element getArea(Document document, Area area){
        Element areaElement = document.createElementNS(svgNS,"path");
        areaElement.setAttributeNS(null,"fill","cyan");
        areaElement.setAttributeNS(null,"stroke","black");
        areaElement.setAttributeNS(null,"d",pathToString(area.getPath()));
        return areaElement;
    }

    private Element getLine(Document document, Line line){
        Element lineElement = document.createElementNS(svgNS,"path");
        lineElement.setAttributeNS(null,"stroke","black");
        lineElement.setAttributeNS(null,"fill-opacity","0.0");
        lineElement.setAttributeNS(null,"d",pathToString(line.getPath()));
        return lineElement;
    }

    private Element getPoint(Document document, Point point){
        Element circleElement = document.createElementNS(svgNS,"circle");
        circleElement.setAttributeNS(null,"cx",point.getX().toString());
        circleElement.setAttributeNS(null,"cy",point.getY().toString());
        if(point.getStyle() != null){
            circleElement.setAttributeNS(null,"stroke",point.getStyle().get("stroke"));
            circleElement.setAttributeNS(null,"fill",point.getStyle().get("fill"));
            circleElement.setAttributeNS(null,"r",point.getStyle().get("r"));
        }else{
            circleElement.setAttributeNS(null,"stroke","black");
            circleElement.setAttributeNS(null,"fill","green");
            circleElement.setAttributeNS(null,"r","10");
        }
        return circleElement;
    }

    private Element getText(Document document, Text text){
        Element textElement = document.createElementNS(svgNS,"text");
        textElement.setAttributeNS(null,"x",text.getX().toString());
        textElement.setAttributeNS(null,"y",text.getY().toString());
        textElement.setAttributeNS(null,"font-family","Arial");
        textElement.setAttributeNS(null,"font-size","10");
        textElement.setAttributeNS(null,"style","text-anchor: middle");


        textElement.setTextContent(text.getValue());

        return textElement;
    }

    public byte[] toByteSVGImage(MapDetails mapDetails) throws TransformerException {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();

        Document doc = impl.createDocument(svgNS,"svg",null);
        Element svgRoot = doc.getDocumentElement();



        svgRoot.setAttributeNS(null, "width", Integer.toString(mapDetails.getWidth().intValue()));
        svgRoot.setAttributeNS(null, "height",  Integer.toString(mapDetails.getHeight().intValue()));
        Element rectangle = doc.createElementNS(svgNS, "rect");
        rectangle.setAttributeNS(null, "x", "0");
        rectangle.setAttributeNS(null, "y", "0");
        rectangle.setAttributeNS(null, "width",  mapDetails.getWidth().toString());
        rectangle.setAttributeNS(null, "height", mapDetails.getHeight().toString());
        rectangle.setAttributeNS(null, "fill", "white");
        rectangle.setAttributeNS(null,"stroke","black");
        svgRoot.appendChild(rectangle);
        List<Text> texts = new ArrayList<>();
        mapDetails.getAreas().forEach(area -> {texts.add(area.getName());svgRoot.appendChild(getArea(doc,area));});
        mapDetails.getLines().forEach(line -> {texts.add(line.getName());svgRoot.appendChild(getLine(doc,line));});
        mapDetails.getPoints().forEach(point ->{texts.add(point.getName());svgRoot.appendChild(getPoint(doc,point));});
        texts.forEach(text -> svgRoot.appendChild(getText(doc,text)));

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Result result = new StreamResult(output);
        Source source = new DOMSource(doc);

        transformer.transform(source,result);
        return output.toByteArray();
    }
}
