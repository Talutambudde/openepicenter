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
EpiCenter.panel.Charts = function(config) {
	
	var chartsId = Ext.id();
	
	var chartsContainer = chartsId + "-container";
	
	var noAnalysisText = "<a>No Analysis</a>";
	
	var whenWherePanel = new EpiCenter.lib.WhenWherePanel({
		region: "north",
		border: true,
		height: 90,
		linkText: noAnalysisText
	});
	
	whenWherePanel.historyField.setValue("1 month");
	
	var dataTypePanel = new EpiCenter.lib.DataTypePanel({
		region: "center"
	});
	
	whenWherePanel.on("accept", renderCharts);
	
	var algorithm;
	
	var advancedOptionsId = Ext.id();
	
	var analyzerDetails;
	
	var combinedResults = true;
	
	var dataConditioning = EpiCenter.common.conditioningDataStore.getAt(0).get("id");
	
	var dataRepresentation = EpiCenter.common.representationDataStore.getAt(0).get("id");
	
	var algorithmMenu = createAlgorithmMenu();
	
		
	whenWherePanel.on("optionsclick", function(cmp, e) {
		if (e.getTarget().id == advancedOptionsId) {
			if (analyzerDetails) {
				analyzerDetails.close();
			}
			analyzerDetails = new EpiCenter.lib.AnalyzerDetails({
				algorithm: algorithm
			});
			analyzerDetails.show();
		}
		else {
			algorithmMenu.show(cmp.body);
		}
	});
	
	var chartPanel = new Ext.Panel({
		region: "center",
		layout: "absolute",
		autoScroll: true,
		html: '<div id="' + chartsId + '"><div id="' + chartsContainer + '" class="chart-container"></div></div>',
		listeners: {
			"resize": {
				fn: reposition.createDelegate(this)
			}
		}
	});
	
	function createAlgorithmMenu() {
		
		var algorithmMenu = new Ext.menu.Menu();
	
		EpiCenter.common.algorithmDataStore.each(function(item)  {
			algorithmMenu.add({
				text: item.get("value"),
				checked: item.get("id") == algorithm,
				group: "chartAlgorithm",
				key: item.get("id"), 
				checkHandler: switchAlgorithm,
				scope: this
			});
		}, this);

		algorithmMenu.addSeparator();
		
		var dcMenu = new Ext.menu.Menu();
		
		EpiCenter.common.conditioningDataStore.each(function(item) {
			dcMenu.add({
				text: item.get("value"),
				checked: item.get("id") == dataConditioning,
				group: "chartConditioning",
				key: item.get("id"),
				checkHandler: function(item) {
					dataConditioning = item.key;
				},
				scope: this
			});
		}, this);
		
		algorithmMenu.add({
			text: "Data Conditioning",
			menu: dcMenu
		});
		
		var drMenu = new Ext.menu.Menu();
		
		EpiCenter.common.representationDataStore.each(function(item) {
			drMenu.add({
				text: item.get("value"),
				checked: item.get("id") == dataRepresentation,
				group: "chartRepresentation",
				key: item.get("id"),
				checkHandler: function(item) {
					dataRepresentation = item.key;
				},
				scope: this
			});
		}, this);
		
		algorithmMenu.add({
			text: "Data Representation",
			menu: drMenu
		});
		
		algorithmMenu.addSeparator();
		
		algorithmMenu.add({
			text: "Advanced Options",
			menu: {
				items: [ {
					text: "Combined Results",
					checked: true,
					checkHandler: function(menu, value) {
						combinedResults = value;
					},
					scope: this
				} ]
			}
		});
		
		return algorithmMenu;
	}
	
	function switchAlgorithm(item) {
		
		if (analyzerDetails) {
			analyzerDetails.close();
		}
		algorithm = item.key;
		if (item.key) {
			whenWherePanel.setLinkText("<div id='" + advancedOptionsId + "' class='icon-gear' style='cursor: pointer; background-repeat: no-repeat; padding-left: 18px;'><a>" + item.text + "</a></div>");
		}
		else {
			whenWherePanel.setLinkText(noAnalysisText);
		}
	}
	
	function reposition() {
		var chartsWidth = (Math.floor(Ext.get(chartsId).getWidth() / 330)) * 330;
		Ext.get(chartsContainer).setWidth(chartsWidth);
	}
	
	function getParameters() {
	
		return Ext.apply(dataTypePanel.getValues(), Ext.apply(whenWherePanel.getValues(), {
			algorithmName: algorithm,
			algorithmProperties: (analyzerDetails ? analyzerDetails.getValues() : null),
			conditioning: dataConditioning,
			representation: dataRepresentation
		}));
	}

	this.setParameters = function(params) {
		whenWherePanel.setValuesFromParameters(params);
		dataTypePanel.setClassifier(params.classifier);
		dataTypePanel.setSelectedCategories(params.category);
		dataTypePanel.setSelectedAgeGroups(params.ageGroup);
		dataTypePanel.setSelectedGenders(params.gender);
		dataTypePanel.setLocation(params.location);
		
		dataRepresentation = params.representation;
		dataConditioning = params.conditioning;
	};

	function renderCharts(){
	
		if (whenWherePanel.getForm().isValid() && dataTypePanel.isValid()) {
		
			if (whenWherePanel.getVisibilityForSelectedGeography() == "AGGREGATE_ONLY" && dataTypePanel.getValues().location == "FACILITY") {
				whenWherePanel.geographyField.markInvalid("Facility level data is not available for this region.");
			} else {
				
				var params = getParameters();
				var title;
				
				if (params.classifier == "TOTAL") {
					title = dataTypePanel.getSelectedDataType();
					renderChart(params, title);
					
				} else if (combinedResults) {
					title = dataTypePanel.getSelectedCategories().join(", ");
					renderChart(params, title);
					
				} else {
					var cloned = Object.clone(params);
					var selected = dataTypePanel.getSelectedCategoriesAsObject();
					Ext.each(Object.keys(selected), function(id) {
						cloned.category = id;
						title = selected[id];
						renderChart(cloned, title);
					}, this);
				}
				
			}
		}
	}
	
	function renderChart(params, title) {
		
		var chart = new EpiCenter.lib.Chart(chartsContainer, {
			height: 200,
			width: 300,
			closable: true,
			clickable: true,
			constrainTo: chartPanel.body,
			title: whenWherePanel.geographyField.getTextValue() + " - " + title
		});
		
		chartPanel.body.scroll("down",  chartPanel.body.dom.scrollHeight, true);
		
		AnalysisService.getTimeSeriesChart(params, chart.wrapDwrCallback(function(chartData){
			chart.load(chartData);
			reposition();
			
			chart.on("click", function(){
				var details = new EpiCenter.lib.ChartDetails({
					parameters: params,
					title: chart.getTitle(),
					originalChart: chart,
					disableCases: whenWherePanel.getVisibilityForSelectedGeography() == "AGGREGATE_ONLY"
				});
				console.log("params:", params);
				details.show();
			});
		}));		
	}
	
	EpiCenter.panel.Charts.superclass.constructor.call(this, {
		title: "Charts",
		layout: "border",
		border: false,
		items: [{
			region: "west",
			layout: "border",
			minSize: 260,
			maxSize: 260,
			collapsible: true,
			collapseMode: "mini",
			split: true,
			border: false,
			width: 260,
			items: [ whenWherePanel, dataTypePanel ]
		}, chartPanel ]
	});
	
};

Ext.extend(EpiCenter.panel.Charts, Ext.Panel);
