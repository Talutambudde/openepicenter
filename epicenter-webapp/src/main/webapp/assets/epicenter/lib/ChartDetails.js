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
EpiCenter.lib.ChartDetails = function(config) {

	var chartEl = Ext.id();
	
	var casesPanel = new EpiCenter.lib.Cases({}, config.parameters);
	
	if (config.disableCases) {
		casesPanel.disable();
	}
	
	var chartPanel = new Ext.Panel({
		layout: "fit",
		border: false,
		frame: true,
		title: "Chart",
		html: "<div id='" + chartEl + "' style='text-align: center; margin-left: auto; margin-right: auto;'></div>",
		bbar: [ {
			text: "Download Counts as CSV",
			iconCls: "icon-save",
			tooltip: "Download counts as a comma-separated-values file.",
			handler: downloadCounts,
			scope: this
		}]
	});
	
	var tabs = new Ext.TabPanel({
		border: false,
		activeItem: 0,
		enableTabScroll: true,
		items: [ chartPanel, casesPanel ]
	});
	
	EpiCenter.lib.ChartDetails.superclass.constructor.call(this, {
		title: "Chart Details",
		layout: "fit",
		height: 400,
		width: 800,
		closeable: true,
		draggable: true,
		border: false,
		items: [ tabs ]
	});

	// Stop mouse wheel events from going thru the window
	this.on("render", function() {
		this.body.on("mousewheel", function(e) {
			e.stopPropagation();
		}, this);
	}, this);
	
	chartPanel.on("afterlayout", function() {
		
		// Need setTimeout here with the toolbar for some reason
		setTimeout(function() {
			var elm = chartPanel.body.getSize(true);
			var chart = new EpiCenter.lib.Chart(chartEl, {
				height: elm.height - 25,
				width: elm.width - 28,
				title: config.title
			});
		
			if (config.originalChart !== undefined) {
				renderChart(config.originalChart.url, chart);
				
			} else if (config.eventId !== undefined) {
				
				EventService.getEventChart(config.eventId, "ACTUAL", chart.wrapDwrCallback(function(chartData) {
					renderChart(chartData, chart);
				}));
				
			} else {
				
				AnalysisService.getTimeSeriesChart(config.parameters, chart.wrapDwrCallback(function(chartData) {
					renderChart(chartData, chart);
				}));
			}
			
		}, 100);
		
	}, this, { single: true });
		
	
	function downloadCounts() {
		var downloadParams = {};
		Ext.apply(downloadParams, config.parameters);
		if (config.historyDays !== undefined) {
			downloadParams.start = downloadParams.start.add(Date.DAY, -config.historyDays);
		}
				
		Ext.ux.PostAction.post("download/counts", downloadParams);
	}
	
	function renderChart(chartData, chart) {
		
		chart.load(chartData);
	
		chartPanel.on("resize", function(panel, width, height) {
			setTimeout(function() {
				var cs = chartPanel.body.getSize(true);
				chart.resize(cs.width - 28, cs.height - 25);
			}, 100);
		}, this);
	}
};

Ext.extend(EpiCenter.lib.ChartDetails, Ext.Window);
