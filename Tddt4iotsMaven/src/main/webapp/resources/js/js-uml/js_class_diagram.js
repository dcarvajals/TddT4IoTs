/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* global mainContext, width, motionContext, height, diagramClass, UMLGeneralization, UMLDependency, UMLAssociation, UMLAggregation, UMLComposition */

var elementsClass = [];
const visibilityGlobal = {
    "public": "+",
    "private": "-",
    "protected": "#",
    "static": "$",
    "abstract": "?",
    "final": "¬",
    "interface": "@"
};

const relationsGlobal = {
    "agregation": UMLAggregation,
    "aggregation": UMLAggregation,
    "dependency": UMLDependency,
    "generalization": UMLGeneralization,
    "asociation": UMLAssociation,
    "association": UMLAssociation,
    "composition": UMLComposition
};

const relationsGlobalname = {
    "agregation": "agregation",
    "aggregation": "agregation",
    "dependency": "dependency",
    "generalization": "generalization",
    "asociation": "asociation",
    "association": "asociation",
    "composition": "composition"
};

function updateClassDiagramOpenAi(jsonInterprete, action) {
    if (!jsonInterprete || !Array.isArray(jsonInterprete.diagram)) {
        throw new Error("The generated model does not include a diagram list.");
    }

    const symbolForVisibility = (visibility) => visibilityGlobal[String(visibility || "").toLowerCase()] || "";
    const arrayOrEmpty = (value) => Array.isArray(value) ? value : [];
    const diagramArea = document.getElementById("areaDiagram");
    const availableWidth = diagramArea ? Math.max(diagramArea.clientWidth, 320) : 900;
    const columns = Math.max(1, Math.min(4, Math.floor((availableWidth - 50) / 260)));
    let elementIndex = 0;
    let classCount = 0;
    let enumCount = 0;

    const nextPosition = () => {
        const position = {
            x: 45 + (elementIndex % columns) * 260,
            y: 45 + Math.floor(elementIndex / columns) * 210
        };
        elementIndex++;
        return position;
    };

    jsonInterprete.diagram.forEach((packageData) => {
        arrayOrEmpty(packageData && packageData.class).forEach((classData) => {
            if (!classData || !classData.className) {
                return;
            }

            const position = nextPosition();
            const className = (symbolForVisibility(classData.visibility) + " " + classData.className).trim();
            const isInterface = String(classData.modifiers || "").toLowerCase() === "interface";
            const classElement = isInterface ? createInterface({name: className, x: position.x, y: position.y}) :
                createClass({name: className, x: position.x, y: position.y});

            elementsClass.push({element: classElement});
            classCount++;

            arrayOrEmpty(classData.attributes).forEach((attributeData) => {
                if (!attributeData || attributeData.name === undefined || String(attributeData.name).trim() === "") {
                    return;
                }
                const attributeName = String(attributeData.name).trim();
                const attributeType = attributeData.type || "Object";
                const visibility = symbolForVisibility(attributeData.visibility);
                const renderedType = attributeType === "fk" || attributeType === "enumeration" ? "fk" : attributeType;
                classElement.addAttribute((visibility + " " + attributeName + ":" + renderedType).trim());
            });

            arrayOrEmpty(classData.methods).forEach((methodData) => {
                if (!methodData || !methodData.name) {
                    return;
                }
                const returnType = methodData.type || methodData.returnType || "void";
                classElement.addOperation((symbolForVisibility(methodData.visibility) + " " + methodData.name +
                    "(" + getParamethers(arrayOrEmpty(methodData.parameters)) + "):" + returnType).trim());
            });

            arrayOrEmpty(classData.constructors).forEach((constructorData) => {
                if (!constructorData) {
                    return;
                }
                classElement.addOperation((symbolForVisibility(constructorData.visibility) + " new " + classData.className +
                    "(" + getParamethers(arrayOrEmpty(constructorData.parameters)) + ")").trim());
            });
        });

        arrayOrEmpty(packageData && packageData.enums).forEach((enumData) => {
            if (!enumData || !enumData.name) {
                return;
            }
            const position = nextPosition();
            const enumElement = createEnum({name: enumData.name, x: position.x, y: position.y});
            elementsClass.push({element: enumElement, name: enumElement.getName()});
            enumCount++;
            arrayOrEmpty(enumData.elements).forEach((element) => enumElement.addAttribute(element));
        });
    });

    const relationships = arrayOrEmpty(jsonInterprete.relationships).map((relationship) => {
        if (!relationship) {
            return relationship;
        }
        const normalized = Object.assign({}, relationship);
        const relationshipType = String(normalized.typeRelatioship || "association").toLowerCase();
        normalized.typeRelatioship = relationsGlobal[relationshipType] ? relationshipType : "association";
        normalized.cardinalidate = normalized.cardinalidate && String(normalized.cardinalidate).includes("..") ?
            normalized.cardinalidate : "u..u";
        normalized.value = normalized.value || "";
        return normalized;
    });

    relationsClass(relationships);
    applyClassDiagramLayout("orthogonal");
    diagramClass.interaction(true);

    return {
        classes: classCount + enumCount,
        relationships: Array.isArray(diagramClass._relations) ? diagramClass._relations.length : relationships.length
    };
}

