/* global go, i, rutasStorage, angular, Swal */

var myDiagram;
var myPalette;
var myOverview;
var goJs;
var nodeMenu;
var lineMenu;
var objpos = [];
var clearDiagram = true;

var UnselectedBrush = "lightgray";  // item appearance, if not "selected"
var SelectedBrush = "dodgerblue";   // item appearance, if "selected"
var originalNodeDataArray = [];  // Array para almacenar los datos originales
var puertos = [];


//function para inicializar los tooltip de los componentes de boostrap
$(function () {
    $('[data-toggle="tooltip"]').tooltip();
});

//Inicializar el diagrama
function initDiagramProject() {
    // diagram.isReadOnly = true; // solo lectura
    goJs = go.GraphObject.make;

    myDiagram = goJs(go.Diagram, "lienzo", {
        padding: 20,
        allowCopy: false,
        "animationManager.isEnabled": false,
        "toolManager.mouseWheelBehavior": go.ToolManager.WheelZoom,
        scale: 0.5, // extra space when scrolled all the way
        grid: goJs(go.Panel, "Grid", // a simple 10x10 grid
                goJs(go.Shape, "LineH", {stroke: "lightgray", strokeWidth: 0.5}),
                goJs(go.Shape, "LineV", {stroke: "lightgray", strokeWidth: 0.5})
                )});
    myDiagram.layoutDiagram(false);

    nodeMenu =
            goJs("ContextMenu",
                    itemButton("Configuration and parameterization",
                            function (e, obj) {
                                e.diagram.commandHandler.copySelection();
                                //alert("Construction zone");
                                //console.log(obj.part.data);
                                //console.log(myDiagram.model.linkDataArray);
                                angular.element($('[ng-controller="controllerWorkIoT"]')).scope()
                                        .getParametersBD(obj.part.data, myDiagram.model.linkDataArray);
                            }),
                    itemButton("Delete component",
                            function (e, obj) {
                                Swal.fire({
                                    title: 'Are you sure?',
                                    text: "All data entered will be permanently deleted.!",
                                    icon: 'warning',
                                    showCancelButton: true,
                                    confirmButtonColor: '#3085d6',
                                    cancelButtonColor: '#d33',
                                    confirmButtonText: 'Yes, delete it.'
                                }).then((result) => {
                                    if (result.isConfirmed) {
                                        e.diagram.commandHandler.deleteSelection();
                                        return true;
                                    }
                                    ;
                                });
                            })
                    );

    lineMenu =
            goJs("ContextMenu",
                    itemButton("Delete",
                            function (e, obj) {
                                Swal.fire({
                                    title: 'Are you sure?',
                                    text: "All data entered will be permanently deleted.!",
                                    icon: 'warning',
                                    showCancelButton: true,
                                    confirmButtonColor: '#3085d6',
                                    cancelButtonColor: '#d33',
                                    confirmButtonText: 'Yes, cancels all!'
                                }).then((result) => {
                                    if (result.isConfirmed) {
                                        e.diagram.commandHandler.deleteSelection();
                                        return true;
                                    }
                                    ;
                                });
                            })
                    );

    var toolTipTemplate =
            goJs(go.Adornment, "Auto",
                    goJs(go.Shape, {fill: "#FFFFCC"}),
                    goJs(go.TextBlock, {margin: 4},
                            new go.Binding("text", "", function (data) {
                                return "Port: " + getPortsInfo(data);
                            })
                            )
                    );

    myDiagram.nodeTemplate =
            goJs(go.Node, "Auto",
                    {
                        selectionAdorned: false,
                        contextMenu: nodeMenu,
                        desiredSize: new go.Size(20, 20)
                                //movable: false
                    }, new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify),
                    goJs(go.Shape,
                            {
                                name: "SHAPE",
                                fill: "transparent",
                                stroke: "transparent",
                                strokeWidth: 5,
                                figure: "Rectangle",
                                /*spot1: new go.Spot(0, 0, 5, 1), // keep the text inside the shape
                                 spot2: new go.Spot(1, 1, -5, 0),*/
                                // some port-related properties
                                portId: "",
                                fromSpot: new go.Spot(0.50, 0.25), toSpot: new go.Spot(0.50, 0.25),
                                toLinkable: true,
                                fromEndSegmentLength: 0, toEndSegmentLength: 0,
                                fromLinkable: true,
                                cursor: "pointer",
                                mouseEnter: function (e, port) {
                                    if (!e.diagram.isReadOnly) {
                                        port.fill = "red";
                                        port.stroke = "black";
                                    }
                                },
                                mouseLeave: function (e, port) {
                                    port.fill = "transparent";
                                    port.stroke = "transparent";
                                },
                                toolTip: toolTipTemplate
                            }
                    ));


    myDiagram.groupTemplate =
            goJs(go.Group, "Auto",
                    {
                        selectionAdorned: false,
                        locationSpot: go.Spot.Center, locationObjectName: "ICON",
                        //movable: false,
                        contextMenu: nodeMenu,
                        cursor: "move"
                    },
                    new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify),
                    goJs(go.Shape, "RoundedRectangle",
                            {stroke: "gray", strokeWidth: 1, fill: "transparent"},
                            new go.Binding("stroke", "isSelected", function (b) {
                                return b ? SelectedBrush : UnselectedBrush;
                            }).ofObject()),
                    goJs(go.Panel, "Vertical",
                            {margin: 0},
                            goJs(go.Panel, "Spot",
                                    {name: "ICON"}, // an initial height; size will be set by InputOutputGroupLayout
                                    goJs(go.Shape,
                                            {fill: null, stroke: null, stretch: go.GraphObject.Fill}),
                                    goJs(go.Picture,
                                            new go.Binding("source", "img"), {})
                                    )
                            )
                    );


    myDiagram.linkTemplate =
            goJs(go.Link,
                    {
                        contextMenu: lineMenu,
                        routing: go.Link.Orthogonal,
                        curve: go.Link.JumpOver,
                        reshapable: true,
                        resegmentable: true,
                        relinkableFrom: true,
                        relinkableTo: true,
                        selectionAdorned: true,
                        adjusting: go.Link.End,
                        corner: 10,
                        fromShortLength: 2,
                        toShortLength: 2,
                        cursor: "pointer",
                        selectionAdornmentTemplate:
                                goJs(go.Adornment, "Link",
                                        goJs(go.Shape, {
                                            isPanelMain: true,
                                            stroke: "#176b48",
                                            strokeWidth: 12,
                                            opacity: 0.14
                                        })),
                        mouseEnter: function (e, link) {
                            var hoverShape = link.findObject("CABLE_HOVER");
                            if (hoverShape) {
                                hoverShape.stroke = "rgba(23, 107, 72, 0.12)";
                            }
                        },
                        mouseLeave: function (e, link) {
                            var hoverShape = link.findObject("CABLE_HOVER");
                            if (hoverShape) {
                                hoverShape.stroke = "transparent";
                            }
                        }
                    },
                    new go.Binding("points").makeTwoWay(),
                    // Wide transparent hit area: the visible cable stays compact but is easy to select.
                    goJs(go.Shape, {
                        isPanelMain: true,
                        stroke: "transparent",
                        strokeWidth: 18
                    }),
                    goJs(go.Shape, {
                        name: "CABLE_HOVER",
                        isPanelMain: true,
                        stroke: "transparent",
                        strokeWidth: 11,
                        strokeCap: "round",
                        strokeJoin: "round"
                    }),
                    // Light outline keeps colored wires legible over devices and the drawing grid.
                    goJs(go.Shape, {
                        isPanelMain: true,
                        stroke: "rgba(255, 255, 255, 0.92)",
                        strokeWidth: 7,
                        strokeCap: "round",
                        strokeJoin: "round"
                    }),
                    goJs(go.Shape, {
                        name: "CABLE",
                        isPanelMain: true,
                        strokeWidth: 4.5,
                        strokeCap: "round",
                        strokeJoin: "round"
                    }, new go.Binding("stroke", "color", function (color) {
                        return color || "#3C4042";
                    })));

    configureCableEditingTools();

    var tool = myDiagram.toolManager.relinkingTool;

    // Keep reconnection restricted to actual component-port nodes.
    tool.linkValidation = function (fromnode, fromport, tonode, toport, link) {
        return Boolean(fromnode && tonode && fromport && toport &&
                fromnode.data && tonode.data &&
                fromnode.data._g !== undefined && tonode.data._g !== undefined);
    };

    myOverview = goJs(go.Overview, "overView", {
        observed: myDiagram
    });

    myDiagram.undoManager.isEnabled = true;
    myDiagram.toolManager.draggingTool.isGridSnapEnabled = true;
    myDiagram.grid.gridCellSize = new go.Size(20, 20);
    myDiagram.toolManager.toolTipDuration = 5000;

    myDiagram.mouseDrop = function (e) {
        ////console.log(myDiagram.model.part)
        //console.log(myDiagram.selection);
        //console.log(myDiagram.model.nodeDataArray);
        //console.log(myDiagram.model.nodeDataArray[myDiagram.model.nodeDataArray.length - 1]);
        //angular.element($('[ng-controller="controllerWork"]')).scope().sendModel();
    };

    myDiagram.addDiagramListener("LinkDrawn", function (e) {
        validateCableLink(e.subject);
    });

    myDiagram.addDiagramListener("LinkRelinked", function (e) {
        validateCableLink(e.subject);
    });
}

