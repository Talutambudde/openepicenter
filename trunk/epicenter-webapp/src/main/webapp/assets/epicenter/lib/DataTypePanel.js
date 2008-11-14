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
EpiCenter.lib.DataTypePanel = Ext.extend(Ext.Panel, {

	layout: "accordion",
	
	defaults: {
		bodyStyle: "padding: 10px 10px 0px 15px;"
	},
	
	layoutConfig: {
		animate: true,
		fill: true
	},
	
	activeItem: 0,
	
	onRender: function(ct, position) {
		
		EpiCenter.lib.DataTypePanel.superclass.onRender.call(this, ct, position);
				
		Ext.each(EpiCenter.common.dataTypes, function(datatype, index) {
			
			var item = new EpiCenter.lib.DataTypePanelItem({
				title: datatype.name
			}, datatype);
			
			this.add(item);
			
			if (item.classifierDataStore.getCount() < 2) {
				item.disable();
			}
		}, this);
		
		
		//this.doLayout();
		//this.getLayout().setActiveItem(1);
		
	},
	
	getValues: function() {
		var item = this.getLayout().getActiveItem();
		if (item.getForm().isValid()) {
			return Ext.apply(item.getForm().getValues(), { datatype: item.datatype.id });
		}
	},
	
	getField: function(name) {
		return this.getLayout().getActiveItem().getForm().findField(name);
	},
	
	isValid: function() {
		return this.getLayout().getActiveItem().getForm().isValid();
	},
	
	getSelectedCategories: function() {
		return [].concat(this.getField("category").getValue("value").split(",")).sort();
	},
	
	getSelectedCategoriesAsObject: function() {
		var ret = {};
		var field = this.getField("category");
		Ext.each(field.getValue().split(","), function(id) {
			var value = field.store.getAt(field.store.find("id", id)).get("value");
			ret[id] = value;	
		}, this);
		return ret;
	},
		
	selectMultiselect: function(name, value, selectFirstIfEmpty) {
		var multiselect = this.getField(name);
		multiselect.setValue(value ? '' + value : value);
		if (selectFirstIfEmpty && (!value || value.length === 0)) {
			multiselect.view.select(0);
		}
	},

	selectCombobox: function(name, value, selectFirstIfEmpty, fireEvent) {
		var combo = this.getField(name);
		combo.setValue(value ? '' + value : value);
		if (selectFirstIfEmpty && (!value || value.length === 0)) {
			combo.setValue(combo.store.getAt(0).get('id'));
		}
		if (fireEvent) {
			combo.fireEvent('select', combo);
		}
	},
	
	setClassifier: function(classifier) {
		this.selectCombobox('classifier', classifier, true, true);
	},

	setSelectedCategories: function(categories) {
		this.selectMultiselect('category', categories, false);
	},

	setSelectedAgeGroups: function(ageGroups) {
		this.selectMultiselect('ageGroup', ageGroups, true);
	},

	setSelectedGenders: function(genders) {
		this.selectMultiselect('gender', genders, true);
	},

	setLocation: function(location) {
		this.selectCombobox('location', location, true);
	},
	
	getSelectedDataType: function() {
		return this.getLayout().getActiveItem().datatype.name;
	}

	
});		

