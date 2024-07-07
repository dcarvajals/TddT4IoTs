app.controller("basemodels_controller", function ($scope, $http) {

    $scope.baseModels = [];

    $(document).ready(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("Base Models", true);
        $scope.appPage.Select = "basemodels";
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
                $scope.$apply(function  () {
                    $scope.baseModels = response.data;
                });
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.openModalNewModel = () => {
        $("#modalNewBaseModel").modal();
    }

    $scope.closeModalNewModel = () => {
        $("#modalNewBaseModel").modal("hide");
        $scope.cleanFormProtocol();
    }

});