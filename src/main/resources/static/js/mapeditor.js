function calculateTextPosition(px,py,alpha,textWidth,textHeight) {
    let xOffset = 0;
    let yOffset = 0;
    let angle = 0;
    if(alpha >= 360){
        angle = alpha - 360;
        xOffset = textWidth * (angle/90);
        let revAngle = 1 - (angle/90);
        yOffset = -textHeight * revAngle;
    }else{
        angle = alpha - 90;
        let revAngle = 1 - (angle/90);
        xOffset = -textWidth * revAngle;
        if(alpha > 180){
            angle = angle - 180;
            yOffset = textHeight * (angle/90);
        }else{
            yOffset = -textHeight * (angle/90);
        }
    }
    return {x:px+xOffset,y:py+yOffset};
}



let points = [];
let lines = [];
let areas = [];

let lastArea = null;
let lastPoint = null;
let lastLine = null;
let lastText = null;
let dragging = false;
let dragStop = false;
let mapRect = null;

function insertPoint(point){
    point.insertBefore(lastPoint);
}
function insertLine(line){
    line.insertBefore(lastLine);
}
function insertArea(area){
    area.insertBefore(lastArea);
}
function insertText(text){
    text.insertBefore(lastText);
}

class ColorChooser {
    constructor(panel,chooser){
        this.panel = panel;
        this.chooser = chooser;
        this.changeOnColorContext = null;
        this.colorChangedFunction = function(context) {};
        this.hidePanel();
        let tThis = this;
        $("#" + this.chooser).spectrum({
            showPalette : true,
            color: "#000000",
            change : function () {
                tThis.colorChange();
            }
        });
    }

    colorChange(){
        this.colorChangedFunction(this.changeOnColorContext);
    }

    setOnColorChangeFunction(context,func){
        this.changeOnColorContext = context;
        this.colorChangedFunction = func;
    }

    showPanel(){
        $("#"+this.panel).show();
    }

    hidePanel(){
        $("#"+this.panel).hide();
    }

    getColorRGBValue() {
        return $("#"+this.chooser).spectrum("get").toHexString();
    }

    setColor(colorString){
        $("#"+this.chooser).spectrum("set",colorString);
    }
}

let strokeColorChooser = null;
let fillColorChooser = null;


let paper = null;

function getRectFromElement(element){
    let bbox = element.getBBox();
    return paper.rect(bbox.x,bbox.y,bbox.width,bbox.height);
}

let currentAction = null;
let nameInput = null;
let nameField = null;
let addSegmentButton = null;
let deleteSegmentButton = null;
let modCurve = null;
let enterButton = null;

class Element{
    constructor(shape, text){
        this.shape = shape;
        this.text = text;
        this.editTextPos = false;
    }

    elementSelected() {}
    selectionRemoved() {
        if(this.editTextPos){
            this.editTextPosSqr.remove();
            this.editTextPos = false;
        }
    }
    moveStart() {}
    onMove(dx,dy) {}

    nameChanged(newName) {}
    elementDeleted() {}
    startAddingSegments() {}
    segmentAdded(x, y) {}
    stopAddingSegments() {}

    removePathSegment() {}

    textDblClick() {
        this.editTextPos = true;
        this.editTextPosSqr = getRectFromElement(this.text);//.attr({x:x-5,y:y-5,width:width+5,height:height+5});
        this.editTextPosSqr.attr({x: this.editTextPosSqr.attrs.x - 5, y: this.editTextPosSqr.attrs.y - 5,
                                width: this.editTextPosSqr.attrs.width + 10, height: this.editTextPosSqr.attrs.height + 10})
    }

    textMoveStart(){
        this.textCoords = {
            x : this.text.attr("x"),
            y : this.text.attr("y"),
            tbx : this.editTextPosSqr.attr("x"),
            tby : this.editTextPosSqr.attr("y"),
        };
        // noinspection JSUnresolvedVariable
        if(this.textSelectionBox != null){
            // noinspection JSUnresolvedVariable
            this.textCoords.atbx = this.textSelectionBox.attr("x");
            // noinspection JSUnresolvedVariable
            this.textCoords.atby = this.textSelectionBox.attr("y");
        }
    }

