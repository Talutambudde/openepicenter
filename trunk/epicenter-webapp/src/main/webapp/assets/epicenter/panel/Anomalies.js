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
EpiCenter.panel.Anomalies = function() {

	// Events Store
	var eventsDataStore = new Ext.data.GroupingStore({
		groupField: "geographyName",
		remoteGroup: true,
		remoteSort: true,
		sortInfo: { field: 'timestamp', direction: "DESC" },
		proxy: new Ext.data.DWRProxy(EventService.getEvents, [ false ], true),
		reader: new Ext.data.ObjectReader({
			root: "items",
			totalProperty: "totalItems"
		}, EpiCenter.common.eventRecordType)
	});
	
	this.activatedExternal = false;
	
	this.showAllEvents = false;
	
	// Anomaly list
	this.anomalyList = new EpiCenter.lib.LinkedItemView({
		store: eventsDataStore,
		headerText: "Matching Anomalies",
		region: "center",
		layout: "fit",
		groupName: "geographyName",
		border: false,
		tpl: new Ext.XTemplate(
			'<tpl for=".">',
			'  <div class="linked-view-item">',
			'    <tpl if="iconPath != null">',
			'      <div style="background-image: url(\'{iconPath}\')"> </div>',
			'    </tpl>',
			     EpiCenter.common.templateSources.anomalyTitle,
			'  </div>',
			'</tpl>'),
		pagerButtons: [ "->", {
			iconCls: "icon-go",
			tooltip: "Return to the overview map.",
			handler: function() {
				this.cards.getLayout().setActiveItem(0);
			}.createDelegate(this)
		} ]
	});

	// Controls
	this.whenWherePanel = new EpiCenter.lib.WhenWherePanel({
		region: "north",
		border: false,
		linkText: "Advanced Options",
		allowBlankGeography: true
	});
				
	this.mapPanel = new EpiCenter.lib.MapPanel({
		region: "center",
		border: false
	});
	
	this.detailsPanel = new EpiCenter.lib.AnomalyDetails();
	
	this.cards = new Ext.Panel({
		layout: "card",
		region: "center",
		activeItem: 0,
		items: [ this.mapPanel, this.detailsPanel ]
	});

	this.advancedOptionsMenu = new Ext.menu.Menu({
		id: "anomaliesAdvancedOptions",
		items: [ {
			text: "Show inactive anomalies",
			checked: this.showAllEvents,
			checkHandler: function(item, value) {
				this.showAllEvents = value;
				this.loadAnomalies();
			}.createDelegate(this)
		} ]
	});
	
	this.whenWherePanel.on("optionsclick", function(c) {
		this.advancedOptionsMenu.show(c.body);
	}, this);

	this.setParameters = function(params) {
		console.log("params: ", params);
		this.whenWherePanel.setValuesFromParameters(params);
		this.loadAnomalies();
		if (this.cards.getLayout().setActiveItem) {
			this.cards.getLayout().setActiveItem(0);
		}
	};
	
	EpiCenter.panel.Anomalies.superclass.constructor.call(this, {
		layout: "border",
		border: false,
		title: "Anomalies",
		items: [{
			region: "west",
			layout: "border",
			minSize: 260,
			maxSize: 260,
			collapsible: true,
			collapseMode: "mini",
			split: true,
			width: 260,
			items: [ this.whenWherePanel, this.anomalyList ]
		}, this.cards]
	});
	
	
};

Ext.extend(EpiCenter.panel.Anomalies, Ext.Panel, {
	
	loadAnomalies: function() {
		this.anomalyList.store.proxy.args = [this.showAllEvents, this.whenWherePanel.getStartDate(), this.whenWherePanel.getEndDate(), this.whenWherePanel.getGeography() ];
		this.anomalyList.refresh();
	},
	
	renderMarkers: function() {
		
		this.mapPanel.clearMarkers();
		
		var store = this.anomalyList.store;
		
		if (store.getTotalCount() > 0) {
			this.mapPanel.changeMapExtents(store.attributes.bbox);
		} else {
			this.mapPanel.changeMapExtents(null);
		}
		
		// Don't render duplicate markers
		var mrendered = {};
		
		store.each(function(record) {
			
			var geographyName = record.get("geographyName");
			if (!mrendered[geographyName]) {
				mrendered[geographyName] = {
					letter: this.anomalyList.getLetterForIndex(geographyName),
					title: geographyName,
					content: this.formatInfoWindowItem(record),
					locationPoint: record.get("locationPoint")
				};
			} else {
				mrendered[geographyName].content = mrendered[geographyName].content + this.formatInfoWindowItem(record);
			}
		}, this);
		
		Ext.each(Object.keys(mrendered), function(key) {
			var item = mrendered[key];
			this.mapPanel.addMarker(item.locationPoint, item);
		}, this);
		
	},
	
	formatInfoWindowItem: function(item) {
		return EpiCenter.common.templates.anomalyTitle.applyTemplate(item.data);
	},
	
	switchToOverview: function() {
		this.cards.getLayout().setActiveItem(0);
		this.anomalyList.refresh();
	},
	
	switchToDetails: function(id, date, geography, geographyId) {
		
		this.activatedExternal = true;
		
		Ext.ux.StoreSynchronizer.sync(this.anomalyList.store, function() {
			var index = this.anomalyList.store.find("id", id);
			
			var letter = index == -1 ? null : this.anomalyList.getLetterForIndex(this.anomalyList.store.getAt(index).get("geographyName"));
			this.detailsPanel.showDetails(id, letter);
			
			this.cards.getLayout().setActiveItem(1);
			this.anomalyList.view.select(index, false, true);
			
		}, this, (date && geography));
		
		if (date && geography && geographyId) {
			this.showAllEvents = true;
			this.whenWherePanel.setValues(date, "1 day", geography, geographyId);
			this.anomalyList.store.proxy.args = [this.showAllEvents, this.whenWherePanel.getStartDate(), this.whenWherePanel.getEndDate(), geographyId ];
			this.anomalyList.refresh();
		}	
		
		
	},
	
	initComponent: function() {
		
		EpiCenter.panel.Anomalies.superclass.initComponent.call(this);
		
		this.whenWherePanel.historyField.setValue(EpiCenter.common.oldestAnomaly);
	
		this.whenWherePanel.on("accept", function(panel) {
			this.loadAnomalies();
		}, this);
		
		/*
		 * Switches to the details view when an item is selected.
		 * Renders the map, charts, and the text description.
		 */
		this.anomalyList.view.on("click", function(view, index) {
			this.switchToDetails(view.store.getAt(index).get("id"));
		}, this);

		// Render markers on the overview map
		this.anomalyList.store.on("load", function(store) {
			this.renderMarkers();
		}, this);
		
		this.on("afterlayout", function() {
			if (!this.activatedExternal) {
				this.loadAnomalies();
			}
		}, this, { single: true } );
		
	}
});
