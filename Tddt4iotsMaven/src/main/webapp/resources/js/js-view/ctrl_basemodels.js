app.controller("basemodels_controller", function ($scope, $http) {

    $scope.baseModels = [];

    $(document).ready(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("Base Models", true);
        $scope.appPage.Select = "basemodels";
        $scope.loadBaseModels();
    });

    $scope.loadBaseModels = function () {
        $http.get(urlWsOpenAi + "model-open-ai/list").then(function (response) {
            $scope.baseModels = response.data.data;
            console.log($scope.baseModels);
        });
    };

    $scope.openModalNewModel = () => {
        $("#modalNewBaseModel").modal();
    }

    $scope.closeModalNewModel = () => {
        $("#modalNewBaseModel").modal("hide");
        $scope.cleanFormProtocol();
    }

});