    textOnMove(dx,dy){
        this.text.attr({x:this.textCoords.x+dx,y:this.textCoords.y+dy});
        this.editTextPosSqr.attr({x:this.textCoords.tbx+dx,y:this.textCoords.tby+dy});
        // noinspection JSUnresolvedVariable
        if(this.textSelectionBox != null){
            // noinspection JSUnresolvedVariable
            this.textSelectionBox.attr({x:this.textCoords.atbx+dx,y:this.textCoords.atby+dy});
        }
    }
}

class PointElement extends Element{
    constructor(shape, text){
        super(shape, text);
        this.shapeSelectionBox = null;
        this.textSelectionBox = null;
    }

    elementSelected(){
        this.shapeSelectionBox = getRectFromElement(this.shape);
        this.textSelectionBox = getRectFromElement(this.text);
        strokeColorChooser.showPanel();
        strokeColorChooser.setColor(this.shape.attrs.stroke);
        strokeColorChooser.setOnColorChangeFunction(this,function(context){
            context.shape.attr({stroke: this.getColorRGBValue()});
        });

        fillColorChooser.showPanel();
        fillColorChooser.setColor(this.shape.attrs.fill);
        fillColorChooser.setOnColorChangeFunction(this,function(context){
            context.shape.attr({fill: this.getColorRGBValue()});
        });
    }

    selectionRemoved() {
        super.selectionRemoved();
        this.shapeSelectionBox.remove();
        this.textSelectionBox.remove();
        strokeColorChooser.hidePanel();
        fillColorChooser.hidePanel();

    }

    moveStart(){
        if(this.editTextPos){
            this.textMoveStart();
            return;
        }
        this.startCoords = {
            sx : this.shape.attr("cx"),
            sy : this.shape.attr("cy"),
            tx : this.text.attr("x"),
            ty : this.text.attr("y"),
            sbx : this.shapeSelectionBox.attr("x"),
            sby : this.shapeSelectionBox.attr("y"),
            tbx : this.textSelectionBox.attr("x"),
            tby : this.textSelectionBox.attr("y"),
        };
    }

    onMove(dx,dy){
        if(this.editTextPos){
            this.textOnMove(dx,dy);
            return;
        }
        this.shape.attr({cx:this.startCoords.sx+dx,cy:this.startCoords.sy+dy});
        this.text.attr({x:this.startCoords.tx+dx,y:this.startCoords.ty+dy});

        this.shapeSelectionBox.attr({x:this.startCoords.sbx+dx,y:this.startCoords.sby+dy});
        this.textSelectionBox.attr({x:this.startCoords.tbx+dx,y:this.startCoords.tby+dy});

    }

    nameChanged(newName) {
        this.text.attr({text:newName});
        this.textSelectionBox.remove();
        this.textSelectionBox = getRectFromElement(this.text);
    }

    elementDeleted() {
        this.text.remove();
        this.shape.remove();
        points.splice(points.indexOf(this),1);
    }

}

function applyDiff(square,x,y){
    return {x : square.attrs.x+x, y: square.attrs.y+y};
}

function getMiddlePoint(p1,p2){
    let p1l = p1.length;
    let p2l = p2.length;
    return {
        x: (p1[p1l-2] + p2[p2l-2])/2,
        y: (p1[p1l-1] + p2[p2l-1])/2
    }
}

class LineElement extends Element{
    constructor(shape, text){
        super(shape,text);
        this.path = shape.attr("path");
        this.currentSquare = null;
        this.pointSet = paper.set();
    }


    unselectSquare(){
        if(this.currentSquare != null){
            this.currentSquare.selSquare.remove();
            if(this.currentSquare.curveCircle != null){
                this.currentSquare.curveCircle.remove();
                this.currentSquare.curveCircle = null;
            }
            this.currentSquare = null;
            deleteSegmentButton.disabled = true;
            modCurve.disabled = true;
        }
    }

