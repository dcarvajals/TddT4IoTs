/* global keyGroup, go, myDiagram, urlWebServicies, store, jsonCodeMirror, libraries, variables, setup, loop, loop, methods, swal, angular, app, rutasStorage, Swal */

app.controller("users_controller", function ($scope, $http) {

    angular.element($('[ng-controller="application"]')).scope().changeTittlePage("User Administration", false);
    $scope.appPage.Select = "usermanagement";

    $scope.arrayAdmins = [];
    $scope.arrayUsers = [];
    $scope.userFilters = {
        global: "",
        admin: {name: "", email: "", date: ""},
        user: {name: "", email: "", date: "", status: ""}
    };

    const containsText = (value, search) => {
        return String(value || "").toLowerCase().indexOf(String(search || "").toLowerCase()) !== -1;
    };

    const matchesIdentity = (person, search) => {
        return containsText(`${person.name_person || ""} ${person.lastname_person || ""}`, search);
    };

    const matchesGlobal = (person) => {
        const search = $scope.userFilters.global;
        const statusLabels = {U: "active user", S: "on hold pending", I: "inactive"};
        return matchesIdentity(person, search)
                || containsText(person.email_person, search)
                || containsText(person.datereg_person, search)
                || containsText(statusLabels[person.type_person], search)
                || (containsText("administrator", search) && person.type_person !== "U"
                && person.type_person !== "S" && person.type_person !== "I");
    };

    $scope.filterAdminRow = (person) => {
        const filters = $scope.userFilters.admin;
        return matchesGlobal(person)
                && matchesIdentity(person, filters.name)
                && containsText(person.email_person, filters.email)
                && containsText(person.datereg_person, filters.date);
    };

    $scope.filterUserRow = (person) => {
        const filters = $scope.userFilters.user;
        return matchesGlobal(person)
                && matchesIdentity(person, filters.name)
                && containsText(person.email_person, filters.email)
                && containsText(person.datereg_person, filters.date)
                && (!filters.status || person.type_person === filters.status);
    };

    $(document).ready(() => {
        $scope.loadUsers();
    });

    $scope.loadUsers = () => {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'personapis/getUsers',
                data: JSON.stringify({"user_token": dataUser.user_token}),
                beforeSend: function (xhr) {
                    loading();
                },
                success: function (data) {
                    swal.close();
                    $scope.$apply(function () {// Se puede optimizar con una sola petición en un ArrayJSON
                        $scope.arrayAdmins = data.data.admins;
                        $scope.arrayUsers = data.data.users;
                        // console.log($scope.arrayAdmins);
                        // console.log($scope.arrayUsers);
                    });
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    // console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index";
        }
    };
    $scope.stateUser = (action, id_person) => {
        swal.fire({
            title: 'Are you sure to take this action?',
            showDenyButton: true,
//                showCancelButton: true,
            confirmButtonText: `Confirm`,
            denyButtonText: `Cancel`
        }).then((result) => {
            if (result.isConfirmed) {
                var dataUser = store.session.get("user_tddm4iotbs");
                if (dataUser !== undefined && dataUser !== null) {
                    let api_param = {
                        user_token: dataUser.user_token,
                        id_person: id_person,
                        action: action
                    };
                    apiStateUser(api_param);
                }
            } else if (result.isDenied) {
                swal.fire("Okay ... think better next time");
            }
        });
    };

    apiStateUser = (api_param) => {
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'personapis/stateUsers',
            data: JSON.stringify({...api_param}),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (data) {
                swal.close();
                // console.log(data);
                alertAll(data);
                $scope.loadUsers();

            },
            error: function (objXMLHttpRequest) {
                // console.log("error: ", objXMLHttpRequest);
            }
        });
    };

});
