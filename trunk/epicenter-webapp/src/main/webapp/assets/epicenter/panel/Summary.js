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
EpiCenter.panel.Summary = function(){

	var chartsId = Ext.id();
	
	var chartsContainer = chartsId + "-container";
	
	// Geography ComboBox with autocomplete	
	var whenWherePanel = new EpiCenter.lib.WhenWherePanel({
		border: false
	});
	
	var totalDataType = "Emergency Department Registrations";
	
	var rendered = false;
	
	whenWherePanel.historyField.disable();
	whenWherePanel.endDateField.disable();
	whenWherePanel.on("accept", updateCharts);

	var adjustedWidth;
	
	var lastSize;
	
	function updateCharts(){
		
		adjustedWidth = Math.round(chartsPanel.body.getSize().width / 2) - 80;
		var adjustedHeight = Math.round(adjustedWidth * 0.75);
		reposition();
		
		var dataType = EpiCenter.common.dataTypes.find(function(s) {
			return s.name == totalDataType;
		});
		
		var totalChart = new EpiCenter.lib.Chart(chartsContainer, {
			name: chartsId + "-total",
			height: adjustedHeight,
			width: adjustedWidth,
			title: whenWherePanel.geographyField.getTextValue() + " - " + totalDataType
		});
		
		var v = whenWherePanel.getValues();
		
		// Show the totals
		AnalysisService.getTotalChart(Ext.apply(v, {
		
			location: "HOME",
			datatype : dataType.id
			
		}), totalChart.wrapDwrCallback(function(chartData) {
					totalChart.load(chartData);
			})
		);
		
		// Show each classifier
		dwr.engine.beginBatch();
		
		EpiCenter.common.dataTypes.each(function(dataType){
			
			dataType.classifiers.each(function(classifier) {
				
				if (classifier.id != "TOTAL") {
					var chart = new EpiCenter.lib.Chart(chartsContainer, {
						name: chartsId + "-" + classifier.id,
						height: adjustedHeight,
						width: adjustedWidth,
						title: whenWherePanel.geographyField.getTextValue() + " - " + classifier.description
					});
					
					AnalysisService.getSummaryChart(Ext.apply(v, {
					
						classifier: classifier.id,
						location: "HOME",
						datatype: dataType.id
					
					}), chart.wrapDwrCallback(function(chartData) {
							chart.load(chartData);
						})
					);
				}
			}, this);

		}, this);
		
		dwr.engine.endBatch();		
	}
	
	var resizing = false;
	
	function reposition(){
		if (adjustedWidth) {
			resizing = true;
			var ct = chartsPanel.body;
			Ext.get(chartsContainer).setWidth(ct.getSize().width - 100);
			resizing = false;
		}
	}
	
	var chartsPanel = new Ext.Panel({
		region: "center",
		layout: "absolute",
		autoScroll: true,
		html: '<div id="' + chartsId + '"><div id="' + chartsContainer + '" class="chart-container"></div></div>'
	});
		
	var chartResizingTask = new Ext.util.DelayedTask(function() {	
		updateCharts();
	}, this);
	
	chartsPanel.on("resize", function(){
		var size = chartsPanel.body.getSize().width;
		if (size != lastSize) {
			lastSize = size;
			if (!resizing) {
				chartResizingTask.delay(250);
			}
		}
	}, this);
	
	EpiCenter.panel.Summary.superclass.constructor.call(this, {
		title: "Summary",
		layout: "border",
		border: false,
		items: [{
			region: "west",
			minSize: 260,
			maxSize: 260,
			collapsible: true,
			collapseMode: "mini",
			split: true,
			width: 260,
			items: [ whenWherePanel ]
		}, chartsPanel ]
	});

};

Ext.extend(EpiCenter.panel.Summary, Ext.Panel);