function updateClassDiagram(jsonInterprete, action) {
    try {
        let ac = angular.element($('[ng-controller="workAreaController"]')).scope();
        ac.showMessageOpenAi = true;
        if (elementsClass.length > 0) {
            for (ielement = 0; ielement < elementsClass.length; ielement++) {
                deleteElement(elementsClass[ielement].element);
            }
            elementsClass.length = 0;
        }

        // verificar y eliminar relaciones relaciones redundantes
        let newRelations = deleteRelationsRedundant(jsonInterprete.relationships);
        jsonInterprete.relationships.length = 0;
        jsonInterprete.relationships = Object.assign([], newRelations);

        for (let ipackage = 0; ipackage < jsonInterprete.diagram.length; ipackage++) {
            package = jsonInterprete.diagram[ipackage];
            //let newpackage = cretaePackcage({name: package.packName, x: 50, y: 70});
            //elementsClass.push({"element": newpackage});
            for (let iclass = 0; iclass < package.class.length; iclass++) {
                clas = package.class[iclass];
                ac.data_type.push(clas.className);
                if (clas.modifiers === "interface") {
                    newclass = createInterface({
                        name: visibilityGlobal[clas.visibility] + " " + clas.className,
                        x: 0,
                        y: 0
                    });
                } else {
                    newclass = createClass({
                        name: visibilityGlobal[clas.visibility] + " " + clas.className,
                        x: 0,
                        y: 0
                    });
                }
                //newpackage.addChild(newclass);
                elementsClass.push({"element": newclass});
                for (let iattributes = 0; iattributes < clas.attributes.length; iattributes++) {
                    attributes = clas.attributes[iattributes];
                    if (attributes.name.toString().trim() !== "") {
                        if (attributes.type === "fk" || attributes.type === "enumeration")
                            newclass.addAttribute(visibilityGlobal[attributes.visibility] + " " + attributes.name.toString().trim() + " fk");
                        else
                            newclass.addAttribute(visibilityGlobal[attributes.visibility] + " " + attributes.name + ":" + attributes.type);
                    }

                }
                for (let imethods = 0; imethods < clas.methods.length; imethods++) {
                    methods = clas.methods[imethods];
                    newclass.addOperation(visibilityGlobal[methods.visibility] + " " + methods.name + "(" + getParamethers(methods.parameters) + "):" + methods.type);
                }

                for (let imethods = 0; imethods < clas.constructors.length; imethods++) {
                    constructor = clas.constructors[imethods];
                    newclass.addOperation(visibilityGlobal[constructor.visibility] + " new " + clas.className + "(" + getParamethers(constructor.parameters) + ")");
                }
            }

            for (let ienum = 0; ienum < package.enums.length; ienum++) {
                enumsx = package.enums[ienum];
                newenum = createEnum({
                    name: enumsx.name,
                    x: 0,
                    y: 0
                });
                elementsClass.push({"element": newenum, "name": newenum.getName()});
                for (let iattributes = 0; iattributes < enumsx.elements.length; iattributes++) {
                    elementsE = enumsx.elements[iattributes];
                    newenum.addAttribute(elementsE);
                }
            }
        }
        relationsClass(jsonInterprete.relationships);
        applyClassDiagramLayout("orthogonal");
        alertAll({"status": 2, "information": "Class diagram successfully updated."});
        //ac.showMessageOpenAi = false;
    } catch (ErrorMessage) {
        //consolo.log(ErrorMessage);
        alertAll({
            "status": 4,
            "information": "[updateClassDiagram]: " + ErrorMessage.message
        });
    }
}

