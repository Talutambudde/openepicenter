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
EpiCenter.core.Viewport = function(){

	var logoutHandler = function(e){
		Ext.MessageBox.show({
			title: 'Logout',
			msg: 'Are you sure?',
			buttons: Ext.MessageBox.OKCANCEL,
			icon: Ext.MessageBox.QUESTION,
			animEl: 'logout',
			fn: function(btn){
				if (btn == 'ok') {
					EpiCenter.common.setCatchUnload(false);
					window.location = 'j_spring_security_logout';
				}
			}
		});
	};
	
	var viewport;
	
	var tabs;
	
	var panels = {};
	
	// Direct link helper for external event links
	var getDirectEventId = function(){
		return Ext.urlDecode(document.URL.split("?")[1]).event;
	};
	
	return Ext.apply(new Ext.util.Observable(), {

		panels: panels,
		
		setActiveTab: function(id) {
			tabs.setActiveTab(id);
		},
		
		
		init: function(){
		
			EpiCenter.core.Viewport.addEvents({ load: true });
			
			// Logout handler:
			Ext.get('logout').on('click', logoutHandler);
					
			EpiCenter.common.afterLoad(function(){
				
				var eventId = getDirectEventId();
				
				panels.summaryPanel = new EpiCenter.panel.Summary();
				panels.chartsPanel = new EpiCenter.panel.Charts();
				panels.mapsPanel = new EpiCenter.panel.Maps();
				panels.forecastingPanel = new EpiCenter.panel.Forecasting();
				panels.anomaliesPanel = new EpiCenter.panel.Anomalies();
				panels.investigationPanel = new EpiCenter.panel.Investigation();
				panels.optionsPanel = new EpiCenter.panel.Options();
			
				tabs = new Ext.TabPanel({
					region: "center",
					border: false,
					defaults: {
						hideMode: "offsets"
					},
					layoutOnTabChange: true,
					items: [ panels.summaryPanel, panels.anomaliesPanel, panels.investigationPanel, 
						     panels.chartsPanel, panels.mapsPanel, panels.forecastingPanel, panels.optionsPanel ]
				});
			
				viewport = new Ext.Viewport({
					layout: "border",
					items: [{
						region: "north",
						contentEl: "header",
						height: 45,
						border: false
					}, tabs]
				});
					
				// Display the Admin tab and unmask after load	
				Ext.each(EpiCenter.common.userinfo.roles, function(role){
					if (role.authority == "ROLE_ADMIN") {
						tabs.add(new EpiCenter.panel.Admin());
						return false;
					}
				}, this);
				
					
				// Go directly to an event if requested
				if (eventId) {
					EventService.getEvent(eventId, function(event) {
						EpiCenter.core.Viewport.activateEvent(event.id, event.timestamp, event.geography.name, event.geography.id);
					});
				} else {
					tabs.setActiveTab(0);
				}
				
				
				Ext.get('loading').remove();
				Ext.get('loading-mask').fadeOut({
					remove: true
				});
				
				
			}, this);
									
		},
		
		activateEvent: function(eventId, date, geographyName, geographyId) {
			panels.anomaliesPanel.switchToDetails(eventId, date, geographyName, geographyId);
			tabs.setActiveTab(1);
		},
		
		activateInvestigation: function(investigationId) {
			tabs.setActiveTab(2);
			panels.investigationPanel.viewInvestigation(investigationId);
		},
		
		activateCharts: function(params) {
			tabs.setActiveTab(3);
			panels.chartsPanel.setParameters(params);
		},
		
		activateAnomalies: function(params) {
			panels.anomaliesPanel.setParameters(params);
			tabs.setActiveTab(1);
		}
	});
}();

Ext.onReady(EpiCenter.core.Viewport.init, EpiCenter, true);
