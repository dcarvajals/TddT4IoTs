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
            .when("/trainmodels", {
                templateUrl: 'trainmodels.html',
                controller: "trainmodels_controller"
            })
            .when("/testmodels", {
                templateUrl: 'testmodels.html',
                controller: "testmodels_controller"
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
    $scope.baseModels = [];
    $scope.baseModelSelcted = {};
    $scope.useOpenAiSelected = {};
    $scope.useOpenAi = [{name: "TRAINING", description: "This option will allow you to train models based on the base model you selected in the previous option.", value: false, pathWs: "create-train"},
                        {name: "USE", description: "This option will only allow you to consume already trained models based on the base model you selected.", value: false, pathWs: "create-use"}]
    $scope.menuSettings = [{label: "OpenAI", icon: "fas fa-gem", active: true, visible:true},
                           {label: "Training", icon: "fas fa-splotch", active: false, visible:true}];
    $scope.modelPermissions = [];
    $scope.trainingHistoryValidate = {};
    $scope.trainingHistoryFromBaseModel = [];

    $scope.suggestion_type = null;
    $scope.observation = null;

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
        $scope.loadBaseModels();
        $scope.validatePermissoModel();
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

    $scope.openModalSettings = () => {
        $("#modalSettings").modal();
    };

    $scope.closeModalSettings = () => {
        $("#modalSettings").modal("hide");
    }

    $scope.changeMenu = (option) => {
        $scope.menuSettings.forEach(menu => {
            menu.active = false;
        });

        option.active = true;
    };

    $scope.selectedModelBase = (modelBase) => {
        let backgroundNormal = "alert alert-light border-primary cursorPointer";
        let backgroundSelected = "alert alert-primary border-primary";
        $scope.baseModels.forEach(model => {
            model.background = backgroundNormal;
            model.selected = false;
        });

        modelBase.background = backgroundSelected;
        modelBase.selected = true;
        $scope.baseModelSelcted = modelBase;
    };

    $scope.selectedModeUseOpenAi = (mode) => {
        $scope.useOpenAi.forEach(mode => {
            mode.value = false;
        });

        mode.value = true;
        $scope.useOpenAiSelected = mode;
    };

    $scope.saveSettingOpenAi = () => {
        if(JSON.stringify($scope.baseModelSelcted) === '{}') {
            alertAll({status: 3, information: "Select a base model."});
            return;
        }

        if(JSON.stringify($scope.useOpenAiSelected) === '{}') {
            alertAll({status: 3, information: "Select the mode to use the tool."});
            return;
        }

        let saveModeOpenAiReq = {
            request: {classDTO: {model: {id: $scope.baseModelSelcted.id}}},
            path: $scope.useOpenAiSelected.pathWs
        }
        $scope.saveModeOpenAi(saveModeOpenAiReq);
    };

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
                console.log("Error: ", objXMLHttpRequest.responseText)
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
                $scope.$apply(function  () {
                    $scope.baseModels = response.data;
                    $scope.baseModels.forEach(model => {
                       model["background"] = "alert alert-light border-primary cursorPointer";
                       model["selected"] = false;
                    });
                });
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    // api para guardar la secret key de openai
    $scope.saveModeOpenAi = (parameter) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'model-permission/' + parameter.path,
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(parameter.request),
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
                swal.close();
                console.log("Error: ", objXMLHttpRequest.responseText);
                alertAll(JSON.parse(objXMLHttpRequest.responseText));
            }
        });
    };

    $scope.validatePermissoModel = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "GET",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'model-permission/validate',
            headers: {"userToken": dataUser.user_token},
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                data = response.data;
                console.log("validatePermissoModel: ", data)
                $scope.$apply(function  () {
                   $scope.baseModels.forEach(model => {
                      if(model.id === data.model.id) {
                          model["selected"] = true;
                          model["background"] = "alert alert-primary border-primary";
                      }
                   });
                   if(data.canTrain) {
                       $scope.useOpenAi[0].value = data.canTrain;
                       swal.close();
                   } else if (data.canUse) {
                       $scope.useOpenAi[1].value = data.canUse;

                       var request = {
                           classDTO:
                               {
                                   idModel: data.model.id
                               }
                       };
                       console.log("apiPermissionModels: ", request);
                       $scope.apiPermissionModels(request);
                   }

                });
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apiPermissionModels = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'model-permission/permission-models',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
            },
            success: function (response) {
                console.log(response);
                $scope.modelPermissions = response.data;
                $scope.modelPermissions.forEach(model => {
                    model["selected"] = false;
                });
                $scope.apiTrainingHistoryValidate();
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apitrainingFromBaseModels = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'training-history/training-from-base-models',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
            },
            success: function (response) {
                swal.close();
                console.log("apitrainingFromBaseModels: ", response);
                $scope.trainingHistoryFromBaseModel = response.data;
                $scope.trainingHistoryFromBaseModel.forEach(item => {
                    item["selected"] = false;
                });
                $scope.trainingHistoryFromBaseModel.forEach(item => {
                    if(item.id === $scope.trainingHistoryValidate.id) {
                        item["selected"] = true;
                    }
                });
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apiTrainingHistoryValidate = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "GET",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'training-history/validate',
            headers: {"userToken": dataUser.user_token},
            beforeSend: function (xhr) {
            },
            success: function (response) {
                console.log(response);
                $scope.trainingHistoryValidate = response.data;
                $scope.modelPermissions.forEach(modelPermission => {
                    if($scope.trainingHistoryValidate.model.id === modelPermission.id) {
                        modelPermission["selected"] = true;
                        let request = {
                            classDTO:
                                {
                                    idModelPermission: modelPermission.id,
                                    idPersonModelTraining: modelPermission.person.id
                                }
                        }
                        $scope.apitrainingFromBaseModels(request);
                    }
                });
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    //SUGGESTION


    $scope.openModalOpinion = () =>
    {
        $("#ModalOpinion").modal();
    };


    $scope.CloseModalOpinion = () => {
        $("#ModalOpinion").modal('hide');
    };


    $scope.CreateOpinion = (form) =>
    {

        console.log(form);
        console.log(cantidad_estrellas_seleccionadas);
        if (form.$valid && cantidad_estrellas_seleccionadas>0) {
            var dataUser = store.session.get("user_tddm4iotbs");
            let suggestion_type = form.suggestion_type.$viewValue;
            let observation = form.observation.$viewValue;
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + "suggestionpis/registerSuggestion",
                data: JSON.stringify({
                    "emailperson":dataUser.email_person,
                    'valoration': cantidad_estrellas_seleccionadas,
                    'suggestion_type': suggestion_type,
                    'observation': observation,
                }),
                beforeSend: function () {
                    loading();
                },
                success: function (data)
                {
                    $("#ModalOpinion").modal('hide');
                    swal.close();
                    console.log(data);
                    infoTo({
                        "message":"Thank you your opinion has been registered",
                        "nameApplication":"TddT4IoTs"
                    });
                    $scope.$apply(function () {
                        $scope.ClearSuggestion();
                    });
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error", objXMLHttpRequest);
                }
            });
        }
    };

    $scope.ClearSuggestion = () =>
    {
         $scope.suggestion_type = null;
        $scope.observation = null;
        persistir=false;
        cantidad_estrellas_seleccionadas=0;
        let controls = document.querySelectorAll('.startimg');
        for(let x=0;x<controls.length;x++)
            controls[x].src = 'resources/img/img-opinions/estrella_black.svg';
    }



    btn_move_suggestion();



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