function validateCableLink(link) {
    if (!link || !link.fromNode || !link.toNode) {
        return;
    }

    // Ports are represented by child Nodes; their public data object contains
    // the electrical capabilities and the _g key of the owning component.
    validaciones = validarConexionCables(link.fromNode.data, link.toNode.data, link);
    if (validaciones.showAlert) {
        alertAll(validaciones);
        if (validaciones.status !== 1) {
            myDiagram.model.removeLinkData(link.data);
        }
    }
}

function configureCableEditingTools() {
    var reshapeTool = myDiagram.toolManager.linkReshapingTool;
    var linkingTool = myDiagram.toolManager.linkingTool;
    var relinkingTool = myDiagram.toolManager.relinkingTool;

    // Existing bend: larger white handle with a green outline.
    reshapeTool.handleArchetype = goJs(go.Shape, "Circle", {
        desiredSize: new go.Size(11, 11),
        fill: "#ffffff",
        stroke: "#176b48",
        strokeWidth: 2,
        cursor: "move"
    });

    // Intermediate handle: dragging it creates a new orthogonal bend.
    reshapeTool.midHandleArchetype = goJs(go.Shape, "Circle", {
        desiredSize: new go.Size(8, 8),
        fill: "#8fc9ad",
        stroke: "#ffffff",
        strokeWidth: 1.5,
        cursor: "copy"
    });

    linkingTool.portGravity = 24;
    relinkingTool.portGravity = 24;
    relinkingTool.fromHandleArchetype = goJs(go.Shape, "Circle", {
        desiredSize: new go.Size(13, 13),
        fill: "#ffffff",
        stroke: "#287ba3",
        strokeWidth: 2.5,
        cursor: "pointer"
    });
    relinkingTool.toHandleArchetype = goJs(go.Shape, "Circle", {
        desiredSize: new go.Size(13, 13),
        fill: "#ffffff",
        stroke: "#d36b4c",
        strokeWidth: 2.5,
        cursor: "pointer"
    });

    // Preview the same orthogonal, rounded cable while the user is connecting ports.
    linkingTool.temporaryLink = goJs(go.Link, {
        routing: go.Link.Orthogonal,
        corner: 10,
        curve: go.Link.JumpOver
    },
            goJs(go.Shape, {
                isPanelMain: true,
                stroke: "rgba(255, 255, 255, 0.92)",
                strokeWidth: 7,
                strokeCap: "round",
                strokeJoin: "round"
            }),
            goJs(go.Shape, {
                isPanelMain: true,
                stroke: "#3C4042",
                strokeWidth: 4.5,
                strokeCap: "round",
                strokeJoin: "round"
            }));
}

