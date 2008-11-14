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
EpiCenter.panel.Maps = function() {

	var advancedOptionsId = Ext.id();
	
	var algorithm;
	
	var algorithmMenu;
	
	var analyzerDetails;
	
	var dataConditioning = EpiCenter.common.conditioningDataStore.getAt(0).get("id");
	
	var dataRepresentation = EpiCenter.common.representationDataStore.getAt(0).get("id");
	
	var defaultLink = getDefaultLinkText();
	
	var whenWherePanel = new EpiCenter.lib.WhenWherePanel({
		region: "north",
		border: true,
		height: 90,
		linkText: defaultLink
	});
	
	var dataTypePanel = new EpiCenter.lib.DataTypePanel({
		region: "center"
	});
	
	var mapPanel = new EpiCenter.lib.MapPanel({
		featureControlEnabled: true,
		id: "map-panel1",
		region: "center"
	});
		
	whenWherePanel.historyField.setValue("1 day");
	whenWherePanel.historyField.disable();
	whenWherePanel.geographyField.disable();
	whenWherePanel.on("accept", updateMap, this);
	whenWherePanel.on("render", function() {
		algorithmMenu = createAlgorithmMenu();
	}, this);
	
	whenWherePanel.on("optionsclick", function(cmp, e) {
		if (e.getTarget().id == advancedOptionsId) {
			if (analyzerDetails) {
				analyzerDetails.close();
			}
			analyzerDetails = new EpiCenter.lib.AnalyzerDetails({ algorithm: algorithm });
			analyzerDetails.show();
		}
		else {
			algorithmMenu.show(cmp.body);
		}
	});
	

	function createAlgorithmMenu() {
		
		var algorithmMenu = new Ext.menu.Menu();
	
		EpiCenter.common.posteriorAlgorithmDataStore.each(function(item)  {
			algorithmMenu.add({
				text: item.get("value"),
				checked: item.get("id") == algorithm,
				group: "mapAlgorithm",
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
				group: "mapConditioning",
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
				group: "mapRepresentation",
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

	function getDefaultLinkText() {
		var a = EpiCenter.common.posteriorAlgorithmDataStore.getAt(0);
		algorithm = a.get("id");	
		return "<div id='" + advancedOptionsId + "' class='icon-gear' style='background-repeat: no-repeat; padding-left: 18px;'><a>" 
				+ a.get("value") + "</a></div>";
	}
	
	function waitForMapOverlayData(callback) {
		var waitInterval = 100;
		var acceptedParameters = getParameters();
		if (acceptedParameters && acceptedParameters.algorithmName && acceptedParameters.category) {
			callback();
		} else {
			setTimeout(function() { waitForMapOverlayData(callback); }, waitInterval);
		}
	}
	
	function updateMap() {
		
		if (whenWherePanel.getForm().isValid() && dataTypePanel.isValid()) {
			mapPanel.updateMap(getParameters());
		}
	}

	function getParameters() {
		var today = new Date();
		var selected = whenWherePanel.getEndDate();
		
		var isToday = (today.getMonth() == selected.getMonth() && today.getDate() == selected.getDate() && today.getYear() == selected.getYear());
		//if (isToday) {
			
			// Go back 1 hour if it's today
			//selected = selected.add(Date.HOUR, -1);
		//}

		var rtn = Ext.apply({}, dataTypePanel.getValues());
		
		return Ext.apply(rtn, {
			start: selected.getTime(),
			end: selected.getTime(),
			algorithmName: algorithm,
			algorithmProperties: (analyzerDetails ? analyzerDetails.getValues() : null),
			conditioning: dataConditioning,
			representation: dataRepresentation,
			fixDates: !isToday,
			categoryTitle: rtn.classifier == "TOTAL" ? dataTypePanel.getSelectedDataType() : dataTypePanel.getSelectedCategories().join(", ")
		});
	}
	
	this.constructor.superclass.constructor.call(this, {
		title: "Maps",
		layout: "border",
		border: false,
		items: [ {
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
		}, mapPanel ]
	});
};

Ext.extend(EpiCenter.panel.Maps, Ext.Panel);

