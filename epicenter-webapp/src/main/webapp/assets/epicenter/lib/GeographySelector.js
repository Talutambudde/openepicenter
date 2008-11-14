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
/*
 * EpiCenter.lib.GeographySelector
 *
 * @author  Steve Kondik
 * @version 0.1
 *
 * @class EpiCenter.lib.GeographySelector
 * @extends Ext.form.ComboBox
 */
EpiCenter.lib.GeographySelector = Ext.extend(Ext.form.ComboBox, {
	
	fieldLabel: "Where",
	forceSelection: false,
	valueField: 'id',
	displayField: 'name',
	mode: 'remote',
	width: 140,
	selectOnFocus: true,
	editable: true,
	typeAhead: true,
	allowBlank: false,
	autoListWidth: true,
	triggerAction: "query",
	allQuery: "ALL",
	minChars: 2,
	emptyText: 'Select a region..',

	initComponent: function() {
			
		this.store = new Ext.data.Store({
			proxy: new Ext.data.DWRProxy(GeographyService.autocompleteGeography, [(this.organization ? this.organization : null)]),
			reader: new Ext.data.ObjectReader({}, EpiCenter.common.geographyRecordType)
		});
				
		EpiCenter.lib.GeographySelector.superclass.initComponent.call(this);
		
		var firstLoad = true;
		
		this.store.on("load", function(store) {
			if (this.store.getCount() > 0) {
				if (firstLoad) {
					firstLoad = false;
					this.setValue(this.store.getAt(0).get(this.valueField));
				} else {
					var first = this.store.getAt(0);
					if (first !== undefined) {
						if (first.get(this.displayField).toLowerCase() == this.getRawValue()) {
							this.setValue(first.get(this.valueField));
						}
					} 
				}
			} else {
				this.markInvalid("Invalid geography");
			}
		}, this);
		
		if (this.allowBlank === true) {
			
			this.on("beforequery", function(queryEvent) {
				
				if (this.getRawValue().length === 0) {
					console.log("force all query");
					queryEvent.forceAll = true;
					queryEvent.query = this.allQuery;
				}
				
			}, this);	
			
		} else {
			var defaultRegion = EpiCenter.common.userinfo.getDefaultRegion();
			this.store.loadData([defaultRegion]);
		}
	},
	
	load: function(value) {
		this.store.load({ params: { query: value } });
	},
	
	setOrganization: function(id) {
		this.organization = id;
		this.store.proxy.args = [ id ];
	},
	
	getTextValue: function() {
		var ret;
		var id = this.store.find("id", this.getValue());
		if (id > -1) {
			var value = this.store.getAt(id);
			if (value) {
				ret = value.get(this.displayField);
			}
		}
		return ret;
	},
	
	tpl: new Ext.XTemplate('<tpl for="."><div class="x-combo-list-item">{name}<tpl if="visibility == \'LIMITED\'"><span style="color:red;"> *</span></tpl></div></tpl>')
});

Ext.reg("geography", EpiCenter.lib.GeographySelector);