getPortsInfo = (port) => {
    let description = port.name_port; // Comienza con el nombre del puerto

    // Lista de propiedades a verificar
    const properties = ["digital", "analog", "energy", "digital_analog", "input", "output", "input_output"];

    // Recorre las propiedades y verifica cuál está en true
    for (let prop of properties) {
        if (port[prop] === true) {
            description += ", " + prop; // Concatena el nombre de la propiedad si es true
            break; // Sale del bucle después de encontrar el primer true
        }
    }

    return description; // Devuelve la descripción del puerto
}

validarConexionCables = (fromNode, toNode, link) => {
    let ac = angular.element($('[ng-controller="controllerWorkIoT"]')).scope();
    let validationCable = {showAlert: true, status: 4, information: "The port is not configured correctly."};

    if ((fromNode.input ||
            fromNode.output ||
            fromNode.digital ||
            fromNode.analog ||
            fromNode.digital_analog ||
            fromNode.input_output ||
            fromNode.energy ||
            fromNode.data ||
            fromNode.gnd) &&
            (!toNode.input &&
                    !toNode.output &&
                    !toNode.digital &&
                    !toNode.analog &&
                    !toNode.digital_analog &&
                    !toNode.input_output &&
                    !toNode.gnd &&
                    !toNode.energy &&
                    !toNode.data)) {
        validationCable.showAlert = false;
        return validationCable;
    }

    if ((fromNode.digital || fromNode.analog || fromNode.digital_analog)
            && (!fromNode.input || !fromNode.output || !fromNode.input_output)) {
        // validar la conexion entre puertos digitales y analogicos
        if ((fromNode.digital && !fromNode.analog) && (toNode.digital && !toNode.analog) && !fromNode.digital_analog) {
            validationCable.showAlert = false;
            return validationCable;
        } else {
            validationCable.showAlert = true;
            validationCable.status = 3;
            validationCable.information = "The starting port expects a digital port";
        }

        if ((!fromNode.digital && fromNode.analog) && (!toNode.digital && toNode.analog) && !fromNode.digital_analog) {
            validationCable.showAlert = false;
            return validationCable;
        } else {
            validationCable.showAlert = true;
            validationCable.status = 3;
            validationCable.information = "The starting port expects an analog port";
        }

        if (!fromNode.digital && !fromNode.analog && fromNode.digital_analog) {
            $("#modalValidationCable").modal();
            validationCable.showAlert = true;
            validationCable.status = 1;
            validationCable.information = "Configure the port to your preference";
            ac.$apply(function () {
                ac.typeFromPorts[0].info = "Digital";
                ac.typeFromPorts[0].value = "digital";
                ac.typeFromPorts[1].info = "Analog";
                ac.typeFromPorts[1].value = "analog";
                ac.ip_portFrom_name = fromNode.name_port;
                ac.fromNode = fromNode;
                ac.toNode = toNode;
                ac.cable = link;
                ac.legendValidation = "Port connection usage verification be digital or analog.";
            });
        }

    }

    if ((fromNode.input || fromNode.output || fromNode.input_output)
            && (!fromNode.digital || !fromNode.analog || !fromNode.digital_analog)) {

        // validar la conexion entre los puertos de entrada y salida
        if ((fromNode.input && !fromNode.output) && (toNode.output && !toNode.input) && !fromNode.input_output) {
            validationCable.showAlert = false;
            return validationCable;
        } else {
            validationCable.showAlert = true;
            validationCable.status = 3;
            validationCable.information = "The port of departure expects a port of departure, because it is a port of entry.";
        }

        if ((!fromNode.input && fromNode.output) && (!toNode.output && toNode.input) && !fromNode.input_output) {
            validationCable.showAlert = false;
            return validationCable;
        } else {
            validationCable.showAlert = true;
            validationCable.status = 3;
            validationCable.information = "The port of departure expects a port of entry, because it is an exit port.";
        }

        if (!fromNode.input && !fromNode.output && fromNode.input_output) {
            $("#modalValidationCable").modal();
            ac.$apply(function () {
                ac.typeFromPorts[0].info = "Input";
                ac.typeFromPorts[0].value = "input";
                ac.typeFromPorts[1].info = "Output";
                ac.typeFromPorts[1].value = "output";
                ac.ip_portFrom_name = fromNode.name_port;
                ac.fromNode = fromNode;
                ac.toNode = toNode;
                ac.cable = link;
                ac.legendValidation = "Port connection usage verification be input or output.";
            });
        }
    }

    if ((fromNode.gnd && toNode.gnd) || (fromNode.energy && toNode.energy)) {
        validationCable.showAlert = false;
        return validationCable;
    }

    return validationCable;
};

