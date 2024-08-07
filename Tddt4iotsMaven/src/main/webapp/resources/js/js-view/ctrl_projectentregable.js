/* global urlWebServicies, store, swal */

app.factory('ganttDiagram', function() {
   return {
       //Color of the blocks and the borders based on the status (change if you want)
       
   };
});

app.controller("projectentregable_controller", function ($scope, $http) {
    $scope.cadena=store.session.get("projectname");
    $scope.nameentregable = null;
    $scope.namecomponent = null;
    
    $scope.selected_project = null;
    $scope.alldata_selected_project = null;
    $scope.selected_component = -1;
    $scope.selected_entregable = null;

    $scope.project_entregable = [];
    $scope.project_members_entregable = [];
    $scope.project_entregable_components = [];
    $scope.project_entregable_components_tasks = [];

    $scope.history_update_task = [];

    $scope.flag_selected_entregable = false;
    $scope.flag_selected_component = false;
    $scope.flag_selected_task = false;

    $scope.project_entregable_component_selected = null;
    $scope.entregable_component_task_selected = null;

    $scope.actionToModal = null;
    $scope.todayDate = null;
    $scope.dataError = "Error the total percentage is bigger than 100%";
    $scope.selecteditem=null;

    $scope.flagShowingAlert = false;
    
    $scope.idcomponenteSelectedViewTask=null;
    $scope.pathProj=null;
    
    $scope.loadComponentsEntregable =null;
    $scope.rolcomponent = "";
    $scope.rolentregable = "";
    $scope.rolusuario = store.session.get("user_tddm4iotbs");

    
    $scope.get_task_for_component_data=null;
    $scope.get_components_for_deliverate_data=null;
    
    
    $(document).ready(function () {
        //console.log($scope.rolusuario);
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("My Projects - Deliverables", true);
        $scope.updateInformationElements();
        $scope.flagShowingAlert = true;
        $scope.selecteditem=null;

        $scope.todayDate = new Date();

        $scope.selected_project = window.sessionStorage.getItem("id_project");
        $scope.loadEntregablesProject();
        $scope.selectProjectMaster();
        $scope.selected_component = 1;
        $scope.flag_selected_entregable = true;
        $scope.flag_selected_component = false;
        $scope.flag_selected_task = false;
        //$scope.selectEntregableMembers();   
        $scope.pathProj=store.session.get("projectpath");
        $scope.appPage.Select = "entregablesproject";
        
    });

    $scope.executeLoadEntregables = () => {
        $scope.loadEntregablesProject();
        $scope.selectProjectMaster();
        $('#progress_bar_master_project').load();        
    };

    $scope.executeLoadComponents = () => {
        $scope.loadComponentsEntregable();
        //$scope.loadComponentEntregableSelected($scope.project_entregable_component_selected.id_entregable_component);
        $scope.getSelectedEntregableProject($scope.project_entregable_component_selected.id_entregable);

        $('#progress_bar_entregable_component').load();
        $('#progress_bar_project_entregable').load();
    };
    
    $scope.getProjectProperty = () => {        
        var dataUser = store.session.get("user_tddm4iotbs");
        if ($scope.selected_project !== undefined && $scope.selected_project !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/getProjectProperty',
                data: JSON.stringify({
                    "idmasterproject": $scope.selected_project,
                    "emailperson": dataUser.email_person
                }),
                success: function (data) {                    
                    window.sessionStorage.setItem("permiProjectProperty", data);
                }
            });
        }
    };

    $scope.loadEntregablesProject = () => {
        var dataUser = store.session.get("user_tddm4iotbs");       
        if ($scope.selected_project !== undefined && $scope.selected_project !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'entregable/selectEntregables',
                data: JSON.stringify({
                    "idmasterproject": $scope.selected_project,
                    "type": 4,
                    "email": dataUser.email_person
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    // console.log(data);
                    $scope.$apply(function () {
                        $scope.project_entregable = data.data;
                        swal.close();
                        
                    });                                        
                    if ($scope.flagShowingAlert === false)
                        alertAll(data);
                    
                    $scope.getProjectProperty();
                }
            });
        }
    };

    $scope.loadComponentsEntregable = () => {
        var dataUser = store.session.get("user_tddm4iotbs");        
        if ($scope.selected_component !== undefined && $scope.selected_component !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'entregableComponents/getComponentas',
                data: JSON.stringify({
                    "id_entregable": $scope.selected_component,
                    "type": 4,
                    "email": dataUser.email_person
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    $scope.$apply(function () {
                        $scope.project_entregable_components = data.data;
                      
                        // console.log(data);
                    });
                    swal.close();
                    //alertAll(data.information);
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        }
    };

    $scope.loadComponentEntregableSelected = (id_component) => {
        if (id_component !== undefined && id_component !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'entregableComponents/getComponentas',
                data: JSON.stringify({
                    "id_entregable": id_component,
                    "type": 3
                }),
                beforeSend: function (xhr) {
                    //loading();
                },
                success: function (data) {
                    $scope.$apply(function () {
                        $scope.project_entregable_component_selected = data.data[0];
                    });                    
                    //alertAll(data.information);                   
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        }
    };

    $scope.selectEntregableMembers = (idtype, idElement) => {
        if ($scope.selected_component !== undefined && $scope.selected_component !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'members/getMembers',
                data: JSON.stringify({
                    "id_project_task": idElement,
                    "type": idtype
                }),
                success: function (data) {
                    console.log("memberssssssssssssssss");
                    console.log(data.data);
                    $scope.$apply(function () {
                        $scope.project_members_entregable = data.data;
                    });                    
                    alertAll(data.information);
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        }
    };

    $scope.selectProjectMaster = function () {
        if ($scope.selected_project !== undefined && $scope.selected_project !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/getProject',
                data: JSON.stringify({
                    "idmasterproject": $scope.selected_project
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    
                    $scope.$apply(() => {
                        $scope.alldata_selected_project = {                        
                            "idproj": data[0]["id_masterproject"],
                            "creationdate_mp": data[0]["creationdate_mp"],
                            "updatedate_mp": data[0]["updatedate_mp"],
                            "stimateddate_mp": data[0]["stimateddate_mp"],
                            "name_mp": data[0]["name_mp"],
                            "path_mp": data[0]["path_mp"],
                            "code_mp": data[0]["code_mp"],
                            "description_mp": data[0]["description_mp"],
                            "permit_mp": data[0]["permit_pm"],
                            "actual_percentage": data[0]["actual_percentage"],
                            "develop_status": data[0]["develop_status"]
                        };
                    });
                    swal.close();
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        }        
    };

    $scope.getSelectedEntregableProject = (id_entregable) => {        
        if (id_entregable !== undefined && id_entregable !== null) {
            $.ajax({
                method: "POST",
                type: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'entregable/selectEntregables',
                data: JSON.stringify({
                    "idmasterproject": id_entregable,
                    "type": 3
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    $scope.$apply(function () {
                        $scope.selected_entregable = data.data[0];                        
                        alertAll(data);
                        swal.close();
                    });
                }
            });
        }
    };
    
    $scope.typeMemberViewSelected = -1;
    
    $scope.openModalViewMembers = (element, type) => {        
        $scope.typeMemberViewSelected = type;
        if(type === 0){
            document.getElementById("modal_title_view_members_candtask").textContent = element.name_entregable + ": Collaborators";
            $scope.selected_entregable = element;            
            $scope.selectEntregableMembers(type, element.id_entregable);
            $("#modalViewMembersdeliverable").modal();
            return;
        }
        if(type === 1){
            document.getElementById("modal_title_view_members_candtask").textContent = element.name_component + ": Collaborators";
            $scope.project_entregable_component_selected = element;
            $scope.selectEntregableMembers(type, element.id_entregable_component);
            $("#modalViewMembersComponent").modal();
            return;
        }
        if(type === 2){
            document.getElementById("modal_title_view_members").textContent = element.name_task + ": Members";
            $scope.entregable_component_task_selected = element;
            $scope.selectEntregableMembers(type, element.id_task);
            $("#modalViewMembers").modal();
            return;
        }
    };

    $scope.openModalViewMembers2 = (element, type) => {        
        $scope.typeMemberViewSelected = type;
        if(type === 0){
            document.getElementById("modal_title_view_members").textContent = element.name_entregable + ": Collaborators";
            
            $scope.selected_entregable = element;            
            $scope.selectEntregableMembers(type, element.id_entregable);          
        }
        if(type === 1){
            document.getElementById("modal_title_view_members").textContent = element.name_component + ": Collaborators";
            
            //document.getElementById("modal_members_role_show_label").visibility = "hidden";
            //document.getElementById("modal_members_role_show_select").visibility = "hidden";
            
            $scope.project_entregable_component_selected = element;
            $scope.selectEntregableMembers(type, element.id_entregable_component);
        }
        if(type === 2){
            document.getElementById("modal_title_view_members").textContent = element.name_task + ": Collaborators";
            $scope.entregable_component_task_selected = element;
            $scope.selectEntregableMembers(type, element.id_task);
        }
        

        $("#modalViewMembers").modal();
    };

    $scope.selectProjectEntregable = (id_entregable, entregableSelected,nameentregable) => {
        $scope.nameentregable = nameentregable;
        $scope.tittlereport(2);
        $scope.selected_component = id_entregable;
        $scope.flag_selected_component = true;
        $scope.loadComponentsEntregable();
        $scope.loadComponentsEntregable();
        $scope.selected_entregable = entregableSelected;
    };

    $scope.newEntregable = ((form) => {        
        if (form.$valid) {
            let
                    id_masterproject = form.id_masterproject.$$attr.value,
                    name_entregable = form.name_entregable.$viewValue,
                    description_entregable = form.description_entregable.$viewValue,
                    status_entregable = form.status_entregable.$viewValue,
                    path_entregable = form.path_entregable.$$attr.value,
                    creationdate_entregable = "CreationDate",
                    updatedate_entregable = "updateDate",
                    //stimateddate_entregable=form.stimateddate_entregable.$viewValue,
                    stimateddate_entregable = document.getElementById("stimateddate_entregable_create").value,
                    finishdate_entregable = "Finished",
                    prioritylevel_entregable = form.prioritylevel_entregable.$viewValue,
                    base_percentage_entregable = form.base_percentage_entregable.$viewValue,
                    startdate_entregable = form.startdate_entregable.$viewValue;

            let dateone = new Date(startdate_entregable);
            let datetwo = new Date(stimateddate_entregable);
            if ((datetwo - dateone) / (1000 * 60 * 60 * 24) < 0) {
                let jsonError = {status: 3, information: "The dates are not valid.", data: "[]"};
                alertAll(jsonError);
                return;
            }

            var dataUser = store.session.get("user_tddm4iotbs");
            if (dataUser !== undefined && dataUser !== null) {
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + 'entregable/saveEntregable',
                    data: JSON.stringify({
                        "idmasterproject": $scope.selected_project,
                        "name_entregable": name_entregable,
                        "description_entregable": description_entregable,
                        "status_entregable": status_entregable,
                        "stimateddate_entregable": stimateddate_entregable,
                        "path_entregable": path_entregable,
                        "prioritylevel_entregable": prioritylevel_entregable,
                        "base_percentage_entregable": base_percentage_entregable,
                        "actual_percentage_entregable": 100,
                        "develop_status_entregable": "P",
                        "path_master_project": $scope.alldata_selected_project.path_mp,
                        "startdate_entregable": startdate_entregable,
                        "emailperson": dataUser.email_person
                    }),
                    beforeSend: function (xhr) {
                        loading();
                    },
                    success: function (data) {
                        swal.close();
                        $scope.dataError = data.information;                        

                        if (data.information === 'Error the total sum of the percentages in entregables is bigger than 100%') {
                            $scope.openErrorModal();
                        } else {
                            $scope.$apply(() => {
                                $scope.loadEntregablesProject();    
                                
                                $("#modalCreateEntre").modal('hide');
                                swal.close();

                                $scope.selectProjectMaster();
                                $('#progress_bar_master_project').load();
                                
                            });
                            alertAll(data);
                            $scope.formreset(form);
                            delete_forms_values();
                        }
                    },
                    error: function (objXMLHttpRequest) {
                        // console.log("error: ", objXMLHttpRequest);
                    }
                });
            } else {
                location.href = "index";
            }
        }
    });

    $scope.updateEntregable = ((form) => {        
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");            
            let
                    id_entregable = $scope.selected_entregable.id_entregable,
                    name_entregable = form.name_entregable_edit.$viewValue,
                    description_entregable = form.description_entregable_edit.$viewValue,
                    status_entregable = form.status_entregable_edit.$viewValue,
                    path_entregable = form.path_entregable_edit.$$attr.value,
                    stimateddate_entregable = form.stimateddate_entregable_edit.$viewValue,
                    prioritylevel_entregable = form.prioritylevel_entregable_edit.$viewValue;
            base_percentage_entregable = form.base_percentage_entregable_edit.$viewValue;
            startdate_entregable = form.startdate_entregable_edit.$viewValue;
                        
            var dataUser = store.session.get("user_tddm4iotbs");
            if (dataUser !== undefined && dataUser !== null) {
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + 'entregable/updateEntregable',
                    data: JSON.stringify({
                        "id_entregable": id_entregable,
                        "id_masterproject": $scope.selected_entregable.id_masterproject,
                        "name_entregable": name_entregable,
                        "description_entregable": description_entregable,
                        "status_entregable": status_entregable,
                        "stimateddate_entregable": stimateddate_entregable,
                        "path_entregable": path_entregable,
                        "prioritylevel_entregable": prioritylevel_entregable,
                        "base_percentage_entregable": base_percentage_entregable,
                        "actual_percentage_entregable": $scope.selected_entregable.actual_percentage_entregable,
                        "startdate_entregable": startdate_entregable,
                        "emailperson":dataUser.email_person
                    }),
                    beforeSend: function (xhr) {
                        loading();
                    },
                    success: function (data) {
                        swal.close();                        

                        //alert("hecho");
                        $scope.$apply(() => {
                            $scope.loadEntregablesProject();
                            $scope.selectProjectMaster();
                            $('#progress_bar_master_project').load();
                        });

                        alertAll(data);

                        $("#modalEditEntre").modal('hide');
                    },
                    error: function (objXMLHttpRequest) {
                        // console.log("error: ", objXMLHttpRequest);
                    }
                });
            } else {
                location.href = "index";
            }
        }
    });


    $scope.newComponent = (form) => {
       var dataUser = store.session.get("user_tddm4iotbs"); 
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            if (dataUser !== undefined && dataUser !== null) {
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + 'entregableComponents/saveEntregableTask',
                    data: JSON.stringify({
                        "id_entregable": $scope.selected_component,
                        "name_component": form.name_component.$viewValue,
                        "description_component": form.description_component.$viewValue,
                        "status_component": form.status_component.$viewValue,
                        "visibility_component": "1",
                        "stimateddate_component": form.stimateddate_component.$viewValue,
                        "finishdate_component": "",
                        "path_component": form.name_component.$viewValue,
                        "base_percentage_component": form.base_percentage_component.$viewValue,
                        "actual_percentage_component": 100,
                        "develop_status_component": "P",
                        "path_master_project": $scope.alldata_selected_project.path_mp,
                        "path_project_entregable": $scope.selected_entregable.path_entregable,
                        "startdate_component": form.startdate_component.$viewValue,
                        "emailperson":dataUser.email_person,
                        "id_tcejorpmaster": $scope.selected_project
                    }),
                    beforeSend: function (xhr) {
                        loading();
                    },
                    success: function (data) {

                        $scope.$apply(() => {
                            $scope.loadComponentsEntregable();
                            $("#modalCreateComponent").modal('hide');                            
                            swal.close();
                            $scope.getSelectedEntregableProject($scope.selected_component);
                            $('#progress_bar_project_entregable').load();
                        });

                        alertAll(data);
                        delete_forms_values();
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


    $scope.returnToEntregables = () => {
        $scope.flag_selected_entregable = false;
    };


    $scope.closeModalViewMembers = () => {
        $("#modalViewMembers").modal('hide');
    };
    
     $scope.closeModalViewMembersTask = () => {
        $("#modalViewMembers").modal('hide');
    };
    
    $scope.closeModalViewMembersComponent = () => {
        $("#modalViewMembersComponent").modal('hide');
    };
    
    $scope.closeModalViewMembersDeliverable = () => {
        $("#modalViewMembersdeliverable").modal('hide');
    };

    $scope.openEntregableModal = (actionModal, entregable_selected) => {
        $scope.actionToModal = actionModal;
        if (actionModal === 'edit') {
            $scope.selected_entregable = entregable_selected;
        }
        $("#modalCreateEntre").modal();
    };

    $scope.openEntregableEditModal = (actionModal, entregable_selected, rol) => {
        $scope.actionToModal = actionModal;
        if (actionModal === 'edit') {
            $scope.selected_entregable = entregable_selected;

            $scope.getSelectedEntregableProject(entregable_selected.id_entregable);
            $scope.name_entregable_edit = $scope.selected_entregable.name_entregable;
            $scope.id_entregable_edit = $scope.selected_entregable.id_entregable;
            $scope.id_masterproject_edit = $scope.selected_entregable.id_masterproject;
            $scope.description_entregable_edit = $scope.selected_entregable.description_entregable;
            $scope.stimateddate_entregable_edit = new Date($scope.selected_entregable.stimateddate_entregable);
            $scope.status_entregable_edit = $scope.selected_entregable.status_entregable;
            $scope.prioritylevel_entregable_edit = $scope.selected_entregable.prioritylevel_entregable.toString();
            $scope.path_entregable_edit = $scope.selected_entregable.path_entregable;
            $scope.base_percentage_entregable_edit = $scope.selected_entregable.base_percentage_entregable;
            $scope.startdate_entregable_edit = new Date($scope.selected_entregable.startdate_entregable);
            $scope.rolentregable = rol;
        }
        $("#modalEditEntre").modal();
    };

    $scope.closeEntregableEditModal = () => {
        $("#modalEditEntre").modal('hide');
    };


    $scope.closeEntregableModal = () => {
        $("#modalCreateEntre").modal('hide');
    };

    $scope.returnToMyProjects = () => {
        window.location = "app.html#!/myprojects";
    };

    $scope.openCreateTask = function () {
        $("#modalCreateComponent").modal();
    };

    $scope.closeCreateTask = function () {
        $("#modalCreateComponent").modal('hide');
    };

    $scope.closeErrorModal = function () {
        $("#modalError").modal('hide');
    };

    $scope.openErrorModal = function () {
        $("#modalError").modal();
    };


    $scope.updateComponent = (form) => {
       
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + "entregableComponents/updateEntregableComponent",
                data: JSON.stringify({
                    'id_entregable': $scope.project_entregable_component_selected.id_entregable,
                    'name_component': form.name_component_edit.$viewValue,
                    'description_component': form.description_component_edit.$viewValue,
                    'status_component': form.status_component_edit.$viewValue,
                    'stimateddate_component': form.stimateddate_component_edit.$viewValue,
                    'base_percentage_component': form.base_percentage_component_edit.$viewValue,
                    'identregablecomponent': $scope.project_entregable_component_selected.id_entregable_component,
                    'actual_percentage_component': $scope.project_entregable_component_selected.actual_percentage_component,
                    'startdate_component': form.startdate_component_edit.$viewValue,
                    "emailperson":dataUser.email_person,
                    "id_tcejorpmaster": $scope.selected_entregable.id_masterproject
                }),
                beforeSend: function () {
                    loading();
                },
                success: function (data) {
                    $scope.$apply(() => {
                        $scope.loadComponentsEntregable();
                        $scope.closeModalEditComponent();
                        $scope.getSelectedEntregableProject($scope.project_entregable_component_selected.id_entregable);
                        $('#progress_bar_project_entregable').load();

                        swal.close();
                    });
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error", objXMLHttpRequest);
                }
            });
        }
    };

    $scope.openModalTaskEditProccess = (task_selected) => {
        $scope.entregable_component_task_selected = task_selected;
        $scope.actual_percentage_task_before_edit = $scope.entregable_component_task_selected.actual_percentage_task;
        //alert(component_selected);
        $("#modalTaskEditProccess").modal();
    };

    $scope.closeModalTaskEditProccess = () => {
        $("#modalTaskEditProccess").modal('hide');
    };

    $scope.openModalEditComponent = (component_selected, rol) => {
        $scope.project_entregable_component_selected = component_selected;

        if ($scope.project_entregable_component_selected !== null && $scope.project_entregable_component_selected !== undefined) {
            $scope.name_component_edit = $scope.project_entregable_component_selected.name_component;
            $scope.description_component_edit = $scope.project_entregable_component_selected.description_component;
            $scope.status_component_edit = $scope.project_entregable_component_selected.status_component;
            $scope.base_percentage_component_edit = $scope.project_entregable_component_selected.base_percentage_component;
            $scope.stimateddate_component_edit = new Date($scope.project_entregable_component_selected.stimateddate_component);
            $scope.path_component_edit = $scope.project_entregable_component_selected.base_percentage_component;
            $scope.startdate_component_edit = new Date($scope.project_entregable_component_selected.startdate_component);
            $scope.rolcomponent = rol;
            $("#modalEditComponent").modal();
        }
    };

    $scope.closeModalEditComponent = () => {
        $("#modalEditComponent").modal('hide');
    };

    $scope.updateActualPercentageTask = (form) => {
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + "componentTask/updateComponentTask",
                data: JSON.stringify({
                    'id_task': $scope.entregable_component_task_selected.id_task,
                    'id_component': $scope.entregable_component_task_selected.id_component,
                    'name_task': $scope.entregable_component_task_selected.name_task,
                    'description_task': $scope.entregable_component_task_selected.description_task,
                    'status_task': $scope.entregable_component_task_selected.status_task,
                    'path_task': $scope.entregable_component_task_selected.path_task,
                    'base_percentage_task': $scope.entregable_component_task_selected.base_percentage_task,
                    'actual_percentage_task': form.actual_percentage_task_after_edit.$viewValue,
                    'stimateddate_task': $scope.entregable_component_task_selected.stimateddate_task,
                    'develop_status_task': $scope.entregable_component_task_selected.develop_status_task,
                    "path_master_project": $scope.alldata_selected_project.path_mp,
                    "path_project_entregable": $scope.selected_entregable.path_entregable,
                    'path_component': $scope.project_entregable_component_selected.path_component,
                    'description_update_task': form.description_task_after_edit.$viewValue,
                    'startdate_task': $scope.entregable_component_task_selected.startdate_task,
                    "emailperson":dataUser.email_person,
                    "id_masterproject": $scope.selected_project
                }),
                beforeSend: function () {
                    loading();
                },
                success: function (data) {                    
                    $scope.loadComponentTasks();
                    $scope.closeModalTaskEditProccess();

                    alertAll(data);
                    swal.close();

                    $scope.loadComponentEntregableSelected($scope.project_entregable_component_selected.id_entregable_component);
                    $('#progress_bar_component_task').load();
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error", objXMLHttpRequest);
                }
            });
        }
    };


    $scope.viewTasks = (component_selected,namecomponent) => {
        $scope.namecomponent=namecomponent;
        $scope.tittlereport(3);
        $scope.project_entregable_component_selected = component_selected;
        $scope.idcomponenteSelectedViewTask=component_selected.id_entregable_component;
        $scope.flag_selected_task = true;
        $scope.loadComponentTasks();
        $scope.loadMembersTask();
    };

    $scope.closeViewTasks = () => {
        $scope.flag_selected_task = false;
    };

    $scope.openEditTaskModal = (taskEdit) => {
        $scope.entregable_component_task_selected = taskEdit;

        $scope.base_percentage_task_edit = $scope.entregable_component_task_selected.base_percentage_task;
        $scope.stimateddate_task_edit = new Date($scope.entregable_component_task_selected.stimateddate_task);
        $scope.path_task_edit = $scope.entregable_component_task_selected.path_task;
        $scope.status_task_edit = $scope.entregable_component_task_selected.status_task;
        $scope.description_task_edit = $scope.entregable_component_task_selected.description_task;
        $scope.name_task_edit = $scope.entregable_component_task_selected.name_task;
        $scope.startdate_task_edit = new Date($scope.entregable_component_task_selected.startdate_task);

        $("#modalUpdateComponentTask").modal();
    };

    $scope.closeEditTaskModal = () => {
        $("#modalUpdateComponentTask").modal('hide');
    };

    $scope.openCreateComponentTask = () => {
        $("#modalCreateTask").modal();
    };

    $scope.closeCreateComponentTask = () => {
        $("#modalCreateTask").modal('hide');
    };


    $scope.loadMembersTask = () =>
    {
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + "members/selectMembersentregable",
            data: JSON.stringify({
                'id_entregable': $scope.selected_project
            }),
            error: function (objXMLHttpRequest)
            {
                console.log("error", objXMLHttpRequest);
            },
            success: function (data) {
                console.log("se cargaron los datos de los miembros");
                console.log(data.data);
                $scope.memberstask=data.data;
            }
        });            
    };

    $scope.newTask = (form) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (form.$valid) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + "componentTask/insertComponentTask",
                data: JSON.stringify({
                    'id_component': $scope.project_entregable_component_selected.id_entregable_component,
                    'name_task': form.name_task_create.$viewValue,
                    'description_task': form.description_task_create.$viewValue,
                    'status_task': form.status_task_create.$viewValue,
                    'path_task': form.path_task_create.$viewValue,
                    'base_percentage_task': form.base_percentage_task_create.$viewValue,
                    'actual_percentage_task': 100,
                    'stimateddate_task': form.stimateddate_task_create.$viewValue,
                    'develop_status_task': "P",
                    "path_master_project": $scope.alldata_selected_project.path_mp,
                    "path_project_entregable": $scope.selected_entregable.path_entregable,
                    'path_component': $scope.project_entregable_component_selected.path_component,
                    'startdate_task': form.startdate_task.$viewValue,
                    'emailperson':dataUser.email_person,
                    "id_masterproject": $scope.selected_project
                }),
                beforeSend: function () {
                    loading();
                },
                success: function (data) {                    
                    $scope.loadComponentTasks();                    
                    $scope.closeCreateComponentTask();
                    alertAll(data);                    
                    
                    swal.close();
                     delete_forms_values();
                    $scope.loadComponentEntregableSelected($scope.project_entregable_component_selected.id_entregable_component);
                    $('#progress_bar_component_task').load();
                   
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error", objXMLHttpRequest);
                }
            });
        }
    };

    $scope.loadComponentTasks = () => {
        var dataUser = store.session.get("user_tddm4iotbs");        
        if ($scope.selected_component !== undefined && $scope.selected_component !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'componentTask/selectTasks',
                data: JSON.stringify({
                    "id_element": $scope.project_entregable_component_selected.id_entregable_component,
                    "type": 3,
                    "email": dataUser.email_person
                }),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    
                    $scope.$apply(function () {
                        $scope.project_entregable_components_tasks = data.data;
                    });                    
                    
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        }
    };

    $scope.updateTask = (form) => {        
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + "componentTask/updateComponentTask",
                data: JSON.stringify({
                    'id_task': $scope.entregable_component_task_selected.id_task,
                    'id_component': $scope.entregable_component_task_selected.id_component,
                    'name_task': form.name_task_edit.$viewValue,
                    'description_task': form.description_task_edit.$viewValue,
                    'status_task': form.status_task_edit.$viewValue,
                    'path_task': $scope.entregable_component_task_selected.path_task,
                    'base_percentage_task': form.base_percentage_task_edit.$viewValue,
                    'actual_percentage_task': $scope.entregable_component_task_selected.actual_percentage_task,
                    'stimateddate_task': form.stimateddate_task_edit.$viewValue,
                    'develop_status_task': "P",
                    "path_master_project": $scope.alldata_selected_project.path_mp,
                    "path_project_entregable": $scope.selected_entregable.path_entregable,
                    'path_component': $scope.project_entregable_component_selected.path_component,
                    'startdate_task': form.startdate_task_edit.$viewValue,
                    "emailperson":dataUser.email_person,
                    "id_masterproject": $scope.selected_project
                }),
                beforeSend: function () {
                    loading();
                },
                success: function (data) {                    
                    $scope.loadComponentTasks();
                    $scope.closeEditTaskModal();
                    swal.close();
                    alertAll(data);

                    $scope.loadComponentEntregableSelected($scope.project_entregable_component_selected.id_entregable_component);
                    $('#progress_bar_component_task').load();
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error", objXMLHttpRequest);
                }
            });
        }
    };

    $scope.openModalViewHistoryTask = (task) => {
        $scope.entregable_component_task_selected = task;
        $scope.viewUpdateHistory();

        $("#modalViewHistoryTask").modal();
    };

    $scope.closeModalViewHistoryTask = () => {
        $("#modalViewHistoryTask").modal('hide');
    };

    $scope.viewUpdateHistory = () => {
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + "componentTask/viewUpdateHistory",
            data: JSON.stringify({
                'id_task': $scope.entregable_component_task_selected.id_task,
                'path_task': $scope.entregable_component_task_selected.path_task,
                "path_master_project": $scope.alldata_selected_project.path_mp,
                "path_project_entregable": $scope.selected_entregable.path_entregable,
                'path_component': $scope.project_entregable_component_selected.path_component
            }),
            beforeSend: function () {
                loading();
            },
            success: function (data) {
                for (let i = 0; i < data.data.dataUpdate.length; i++) {
                    data.data.dataUpdate[i].update_date = data.data.dataUpdate[i].update_date.toString().replaceAll("/", "-");
                }
                $scope.history_update_task = data.data.dataUpdate;
                
                $scope.loadComponentTasks();
                swal.close();
            },
            error: function (objXMLHttpRequest) {
                // console.log("error", objXMLHttpRequest);
            }
        });
    };


    $scope.openModalEntregablesBasePercentage = () => {
        $("#modalEntregablesBasePercentage").modal();
    };

    $scope.closeModalEntregablesBasePercentage = () => {
        $("#modalEntregablesBasePercentage").modal('hide');
    };

    $scope.openModalComponentBasePercentage = () => {
        $("#modalComponentsBasePercentage").modal();
    };

    $scope.closeModalComponentBasePercentage = () => {
        $("#modalComponentsBasePercentage").modal('hide');
    };

    $scope.openModalTaskBasePercentage = () => {

        $("#modalTasksBasePercentage").modal();
    };

    $scope.closeModalTaskBasePercentage = () => {
        $("#modalTasksBasePercentage").modal('hide');
    };


    $scope.getSumBasePercentages = (type) => {
        let totalSum = 0;
        if (type === 1) {
            for (let i = 0; i < $scope.project_entregable.length; i++) {
                totalSum += $scope.project_entregable[i].base_percentage_entregable;
            }
        }
        if (type === 2) {
            for (let i = 0; i < $scope.project_entregable_components.length; i++) {
                totalSum += $scope.project_entregable_components[i].base_percentage_component;
            }
        }
        if (type === 3) {
            for (let i = 0; i < $scope.project_entregable_components_tasks.length; i++) {
                totalSum += $scope.project_entregable_components_tasks[i].base_percentage_task;
            }
        }

        return totalSum;
    };

    $scope.updateBasePercentagesGeneralEntregable = (form) => {
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            myArray = document.getElementsByName("base_percentage_entregable_edit_all");            
            for (let i = 0; i < $scope.project_entregable.length; i++) {
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + 'entregable/updateEntregable',
                    data: JSON.stringify({
                        "idmasterproject": $scope.project_entregable[i].id_masterproject,
                        "id_masterproject": $scope.project_entregable[i].id_masterproject,
                        "id_entregable": $scope.project_entregable[i].id_entregable,
                        "name_entregable": $scope.project_entregable[i].name_entregable,
                        "description_entregable": $scope.project_entregable[i].description_entregable,
                        "status_entregable": $scope.project_entregable[i].status_entregable,
                        "stimateddate_entregable": $scope.project_entregable[i].stimateddate_entregable,
                        "path_entregable": $scope.project_entregable[i].path_entregable,
                        "prioritylevel_entregable": $scope.project_entregable[i].prioritylevel_entregable,
                        "base_percentage_entregable": myArray[i].value,
                        "develop_status_entregable": $scope.project_entregable[i].develop_status_entregable,
                        "actual_percentage_entregable": $scope.project_entregable[i].actual_percentage_entregable,
                        "startdate_entregable": $scope.project_entregable[i].startdate_entregable,
                        "emailperson": dataUser.email_person
                    }),
                    beforeSend: function () {
                        loading();
                    },
                    success: function (data) {
                        $scope.loadEntregablesProject();
                        $scope.closeModalEntregablesBasePercentage();

                        alertAll(data);

                        $scope.selectProjectMaster();
                        $('#progress_bar_master_project').load();
                        swal.close();
                    },
                    error: function (objXMLHttpRequest) {
                        // console.log("error", objXMLHttpRequest);
                    }
                });
            }
        }
    };

    $scope.updateBasePercentagesGeneralComponents = (form) => {
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            myArray = document.getElementsByName("base_percentage_component_edit_all");
            for (let i = 0; i < $scope.project_entregable_components.length; i++) {
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + "entregableComponents/updateEntregableComponent",
                    data: JSON.stringify({
                        "identregablecomponent": $scope.project_entregable_components[i].id_entregable_component,
                        "id_entregable": $scope.project_entregable_components[i].id_entregable,
                        "name_component": $scope.project_entregable_components[i].name_component,
                        "description_component": $scope.project_entregable_components[i].description_component,
                        "status_component": $scope.project_entregable_components[i].status_component,
                        "stimateddate_component": $scope.project_entregable_components[i].stimateddate_component,
                        "path_component": $scope.project_entregable_components[i].path_component,
                        "base_percentage_component": myArray[i].value,
                        "develop_status_component": $scope.project_entregable_components[i].develop_status_component,
                        "actual_percentage_component": $scope.project_entregable_components[i].actual_percentage_component,
                        "startdate_component": $scope.project_entregable_components[i].startdate_component,
                        "emailperson": dataUser.email_person,
                        "id_tcejorpmaster": $scope.selected_entregable.id_masterproject
                    }),
                    beforeSend: function () {
                        loading();
                    },
                    success: function (data) {                        
                        $scope.loadComponentsEntregable();
                        $scope.closeModalComponentBasePercentage();
                        $scope.loadEntregablesProject();
                        $scope.getSelectedEntregableProject($scope.project_entregable_components[i].id_entregable);
                        swal.close();
                        $('#progress_bar_project_entregable').load();
                    },
                    error: function (objXMLHttpRequest) {
                        // console.log("error", objXMLHttpRequest);
                    }
                });
            }
        }
    };

    $scope.updateBasePercentagesGeneralTasks = (form) => {
        if (form.$valid) {
            var dataUser = store.session.get("user_tddm4iotbs");
            myArray = document.getElementsByName("base_percentage_task_edit_all");
            for (let i = 0; i < $scope.project_entregable_components_tasks.length; i++) {
                $.ajax({
                    method: "POST",
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    url: urlWebServicies + "componentTask/updateComponentTask",
                    data: JSON.stringify({
                        'id_task': $scope.project_entregable_components_tasks[i].id_task,
                        'id_component': $scope.project_entregable_components_tasks[i].id_component,
                        'name_task': $scope.project_entregable_components_tasks[i].name_task,
                        'description_task': $scope.project_entregable_components_tasks[i].description_task,
                        'status_task': $scope.project_entregable_components_tasks[i].status_task,
                        'path_task': $scope.project_entregable_components_tasks[i].path_task,
                        'base_percentage_task': myArray[i].value,
                        'actual_percentage_task': $scope.project_entregable_components_tasks[i].actual_percentage_task,
                        'stimateddate_task': $scope.project_entregable_components_tasks[i].stimateddate_task,
                        'develop_status_task': $scope.project_entregable_components_tasks[i].develop_status_task,
                        'startdate_task': $scope.project_entregable_components_tasks[i].startdate_task,
                        "path_master_project": $scope.alldata_selected_project.path_mp,
                        "path_project_entregable": $scope.selected_entregable.path_entregable,
                        'path_component': $scope.project_entregable_component_selected.path_component,
                        "emailperson":dataUser.email_person,
                        "id_masterproject": $scope.selected_project
                    }),
                    beforeSend: function () {
                        loading();
                    },
                    success: function (data) {                        
                        $scope.loadComponentTasks();
                        $scope.closeModalTaskBasePercentage();
                        swal.close();                        
                        
                        $scope.loadComponentEntregableSelected($scope.project_entregable_component_selected.id_entregable_component);
                        $('#progress_bar_component_task').load();
                    },
                    error: function (objXMLHttpRequest) {
                        // console.log("error", objXMLHttpRequest);
                    }
                });
            }
        }
    };

    $scope.getDevelopStatus = (letter) => {
        
        if (letter === 'NOI') {
            return {data: [{"Status": 'No initialized', "Class": 'bg-st-noi', "TextClass": 'bg-txt-noi'}]};
        }
        if (letter === 'IOT') {
            return {data: [{"Status": 'Intialized on time', "Class": 'bg-st-iot', "TextClass": 'bg-txt-iot'}]};
        }
        if (letter === 'IOO') {
            return {data: [{"Status": 'Intialized out of time', "Class": 'bg-st-ioo', "TextClass": 'bg-txt-ioo'}]};
        }
        if (letter === 'IPI') {
            return {data: [{"Status": 'In proccess', "Class": 'bg-st-ipi', "TextClass": 'bg-txt-ipi'}]};
        }
        if (letter === 'IPO') {
            return {data: [{"Status": 'Delayed', "Class": 'bg-st-ipo', "TextClass": 'bg-txt-ipo'}]};
        }
        if (letter === 'FOT') {
            return {data: [{"Status": 'Finished on time', "Class": 'bg-st-fot', "TextClass": 'bg-txt-fot'}]};
        }
        if (letter === 'FOO') {
            return {data: [{"Status": 'Finished out of time', "Class": 'bg-st-foo', "TextClass": 'bg-txt-foo'}]};
        }
        if (letter === 'DNI') {
            return {data: [{"Status": 'Delayed and no intialized', "Class": 'bg-st-dni', "TextClass": 'bg-txt-dni'}]};
        }
        
        /*
        if (letter === 'P') {
            return {data: [{"Status": 'In process', "Class": 'bg-info'}]};
        }
        if (letter === 'R') {
            return {data: [{"Status": 'Delayed', "Class": 'bg-danger'}]};
        }
        if (letter === 'I') {
            return {data: [{"Status": 'In time', "Class": 'bg-warning'}]};
        }
        if (letter === 'F') {
            return {data: [{"Status": 'Finish', "Class": 'bg-success'}]};
        }
        */
        return {data: [{"Status": "Not found", "Class": 'bg-st-dni', "TextClass": 'bg-txt-dni'}]};
    };

    $scope.checkValidDate = (inputStartDate, inputFinishSDate) => {
        let start = document.getElementById(inputStartDate);
        let endS = document.getElementById(inputFinishSDate);
        endS.min = start.value;
    };


    $scope.openCreateMembers = function (task) {
        $scope.selected_Task = task.id_task;
        $scope.entregable_component_task_selected = task;
        $("#addMembers").modal();
    };

    $scope.closeCreateMembers = function () {
        $("#addMembers").modal('hide');        
    };
    
    $scope.openEditMembers = function (task) {    
         $scope.entregable_component_task_selected = task;
        $("#modalEditMembersTask").modal();
    };

    $scope.closeEditMembers = function () {
        $("#modalEditMembersTask").modal('hide');        
    };


   $scope.saveMembers = (form) =>
    {
        var dataUser = store.session.get("user_tddm4iotbs");
        console.log(form);
        console.log(form.selecteditem.$viewValue.id_permitmaster);
        if (form.$valid)
        {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + "members/saveMemberss",
                data: JSON.stringify({
                    'permit_master': form.selecteditem.$viewValue.id_permitmaster,
                    'role': form.role.$viewValue,
                    'idTask': $scope.selected_Task,
                    'status': form.status_new_member.$viewValue,
                    "emailperson": dataUser.email_person,
                    "idmasterproject": $scope.selected_project
                }),
                error: function (objXMLHttpRequest)
                {
                    // console.log("error", objXMLHttpRequest);
                },
                success: function (data) {
                    alertAll(data);
                    $scope.shareprojectM(form.email.$viewValue);
                }
            });
            //alert("asssssssssss");
        }
    };


    $scope.updateMembers = (form) =>
    {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (form.$valid)
        {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + "members/updateMembers",
                data: JSON.stringify({
                    
                    'permit_master': form.selecteditem.$viewValue.id_permitmaster,
                    'role_member': form.role_member_edit.$viewValue,                    
                    'status_member' : form.status_member_edit.$viewValue,
                    'id_task' : $scope.entregable_component_task_selected.id_task,
                    "emailperson":dataUser.email_person,
                    "idmasterproject": $scope.selected_project
                }),
                error: function (objXMLHttpRequest)
                {
                    alert("Hola");
                },
                success: function (data) {
                   alertAll(data);
                   $scope.closeEditMembers();
                }
            });            
        }
    };
    
    
    $scope.getGanttObjectts = () =>
    {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + "projects/getGanttObjects",
            data: JSON.stringify({
                "idmasterproject": $scope.selected_project
            }),
            error: function (objXMLHttpRequest)
            {
                
            },
            success: function (data) {
                var jsonData = data.data;
                if (data.status === "2" || data.status === 2) {
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

                      tasks.forEach((task, index) => {
                          //Get the values of the objects
                          var name = task.name_task;
                          var status = task.develop_status_task;
                          var startdate = task.startdate_task;
                          var stimateddate = task.stimateddate_task;
                          var updatedate = task.updatedate_task;
                          var percentage =  task.actual_percentage_task;

                          var maxdate = stimateddate > updatedate ? stimateddate : updatedate;

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
                      components.forEach((component, index) => {
                          //Get the values of the objects
                          var name = component.name_component;
                          var status = component.develop_status_component;
                          var startdate = component.startdate_component;
                          var stimateddate = component.stimateddate_component;
                          var updatedate = component.updatedate_component;
                          var percentage = component.actual_percentage_component;

                          var maxdate = stimateddate > updatedate ? stimateddate : updatedate;

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

                        masterProject.deliverables.forEach((deliverable, index) => {          
                          //Get the values of the objects
                          var name = deliverable.name_entregable;
                          var status = deliverable.develop_status_entregable;
                          var startdate = deliverable.startdate_entregable;
                          var updatedate = deliverable.updatedate_entregable;
                          var stimateddate = deliverable.stimateddate_entregable;
                          var percentage = deliverable.actual_percentage_entregable;

                          var maxdate = stimateddate > updatedate ? stimateddate : updatedate;

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
                            right: 250,
                            left: 100
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

                        var percentageData = data.datasets[0].data;

                        percentageData.forEach((datapoint, index) => {         
                          percentage = datapoint.percentage + "%";
                          type = datapoint.type;
                          status = dataByStatus[datapoint.status].status;

                          if (type === "Deliverable" || type === "Project"){
                            ctx.font = 'bolder 16px monospace';
                          } else {            
                            ctx.font = '16px monospace';            
                          }

                          var posY = y.getPixelForValue(index);
                          ctx.fillText(type, 0, posY);
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
                    
                    const config = {
                      type: 'bar',
                      data,
                      options: options,
                      plugins: [extraDataOfGantt, drawLineToday]
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

    //modalpdf
    $scope.openModalpdf = (type) => {

    };

    $scope.closeModalpdf = () => {
        $("#modalpdf").modal('hide');
    };
    
    $scope.closeGanttModal = () => {
        $("#ganttModal").modal('hide');
    };

    $scope.tittlereport = (select) => {
        $scope.cadena=store.session.get("projectname");
        if (select === 2){
            $scope.cadena+=" - "+$scope.nameentregable;
        }else if (select === 3){
            $scope.cadena+=" - "+$scope.nameentregable+" - "+$scope.namecomponent;
        }
    };


      $scope.viewFile = (select, type,part,nameEntregable) => {        
        $scope.cadena=store.session.get("projectname");
        if (select === 1){
            $("#tituloReport").html("Deliverables");
        }else if (select === 2){
            $("#tituloReport").html("Components");
            $scope.cadena+=" - "+$scope.nameentregable;
        }else if (select === 3){
            $("#tituloReport").html("Tasks");
            $scope.cadena+=" - "+$scope.nameentregable+" - "+$scope.namecomponent;
        }else{
            if(part)
                $("#tituloReport").html("All elements");
            else
                $("#tituloReport").html(nameEntregable);
        }   // alert(urlWebServicies + `reportes/exportarPdf?select=${select}&type=${type}&part=${part}&rutap=${$scope.pathProj}`);
        // console.log($scope.cadena+" este es el reporte");
        $.ajax({
            method: "GET",
            xhrFields: {responseType: "arraybuffer"},
            url: urlWebServicies + `reportes/exportarPdf?select=${select}&type=${type}&part=${part}&rutap=${$scope.pathProj}&cad=${$scope.cadena}`,
            beforeSend: function (xhr) {
                //loading();
            },
            success: function (data) {                
                var blob = new Blob([data], {type: 'application/pdf'});
//                // console.log($scope.trustAsResourceUrl(window.URL.createObjectURL(blob)));
                //  $("#viewFiles").attr("href",window.URL.createObjectURL(blob));
                $("#obj").attr("src", window.URL.createObjectURL(blob));
                $("#modalpdf").modal();
            },
            error: function (objXMLHttpRequest) {
                // console.log("error: ", objXMLHttpRequest);
            }
        });
    };
    
    
     $scope.updateInformationElements = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'componentTask/updateAllElements',
            data: JSON.stringify({
                'id_person': dataUser.email_person           
            }),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {               
            },
            error: function (objXMLHttpRequest) {
                // console.log("error: ", objXMLHttpRequest);
            }
        });
    };
    


    $scope.shareprojectM=(emailp)=>
    {        
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + "projects/shareProjectMembers",
            data: JSON.stringify({
                'emailShare': emailp,
                'idproj': $scope.selected_project,
                'permit': 'SHARE_WRITER',
                "stateShare": 'WAITING'
            }),
            error: function (objXMLHttpRequest)
            {
                // console.log("error", objXMLHttpRequest);
            },
            success: function (data) {
                // console.log(data);
            }
        });
    };
    
    
    
    $scope.Get_task_for_component = (id_component) => {
        
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'componentTask/getInfoTaskforComponent',
            data: JSON.stringify({
                'id_component': id_component         
            }),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {   
                console.log(data);
            },
            error: function (objXMLHttpRequest) {
                // console.log("error: ", objXMLHttpRequest);
            }
        });
    };
    
    
    $scope.Get_components_for_deliverable = (id_entregable) => {
        
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'components/getInfoComponentfordeliverable',
            data: JSON.stringify({
                'id_entregable': id_entregable         
            }),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {   
                console.log(data);
            },
            error: function (objXMLHttpRequest) {
                // console.log("error: ", objXMLHttpRequest);
            }
        });
    };
    
    
    $scope.open_entregable_info_component = (entregable) => {
       console.log(entregable);
       $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'components/getInfoComponentfordeliverable',
            data: JSON.stringify({
                'id_entregable': entregable.id_entregable         
            }),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {
                swal.close();
                $scope.$apply(function () {
                    $scope.get_components_for_deliverate_data = data.data;
                });            
                $("#modal_compotens_for_deliverate").modal();
                console.log(data);
            },
            error: function (objXMLHttpRequest) {
                swal.close();
                console.log("error: ", objXMLHttpRequest);
            }
        });
    };

     $scope.close_entregable_info_component = () => {
        $("#modal_compotens_for_deliverate").modal('hide');        
    };
    
    
    $scope.open_task_for_component = (component) => {
       console.log(component);
       $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'componentTask/getInfoTaskforComponent',
            data: JSON.stringify({
                'id_component': component.id_entregable_component   
            }),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {
                swal.close();
                $scope.$apply(function () {
                    $scope.get_task_for_component_data = data.data;
                });            
                $("#modal_task_for_componets").modal();
                console.log(data);
            },
            error: function (objXMLHttpRequest) {
                swal.close();
                console.log("error: ", objXMLHttpRequest);
            }
        });
    };

     $scope.close_task_for_component = () => {
        $("#modal_task_for_componets").modal('hide');        
    };
    
    
    
    $scope.typeMembersI = null;
    
    $scope.openModalInformationMembers = (type) => {
        $("#modalInformationMembers").modal();
        
        $scope.typeMembersI = type;
        
        if(type === 'create')
            $scope.closeCreateMembers();
        else
            $scope.closeEditMembers();
    };

    $scope.closeModalInformationMembers = () => {
        $("#modalInformationMembers").modal('hide');        
        if($scope.typeMembersI === 'create')
            $scope.openCreateMembers($scope.entregable_component_task_selected);
        else
            $scope.openEditMembers($scope.entregable_component_task_selected);
    };
    
    $scope.hideButtons = (role) => {
        const valid_roles = ['A', 'F'];        
        if (valid_roles.some(v => role === v)) {
            return true;
        }       
        
        return false;
    };
    
    $scope.formreset = (form) => {
        if(form)
        {
            form.$setPristine();
            form.$setUntouched();
            
        }
    };
    

    asignar_eventos();
});


