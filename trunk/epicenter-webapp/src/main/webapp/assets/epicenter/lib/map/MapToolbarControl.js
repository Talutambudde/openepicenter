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
EpiCenter.lib.map.MapToolbarControl = function(mapPanel) {

	var features = {
		state: new Ext.menu.CheckItem({
			value: "state",
			text: "State",
			group: "feature",
			checked: true,
			handler: featureSelectHandler
		}),
		county: new Ext.menu.CheckItem({
			value: "county",
			text: "County",
			group: "feature",
			checked: false,
			handler: featureSelectHandler
		}),
		zipcode: new Ext.menu.CheckItem({
			value: "zipcode",
			text: "Zipcode",
			group: "feature",
			checked: false,
			handler: featureSelectHandler
		})
	};
	
	var mapTypes = [];

	var mapTypeMenu = new Ext.menu.Menu({});

	var mapTypeButton = new Ext.Toolbar.SplitButton({
		menu: mapTypeMenu,
		tooltip: "Selects the base map type.",
		handler: function(){
			mapTypeButton.showMenu();
		}
	});

	var statusItems = [ "->", "-" ];
	
	var featureMenu;
	
	var featureButton;
	
	var featureItems = [];
	
	var labelFeatures = false;
			
	// Configure the feature selector if requested.
	if (mapPanel.featureControlEnabled) {
		
		featureMenu = new Ext.menu.Menu({});
		
		featureButton = new Ext.Toolbar.SplitButton({
			menu: featureMenu,
			tooltip: "Selects the region type to view data for.",
			handler: function(){
				featureButton.showMenu();
			}
		});
		
		Ext.each(Object.keys(features), function(feature) {
			var f = features[feature];
			if (f.checked) {
				featureButton.setText(f.text);
			}
			featureMenu.add(f);
		}, this)
		
		featureMenu.addSeparator();
		featureMenu.add({
			text: "Label Features",
			checked: labelFeatures,
			checkHandler: labelFeaturesHandler
		});
		
		mapPanel.on("map_loaded", function() {
			mapPanel.mapAdapter.changeFeature(getSelectedFeature());			
		}, this, { single: true });


		mapPanel.on("map_updated", function(mapPanel, parameters, feature) {
			setSelectedFeature(feature);
		}, this);
		
		var spatialScanButton = new Ext.Toolbar.Button({
			text: "Spatial Scan",
			iconCls: "icon-find",
			tooltip: "Execute a Bayesian Spatial Scan on the viewing window.",
			handler: spatialScan,
			disabled: true
		});
		
		mapPanel.on("map_updated", function() {
			spatialScanButton.enable();
		}, this, { single: true });
		
		statusItems.push(spatialScanButton, "-", featureButton, "-");
		
	}
	
	statusItems.push(mapTypeButton, "-");
	
	var statusBar = new Ext.StatusBar({
		defaultText: " ",
		items: statusItems
	});
	
	mapPanel.on("overlay_loading", function() {
		statusBar.showBusy("Loading..");
	}, this);
	
	mapPanel.on("overlay_loaded", function() {
		statusBar.clearStatus();
	}, this);
	
	// Build the list of map types for the selector
	mapPanel.on("map_loaded", function() {
		
		mapTypes = mapPanel.mapAdapter.getMapTypes();
		
		var currentMapType = mapPanel.mapAdapter.getCurrentMapType();
		
		var i = 0;
		Ext.each(mapTypes, function(mapType) {
			mapTypeMenu.add({
				value: i,
				text: mapType.getName(),
				checked: currentMapType.getName() === mapType.getName(),
				group: "mapType",
				checkHandler: mapTypeSelectHandler
			});
			i++;
		}, this);
		
		mapTypeButton.setText(currentMapType.getName());
		
	}, this);
	
	
	function mapTypeSelectHandler(item, checked) {
		if (mapPanel.mapAdapter) {
			mapPanel.mapAdapter.setMapType(mapTypes[item.value]);
		}
		mapTypeButton.setText(item.text);
	}

	function setSelectedFeature(feature) {
		console.log("selected feature: ", feature);
		
		var f = features[feature.toLowerCase()];
		
		f.setChecked(true, true);
		featureButton.setText(f.text);
	}
		
	function getSelectedFeature() {
		var selected;
		Ext.each(Object.keys(features), function(key) {
			console.log("item: ", key);
			if (features[key].checked) {
				selected = features[key].value;
				return false;
			}
		}, this);
		return selected;
	}
		
	function featureSelectHandler(item, checked) {
		if (mapPanel.mapAdapter) {
			mapPanel.mapAdapter.changeFeature(item.value);
		}
		featureButton.setText(item.text);
	}
	
	function labelFeaturesHandler(item, checked) {
		labelFeatures = checked;
		if (mapPanel.mapAdapter) {
			mapPanel.mapAdapter.labelFeatures(labelFeatures);
		}
	}
	
	function spatialScan() {
		var parameters = mapPanel.getParameters();
		console.log(mapPanel.mapAdapter.getBounds());
		
		statusBar.showBusy("Scanning..");
		var bounds = mapPanel.mapAdapter.getBounds();
		
		AnalysisService.spatialScan(parameters, bounds.minX, bounds.minY, bounds.maxX, bounds.maxY, 
			parameters.feature.toUpperCase(), mapPanel.getInnerWidth(), mapPanel.getInnerHeight(),
			function(result) {
			
				statusBar.clearStatus();
				statusBar.setStatus({
					text: "Probability of Outbreak: " + result.probabilityOfOutBreak.format("0.00")
				});
				
				mapPanel.mapAdapter.addImageOverlay(bounds.minX, bounds.minY, bounds.maxX, bounds.maxY, result.image);
			}
		);
	}
	
	return statusBar;

};
