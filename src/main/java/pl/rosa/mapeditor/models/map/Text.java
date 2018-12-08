package pl.rosa.mapeditor.models.map;

/**
 * Created by Maciej on 2018-12-08 12:45
 */
public class Text {

    private String value;
    private Double x;
    private Double y;
    private Style style;

    public Text(){

    }

    public Text(String value, Double x, Double y, Style style) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.style = style;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }
}