searchModel = (json_structure_iot_device) => {
    let model_loader = JSON.parse(json_structure_iot_device);
    if (!isObjEmpty(model_loader)) {
        if (model_loader.nodeDataArray.length > 0) {
            loadModel(JSON.parse(json_structure_iot_device));
        } else {
            myDiagram.clear();
        }
    }
};

loadModel = (datajson_iot) => {
    var modelNew = new go.GraphLinksModel();

    modelNew.nodeIsGroupProperty = "_isg";
    modelNew.nodeGroupKeyProperty = "_g";

    let components = datajson_iot.nodeDataArray;
    for (let i = 0; i < components.length; i++) {
        let nodedata = components[i];
        nodedata._isg = true;
        //nodedata.loc = "0 0";
        /*let newKey = nodedata.key.toString();
         newKey += "loader";
         //console.log(newKey);
         nodedata.key = newKey;*/
        for (let iports = 0; iports < nodedata.ports.length; iports++) {
            nodedata.ports[iports]._g = nodedata.key;
        }

        //console.log(nodedata.ports);
        modelNew.addNodeDataCollection(components);
        modelNew.addNodeDataCollection(nodedata.ports);
    }

    modelNew.addLinkDataCollection(datajson_iot.linkDataArray);
    myDiagram.model = modelNew;
};

