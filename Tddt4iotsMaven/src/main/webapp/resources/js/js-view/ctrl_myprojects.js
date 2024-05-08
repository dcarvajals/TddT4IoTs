/* global urlWebServicies, store, swal */

app.controller("myprojects_controller", function ($scope, $http) {

    $scope.mavenProject = "";

    $(document).ready(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("My Projects", true);
        $scope.loadProjects();
        $scope.appPage.Select = "myprojects";
        $scope.flag_share_user_exists = true;
    });

    /*
     * Status
     * Active => A
     * Inactive => I
     * */
    
    $scope.members_in_project = [];
    $scope.myprojects = [];
    $scope.myprojects_entregables = [];

    $scope.selected_project = {};
    $scope.flag_selected_project = false;
    $scope.flag_share_user_exists = true;

    $scope.loadEntregablesProject = () => {
        if ($scope.selected_project !== undefined && $scope.selected_project !== null) {

            window.sessionStorage.setItem("id_project", $scope.selected_project.idproj);
            window.location = "app.html#!/entregablesproject";

            /*$.ajax({
             method:"POST",
             dataType: "json",
             contentType: "application/json; charset=utf-8",
             url: urlWebServicies + 'entregable/selectEntregables',
             data: JSON.stringify({
             "idmasterproject": $scope.selected_project.idproj
             }),
             success: function(data){
             window.location = "app.html#!/home";
             }
             });*/
        }

    };

    //funcion para cargar los proyectos existentes
    $scope.loadProjects = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/getProjects',
                data: JSON.stringify({
                    "user_token": dataUser.user_token,
                    "type": "PROJECT_USER"
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    // console.log(data);
                    $scope.$apply(function () {
                        $scope.myprojects = data.data;
                    });

                    alertAll(data);
                    // console.log($scope.myprojects);
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
    };
    
    //Function that loads the members in a specific project
    $scope.loadMembersInProjects = (id_masterproject) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/selectMembers',
                data: JSON.stringify({
                    "user_token": dataUser.user_token,
                    "type": "PROJECT_USER",
                    "idproj": id_masterproject
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    // console.log(data);
                    $scope.$apply(function () {
                        $scope.members_in_project = data.data;
                    });

                    alertAll(data);
                    // console.log($scope.myprojects);
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
    };

    $scope.alert = function (arg) {
        alert(arg);
    };
    $scope.selectProject = function (objetct_selected_project) {
        var dataUser = store.session.get("user_tddm4iotbs");
        window.sessionStorage.setItem("projectpath", objetct_selected_project.path_mp);
        window.sessionStorage.setItem("projectname", objetct_selected_project.name_mp);
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/getModules',
                data: JSON.stringify({
                    "user_token": dataUser.user_token,
                    "folder": objetct_selected_project.path_mp,
                    "stateUml": objetct_selected_project.status_uml,
                    "stateIoT": objetct_selected_project.status_iot
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    // console.log(data);
                    $scope.flag_selected_project = true;
                    // console.log(objetct_selected_project);

                    $scope.$apply(() => {
                        $scope.selected_project = {
                            "idproj": objetct_selected_project.id_masterproject,
                            "creationdate_mp": objetct_selected_project.creationdate_mp,
                            "updatedate_mp": objetct_selected_project.updatedate_mp,
                            "name_mp": objetct_selected_project.name_mp,
                            "code_mp": objetct_selected_project.code_mp,
                            "permit_mp": objetct_selected_project.permit_pm,
                            "uml": {data: data.data.dataUml, "msgUml": data.data.msgUml},
                            "iot": {data: data.data.dataIoT, "msgIoT": data.data.msgIoT},
                            "download": objetct_selected_project.download,
                            "id_masterproject" : objetct_selected_project.id_masterproject
                        };
                        $scope.mavenProject = rutasStorage.projects + objetct_selected_project.path_mp + "/ProjectMvnSpr.zip";
                    });
                    // console.log("PATH DEEL RPOYECTO ", objetct_selected_project.path_mp);
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
        // console.log($scope.selected_project);
    };

    $scope.deleteProject = function (objetct_selected_project) {
        swal.fire({
            title: '¿You want to delete this project?',
            showDenyButton: true,
//                showCancelButton: true,
            confirmButtonText: `Delete`,
            denyButtonText: `Cancel`
        }).then((result) => {
            if (result.isConfirmed) {

                var dataUser = store.session.get("user_tddm4iotbs");
                if (dataUser !== undefined && dataUser !== null) {
                    $.ajax({
                        method: "POST",
                        dataType: "json",
                        contentType: "application/json; charset=utf-8",
                        url: urlWebServicies + 'projects/deleteProject',
                        data: JSON.stringify({
                            "user_token": dataUser.user_token,
                            "idproj": objetct_selected_project.id_masterproject
                        }),
                        beforeSend: function (xhr) {
                            loading();
                        },
                        success: function (data) {
                            swal.close();
                            // console.log(data);
//                    $scope.$apply(() => {
//
//                    });
                            $scope.loadProjects();
                            alertAll(data);
                        },
                        error: function (objXMLHttpRequest) {
                            // console.log("error: ", objXMLHttpRequest);
                        }
                    });
                } else {
                    location.href = "index";
                }
            } else if (result.isDenied) {
                swal.fire("Okay ... think better next time");
            }
        });
    };

    $scope.deleteModule = function (objetct_selected_project, type) {

        swal.fire({
            title: '¿You want to delete this project?',
            showDenyButton: true,
//                showCancelButton: true,
            confirmButtonText: `Delete`,
            denyButtonText: `Cancel`
        }).then((result) => {
            if (result.isConfirmed) {

                var dataUser = store.session.get("user_tddm4iotbs");
                if (dataUser !== undefined && dataUser !== null) {
                    // console.log(objetct_selected_project);
                    $.ajax({
                        method: "POST",
                        dataType: "json",
                        contentType: "application/json; charset=utf-8",
                        url: urlWebServicies + 'projects/deleteModules',
                        data: JSON.stringify({
                            "user_token": dataUser.user_token,
                            "idproj": objetct_selected_project.idproj,
                            "type": type
                        }),
                        beforeSend: function (xhr) {
                            loading();
                        },
                        success: function (data) {
                            swal.close();
                            // console.log(data);
//                    $scope.$apply(() => {
//
//                    });
                            window.location.reload();
                            alertAll(data);
                        },
                        error: function (objXMLHttpRequest) {
                            // console.log("error: ", objXMLHttpRequest);
                        }
                    });

                } else {
                    location.href = "index";
                }
            } else if (result.isDenied) {
                swal.fire("Okay ... think better next time");
            }
        });
    };


    $scope.createModuleUml = function (selected_project) {
        // console.log(selected_project.idproj);
        var dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/updateModule',
                data: JSON.stringify({
                    "user_token": dataUser.user_token,
                    "idproj": selected_project.idproj,
                    "module": "DiagramUml",
                    "state": "CREATE"
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    // console.log(data);
                    if (data.status === 2) {
                        $scope.$apply(() => {
                            selected_project.uml.data.push("true");
                        });
                        alertAll(data);
                    }
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }

        // console.log(selected_project);
        //selected_project.uml.status = "A";
    };

    $scope.createModuleIoT = function (selected_project) {
        // console.log(selected_project.idproj);
        var dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/updateModule',
                data: JSON.stringify({
                    "user_token": dataUser.user_token,
                    "idproj": selected_project.idproj,
                    "module": "EasyIoT",
                    "state": "CREATE"
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    // console.log(data);
                    if (data.status === 2) {
                        $scope.$apply(() => {
                            selected_project.iot.data.push("true");
                        });
                        alertAll(data);
                    }
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
        // console.log(selected_project);
        //selected_project.iot.status = "A";
    };

    $scope.openUml = function () {
        location.href = "jobsAreaUml.html";
    };

    $scope.openIoT = function () {
        location.href = "jobsAreaIoT.html?identifiquer=" + $scope.selected_project.idproj;
    };

    $scope.openUml = function () {
        location.href = "jobsAreaUml.html?identifiquer=" + $scope.selected_project.idproj;
    };

    $scope.newProject = (form) => {
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            if (dataUser !== undefined && dataUser !== null) {
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + 'projects/saveComponent',
                    data: JSON.stringify({
                        "user_token": dataUser.user_token,
                        "nameProject": form.name_np.$viewValue,
                        "description": form.description_np.$viewValue
                    }),
                    beforeSend: function (xhr) {
                        loading();
                    },
                    success: function (data) {
                        swal.close();
                        // console.log(data);
                        //actualizar la tabla de los proyectos
                        $scope.loadProjects();
                        alertAll(data);
                        $scope.name_np = "";
                        $scope.description_np = "";
                        $("#modalCreateProject").modal('hide');
                    },
                    error: function (objXMLHttpRequest) {
                        // console.log("error: ", objXMLHttpRequest);
                    }
                });
            } else {
                location.href = "index";
            }
        }
    };

    $scope.shareProject = (form) => {
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            if (form.shared_np.$viewValue === dataUser["email_person"]) {
                alertAll({status: 3, information: "You cannot share your project to yourself."});
                return;
            }
            if (dataUser !== undefined && dataUser !== null) {
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + 'projects/shareProject',
                    data: JSON.stringify({
                        "user_token": dataUser.user_token,
                        "emailShare": form.shared_np.$viewValue,
                        "idproj": id_masterprojectShare,
                        "permit": form.shared_sp.$modelValue,
                        "stateShare": 'WAITING'
                    }),
                    beforeSend: function (xhr) {
                        loading();
                    },
                    success: function (data) {
                        swal.close();
                        // console.log(data);
                        alertAll(data);
                        $scope.shared_np = "";
                        $("#modalShareProject").modal('hide');
                    },
                    error: function (objXMLHttpRequest) {
                        // console.log("error: ", objXMLHttpRequest);
                    }
                });
            } else {
                location.href = "index";
            }
        }
    };
    
    $scope.shareProjectComplete = (form) => {
        console.log(form);
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            
            if (form.shared_np.$viewValue === dataUser["email_person"]) {
                alertAll({status: 3, information: "You cannot share your project to yourself."});
                return;
            }
            if (dataUser !== undefined && dataUser !== null) {
                var shareJson = "";
                if ($scope.flag_share_user_exists === false) {
                    shareJson = JSON.stringify({
                        "user_token": dataUser.user_token,
                        "idproj": id_masterprojectShare,
                        "email": form.shared_np.$viewValue,                        
                        "name_person": form.shared_user_name.$modelValue,
                        "lastname_person": form.shared_user_last_name.$modelValue,
                        "permit": form.shared_sp.$modelValue,
                        "role": form.shared_role.$modelValue,
                        "speciality": form.shared_speciality.$modelValue,
                        "stateShare": 'WAITING'
                    });
                } else {
                    shareJson = JSON.stringify({
                        "user_token": dataUser.user_token,
                        "idproj": id_masterprojectShare,
                        "email": form.shared_np.$viewValue,                        
                        "permit": form.shared_sp.$modelValue,
                        "role": form.shared_role.$modelValue,
                        "speciality": form.shared_speciality.$modelValue,
                        "stateShare": 'WAITING'
                    });
                }
                
                //Reset the value of the flag
                $scope.flag_share_user_exists = true;
                
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + 'projects/shareProjectComplete',
                    data: shareJson,
                    beforeSend: function (xhr) {
                        loading();
                    },
                    success: function (data) {
                        $scope.$apply(() => {
                            //If the user is not valid then change the flag to force the user
                            //to enter the names and last names.
                            if (data.data["valid_user"] !== null){
                                $scope.flag_share_user_exists = data.data["valid_user"];
                            }
                            else {
                                $scope.flag_share_user_exists = false;
                            }
                            if (data.status ===  "2" || data.status ===  2) {
                                $("#modalShareProjectComplete").modal('hide');
                                $scope.shared_user_name = "";
                                $scope.shared_user_last_name = "";
                                $scope.shared_np = "";
                            }
                        });
                        swal.close();
                        alertAll(data);
                    },
                    error: function (objXMLHttpRequest) {
                        // console.log("error: ", objXMLHttpRequest);
                    }
                });
            } else {
                location.href = "index";
            }
        }
    };

    // descargar
    $scope.downloadPrjMav = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        let api_data = {
            "user_token": dataUser.user_token,
            "idProj": $scope.selected_project.id_masterproject,
            "module": 'DownloadMvn'
        };
        apiencapsulateProject(api_data);
    };

    apiencapsulateProject = (api_param) => {
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'projects/MavenProject',
            data: JSON.stringify({...api_param}),
            beforeSend: () => {
                loading();
            },
            success: (data) => {
                swal.close();
                alertAll(data);
                //$("#modal_package_maven").modal("show");
                $scope.$apply(() => {
                    swal.close();
                    // console.log(data);
                    $("#modal_package_maven").modal("hide");
                    $("#modal_download_maven").modal("hide");
                    $scope.mavenProject = data.data.MavenApplication;
                    download("ProjectMvnSpr.zip", data.data.MavenApplication);
                });
            },
            error: (objXMLHttpRequest) => {
                // console.log("error: ", objXMLHttpRequest);
            }
        });
    };

    $scope.getGanttObjectts = (selected_project) =>
    {
        console.log(selected_project);
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + "projects/getGanttObjects",
            data: JSON.stringify({
                "idmasterproject": selected_project.id_masterproject
            }),
            error: function (objXMLHttpRequest)
            {
                
            },
            success: function (data) {
                var jsonData = data.data;
                if (data.status === "2" || data.status === 2) {
                    
                    dataByElement = {
                        'Project': {
                            'spaces': 0
                        },
                        'Deliverable': {
                            'spaces': 2
                        },
                        'Component': {
                            'spaces': 4
                        },
                        'Task': {
                            'spaces': 6
                        }
                    };
                    
                    dataByStatus = {
                      'NOI': {
                        'blockColor': 'rgba(50, 255, 50, 0.4)',
                        'borderColor': 'rgba(35, 170, 35, 1)',
                        'status': 'No initialized'
                      },
                      'IOT': {
                        'blockColor': 'rgba(20, 130, 255, 0.4)',
                        'borderColor': 'rgba(0, 70, 200, 1)',
                        'status': 'Intialized on time'
                      },
                      'IOO': {
                        'blockColor': 'rgba(255, 255, 25, 0.4)',
                        'borderColor': 'rgba(165, 165, 0, 1)',
                        'status': 'Intialized out of time'
                      },
                      'IPI': {
                        'blockColor': 'rgba(20, 130, 255, 0.4)',
                        'borderColor': 'rgba(0, 70, 200, 1)',
                        'status': 'In proccess'
                      },
                      'IPO': {
                        'blockColor': 'rgba(255, 255, 25, 0.4)',
                        'borderColor': 'rgba(165, 165, 0, 1)',
                        'status': 'Delayed'
                      },
                      'FOT': {
                        'blockColor': 'rgba(219, 164, 12, 0.4)',
                        'borderColor': 'rgba(190, 120, 10, 1)',
                        'status': 'Finished on time'
                      },
                      'FOO': {
                        'blockColor': 'rgba(30, 160, 30, 0.4)',
                        'borderColor': 'rgba(19, 107, 0, 1)',
                        'status': 'Finished out of time'
                      },
                      'DNI': {
                        'blockColor': 'rgba(255, 50, 50, 0.4)',
                        'borderColor':  'rgba(170, 30, 30, 1)',
                        'status': 'Delayed and no intialized'
                      }
                    };

                    //Object to save the colors for each object of the project (deliverable, component and task)
                    var colorsObjectsProject = { 'blockColors': [], 'borderColors': [] };
                    
                    var miniumLevel = 'P';
                    var masterProjectData = jsonData;
                    var parsedProjectData = [];

                    function validName(objectName) {
                      for (item of parsedProjectData) {
                        if (item.y === objectName) {          
                          return false;          
                        }
                      }

                      return true;
                    }

                    function parseTasks(component){
                      tasks = component.tasks;

                      if (!tasks) return;
                      miniumLevel = 'T';
                      tasks.forEach((task, index) => {
                          //Get the values of the objects
                          var name = task.name_task;
                          var status = task.develop_status_task;
                          var startdate = task.startdate_task;
                          var stimateddate = task.stimateddate_task;
                          var updatedate = task.percentageupdate_task;
                          var percentage =  task.actual_percentage_task;

                          var maxdate = stimateddate > updatedate ? stimateddate : updatedate;
                          maxdate = maxdate === null ? stimateddate : maxdate;

                          colorsObjectsProject.blockColors.push(dataByStatus[status].blockColor)
                          colorsObjectsProject.borderColors.push(dataByStatus[status].borderColor)

                          if (validName(name) === false) {
                            name = ' ' + name;
                          }

                          parsedProjectData.push({            
                            "x": [startdate, maxdate],
                            "y": name,
                            "status": status,
                            "stimateddate": stimateddate,
                            "updatedate": updatedate,
                            "type": "Task",
                            "percentage": percentage
                          });
                        });
                     }

                     function parseComponents(deliverable){
                      components = deliverable.components;

                      if (!components) return;
                      miniumLevel = 'C';
                      
                      components.forEach((component, index) => {
                          //Get the values of the objects
                          var name = component.name_component;
                          var status = component.develop_status_component;
                          var startdate = component.startdate_component;
                          var stimateddate = component.stimateddate_component;
                          var updatedate = component.percentageupdate_component;
                          var percentage = component.actual_percentage_component;

                          var maxdate = stimateddate > updatedate ? stimateddate : updatedate;
                          maxdate = maxdate === null ? stimateddate : maxdate;

                          colorsObjectsProject.blockColors.push(dataByStatus[status].blockColor)
                          colorsObjectsProject.borderColors.push(dataByStatus[status].borderColor)

                          if (validName(name) === false) {
                            name = ' ' + name;
                          }

                          parsedProjectData.push({            
                            "x": [startdate, maxdate],
                            "y": name,
                            "stimateddate": stimateddate,
                            "updatedate": updatedate,
                            "type": "Component",
                            "status": status,
                            "percentage": percentage
                          });

                          parseTasks(component);
                        });
                     }

                     function parseDeliverableData(masterProject){   

                        deliverables = masterProject.deliverables;
                        if (!deliverables) return;   
                        miniumLevel = 'D';
                        
                        masterProject.deliverables.forEach((deliverable, index) => {          
                          //Get the values of the objects
                          var name = deliverable.name_entregable;
                          var status = deliverable.develop_status_entregable;
                          var startdate = deliverable.startdate_entregable;
                          var updatedate = deliverable.percentageupdate_entregable;
                          var stimateddate = deliverable.stimateddate_entregable;
                          var percentage = deliverable.actual_percentage_entregable;
                          
                          var maxdate = stimateddate > updatedate ? stimateddate : updatedate;
                          maxdate = maxdate === null ? stimateddate : maxdate;
                          
                          colorsObjectsProject.blockColors.push(dataByStatus[status].blockColor);
                          colorsObjectsProject.borderColors.push(dataByStatus[status].borderColor);

                          if (validName(name) === false) {
                            name = ' ' + name;
                          }

                          parsedProjectData.push({            
                            "x": [startdate, maxdate],
                            "y": name,
                            "stimateddate": stimateddate,
                            "updatedate": updatedate,
                            "status": status,
                            "type": "Deliverable",
                            "percentage": percentage
                          });

                          parseComponents(deliverable);
                        });
                     }

                     function parseProjectData(masterProjectData){

                        var projectName = masterProjectData.name_mp;
                        var startplan_date = masterProjectData.startplan_date;
                        var endplan_date = masterProjectData.endplan_date;
                        var develop_status = masterProjectData.develop_status;
                        var actual_percentage = masterProjectData.actual_percentage;

                        colorsObjectsProject.blockColors.push(dataByStatus[develop_status].blockColor)
                        colorsObjectsProject.borderColors.push(dataByStatus[develop_status].borderColor)
                           
                        miniumLevel = 'P';
                        
                        parsedProjectData.push({            
                            "x": [startplan_date, endplan_date],
                            "y": projectName,
                            "stimateddate": endplan_date,
                            "updatedate": endplan_date,
                            "status": develop_status,
                            "type": "Project",
                            "percentage": actual_percentage
                        });

                        parseDeliverableData(masterProjectData);
                     }

                    parseProjectData(masterProjectData);

                    //Get the min and max date of the project
                    const minDate = new Date(parsedProjectData[0].x[0]);//new Date(Math.min(...parsedProjectData.map(data => new Date(data.x[0]))));
                    const maxDate = new Date(parsedProjectData[0].x[1]);//new Date(Math.max(...parsedProjectData.map(data => new Date(data.x[1]))));
                    
                    const leftPadding = miniumLevel === 'P' ? 0 : miniumLevel === 'D' ? 2 : miniumLevel === 'C' ? 4 : 6;
                    
                    const data = {    
                        datasets: [
                            {
                                label: 'On time',
                                data: parsedProjectData,
                                borderWidth: 1,
                                borderSkipped: false,
                                borderRadius: 5,
                                backgroundColor: colorsObjectsProject.blockColors,
                                borderColor: colorsObjectsProject.borderColors
                            }   
                        ]
                    };

                    const options = {
                        responsive: true,
                        maintainAspectRatio: false,
                        indexAxis: 'y',
                        layout: {
                          padding: {
                            right: 300,
                            left: leftPadding
                          }
                        },        
                        scales: {         
                            x: {
                                type: 'time',                
                                time: {
                                    unit: 'month'
                                },
                                ticks: {
                                    beginAtZero: true,
                                    font: {
                                        family: 'monospace',
                                        size: 16
                                    }
                                },
                                min: minDate,
                                max: maxDate,
                                grid: {
                                  color: 'rgba(0, 0, 0, 0.2)',
                                  display: true,
                                  stacked: true
                                }
                            },
                            y: {
                                stacked: true,
                                grid: {
                                    color: 'rgba(0, 0, 0, 0.5)',
                                    display: false
                                },
                                ticks: {
                                    beginAtZero: true,
                                    display: true,
                                    color: 'rgba(255, 255, 255, 1)',
                                    font: {
                                        family: 'monospace',
                                        size: 16
                                    }
                                }
                            }
                          },
                        plugins: {
                            zoom: {
                                zoom: {
                                    wheel: {
                                        enabled: true
                                    },
                                    pinch: {
                                        enabled: true
                                    },              
                                    mode: 'x'
                                }
                            },
                            legend: {
                                display: false
                            }                           
                        }
                    };

                    const extraDataOfGantt = {
                      afterDatasetsDraw(chart, args, pluginOptions) {
                        const { ctx, data, chartArea: { top, bottom, left, right, height }, scales: {x, y}} = chart;

                        ctx.font = '16px monospace';
                        ctx.fillStyle = 'rgba(90, 92, 105, 1)';
                        ctx.textBaseLine = 'middle';
                        ctx.textAlign = 'left';

                        var percentageData = data.datasets[0].data;

                        percentageData.forEach((datapoint, index) => {         
                          percentage = datapoint.percentage + "%";
                          name = datapoint.y;
                          type = datapoint.type;
                          status = dataByStatus[datapoint.status].status;
                          
                          spaces = '';
                          if (type === "Deliverable" || type === "Project"){
                            ctx.font = 'bolder 16px monospace';                          
                          } else {            
                            ctx.font = '16px monospace';            
                          }                       

                          var posY = y.getPixelForValue(index);
                          //ctx.fillText(spaces + type, 0, posY);
                          //ctx.fillText(name, 0, posY);
                          ctx.fillText(percentage, right + 20, posY);
                          ctx.fillText(status, right + 100, posY);
                        });
                      }
                    };

                    const drawLineToday = {
                      afterDatasetsDraw(chart, args, pluginOptions) {
                        const { ctx, data, chartArea: { top, bottom, left, right }, scales: {x, y}} = chart;

                        ctx.beginPath();
                        ctx.lineWidth = 2;
                        ctx.setLineDash([6, 6]);
                        ctx.moveTo(x.getPixelForValue(new Date()), top);
                        ctx.lineTo(x.getPixelForValue(new Date()), bottom);
                        ctx.stroke();
                      }
                    };
                    
                    function numberToBlankSpaces(number) {
                        var spaces = '';
                        for (var i = 0; i < number; i++) { 
                            spaces += ' ';
                        }
                        return spaces;
                    }
                    
                    const rewriteLabelsOfElements = {
                        afterDatasetsDraw(chart, args, pluginOptions) {
                            const { ctx, data, chartArea: { top, bottom, left, right }, scales: {x, y}} = chart;
                           
                            var elementsData = data.datasets[0].data;
                            var i = 0;
                            
                            ctx.save();
                            ctx.textAlign = 'left';
                            ctx.textBaseline = 'middle';
                            
                            chart.data.labels.forEach(function(label, index) {
                                var xPoint = 0;//xAxis.left;
                                var yPoint = y.getPixelForValue(label);
                                ctx.fillText(numberToBlankSpaces(dataByElement[elementsData[i].type].spaces) + label, xPoint, yPoint);
                                
                                i++;
                            });                            

                            ctx.restore();
                      }
                    };
                    
                    const config = {
                      type: 'bar',
                      data,
                      options: options,
                      plugins: [extraDataOfGantt, drawLineToday, rewriteLabelsOfElements]
                    };
                    
                    var myChart = Chart.getChart("ganttChart");
                    if (myChart !== undefined )
                    myChart.destroy();
                    
                    myChart = new Chart(
                      document.getElementById('ganttChart'),
                      config
                    );

                    //Variables for move the chart
                    var isDragging = false;
                    var startX;

                    myChart.canvas.addEventListener('mousedown', function(event) {
                        isDragging = true;
                        startX = event.clientX;
                    });

                    myChart.canvas.addEventListener('mousemove', (e) => {
                        if (isDragging) {
                            const { scales: { x, y } } = myChart;
                            const movementX = e.clientX - startX;

                            //Calculate the movement in terms of pixels
                            const movementPx = movementX / (x.right - x.left) * (x.max - x.min) / 5;

                            //Update the min and max values of the x axis without changing the scale
                            myChart.options.scales.x.min -= movementPx;
                            myChart.options.scales.x.max -= movementPx;

                            // Update the chart
                            myChart.update();
                          }
                        });

                    myChart.canvas.addEventListener('mouseup', function() {
                          isDragging = false;
                    });
                    
                    $("#ganttModal").modal();
                }
                
                alertAll(data);                
            }
        });
    };

    function download(filename, textInput) {
        var element = document.createElement('a');
        //element.setAttribute('href','data:text/plain;charset=utf-8, ' + encodeURIComponent(textInput));
        element.setAttribute('href', location.origin + "/storageTddm4IoTbs/" + textInput);
        element.setAttribute('download', filename);
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
    }

    $scope.listEntregablesProject = (selected_project) => {
        alert(selected_project);
    };

    /* *
     * Abrir modal para mostrar los entregables existentes 
     * para el proyecto seleccionado
     * */
    $scope.selectEntregable = () => {
        $("#modalSelectEntregable").modal();
    };
    /**
     * Abrir modal para crear un nuevo proyecto
     * */
    $scope.openInsertProject = () => {
        $("#modalCreateProject").modal();
    };
    
    let id_masterprojectShare = '';
    
    $scope.openShareProject = function (objetct_selected_project) {
        $("#modalShareProjectComplete").modal();
        id_masterprojectShare = objetct_selected_project.id_masterproject;
    };
    
    $scope.openSeeMembersInProject = function (object_selected_project) {
        $("#modalMembersInProject").modal();        
        $scope.loadMembersInProjects(object_selected_project.id_masterproject);
    };
    
     $scope.closeSeeMembersInProject = () => {
        $("#modalMembersInProject").modal('hide');        
    };

    $scope.cancelProject = () => {
        $scope.name_np = "";
        $scope.description_np = "";
        $("#modalCreateProject").modal('hide');
    };

    $scope.cancelShareProject = () => {
        $("#modalShareProjectComplete").modal('hide');
        $scope.flag_share_user_exists = true;
    };
    
    $scope.closeShareProject = () => {
        $("#modalShareProjectComplete").modal('hide');
        $scope.flag_share_user_exists = true;
    };
    
    $scope.closeGanttModal = () => {
        $("#ganttModal").modal('hide');
    };

    $scope.parseRole = function (role){
        if (role === "A") {
            return "Project facilitator";
        } else if (role === "F") {
            return "Project facilitator";
        } else if (role === "U") {
            return "Final user";
        } else if (role === "C") {
            return "Advisor";
        } else if (role === "E") {
            return "Develop team";
        }        
    };
});
