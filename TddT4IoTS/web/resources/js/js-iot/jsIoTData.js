/* global rutasStorage, angular, store, urlWebServicies, myDiagram, go, swal, clearDiagram */

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

    $(document).ready(() => {
        if (window.location.search !== "") {
            $scope.DatoUsuario = getDataSession();
            let urlParams = new URLSearchParams(window.location.search);
            id_project = urlParams.get('identifiquer');
            console.log(id_project);

            $scope.loadDataProject(id_project);

            initDiagramProject();
            initPalette();
            initContextMenu();

            $scope.getComponentes();
            $scope.loadColors();
            $scope.loadProject();
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
                    console.log(data);
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
        console.log($scope.arrayColors);
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
                    console.log(data);
                    $scope.$apply(function () {
                        $scope.arrayComponentes = data;
                    });
                    console.log(data);
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
        //verificar que tipo de componente es al que se le dio click derecho
        let type_component = obj.type;
        //abrir la venta donde se visualizara tanto los bloques de codigo o las secciones de codigo de caca componente
        let moduleParam = $("#moduleParam");
        moduleParam.removeClass("animate__slideOutUp");
        moduleParam.addClass("animate__slideInDown");
        moduleParam.show();
        console.log("tipo de componente => ", type_component);
        if (type_component === "embedded_system") { // seccion para cargar la seccion de los bloques generadores de cod
            $("#blockCode").show();
            $("#sectionCode").hide();
            //cargamos solo una vez los bloques de codigo
            if (!loadBlocky) {
                loadBlocky = true;
                $scope.$apply(() => {
                    initBlockCode();
                });
            }
        } else {
            $("#sectionCode").show();
            $("#blockCode").hide();
        }
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
            "dataScript": "#test",
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
                    console.log(data);
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
                    console.log(data);
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

});
