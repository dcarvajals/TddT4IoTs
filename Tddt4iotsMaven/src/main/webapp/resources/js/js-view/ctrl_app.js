var app = angular.module('app', ["ngRoute"]);//, "ngAnimate"
app.config(function ($routeProvider) {
    $routeProvider
            .when("/", {
                templateUrl: "home.html",
                controller: "home_controller"
            })
            .when("/home", {
                templateUrl: "home.html",
                controller: "home_controller"
//                controllerAs: "mapa"
            })
            .when("/myprojects", {
                templateUrl: "myprojects.html",
                controller: "myprojects_controller"
            })
            .when("/shareprojects", {
                templateUrl: "shareprojects.html",
                controller: "shareprojects_controller"
            })
            .when("/diagramsuml", {
                templateUrl: "diagramsuml.html",
                controller: "diagramsuml_controller"
            })
            .when("/dispositiveiot", {
                templateUrl: "dispositiveiot.html",
                controller: "dispositiveiot_controller"
            })
            .when("/componentsiot", {
                templateUrl: "componentsiot.html",
                controller: "component_controller"
            })
            .when("/usermanagement", {
                templateUrl: "usermanagement.html",
                controller: "users_controller"
            })
            .when("/interpret", {
                templateUrl: "interpret.html",
                controller: "interpret_controller"
            })
            .when("/entregablesproject", {
                templateUrl: "entregablesproject.html",
                controller: "projectentregable_controller"
            })
            .when("/settingsprotocol", {
                templateUrl: "settingsprotocol.html",
                controller: "settings_controller"
            })
            .when("/basemodels", {
                templateUrl: 'basemodels.html',
                controller: "basemodels_controller"
            })
            .otherwise({
                redirectTo: 'notfound',
                templateUrl: 'notfound.html',
                controller: "notfound_controller"
            });
});


app.controller("application", function ($scope, $http) {

    $scope.DatoUsuario = {};
    $scope.rutaImgUser = location.origin + rutasStorage.imguser;
    $scope.responseHasSecretKey = {secretKey: "", hasSecretKey: false};

    $scope.appPage = {
        tittle: "Home",
        Select: ''
    };

    $scope.validatePermit = (DatoUsuario) => {
        return  DatoUsuario["type_person"] === "U" ? "Usuario" :
            DatoUsuario["type_person"] === "A" ? "Administrator" :
                DatoUsuario["type_person"] === "R" ? "Root" : "Inactive";
    };

    $scope.nameUser = (DatoUsuario) => {
      return DatoUsuario.name_person + ' ' + DatoUsuario.lastname_person;
    };

    $(document).ready(function () {
        $scope.validateSecretKey();
    });

    $scope.changeTittlePage = function (tittle, apply) {
        if (apply) {
            $scope.$apply(() => {
                $scope.appPage.tittle = tittle;
            });
        } else {
            $scope.appPage.tittle = tittle;
        }
    };

    $scope.redirect = function (page) {
        if (page) {
            $scope.appPage.Select = page;
            location.href = "#!" + page;
        }
    };

    $scope.logOut = () => {
        cerrarSesion();
    };

    $scope.openModalNewSecretKey = () => {
        $("#modalNewSecretKey").modal();
    }

    $scope.closeModalNewSecretKey = () => {
        $("#modalNewSecretKey").modal("hide");
    }

    $scope.addNewSecretKey = (form) => {
        let request = {"classDTO": {"openaiSecretKey": ""}};
        if(form.$valid) {
            request.classDTO.openaiSecretKey = form.ip_secretKey.$viewValue;
            $scope.saveNewSecretKey(request);
        }
    }


    // api para guardar la secret key de openai
    $scope.saveNewSecretKey = (parameter) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'person/create-secret-key-openai',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(parameter),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {
                swal.close();
                $scope.closeModalNewSecretKey();
                $scope.validateSecretKey();
                alertAll(data);
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    };

    $scope.validateSecretKey = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "GET",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'person/validate-secret-key-openai',
            headers: {"userToken": dataUser.user_token},
            beforeSend: function (xhr) { },
            success: function (data) {
                swal.close();
                $scope.$apply(function() { // Asegura que AngularJS se entere de los cambios
                    $scope.responseHasSecretKey.hasSecretKey = data.data.hasSecretKey;
                    $scope.DatoUsuario = getDataSession();
                    let permitUser = document.getElementById("permitUser");
                    let nameUser = document.getElementById("nameUser");
                    permitUser.innerHTML = $scope.validatePermit($scope.DatoUsuario);
                    let iconoCheck = $scope.responseHasSecretKey.hasSecretKey ? "<i class=\"fas fa-check-circle text-success-400 mr-1\"></i>" : "";
                    nameUser.innerHTML = iconoCheck + $scope.nameUser($scope.DatoUsuario);
                    if($scope.responseHasSecretKey.hasSecretKey) {
                        $scope.responseHasSecretKey.secretKey = data.data.secretKey;
                        $scope.ip_secretKey = $scope.responseHasSecretKey.secretKey
                    }
                });
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

});

function verpunto(val) {
    // console.log("llamar a", val);
    angular.element($('[ng-view')).scope().verinfopunto(val);
}

function verRuta(val) {
    // console.log("llamar a", val);
    angular.element($('[ng-view')).scope().verinforuta(val);
}

//angular.element($('[ng-view')).scope().fun()