//Inicializar el menu contextual
function itemButton(text, action, flag) {
    return goJs("ContextMenuButton",
            goJs(go.TextBlock, text),
            {click: action},
            flag ? new go.Binding("visible", "", function (o, e) {
                return o.diagram ? flag(o, e) : false;
            }).ofObject() : {});
}

function initContextMenu() {
    myDiagram.contextMenu = goJs("ContextMenu",
            itemButton("Paste",
                    function (e, obj) {
                        e.diagram.commandHandler.pasteSelection(e.diagram.toolManager.contextMenuTool.mouseDownPoint);
                    },
                    function (o) {
                        return o.diagram.commandHandler.canPasteSelection(o.diagram.toolManager.contextMenuTool.mouseDownPoint);
                    }),
            itemButton("Undo",
                    function (e, obj) {
                        e.diagram.commandHandler.undo();
                    },
                    function (o) {
                        return o.diagram.commandHandler.canUndo();
                    }),
            itemButton("Redo",
                    function (e, obj) {
                        e.diagram.commandHandler.redo();
                    },
                    function (o) {
                        return o.diagram.commandHandler.canRedo();
                    })
            );
}

function normalizePaletteText(value) {
    return String(value || "")
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .trim();
}

function createPaletteModel(nodeData) {
    var modelPalette = new go.GraphLinksModel();
    modelPalette.nodeIsGroupProperty = "_isg";
    modelPalette.nodeGroupKeyProperty = "_g";
    modelPalette.addNodeDataCollection(JSON.parse(JSON.stringify(nodeData || [])));
    return modelPalette;
}