/**
 * Deterministic hierarchical layout for the generated structural model.
 * "straight" keeps two-point relations. "orthogonal" assigns independent
 * routing channels and bend points so relations do not cross class boxes.
 */
function applyClassDiagramLayout(mode) {
    mode = mode === "straight" ? "straight" : "orthogonal";
    if (!diagramClass || !Array.isArray(diagramClass._nodes) || !diagramClass._nodes.length) {
        return {mode: mode, nodes: 0, relations: 0};
    }

    const nodes = diagramClass._nodes.filter(node => node && node._type !== "UMLPackage");
    if (!nodes.length) {
        return {mode: mode, nodes: 0, relations: 0};
    }
    const relations = (diagramClass._relations || []).filter(relation => relation && relation._elemA && relation._elemB);
    const nodeIndex = new Map(nodes.map((node, index) => [node, index]));
    const adjacency = nodes.map(() => []);
    const reverseAdjacency = nodes.map(() => []);

    relations.forEach(relation => {
        const from = nodeIndex.get(relation._elemA);
        const to = nodeIndex.get(relation._elemB);
        if (from === undefined || to === undefined || from === to) return;
        if (adjacency[from].indexOf(to) === -1) adjacency[from].push(to);
        if (reverseAdjacency[to].indexOf(from) === -1) reverseAdjacency[to].push(from);
    });

    // Collapse cycles first so inheritance/association loops do not destroy
    // the hierarchy. The condensed graph is always acyclic.
    let tarjanIndex = 0;
    const indexByNode = new Array(nodes.length).fill(-1);
    const lowLink = new Array(nodes.length).fill(0);
    const stack = [];
    const onStack = new Array(nodes.length).fill(false);
    const components = [];
    const strongConnect = vertex => {
        indexByNode[vertex] = tarjanIndex;
        lowLink[vertex] = tarjanIndex++;
        stack.push(vertex);
        onStack[vertex] = true;
        adjacency[vertex].forEach(next => {
            if (indexByNode[next] === -1) {
                strongConnect(next);
                lowLink[vertex] = Math.min(lowLink[vertex], lowLink[next]);
            } else if (onStack[next]) {
                lowLink[vertex] = Math.min(lowLink[vertex], indexByNode[next]);
            }
        });
        if (lowLink[vertex] === indexByNode[vertex]) {
            const component = [];
            let current;
            do {
                current = stack.pop();
                onStack[current] = false;
                component.push(current);
            } while (current !== vertex);
            components.push(component);
        }
    };
    nodes.forEach((node, index) => {
        if (indexByNode[index] === -1) strongConnect(index);
    });

    const componentOf = new Array(nodes.length);
    components.forEach((component, componentIndex) => component.forEach(index => componentOf[index] = componentIndex));
    const componentAdjacency = components.map(() => []);
    const componentIndegree = components.map(() => 0);
    adjacency.forEach((targets, from) => targets.forEach(to => {
        const componentFrom = componentOf[from];
        const componentTo = componentOf[to];
        if (componentFrom !== componentTo && componentAdjacency[componentFrom].indexOf(componentTo) === -1) {
            componentAdjacency[componentFrom].push(componentTo);
            componentIndegree[componentTo]++;
        }
    }));

    const componentRank = components.map(() => 0);
    const componentQueue = componentIndegree.map((degree, index) => degree === 0 ? index : -1).filter(index => index >= 0);
    while (componentQueue.length) {
        const component = componentQueue.shift();
        componentAdjacency[component].forEach(next => {
            componentRank[next] = Math.max(componentRank[next], componentRank[component] + 1);
            componentIndegree[next]--;
            if (componentIndegree[next] === 0) componentQueue.push(next);
        });
    }

    const levelOf = nodes.map((node, index) => componentRank[componentOf[index]]);
    const isolatedIndexes = nodes.map((node, index) => index).filter(index =>
        adjacency[index].length === 0 && reverseAdjacency[index].length === 0);
    const isolatedSet = new Set(isolatedIndexes);
    const connectedIndexes = nodes.map((node, index) => index).filter(index => !isolatedSet.has(index));
    const maxLevel = connectedIndexes.length
        ? Math.max.apply(null, connectedIndexes.map(index => levelOf[index]))
        : -1;
    const levels = Array.from({length: maxLevel + 1}, () => []);
    connectedIndexes.forEach(index => levels[levelOf[index]].push(index));
    const nodeName = index => String(nodes[index].getName ? nodes[index].getName() : index).toLowerCase();
    levels.forEach(level => level.sort((a, b) => nodeName(a).localeCompare(nodeName(b))));
    isolatedIndexes.sort((a, b) => nodeName(a).localeCompare(nodeName(b)));

    // Barycentric sweeps reduce crossings before coordinates are assigned.
    const sortLevel = (levelIndex, neighborList) => {
        const previousPositions = new Map();
        levels.forEach(level => level.forEach((node, index) => previousPositions.set(node, index)));
        levels[levelIndex].sort((a, b) => {
            const average = node => {
                const neighbors = neighborList[node];
                if (!neighbors.length) return previousPositions.get(node) || 0;
                return neighbors.reduce((sum, neighbor) => sum + (previousPositions.get(neighbor) || 0), 0) / neighbors.length;
            };
            return average(a) - average(b) || nodeName(a).localeCompare(nodeName(b));
        });
    };
    for (let sweep = 0; sweep < 3; sweep++) {
        for (let level = 1; level <= maxLevel; level++) sortLevel(level, reverseAdjacency);
        for (let level = maxLevel - 1; level >= 0; level--) sortLevel(level, adjacency);
    }

    // A logical hierarchy can be very wide. Split it into compact visual rows
    // and reserve the final rows exclusively for classes without relations.
    const maximumNodesPerRow = 4;
    const visualRows = [];
    levels.forEach((level, logicalLevel) => {
        for (let start = 0; start < level.length; start += maximumNodesPerRow) {
            visualRows.push({nodes: level.slice(start, start + maximumNodesPerRow), logicalLevel: logicalLevel, isolated: false});
        }
    });
    for (let start = 0; start < isolatedIndexes.length; start += maximumNodesPerRow) {
        visualRows.push({nodes: isolatedIndexes.slice(start, start + maximumNodesPerRow), logicalLevel: null, isolated: true});
    }
    const rowOf = new Array(nodes.length).fill(-1);
    visualRows.forEach((row, rowIndex) => row.nodes.forEach(index => rowOf[index] = rowIndex));

    const adjacentRelationsByLevel = {};
    const sameLevelRelations = {};
    let externalRouteCount = 0;
    relations.forEach(relation => {
        const from = nodeIndex.get(relation._elemA);
        const to = nodeIndex.get(relation._elemB);
        if (from === undefined || to === undefined) return;
        const fromLevel = rowOf[from];
        const toLevel = rowOf[to];
        if (toLevel === fromLevel + 1) {
            (adjacentRelationsByLevel[fromLevel] = adjacentRelationsByLevel[fromLevel] || []).push(relation);
        } else if (toLevel === fromLevel) {
            (sameLevelRelations[fromLevel] = sameLevelRelations[fromLevel] || []).push(relation);
        } else {
            externalRouteCount++;
        }
    });

    const horizontalGap = mode === "orthogonal" ? 92 : 76;
    const externalMargin = mode === "orthogonal" ? 75 + Math.ceil(externalRouteCount / 2) * 18 : 55;
    const topLaneCount = Math.max(0, ...Object.keys(sameLevelRelations).map(key => sameLevelRelations[key].length));
    const topMargin = mode === "orthogonal" ? 65 + topLaneCount * 14 : 55;
    const levelHeights = visualRows.map(row => Math.max(90, ...row.nodes.map(index => nodes[index]._height || 100)));
    const levelWidths = visualRows.map(row => row.nodes.reduce((width, index, position) =>
        width + (nodes[index]._width || 150) + (position ? horizontalGap : 0), 0));
    const widestLevel = Math.max.apply(null, levelWidths);
    const levelTops = [];
    let nextTop = topMargin;
    visualRows.forEach((row, levelIndex) => {
        if (row.isolated && levelIndex > 0 && !visualRows[levelIndex - 1].isolated) nextTop += 48;
        levelTops[levelIndex] = nextTop;
        const channelCount = (adjacentRelationsByLevel[levelIndex] || []).length;
        const verticalGap = row.isolated ? 48 : (mode === "orthogonal" ? Math.max(105, 58 + channelCount * 14) : 115);
        nextTop += levelHeights[levelIndex] + verticalGap;
    });

    visualRows.forEach((row, levelIndex) => {
        let x = externalMargin + (widestLevel - levelWidths[levelIndex]) / 2;
        row.nodes.forEach(index => {
            const node = nodes[index];
            // position() moves the node and all of its text/components.
            // setPosition() only changes the geometric box coordinates.
            node.position(Math.round(x), Math.round(levelTops[levelIndex]));
            x += (node._width || 150) + horizontalGap;
        });
    });

    const incidence = new Map(nodes.map(node => [node, []]));
    relations.forEach(relation => {
        if (incidence.has(relation._elemA)) incidence.get(relation._elemA).push({relation: relation, other: relation._elemB});
        if (incidence.has(relation._elemB)) incidence.get(relation._elemB).push({relation: relation, other: relation._elemA});
    });
    const portOffset = (node, relation) => {
        const connected = incidence.get(node) || [];
        connected.sort((a, b) => (a.other._x || 0) - (b.other._x || 0));
        const index = Math.max(0, connected.findIndex(item => item.relation === relation));
        const spacing = Math.min(15, Math.max(7, (node._width || 120) / Math.max(connected.length + 1, 2)));
        return (index - (connected.length - 1) / 2) * spacing;
    };

    const setRelationPoints = (relation, coordinates) => {
        relation._points = coordinates.map(point => new Point(Math.round(point.x), Math.round(point.y)));
        relation._calculateLineEnds();
        relation._updateComponents();
    };

    if (mode === "straight") {
        relations.forEach(relation => {
            relation._points = [new Point(), new Point()];
            relation._calculateLineEnds();
            relation._updateComponents();
        });
    } else {
        const graphLeft = Math.min.apply(null, nodes.map(node => node._x));
        const graphRight = Math.max.apply(null, nodes.map(node => node._x + node._width));
        const adjacentLaneIndex = {};
        const sameLaneIndex = {};
        let leftExternalLane = 0;
        let rightExternalLane = 0;

        relations.forEach((relation, relationIndex) => {
            const source = relation._elemA;
            const target = relation._elemB;
            const sourceIndex = nodeIndex.get(source);
            const targetIndex = nodeIndex.get(target);
            if (sourceIndex === undefined || targetIndex === undefined) return;
            if (source === target) {
                relation._points = [new Point(), new Point()];
                relation._calculateLineEnds();
                relation._updateComponents();
                return;
            }
            const sourceLevel = rowOf[sourceIndex];
            const targetLevel = rowOf[targetIndex];
            const sourceX = source._x + source._width / 2 + portOffset(source, relation);
            const targetX = target._x + target._width / 2 + portOffset(target, relation);

            if (targetLevel === sourceLevel + 1) {
                const lane = adjacentLaneIndex[sourceLevel] || 0;
                adjacentLaneIndex[sourceLevel] = lane + 1;
                const sourceBottom = source._y + source._height;
                const targetTop = target._y;
                const count = (adjacentRelationsByLevel[sourceLevel] || []).length;
                const usableGap = Math.max(30, targetTop - sourceBottom - 40);
                const laneY = sourceBottom + 20 + usableGap * ((lane + 1) / (count + 1));
                setRelationPoints(relation, [
                    {x: sourceX, y: sourceBottom}, {x: sourceX, y: laneY},
                    {x: targetX, y: laneY}, {x: targetX, y: targetTop}
                ]);
            } else if (targetLevel === sourceLevel) {
                const lane = sameLaneIndex[sourceLevel] || 0;
                sameLaneIndex[sourceLevel] = lane + 1;
                const laneY = Math.max(18, levelTops[sourceLevel] - 28 - lane * 14);
                setRelationPoints(relation, [
                    {x: sourceX, y: source._y}, {x: sourceX, y: laneY},
                    {x: targetX, y: laneY}, {x: targetX, y: target._y}
                ]);
            } else {
                const routeRight = relationIndex % 2 === 0;
                const laneNumber = routeRight ? rightExternalLane++ : leftExternalLane++;
                const routeX = routeRight ? graphRight + 38 + laneNumber * 18 : graphLeft - 38 - laneNumber * 18;
                const movingDown = targetLevel > sourceLevel;
                const sourceY = movingDown ? source._y + source._height : source._y;
                const targetY = movingDown ? target._y : target._y + target._height;
                const sourceChannelY = sourceY + (movingDown ? 24 : -24);
                const targetChannelY = targetY + (movingDown ? -24 : 24);
                setRelationPoints(relation, [
                    {x: sourceX, y: sourceY}, {x: sourceX, y: sourceChannelY},
                    {x: routeX, y: sourceChannelY}, {x: routeX, y: targetChannelY},
                    {x: targetX, y: targetChannelY}, {x: targetX, y: targetY}
                ]);
            }
        });
    }

    if (typeof diagramClass.setZoom === "function") diagramClass.setZoom(1);
    diagramClass.draw();
    return {
        mode: mode,
        nodes: nodes.length,
        relations: relations.length,
        levels: visualRows.length,
        logicalLevels: levels.length,
        isolated: isolatedIndexes.length,
        maximumNodesPerRow: maximumNodesPerRow
    };
}

