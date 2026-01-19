app.controller("testmodels_controller", function ($scope, $http) {

    $(document).ready(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("Tests Models", true);
        $scope.appPage.Select = "testmodels";
        $scope.initDiagramClass();
    });

    $scope.initDiagramClass = () => {
        diagramClass = createDiagram({
            "width": 100,
            "height": 81,
            "id_div": "areaDiagram",
            "diagram": UMLClassDiagram,
            "name": "Class Diagram",
            "interaction": true,
            "draw": true
        });
    };

    $scope.analizeUseCaseDescription = (form) => {
        console.log(form.ip_descriptionUseCase.$viewValue);
        let request = {
            classDTO: {
                descriptionUseCase: form.ip_descriptionUseCase.$viewValue
            }
        };
        $scope.apiUseModel(request);
    }

    $scope.apiUseModel = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'master-project/useModelTraining',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close();
                let dataDiagramClass = JSON.parse(response.data);
                response.data = JSON.parse(dataDiagramClass.data);
                //response.data = response.data.data;
                $scope.initDiagramClass();
                updateClassDiagramOpenAi(response.data, "C");
                console.log(response);
                alertAll(response);
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    };

});