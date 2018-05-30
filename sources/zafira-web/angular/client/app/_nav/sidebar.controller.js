(function () {
    'use strict';

    angular
        .module('app.sidebar')
        .controller('SidebarController', ['$scope', '$rootScope', '$cookies', '$q', '$mdDialog', '$state', 'ViewService', 'ConfigService', 'ProjectService', 'ProjectProvider', 'UtilService', 'UserService', 'DashboardService', 'AuthService', 'SettingsService', 'UploadService', SidebarController])

    // **************************************************************************
    function SidebarController($scope, $rootScope, $cookies, $q, $mdDialog, $state, ViewService, ConfigService, ProjectService, ProjectProvider, UtilService, UserService, DashboardService, AuthService, SettingsService, UploadService) {

    	$scope.DashboardService = DashboardService;

        $scope.selectedProjects = ProjectProvider.getProjects();
        $scope.version = null;
        $scope.projects = [];
        $scope.dashboards = [];
        $scope.views = [];
        $scope.tools = {};

        var FILE_LOGO_TYPE = "COMMON";

        $scope.hasHiddenDashboardPermission = function(){
        	return AuthService.UserHasAnyPermission(["VIEW_HIDDEN_DASHBOARDS"]);
        };

        $scope.loadProjects = function(){
            ConfigService.getConfig("projects").then(function(rs) {
                if(rs.success)
                {
                    $scope.projects = rs.data;
                    if($scope.selectedProjects) {
                        $scope.projects.forEach(function (project) {
                            if ($scope.selectedProjects.indexOfField('id', project.id) >= 0) {
                                project.selected = true;
                            }
                        });
                    }
                }
                else
                {
                	alertify.error("Unable to load projects");
                }
            });
        };

        function getViews(){
            return $q(function (resolve, reject) {
                ViewService.getViewById(ProjectProvider.getProjectsIdQueryParam()).then(function(rs) {
                    if(rs.success)
                    {
                        $scope.views = rs.data;
                        resolve(rs.data);
                    }
                    else
                    {
                        reject(rs.message);
                    }
                });
            });
        };

        $scope.loadViews = function () {
            getViews().then(function (response) {
               $scope.viewsLoaded = true;
            });
        };

        $scope.loadDashboards = function() {
            getDashboards().then(function (response) {
                $scope.dashboardsLoaded = true;
            });
        };

        function getDashboards() {
            return $q(function (resolve, reject) {
                if ($scope.hasHiddenDashboardPermission() == true) {
                    DashboardService.GetDashboards().then(function (rs) {
                        if (rs.success) {
                            $scope.dashboards = rs.data;
                            resolve(rs.data);
                        } else {
                            reject(rs.message);
                        }
                    });
                }
                else {
                    var hidden = true;
                    DashboardService.GetDashboards(hidden).then(function (rs) {
                        if (rs.success) {
                            $scope.dashboards = rs.data;
                            resolve(rs.data);
                        } else {
                            reject(rs.message);
                        }
                    });
                }
            });
        };

        $scope.selectedProjectsPresent = function () {
            return $scope.selectedProjects && $scope.selectedProjects.length != 0;
        };

        $scope.joinProjectNames = function () {
            var proj = $scope.selectedProjects.map(function(project, index) {
                return project.name;
            }).join(', ');
            if(proj.length > 10) {
                proj = proj.substring(0, 10) + '....';
            }
            return proj;
        };

        $scope.resetProjects = function () {
            $scope.selectedProjects = [];
            $scope.projects.forEach(function(project) {
                project.selected = undefined;
            });
            ProjectProvider.setProjects([]);
            $state.reload();
        };

        $scope.$on("$mdMenuClose", function(name, listener) {
            if(listener[0].id == 'projects-menu') {
                $scope.selectedProjects = $scope.projects.filter(function (value) {
                    return value.selected;
                });
                if((! ProjectProvider.getProjects() && $scope.selectedProjects) || ! ProjectProvider.getProjects().equalsByField($scope.selectedProjects, 'id')) {
                    ProjectProvider.setProjects($scope.selectedProjects);
                    $state.reload();
                }
            }
        });

        $scope.showProjectDialog = function(event) {
            $mdDialog.show({
                controller: ProjectController,
                templateUrl: 'app/_nav/project_modal.html',
                parent: angular.element(document.body),
                targetEvent: event,
                clickOutsideToClose:true,
                fullscreen: true
            })
                .then(function(answer) {
                }, function() {
                });
        };

        $scope.showViewDialog = function(event, view) {
            $mdDialog.show({
                controller: ViewController,
                templateUrl: 'app/_nav/view_modal.html',
                parent: angular.element(document.body),
                targetEvent: event,
                clickOutsideToClose:true,
                fullscreen: true,
                locals: {
                    view: view
                }
            })
                .then(function(answer) {
                }, function() {
                });
        };

        $scope.showUploadImageDialog = function($event) {
            $mdDialog.show({
                controller: FileUploadController,
                templateUrl: 'app/_users/upload_image_modal.html',
                parent: angular.element(document.body),
                targetEvent: $event,
                clickOutsideToClose:true,
                fullscreen: true,
                scope: $scope,
                preserveScope: true
            })
                .then(function(answer) {
                }, function() {
                });
        };

        // ***** Modals Controllers *****
        function ProjectController($scope, $mdDialog) {
            $scope.project = {};
            $scope.createProject = function(project){
                ProjectService.createProject(project).then(function(rs) {
                    if(rs.success)
                    {
                        alertify.success("Project created successfully");
                    }
                    else
                    {
                        alertify.error(rs.message);
                    }
                });
                $scope.hide();
            };
            $scope.hide = function() {
                $mdDialog.hide();
            };
            $scope.cancel = function() {
                $mdDialog.cancel();
            };
            (function initController() {
            })();
        }

        function ViewController($scope, $mdDialog, view) {
            $scope.view = {};
            if(view)
            {
                $scope.view.id = view.id;
                $scope.view.name = view.name;
                $scope.view.projectId = view.project.id;
            }

            ConfigService.getConfig("projects").then(function(rs) {
                if(rs.success)
                {
                    $scope.projects = rs.data;
                }
                else
                {
                }
            });

            $scope.createView = function(view){
                ViewService.createView(view).then(function(rs) {
                    if(rs.success)
                    {
                        alertify.success("View created successfully");
                    }
                    else
                    {
                        alertify.error(rs.message);
                    }
                });
                $scope.hide();
            };

            $scope.updateView = function(view){
                ViewService.updateView(view).then(function(rs) {
                    if(rs.success)
                    {
                        alertify.success("View updated successfully");
                    }
                    else
                    {
                        alertify.error(rs.message);
                    }
                });
                $scope.hide();
            };

            $scope.deleteView = function(view){
                ViewService.deleteView(view.id).then(function(rs) {
                    if(rs.success)
                    {
                        alertify.success("View deleted successfully");
                    }
                    else
                    {
                        alertify.error(rs.message);
                    }
                });
                $scope.hide();
            };
            $scope.hide = function() {
                $mdDialog.hide();
            };
            $scope.cancel = function() {
                $mdDialog.cancel();
            };
            (function initController() {
            })();
        }

        function FileUploadController($scope, $mdDialog) {
            $scope.uploadImage = function (multipartFile) {
                UploadService.upload(multipartFile, FILE_LOGO_TYPE).then(function (rs) {
                    if(rs.success)
                    {
                        $rootScope.companyLogo.value = rs.data.url;
                        SettingsService.editSetting($rootScope.companyLogo)
                            .then(function (prs) {
                                if(prs.success)
                                {
                                    $rootScope.companyLogo.value += '?' + (new Date()).getTime();
                                    alertify.success("Photo was uploaded");
                                    $scope.hide();
                                } else {
                                    alertify.error(prs.message);
                                }
                            });
                    }
                    else
                    {
                        alertify.error(rs.message);
                    }
                });
            };
            $scope.hide = function() {
                $mdDialog.hide(true);
            };
            $scope.cancel = function() {
                $mdDialog.cancel(false);
            };
        }

        (function initController() {
            $scope.selectedProjects = ProjectProvider.getProjects();
            // TODO: 3/20/18  remove on next release
            if((!$scope.selectedProjects || ! $scope.selectedProjects.length == 0) && ProjectProvider.getProject())
            {
                ProjectProvider.setProjects([].push(ProjectProvider.getProject()));
                ProjectProvider.removeProject();
                console.log('Project cookies was removed');
            }
        })();
    }
})();