//funcion para verificar valores unicos
function onlyUnique(value, index, self) {
    return self.indexOf(value) === index;
}

function deleteRelationsRedundant(relations) {
    if (relations.length > 0) {
        let newRelations = [];
        let relationsSuccess = [];
        for (let positionRelation = 0; positionRelation < relations.length; positionRelation++) {
            let rel_origi = relations[positionRelation];

            if (rel_origi !== undefined && rel_origi !== null) {

                let from = rel_origi.from;
                let to = rel_origi.to;
                for (let positionRelationAux = 0; positionRelationAux < relations.length; positionRelationAux++) {
                    let rela = relations[positionRelationAux];

                    if (rela !== undefined && rela !== null) {

                        let fromx = rela.from;
                        let tox = rela.to;

                        if (positionRelationAux !== positionRelation) {
                            if ((from === fromx && to === tox) || (from === tox && to === fromx)) {
                                relationsSuccess.push(positionRelationAux);
                            }
                        }
                    }
                }
            }
        }
        //consolo.log("cantidad de relaciones redundantes: ", relationsSuccess.length);
        let unique_data = [];
        unique_data = relationsSuccess.filter(onlyUnique);
        // agregar las relaciones que no se repitieron
        for (let newRelation = 0; newRelation < relations.length; newRelation++) {
            let relation = relations[newRelation];
            let flag = true;
            for (let positionRedudant = 0; positionRedudant < unique_data.length; positionRedudant++) {
                if (newRelation === unique_data[positionRedudant]) {
                    flag = false;
                }
            }
            if (flag) {
                newRelations.push(relation);
            }
        }
        return newRelations;
    }
}

