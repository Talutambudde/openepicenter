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
EpiCenter.lib.admin.SponsorshipEditor = Ext.extend(Ext.form.FormPanel, {

	labelWidth: 150,
	autoScroll: true,
	autoHeight: true,
	autoWidth: true,
	
	initComponent: function() {

		this.fromStore = new Ext.data.Store({
			reader: new Ext.data.ObjectReader({}, EpiCenter.common.simpleRecordType),
			proxy: new Ext.data.MemoryProxy([])
		});
			
		this.toStore = new Ext.data.Store({
			proxy: new Ext.data.DWRProxy(AdminService.getSponsors),
			reader: new Ext.data.ObjectReader({}, EpiCenter.common.simpleRecordType)
		});
		
		// Remove records from the from side that are in the to side
		this.toStore.on("load", function() {
			this.toStore.each(function(record) {
				var from = this.fromStore.find("id", record.get("id"));
				if (from > -1) {
					this.fromStore.remove(this.fromStore.getAt(from));
				}
			}, this);
		}, this);
		
		this.itemSelector = new Ext.ux.ItemSelector({
			name: "sponsors",
			hiddenName: "sponsors",
			fieldLabel: "Sponsoring Organizations",
			imagePath: "assets/epicenter/images",
			displayField: "value",
			valueField: "id",
			toLegend: "Selected",
			fromLegend: "Available",
			msWidth: 280,
			msHeight: 250,
			width: 600,
			dataFields: [ "id", "value" ],
			fromSortField: "value",
			toSortField: "value",
			fromStore: this.fromStore,
			toStore: this.toStore
		});
		
		this.items = [ this.itemSelector ];
		
		EpiCenter.lib.admin.SponsorshipEditor.superclass.initComponent.call(this);
		
	},
	
	load: function(organizations, organization) {
		
		if (organizations !== undefined) {
			this.fromStore.loadData(organizations);
		}
		
		if (organization === undefined) {
			this.toStore.removeAll();
		} else {
			this.toStore.proxy.args = [organization.id];
			this.toStore.load();
		}
	},
	
	getSelectedValues: function() {
		return this.itemSelector.getValue();
	}
});
