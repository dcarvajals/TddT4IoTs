/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function updateSecuenceDiagram(diagram, jsonData) {
    // Each use case owns its sequence diagram. Keeping the generated elements
    // on the diagram avoids one sequence update deleting another diagram.
    let elements = diagram._tddSequenceElements || [];

    if (elements.length > 0) {
        for (let ielement = 0; ielement < elements.length; ielement++) {
            deleteElementSecuence(elements[ielement].element, diagram);
        }
        elements.length = 0;
    }
    xlifeline = -130;
    ysendmessage = 50;
    diagram._tddSequenceElements = elements;
    
    jsonData = jsonData || {};
    let flujealternative = Array.isArray(jsonData.table_normal_flow) ? jsonData.table_normal_flow : [];
    const registeredLifelines = new Set();
    if (flujealternative.length > 0) {
        for (let ifluje = 0; ifluje < flujealternative.length; ifluje++) {
            let object = flujealternative[ifluje];
            //crear los objetos
            if (!object || !object.from || !object.to || !object.message) continue;
            let searchFrom = searchLife(diagram, object.from);
            let searchto = searchLife(diagram, object.to);

            let from = searchFrom === undefined ? newLifeLine({diagram: diagram, name: object.from}) : searchFrom;
            let to = searchto === undefined ? newLifeLine({diagram: diagram, name: object.to}) : searchto;

            if (!registeredLifelines.has(from)) {
                elements.push({element: from});
                registeredLifelines.add(from);
            }
            if (!registeredLifelines.has(to)) {
                elements.push({element: to});
                registeredLifelines.add(to);
            }

            let message = {};
            let response = {};
            //mensajes asincronos y sincronas
            if (object.message !== "") {
                if (object.message_type === "async_message") { //si es asincrono
                    message = asynchronousMessage({diagram: diagram, name: object.message, a: from, b: to});
                } else {
                    message = synchronousMessage({diagram: diagram, name: object.message, a: from, b: to});
                }
                elements.push({element: message});
            }

            //respuestas asincronas y sincronas
            if (object.response !== "") {
                if (object.response_type === "async_response" && message.getElementB) { // para todos los casos
                    response = asynchronousSynchronousResponse({diagram: diagram, name: object.response, a: message.getElementB(), b: from});
                } else {
                    response = asynchronousSynchronousResponse({diagram: diagram, name: object.response, a: to, b: from});
                }
                elements.push({element: response});
            }
        }
    }
    if (typeof diagram.setZoom === "function") diagram.setZoom(1);
    diagram.draw();
}

function searchLife(diagram, name) {
    let nodes = diagram._nodes;
    if (nodes.length > 0) {
        for (let inode = 0; inode < nodes.length; inode++) {
            let node = nodes[inode];
            if (node._type === "UMLLifeline") {
                let namenode = node.getName();
                if (name === namenode) {
                    return node;
                }
            }
        }
    }
}
