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
            .otherwise({
                redirectTo: 'notfound',
                templateUrl: 'notfound.html',
                controller: "notfound_controller"
            });
});


app.controller("application", function ($scope, $http) 
{

    $scope.DatoUsuario = {};
    $scope.rutaImgUser = location.origin + rutasStorage.imguser    
    
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
        $scope.DatoUsuario = getDataSession();
        //let permitUser = document.getElementById("permitUser");
        let nameUser = document.getElementById("nameUser");
        //permitUser.innerHTML = $scope.validatePermit($scope.DatoUsuario);
        nameUser.innerHTML = $scope.nameUser($scope.DatoUsuario);
        // console.log($scope.DatoUsuario);
        // console.log("tddm4iots iniciado correctamente");
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