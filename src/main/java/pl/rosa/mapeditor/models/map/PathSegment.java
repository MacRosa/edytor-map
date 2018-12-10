package pl.rosa.mapeditor.models.map;

/**
 * Created by Maciej on 2018-12-08 17:34
 */
public class PathSegment {
    private char Instruction;
    private double X;
    private double Y;

    public PathSegment(){

    }

    public PathSegment(char instruction, double x, double y) {
        Instruction = instruction;
        X = x;
        Y = y;
    }

    public char getInstruction() {
        return Instruction;
    }

    public void setInstruction(char instruction) {
        Instruction = instruction;
    }

    public double getX() {
        return X;
    }

    public void setX(double x) {
        X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        Y = y;
    }
}