    selectSquare(pathPoint, sq){
        this.unselectSquare();
        let squareOfSelection = paper.rect(sq.attrs.x-5,sq.attrs.y-5,sq.attrs.width+10,sq.attrs.height+10);
        this.currentSquare = { pointInPath: pathPoint, square: sq, selSquare: squareOfSelection, pathIndex : this.path.indexOf(pathPoint)};
        deleteSegmentButton.disabled = false;
        modCurve.disabled = false;
        if(pathPoint[0] === 'L'){
            modCurve.innerHTML = "Add curve";
        }else if(pathPoint[0] === 'Q'){
            modCurve.innerHTML = "Modify curve";
        }
    }

    addSquare(element,x,y){
        let tThis = this;
        let sqr = paper.rect(x-5,y-5,10,10).attr({fill:"black",'fill-opacity':'0.0'})
            .drag(currentAction.onMovement,currentAction.enterMovement)
            .dblclick(function(){tThis.selectSquare(element,this)});
        this.pointSet.push(sqr);
    }


    selectPath(){
        let tThis = this;
        if(currentAction instanceof EditElementAction){
            this.path.forEach(
                function(element){
                    let len = element.length;
                    tThis.addSquare(element,element[len-2],element[len-1]);
                }
            );
        }else{
            this.shape.attrs.path.forEach(
                function(element){
                    paper.rect(element[1]-5,element[2]-5,10,10).attr({fill:"black"});
                }
            );
        }
    }

    removePathSelection(){
        while(this.pointSet.length > 0){
            this.pointSet.pop().remove();
        }
    }

    elementSelected() {
        this.textSelectionBox = getRectFromElement(this.text);
        this.selectPath();
        addSegmentButton.disabled = false;
    }

    selectionRemoved() {
        super.selectionRemoved();
        this.textSelectionBox.remove();
        this.unselectSquare();
        this.removePathSelection();
        addSegmentButton.disabled = true;
    }

    onMovePoint(dx, dy){
        let ndx = dx - this.ld.x;
        let ndy = dy - this.ld.y;
        let len = this.currentSquare.pointInPath.length;
        this.currentSquare.pointInPath[len-2] += ndx;
        this.currentSquare.pointInPath[len-1] += ndy;
        if(this.currentSquare.pointInPath[0] === 'Q'){
            this.currentSquare.pointInPath[1] += ndx;
            this.currentSquare.pointInPath[2] += ndy;
        }
        this.path[this.currentSquare.pathIndex] = this.currentSquare.pointInPath;
        this.shape.attr({path: this.path});
        this.currentSquare.square.attr(applyDiff(this.currentSquare.square,ndx,ndy));
        this.currentSquare.selSquare.attr(applyDiff(this.currentSquare.selSquare,ndx, ndy));
        this.ld.x = dx;
        this.ld.y = dy;
    }

    moveStart() {
        if(this.editTextPos){
            this.textMoveStart();
            return;
        }
        this.ld = {x: 0, y: 0};
    }

    onMove(dx,dy) {
        if(this.editTextPos){
            this.textOnMove(dx,dy);
            return;
        }
        if(this.currentSquare != null){
            this.onMovePoint(dx,dy);
            return;
        }
        let ndx = dx - this.ld.x;
        let ndy = dy - this.ld.y;
        this.text.attr(applyDiff(this.text,ndx,ndy));
        this.textSelectionBox.attr(applyDiff(this.textSelectionBox,ndx,ndy));
        let i = 0;
        let tThis = this;
        this.pointSet.forEach(
            function(el){
                let len = tThis.path[i].length;
                tThis.path[i][len-2] += ndx;
                tThis.path[i][len-1] += ndy;
                if(tThis.path[i][0] === 'Q'){
                    tThis.path[i][1] += ndx;
                    tThis.path[i][2] += ndy;
                }
                el.attr(applyDiff(el,ndx,ndy));
                i++;
            }
        );
        this.shape.attr({path:tThis.path});
        this.ld.x = dx;
        this.ld.y = dy;

    }

    nameChanged(newName) {
        this.text.attr({text:newName});
        this.textSelectionBox.remove();
        this.textSelectionBox = getRectFromElement(this.text);
    }

    elementDeleted() {
        this.text.remove();
        this.shape.remove();
        lines.splice(lines.indexOf(this),1);
    }

