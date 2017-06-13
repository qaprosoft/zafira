'use strict';

ZafiraApp.controller('DashboardsCtrl', [ '$scope', '$rootScope', '$http', '$location', 'ProjectProvider', '$modal', '$route', '$cookieStore', '$mdConstant', function($scope, $rootScope, $http, $location, ProjectProvider, $modal, $route, $cookieStore, $mdConstant) {

	$scope.dashboardId = $location.search().id;
	$scope.currentUserId = $location.search().userId;

	$scope.loadAllDashboards = function() {
		$http.get('dashboards/all' + ($scope.currentUserId != null ? '?userId=' + $scope.currentUserId : '')).then(function successCallback(dashboards) {
			$scope.dashboards = dashboards.data;
			if($scope.dashboardId == null && $scope.dashboards.length > 0)
			{
				$scope.dashboard = $scope.dashboards[0];
				$scope.dashboard.active = true;
				$scope.loadDashboardData($scope.dashboard);
			}
			else if($scope.dashboardId != null && $scope.dashboards.length > 0)
			{
				for(var i = 0; i < $scope.dashboards.length; i++)
				{
					if($scope.dashboards[i].id == $scope.dashboardId)
					{
						$scope.dashboard = $scope.dashboards[i];
						$scope.dashboard.active = true;
						$scope.loadDashboardData($scope.dashboard);
						break;
					}
				}
			}
		});
	};

	$scope.loadAllWidgets = function() {
		$http.get('widgets/all').then(function successCallback(widgets) {
			$scope.widgets = widgets.data;
		});
	};
	
	$scope.switchDashboard = function(id) {
		window.open($location.$$absUrl.split("?")[0] + "?id=" + id, '_self');
	};
	
	$scope.loadDashboardData = function(dashboard) {
		for(var i = 0; i < dashboard.widgets.length; i++)
		{
			if(!isSQLWidget(dashboard.widgets[i]))
			{
				$scope.loadWidget(dashboard.title, dashboard.widgets[i], dashboard.attributes);
			}
		}
	};
	
	$scope.loadWidget = function(dashboardName, widget, attributes) {
		var sqlAdapter = {};
		sqlAdapter.sql = widget.sql;
		sqlAdapter.attributes = attributes;
		var params = ProjectProvider.getProjectQueryParam();
        if ($scope.dashboard.attributes[0] != null && $scope.dashboard.attributes[0] != '%' && $scope.dashboard.attributes[0] != '') {
            params = "?project=" + $scope.dashboard.attributes[0].value;
        }
		params = params != "" ? params + "&dashboardName=" + dashboardName : params + "?dashboardName=" + dashboardName;
		if($scope.currentUserId != null)
		{
			params = params + "&currentUserId=" + $scope.currentUserId;
		}
		$scope.isLoading = true;
		$http.post('widgets/sql' + params, sqlAdapter).then(function successCallback(data) {
			var data = data.data;
			for(var j = 0; j < data.length; j++)
			{
				if(data[j].CREATED_AT)
				{
					data[j].CREATED_AT = new Date(data[j].CREATED_AT);
				}
			}
			
			if(!isSQLWidget(widget))
			{
				widget.model = JSON.parse(widget.model);
				widget.data = {};
				widget.data.dataset = data;
			}
			else
			{
				alertify.success('Query executed successfully');
			}
            if (data.length!=0){
                $scope.isLoading = false;
            }
		}, function errorCallback(data) {
			if(isSQLWidget(widget))
			{
				alertify.error('Query executed with failures');
			}
		});
	};
	
	var isSQLWidget = function(widget)
	{
		return 'sql' == widget.type;
	};
	
	(function init(){
		$scope.loadAllDashboards();
		$scope.loadAllWidgets();
	})();
	
	$scope.asString = function(value) {
		if(value != null)
		{
			value = value.toString();
		}
		return value;
	};
	
	$scope.openDashboardWidgetModal = function(widget, isNew) {
		$modal.open({
			templateUrl : 'resources/templates/dashboard-widget-details-modal.jsp',
			resolve : {
				'dashboardId' : function(){
					return $scope.dashboard.id;
				},
				'widget' : function(){
					return widget;
				},
				'isNew' : function(){
					return isNew;
				}
			},
			controller : function($scope, $modalInstance, isNew, dashboardId, widget){
				
				$scope.isNew = isNew;
				$scope.widget = widget;
				
				
				if(isNew)
				{
					$scope.widget.position = 0;
					$scope.widget.size = 4;
				}
				
				$scope.addDashboardWidget = function(widget){
					$http.post('dashboards/' + dashboardId + '/widgets', widget).then(function successCallback(data) {
						$route.reload();
					}, function errorCallback(data) {
						alertify.error('Failed to add widget');
					});
					$modalInstance.close(0);
				};
				
				$scope.deleteDashboardWidget = function(widget){
					$http.delete('dashboards/' + dashboardId + '/widgets/' + widget.id).then(function successCallback(data) {
						$route.reload();
					}, function errorCallback(data) {
						alertify.error('Failed to delete widget');
					});
					$modalInstance.close(0);
				};
				
				$scope.updateDashboardWidget = function(widget){
					$http.put('dashboards/' + dashboardId + '/widgets', {"id" : widget.id, "size" : widget.size, "position": widget.position}).then(function successCallback(data) {
						$route.reload();
					}, function errorCallback(data) {
						alertify.error('Failed to update widget');
					});
					$modalInstance.close(0);
				};
				
				$scope.cancel = function(){
					$modalInstance.close(0);
				};
			}
		}).result.then(function(data) {
        }, function () {
        });
	};
	
	$scope.openDashboardDetailsModal = function(dashboard, isNew){
		$modal.open({
			templateUrl : 'resources/templates/dashboard-details-modal.jsp',
			resolve : {
				'dashboard' : function(){
					return dashboard;
				},
				'isNew' : function(){
					return isNew;
				}
			},
			controller : function($scope, $modalInstance, dashboard, isNew){
				
				$scope.isNew = isNew;
				$scope.dashboard = dashboard;
				$scope.newAttribute = {};
				
				if($scope.isNew)
				{
					$scope.dashboard.type = 'GENERAL';
				}
				
				$scope.createDashboard = function(dashboard){
					$http.post('dashboards', dashboard).then(function successCallback(data){
						$route.reload();
					},function errorCallback(data){
						alertify.error('Failed to create dashboard');
					});
					$modalInstance.close(0);
				};

				$scope.updateDashboard = function(dashboard){
					dashboard.widgets = null;
					$http.put('dashboards', dashboard).then(function successCallback(data) {
						$route.reload();
					}, function errorCallback(data) {
						alertify.error('Failed to update dashboard');
					});
					$modalInstance.close(0);
				};
				
				$scope.deleteDashboard = function(dashboard){
					$http.delete('dashboards/' + dashboard.id).then(function successCallback(data) {
						window.open($location.$$absUrl.split("?")[0], '_self');
					}, function errorCallback(data) {
						alertify.error('Failed to delete dashboard');
					});
					$modalInstance.close(0);
				};
				
				// Dashboard attributes
				$scope.createAttribute = function(attribute){
					$http.post('dashboards/' + dashboard.id + '/attributes', attribute).then(function successCallback(rs){
						$scope.dashboard.attributes = rs.data;
						$scope.newAttribute = {};
						alertify.success('Dashboard attribute created');
					},function errorCallback(rs){
						alertify.error('Failed to create dashboard attribute');
					});
				};

				$scope.updateAttribute = function(attribute){
					$http.put('dashboards/' + dashboard.id + '/attributes', attribute).then(function successCallback(rs) {
						$scope.dashboard.attributes = rs.data;
						alertify.success('Dashboard attribute updated');
					}, function errorCallback(rs) {
						alertify.error('Failed to update dashboard attribute');
					});
				};
				
				$scope.deleteAttribute = function(attribute){
					$http.delete('dashboards/' + dashboard.id + '/attributes/' + attribute.id).then(function successCallback(rs) {
						$scope.dashboard.attributes = rs.data;
						alertify.success('Dashboard attribute removed');
					}, function errorCallback(rs) {
						alertify.error('Failed to delete dashboard attribute');
					});
				};
				
				$scope.cancel = function(){
					$modalInstance.close(0);
				};
			}
		}).result.then(function(data) {
        }, function () {
        });
	};
	
	$scope.openWidgetDetailsModal = function(widget, isNew){
		
		$modal.open({
			templateUrl : 'resources/templates/widget-details-modal.jsp',
			resolve : {
				'widget' : function(){
					return widget;
				},
				'isNew' : function(){
					return isNew;
				}
			},
			controller : function($scope, $modalInstance, widget, isNew){
				
				$scope.isNew = isNew;
				$scope.widget = widget;
				if($scope.isNew && $scope.widget != null)
				{
					$scope.widget.id = null;
				}

				$scope.createWidget = function(widget){
					$http.post('widgets', widget).then(function successCallback(data) {
						$route.reload();
					}, function errorCallback(data) {
						alertify.error('Failed to create widget');
					});
					$modalInstance.close(0);
				};

				$scope.updateWidget = function(widget){
					$http.put('widgets', widget).then(function successCallback(data) {
						$route.reload();
					}, function errorCallback(data) {
						alertify.error('Failed to update widget');
					});
					$modalInstance.close(0);
				};
				
				$scope.deleteWidget = function(widget){
					$http.delete('widgets/' + widget.id).then(function successCallback(data) {
						$route.reload();
					}, function errorCallback(data) {
						alertify.error('Failed to delete widget');
					});
					$modalInstance.close(0);
				};
				
				$scope.cancel = function(){
					$modalInstance.close(0);
				};
			}
		}).result.then(function(data) {
        }, function () {
        });
	};
	
	$scope.sort = {
        column: null,
        descending: false
    };
    
    $scope.changeSorting = function(column) {
        var sort = $scope.sort;
        if (sort.column == column) {
            sort.descending = !sort.descending;
        } else {
            sort.column = column;
            sort.descending = false;
        }
    };
    
    $scope.openEmailModal = function(){
		$modal.open({
			templateUrl : 'resources/templates/email-details-modal.jsp',
			controller : function($scope, $modalInstance, $mdConstant){
				
				$scope.title = "Zafira Dashboard";
				$scope.subjectRequired = true;
				$scope.textRequired = true;
				
				$scope.email = {};
				$scope.email.subject = "Zafira Dashboards";
				$scope.email.text = "This is auto-generated email, please do not reply!";
				$scope.email.hostname = document.location.hostname;
				$scope.email.urls = [document.location.href];
				$scope.email.recipients = [];
				$scope.users = [];
				$scope.keys = [$mdConstant.KEY_CODE.ENTER, $mdConstant.KEY_CODE.TAB, $mdConstant.KEY_CODE.COMMA, $mdConstant.KEY_CODE.SEMICOLON, $mdConstant.KEY_CODE.SPACE];

                var currentText;

				$scope.sendEmail = function(id){
                    if($scope.users.length == 0) {
                        if(currentText != null && currentText.length != 0) {
                            $scope.email.recipients.push(currentText);
                        } else {
                            alertify.error('Add a recipient!')
                            return;
                        }
                    }
					$modalInstance.close(0);
					$scope.email.recipients = $scope.email.recipients.toString();
					$http.post('dashboards/email', $scope.email).then(function successCallback(data) {
						alertify.success('Email was successfully sent!');
					}, function errorCallback(data) {
						alertify.error('Failed to send email');
					});
				};
                $scope.users_all = [];

                $scope.usersSearchCriteria = {};
                $scope.asyncContacts = [];
                $scope.filterSelected = true;

                $scope.querySearch = querySearch;
                var stopCriteria = '########';
                function querySearch (criteria) {
                    $scope.usersSearchCriteria.email = criteria;
                    currentText = criteria;
                    if(!criteria.includes(stopCriteria)) {
                        stopCriteria = '########';
                        return $http.post('users/search', $scope.usersSearchCriteria, {params: {q: criteria}})
                            .then(function (response) {
                                if (response.data.results.length == 0) {
                                    stopCriteria = criteria;
                                }
                                return response.data.results.filter(searchFilter($scope.users));
                            });
                    }
                    return "";
                }

                function searchFilter(addedUsers) {
                    return function filterFn(user) {
                        var users = addedUsers;
                        for(var i = 0; i < users.length; i++) {
                            if(users[i].id == user.id) {
                                return false;
                            }
                        }
                        return true;
                    };
                }


                $scope.checkAndTransformRecipient = function (currentUser) {
                    var user = {};
					if(currentUser.userName == null) {
						//user.userName = currentUser;
						user.email = currentUser;
						$scope.email.recipients.push(currentUser);
						$scope.users.push(user);
					} else {
						user = currentUser;
                        $scope.email.recipients.push(user.email);
                        $scope.users.push(user);
					}
					return user;
                }
                $scope.removeRecipient = function (user) {
                    var index = $scope.email.recipients.indexOf(user.email);
                    if (index >= 0) {
                        $scope.email.recipients.splice( index, 1 );
                    }
                }
				$scope.cancel = function(){
					$modalInstance.close(0);
				};
			}
		}).result.then(function(data) {
        }, function () {
        });
	};
	
}]);