function relationsClass(relations) {
    for (let irelation = 0; irelation < relations.length; irelation++) {
        if (relations[irelation] !== null) {
            if (relations[irelation] !== undefined) {
                if (relations[irelation].from !== undefined && relations[irelation].to !== undefined) {
                    let from = getFromToRelation(relations[irelation].from);
                    let to = getFromToRelation(relations[irelation].to);

                    if (from === undefined || to === undefined) {
                        //consolo.log("NO EXISTEN OBJETOS PARA REALIZAR ESTE TIPO DE RELACION => " + relations[irelation].typeRelatioship);
                    } else {

                        if (relations[irelation].cardinalidate === undefined) {
                            relations[irelation]["cardinalidate"] = "u..u";
                        }

                        if (relations[irelation].from_fk === undefined) {
                            relations[irelation]["from_fk"] = relations[irelation].to.toString().toLowerCase() + ":" + relations[irelation].to;
                        }

                        if (relations[irelation].simbol === undefined) {
                            relations[irelation]["simbol"] = "";
                        }

                        if (relations[irelation].to_fk === undefined) {
                            relations[irelation]["to_fk"] = relations[irelation].from.toString().toLowerCase() + ":" + relations[irelation].from;
                        }

                        if (relations[irelation].value === undefined) {
                            relations[irelation]["value"] = relations[irelation].typeRelatioship.toLowerCase();
                        }

                        let relation = createRelationClass({
                            type: relationsGlobal[relations[irelation].typeRelatioship.toLowerCase()],
                            a: from,
                            b: to,
                            card_A: relations[irelation].cardinalidate.split("..")[0],
                            card_B: relations[irelation].cardinalidate.split("..")[1],
                            value: relations[irelation].value,
                            typeRelatioship: relations[irelation].typeRelatioship.toLowerCase()
                        });
                        elementsClass.push({"element": relation});
                    }
                }
            }
        }
    }
}

function getFromToRelation(nameClass) {
    for (let i = 0; i < elementsClass.length; i++) {
        let getNameClass = elementsClass[i].element.getName();
        if (getNameClass.includes(" ")) {
            if (getNameClass.split(" ")[1] === nameClass) {
                return elementsClass[i].element;
            }
        } else {
            if (getNameClass === nameClass) {
                return elementsClass[i].element;
            }
        }

    }
}

function getParamethers(parameters) {
    let rparam = "";
    if (parameters.length > 0) {
        for (let i = 0; i < parameters.length; i++) {
            if (i === parameters.length - 1) {
                rparam += parameters[i].name + ":" + parameters[i].type;
            } else {
                rparam += parameters[i].name + ":" + parameters[i].type + ",";
            }
        }
    }
    return rparam;
}

function getParamethersReplace(parameters) {
    let rparam = "";
    if (parameters.length > 0) {
        for (let i = 0; i < parameters.length; i++) {
            if (i === parameters.length - 1) {
                rparam += parameters[i].name + ":" + parameters[i].type.replace("string:", "");
            } else {
                rparam += parameters[i].name + ":" + parameters[i].type.replace("string:", "") + ",";
            }
        }
    }
    return rparam;
}
