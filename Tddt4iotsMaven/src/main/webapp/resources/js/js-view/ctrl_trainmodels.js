app.controller("trainmodels_controller", function ($scope, $http) {

    $scope.optionsTrainModel = [
        {name: "Create File", classActive: "btn btn-sm button-active mr-2 mt-1", showPanel: true},
        {name: "Train Model", classActive: "btn btn-sm button-dt mr-2 mt-1", showPanel: false},
    ];

    $scope.requestTrainingModel = {
        startDate: "",
        endDate: "",
        commit: "",
        pathJson: "",
        idTrainig: -1
    }

    $scope.listTrainingHistory = [];
    $scope.resultJson = {};


    $(document).ready(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("Train Models", true);
        $scope.appPage.Select = "trainmodels";
        $scope.apiGetListTrainingHistory();
        $scope.initViewResult();
    });

    $scope.initViewResult = () => {
        $scope.resultJson = ace.edit("resultJson");

        var theme = "ace/theme/chrome";  // Tema claro
        var mode = "ace/mode/json";  // Modo C/C++

        $scope.resultJson.setTheme(theme);
        $scope.resultJson.getSession().setMode(mode);
        $scope.resultJson.getSession().setUseWrapMode(true);  // Habilita el ajuste de línea
        $scope.resultJson.setReadOnly(true); // Deshabilita la edición
        $scope.resultJson.setValue('', 1); // Inicializa con el contenido en blanco
    }

    $scope.createFileModel = (form) => {
        if(form.$valid) {
            console.log(form.ip_startDate.$viewValue);
            console.log(form.ip_endDate.$viewValue);
            console.log(form.ip_commit.$viewValue);

            $scope.requestTrainingModel.startDate = form.ip_startDate.$viewValue;
            $scope.requestTrainingModel.endDate = form.ip_endDate.$viewValue;
            $scope.requestTrainingModel.commit = form.ip_commit.$viewValue;

            $scope.apiCreateFileModel($scope.requestTrainingModel);
            // Call API create file model
        }
    }

    $scope.trainModel = () => {
        console.log($scope.requestTrainingModel.pathJson);
        let request = {
            "startDate": $scope.requestTrainingModel.startDate,
            "endDate": $scope.requestTrainingModel.endDate,
            idTrainingHistory: $scope.requestTrainingModel.idTrainig
        }
        console.log(request);
        $scope.apiTrainModel(request);
    };

    $scope.deleteTraining = () => {
        let request =
        {
            "classDTO": $scope.requestTrainingModel.idTrainig
        };
        $scope.apiDeleteTraining(request);
    };

    $scope.apiDeleteTraining = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'master-project/deleteTraining',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close()
                console.log(response);
                let data = response.data;
                if(response.status === "OK") {
                    $scope.closeModalTrainModel();
                    alertAll({status: 2, information: data});
                }
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apiTrainModel = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'master-project/trainingModelOpenAi',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close();
                $scope.closeModalTrainModel();
                $scope.apiGetListTrainingHistory();
                alertAll(response);
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apiCreateFileModel = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'master-project/createFileTraining',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close();
                console.log(response);
                let data = response.data;
                if(response.status === "OK") {
                    $scope.$apply(function () {
                        $scope.requestTrainingModel.pathJson = data.pathJsonFile;
                        $scope.requestTrainingModel.idTrainig = data.idTrainingHistory;
                        $scope.changeOption($scope.optionsTrainModel[1]);
                    });
                }
                alertAll(response);
                console.log(data);
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apiGetListTrainingHistory = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "GET",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'training-history/list',
            headers: {"userToken": dataUser.user_token},
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close();
                console.log(response);
                $scope.$apply(function  () {
                    $scope.listTrainingHistory = response.data;

                    $scope.listTrainingHistory.forEach(item => {
                        item["resultJson"] = JSON.parse(item.resultTrining);
                    });

                    console.log($scope.listTrainingHistory);
                });
            },
            error: function (objXMLHttpRequest) {
                swal.close();
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    };

    $scope.changeOption = (optionActual) => {
        $scope.optionsTrainModel.forEach(option => {
            if (option.showPanel) {
                option.classActive = "btn btn-sm button-dt mr-2 mt-1";
                option.showPanel = false;
            }
        });
        optionActual.classActive = "btn btn-sm button-active mr-2 mt-1";
        optionActual.showPanel = true;
    }

    $scope.openModalTrainModel = () => {
        $("#modalNewTrainModel").modal();
        $scope.changeOption($scope.optionsTrainModel[0]);
    }

    $scope.closeModalTrainModel = () => {
        $("#modalNewTrainModel").modal("hide");
    }

    $scope.openModalMoreInformation = (trainingModel) => {
        $("#modalMoreInformationTrain").modal();
        $scope.changeOption($scope.optionsTrainModel[0]);

        let response = "";
        if(trainingModel.resultJson.status === "OK") {
            response = JSON.stringify(trainingModel.resultJson, null, 4);
        } else {
            let jsonString = trainingModel.resultTrining;
            response = JSON.stringify(JSON.parse(jsonString), null, 4);
        }

        $scope.resultJson.getSession().setValue(response, -1);
    }

    // Función para escapar comillas dobles dentro de una cadena JSON
    function escapeJsonString(str) {
        return str.replace(/\\([\s\S])|(")/g, "\\$1$2");
    }

    $scope.closeModalMoreInformation = () => {
        $("#modalMoreInformationTrain").modal("hide");
    }

});