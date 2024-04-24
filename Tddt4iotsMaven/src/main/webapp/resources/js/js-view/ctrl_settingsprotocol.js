app.controller("settings_controller", function ($scope, $http) {

    $(document).ready(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("Settings Procotol", true);
    });

    $scope.selectedTechnology = {
        valueLibraries: '',
        valueVariables: '',
        valueSetup: '',
        valueLoop: '',
        valueMethods: ''
    };
    $scope.editors = {};  // Guardamos las instancias de los editores

    $scope.dataArrayProtocol = [];
    $scope.sourceJson = "/comunication.json";
    $scope.posicionUpdate = -1;
    $scope.protocolLoaded = "Communication Protocol";

    $(document).ready(() => {
        $scope.loadInterchangeProtocol({"filePath": $scope.sourceJson});
        initEditors();
        $scope.appPage.Select = "settingsprotocol";
    });

    $scope.changeSourceProtocol = (sourcePath) => {
        $scope.sourceJson = sourcePath;
        $scope.protocolLoaded =  sourcePath === "/comunication.json" ? "Communication Protocol" : "Interchange Data Protocol" ;
        $scope.loadInterchangeProtocol({"filePath": $scope.sourceJson});
    };

    // seleccionear un protocolo para editar o ver su informacion
    $scope.selectedProtocol = (data, index) => {
        console.log(data);
        $scope.posicionUpdate = index;
        $scope.openModalNewProtocol();
        $scope.selectedTechnology.valueLibraries = data.code.valueLibraries;
        $scope.selectedTechnology.valueVariables = data.code.valueVariables;
        $scope.selectedTechnology.valueSetup = data.code.valueSetup;
        $scope.selectedTechnology.valueLoop = data.code.valueLoop;
        $scope.selectedTechnology.valueMethods = data.code.valueMethods;
        $scope.ip_nameprotocol = data.name;
        initEditors();
        addCodeEditor(data.code)
    };

    // Agregar o editar un nuevo portocolo
    $scope.addNewProtocol = (form) => {
        if (form.$valid) {
            console.log(form.ip_nameprotocol.$viewValue);
            // Guardar los cambios hechos en los editores
            $scope.selectedTechnology.valueLibraries = $scope.editors.libraries.getValue();
            $scope.selectedTechnology.valueVariables = $scope.editors.variables.getValue();
            $scope.selectedTechnology.valueSetup = $scope.editors.setup.getValue();
            $scope.selectedTechnology.valueLoop = $scope.editors.loop.getValue();
            $scope.selectedTechnology.valueMethods = $scope.editors.methods.getValue();
            // Enviar datos actualizados al servidor o backend
            console.log($scope.selectedTechnology);

            if($scope.posicionUpdate !== -1) {
                $scope.dataArrayProtocol[$scope.posicionUpdate].name = form.ip_nameprotocol.$viewValue;
                $scope.dataArrayProtocol[$scope.posicionUpdate].code = $scope.selectedTechnology;
                $scope.posicionUpdate = -1
            } else {
                $scope.dataArrayProtocol.push({
                    name: form.ip_nameprotocol.$viewValue,
                    code: $scope.selectedTechnology
                });
            }

            // Limpiamos el arreglo
            let cleanedJsonArray = cleanJsonArray($scope.dataArrayProtocol);
            let data = {
                filePath: $scope.sourceJson ,
                fileContent: JSON.stringify(cleanedJsonArray)
            };
            $scope.saveFileProtocol(data);
            $scope.dataArrayProtocol.length = 0;
            $scope.cleanFormProtocol();
            $scope.loadInterchangeProtocol({"filePath": $scope.sourceJson});
        }
    }

    $scope.deleteProtocol = (index) => {
        swal.fire({
            title: 'Are you sure to remove this protocol ?',
            text: $scope.dataArrayProtocol[index].name,
            showDenyButton: true,
            confirmButtonText: `Yes`,
            denyButtonText: `No`
        }).then((result) => {
            if (result.isConfirmed) {
               $scope.dataArrayProtocol.splice(index, 1);
                let cleanedJsonArray = cleanJsonArray($scope.dataArrayProtocol);
                let data = {
                    filePath: $scope.sourceJson ,
                    fileContent: JSON.stringify(cleanedJsonArray)
                };
                $scope.saveFileProtocol(data);
                $scope.dataArrayProtocol.length = 0;
                $scope.loadInterchangeProtocol({"filePath": $scope.sourceJson});
            } else if (result.isDenied) {
                //
            }
        });
    };

    // limpiar el formulario de los protocolos
    $scope.cleanFormProtocol = () => {
        $scope.selectedTechnology.valueLibraries = "";
        $scope.selectedTechnology.valueVariables = "";
        $scope.selectedTechnology.valueSetup = "";
        $scope.selectedTechnology.valueLoop = "";
        $scope.selectedTechnology.valueMethods = "";
        $scope.ip_nameprotocol = "";
        initEditors();
    }


    $scope.openModalNewProtocol = () => {
        $("#modalNewUpdateProtocol").modal();
    }

    $scope.closeModalNewProtocol = () => {
        $("#modalNewUpdateProtocol").modal("hide");
        $scope.cleanFormProtocol();
    }


    // cargar los protcolos de bajo nivel
    $scope.loadInterchangeProtocol = (parameter) => {
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'fileManagerApi/loadFile',
            data: JSON.stringify({...parameter}),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {
                swal.close();
                console.log(data);
                $scope.$apply(function () {
                    $scope.dataArrayProtocol = data.data;
                });
            },
            error: function (objXMLHttpRequest) {
                console.error("Error: ", objXMLHttpRequest);
            }
        });

    };

    // Usar la API para guardar el archivo con el json editado
    $scope.saveFileProtocol = (parameter) => {
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'fileManagerApi/saveFile',
            data: JSON.stringify({...parameter}),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {
                swal.close();
                alertAll(data);
                $scope.closeModalNewProtocol();
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    };

    function cleanJsonArray(array) {
        return array.map(obj => {
            // Utilizamos la desestructuración para omitir la propiedad `$$hashKey`
            const { $$hashKey, ...cleanObj } = obj;
            return cleanObj;
        });
    }


    // inicializar los editores de codigo
    function initEditors() {
        $scope.editors.libraries = ace.edit("editorLibraries");
        $scope.editors.variables = ace.edit("editorVariables");
        $scope.editors.setup = ace.edit("editorSetup");
        $scope.editors.loop = ace.edit("editorLoop");
        $scope.editors.methods = ace.edit("editorMethods");

        var theme = "ace/theme/chrome";  // Tema claro
        var mode = "ace/mode/c_cpp";  // Modo C/C++
        Object.values($scope.editors).forEach(function (editor) {
            editor.setTheme(theme);
            editor.getSession().setMode(mode);
            editor.getSession().setUseWrapMode(true);  // Habilita el ajuste de línea
            editor.setValue('', 1); // Inicializa con el contenido en blanco
        });
    }

    function addCodeEditor(code) {
        $scope.editors.libraries.setValue(code.valueLibraries);
        $scope.editors.variables.setValue(code.valueVariables);
        $scope.editors.setup.setValue(code.valueSetup);
        $scope.editors.loop.setValue(code.valueLoop);
        $scope.editors.methods.setValue(code.valueMethods);
    }


});