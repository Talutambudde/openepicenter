/*
 * Copyright (C) 2008 University of Pittsburgh
 * 
 * 
 * This file is part of Open EpiCenter
 * 
 *     Open EpiCenter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Open EpiCenter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with Open EpiCenter.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 *   
 */
EpiCenter.common = function(){

	var templateSources = {
		anomalyTitle: '<p>{timestamp:date("m/d/Y")} {category} by {algorithmName} in <tpl for="geography">{name}</tpl> found {actualValue} visits with maximum of {actualThreshold}</p>'
	};

	var templates = {};

	$H(templateSources).each(function(pair) {
		templates[pair.key] = new Ext.XTemplate(pair.value);
	});

	// Simple record type for key-value pairs from DWR
	var simpleRecordType = Ext.data.Record.create([{
		name: 'id'
	}, {
		name: 'value'
	}, {
		name: 'description'
	}]);
	
	// Record type for Geography
	var geographyRecordType = Ext.data.Record.create([{
		name: 'id'
	}, {
		name: 'name'
	}, {
		name: 'visibility'
	}]);

	// Record type for Events
	var eventRecordType = Ext.data.Record.create([{
		name: 'id'
	}, {
		name: 'timestamp'
	}, {
		name: 'description'
	}, {
		name: 'state'
	}, {
		name: 'owner'
	}, {
		name: 'algorithmName'
	}, {
		name: 'geography'
	}, {
		name: 'geographyName',
		mapping: 'geography.name'
	}, {
		name: 'category'
	}, {
		name: 'actualValue'
	}, {
		name: 'actualThreshold'
	}, {
		name: 'locationPoint'
	}]);
	
	// Record type for Investigations
	var investigationRecordType = Ext.data.Record.create([{
		name: 'id'
	}, {
		name: 'timestamp'
	}, {
		name: 'description'
	}, {
		name: 'anomalies'
	}, {
		name: 'organizationName'
	}, {
		name: 'organizationPoint'
	}, {
		name: 'createdBy'
	}, {
		name: 'assignedTo'
	}]);
			
	// Users Store
	var usersDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(WorkflowService.getUsersInOrganization),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	// States dataStore
	var statesDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(GeographyService.getStates),
		reader: new Ext.data.ObjectReader({}, geographyRecordType),
		sortInfo: {
			field: "name",
			direction: "ASC"
		}
	});

	// Locations dataStore
	var locationsDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(MetadataService.getLocations),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	// Algorithms dataStore
	var algorithmDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(AnalysisService.getAlgorithms),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	// PosteriorAlgorithms dataStore
	var posteriorAlgorithmDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(AnalysisService.getPosteriorAlgorithms),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	// AgeGroup dataStore
	var ageGroupDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(MetadataService.getAgeGroups),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	// Genders dataStore
	var genderDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(MetadataService.getGenders),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	// Data representation methods
	var representationDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(MetadataService.getDataRepresentationMethods),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	// Data conditioning methods
	var conditioningDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(MetadataService.getDataConditioningMethods),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	// Descriptive analysis types
	var descriptiveAnalysisDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(MetadataService.getDescriptiveAnalysisTypes),
		reader: new Ext.data.ObjectReader({}, simpleRecordType)
	});
	
	var userinfo = new EpiCenter.Userinfo();
	
	function readCookie(name) {
		var nameEQ = name + "=";
		var ca = document.cookie.split(';');
		for(var i=0;i < ca.length;i++) {
			var c = ca[i];
			while (c.charAt(0)==' ') {
				c = c.substring(1,c.length);
			}
			if (c.indexOf(nameEQ) === 0) {
				return c.substring(nameEQ.length,c.length);
			}
		}
		return null;
	}
	
	/*
	 * We can enable active reverse ajax for Firefox.
	 * IE pukes violently (surprise), and Opera is a bit buggy.
	 */
	if (Ext.isGecko) {
		// dwr.engine.setActiveReverseAjax(true);	
	}
	
	/*
	 * DWR exception handler
	 */
	function globalDwrErrorHandler(error, exception) {
		if (error) {
			Ext.Msg.show({
				title: "Error",
				msg: error + (exception ? "<br/>" + exception : ""),
				buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
			});
		}
	}

	dwr.engine.setErrorHandler(globalDwrErrorHandler);
	
	/*
	 * Handler invoked when the user tries to navigate away from the application
	 * (back button clicked, etc).  We show a message to make sure that this is what
	 * they want, since the application will close.
	 */
	var catchUnload = true;
	var unloadWarningText = "This will exit the EpiCenter application.";
	window.onbeforeunload = function() {
		if (catchUnload) {
			return unloadWarningText;
		}
	};
	
	/*
	 * Handler invoked when DWR gets an unexpected text/html response.
	 * This indicates that the session has timed out.  We display a message
	 * to the user, and redirect them to the login page.  In the future, we
	 * can have them dynamically reauthenticate using ajax.
	 */
	function globalDwrTextHtmlHandler() {
		Ext.Msg.show({
			title: "Session Timed Out",
			msg: "Your session has timed out due to inactivity.  Press OK to go to the login page.",
			buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.WARNING,
			fn: function(btn) {
				catchUnload = false;
				window.location.reload(true);
			}
		});
	}

	dwr.engine.setTextHtmlHandler(globalDwrTextHtmlHandler);

	Ext.ux.StoreSynchronizer.sync([ usersDataStore, statesDataStore, locationsDataStore,
		algorithmDataStore, posteriorAlgorithmDataStore, ageGroupDataStore, genderDataStore ], function() {

			EpiCenter.common.loaded = true;
			EpiCenter.common.fireEvent("load");
	});
	
	dwr.engine.beginBatch();
	
	userinfo.load();
	
	MetadataService.getDataTypes(function(result) {
		EpiCenter.common.dataTypes = result;
	});
	
	EventService.getDateOfOldestAnomaly(function(result) {
		EpiCenter.common.oldestAnomaly = result;
	});
	
	WorkflowService.getEventDispositions(function(result) {
		EpiCenter.common.eventDispositions = result;
	});
	
	WorkflowService.getDateOfOldestInvestigation(function(result) {
		EpiCenter.common.oldestInvestigation = result;
	});
	
	usersDataStore.load();
	statesDataStore.load();
	locationsDataStore.load();
	algorithmDataStore.load();
	posteriorAlgorithmDataStore.load();
	ageGroupDataStore.load();
	genderDataStore.load();
	representationDataStore.load();
	conditioningDataStore.load();
	descriptiveAnalysisDataStore.load();
	
	dwr.engine.endBatch();
	
	return Ext.apply(new Ext.util.Observable(), {
	
		simpleRecordType: simpleRecordType,
		
		geographyRecordType: geographyRecordType,
		
		eventRecordType: eventRecordType,
		
		investigationRecordType: investigationRecordType,
				
		usersDataStore: usersDataStore,
		
		statesDataStore: statesDataStore,
						
		locationsDataStore: locationsDataStore,
		
		algorithmDataStore: algorithmDataStore,

		posteriorAlgorithmDataStore: posteriorAlgorithmDataStore,
		
		ageGroupDataStore: ageGroupDataStore,
		
		genderDataStore: genderDataStore,
		
		representationDataStore: representationDataStore,
		
		conditioningDataStore: conditioningDataStore,
		
		descriptiveAnalysisDataStore: descriptiveAnalysisDataStore,
		
		userinfo: userinfo,

		templates: templates,

		templateSources: templateSources,

		globalDwrErrorHandler: globalDwrErrorHandler,

		globalDwrTextHtmlHandler: globalDwrTextHtmlHandler,
		
		loaded: false,
		
		events: {
			load: true
		},
		
		afterLoad: function(callback, scope) {

			if (this.loaded) {
				if (scope) {
					callback.createDelegate(scope).call();
				} else {
					callback.call();
				}
			} else {
				this.on("load", callback, (scope ? scope : this), { single: true });
			}
		},
				
		getSessionId: function() {
			return readCookie("JSESSIONID");
		},
		
		setCatchUnload: function(unload) {
			catchUnload = unload;
		}
	});
}();
