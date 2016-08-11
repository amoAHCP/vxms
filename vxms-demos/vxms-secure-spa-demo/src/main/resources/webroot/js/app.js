angular.module('CrudApp', []).config(['$routeProvider','$locationProvider', function ($routeProvider,$locationProvider) {
    $routeProvider.
        when('/', {templateUrl: '/private/tpl/lists.html', controller: ListCtrl}).
        when('/add-user', {templateUrl: '/private/tpl/add-new.html', controller: AddCtrl}).
        when('/edit/:id', {templateUrl: '/private/tpl/edit.html', controller: EditCtrl}).
        otherwise({redirectTo: '/'});
}]);

function ListCtrl($scope, $http,$location) {

    var code = $location.search().code;
    window.console.log(code);
    $http.get('/private/api/users').success(function (data) {
        $scope.users = data;
    });
}

function AddCtrl($scope, $http, $location) {
    $scope.master = {};
    $scope.activePath = null;

    $scope.add_new = function (user, AddNewForm) {

        $http.post('/private/api/users', user).success(function () {
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
    $http.get('/private/api/users/' + id).success(function (data) {
        $scope.user = data;
    });

    $scope.update = function (user) {
        $http.put('/private/api/users/' + id, user).success(function (data) {
            $scope.user = data;
            $scope.activePath = $location.path('/');
        });
    };

    $scope.delete = function (user) {
        var deleteUser = confirm('Are you absolutely sure you want to delete ?');
        if (deleteUser) {
            $http.delete('/private/api/users/' + id)
                .success(function(data, status, headers, config) {
                    $scope.activePath = $location.path('/');
                }).
                error(function(data, status, headers, config) {
                    console.log("error");
                    // custom handle error
                });
        }
    };
}