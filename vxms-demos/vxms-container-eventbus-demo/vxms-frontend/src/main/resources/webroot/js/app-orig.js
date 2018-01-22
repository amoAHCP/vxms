/*
 * Copyright [2018] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

angular.module('CrudApp', []).config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: '/tpl/lists.html',
        controller: ListCtrl
    }).when('/add-user', {
        templateUrl: '/tpl/add-new.html',
        controller: AddCtrl
    }).when('/edit/:id', {templateUrl: '/tpl/edit.html', controller: EditCtrl}).otherwise({redirectTo: '/'});
}]);

function ListCtrl($scope, $http) {
    $http.get('http://localhost:9090/api/users').success(function (data) {
        $scope.users = data;
    });
}

function AddCtrl($scope, $http, $location) {
    $scope.master = {};
    $scope.activePath = null;

    $scope.add_new = function (user, AddNewForm) {

        $http.post('http://localhost:9090/api/users', user).success(function () {
            $scope.reset();
            $scope.activePath = $location.path('/');
        });

        $scope.reset = function () {
            $scope.user = angular.copy($scope.master);
        };

        $scope.reset();

    };
}

function EditCtrl($scope, $http, $location, $routeParams) {
    var id = $routeParams.id;
    $scope.activePath = null;

    $http.get('http://localhost:9090/api/users/' + id).success(function (data) {
        $scope.user = data;
    });

    $scope.update = function (user) {
        $http.put('http://localhost:9090/api/users/' + id, user).success(function (data) {
            $scope.user = data;
            $scope.activePath = $location.path('/');
        });
    };

    $scope.delete = function (user) {
        var deleteUser = confirm('Are you absolutely sure you want to delete ?');
        if (deleteUser) {
            $http.delete('http://localhost:9090/api/users/' + id)
                .success(function (data, status, headers, config) {
                    $scope.activePath = $location.path('/');
                }).error(function (data, status, headers, config) {
                console.log("error");
                // custom handle error
            });
        }
    };
}