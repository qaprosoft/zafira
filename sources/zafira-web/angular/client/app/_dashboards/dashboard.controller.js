import dashboardSettingsModalController from './dashboard-settings-modal/dashboard-settings-modal.controller';
import dashboardSettingsModalTemplate from './dashboard-settings-modal/dashboard-settings-modal.html';
import widgetDialog from './widget-dialog/widget-dialog.html';
import widgetDialogController from './widget-dialog/widget-dialog.controller';
import dashboardEmailModalTemplate from './dashboard-email-modal/dashboard-email-modal.html';
import dashboardEmailModalController from './dashboard-email-modal/dashboard-email-modal.controller';

const dashboardController = function dashboardController($scope, $rootScope, $q, $timeout, $interval, $cookies, $location, $state,
                                 $http, $mdConstant, $stateParams, $mdDialog, $mdToast, UtilService, DashboardService,
                                 UserService, AuthService, ProjectProvider) {
    'ngInject';

    $scope.currentUserId = $location.search().userId;

    $scope.pristineWidgets = [];

    $scope.unexistWidgets = [];

    $scope.dashboard = {};

    $scope.gridstackOptions = {
        disableDrag: true,
        disableResize: true,
        verticalMargin: 20,
        resizable: {
            handles: 'se, sw'
        },
        cellHeight: 20
    };

    $scope.isJson = function(json) {
        return typeof(json) === 'object';
    };

    $scope.isGridStackEvailableToEdit = function() {
        return !angular.element('.grid-stack-one-column-mode').is(':visible');
    };

    $scope.startEditWidgets = function () {
        angular.element('.grid-stack').gridstack($scope.gridstackOptions).data('gridstack').enable();
        showGridActionToast();
    };

    var defaultWidgetLocation = '{ "x":0, "y":0, "width":4, "height":11 }';

    function loadDashboardData (dashboard, refresh) {
        for (var i = 0; i < dashboard.widgets.length; i++) {
            var currentWidget = dashboard.widgets[i];
            currentWidget.location = jsonSafeParse(currentWidget.location);
            if (!refresh || refresh && currentWidget.refreshable) {
                loadWidget(dashboard.title, currentWidget, dashboard.attributes, refresh);
            }
        }
        angular.copy(dashboard.widgets, $scope.pristineWidgets);
    };

    function loadWidget (dashboardName, widget, attributes, refresh) {
        var sqlAdapter = {'sql': widget.sql, 'attributes': attributes};
        if(!refresh){
            $scope.isLoading = true;
        }
        var params = setQueryParams(dashboardName);
        DashboardService.ExecuteWidgetSQL(params, sqlAdapter).then(function (rs) {
            if (rs.success) {
                var data = rs.data;
                for (var j = 0; j < data.length; j++) {
                    if (data[j] !== null && data[j].CREATED_AT) {
                        data[j].CREATED_AT = new Date(data[j].CREATED_AT);
                    }
                }
                if(!refresh){
                    widget.model = jsonSafeParse(widget.model);
                }
                widget.data = {};
                widget.data.dataset = data;
                if (widget.title.toUpperCase().includes("CRON")) {
                    addOnClickConfirm();
                }
                if (data.length !== 0) {
                    $scope.isLoading = false;
                }
            }
            else {
                alertify.error(rs.message);
            }
        });
    }

    function getNextEmptyGridArea(defaultLocation) {
        var gridstack = angular.element('.grid-stack').gridstack($scope.gridstackOptions).data('gridstack');
        var location = jsonSafeParse(defaultLocation);
        while(! gridstack.isAreaEmpty(location.x, location.y, location.width, location.height)) {
            location.y = location.y + 11;
            if(location.y > 1100)
                break;
        }
        return jsonSafeStringify(location);
    }

    $scope.addDashboardWidget = function (widget) {
        widget.location = getNextEmptyGridArea(defaultWidgetLocation);
        var data = {"id": widget.id, "location": widget.location};
        DashboardService.AddDashboardWidget($stateParams.id, data).then(function (rs) {
            if (rs.success) {
                $scope.dashboard.widgets.push(widget);
                $scope.dashboard.widgets.forEach(function (widget) {
                    widget.location = jsonSafeStringify(widget.location);
                });
                loadDashboardData($scope.dashboard, false);
                alertify.success("Widget added");
                updateWidgetsToAdd();
            }
            else {
                alertify.error(rs.message);
            }
        });
    };

    $scope.deleteDashboardWidget = function (widget) {
        var confirmedDelete = confirm('Would you like to delete widget "' + widget.title + '" from dashboard?');
        if (confirmedDelete) {
            DashboardService.DeleteDashboardWidget($stateParams.id, widget.id).then(function (rs) {
                if (rs.success) {
                    $scope.dashboard.widgets.splice($scope.dashboard.widgets.indexOf(widget), 1);
                    $scope.dashboard.widgets.forEach(function (widget) {
                        widget.location = jsonSafeStringify(widget.location);
                    });
                    loadDashboardData($scope.dashboard, false);
                    alertify.success("Widget deleted");
                    updateWidgetsToAdd();
                }
                else {
                    alertify.error(rs.message);
                }
            });
        }
    };

    var isJSON = function (json) {
        try {
            JSON.parse(json);
            return false;
        } catch (e) {
            return true;
        }
    };

    function updateWidgetsToAdd () {
        $timeout(function () {
            if($scope.widgets && $scope.dashboard.widgets) {
                $scope.unexistWidgets =  $scope.widgets.filter(function(widget) {
                    var existingWidget = $scope.dashboard.widgets.filter(function(w) {
                        return w.id == widget.id;
                    });
                    return !existingWidget.length || widget.id != existingWidget[0].id;
                });
            }
        }, 800);
    };

    $scope.resetGrid = function () {
        var gridstack = angular.element('.grid-stack').gridstack($scope.gridstackOptions).data('gridstack');
        //gridstack.batchUpdate();
        $scope.pristineWidgets.forEach(function (widget) {
            var currentWidget = $scope.dashboard.widgets.filter(function(w) {
                return widget.id === w.id;
            })[0];
            if(currentWidget) {
                widget.location = jsonSafeParse(widget.location);
                currentWidget.location.x = widget.location.x;
                currentWidget.location.y = widget.location.y;
                currentWidget.location.height = widget.location.height;
                currentWidget.location.width = widget.location.width;
                var element = angular.element('#widget-' + currentWidget.id);
                gridstack.update(element, widget.location.x, widget.location.y,
                    widget.location.width, widget.location.height);
            }
        });
        gridstack.disable();
        //gridstack.commit();
    };

    function showGridActionToast() {
        $mdToast.show({
            hideDelay: 0,
            position: 'bottom right',
            scope: $scope,
            preserveScope: true,
            controller  : function ($scope, $mdToast) {
                $scope.updateWidgetsPosition = function(){
                    var widgets = [];
                    for(var i = 0; i < $scope.dashboard.widgets.length; i++) {
                        var currentWidget = $scope.dashboard.widgets[i];
                        var widgetData = {};
                        angular.copy(currentWidget, widgetData);
                        widgetData.location = JSON.stringify(widgetData.location);
                        widgets.push({'id': currentWidget.id, 'location': widgetData.location});
                    }
                    DashboardService.UpdateDashboardWidgets($stateParams.id, widgets).then(function (rs) {
                        if (rs.success) {
                            angular.copy(rs.data, $scope.pristineWidgets);
                            $scope.resetGrid();
                            $scope.closeToast();
                            alertify.success("Widget positions were updated");
                        }
                        else {
                            alertify.error(rs.message);
                        }
                    });
                };

                $scope.closeToast = function() {
                    $mdToast
                        .hide()
                        .then(function() {
                        });
                };
            },
            template : require('./widget-placement_toast.html')
        });
    };

    var setQueryParams = function(dashboardName){
        var params = ProjectProvider.getProjectsQueryParam();
        for(var i = 0; i<$scope.dashboard.attributes.length; i++){
            if ($scope.dashboard.attributes[i].key != null && $scope.dashboard.attributes[i].key == 'project'){
                params = "?projects=" + $scope.dashboard.attributes[i].value;
            }
        }
        params = params != "" ? params + "&dashboardName=" + dashboardName : params + "?dashboardName=" + dashboardName;
        if ($scope.currentUserId) {
            params = params + "&currentUserId=" + $scope.currentUserId;
        }
        return params;
    };

    $scope.asString = function (value) {
        if (value) {
            value = value.toString();
        }
        return value;
    };

    $scope.isFormatted = function (string) {
        var pattern = /^<.+>.*<\/.+>$/g;
        return pattern.test(string);
    };

    function jsonSafeParse (preparedJson) {
        if(!isJSON(preparedJson)) {
            return JSON.parse(preparedJson);
        }
        return preparedJson;
    };

    function jsonSafeStringify (preparedJson) {
        if(isJSON(preparedJson)) {
            return JSON.stringify(preparedJson);
        }
        return preparedJson;
    };

    $scope.sort = {
        column: null,
        descending: false
    };

    $scope.deleteWidget = function($event, widget){
        var confirmedDelete = confirm('Would you like to delete widget "' + widget.title + '" ?');
        if (confirmedDelete) {
            DashboardService.DeleteWidget(widget.id).then(function (rs) {
                if (rs.success) {
                    $scope.widgets.splice($scope.widgets.indexOfId(widget.id), 1);
                    if($scope.dashboard.widgets.indexOfId(widget.id) >= 0) {
                        $scope.dashboard.widgets.splice($scope.dashboard.widgets.indexOfId(widget.id), 1);
                    }
                    updateWidgetsToAdd();
                    alertify.success("Widget deleted");
                }
                else {
                    alertify.error(rs.message);
                }
            });
        }
    };

    $scope.changeSorting = function(widget, column) {
        var specCharRegexp = /[-[\]{}()*+?.,\\^$|#\s%]/g;

        if (column.search(specCharRegexp) != -1) {
            column.replace("\"\"", "\"");
         }
         if(! widget.sort) {
             widget.sort = {};
             angular.copy($scope.sort, widget.sort);
         }
        if (widget.sort.column == column) {
            widget.sort.descending = !widget.sort.descending;
        } else {
            widget.sort.column = column;
            widget.sort.descending = false;
        }
    };

    /*$scope.deleteDashboard = function(dashboard){
        var confirmedDelete = confirm('Would you like to delete dashboard "' + dashboard.title + '"?');
        if (confirmedDelete) {
            DashboardService.DeleteDashboard(dashboard.id).then(function (rs) {
                if (rs.success) {
                    alertify.success("Dashboard deleted");
                    var mainDashboard = $location.$$absUrl.substring(0, $location.$$absUrl.lastIndexOf('/'));
                    window.open(mainDashboard, '_self');
                }
                else {
                    alertify.error(rs.message);
                }
            });
        }
        $scope.hide();
    };*/

    // didn't find any call of this
    // $scope.showDashboardWidgetDialog = function (event, widget, isNew) {
    //     $mdDialog.show({
    //         controller: 'dashboardWidgetModalController',
    //         template: require('./dashboard-widget-modal/dashboard-widget-modal.html'),
    //         parent: angular.element(document.body),
    //         targetEvent: event,
    //         clickOutsideToClose: true,
    //         fullscreen: true,
    //         locals: {
    //             widget: widget,
    //             isNew: isNew,
    //             dashboardId: $stateParams.id
    //         }
    //     })
    //         .then(function (rs, action) {
    //         }, function () {
    //         });
    // };

    $scope.showDashboardSettingsModal = function (event, dashboard, isNew) {
        $mdDialog.show({
            controller: dashboardSettingsModalController,
            template: dashboardSettingsModalTemplate,
            parent: angular.element(document.body),
            targetEvent: event,
            clickOutsideToClose: true,
            fullscreen: true,
            autoWrap: false,
            locals: {
                dashboard: dashboard,
                isNew: isNew
            }
        })
        .then(function (rs) {
            if(rs) {
                switch(rs.action) {
                    case 'CREATE':
                        $state.go('dashboard', {id: rs.id});
                        $rootScope.dashboardList.splice(rs.position, 0, rs);
                        break;
                    case 'UPDATE':
                        rs.widgets = $scope.dashboard.widgets;
                        $scope.dashboard = angular.copy(rs);
                        $rootScope.dashboardList.splice(rs.position, 1, rs);
                        break;
                    default:
                        break;
                }
                delete rs.action;
            }
        }, function () {
        });
    };

    $scope.showWidgetDialog = function (event, widget, isNew, dashboard) {
        $mdDialog.show({
            controller: widgetDialogController,
            template: widgetDialog,
            parent: angular.element(document.body),
            targetEvent: event,
            clickOutsideToClose: true,
            fullscreen: true,
            locals: {
                widget: widget,
                isNew: isNew,
                dashboard: dashboard,
                currentUserId: $scope.currentUserId
            }
        })
        .then(function (rs) {
            if(rs) {
                switch(rs.action) {
                    case 'CREATE':
                        $scope.widgets.push(rs);
                        updateWidgetsToAdd();
                        break;
                    case 'UPDATE':
                        $scope.widgets.splice($scope.widgets.indexOfId(rs.id), 1, rs);
                        updateWidgetsToAdd();
                        break;
                    default:
                        break;
                }
                delete rs.action;
            }
        }, function () {
        });
    };

    $scope.showEmailDialog = function (event, widgetId) {
        $mdDialog.show({
            controller: dashboardEmailModalController,
            template: dashboardEmailModalTemplate,
            parent: angular.element(document.body),
            targetEvent: event,
            clickOutsideToClose: true,
            fullscreen: true,
            locals: {
                widgetId: widgetId
            }
        })
        .then(function () {}, function () {});
    };

    var toAttributes = function (qParams) {
        var attributes = [];
        for(var param in qParams) {
            var currentAttribute = {};
            currentAttribute.key = param;
            currentAttribute.value = qParams[param];
            attributes.push(currentAttribute);
        }
        return attributes;
    };

    var getQueryAttributes = function () {
        var qParams = $location.search();
        var qParamsLength = Object.keys(qParams).length;
        if(qParamsLength > 0 && $stateParams.id) {
            return toAttributes(qParams);
        }
    };

    $scope.getDataWithAttributes = function (dashboard, refresh) {
        var queryAttributes = getQueryAttributes();
        if(queryAttributes) {
            for (var i = 0; i < queryAttributes.length; i++) {
                dashboard.attributes.push(queryAttributes[i]);
            }
        }
        loadDashboardData(dashboard, refresh);
    };

    $scope.optimizeWidget = function (widget, index) {
        if (widget.type == 'table' && (Object.size(widget.data.dataset) == 0 || Object.size(widget.data.dataset) == index + 1)) {
            $timeout(function () {
                var gridstack = angular.element('.grid-stack').gridstack($scope.gridstackOptions).data('gridstack');
                $scope.gridstackOptions.disableResize = false;
                var el = angular.element('#' + widget.id)[0];
                var gridstackEl = angular.element('#widget-' + widget.id)[0];
                if(Object.size(widget.data.dataset) == 0) {
                    gridstack.resize(gridstackEl, widget.location.width, (Math.ceil(el.offsetHeight / $scope.gridstackOptions.cellHeight / 2)) + 2);
                } else {
                    gridstack.resize(gridstackEl, widget.location.width, (Math.ceil(el.offsetHeight / $scope.gridstackOptions.cellHeight / 2)) + 2);
                }
                $scope.gridstackOptions.disableResize = true;
            }, 100);
        }
    };


    var refreshIntervalInterval;

    function refresh() {
        const currentUser = UserService.currentUser;

        if (currentUser.isAdmin) {
            DashboardService.GetWidgets().then(function (rs) {
                if (rs.success) {
                    $scope.widgets = rs.data;
                    updateWidgetsToAdd();
                } else {
                    alertify.error(rs.message);
                }
            });
        }

        if($scope.dashboard.title && currentUser.refreshInterval && currentUser.refreshInterval !== 0) {
            refreshIntervalInterval = $interval(function () {
                loadDashboardData($scope.dashboard, true);
            }, currentUser.refreshInterval);
        }
    };

    $scope.stopRefreshIntervalInterval = function() {
        if (angular.isDefined(refreshIntervalInterval)) {
            $interval.cancel(refreshIntervalInterval);
            refreshIntervalInterval = undefined;
        }
    };

    $scope.$on('$destroy', function() {
        $scope.stopRefreshIntervalInterval();
    });

    function getDashboardById(dashboardId) {
        return $q(function (resolve, reject) {
            DashboardService.GetDashboardById(dashboardId).then(function (rs) {
                if (rs.success) {
                    $scope.dashboard = rs.data;
                    $scope.getDataWithAttributes($scope.dashboard, false);
                    resolve(rs.data);
                } else {
                    reject(rs.message);
                }
            });
        });
    }

    $scope.$watch(
        function() {
            if ($scope.currentUserId && $location.$$search.userId){
                return $scope.currentUserId !== $location.$$search.userId;
            }
        },
        function() {
            if ($scope.currentUserId && $location.$$search.userId) {
                if ($scope.currentUserId !== $location.$$search.userId) {
                    $scope.currentUserId = $location.search().userId;
                    getDashboardById($stateParams.id);
                }
            }
        }
    );

    $scope.$on("$event:widgetIsUpdated", function () {
        getDashboardById($stateParams.id);
    });

    $scope.$on('$destroy', function () {
        $scope.resetGrid();
    });

    function addOnClickConfirm() {
        $scope.$watch(function () {
            return angular.element('#cron_rerun').is(':visible')
        }, function () {
            var rerunAllLinks = document.getElementsByClassName("cron_rerun_all");
            Array.prototype.forEach.call(rerunAllLinks, function(link) {
                link.addEventListener("click", function (event) {
                    if (!confirm('Rebuild for all tests in cron job will be started. Continue?')) {
                        event.preventDefault();
                    }
                }, false);
            });
            var rerunFailuresLinks = document.getElementsByClassName("cron_rerun_failures");
            Array.prototype.forEach.call(rerunFailuresLinks, function(link) {
                link.addEventListener("click", function (event) {
                    if (!confirm('Rebuild for failures in cron job will be started. Continue?')) {
                        event.preventDefault();
                    }
                }, false);
            });
        });
    }

    (function init() {
        getDashboardById($stateParams.id).then(function (rs) {
            $timeout(function () {
                refresh();
            }, 0, false);
        });
    })();
};

export default dashboardController;