EpiCenter.lib.DataTypePanelItem = function(config, datatype) {
	
	this.datatype = datatype;
		
	// Classifier dataStore
	this.classifierDataStore = new Ext.data.Store({
		reader: new Ext.data.ObjectReader({}, EpiCenter.common.simpleRecordType)
	});
	
	// Categories dataStore
	var categoriesDataStore = new Ext.data.Store({
		reader: new Ext.data.ObjectReader({}, EpiCenter.common.simpleRecordType)
	});
		
	// Classifier ComboBox
	var classifierCombo = new Ext.form.ComboBox({
		name: "classifier",
		hiddenName: "classifier",
		store: this.classifierDataStore,
		autoListWidth: true,
		forceSelection: true,
		fieldLabel: "Classifier",
		valueField: 'id',
		displayField: 'value',
		mode: 'local',
		triggerAction: 'all',
		width: 198,
		selectOnFocus: true,
		allowBlank: false,
		editable: false,
		emptyText: 'Select a classifier..',
		listeners: {
			select: {
				fn: function(combo, data, index){
					if (combo.getValue() == "TOTAL") {
						categoriesSelect.disable();
						categoriesDataStore.removeAll();
					} else {
						categoriesSelect.enable();
						categoriesDataStore.loadData(datatype.categories[combo.getValue()]);
					}
				}
			}
		}
	});
	
	// Categories MultiSelect
	var categoriesSelect = new Ext.ux.Multiselect({
		name: "category",
		store: categoriesDataStore,
		hideLabel: true,
		valueField: 'id',
		displayField: 'value',
		mode: 'local',
		width: 215,
		height: 85,
		selectOnFocus: true,
		allowBlank: false,
		emptyText: 'Select a category..'
	});
	
	this.classifierDataStore.loadData(datatype.classifiers);
	
	
	// AgeGroup attribute MultiSelect
	var ageGroupSelect = new Ext.ux.Multiselect({
		name: "ageGroup",
		store: EpiCenter.common.ageGroupDataStore,
		fieldLabel: "Age Group",
		valueField: 'id',
		displayField: 'value',
		mode: 'local',
		width: 215,
		height: 85,
		selectOnFocus: true,
		allowBlank: false,
		emptyText: 'Select an age group..'
	});
	
	ageGroupSelect.on("change", selectClickHandler);
	
	// Gender attribute MultiSelect
	var genderSelect = new Ext.ux.Multiselect({
		name: "gender",
		store: EpiCenter.common.genderDataStore,
		valueField: 'id',
		displayField: 'value',
		fieldLabel: "Gender",
		mode: 'local',
		width: 215,
		height: 85,
		selectOnFocus: true,
		allowBlank: false,
		emptyText: 'Select a gender..'
	});
	
	genderSelect.on("change", selectClickHandler);
	
	var locationCombo = new Ext.form.ComboBox({
		name: "location",
		hiddenName: "location",
		store: EpiCenter.common.locationsDataStore,
		autoListWidth: true,
		forceSelection: true,
		valueField: 'id',
		displayField: 'value',
		hideLabel: true,
		mode: 'local',
		triggerAction: 'all',
		width: 198,
		selectOnFocus: true,
		allowBlank: false,
		editable: false
	});
	
	classifierCombo.on("render", function() {
		if (this.classifierDataStore.getCount() > 1) {
			classifierCombo.setValue(this.classifierDataStore.getAt(1).get("id"));
			categoriesDataStore.loadData(datatype.categories[classifierCombo.getValue()]);
		}
	}, this);
	
	locationCombo.on("render", function() {
		locationCombo.setValue(EpiCenter.common.locationsDataStore.getAt(0).get("id"));
	}, this);
		
	ageGroupSelect.on("render", function() {
		ageGroupSelect.setValue(EpiCenter.common.ageGroupDataStore.getAt(0).get("id"));
	});
	
	genderSelect.on("render", function() {
		genderSelect.setValue(EpiCenter.common.genderDataStore.getAt(0).get("id"));
	});
	
	/*
	 * Handles MultiSelects with an "ALL" option.
	 */
	function selectClickHandler(select, value, previousValue) {
		
		var raw = select.getRawValue();
		var previous = previousValue.length ? previousValue.split(select.delimiter) : [];
		
		if (raw.indexOf("ALL") > -1 && previous.indexOf("ALL") == -1) {
			select.setValue("ALL");
		} else if (previous.indexOf("ALL") > -1 && raw.without("ALL").length) {
			select.setValue(raw.without("ALL"));
		}
	}
	
	Ext.apply(this, config);
	
	EpiCenter.lib.DataTypePanelItem.superclass.constructor.call(this, {
		layout: "form",
		border: false,
		fill: true,
		autoScroll: true,
		labelAlign: "top",
		items: [ classifierCombo, categoriesSelect, ageGroupSelect, genderSelect, locationCombo ]
	});
				
};

Ext.extend(EpiCenter.lib.DataTypePanelItem, Ext.form.FormPanel);