function updatePaletteSearchState(resultCount, query) {
    var count = document.getElementById("paletteResultCount");
    var empty = document.getElementById("paletteEmptyState");
    var search = document.querySelector(".iot-component-search");

    if (count) {
        count.textContent = resultCount + (resultCount === 1 ? " device" : " devices");
    }
    if (empty) {
        empty.classList.toggle("is-visible", resultCount === 0 && originalNodeDataArray.length > 0);
    }
    if (search) {
        search.classList.toggle("has-query", Boolean(query));
    }
}

function searchComponentPalette() {
    var searchBox = document.getElementById("searchBox");
    var query = normalizePaletteText(searchBox ? searchBox.value : "");

    if (!myPalette || !originalNodeDataArray.length) {
        updatePaletteSearchState(0, query);
        return;
    }

    var matchingGroupKeys = {};
    var matchedComponents = originalNodeDataArray.filter(function (item) {
        if (!item || item._isg !== true) {
            return false;
        }
        var searchableText = normalizePaletteText([item.name, item.type, item.description].join(" "));
        var matches = !query || searchableText.indexOf(query) !== -1;
        if (matches) {
            matchingGroupKeys[String(item.key)] = true;
        }
        return matches;
    });

    var filteredData = originalNodeDataArray.filter(function (item) {
        return item && (item._isg === true ? matchingGroupKeys[String(item.key)] : matchingGroupKeys[String(item._g)]);
    });

    myPalette.model = createPaletteModel(filteredData);
    updatePaletteSearchState(matchedComponents.length, query);
    requestIoTDiagramResize();
}

function clearComponentPaletteSearch() {
    var searchBox = document.getElementById("searchBox");
    if (searchBox) {
        searchBox.value = "";
        searchBox.focus();
    }
    searchComponentPalette();
}

function undoIoTDiagram() {
    if (myDiagram && myDiagram.commandHandler.canUndo()) {
        myDiagram.commandHandler.undo();
    }
}

function redoIoTDiagram() {
    if (myDiagram && myDiagram.commandHandler.canRedo()) {
        myDiagram.commandHandler.redo();
    }
}

function requestIoTDiagramResize() {
    window.requestAnimationFrame(function () {
        if (myDiagram) {
            myDiagram.requestUpdate();
        }
        if (myPalette) {
            myPalette.requestUpdate();
        }
        if (myOverview) {
            myOverview.requestUpdate();
        }
    });
}

getPortsSearch = (param_ports) => {
    let ports = [];
    let ports_param = param_ports.ports;
    if (ports_param !== undefined) {
        for (let i = 0; i < ports_param.length; i++) {
            ports.push({
                "key": ports_param[i].name_port + "-" + param_ports.key,
                "name_port": ports_param[i].name_port,
                "digital": ports_param[i].digital,
                "analog": ports_param[i].analog,
                "data": ports_param[i].data,
                "loc": ports_param[i].loc,
                "energy": ports_param[i].energy,
                "digital_analog": ports_param[i].digital_analog,
                "input": ports_param[i].input,
                "output": ports_param[i].output,
                "input_output": ports_param[i].input_output,
                "gnd": ports_param[i].gnd,
                "min": ports_param[i].min,
                "max": ports_param[i].max,
                "idComponent": param_ports.key,
                "_g": param_ports.key
            });
        }
    }

    return ports;

};

