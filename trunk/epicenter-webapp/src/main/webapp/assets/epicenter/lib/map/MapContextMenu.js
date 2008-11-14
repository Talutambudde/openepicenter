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
EpiCenter.lib.map.MapContextMenu = Ext.extend(Ext.util.Observable, {
	
	init: function(mapPanel) {
		
		mapPanel.on("map_loaded", function() {
			
			var menu = new Ext.ux.ContextMenu();
				
			var nameItem = new Ext.menu.Item({
				text: '',
				canActivate: false
			});
	
			var populationItem = new Ext.menu.Item({
				text: '',
				canActivate: false
			});
			
			var quickChartItem = new Ext.menu.Item({
				text: 'Quick chart',
				handler: onQuickChart
			});
	
			var goToChartsItem = new Ext.menu.Item({
				text: 'Charts',
				handler: onGoToCharts
			});
	
			var goToAnomaliesItem = new Ext.menu.Item({
				text: 'Local anomalies',
				handler: onGoToAnomalies
			});
	
			var allItems = [
				nameItem,
				populationItem,
				quickChartItem,
				goToChartsItem,
				goToAnomaliesItem
			];
	
			menu.add(nameItem);
			menu.add(populationItem);
			menu.add('-');
			menu.add(quickChartItem);
			menu.add(goToChartsItem);
			menu.add(goToAnomaliesItem);
	
			var dt = new Ext.util.DelayedTask(function() {
				menu.hide();
			}, this);
	
			var geography;
	
			function prepareParameters() {
				
				var params = mapPanel.mapAdapter.getAcceptedParameters();
				
				console.log("Parameters: ", params);
				
				var analysisDays = (params.end - params.start) / (1000*60*60*24);
				if (analysisDays <= 7) {
					var monthAgo = new Date();
					monthAgo.setTime(params.end);
					monthAgo = monthAgo.add(Date.MONTH, -1);
	
					params.start = monthAgo.getTime();
				}
	
				Ext.apply(params, { 
					geography: geography.id,
					geographyName: geography.name,
					algorithmName: null
				});
				
				return params;
			}
	
			function onQuickChart() {
				var params = prepareParameters();
				var details = new EpiCenter.lib.ChartDetails({
					parameters: params,
					title: geography.name + ' - ' + params.categoryTitle,
					disableCases: geography.visibility == "AGGREGATE_ONLY"
				});
				details.show();
			}
	
			function onGoToCharts() {
				EpiCenter.core.Viewport.activateCharts(prepareParameters());
			}
	
			function onGoToAnomalies() {
				EpiCenter.core.Viewport.activateAnomalies(prepareParameters());
			}
	
			menu.on('beforeshow', function() {
								
				nameItem.setText("Loading...");
				menu.disableItems(allItems);
				GeographyService.getGeography(menu.opts.lat, menu.opts.lng, menu.opts.feature, {
					callback: function(g) {
						if (g) {
							nameItem.setText(g.name);
							populationItem.setText("Pop. " + g.population);
							
							geography = g;
							if (g.visibility == "NONE" || g.visibility == "LIMITED") {
								nameItem.enable();
								populationItem.enable();
							} else {
								this.enableItems(allItems);
							}
						} else {
							nameItem.setText("Not Found");
							nameItem.enable();
							populationItem.enable();
							dt.delay(menu.closingDelay);
						}
						// forcing ext to refit the menu into constrained region
						menu.adjustForConstraints();
					}.createDelegate(this),
					errorHandler: function(error, exception) {
						nameItem.setText("Error");
						nameItem.enable();
						populationItem.enable();
						EpiCenter.common.globalDwrErrorHandler(error, exception);
						dt.delay(menu.closingDelay);
					},
					textHtmlHandler: function() {
						nameItem.setText("Session timeout");
						nameItem.enable();
						populationItem.enable();
						dt.delay(menu.closingDelay);
						EpiCenter.common.globalDwrTextHtmlHandler();
					}
				});
			});
	
			menu.on('hide', function() {
				nameItem.setText("");
				populationItem.setText("");
			}, this);
	
			mapPanel.mapAdapter.on("context_menu", function(opts) {
				if (opts.feature) {
					menu.opts = opts;
					menu.display();
				}
			}, this);
			
		}, this, { single: true });	
	}
	
});


