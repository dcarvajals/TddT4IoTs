// expansion del controlador de iot para cargar el codigo a la placa
app.expandControllerIoT = function ($scope) {

    var theme = "ace/theme/chrome";  // Tema claro
    var mode = "ace/mode/c_cpp";  // Modo C/C++
    $scope.codeLoad = {};
    $scope.utilParams = {
        active: -1,
        you: -1,
        room: ""
    };
    $scope.DataSession = {};
    $scope.deviceActive = {};
    $scope.devices = [];
    $scope.command_list = [];
    $scope.dataDevice = {identifier: "",so: "", userDevice:"", status: "Disconected", css:"badge-danger"};
    $scope.command = "";

    $(document).ready(() => {
        $scope.initCodeBoard();

        $scope.DataSession = getDataSession();
        $scope.utilParams.room = $scope.DataSession.email_person;
        websocketInit($scope.utilParams.room);
    });

    $scope.openModalLoadCode = function () {
        $scope.codeLoad.setValue(codeGeneral.getValue());
        $("#modalLoadCode").modal();
    }

    $scope.loadCodeBoard = function () {
        $scope.command = "arduino-cli  upload -p COM4 --fqbn arduino:renesas_uno:unor4wifi scriptTmp";
        $scope.sendCommand();
    }

    $scope.downloadDaemon = () => {
      return   location.origin + rutasStorage.fileZip + "/tddt4iotsDaemon.zip";
    };

    $scope.compile = () => {
        var projectpath = store.session.get("projectpath");
        let command = "";
        command = 'curl -o \"scriptTmp/scriptTmp.ino \" \"' + location.origin + rutasStorage.projects + projectpath + '/EasyIoT/script.ino\"';
        $scope.command = command;
        //console.log($scope.command);
        $scope.sendCommand();
        command = "arduino-cli  compile --fqbn arduino:renesas_uno:unor4wifi scriptTmp";
        //console.log(command);
        $scope.command = command;
        $scope.sendCommand();
    };

    $scope.checkEnter = function(event) {
        if (event.key === "Enter") {
            $scope.sendCommand();
        }
    };

    $scope.initCodeBoard = function () {
        $scope.codeLoad = ace.edit("editrCodeComplete");
        $scope.codeLoad.setTheme(theme);
        $scope.codeLoad.getSession().setMode(mode);
        $scope.codeLoad.getSession().setUseWrapMode(true);  // Habilita el ajuste de línea
        $scope.codeLoad.setValue('', 1); // Inicializa con el contenido en blanco
    }

    $scope.sendCommand = function () {
        let obj = {
            "header": $scope.utilParams.room,
            "content": JSON.stringify({"command": $scope.command}),
            "config": "sendComand" //$scope.dataDevice.identifier
        };
        MessageSend(obj);
    };

    $scope.setCommandResult = function (value, apply) {
        if (value)
        {
            if (apply === true)
            {
                $scope.$apply(() => {
                    document.querySelector("#mi_terminal").innerHTML += "<pre style='line-height: 15px; font-size: 12px'>" + value + "</pre> <hr/>";
                });
            } else {
                document.querySelector("#mi_terminal").innerHTML += "<pre style='line-height: 15px; font-size: 12px'>" + value + "</pre> <hr/>";
            }
        }
    };

    $scope.setIdentifier = function (value, apply) {
        if (apply === true)
        {
            $scope.$apply(() => {
                $scope.utilParams.you = value;
            });
        } else {
            $scope.utilParams.you = value;
        }
    };
    $scope.setDevicesList = function (value, apply) {
        if (apply === true)
        {
            $scope.$apply(() => {
                $scope.devices = value;
                $scope.utilParams.active = -1;
            });
        } else {
            $scope.devices = value;
        }
    };

    $scope.clearConsole =() => {
        document.querySelector("#mi_terminal").innerHTML = "";
    }

    // #########################################################################
    // Web socket para conectar con la app de escritorio
    var url = (window.location.protocol === 'https:' ? 'wss://' : 'ws://') + window.location.host
        + "/Tddm4IoTbsServer/daemon_socket";
    var ardWs;
    var SessionParams = {};

    function websocketInit(UserParams) {
        ardWs = new WebSocket(url);
        ardWs.onopen = onOpen;
        ardWs.onclose = onClose;
        ardWs.onmessage = onMessage;
        ardWs.onerror = onError;
        if (UserParams)
        {
            SessionParams.groupid = UserParams;
        }
    }
    function websocketOpen() {
        try {
            if (ardWs.readyState === 1)
            {
            }
        } catch (e) {
            console.error("Error (websocketOpen): ", e);
        }
    }
    function websocketClose() {
        bioWs.close();
    }
    function onOpen() {
        // //console.log("conectado...");
        let objmsg = {
            "header": SessionParams.groupid,
            "content": {
                "name": "Unknown - Anonymous",
                "type": "WebApp"
            },
            "config": "init"
        };
        MessageSend(objmsg);
    }
    function onClose(evt) {
        console.info("Desconectado...");
    }
    function onError(event) {
        console.error("Error en el WebSocket detectado:", event);
    }
    function MessageSend(obj) {
        let objmsg = JSON.stringify(obj);
        if (objmsg.length <= 10000450)
        {
            ardWs.send(objmsg);
        } else {
            alertAll({status: 4, information: "The message exceeds the limit."});
        }
    }
    function onMessage(evt) {
        var obj = JSON.parse(evt.data);
        //console.log(obj);
        switch (obj.config) {
            case "join":
                //console.log("Acaba de crearse una nueva sesion.");
                $scope.$apply(function(){
                    $scope.dataDevice.so = obj.content.type;
                    $scope.dataDevice.userDevice = obj.content.name;
                    $scope.dataDevice.identifier = obj.content.identifier;
                    $scope.dataDevice.status = "Connected";
                    $scope.dataDevice.css = "badge-success";
                });
                break;
            case "displayTerminal":
                $scope.setCommandResult(obj.content, true);
                break;
            case "close":
                $scope.$apply(function () {
                    $scope.dataDevice = {identifier: "",so: "", userDevice:"", status: "Disconected", css:"badge-danger"};
                });
                break;
                //
                break;
        }
    }

};