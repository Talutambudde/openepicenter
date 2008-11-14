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
EpiCenter.lib.ResolutionDetails = function(config) {
	var window = this;
			
	var toStateName = config.toState.name;
	var submitCallback = config.submitCallback;
	var anomalies = config.investigation.anomalies;

	// Record type for Geography
	var dispositionRecordType = Ext.data.Record.create([{
		name: 'id'
	}, {
		name: 'name'
	}, {
		name: 'type'
	}]);


	var dispositionsDataStore = new Ext.data.Store({
		reader: new Ext.data.ObjectReader({}, dispositionRecordType)
	});

	dispositionsDataStore.loadData(EpiCenter.common.eventDispositions.select(function(ed) {
		return ed.type == 'TERMINAL';
	}));
	
	var form = new Ext.form.FormPanel({
		border: true,
		height: 300,
		autoScroll: true
	});

	var header = {
		autoHeight: true,
		html: 
			'<p>' +
			'  You have selected to resolve this investigation as <em>'+toStateName.toLowerCase()+'</em>. Click OK below to assign this final state to all associated anomalies. If you wish to assign a different final state to one or more of the anomalies please select a new state in the corresponding drop down box before selecting OK.' +
			'</p>',
		border: false
	};

	var firstField;

	var indeterminateIndex = dispositionsDataStore.find('name', 'Indeterminate');
	var indeterminateId = dispositionsDataStore.getAt(indeterminateIndex).get('id');

	Ext.each(anomalies, function(anomaly) {
		var item = new Ext.form.ComboBox({
			hiddenName: anomaly.id,
			value: indeterminateId,
			fieldLabel: anomaly.timestamp.format("m/d/Y") + ': ' + anomaly.description,
			mode: 'local',
			store: dispositionsDataStore,
			valueField: 'id',
			displayField: 'name',
			labelStyle: 'width: 250px; padding: 0 30px 0 0; font-weight: normal',
			style: "width: 190px; margin-bottom: 20px;",
			editable: false,
			forceSelection: true,
			autoListWidth: true,
			triggerAction: 'all',
			allowBlank: false
		});
		form.add(item);
		if (!firstField) {
			firstField = item;
		}
	}, this);

	if (!firstField) {
		form.add({
			autoHeight: true,
			html: 
				'<p>' +
				'  No anomalies investigated.' +
				'</p>',
			border: false
		});
	}

	var okButton = new Ext.Button({
		text: "OK",
		handler: function() {
			window.hide();
			submitCallback(form.getForm().getValues(false));
		}
	});

	EpiCenter.lib.ResolutionDetails.superclass.constructor.call(this, {
		title: "Resolve Investigation",
		cls: "investigation-resolution-main",
		layout: "fit",
		defaults: {
			bodyStyle: "padding: 10px 10px 0px 15px;"
		},
		modal: true,
		border: false,
		width: 550,
		autoHeight: true,
		resizable: false,
		items: [
			header,
			form
		],
		defaultButton: firstField || okButton,
		buttons: [
			okButton, {
				text: "Cancel",
				handler: function() {
					window.hide();
				}
		} ]
	});
};

Ext.extend(EpiCenter.lib.ResolutionDetails, Ext.Window);

