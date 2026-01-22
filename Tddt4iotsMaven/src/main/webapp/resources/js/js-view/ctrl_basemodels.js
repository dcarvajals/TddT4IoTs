/* global store, swal */

app.controller("basemodels_controller", function ($scope, $http) {

    $scope.baseModels = [];
    $scope.modelSelected = {};

    $(document).ready(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("Base Models", true);
        $scope.appPage.Select = "basemodels";
        $scope.initForm();
        $scope.loadBaseModels();
    });

    $scope.loadBaseModels = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "GET",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'model-open-ai/list',
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close();
                $scope.$apply(function () {
                    $scope.baseModels = response.data;
                });
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    };

    $scope.saveModel = async (form) => {
        if (form.$valid) {
            let response;
            if (form.ip_idModel.$viewValue !== null) {
                // editar
                $scope.modelSelected.name = form.ip_nameModel.$viewValue;
                $scope.modelSelected.description = form.ip_descriptionModel.$viewValue;
                response = await HttpService.post('model-open-ai/update-model', $scope.modelSelected, 'openai');
            } else {
                // crear
                const request = {name: form.ip_nameModel.$viewValue, description: form.ip_descriptionModel.$viewValue};
                response = await HttpService.post('model-open-ai/create-model', request, 'openai');
            }

            if (response.status === "OK") {
                // limpiar form
                $scope.initForm();
                $scope.closeModalNewModel();
                // cargar la lista de nuevo
                $scope.loadBaseModels();
                alertAll({status: 2, information: response.message});
            }
        }
    };

    $scope.deleteModel = async (data) => {
        swal.fire({
            title: "Are you sure?",
            text: "You won't be able to revert this!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, delete it!"
        }).then(async (result) => {
            if (result.isConfirmed) {
                const response = await HttpService.post('model-open-ai/inactive-model', data, 'openai');
                if (response.status === "OK") {
                    // cargar la lista de nuevo
                    $scope.loadBaseModels();
                    alertAll({status: 2, information: response.message});
                }
            }
        });
    };

    $scope.selectedModel = async (data) => {
        console.log(data);
        $scope.openModalNewModel();
        $scope.ip_nameModel = data.name;
        $scope.ip_descriptionModel = data.description;
        $scope.ip_idModel = data.id;
        $scope.modelSelected = data;
    };

    $scope.openModalNewModel = () => {
        $("#modalNewBaseModel").modal();
    };

    $scope.closeModalNewModel = () => {
        $scope.initForm();
        $("#modalNewBaseModel").modal("hide");
    };

    $scope.initForm = () => {
        $scope.ip_nameModel = "";
        $scope.ip_descriptionModel = "";
        $scope.ip_idModel = null;
    };

});