let persistir=false;
let cantidad_estrellas_seleccionadas=0;

function cambiarColor(e,controls,indice)
{
    //alert(indice)
    if(e.type=="mouseover")
    {
        if(!persistir)
        {
            if(indice===0)
                e.currentTarget.src = 'resources/img/img-opinions/estrella_yellow.svg';
            else
            {
                for(let x=0;x<indice;x++)
                    controls[x].src = 'resources/img/img-opinions/estrella_yellow.svg';
            }
        }
    }
    else if(e.type=="mouseout")
    {
        if(!persistir){
            for(let x=0;x<controls.length;x++)
                controls[x].src = 'resources/img/img-opinions/estrella_black.svg';
        }
    }
    else if(e.type=="click")
    {
        if(!persistir){
            persistir=true;
            cantidad_estrellas_seleccionadas=indice;
            for(let x=0;x<indice;x++)
                controls[x].src = 'resources/img/img-opinions/estrella_yellow.svg';
        }
        else{
            persistir=false;
            cantidad_estrellas_seleccionadas=0;
        }
    }

}


function asignar_eventos()
{
    let imgsaux = document.querySelectorAll('.startimg');
    let imgs=imgsaux;
    imgs.forEach((value,index)=>{
        value.addEventListener('mouseover', (e)=>cambiarColor(e,imgsaux,index+1));
        value.addEventListener('mouseout', (e)=>cambiarColor(e,imgsaux,index+1));
        value.addEventListener('click', (e)=>cambiarColor(e,imgsaux,index+1));
    });
}

let move_suggestion=false;

function btn_move_suggestion()
{
    let btn_suggestion = document.querySelector(".btn-suggestion");
    btn_suggestion.addEventListener("mousedown", function(e) {
        move_suggestion=true;

        //console.log(e.pageX+" - "+e.pageY);
        //btn_suggestion.style.left=`${e.pageX}px`;
        //btn_suggestion.style.top=`${e.pageY}px`;
    });

    btn_suggestion.addEventListener("mouseup", function(e) {
        move_suggestion=false;
    });


    window.addEventListener("mousemove", function(e) {
        if(move_suggestion)
        {
            btn_suggestion.style.left=`${e.clientX-30}px`;
            btn_suggestion.style.top=`${e.clientY-25}px`;
        }
    });


}