    startAddingSegments() {
        let point = this.path[this.path.length-1];
        this.circle = paper.circle(point[point.length-2],point[point.length-1],10);
    }

    segmentAdded(x, y) {
        let element = ['L',x,y];
        this.path.push(element);
        this.addSquare(element,x,y);
        this.shape.attr({path:this.path});
        this.circle.attr({cx:x,cy:y});
    }

    stopAddingSegments() {
        this.circle.remove();
    }

    removePathSegment() {
        if(this.currentSquare != null){
            this.pointSet.exclude(this.currentSquare.square);
            this.currentSquare.square.remove();
            if(this.currentSquare.pointInPath[0] === 'M'){
                let next = this.path[this.currentSquare.pathIndex+1];
                this.path[this.currentSquare.pathIndex+1] = ['M',next[next.length-2],next[next.length-1]];
            }
            this.path.splice(this.currentSquare.pathIndex,1);
            this.shape.attr({path:this.path});
            this.unselectSquare();
        }
    }

    modifyCurve(){
        if(this.currentSquare != null){
            if(this.currentSquare.curveCircle != null){
                if(this.currentSquare.pointInPath[0] === 'Q'){
                    let len = this.currentSquare.pointInPath.length;
                    this.currentSquare.pointInPath = ['L',this.currentSquare.pointInPath[len-2],this.currentSquare.pointInPath[len-1]];
                    this.path[this.currentSquare.pathIndex] = this.currentSquare.pointInPath;
                    this.shape.attr({path:this.path});
                    this.currentSquare.curveCircle.remove();
                    this.currentSquare.curveCircle = null;
                    modCurve.innerHTML = "Add curve";
                }
                return;
            }
            if(this.currentSquare.pointInPath[0] === 'L'){
                let previousPoint = this.path[this.currentSquare.pathIndex-1];
                let middle = getMiddlePoint(this.currentSquare.pointInPath,previousPoint);
                this.currentSquare.pointInPath = ['Q',middle.x,middle.y,this.currentSquare.pointInPath[1],this.currentSquare.pointInPath[2]];
                this.path[this.currentSquare.pathIndex] = this.currentSquare.pointInPath;
                let tThis = this;
                this.currentSquare.curveCircle = paper.circle(middle.x,middle.y,5).attr({fill:"black",'fill-opacity':'0.0',stroke:"black"})
                    .drag(
                        function(dx,dy) {
                            let ndx = dx - this.ld.x;
                            let ndy = dy - this.ld.y;
                            this.attr({cx : this.attrs.cx+ndx, cy : this.attrs.cy+ndy});
                            tThis.currentSquare.pointInPath[1] = this.attrs.cx;
                            tThis.currentSquare.pointInPath[2] = this.attrs.cy;
                            tThis.path[tThis.currentSquare.pathIndex] = tThis.currentSquare.pointInPath;
                            tThis.shape.attr({path:tThis.path});
                            this.ld.x = dx;
                            this.ld.y = dy;
                        },
                        function() {
                            this.ld = {x: 0, y: 0};
                            dragging = true;
                        },
                        function(){
                            dragStop = true;
                        }
                    );
                modCurve.innerHTML = "Remove curve";
            }else if(this.currentSquare.pointInPath[0] === 'Q'){
                let tThis = this;
                let point = this.currentSquare.pointInPath;
                this.currentSquare.curveCircle = paper.circle(point[1],point[2],5).attr({fill:"black",'fill-opacity':'0.0',stroke:"black"})
                    .drag(
                        function(dx,dy) {
                            let ndx = dx - this.ld.x;
                            let ndy = dy - this.ld.y;
                            this.attr({cx : this.attrs.cx+ndx, cy : this.attrs.cy+ndy});
                            tThis.currentSquare.pointInPath[1] = this.attrs.cx;
                            tThis.currentSquare.pointInPath[2] = this.attrs.cy;
                            tThis.path[tThis.currentSquare.pathIndex] = tThis.currentSquare.pointInPath;
                            tThis.shape.attr({path:tThis.path});
                            this.ld.x = dx;
                            this.ld.y = dy;
                        },
                        function() {
                            this.ld = {x: 0, y: 0};
                            dragging = true;
                        },
                        function(){
                            dragStop = true;
                        }
                    );
                modCurve.innerHTML = "Remove curve";
            }
        }
    }
}

