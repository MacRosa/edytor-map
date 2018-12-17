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



let paper = null;

function getRectFromElement(element){
    let bbox = element.getBBox();
    return paper.rect(bbox.x,bbox.y,bbox.width,bbox.height);
}

let currentAction = null;
let nameInput = null;
let nameField = null;
let currentSquare = null;
let addSegmentButton = null;

class Element{
    constructor(shape, text){
        this.shape = shape;
        this.text = text;
    }

    elementSelected() {}
    selectionRemoved() {}
    moveStart() {}
    onMove(dx,dy) {}

    nameChanged(newName) {}
    elementDeleted() {}
    startAddingSegments() {}
    segmentAdded(x, y) {}
    stopAddingSegments() {}
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
    }

    selectionRemoved() {
        this.shapeSelectionBox.remove();
        this.textSelectionBox.remove();
    }

    moveStart(){
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

class LineElement extends Element{
    constructor(shape, text){
        super(shape,text);
        this.path = shape.attr("path");
    }

    selectSquare(pathPoint, sq){
        if(currentSquare != null){
            currentSquare.selSquare.remove();
        }
        let squareOfSelection = paper.rect(sq.attrs.x-5,sq.attrs.y-5,sq.attrs.width+10,sq.attrs.height+10);
        currentSquare = { pointInPath: pathPoint, square: sq, selSquare: squareOfSelection, pathIndex : this.path.indexOf(pathPoint)};
    }

    addSquare(element,x,y){
        let tThis = this;
        let sqr = paper.rect(x-5,y-5,10,10).attr({fill:"black"})
            .drag(currentAction.onMovement,currentAction.enterMovement)
            .dblclick(function(){tThis.selectSquare(element,this)});
        this.pointSet.push(sqr);
    }

    selectPath(){
        paper.setStart();
        let tThis = this;
        if(currentAction instanceof EditElementAction){
            this.path.forEach(
                function(element){
                    paper.rect(element[1]-5,element[2]-5,10,10).attr({fill:"black"})
                        .drag(currentAction.onMovement,currentAction.enterMovement)
                        .dblclick(function(){
                                    tThis.selectSquare(element,this);
                                    });

                }
            );
        }else{
            this.path.forEach(
                function(element){
                    paper.rect(element[1]-5,element[2]-5,10,10).attr({fill:"black"});
                }
            );
        }

        this.pointSet = paper.setFinish();
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
        this.textSelectionBox.remove();
        if(currentSquare != null){
            currentSquare.selSquare.remove();
            currentSquare = null;
        }
        this.removePathSelection();
        addSegmentButton.disabled = true;
    }

    movePointStart(){
        this.startCoords = {
            px : currentSquare.pointInPath[1],
            py : currentSquare.pointInPath[2],
            sqx : currentSquare.square.attrs.x,
            sqy : currentSquare.square.attrs.y,
            ssqx : currentSquare.selSquare.attrs.x,
            ssqy : currentSquare.selSquare.attrs.y
        }
    }

    onMovePoint(dx, dy){
        currentSquare.pointInPath[1] = this.startCoords.px + dx;
        currentSquare.pointInPath[2] = this.startCoords.py + dy;
        this.path[currentSquare.pathIndex] = currentSquare.pointInPath;
        this.shape.attr({path: this.path});
        currentSquare.square.attr({x: this.startCoords.sqx+dx,y: this.startCoords.sqy + dy});
        currentSquare.selSquare.attr({x: this.startCoords.ssqx+dx,y: this.startCoords.ssqy + dy});
    }

    moveStart() {
        if(currentSquare != null){
            this.movePointStart();
            return;
        }
        this.startCoords = {
            px :  [],
            py :  [],
            tx : this.text.attr("x"),
            ty : this.text.attr("y"),
            tbx : this.textSelectionBox.attr("x"),
            tby : this.textSelectionBox.attr("y")
        };
        let pst = this.startCoords;
        this.pointSet.forEach(
            function (el) {
                pst.px.push(el.attr("x"));
                pst.py.push(el.attr("y"));
            }
        );
    }
    onMove(dx,dy) {
        if(currentSquare != null){
            this.onMovePoint(dx,dy);
            return;
        }
        this.text.attr({x: this.startCoords.tx+dx,y: this.startCoords.ty+dy});
        this.textSelectionBox.attr({x:this.startCoords.tbx+dx,y:this.startCoords.tby+dy});
        let i = 0;
        let tThis = this;
        this.pointSet.forEach(
            function(el){
                tThis.path[i][1] = tThis.startCoords.px[i]+dx+5;
                tThis.path[i][2] = tThis.startCoords.py[i]+dy+5;
                el.attr({x : tThis.startCoords.px[i]+dx,y : tThis.startCoords.py[i]+dy});
                i++;
            }
        );
        this.shape.attr({path:tThis.path});

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
        this.circle = paper.circle(point[1],point[2],10);
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
}

class AreaElement extends LineElement{

    constructor(shape, text){
        super(shape, text);
        this.secondEndPoint = null;
    }

    onMovePoint(dx, dy){
        super.onMovePoint(dx, dy);
        if(this.secondEndPoint === null)
            return;
        this.secondEndPoint.pointInPath[1] = this.startCoords.px + dx;
        this.secondEndPoint.pointInPath[2] = this.startCoords.py + dy;
        this.path[this.secondEndPoint.pathIndex] = this.secondEndPoint.pointInPath;
        this.shape.attr({path: this.path});
        this.secondEndPoint.square.attr({x: this.startCoords.sqx+dx,y: this.startCoords.sqy + dy});
    }


    selectSquare(pathPoint, sq){
        super.selectSquare(pathPoint, sq);
        let lastIndex = this.path.length-1;

        if(currentSquare.pathIndex === 0){
            this.secondEndPoint = { pointInPath: this.path[lastIndex], pathIndex : lastIndex ,square : this.pointSet.items[lastIndex]};
        }
        else if(currentSquare.pathIndex === lastIndex){
            this.secondEndPoint = { pointInPath: this.path[0], pathIndex : 0 ,square : this.pointSet.items[0]};
        }else {
            this.secondEndPoint = null;
        }
    }

    selectionRemoved() {
        super.selectionRemoved();
        this.secondEndPoint = null;
    }


    elementDeleted() {
        this.text.remove();
        this.shape.remove();
        areas.splice(areas.indexOf(this),1);
    }

    startAddingSegments() {
        let point = this.path[this.path.length-2];
        this.circle = paper.circle(point[1],point[2],10);
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
        this.pathArray.push(["L",this.pathArray[0][1],this.pathArray[0][2]]);
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
            this.removeSelection();
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
        this.currentSelection.text.drag(this.onMovement,this.enterMovement);

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

}

function loadMap(mapDetails){

    mapDetails.areas.forEach(
        function(line){
            let pathArray = [];
            line.path.forEach( function(ps) {
                // noinspection JSUnresolvedVariable
                pathArray.push([ps.instruction,ps.x,ps.y]);
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
                pathArray.push([ps.instruction,ps.x,ps.y]);
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
            let text = paper.text(point.name.x,point.name.y,point.name.value);
            insertPoint(pointShape);
            insertText(text);
            points.push(addListeners(new PointElement(pointShape,text)));
        }
    );
}

function getData(){
    let mapData = {
        width : paper.width,
        height : paper.height,
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
                 ty : point.text.attr("y")
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
        saveMap = id,
    }
}
*/
function initMapEditor(UIElements){

    paper = Raphael(UIElements.area.id,UIElements.area.width,UIElements.area.height);
    paper.rect(0,0,paper.width,paper.height);
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
        new AddPointAction(btn.addPoint),
        new AddLineAction(btn.addLine),
        new AddAreaAction(btn.addArea),
        new EditElementAction(btn.editElement),
        new DeleteElementAction(btn.deleteElement)
    ];

    addSegmentButton = document.getElementById(btn.addSegment);

    $(addSegmentButton).click(function(){
      //  console.log("Add segment clicked. Work in progress");
        currentAction.addSegmentPressed();
    });


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
        currentAction.screenClicked(posX,posY);


    }

    $("#"+UIElements.area.id).bind('click',clickArea);


    $("#"+UIElements.saveMap).submit(function () {
        $("<input />").attr('type','hidden')
                .attr('name','mapData')
                .attr('value', JSON.stringify(getData()))
            .appendTo(this);
        return true;
    });


}