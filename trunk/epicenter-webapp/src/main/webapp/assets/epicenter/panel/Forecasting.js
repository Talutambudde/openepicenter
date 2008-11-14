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
EpiCenter.panel.Forecasting = function(config) {

	var chartsId = Ext.id();
	
	var chartsContainer = chartsId + "-container";

	var dataRepresentation = EpiCenter.common.representationDataStore.getAt(0).get("id");
	
	var optionsMenu = createOptionsMenu();
	
	var whenWherePanel = new EpiCenter.lib.WhenWherePanel({
		region: "north",
		border: true,
		height: 90,
		linkText: "Advanced Options"
	});
	
	var dataTypePanel = new EpiCenter.lib.DataTypePanel({
		region: "center"
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
	
	whenWherePanel.historyField.setValue("1 month");
	
	whenWherePanel.on("accept", renderCharts);
	
	whenWherePanel.on("optionsclick", function(cmp, e) {
		optionsMenu.show(cmp.body);
	}, this);
	
		
	function renderCharts(){
	
		if (whenWherePanel.getForm().isValid() && dataTypePanel.isValid()) {
		
			var params = getParameters();
			
			var title = params.classifier == "TOTAL" ? dataTypePanel.getSelectedDataType() : dataTypePanel.getSelectedCategories().join(", ");
			
			var chart = new EpiCenter.lib.Chart(chartsContainer, {
				height: 300,
				width: 500,
				closable: true,
			//	resizable: true,
				constrainTo: chartPanel.body,
				title: whenWherePanel.geographyField.getTextValue() + " - " + title + " - Seasonal Forecast"
			});

			chartPanel.body.scroll("down",  chartPanel.body.dom.scrollHeight, true);
			
			ForecastingService.getSeasonalTrendChart(params,
				chart.wrapDwrCallback(function(chartData) {
					chart.load(chartData);
					reposition();
				})
			);
		}
	}
	
	function createOptionsMenu() {
		
		var optionsMenu = new Ext.menu.Menu();
		
		var drMenu = new Ext.menu.Menu();
		
		EpiCenter.common.representationDataStore.each(function(item) {
			drMenu.add({
				text: item.get("value"),
				checked: item.get("id") == dataRepresentation,
				group: "forecastingRepresentation",
				key: item.get("id"),
				checkHandler: function(item) {
					dataRepresentation = item.key;
				},
				scope: this
			});
		}, this);
		
		optionsMenu.add({
			text: "Data Representation",
			menu: drMenu
		});
			
		return optionsMenu;
	}
	
	function getParameters() {
		return Ext.apply(dataTypePanel.getValues(), Ext.apply(whenWherePanel.getValues(), {
			representation: dataRepresentation
		}));
	}
	
	function reposition() {
		var chartsWidth = (Math.floor(Ext.get(chartsId).getWidth() / 530)) * 530;
		Ext.get(chartsContainer).setWidth(chartsWidth);
	}
	
	EpiCenter.panel.Forecasting.superclass.constructor.call(this, {
		title: "Forecasting",
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

Ext.extend(EpiCenter.panel.Forecasting, Ext.Panel);
