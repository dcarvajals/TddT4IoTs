/* global rutasStorage, angular, store, urlWebServicies, myDiagram, go, swal, clearDiagram, codeGeneral */

/**
 * initDiagram(responseA.data);
 initPalette();
 initContextMenu();
 * */

app = angular.module('app', []);
app.controller('controllerWorkIoT', function ($scope, $http) {

    $scope.DatoUsuario = {};
    $scope.rutaImgUser = location.origin + rutasStorage.imguser;

    $scope.colors = ["Black", "Red", "Orange", "Yellow", "Green", "Turquoise", "Blue", "Purple", "Pink", "Brown", "Gray", "White"];
    $scope.colorsHexa = ["#3C4042", "#EC2222", "#F78300", "#FFDF01", "#40B942", "#71CEDC", "#009ED9", "#7F3B9A", "#D9288C", "#AA7B4C", "#999EA1", "#FFFFFF"];
    $scope.arrayColors = [];
    var id_project = "";
    $scope.dataModel = "";
    $scope.flag_block_code = undefined;
    var loadBlocky = false;
    $scope.dataProject = {};

    $scope.arrayParameters = [];
    $scope.jsonCode = [];

    //variables para manipular el codigo
    $scope.jsonVariables = [];
    $scope.jsonValueDigitalAnalog = [];
    $scope.jsonValueData = [];
    $scope.jsonValueDigitalRead = [];

    // variables para el codigo agregado parametrizado por componente
    $scope.codeParameter = {};
    
    $scope.cofigParam = 0;
    $scope.ip_portFrom_name = "";
    $scope.legendValidation = "";
    $scope.typeFromPorts = [{id: 0,value: "", info: ""}, {id: 1, value: "", info:""}];
    $scope.fromNode = {};
    $scope.toNode = {};
    $scope.cable = {};
    $scope.comunicationMethods = [];
    $scope.modeConnection = {};

    $(document).ready(() => {
        if (window.location.search !== "") {
            $scope.DatoUsuario = getDataSession();
            let urlParams = new URLSearchParams(window.location.search);
            id_project = urlParams.get('identifiquer');

            $scope.loadDataProject(id_project);

            initDiagramProject();
            initPalette();
            initContextMenu();
            // cargar la plantilla del codigo fuente arduino
            $scope.$apply(() => {
                let moduleParam = $("#moduleParam");
                moduleParam.removeClass("animate__slideOutUp");
                moduleParam.addClass("animate__slideInDown");
                moduleParam.show();
                loadCodeGeneral();
                moduleParam.hide();
            });

            $scope.getComponentes();
            $scope.loadColors();
            $scope.loadProject();
            $scope.loadComunicationMethod();
        }
    });

    $scope.loadDataProject = (idProject) => {
        let dataLoadDataProject = {
            "typeSelect": "4",
            "idProject": idProject
        };
        wsLoadDataProject(dataLoadDataProject);
    };

    let wsLoadDataProject = (api_param) => {
        let dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/listShareProject',
                data: JSON.stringify({
                    "user_token": dataUser.user_token,
                    ...api_param
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    $scope.$apply(() => {
                        $scope.dataProject = data.data[0];
                    });
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
    };

    //funcion para cargar los colores
    $scope.loadColors = () => {
        for (let i = 0; i < $scope.colors.length; i++) {
            $scope.arrayColors.push({
                "name": $scope.colors[i],
                "hexadecimal": $scope.colorsHexa[i]
            });
        }
    };

    $scope.changeColorCable = (color) => {
        myDiagram.startTransaction("change color");
        myDiagram.selection.each(function (node) {
            if (node instanceof go.Link) {  // ignore any selected Links and simple Parts
                // Examine and modify the data, not the Node directly.
                var data = node.data;
                // Call setDataProperty to support undo/redo as well as
                // automatically evaluating any relevant bindings.
                myDiagram.model.setDataProperty(data, "color", color);
            }
        });
        myDiagram.commitTransaction("change color");
    };


    $scope.getComponentes = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'components/getComponents',
                data: JSON.stringify({"user_token": dataUser.user_token, "type": "MY ALL ACTIVE"}),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    $scope.$apply(function () {
                        $scope.arrayComponentes = data;
                    });
                    loadComponents(data);
                },
                error: function (objXMLHttpRequest) {
                    console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
    };

    $scope.logOut = () => {
        cerrarSesion();
    };

    /**
     * Funcion para obtener los datos del componente al que le dimos click derecho
     * @param obj objeto que contiene lo datos del componente
     * @param objLink objeto para conocer las relaciones que tinene el componente
     * */
    $scope.getParametersBD = (obj, objLink) => {
        //loadCodeGeneral();
        //verificar que tipo de componente es al que se le dio click derecho
        let type_component = obj.type;
        //abrir la venta donde se visualizara tanto los bloques de codigo o las secciones de codigo de caca componente
        let moduleParam = $("#moduleParam");
        moduleParam.removeClass("animate__slideOutUp");
        moduleParam.addClass("animate__slideInDown");
        moduleParam.show();

        console.log("tipo de componente => ", type_component);
        console.log("code", obj);
        console.log("conexiones: ", objLink);

        $scope.arrayParameters = [];

        let arrayConecctions = objLink;
        let arrayPortsComponentSelected = obj.ports;

        for (let posicionPuertoConexion = 0; posicionPuertoConexion < arrayConecctions.length; posicionPuertoConexion++) {
            let puertoConexion = arrayConecctions[posicionPuertoConexion];
            for (let posicionPuertoComponente = 0; posicionPuertoComponente < arrayPortsComponentSelected.length; posicionPuertoComponente++) {
                let puertoComponente = arrayPortsComponentSelected[posicionPuertoComponente];

                let idComponenteFrom = puertoConexion.from;
                let idComponenteTo = puertoConexion.to;

                if (puertoComponente.key === idComponenteFrom || puertoComponente.key === idComponenteTo) {
                    $scope.$apply(function () {
                        $scope.arrayParameters.push(puertoComponente);
                        posicionPuertoComponente = arrayPortsComponentSelected.length;
                    });
                }
            }

        }

        for (var i = 0; i < $scope.arrayParameters.length; i++) {
            if ($scope.arrayParameters[i].digital && !$scope.arrayParameters[i].analog) {
                document.getElementById("btnDigitalAnalog" + $scope.arrayParameters[i].key).style.display = "block";
                document.getElementById("btnData" + $scope.arrayParameters[i].key).style.display = "none";
                document.getElementById("portDigital" + $scope.arrayParameters[i].key).style.display = "block";
                document.getElementById("statDigital" + $scope.arrayParameters[i].key).style.display = "block";
                document.getElementById("portAnalog" + $scope.arrayParameters[i].key).style.display = "none";
            } else if ($scope.arrayParameters[i].analog && !$scope.arrayParameters[i].digital) {
                document.getElementById("btnDigitalAnalog" + $scope.arrayParameters[i].key).style.display = "block";
                document.getElementById("btnData" + $scope.arrayParameters[i].key).style.display = "none";
                document.getElementById("portDigital" + $scope.arrayParameters[i].key).style.display = "none";
                document.getElementById("statDigital" + $scope.arrayParameters[i].key).style.display = "none";
                document.getElementById("portAnalog" + $scope.arrayParameters[i].key).style.display = "block";
            } else if ($scope.arrayParameters[i].digital && $scope.arrayParameters[i].analog) {
                document.getElementById("btnDigitalAnalog" + $scope.arrayParameters[i].key).style.display = "block";
                document.getElementById("btnData" + $scope.arrayParameters[i].key).style.display = "none";
                document.getElementById("portDigital" + $scope.arrayParameters[i].key).style.display = "block";
                document.getElementById("portAnalog" + $scope.arrayParameters[i].key).style.display = "block";
                document.getElementById("statDigital" + $scope.arrayParameters[i].key).style.display = "block";
            }
        }
        for (var i = 0; i < $scope.arrayParameters.length; i++) {
            if ($scope.arrayParameters[i].data) {
                document.getElementById("btnDigitalAnalog" + $scope.arrayParameters[i].key).style.display = "none";
                document.getElementById("btnData" + $scope.arrayParameters[i].key).style.display = "block";
                document.getElementById("data" + $scope.arrayParameters[i].key).style.display = "block";
            } else {
                //document.getElementById("btnDigitalAnalog").style.display = "none";
                //document.getElementById("btnData").style.display = "none";
                document.getElementById("data" + $scope.arrayParameters[i].key).style.display = "none";
            }
        }

        if ($scope.jsonCode.length === 0) {
            //inicializar el json que contendra todo la estructura del codigo del sistema IOT
            $scope.jsonCode = [
                {
                    library: []
                },{
                    variables: []
                }, {
                    pinMode: []
                }, {
                    logic: []
                }, {
                    params: []
                },

                // VARIABLES PARA EL CODIGO DE COMUNICACION DE DATOS a partir de la posciion 5
                {
                    libraryCommnunication: []
                },
                {
                    variablesCommnunication: []
                },
                {
                    setupCommunication: []
                },
                {
                    loopCommunication: []
                },
                {
                    methodsCommnunication: []
                }];

        } else {
            updateCodeEditor();
        }
        $scope.$apply(function () {
            $scope.codeParameter["Libraries"] = b64_to_utf8(obj.code.Libraries);
            $scope.codeParameter["Variables"] = b64_to_utf8(obj.code.Variables);
            $scope.codeParameter["Loop"] = b64_to_utf8(obj.code.Loop);
            $scope.codeParameter["Setup"] = b64_to_utf8(obj.code.Setup);
            $scope.codeParameter["Methods"] = b64_to_utf8(obj.code.Methods);
        });

        console.log("PUERTOS CONECTADOS DEL COMPONENTE SELECCIONADO: ", $scope.arrayParameters);
        $("#sectionCode").show();
        $("#blockCode").hide();
    };

    /**
     * Funcion para cerrar el modal de los bloques de codigo
     * */
    $scope.closeBlockCode = function () {
        let moduleParam = $("#moduleParam");
        moduleParam.removeClass("animate__slideInDown");
        moduleParam.addClass("animate__slideOutUp");
        //moduleParam.hide();
    };

    $scope.saveDispositive = () => {
        let api_param = {
            "user_token": $scope.DatoUsuario.user_token,
            "dataJson": JSON.stringify([
                {
                    "diagramType": "ports",
                    "dataJson": JSON.parse(saveNewModel()),
                    "base64": ""
                }
            ]),
            "dataScript": codeGeneral.getValue(),
            "module": "EasyIoT",
            "idproj": id_project
        };
        console.log(saveNewModel());
        apiSaveDispositive(api_param);
    };

    apiSaveDispositive = (api_param) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/saveModuleFile',
                data: JSON.stringify({...api_param}),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
    };

    $scope.loadProject = () => {
        let api_param = {
            "user_token": $scope.DatoUsuario.user_token,
            "module": "EasyIoT",
            "idproj": id_project
        };
        apiLoadProject(api_param);
    };

    apiLoadProject = (api_param) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/loadModuleFile',
                data: JSON.stringify({...api_param}),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    data.data = JSON.stringify(data.data);
                    searchModel(data.data);
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
    };

    saveNewModel = () => {
        //console.log(myDiagram.model.toJson());
        var m = new go.GraphLinksModel();

        m.nodeIsGroupProperty = "_isg";
        m.nodeGroupKeyProperty = "_g";

        var modelActual = myDiagram.model;
        var ports = [];
        var components = []

        for (let x = 0; x < modelActual.nodeDataArray.length; x++) {

        }

        myDiagram.nodes.each((g) => {
            if (g instanceof go.Group) {
                if (g.data._isg && g.data._isg !== undefined) {
                    components.push(g.data);
                }
            }
        });

        myDiagram.nodes.each((g) => {
            if (g instanceof go.Node) {
                if (!g.data._isg && g.data._isg === undefined) {
                    ports.push(g.data);
                }
            }
        });

        var cont = 0;
        for (let i = 0; i < components.length; i++) {
            components[i].ports.length = 0;
            for (let j = 0; j < ports.length; j++) {
                if (components[i].key === ports[j]._g) {
                    cont++;
                    console.log(cont);
                    components[i].ports.push(ports[j]);
                } else {
                    cont = 0;
                }
            }
        }

        console.log(components);
        console.log(ports);

        m.addNodeDataCollection(components);
        m.addLinkDataCollection(myDiagram.model.linkDataArray);
        console.log(m.toJson());
        return m.toJson();
    };

    $scope.exportPng = () => {
        makeBlob();
    };
    
    // Metodo para guardar la configuracion inicial de un puerto que fue 
    // configurado como salida/entrada o digital/analogico
    $scope.changeConfigurationPort = (form) => {
        console.log($scope.fromNode);
        
        $scope.fromNode.digital = form.sc_portFrom_type.$viewValue === "digital";
        $scope.fromNode.analog = form.sc_portFrom_type.$viewValue === "analog";
        $scope.fromNode.input = form.sc_portFrom_type.$viewValue === "input";
        $scope.fromNode.output = form.sc_portFrom_type.$viewValue === "output";
        
        $scope.fromNode.digital_analog = false;
        $scope.fromNode.input_output = false;
        
        if(!$scope.toNode.digital && !$scope.toNode.analog && $scope.toNode.digital_analog) {
            // cambiar al estado que se ajuste al nodo de partida
            $scope.toNode.digital = form.sc_portFrom_type.$viewValue === "digital";
            $scope.toNode.analog = form.sc_portFrom_type.$viewValue === "analog";
            $scope.toNode.digital_analog = false;
        } else if (!$scope.toNode.input && !$scope.toNode.output && $scope.toNode.input_output) {
            $scope.toNode.input = !form.sc_portFrom_type.$viewValue === "input";
            $scope.toNode.output = !form.sc_portFrom_type.$viewValue === "output";
            $scope.toNode.input_output = false;
        }
        
        $("#modalValidationCable").modal("hide");
        alertAll({status:2, information: "Successful port configuration."});
        
        validaciones = validarConexionCables($scope.fromNode, $scope.toNode, $scope.cable);
        if(validaciones.showAlert) {
            alertAll(validaciones);
            if(validaciones.status !== 1){
                myDiagram.model.removeLinkData($scope.cable.data);
            }
        }
        
        console.log($scope.fromNode);
    };

    // ******************** //
    // GENERACION DE CÓDIGO //
    // ******************** //

    //agrear los puertos que estan siendo usados por el arduino
    $scope.addPortsDigitalAnalog = function (position) {
        //alert("digital o analogico");

        //alert($("#selectPD" + $scope.arrayParameters[position].name_port).val());

        if ($("#selectPD" + $scope.arrayParameters[position].key).val() !== "---" &&
                $("#selectSD" + $scope.arrayParameters[position].key).val() !== "---" &&
                $("#selectPA" + $scope.arrayParameters[position].key).val() !== "---") {
            errorTo({information: "Error when processing the data"});
            return;
        }

        if ($("#selectPD" + $scope.arrayParameters[position].key).val() !== "---" &&
                $("#selectSD" + $scope.arrayParameters[position].key).val() === "---" &&
                $("#selectPA" + $scope.arrayParameters[position].key).val() !== "---") {
            errorTo({information: "Error when processing the data"});
            return;
        }

        if ($("#selectPD" + $scope.arrayParameters[position].key).val() === "---" &&
                $("#selectSD" + $scope.arrayParameters[position].key).val() !== "---" &&
                $("#selectPA" + $scope.arrayParameters[position].key).val() !== "---") {
            errorTo({information: "Error when processing the data"});
            return;
        }

        if ($("#selectPD" + $scope.arrayParameters[position].key).val() === "---" &&
                $("#selectSD" + $scope.arrayParameters[position].key).val() === "---" &&
                $("#selectPA" + $scope.arrayParameters[position].key).val() === "---") {
            errorTo({information: "Error when processing the data"});
            return;
        }

        if ($("#selectPD" + $scope.arrayParameters[position].key).val() !== "---" &&
                $("#selectSD" + $scope.arrayParameters[position].key).val() === "---" &&
                $("#selectPA" + $scope.arrayParameters[position].key).val() === "---") {
            errorTo({information: "Error when processing the data"});
            return;
        }

        if ($("#selectPD" + $scope.arrayParameters[position].key).val() !== "---" &&
                $("#selectSD" + $scope.arrayParameters[position].key).val() === "---" &&
                $("#selectPA" + $scope.arrayParameters[position].key).val() !== "---") {
            errorTo({information: "Error when processing the data"});
            return;
        }

        if ($("#selectPD" + $scope.arrayParameters[position].key).val() === "---" &&
                $("#selectSD" + $scope.arrayParameters[position].key).val() !== "---" &&
                $("#selectPA" + $scope.arrayParameters[position].key).val() === "---") {
            errorTo({information: "Error when processing the data"});
            return;
        }

        //$scope.jsonValueDigitalRead.length = 0;

        if ($scope.arrayParameters[position].digital === true && $scope.arrayParameters[position].analog === false) {
            if ($("#selectPD" + $scope.arrayParameters[position].key).val() !== "---" &&
                    $("#selectSD" + $scope.arrayParameters[position].key).val() !== "---" &&
                    $("#selectPD" + $scope.arrayParameters[position].key).val() === "digitalWrite") {
                $scope.jsonCode[2].pinMode.push({
                    valueWriteRaad: $("#selectPD" + $scope.arrayParameters[position].key).val() + "(" + $scope.arrayParameters[position].name_port + ", " +
                            $("#selectSD" + $scope.arrayParameters[position].key).val() + ");",
                    pinModePort: "pinMode(" + $scope.arrayParameters[position].name_port + ",OUTPUT);",
                    namePort: $scope.arrayParameters[position].name_port,
                    out_inp: true
                });
            } else {
                $scope.jsonCode[2].pinMode.push({
                    valueWriteRaad: $("#selectPD" + $scope.arrayParameters[position].key).val() + "(" + $scope.arrayParameters[position].name_port + ")",
                    pinModePort: "pinMode(" + $scope.arrayParameters[position].name_port + ",INPUT);",
                    namePort: $scope.arrayParameters[position].name_port,
                    out_inp: false

                });
            }
        } else {
            if ($("#selectPD" + $scope.arrayParameters[position].key).val() === "---" &&
                    $("#selectSD" + $scope.arrayParameters[position].key).val() === "---" &&
                    $("#selectPA" + $scope.arrayParameters[position].key).val() !== "---" &&
                    $("#selectPA" + $scope.arrayParameters[position].key).val() === "analogWrite") {
                $scope.jsonCode[2].pinMode.push({
                    valueWriteRaad: $("#selectPA" + $scope.arrayParameters[position].key).val() + "(" + $scope.arrayParameters[position].name_port + ",255);",
                    pinModePort: "pinMode(" + $scope.arrayParameters[position].name_port + ",OUTPUT);",
                    namePort: $scope.arrayParameters[position].name_port,
                    out_inp: true
                });
            } else {

                if ($("#selectPD" + $scope.arrayParameters[position].key).val() !== "---" &&
                        $("#selectSD" + $scope.arrayParameters[position].key).val() !== "---" &&
                        $("#selectPA" + $scope.arrayParameters[position].key).val() === "---" &&
                        $("#selectPD" + $scope.arrayParameters[position].key).val() === "digitalWrite") {
                    $scope.jsonCode[2].pinMode.push({
                        valueWriteRaad: $("#selectPD" + $scope.arrayParameters[position].key).val() + "(" + $scope.arrayParameters[position].name_port + ", " +
                                $("#selectSD" + $scope.arrayParameters[position].key).val() + ");",
                        pinModePort: "pinMode(" + $scope.arrayParameters[position].name_port + ",OUTPUT);",
                        namePort: $scope.arrayParameters[position].name_port,
                        out_inp: true
                    });

                    console.log($scope.jsonCode[2].pinMode);
                    //updateShina($scope.jsonCode);
                    updateCodeEditor();
                    return;
                } else {
                    $scope.jsonCode[2].pinMode.push({
                        valueWriteRaad: $("#selectPA" + $scope.arrayParameters[position].key).val() + "(" + $scope.arrayParameters[position].name_port + ")",
                        pinModePort: "pinMode(" + $scope.arrayParameters[position].name_port + ",INPUT);",
                        namePort: $scope.arrayParameters[position].name_port,
                        out_inp: false
                    });
                    console.log($scope.jsonCode[2].pinMode);
                    //updateShina($scope.jsonCode);
                    updateCodeEditor();
                    return;
                }

                $scope.jsonCode[2].pinMode.push({
                    valueWriteRaad: $("#selectPA" + $scope.arrayParameters[position].key).val() + "(" + $scope.arrayParameters[position].name_port + ")",
                    pinModePort: "pinMode(" + $scope.arrayParameters[position].name_port + ",INPUT);",
                    namePort: $scope.arrayParameters[position].name_port,
                    out_inp: false
                });
            }
        }

        console.log($scope.jsonValueDigitalRead);
        console.log($scope.jsonCode[0].pinMode);
        //updateShina($scope.jsonCode);
        updateCodeEditor();
    };

    //agregar los value de los parametros de los componentes que no son puertos digitales ni analogicos si no que solo transfieren datos
    $scope.addValueData = function (position) {
        //alert("datito");
        $scope.jsonCode[4].params.push({
            namePort: $scope.arrayParameters[position].name_port,
            valueParam: $("#input" + $scope.arrayParameters[position].key).val(),
            idComponent: $scope.arrayParameters[position].idComponent
        });
        //updateShina($scope.jsonCode);
        updateCodeEditor();
    };

    // agregar todo lo relacion a los modos de conexion del componente iot de comunicacion
    $scope.addModeConection = function () {
        console.log('Selected method:', $scope.modeConnection);
        $scope.jsonCode[5].libraryCommnunication = [];
        $scope.jsonCode[6].variablesCommnunication = [];
        $scope.jsonCode[7].setupCommunication = [];
        $scope.jsonCode[8].loopCommunication = [];
        $scope.jsonCode[9].methodsCommnunication = [];
        $scope.jsonCode[5].libraryCommnunication.push($scope.modeConnection.code.valueLibraries);
        $scope.jsonCode[6].variablesCommnunication.push($scope.modeConnection.code.valueVariables);
        $scope.jsonCode[7].setupCommunication.push($scope.modeConnection.code.valueSetup);
        $scope.jsonCode[8].loopCommunication.push($scope.modeConnection.code.valueLoop);
        $scope.jsonCode[9].methodsCommnunication.push($scope.modeConnection.code.valueMethods);
        updateCodeEditor();
    }


    //funcion que actualiazara el codigo por cualqueir modificacion que se valla realizando
    function updateCodeEditor() {
        code = "";
        const fecha = new Date();
        codeGeneral.setValue(code);
        code += "/* Code generated by TDDT4IoTS \n" +
                " Date:  " + fecha.toDateString() + " */ \n ";

        for(let indexLibrary = 0; indexLibrary < $scope.jsonCode[5].libraryCommnunication.length; indexLibrary++) {
            code += $scope.jsonCode[5].libraryCommnunication[indexLibrary] + "\n";
        }

        //actualizamos los cambios realizados en el json de las variables

        for(let indexVariableCom = 0; indexVariableCom < $scope.jsonCode[6].variablesCommnunication.length; indexVariableCom++) {
            code += $scope.jsonCode[6].variablesCommnunication[indexVariableCom] + "\n";
        }

        for (var indexVariables = 0; indexVariables < $scope.jsonCode[1].variables.length; indexVariables++) {
            code += $scope.jsonCode[1].variables[indexVariables].type + " " + $scope.jsonCode[1].variables[indexVariables].var + ";\n";
        }
        code += "\n";
        code += "void setup () { \n";

        for(let indexSetupCom = 0; indexSetupCom < $scope.jsonCode[7].setupCommunication.length; indexSetupCom++) {
            code += $scope.jsonCode[7].setupCommunication[indexSetupCom] + "\n";
        }

        for (var indexVariables = 0; indexVariables < $scope.jsonCode[2].pinMode.length; indexVariables++) {
            code += "    " + $scope.jsonCode[2].pinMode[indexVariables].pinModePort + "\n";
        }

        for (var indexVariables = 0; indexVariables < $scope.jsonCode[1].variables.length; indexVariables++) {
            code += "    " + $scope.jsonCode[1].variables[indexVariables].var + " = " + $scope.jsonCode[1].variables[indexVariables].value + ";\n";
        }
        code += "}\n";
        code += "void loop() {\n";

        for(let indexLoopCom = 0; indexLoopCom < $scope.jsonCode[8].loopCommunication.length; indexLoopCom++) {
            code += $scope.jsonCode[8].loopCommunication[indexLoopCom] + "\n";
        }

        for (var indexVariables = 0; indexVariables < $scope.jsonCode[2].pinMode.length; indexVariables++) {
            if ($scope.jsonCode[2].pinMode[indexVariables].out_inp === true) {
                code += "    " + $scope.jsonCode[2].pinMode[indexVariables].valueWriteRaad + "\n";
            }
        }

        //code += analize($scope.jsonCode[2].logic, 1);

        code += "}";

        for(let indexMethodsCom = 0; indexMethodsCom < $scope.jsonCode[9].methodsCommnunication.length; indexMethodsCom++) {
            code += $scope.jsonCode[9].methodsCommnunication[indexMethodsCom] + "\n";
        }

        codeGeneral.setValue(code);

        console.log(code);
        //function para inicializar los tooltip de los componentes de boostrap
        $(function () {
            $('[data-toggle="tooltip"]').tooltip();
        });
    }

    // cargar los metodos de comunicacion
    $scope.loadComunicationMethod = () => {
        $http.get('resources/json/comunication.json').then(function(response) {
            // Asumiendo que el archivo JSON tiene una estructura como { "methods": [...] }
            $scope.communicationMethods = response.data;
            console.log('Métodos de comunicación cargados:', $scope.communicationMethods);
        }).catch(function(error) {
            console.error('Error al cargar los métodos de comunicación:', error);
        });
    };

    // descargar el codigo
    $scope.downloadCode = function() {
        // Create a Blob from the Arduino code
        var blob = new Blob([codeGeneral.getValue()], { type: 'text/plain;charset=utf-8' });
        var url = window.URL.createObjectURL(blob);

        // Create a temporary anchor element and trigger the download
        var a = document.createElement('a');
        a.href = url;
        a.download = 'arduino_code.ino'; // Set the file name
        document.body.appendChild(a); // Append the anchor to body
        a.click(); // Simulate click on the anchor
        document.body.removeChild(a); // Remove the anchor from body

        // Clean up the URL object
        window.URL.revokeObjectURL(url);
    };


});