class AreaElement extends LineElement{

    constructor(shape, text){
        super(shape, text);
    }


    selectPath(){
        super.selectPath();
        this.pointSet.pop().remove();
    }

    selectionRemoved() {
        super.selectionRemoved();
    }


    elementDeleted() {
        this.text.remove();
        this.shape.remove();
        areas.splice(areas.indexOf(this),1);
    }

    startAddingSegments() {
        let point = this.path[this.path.length-2];
        this.circle = paper.circle(point[point.length-2],point[point.length-1],10);
    }

    segmentAdded(x, y) {
        let element = ['L',x,y];
        this.path.splice(this.path.length-1,0,element);
        this.addSquare(element,x,y);
        this.shape.attr({path:this.path});
        this.circle.attr({cx:x,cy:y});
    }

}

class ButtonAction {
    constructor(elementId){
        this.element = document.getElementById(elementId);
    }

    screenClicked(x, y){}
    enterPressed() {}
    actionSelected(){}
    selectionRemoved(){}
    nameAdded(value){}
    elementSelected(element) {}

    addSegmentPressed() {}
}

let movingMap = true;

class MoveMapAction extends ButtonAction{
    actionSelected(){
        mapRect.drag(
           function(dx,dy){
               let viewBox = paper._viewBox;
               paper.setViewBox(viewBox[0]-(dx-this.lastd.x),viewBox[1]-(dy-this.lastd.y),500,500);
               this.lastd.x = dx;
               this.lastd.y = dy;
           },
           function() {
               this.lastd = {x: 0,y: 0};
           }
       );
    }

    selectionRemoved(){
        mapRect.undrag();
    }
}

class AddPointAction extends ButtonAction {
    constructor(element){
        super(element);
        this.circle = null;
    }

    screenClicked(x, y){
        this.circle = paper.circle(x,y,10).attr({fill:"green"});
        nameField.begin();
    }

    selectionRemoved(){
        if(this.circle != null){
            this.circle.remove();
            this.circle = null;
        }
    }

    nameAdded(value){
        let text = paper.text(this.circle.attr("cx"),this.circle.attr("cy")-20,value);
        insertPoint(this.circle);
        insertText(text);
        points.push(addListeners(new PointElement(this.circle,text)));
        this.circle = null;

    }

}

class AddLineAction extends ButtonAction {
    constructor(element){
        super(element);
        this.pathArray = null;
        this.path = null;
        this.pathCircle = null;
    }

    screenClicked(x, y){
        if(this.path == null){
            this.pathArray = [ ['M',x,y] ];
            this.path = paper.path(this.pathArray);
            this.pathCircle = paper.circle(x,y,10);
        }else{
            this.pathArray.push(['L',x,y]);
            this.path.attr({path:this.pathArray});
            this.pathCircle.attr({cx:x,cy:y});
        }
    }

    selectionRemoved(){
        if(this.pathCircle != null){
            this.pathCircle.remove();
            this.pathCircle = null;
        }
        if(this.path != null){
            this.path.remove();
            this.path = null;
        }
        this.pathArray = null;
    }

    enterPressed(){
        if(this.pathCircle != null){
            this.pathCircle.remove();
            this.pathCircle = null;
        }
        if(this.path == null)
            return;
        nameField.begin();
    }

    nameAdded(value){
        let text = paper.text(0,0,value);
        let textBbox = text.getBBox();
        let middle = this.path.getPointAtLength(this.path.getTotalLength()/2);
        let textPos = calculateTextPosition(middle.x,middle.y,middle.alpha,textBbox.width,textBbox.height);

        text.attr(textPos);
        insertLine(this.path);
        insertText(text);
        lines.push(addListeners(new LineElement(this.path,text)));
        this.path = null;
        this.pathArray = null;
    }

}

class AddAreaAction extends AddLineAction {
    constructor(element){
        super(element);
    }

    enterPressed(){
        super.enterPressed();
        this.pathArray.push(["Z"]);
        this.path.attr({path:this.pathArray,fill:"cyan"});
    }

