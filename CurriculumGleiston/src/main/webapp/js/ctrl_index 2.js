/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

app = angular.module('app', []);
controller = app.controller("cvController", function ($scope, $http) { 
    
    // Variable para almacenar los datos del JSON
    $scope.cvData = {};
    
    // Array de idiomas con sus archivos JSON
    $scope.languages = [
        { name: 'English', code: 'en', url: 'json/datacv_en.json' },
        { name: 'Spanish', code: 'es', url: 'json/datacv.json' }
    ];
    
    $scope.selectedLanguage = $scope.languages[0].code;
    
    $scope.init = function () {
        $scope.loadJsonData($scope.languages[0].url);
    };
    
    // Método para cargar datos desde un archivo JSON
    $scope.loadJsonData = function(url) {
        $http.get(url)
            .then(function(response) {
                // Éxito en la solicitud, almacenamos los datos en la variable cvData
                $scope.cvData = response.data;
            })
            .catch(function(error) {
                // Error en la solicitud
                console.error('Error al cargar el archivo JSON:', error);
            });
    };
    
    $scope.changeLanguage = function () {
        // Encuentra la URL del archivo JSON según el idioma seleccionado
        const selectedLang = $scope.languages.find(lang => lang.code === $scope.selectedLanguage);
        if (selectedLang) {
            $scope.loadJsonData(selectedLang.url);
        }
    };

});



