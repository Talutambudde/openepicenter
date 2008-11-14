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
EpiCenter.lib.AnomalyDetails = function() {
	
	var chartContainer = Ext.id();
	
	var chartContainer2 = Ext.id();
	
	var detailsContainer = Ext.id();
	
	var newInvestigationId = Ext.id();
	
	var existingInvestigationId = Ext.id();
	
	var dispositionId = "disposition-" + Ext.id();
	
	var analysisTypeId = Ext.id();
	
	var resultTypeId = Ext.id();
	
	var currentAnomaly;
	
	var currentAnalysisType = EpiCenter.common.descriptiveAnalysisDataStore.getAt(0);
	
	var currentAnalysisResultType = "ACTUAL";
	
	var currentResultType = "ACTUAL";
	
	var resultTypes = [ {
		id: "NORMALIZED",
		name: "Percentage of Total"
	}, {
		id: "ACTUAL",
		name: "Actual Value"
	}, {
		id: "POPULATION",
		name: "Population Rate"
	}];
	
	var detailsTemplate = new Ext.XTemplate('<tpl for=".">',
		'<div class="anomaly-details-inner">',
		'<p>Monitoring of emergency department admissions ',
		'<tpl if="location == \'HOME\'">for residents of </tpl>',
		'<tpl if="location != \'HOME\'">in </tpl>',
		'<tpl for="geography">{name}</tpl> identified',
		' {actualValue} interactions classified as {category} {attributeInfo}by the {classifierName} classifier. All interactions occurred between {startDateTimeString}',
		' and {endDateTimeString} and were totaled by {locationString}.</p>',
		'<p>Using {algorithmName} analysis, these {actualValue} interactions exceed the predicted value of {predictedValue}',
		' and the maximum threshold of {actualThreshold}.</p>',
		'<p>The percentage of all records for <tpl for="geography">{name}</tpl> was {normalizedValue}% with a maximum of {normalizedThreshold}%.</p>',
		'<p>The anomaly was detected at {detectionTimeString}.</p>',
		
		'<tpl if="investigations.length &gt; 0">',
		'<p>This anomaly is currently part of the following investigations:</p>',
		'<ol><tpl for="investigations">',
		'<li>{#}. <a id="investigationlink-{id}">{description}</a> ({organizationName})</li>',
		'</tpl></ol><br/></tpl>',
		'<tpl for="disposition">',
		'<p>The current disposition of this anomaly is <a id="{parent.dispositionId}">{name}</a>.</p>',
		'<tpl if="type == \'INITIAL\' || type == \'TRANSITIONAL\'">',
		'<p>This anomaly is available for a <a id="{parent.newInvestigationId}">new investigation</a>,',
		' or inclusion in an <a id="{parent.existingInvestigationId}">existing investigation</a>.',
		'</p></tpl>',
		'</div></tpl>');
		
	var detailsPanel = new Ext.Panel({
		fill: true,
		columnWidth: 0.5,
		autoScroll: true,
		title: " ",
		html: '<div id="' + detailsContainer + '" class="anomaly-details"></div>'
	});
	
	var detailsMap = new EpiCenter.lib.MapPanel({
		mapMode: 'small',
		featureControlEnabled: true
	});

	var detailsChartPanel = new Ext.Panel({
		layout: "fit",
		border: false,
		bodyStyle: "padding: 2px 0 0 0;",
		html: '<div id="' + chartContainer + '" style="text-align: center; margin-left: auto; margin-right: auto"></div>'
	});
	
	var analysisChartPanel = new Ext.Panel({
		layout: "fit",
		border: false,
		bodyStyle: "padding: 2px 0 0 0;",
		html: '<div id="' + chartContainer2 + '" style="text-align: center; margin-left: auto; margin-right: auto"></div>'
	});
		
	var resultTypeMenu = createResultTypeMenu();
	
	var descriptiveAnalysisMenu = createDescriptiveAnalysisMenu();
	
	var investigationsStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(WorkflowService.getActiveInvestigations, null, true),
		reader: new Ext.data.ObjectReader({
			root: "items",
			totalProperty: "totalItems"
		}, EpiCenter.common.investigationRecordType)
	});

	investigationsStore.on("load", function(store) {
		if (currentAnomaly.investigations !== undefined) {
			Ext.each(currentAnomaly.investigations, function(i) {
				var ii = store.find("id", i.id);
				if (ii > -1) {
					store.remove(store.getAt(ii));
				}
			}, this);
		}
	}, this);
	
    var investigationsTemplateString = '{timestamp:date("m/d/Y")} {description} ({organizationName})';

	var dropdownPanel = new Ext.ux.PseudoCombobox({}, {
		store: investigationsStore,
		templateString: investigationsTemplateString,
		callback: function(investigationId) {
			WorkflowService.addEventToInvestigation(investigationId, currentAnomaly.id, function(investigations) {
				currentAnomaly.investigations = investigations;
				renderDetailsText(currentAnomaly);
				EpiCenter.core.Viewport.activateInvestigation(investigationId);
			}.createDelegate(this));
		},
		scope: this
	});
	
	
	var chartResizingTask = new Ext.util.DelayedTask(function() {
		if (currentAnomaly) {
			console.log("chart resize");
			renderChart(currentAnomaly, currentResultType);
		}
	}, this);
			
	var chartResizingTask2 = new Ext.util.DelayedTask(function() {
		if (currentAnomaly) {
			console.log("chart resize 2");
			renderDescriptiveChart(currentAnomaly, currentAnalysisType, currentAnalysisResultType);
		}
	}, this);
			
	var chartsReady = false;
			
	this.showDetails = function(anomalyId, letter) {
		
		this.el.mask("Loading Details..", "x-mask-loading");
		
		EventService.getEventDetail(anomalyId, function(anomaly) {
				
			dwr.engine.beginBatch();
			
			currentAnomaly = anomaly;	
			renderDetailsText(anomaly);
			
			var params = getAnalysisParameters(anomaly);
						
			renderChart(anomaly, currentResultType);
			renderDescriptiveChart(anomaly, currentAnalysisType, currentAnalysisResultType);
			dwr.engine.endBatch();
			
			// This is needed so that the resize handler doesn't fire for no reason when first rendering
			// It is a cheap hack to prevent racing
			if (!chartsReady) {
				detailsChartPanel.on("resize", function() { chartResizingTask.delay(100); }, this);
				analysisChartPanel.on("resize", function() { chartResizingTask2.delay(100); }, this);
				chartsReady = true;
			}
			
			// Generate the map
			detailsMap.clearMarkers();
			detailsMap.changeMapExtents(anomaly.envelope);
			detailsMap.addMarker(anomaly.locationPoint, { title: anomaly.geography.name, content: EpiCenter.common.templates.anomalyTitle.applyTemplate(anomaly), letter: letter });
					
			// TODO: Make the algorithm switchable
			detailsMap.updateMap(params, params.algorithmName);
			detailsMap.showMap();
			
			this.el.unmask();
			
		}.createDelegate(this));
	};
	
	function getAnalysisParameters(anomaly) {
		return {
			classifier: anomaly.classifierId,
			datatype: anomaly.dataTypeId,
			category: anomaly.categoryId,
			start: anomaly.timestamp.getTime(),
			end: anomaly.timestamp.getTime(),
			algorithmName: anomaly.associatedAlgorithmName,
			categoryTitle: anomaly.category,
			location: anomaly.location,
			feature: anomaly.geography.type,
			attributes: anomaly.attributes,
			fixDates: false
		};
	}
	
	function getParameters(anomaly) {
		return {
			classifier: anomaly.classifierId,
			datatype: anomaly.dataTypeId,
			category: anomaly.categoryId,
			start: anomaly.timestamp.add(Date.DAY, -1),
			end: anomaly.timestamp,
			geography: anomaly.geography.id,
			location: anomaly.location,
			attributes: anomaly.attributes,
			fixDates: false
		};
	}
	
	function renderChart(anomaly, resultType) {

		var chart = new EpiCenter.lib.Chart(chartContainer, {
			name: "anomaly-chart",
			clickable: true,
			height: detailsChartPanel.body.getHeight() - 32,
			width: detailsChartPanel.body.getWidth() - 28,
			title: anomaly.geography.name + " - " + anomaly.category + " <a id='" + resultTypeId + "'>(" + resultTypes.find(function(n) { return n.id == resultType; }).name + ")</a>"
		});
		
		chart.on("click", function() {
			var details = new EpiCenter.lib.ChartDetails({
				parameters: getParameters(anomaly),
				title: anomaly.geography.name + " - " + anomaly.category + " (" + resultTypes.find(function(n) { return n.id == resultType; }).name + ")",
				eventId: anomaly.id,
				historyDays: 60,
				originalChart: chart,
				disableCases: anomaly.geography.visibility == "AGGREGATE_ONLY"
			});
			details.show();
		}, this);
		
		chart.on("render", function() {
			var el = Ext.get(resultTypeId);
			el.on("click", function() {
				resultTypeMenu.show(el);
			}, this);
		}, this);
			
		EventService.getEventChart(anomaly.id, resultType, chart.wrapDwrCallback(function(chartURL) {
			chart.load(chartURL);	
		}));
	}
	
	function renderDescriptiveChart(anomaly, analysisType, resultType) {
		
		var v = Ext.get(chartContainer2);
		var chart = new EpiCenter.lib.Chart(chartContainer2, {
			name: "anomaly-descriptive-chart",
			height: analysisChartPanel.body.getHeight() - 32,
			width: analysisChartPanel.body.getWidth() - 28,
			title: "Distribution of Patient Records by <a id='" + analysisTypeId + "'>" + analysisType.get("value") + "</a>"
		});
		
		chart.on("render", function() {
			var el = Ext.get(analysisTypeId);
			el.on("click", function() {
				descriptiveAnalysisMenu.show(el);
			}, this);
		}, this);
			
		EventService.getDescriptiveAnalysisChart(anomaly.id, analysisType.get("id"), resultType, chart.wrapDwrCallback(function(chartURL) {		
			chart.load(chartURL);
		}));
	}
	
	function renderDetailsText(anomaly) {

		var dateTimeFormat = "g:i a T \\o\\n F d, Y";
		
		var detailsContainerEl = Ext.get(detailsContainer);
		anomaly.endDateTimeString = anomaly.timestamp.format(dateTimeFormat);
		anomaly.startDateTimeString = anomaly.timestamp.add(Date.DAY, -1).format(dateTimeFormat);
		anomaly.detectionTimeString = anomaly.detectionTimestamp.format(dateTimeFormat);

		// Render the details
		anomaly.newInvestigationId = newInvestigationId;
		anomaly.existingInvestigationId = existingInvestigationId;
		anomaly.dispositionId = dispositionId;
		anomaly.locationString = anomaly.location == "HOME" ? "patient location" : "facility location";
		
		detailsTemplate.overwrite(detailsContainerEl, anomaly);	
		
		detailsPanel.setTitle(anomaly.category + " in " + anomaly.geography.name + " on " + anomaly.timestamp.format("m/d/Y") + " (" + anomaly.id + ")");
		
		// Link to create a new investigation
		var createLink = Ext.get(anomaly.newInvestigationId);
		if (createLink) {
			createLink.on("click", function(el){
				EpiCenter.core.Viewport.panels.investigationPanel.createNewInvestigation(anomaly.id, anomaly.geography, createLink);
			}, this);
		}
				
		// Link to add to existing investigation
		createExistingInvestigationMenu();
		
		// Links to current investigations
		var ins = Ext.select("a[id^=investigationlink-]", detailsContainerEl);
		ins.on("click", function(event) {
			var id = event.getTarget().id.split("-")[1];
			EpiCenter.core.Viewport.activateInvestigation(id);
		}, this);
			
		var dispositionMenu = createDispositionMenu();
		var dispositionLink = Ext.get(dispositionId);
		dispositionLink.on("click", function() {
			dispositionMenu.show(dispositionLink);
		}, this);
	}
	
	function createDispositionMenu() {
		
		var menu = new Ext.menu.Menu({
			id: "dispositionMenu"
		});
		
		EpiCenter.common.eventDispositions.each(function(item) {
			menu.add({
				text: item.name,
				key: item.id,
				obj: item,
				checked: currentAnomaly.disposition.id == item.id,
				group: "disposition"
			});
		}, this);
		
		menu.on("itemclick", function(item) {
			Ext.Msg.show({
				title: "Change Anomaly Disposition",
				icon: Ext.MessageBox.WARNING,
				buttons: Ext.MessageBox.YESNO,
				animEl: dispositionId,
				width: 400,
				msg: "You are about to change the disposition of this anomaly, which may affect visibility for other users.  Are you sure?",
				fn: function(value) {
					if (value == "yes") {
						EventService.updateEventState(currentAnomaly.id, item.key, function() {
							currentAnomaly.disposition = item.obj;
							renderDetailsText(currentAnomaly);
						});
					}
				}
			});
		}, this);
		
		return menu;
	}
	
	function createResultTypeMenu() {
	
		var menu = new Ext.menu.Menu({
			id: "resultTypeMenu"
		});
		
		Ext.each(resultTypes, function(resultType) {
			menu.add({
				text: resultType.name,
				key: resultType.id,
				checked:  resultType.id == currentResultType,
				group: "resultType"
			});
		}, this);
		
		menu.on("itemclick", function(item) {
			currentResultType = item.key;
			renderChart(currentAnomaly, currentResultType);
		}, this);
		
		return menu;
		
	}
	
	function createDescriptiveAnalysisMenu() {
		
		var menu = new Ext.menu.Menu({
			id: "descriptiveAnalysisMenu"
		});
		
		EpiCenter.common.descriptiveAnalysisDataStore.each(function(item) {
			menu.add({
				text: item.get("value"),
				key: item.get("id"),
				checked: currentAnalysisType.get("id") == item.get("id"),
				group: "analysisType"
			});
		}, this);
		
		menu.addSeparator();
		
		Ext.each(resultTypes, function(resultType) {
			menu.add({
				text: resultType.name,
				key: resultType.id,
				checked: currentAnalysisResultType == resultType.id,
				group: "analysisResultType"
			});
		}, this);
		
		menu.on("itemclick", function(item) {
			
			if (item.group == "analysisType") {
				currentAnalysisType = EpiCenter.common.descriptiveAnalysisDataStore.getAt(EpiCenter.common.descriptiveAnalysisDataStore.find("id", item.key));
			} else if (item.group == "analysisResultType") {
				currentAnalysisResultType = item.key;
			}		
			renderDescriptiveChart(currentAnomaly, currentAnalysisType, currentAnalysisResultType);
		}, this);
		
		return menu;
	}

	function createExistingInvestigationMenu() {
		
		dropdownPanel.refresh();
		
		var existingInvestigationEl = Ext.get(existingInvestigationId);
		if (existingInvestigationEl) {
			existingInvestigationEl.on("click", function(e){
				dropdownPanel.display(existingInvestigationEl);
			}, this);
		}
		
		return;
	}
	
	EpiCenter.lib.AnomalyDetails.superclass.constructor.call(this, {
		layout: "anchor",
		border: false,
		cls: 'details-panel',
		items: [ {
			anchor: '100% 55%',
			layout: "column",
			border: false,
		 	items: [ detailsPanel, {
				fill: true,
				layout: "fit",
				columnWidth: 0.5,
				border: false,
				frame: false,
				items: detailsMap
			} ]
		}, {
			anchor: '100% 45%',
			layout: "column",
			border: false,
			frame: false,
			items: [ {
				fill: true,
				layout: 'fit',
				columnWidth: 0.5,
				border: false,
				frame: false,
		 		items: analysisChartPanel
			}, {
				fill: true,
				layout: 'fit',
				columnWidth: 0.5,
				border: false,
				frame: false,
		 		items: detailsChartPanel
			} ]
		} ]
	});

};

Ext.extend(EpiCenter.lib.AnomalyDetails, Ext.Panel);