    nameAdded(value){
        let areaBox = this.path.getBBox();
        let text = paper.text(areaBox.cx,areaBox.cy,nameInput.value);
        insertArea(this.path);
        insertText(text);
        areas.push(addListeners(new AreaElement(this.path,text)));
        this.path = null;
        this.pathArray = null;
    }
}

class EditElementAction extends ButtonAction{
    constructor(element){
        super(element);
        this.currentSelection = null;
        this.isElementSelected = false;
        this.isElementDragged = false;
        this.addingSegments = false;
    }

    selectionRemoved(){
        this.removeSelection();
    }

    screenClicked(x,y){
        if(this.addingSegments && (this.currentSelection != null)){
            this.currentSelection.segmentAdded(x,y);
            return;
        }
        if(!this.isElementSelected && !this.isElementDragged){
            if(!dragging){
                this.removeSelection();
            }
            if(dragStop){
                dragging = false;
                dragStop = false;
            }
        }else{
            this.isElementSelected = false;
            this.isElementDragged = false;
        }
    }

    selectElement(){

        let currentSelection = this.currentSelection;
        let tThis = this;

        this.enterMovement = function() { tThis.isElementDragged = true; currentSelection.moveStart();};
        this.onMovement = function(dx,dy) { tThis.isElementDragged = true; currentSelection.onMove(dx,dy);};

        this.currentSelection.shape.drag(this.onMovement,this.enterMovement);
        this.currentSelection.text.drag(this.onMovement,this.enterMovement)
            .dblclick(function(){tThis.textDoubleClicked()});

        this.currentSelection.elementSelected();

        nameInput.disabled = false;
        nameInput.value = currentSelection.text.attr("text");
        this.isElementSelected = true;
    }

    removeSelection(){
        this.stopAddingElements();
        if(this.currentSelection != null){
            this.currentSelection.selectionRemoved();
            this.currentSelection.shape.undrag();
            this.currentSelection.text.undrag();
            // noinspection JSUnresolvedFunction
            this.currentSelection.text.undblclick();
            this.currentSelection = null;
            nameInput.disabled = true;
        }
    }

    nameAdded(value){
        if(this.currentSelection != null){
            this.currentSelection.nameChanged(value);
        }
    }

    stopAddingElements(){
        if(this.addingSegments === true){
            this.currentSelection.stopAddingSegments();
            this.addingSegments = false;
        }
    }

    enterPressed() {
        this.stopAddingElements();
    }

    elementSelected(element) {
        if(this.addingSegments){
            return;
        }
        if(element === this.currentSelection){
            this.isElementSelected = true;
            return;
        }
        this.removeSelection();
        this.currentSelection = element;
        this.selectElement();
    }

    addSegmentPressed() {
        if(this.addingSegments){
            return;
        }
        if(this.currentSelection != null){
            this.currentSelection.startAddingSegments();
            this.addingSegments = true;
        }
    }

    removeSegment(){
        if(this.currentSelection != null){
            this.currentSelection.removePathSegment();
        }
    }

    textDoubleClicked(){
        this.currentSelection.textDblClick();
    }


    modifyCurveForPoint(){
        if(this.currentSelection instanceof LineElement){
            this.currentSelection.modifyCurve();
        }
    }
}

function loadMap(mapDetails){

    mapRect.attr({width: mapDetails.width, height: mapDetails.height});


    mapDetails.areas.forEach(
        function(line){
            let pathArray = [];
            line.path.forEach( function(ps) {
                // noinspection JSUnresolvedVariable
                let segmentArray = [ps.instruction];
                pathArray.push(segmentArray.concat(ps.params));
         //       pathArray.push([ps.instruction,ps.x,ps.y]);
            });
            let areaShape = paper.path(pathArray).attr({fill:"cyan"});
            let text = paper.text(line.name.x,line.name.y,line.name.value);
            insertArea(areaShape);
            insertText(text);
            areas.push(addListeners(new AreaElement(areaShape,text)));
        }
    );


    mapDetails.lines.forEach(
        function(line){
            let pathArray = [];
            line.path.forEach( function(ps) {
                // noinspection JSUnresolvedVariable
                let segmentArray = [ps.instruction];
                pathArray.push(segmentArray.concat(ps.params));
            });
            let lineShape = paper.path(pathArray);
            let text = paper.text(line.name.x,line.name.y,line.name.value);
            insertLine(lineShape);
            insertText(text);
            lines.push(addListeners(new LineElement(lineShape,text)));
        }
    );

    mapDetails.points.forEach(
        function(point){
            let pointShape = paper.circle(point.x,point.y,10).attr({fill:"green"});
            if(point.style != null){
                pointShape.attr(point.style.styleMap);
            }
            let text = paper.text(point.name.x,point.name.y,point.name.value);
            insertPoint(pointShape);
            insertText(text);
            points.push(addListeners(new PointElement(pointShape,text)));
        }
    );
}