/*

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
}*/
    



function delete_forms_values()
{
    let controllersforms=document.querySelectorAll(".deletevalue");    
    for(let x of controllersforms)
        x.value="";
    
}

function onChangeBasePercentage(type, showMessage) {
    totalSum = 0;
    let jsonError = {status: 4, information: "The percentages are not valid.", data: "[]"};
    let jsonSuccess = {status: 2, information: "The percentages was updated successfully.", data: "[]"};
    let isCorrect = true;

    if (type === 1) {
        myArray = document.getElementsByName("base_percentage_entregable_edit_all");
        sumElement = document.getElementById("base_percentage_entregable_sum");

        for (let i = 0; i < myArray.length; i++) {
            if (myArray[i].value < 0) {
                myArray[i].value = myArray[i].defaultValue;
            }
        }
        for (let i = 0; i < myArray.length; i++) {
            totalSum += parseInt(myArray[i].value);
        }
        if (totalSum < 0 || totalSum > 100) {
            isCorrect = false;
            totalSum = 0;
            for (let i = 0; i < myArray.length; i++) {
                myArray[i].value = myArray[i].defaultValue;
                totalSum += parseInt(myArray[i].value);
            }
        }

        sumElement.value = totalSum;
    }
    if (type === 2) {
        myArray = document.getElementsByName("base_percentage_component_edit_all");
        sumElement = document.getElementById("base_percentage_component_sum");

        for (let i = 0; i < myArray.length; i++) {
            if (myArray[i].value < 0) {
                myArray[i].value = myArray[i].defaultValue;
            }
        }
        for (let i = 0; i < myArray.length; i++) {
            totalSum += parseInt(myArray[i].value);
        }
        if (totalSum < 0 || totalSum > 100) {
            totalSum = 0;
            isCorrect = false;
            for (let i = 0; i < myArray.length; i++) {
                myArray[i].value = myArray[i].defaultValue;
                totalSum += parseInt(myArray[i].value);
            }
        }
        sumElement.value = totalSum;
    }
    if (type === 3) {
        myArray = document.getElementsByName("base_percentage_task_edit_all");
        sumElement = document.getElementById("base_percentage_task_sum");

        for (let i = 0; i < myArray.length; i++) {
            if (myArray[i].value < 0) {
                myArray[i].value = myArray[i].defaultValue;
            }
        }
        for (let i = 0; i < myArray.length; i++) {
            totalSum += parseInt(myArray[i].value);
        }
        if (totalSum < 0 || totalSum > 100) {
            totalSum = 0;
            isCorrect = false;
            for (let i = 0; i < myArray.length; i++) {
                myArray[i].value = myArray[i].defaultValue;
                totalSum += parseInt(myArray[i].value);
            }
        }
        sumElement.value = totalSum;
    }
    if (showMessage)
        if (isCorrect)
            alertAll(jsonSuccess);
        else
            alertAll(jsonError);
}