//Inicializar la paleta de los componentes
function initPalette() {
    myPalette = goJs(go.Palette, "myPalette",
            {
                padding: new go.Margin(4, 5, 8, 5),
                contentAlignment: go.Spot.TopCenter,
                allowHorizontalScroll: false,
                "animationManager.isEnabled": false,
                layout: goJs(go.GridLayout,
                        {
                            alignment: go.GridLayout.Location,
                            wrappingColumn: 1,
                            spacing: new go.Size(0, 2),
                            cellSize: new go.Size(1, 1)
                        })
            });

    myPalette.nodeTemplate =
            goJs(go.Node, "Auto",
                    {
                        selectionAdorned: false,
                        contextMenu: nodeMenu,
                        desiredSize: new go.Size(20, 20),
                        visible: false,
                        //movable: false
                    }, new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify),
                    goJs(go.Shape,
                            {
                                name: "SHAPE",
                                fill: "red", stroke: "red",
                                figure: "RoundedRectangle",
                                spot1: new go.Spot(0, 0, 5, 1), // keep the text inside the shape
                                spot2: new go.Spot(1, 1, -5, 0),
                                // some port-related properties
                                portId: "",
                                toSpot: go.Spot.Left,
                                toLinkable: true,
                                fromSpot: go.Spot.Right,
                                fromLinkable: true,
                                cursor: "pointer",
                                mouseEnter: function (e, port) {
                                    if (!e.diagram.isReadOnly)
                                        port.fill = "red";
                                },
                                mouseLeave: function (e, port) {
                                    port.fill = "transparent";
                                }
                            })
                    );


    myPalette.groupTemplate =
            goJs(go.Group, "Auto",
                    {
                        name: "toolTdd",
                        selectionAdorned: false,
                        locationSpot: go.Spot.Center,
                        locationObjectName: "CARD",
                        cursor: "grab",
                        width: 244,
                        height: 78,
                        margin: new go.Margin(4, 4, 4, 4),
                        mouseEnter: function (e, toolTdd) {
                            var card = toolTdd.findObject("CARD");
                            if (card) {
                                card.fill = "#f1f8f4";
                                card.stroke = "#9ecbb6";
                            }
                        },
                        mouseLeave: function (e, toolTdd) {
                            var card = toolTdd.findObject("CARD");
                            if (card) {
                                card.fill = "#ffffff";
                                card.stroke = "#dce5e1";
                            }
                        }
                    },
                    new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify),
                    goJs(go.Shape, "RoundedRectangle", {
                        name: "CARD",
                        fill: "#ffffff",
                        stroke: "#dce5e1",
                        strokeWidth: 1,
                        parameter1: 7
                    }),
                    goJs(go.Panel, "Horizontal", {margin: 8},
                            goJs(go.Panel, "Auto", {width: 56, height: 56},
                                    goJs(go.Shape, "RoundedRectangle", {
                                        fill: "#f4f8f6",
                                        stroke: "#e3ebe7",
                                        parameter1: 6
                                    }),
                                    goJs(go.Picture, {
                                        name: "ICON",
                                        width: 45,
                                        height: 45,
                                        imageStretch: go.GraphObject.Uniform
                                    }, new go.Binding("source", "img"))
                                    ),
                            goJs(go.Panel, "Vertical", {
                                width: 162,
                                margin: new go.Margin(1, 0, 0, 9),
                                defaultAlignment: go.Spot.Left
                            },
                                    goJs(go.TextBlock, {
                                        name: "TBE",
                                        font: "600 11px 'Roboto Mono', monospace",
                                        stroke: "#31433a",
                                        width: 158,
                                        maxLines: 1,
                                        overflow: go.TextBlock.OverflowEllipsis
                                    }, new go.Binding("text", "name")),
                                    goJs(go.TextBlock, {
                                        font: "700 7px 'Roboto Mono', monospace",
                                        stroke: "#27805a",
                                        margin: new go.Margin(3, 0, 3, 0),
                                        width: 158
                                    }, new go.Binding("text", "type", function (type) {
                                        return String(type || "IoT device").toUpperCase();
                                    })),
                                    goJs(go.TextBlock, {
                                        name: "TBD",
                                        font: "9px 'Roboto Mono', monospace",
                                        stroke: "#74827b",
                                        width: 158,
                                        maxLines: 2,
                                        overflow: go.TextBlock.OverflowEllipsis,
                                        wrap: go.TextBlock.WrapFit
                                    }, new go.Binding("text", "description"))
                                    )
                            )
                    );

    requestIoTDiagramResize();
}