function getData(){
    let mapData = {
        width : mapRect.attrs.width,
        height : mapRect.attrs.height,
        points : [],
        lines : [],
        areas : [],
     };
     points.forEach(function (point) {
         mapData.points.push(
             {
                 px : point.shape.attr("cx"),
                 py : point.shape.attr("cy"),
                 name : point.text.attr("text"),
                 tx : point.text.attr("x"),
                 ty : point.text.attr("y"),
                 style : {
                     point : {
                         stroke : point.shape.attr("stroke"),
                         fill : point.shape.attr("fill")
                     }
                 }
             }
         );
     });
     lines.forEach(function (line) {
         mapData.lines.push(
             {
                 path : line.shape.attr("path"),
                 name : line.text.attr("text"),
                 tx : line.text.attr("x"),
                 ty : line.text.attr("y")
             }
         );
     });
     areas.forEach(function (area) {
         mapData.areas.push(
             {
                 path : area.shape.attr("path"),
                 name : area.text.attr("text"),
                 tx : area.text.attr("x"),
                 ty : area.text.attr("y")
             }
         );
     });
     return mapData;
}

function elementClicked(element){
    if(currentAction == null)
        return;
    currentAction.elementSelected(element);
}

function addListeners(element){
    element.text.click(function elementCl(){elementClicked(element);});
    element.shape.click(function elementCl2(){elementClicked(element);});
    return element;
}

