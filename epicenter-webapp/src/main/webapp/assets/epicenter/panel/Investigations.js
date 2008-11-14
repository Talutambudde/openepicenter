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
EpiCenter.panel.Investigation = function() {

	this.activatedExternal = false;
	
	this.showAllInvestigations = false;
	
	this.investigationList = new EpiCenter.lib.LinkedItemView({
		store: new Ext.data.GroupingStore({
			groupField: "organizationName",
			sortInfo: { field: 'timestamp', direction: "DESC" },
			proxy: new Ext.data.DWRProxy(WorkflowService.getInvestigations, [ false ], true),
			reader: new Ext.data.ObjectReader({
				root: "items",
				totalProperty: "totalItems"
			}, EpiCenter.common.investigationRecordType)
		}),
		markerColor: "blue-light",
		headerText: "Matching Investigations",
		region: "center",
		layout: "fit",
		groupName: "organizationName",
		border: false,
		tpl: new Ext.XTemplate(
			'<tpl for=".">',
			'  <div class="linked-view-item">',
			'    <tpl if="iconPath != null">',
			'      <div style="background-image: url(\'{iconPath}\')"> </div>',
			'    </tpl>',
			'    <p>{timestamp:date("m/d/Y")} {category} {description} ({organizationName})</p>',
			'  </div>',
			'</tpl>'),
		pagerButtons: [ "->", {
				iconCls: "icon-add",
				tooltip: "Create new investigation.",
				handler: function() {
					this.createNewInvestigation();
				}.createDelegate(this)
			}, {
				iconCls: "icon-go",
				tooltip: "Return to the overview map.",
				handler: function() {
					this.cards.getLayout().setActiveItem(0);
				}.createDelegate(this)
			}
		]
	});
	
	this.whenWherePanel = new EpiCenter.lib.WhenWherePanel({
		region: "north",
		border: false,
		allowBlankGeography: true,
		listeners: {
			accept: {
				scope: this,
				fn: function(panel) {
					this.loadInvestigations();
				}
			},
			scope: this
		},
		linkText: "Advanced Options"
	});
	
	this.mapPanel = new EpiCenter.lib.MapPanel({
		region: "center",
		border: false
	});
		
	this.detailsPanel = new EpiCenter.lib.InvestigationDetails();
		
	this.cards = new Ext.Panel({
		layout: "card",
		region: "center",
		activeItem: 0,
		items: [ this.mapPanel, this.detailsPanel ]
	});
	
	this.advancedOptionsMenu = new Ext.menu.Menu({
		id: "investigationsAdvancedOptions",
		items: [ {
			text: "Show inactive investigations",
			checked: this.showAllInvestigations,
			checkHandler: function(item, value) {
				this.showAllInvestigations = value;
				this.loadInvestigations();
			}.createDelegate(this)
		} ]
	});
	
	this.whenWherePanel.on("optionsclick", function(c) {
		this.advancedOptionsMenu.show(c.body);
	}, this);
	
	EpiCenter.panel.Investigation.superclass.constructor.call(this, {
		layout: "border",
		border: false,
		title: "Investigations",
		items: [{
			region: "west",
			layout: "border",
			minSize: 260,
			maxSize: 260,
			collapsible: true,
			collapseMode: "mini",
			split: true,
			width: 260,
			items: [ this.whenWherePanel, this.investigationList ]
		}, this.cards]
	});

};