//Cargar los datos de los componentes(funcion que debe servir para el webSocket)
function loadComponents(obj) {
    ////console.log(myDiagram.model);
    //let components = [];
    let ports = [];
    puertos = [];

    var model = new go.GraphLinksModel();
    var modelPalet = new go.GraphLinksModel();

    myDiagram.model.nodeIsGroupProperty = "_isg";
    myDiagram.model.nodeGroupKeyProperty = "_g";

    modelPalet.nodeIsGroupProperty = "_isg";
    modelPalet.nodeGroupKeyProperty = "_g";

    let components = obj.data.map(
            dataItem => {
                // Asegúrate de que la descripción no exceda los 100 caracteres
                let description = dataItem.description_component || "No description available.";
                if (description.length > 100) {
                    description = description.substring(0, 99);
                }

                let puertoX = getPorts(dataItem);
                if (puertoX.length > 0) {
                    puertos.push(...puertoX);
                }


                // Estructura de datos para un componente, incluyendo sus puertos
                return {
                    key: dataItem.id_component,
                    img: rutasStorage.components + dataItem.pathimg_component + "/component.png",
                    name: dataItem.name_component,
                    type: dataItem.type_component,
                    description: description,
                    loc: "0 0",
                    _isg: true,
                    ports: getPorts(dataItem),
                    code: dataItem.data_json.code
                };
            });

    modelPalet.addNodeDataCollection(components);
    modelPalet.addNodeDataCollection(puertos);


    //myDiagram.clear();
    myPalette.model = modelPalet;
    // Guardar una copia de los datos originales
    originalNodeDataArray = JSON.parse(JSON.stringify(myPalette.model.nodeDataArray));
    searchComponentPalette();
}

window.addEventListener("resize", requestIoTDiagramResize);

getPorts = (param_ports) => {
    let ports = [];
    let ports_param = param_ports.data_json.parameters;
    if (ports_param !== undefined) {
        for (let i = 0; i < ports_param.length; i++) {
            ports.push({
                "key": ports_param[i].name_port + "-" + param_ports.id_component,
                "name_port": ports_param[i].name_port,
                "digital": ports_param[i].digital,
                "analog": ports_param[i].analog,
                "data": ports_param[i].data,
                "loc": ports_param[i].loc,
                "energy": ports_param[i].energy,
                "digital_analog": ports_param[i].digital_analog,
                "input": ports_param[i].input,
                "output": ports_param[i].output,
                "input_output": ports_param[i].input_output,
                "gnd": ports_param[i].gnd,
                "min": ports_param[i].min,
                "max": ports_param[i].max,
                "idComponent": param_ports.id_component,
                "_g": param_ports.id_component
            });
        }
    }

    return ports;

};

myCallback = (blob) => {
//    var url = window.URL.createObjectURL(blob);
//    var filename = "IoT_Design.png";
//
//    var a = document.createElement("a");
//    a.style = "display: none";
//    a.href = url;
//    a.download = filename;
//
//    // IE 11
//    if (window.navigator.msSaveBlob !== undefined) {
//        window.navigator.msSaveBlob(blob, filename);
//        return;
//    }
//
//    document.body.appendChild(a);
//    requestAnimationFrame(function () {
//        a.click();
//        window.URL.revokeObjectURL(url);
//        document.body.removeChild(a);
//    });



    //console.log(blob);

    let img = new Image();
    img.src = blob;
    img.onload = () => {
        let me_canvas = document.createElement('canvas');
        let ctx = me_canvas.getContext("2d");
        me_canvas.width = img.width;
        me_canvas.height = img.height;

        const marca = new Image();
        ctx.drawImage(img, 0, 0, img.width, img.height);
        marca.src = "resources/img/logo-app/logoGraficosicon.png";
        marca.onload = () => {
            let marcaLen = [
                150,
                90,
                me_canvas.width,
                me_canvas.height];
            ctx.drawImage(marca, 10,
                    marcaLen[3] - marcaLen[1] - 10,
                    marcaLen[0], marcaLen[1]);

            let link = document.createElement('a');
            link.download = 'TddM4IoTs_IoT.png';
            link.href = me_canvas.toDataURL();
            link.click();
        };
    };
};

makeBlob = () => {
    var blob = myDiagram.makeImageData({background: "white", returnType: "image/jpeg", callback: myCallback});
};