/*
UIElements = {
    area {
        id: ""
        width : 0
        height : 0
    },
    nameInput : id,
    buttons : {
        addPoint = id,
        addLine = id,
        addArea = id,
        editElement = id,
        deleteElement = id,
        moveMap = id,
        saveMap = id,
        addSegment: "addSegment",
        deleteSegment: "deleteSegment",
        curveMod: "curveMod",
        setMapSize: id,
        enter : id
    }
    mapHeight : id,
    mapWidth : id,
    elementStyle : {
        strokeColor : {
            panel : "strokeColor",
            chooser : "strokeColorChooser"
        },fillColor : {
             panel : "fillColor",
             chooser : "fillColorChooser"
        }
    }

}
*/
function initMapEditor(UIElements){

    paper = Raphael(UIElements.area.id,UIElements.area.width,UIElements.area.height);
    paper.setViewBox(0,0,paper.width,paper.height);
    mapRect = paper.rect(0,0,paper.width,paper.height).attr({fill:"white"}).drag(
        function(dx,dy){
            let viewBox = paper._viewBox;
            paper.setViewBox(viewBox[0]-(dx-this.lastd.x),viewBox[1]-(dy-this.lastd.y),500,500);
            this.lastd.x = dx;
            this.lastd.y = dy;
        },
        function() {
            this.lastd = {x: 0,y: 0};
        }
    );

    nameInput = document.getElementById(UIElements.nameInput);

    lastArea = paper.circle(0,0,0).hide();
    lastLine = paper.circle(0,0,0).hide();
    lastPoint = paper.circle(0,0,0).hide();
    lastText = paper.circle(0,0,0).hide();

    nameField = {
        input : nameInput,
        active : false,
        begin : function() {
            this.input.disabled = false;
            this.input.focus();
            this.input.select();
            this.active = true;
        },
        end : function() {
            this.input.disabled = true;
            this.active = false;
        },
        getValue : function() { return this.input.value; }
    };

    $(nameInput).focusout(function (){
        if(nameField.active){
            nameInput.focus();
        }
    });

    class DeleteElementAction extends ButtonAction {
        constructor(element){
            super(element);
        }

        actionSelected(){}


        elementSelected(element) {
            if(confirm("Delete element " + element.text.attr("text") + "?")){
                element.elementDeleted();
            }
        }
    }


    $(document).keypress(function (e){
        if(e.which === 13){
            if(currentAction != null){
                currentAction.enterPressed();
            }
        }
    });


    $(nameInput).keypress(function (e){
        if(e.which === 13){
            if(/\S/.test(nameInput.value)){
                if(currentAction != null){
                    currentAction.nameAdded(nameField.getValue());
                    if(!(currentAction instanceof EditElementAction)){
                        nameField.end();
                    }
                }
            }else{
                alert("Name field is empty.");
            }
            return false;
        }
    });

    let btn = UIElements.buttons;

    let actionArray = [
        new MoveMapAction(btn.moveMap),
        new AddPointAction(btn.addPoint),
        new AddLineAction(btn.addLine),
        new AddAreaAction(btn.addArea),
        new EditElementAction(btn.editElement),
        new DeleteElementAction(btn.deleteElement)
    ];


    addSegmentButton = document.getElementById(btn.addSegment);
    deleteSegmentButton = document.getElementById(btn.deleteSegment);
    modCurve = document.getElementById(btn.curveMod);
    enterButton = $("#" + btn.enter);
    $(enterButton).click(function(){
        if(currentAction != null && currentAction instanceof  EditElementAction){
            if(currentAction.addingSegments){
                currentAction.stopAddingElements();
            }else{
                if(/\S/.test(nameInput.value)){
                    if(currentAction != null){
                        currentAction.nameAdded(nameField.getValue());
                        nameField.end();
                    }
                }else{
                    alert("Name field is empty.");
                }
            }
        }else if(nameField.active){
            if(/\S/.test(nameInput.value)){
                if(currentAction != null){
                    currentAction.nameAdded(nameField.getValue());
                    nameField.end();
                }
            }else{
                alert("Name field is empty.");
            }
        }else{
            currentAction.enterPressed();
        }
    });


    $(addSegmentButton).click(function(){
        currentAction.addSegmentPressed();
    });

    $(modCurve).click(function(){
        if(currentAction instanceof EditElementAction){
            currentAction.modifyCurveForPoint();
        }
    });

    $(deleteSegmentButton).click(function(){
        if(currentAction instanceof EditElementAction){
            currentAction.removeSegment();
        }
    });

    let styleElement = UIElements.elementStyle;

    strokeColorChooser = new ColorChooser(styleElement.strokeColor.panel,
                                            styleElement.strokeColor.chooser);

    fillColorChooser = new ColorChooser(styleElement.fillColor.panel,
                                            styleElement.fillColor.chooser);


    actionArray.forEach(function (action) {
        $(action.element).click(function() {
                if(currentAction != null){
                    currentAction.selectionRemoved();
                    currentAction.element.disabled = false;
                }
                nameField.end();
                currentAction = action;
                currentAction.actionSelected();
                this.disabled = true;
            }
        );
    });

    currentAction = actionArray[0];

    function clickArea(event) {
        let posX = event.pageX - $(this).position().left;
        let posY = event.pageY - $(this).position().top;

        if(nameField.active)
            return;
        if(posX > paper.width)
            return;
        if(currentAction == null){
            alert("Choose action.");
            return;
        }
        posX +=  paper._viewBox[0];
        posY +=  paper._viewBox[1];
        currentAction.screenClicked(posX,posY);
    }

    $("#"+UIElements.area.id).bind('click',clickArea);

    let mapWidth = $("#" + UIElements.mapWidth);
    let mapHeight = $("#" + UIElements.mapHeight);
    $("#" + UIElements.buttons.setMapSize).click(function(){
        mapRect.attr({width: mapWidth.val(),height: mapHeight.val()});
    });




    $("#"+UIElements.saveMap).submit(function () {
        $("<input />").attr('type','hidden')
                .attr('name','mapData')
                .attr('value', JSON.stringify(getData()))
            .appendTo(this);
        return true;
    });


}