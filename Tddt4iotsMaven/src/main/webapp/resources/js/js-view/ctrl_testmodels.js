/* global app, angular, store, urlWsOpenAi, loading, alertAll, createDiagram, UMLClassDiagram, updateClassDiagramOpenAi, diagramClass, elementsClass, positionsClass, Swal, swal */

app.controller("testmodels_controller", function ($scope, $http, $timeout) {
    $scope.isAnalyzingModel = false;
    $scope.modelDiagramReady = false;
    $scope.modelDiagramStats = {classes: 0, relationships: 0};

    const closeLoading = () => {
        if (typeof Swal !== "undefined" && Swal.close) {
            Swal.close();
        } else if (typeof swal !== "undefined" && swal.close) {
            swal.close();
        }
    };

    const updateView = (values) => {
        $scope.$applyAsync(() => Object.assign($scope, values));
    };

    const normalizeDiagramResponse = (payload) => {
        let diagramData = payload;

        while (diagramData && diagramData.data && !diagramData.diagram) {
            diagramData = diagramData.data;
        }

        if (typeof diagramData === "string") {
            const jsonText = diagramData.replace(/^```(?:json)?\s*/i, "").replace(/\s*```$/, "");
            diagramData = JSON.parse(jsonText);
        }

        if (!diagramData || !Array.isArray(diagramData.diagram)) {
            throw new Error("The AI response does not contain a valid UML diagram structure.");
        }

        return diagramData;
    };

    $scope.initDiagramClass = () => {
        if (Array.isArray(elementsClass)) {
            elementsClass.length = 0;
        }
        if (Array.isArray(positionsClass)) {
            positionsClass.length = 0;
        }

        diagramClass = createDiagram({
            width: 100,
            height: 68,
            id_div: "areaDiagram",
            diagram: UMLClassDiagram,
            name: "AI generated class diagram",
            interaction: true,
            draw: true
        });
    };

    $scope.analizeUseCaseDescription = (form) => {
        form.$setSubmitted();
        if (form.$invalid || $scope.isAnalyzingModel) {
            return;
        }

        const description = (form.ip_descriptionUseCase.$viewValue || "").trim();
        if (!description) {
            return;
        }

        $scope.apiUseModel({
            classDTO: {
                descriptionUseCase: description
            }
        });
    };

    $scope.apiUseModel = (request) => {
        const dataUser = store.session.get("user_tddm4iotbs");
        if (!dataUser || !dataUser.user_token) {
            alertAll({status: 4, information: "Your session is not available. Sign in again before testing the model.", tittle: "Session required"});
            return;
        }

        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + "master-project/useModelTraining",
            headers: {userToken: dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function () {
                updateView({isAnalyzingModel: true, modelDiagramReady: false});
                loading();
            },
            success: function (response) {
                closeLoading();
                try {
                    if (!response || String(response.status).toUpperCase() !== "OK") {
                        throw new Error((response && response.message) || "The AI service could not generate the model.");
                    }

                    const diagramData = normalizeDiagramResponse(response.data);
                    $scope.initDiagramClass();
                    const stats = updateClassDiagramOpenAi(diagramData, "C");

                    updateView({
                        isAnalyzingModel: false,
                        modelDiagramReady: true,
                        modelDiagramStats: stats
                    });

                    alertAll({status: 2, information: "The UML class diagram was generated successfully.", tittle: "Diagram ready"});
                } catch (error) {
                    console.error("Unable to render the AI diagram:", error);
                    updateView({isAnalyzingModel: false, modelDiagramReady: false});
                    alertAll({status: 4, information: error.message || "The server response could not be rendered as a UML diagram.", tittle: "Diagram generation error"});
                }
            },
            error: function (requestError) {
                closeLoading();
                updateView({isAnalyzingModel: false, modelDiagramReady: false});
                const responseMessage = requestError.responseJSON && requestError.responseJSON.message;
                alertAll({
                    status: 4,
                    information: responseMessage || "The AI service is unavailable or returned an invalid response.",
                    tittle: "AI service error"
                });
            }
        });
    };

    $timeout(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("Test Models", true);
        $scope.appPage.Select = "testmodels";
        $scope.initDiagramClass();
    });
});
