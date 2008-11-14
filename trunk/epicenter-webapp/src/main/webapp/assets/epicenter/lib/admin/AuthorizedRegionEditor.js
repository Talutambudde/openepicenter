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
EpiCenter.lib.admin.AuthorizedRegionEditor = function() {
	
	var AuthorizedRegion = Ext.data.Record.create([
		{ name: "grantedByName" },
		{ name: "grantedById" },
		{ name: "type", type: "string" },
		{ name: "geographyName" },
		{ name: "geographyId" }
	]);
	
	this.containerStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(AdminService.getOrganizationList),
		reader: new Ext.data.ObjectReader({}, EpiCenter.common.simpleRecordType)
	});
	
	this.typeStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(AdminService.getAuthorizedRegionTypes),
		reader: new Ext.data.ObjectReader({}, EpiCenter.common.simpleRecordType)
	});
	
	this.geographySelector = new EpiCenter.lib.GeographySelector({
		lazyRender: true,
		emptyText: "Select a geography..",
		listClass: 'x-combo-list-small',
		listeners: {
			select: function(field, record) {
				field.currentRecord.set("geographyName", record.get("name"));
				field.currentRecord.set("geographyId", record.get("id"));
			} 
		}
	});
	
	var typeCombo = new Ext.form.ComboBox({
		lazyRender: true,
		forceSelection: true,
		valueField: 'value',
		displayField: 'value',
		mode: 'local',
		editable: false,
		allowBlank: false,
		autoListWidth: true,
		triggerAction: "all",
		emptyText: "Select an type..",
		listClass: 'x-combo-list-small',
		store: this.typeStore
	});
	
	var grantedByCombo = new Ext.form.ComboBox({
		lazyRender: true,
		forceSelection: true,
		valueField: 'id',
		displayField: 'value',
		mode: 'local',
		editable: false,
		typeAhead: true,
		autoListWidth: true,
		allowBlank: false,
		triggerAction: "all",
		emptyText: "Select an organization..",
		listClass: 'x-combo-list-small',
		store: this.containerStore,
		listeners: {
			select: function(field, record) {
				this.geographySelector.setOrganization(record.get("id"));
				field.currentRecord.set("geographyName", null);
				field.currentRecord.set("geographyId", null);
				field.currentRecord.set("grantedByName", record.get("value"));
				field.currentRecord.set("grantedById", record.get("id"));
				
			}.createDelegate(this),
			scope: this
		}
	});
	
	var actions = new Ext.ux.grid.RowActions({
		header: "Actions",
		actions: [ {
			iconCls: "icon-trash",
			tooltip: "Delete"
		}],
		callbacks: {
			"icon-trash": function(grid, record, action, row, col) {
				grid.store.remove(record);
			}
		}
	});
	
	var cm = new Ext.grid.ColumnModel([{
			header: "Granted By",
			dataIndex: "grantedById",
			width: 175,
			editor: new Ext.grid.GridEditor(grantedByCombo, {
				listeners: {
					beforestartedit: function(editor){
						editor.field.currentRecord = editor.record;
					}
				}
			}),
			renderer: function(v, p, r) {
				return r.get("grantedByName");
			}
		}, {
			header: "Type",
			dataIndex: "type",
			width: 50,
			editor: typeCombo
		}, {
			header: "Geography",
			dataIndex: "geographyId",
			width: 175,
			editor: new Ext.grid.GridEditor(this.geographySelector, {
				listeners: {
					beforestartedit: function(editor) {
						editor.field.currentRecord = editor.record;
						if (editor.record.get("geographyId")) {
							editor.field.store.loadData([{
								id: editor.record.get("geographyId"),
								name: editor.record.get("geographyName")
							}]);
						}
					}
				}
			}),
			renderer: function(v, p, r) {
				return r.get("geographyName");
			}
		}, actions
	]);
	
	this.store = new Ext.data.Store({
		reader: new Ext.data.ObjectReader({}, AuthorizedRegion),
		proxy: new Ext.data.MemoryProxy([])
	});
	
	EpiCenter.lib.admin.AuthorizedRegionEditor.superclass.constructor.call(this, {
		store: this.store,
        cm: cm,
        width:300,
        height:100,
		clicksToEdit:1,
		plugins: [ actions ],
		stripeRows: true,
		tbar: [{
			text: "Add Authorized Region",
			iconCls: "icon-add",
			handler: function() {
				var r = new AuthorizedRegion({
					type: "FULL",
					geographyId: null,
					geographyName: null,
					grantedById: null,
					grantedByName: null
				});
				this.stopEditing();
				this.store.insert(0, r);
				this.startEditing(0, 0);
			},
			scope: this
		}]
	});
};

Ext.extend(EpiCenter.lib.admin.AuthorizedRegionEditor, Ext.grid.EditorGridPanel, {
	
	load: function(organization) {
		
		if (organization.authorizedRegions) {	
			var recs = organization.authorizedRegions.collect(function(r) {
				return {
					grantedById: r.grantedBy.id,
					grantedByName: r.grantedBy.name,
					geographyId: r.geography.id,
					geographyName: r.geography.displayName,
					type: r.type
				};
			});
			
			this.store.loadData(recs);
		}
		
	},
	
	reset: function() {
		
		this.store.removeAll();
	}
});