Ext.extend(EpiCenter.panel.Investigation, Ext.Panel, function() {

	function getInvestigationLetter(investigationList, investigationId) {
		var index = investigationList.store.find("id", investigationId);
		var investigation = investigationList.store.getAt(index);
		
		var letter = index == -1 ? null : investigationList.getLetterForIndex(investigation.get("organizationName"));
		return letter;
	}

	var investigationInfoPopupTemplate = new Ext.Template(
		'<p>',
		'  {description} - {investigator} ({org})',
		'</p>');
	
	/*
	var createInvestigationLocality = new EpiCenter.lib.GeographySelector({
		fieldLabel: "Locality",
		name: "locality",
		hiddenName: "locality",
		allowBlank: false,
		width: 250
	});
	
	var createInvestigationForm = new Ext.form.FormPanel({
		labelWidth: 100,
		labelAlign: "right",
		buttonAlign: "right",
		autoHeight: true,
		border: false,
		bodyStyle: "padding: 10px 5px 10px 5px;",
		items: [{
			fieldLabel: "Investigation Name",
			name: "name",
			hiddenName: "name",
			xtype: "textfield",
			allowBlank: false,
			width: 250,
		}, createInvestigationLocality ]
	});
	
	var createInvestigationWindow = new Ext.Window({
		layout: "fit",
		closeAction: "hide",
		autoHeight: true,
		width: 400,
		modal: true,
		title: "Create New Investigation",
		items: [ createInvestigationForm ],
		buttons: [{
			text: "Create",
			handler: function() {
				
				if (createInvestigationForm.getForm().isValid()) {
					createInvestigationWindow.body.mask("Creating Investigation..", "x-mask-loading");
					WorkflowService.createInvestigation(createInvestigationForm.getForm().findField("name").getValue(), [].concat(createInvestigationLocality.getValue()), createInvestigationWindow.anomalies, function(result){
						createInvestigationWindow.body.unmask();
						createInvestigationWindow.hide();
						this.loadInvestigations();
						EpiCenter.core.Viewport.activateInvestigation(result);
					}.createDelegate(this));
				}
			},
			scope: this
		}, {
			text: "Cancel",
			handler: function() {
				createInvestigationWindow.hide();
			}
		}]
	});
	
	*/
	
	function formatInfoWindowItem(record) {
		return investigationInfoPopupTemplate.apply({
			description: record.get('description'),
			investigator: record.get('assignedTo') ? record.get('assignedTo').value : record.get('createdBy').value,
			org: record.get('organizationName')
		});
	}
	
	return {

		renderMarkers: function() {
			
			var store = this.investigationList.store;
			
			this.mapPanel.clearMarkers();
			
			if (store.getTotalCount() > 0) {
				this.mapPanel.changeMapExtents(store.attributes.bbox);
			} else {
				this.mapPanel.changeMapExtents(null);
			}
			
			// Don't render duplicate markers
			var mrendered = {};
			
			store.each(function(record) {
				
				var organizationName = record.get("organizationName");
				if (!mrendered[organizationName]) {
					mrendered[organizationName] = {
						letter: this.investigationList.getLetterForIndex(organizationName),
						title: record.get("organizationName"),
						content: formatInfoWindowItem(record),
						organizationPoint: record.get("organizationPoint"),
						colors: ['blue-light', 'blue-dark']
					};
				} else {
					mrendered[organizationName].content = mrendered[organizationName].content + formatInfoWindowItem(record);
				}
			}, this);
			
			Ext.each(Object.keys(mrendered), function(key) {
				var item = mrendered[key];
				this.mapPanel.addMarker(item.organizationPoint, item);
			}, this);
		},
		
		loadInvestigations: function() {
			// todo we should not rely on client's machine clock at all, adding a minute here is a
			// silly short time fix
			var adjustedEndTime = new Date();
			adjustedEndTime.setTime(this.whenWherePanel.getEndDate().getTime() + 60000);
			this.investigationList.store.proxy.args = [this.showAllInvestigations, this.whenWherePanel.getStartDate(), adjustedEndTime, this.whenWherePanel.getGeography() ];
			this.investigationList.refresh();
		},
		
		viewInvestigation: function(investigationId) {
			
			this.activatedExternal = true;
			
			Ext.ux.StoreSynchronizer.sync(this.investigationList.store, function() {
				var index = this.investigationList.store.find("id", investigationId);
				
				var letter = getInvestigationLetter(this.investigationList, investigationId);
				this.detailsPanel.showDetails(investigationId, letter);
				this.cards.getLayout().setActiveItem(1);
				this.investigationList.view.select(index, false, true);
				
			}, this);
			
		},
		
		refreshDetails: function(investigation) {
			this.detailsPanel.refreshDetails(investigation);
		},
		
		createNewInvestigation: function(anomaly, geography, animateTarget) {
			var anomalies = anomaly ? [ anomaly ] : [];

			this.activatedExternal = true;
			
			/*
			var f = createInvestigationForm.getForm();
			f.reset();
			
			if (geography) {
				createInvestigationLocality.store.loadData([geography]);
				createInvestigationLocality.setValue(geography.id);
			}
			
			createInvestigationWindow.anomalies = anomalies;
			
			createInvestigationWindow.show(animateTarget);
			*/
			
			Ext.Msg.prompt("Create New Investigation", "Please enter a name for the new investigation:", function(btn, text) {
				if (btn == "ok") {
					if (text) {
						WorkflowService.createInvestigation(text, anomalies, null, function(result) {
							this.loadInvestigations();
							EpiCenter.core.Viewport.activateInvestigation(result);
						}.createDelegate(this));
					}
				}
			}, this);
			
		},
		
		initComponent: function() {
			
			EpiCenter.panel.Investigation.superclass.initComponent.call(this);

			/*
			* Event handlers
			*/
			EpiCenter.common.afterLoad(function() {
				this.whenWherePanel.historyField.setValue(EpiCenter.common.oldestInvestigation);
			}, this);
			
			
			// Render markers on the overview map
			this.investigationList.store.on("load", function(store) {
				this.renderMarkers();
			}, this);
			
			/*
			* Switches to the details view when an item is selected.
			* Renders the map, charts, and the text description.
			*/
			this.investigationList.view.on("click", function(view, index) {
				this.cards.getLayout().setActiveItem(1);
				var investigationId = view.store.getAt(index).get("id");
				var letter = getInvestigationLetter(this.investigationList, investigationId);
				this.detailsPanel.showDetails(investigationId, letter);
			}, this);
		
			this.on("afterlayout", function() {
				if (!this.activatedExternal) {
					this.loadInvestigations();
				}
			}, this, { single: true });
			
		}
	};
}());	

