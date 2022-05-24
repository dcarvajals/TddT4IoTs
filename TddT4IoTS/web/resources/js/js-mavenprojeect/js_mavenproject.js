app.expandControllerMavenProject = function ($scope) {

    $scope.mavenProject = '';
    $scope.selectedEntitieObj = {};
    $scope.jsonMavenProject = {entities: [], conectionDB: {}};

    /**
     * ##############################################################################################################
     * DESCARGAR Y ENCAPSULAR PROYECTO
     * ##############################################################################################################
     * */
    $scope.encapsulateProject = () => {
        let urlParams = new URLSearchParams(window.location.search);
        let id_project = urlParams.get('identifiquer');
        let api_data = {
            "user_token": $scope.DatoUsuario.user_token,
            "idProj": id_project,
            "info": JSON.stringify($scope.jsonMavenProject),
            "module": 'CreateMvn'
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
                console.log(data);
                alertAll(data);
                $scope.$apply(() => {
                    $scope.mavenProject = rutasStorage.projects + data.data.MavenApplication;
                    console.log($scope.mavenProject);
                });
            },
            error: (objXMLHttpRequest) => {
                console.log("error: ", objXMLHttpRequest);
            }
        });
    };

    $scope.openModalCreateMavenProject = () => {
        // abrir modal
        $("#modal_create_mavenproject").modal();
        // realizar una copia del json del diagrama de clases
        $scope.jsonMavenProject.entities = Object.assign([], $scope.jsonClass.diagram[0].class);
        $scope.jsonMavenProject.enums = Object.assign([], $scope.jsonClass.diagram[0].enums);
        // aumentar los nuevos parametros
        $scope.addNewParams($scope.jsonMavenProject.entities);
        // validar campos de la tabla de los atributos

        console.log("JSON MANVE PROJEECT", $scope.jsonMavenProject);
    };

    // funcion para obtener los atributos de la clase seleccionada
    $scope.selectedEntitie = (entitie) => {
        console.log(entitie);
        $scope.selectedEntitieObj = entitie;
    };

    // funcion para agregar los nuevos parametros a los atributos de cada entidad
    $scope.addNewParams = (entities) => {
        for (let positionEntitie = 0; positionEntitie < entities.length; positionEntitie++) {
            let attributes = entities[positionEntitie].attributes;
            for (let positionAttributes = 0; positionAttributes < attributes.length; positionAttributes++) {
                let attribute = attributes[positionAttributes];
                // aumentar los nuevos parametros
                attribute["not_null"] = positionAttributes === 0;
                attribute["primary_key"] = positionAttributes === 0;
                attribute["length_precision"] = attribute["type"] === "String" ? 30 : -1;
            }
        }
    };

    // actualizar el tipo de dato
    $scope.updateDataTypeMavenProject = (attribute, index) => {
        let newDataType = $("#datatype_maven" + index).val();
        console.log(newDataType);
        attribute["type"] = newDataType.split(":")[1];
    };

    // actualizar el length/preicison
    $scope.updateLengthMavenProject = (attribute, index) => {
      let newLength =   $("#length_maven" + index).val();
      console.log(newLength);
      attribute["length_precision"] = parseInt(newLength);
    };

    // actualizar el notnull
    $scope.updateNotNullMavenProject = (attribute) => {
        attribute["not_null"] = !attribute["not_null"];
    };

    // actualizar el primary key
    $scope.updatePrimaryKeyMavenProject = (attribute, index) => {
        let checkpk = $("#notNull_" + index);

        if(attribute["not_null"] && !attribute["primary_key"]){
            attribute["primary_key"] = !attribute["primary_key"];
            // aseguramos que este en verdadero
            attribute["not_null"] = true
            // aseguramos que el check en la interfaz este en true
            checkpk.prop('checked', true);
            // deshabilitamos el check
            checkpk.attr('disabled','disabled');
        } else {
            attribute["primary_key"] = !attribute["primary_key"];
            attribute["not_null"] = !attribute["not_null"];
            checkpk.prop('checked', attribute["not_null"]);
            if(attribute["not_null"] === true){
                checkpk.attr('disabled','disabled');
            } else{
                checkpk.prop('checked', false);
                checkpk.removeAttr('disabled');
            }
        }

        // desactivar el check de primary key a los demas
        for(let x = 0; x < $scope.selectedEntitieObj.attributes.length; x++){
            let checkAllPk = $("#primary_key" + x);
            let checkAllNotNull = $("#notNull_" + x);
            let attributteAll = $scope.selectedEntitieObj.attributes[x]
            if(x !== index){
                if(attributteAll["not_null"] && attributteAll["primary_key"]){
                    checkAllPk.prop('checked', false);
                    checkAllNotNull.removeAttr('disabled');
                    checkAllNotNull.prop('checked', false);
                    attributteAll["primary_key"] = false;
                    attributteAll["not_null"] = false;
                }
            }
        }
    };

    $scope.nextStep = () => {
      $("#modal_create_mavenproject").modal("hide");
      $("#modal_class_conecction").modal();
    };

    $scope.returnStep = () => {
        $("#modal_create_mavenproject").modal();
        $("#modal_class_conecction").modal("hide");
    };

    $scope.createMavenProject = (form) => {
        let url_data_base = "";
        let jdbc = "";

        switch (form.db_driver.$modelValue){
            case "pg":
                url_data_base = "org.postgresql.Driver";
                jdbc = "jdbc:postgresql://"+form.db_server.$modelValue+":"+form.db_port.$modelValue+"/"+form.db_name.$modelValue;
                break;
            case "mq":
                url_data_base = "com.mysql.jdbc.Driver";
                jdbc = "jdbc:mysql://"+form.db_server.$modelValue+":"+form.db_port.$modelValue+"/"+form.db_name.$modelValue;
                break;
            case "sq":
                url_data_base = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                jdbc = "jdbc:sqlserver://"+form.db_server.$modelValue+":"+form.db_port.$modelValue+";databaseName="+form.db_name.$modelValue;
                break;
        }

        // obtener datos del form para la clase conexion
        $scope.jsonMavenProject.conectionDB = {
            db_name: form.db_name.$modelValue,
            db_user: form.db_user.$modelValue,
            db_password: form.db_password.$modelValue,
            db_server: form.db_server.$modelValue,
            db_port: form.db_port.$modelValue,
            url_data_base:  url_data_base,
            jdbc: jdbc
        };
        console.log($scope.jsonMavenProject);
        $scope.encapsulateProject();
    };
    /**
     * ##############################################################################################################
     * FIN DESCARGAR Y ENCAPSULAR PROYECTO
     * ##############################################################################################################
     * */


}
