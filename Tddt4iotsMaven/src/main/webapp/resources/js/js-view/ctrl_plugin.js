


var app = angular.module("miApp", []);

app.controller("ctrl_ardcoomands", ($scope, $http) => {
    $scope.utilParams = {
        active: -1,
        you: -1,
        room: ""
    };
    $scope.DataSession = {};

    angular.element(document).ready(function () {

        $scope.DataSession = getDataSession();
        $scope.utilParams.room = $scope.DataSession.email_person;
        websocketInit($scope.utilParams.room);
        // console.log("Datos de Usuarios", $scope.utilParams.room);
    });

    $scope.deviceActive = {};

    $scope.devices = [];
    $scope.command_list = [];
    $scope.selectDevice = function (value) {
        if (value > -1 && $scope.devices[value].type !== 'WebApp')
        {
            $scope.utilParams.active = value;
            //do anything
            $scope.deviceActive = $scope.devices[$scope.utilParams.active];
            let obj = {
                "header": $scope.utilParams.room,
                "content": {
                    "conected": $scope.utilParams.you,
                    "to": $scope.deviceActive.identifier
                },
                "config": "conected"

            };
            // console.log("enviando", obj);
            MessageSend(obj);
        }
    };

    $scope.canShow = function (param) {
        if ($scope.textfilt.toString().length > 0) {
            return param[$scope.item].toString().toLowerCase().includes($scope.textfilt.toLowerCase());
        }
        return true;
    };
    $scope.chargeCommands = function () {
        $http.get('commands.json', {
        }).then(function (response) {
            let items = response.data.commands;
            if (items != undefined && items !== null)
            {
                $scope.command_list = items;
            } else {
                $scope.command_list = [];
            }
        }, function (response) {
            // console.log("error");
            // console.log(response);
            $scope.command_list = [];
        });
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
    $scope.sendCommand = function () {
        let obj = {
            "header": $scope.utilParams.room,
            "content": JSON.stringify({"command": $scope.commands.text}),
            "config": $scope.deviceActive.identifier
        };
        MessageSend(obj);
    };
    $scope.setCommandResult = function (value, apply) {
        if (value)
        {
            if (apply === true)
            {
                $scope.$apply(() => {
                    document.querySelector("#mi_terminal").innerHTML += "<pre>" + value + "</pre>";
                });
            } else {
                document.querySelector("#mi_terminal").innerHTML += "<pre>" + value + "</pre>";
            }
        }
    };

    $scope.onload = function () {

    };




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
        // console.log("conectado...");
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
        // console.log(obj);
        if (obj.config === "mejoin")
        {
            SessionParams.identifiew = obj.content;
            $scope.setIdentifier(obj.content, true);
        } else if (obj.config === "list") {
            $scope.setDevicesList(obj.content, true);

        } else if (obj.config === "close") {
            obj.content += " it has disconnected.";
        } else {
            //validate partner
            $scope.setCommandResult(obj.content, true);
        }
    }
});