package pl.rosa.mapeditor.models.map;

import java.util.List;

/**
 * Created by Maciej on 2018-12-08 17:34
 */
public class PathSegment {
    private Character Instruction;
    private List<Double> params;
    public PathSegment(){

    }

    public PathSegment(char instruction, List<Double> params) {
        Instruction = instruction;
        this.params = params;
    }

    public Character getInstruction() {
        return Instruction;
    }

    public void setInstruction(Character instruction) {
        Instruction = instruction;
    }

    public List<Double> getParams() {
        return params;
    }

    public void setParams(List<Double> params) {
        this.params = params;
    }
}
