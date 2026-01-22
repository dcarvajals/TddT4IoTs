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
                try {
                    // 2. Validamos el estado INTERNO
                    if (response.status !== "OK") {
                        // Preparamos el objeto para tu función alertAll
                        alertAll({
                            status: 4, // Caso errorTo
                            information: response.message,
                            tittle: "Error OpenAI"
                        });
                        return; // Detenemos la ejecución
                    }

                    // 3. Si no hay error interno, procedemos con el diagrama
                    // Según tu lógica anterior, parece que hay un tercer nivel de datos
                    $scope.initDiagramClass();
                    updateClassDiagramOpenAi(response.data, "C");

                    alertAll({
                        status: 2, // Caso successTo
                        information: "Diagrama generado",
                        tittle: "Éxito"
                    });

                } catch (e) {
                    console.error("Error al parsear la respuesta:", e);
                    alertAll({
                        status: 4,
                        information: "La respuesta del servidor no tiene el formato esperado",
                        tittle: "Error de Sistema"
                    });
                }
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    };

});