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
EpiCenter.lib.PatientHistory = function(config, facilityId, patientId) {
	
	Ext.apply(this, config);
	
	// Record type for Cases Detail
	var casesDetailRecordType = Ext.data.Record.create([{
		name: 'interactionDate'
	}, {
		name: 'age'
	}, {
		name: 'gender'
	}, {
		name: 'zipcode'
	}, {
		name: 'facilityName'
	}, {
		name: 'visitNumber'
	}, {
		name: 'reason'
	}, {
		name: 'classification'
	}, {
		name: 'patientId'
	}, {
		name: 'facilityId'
	}, {
		name: 'type'
	}, {
		name: 'icd9Codes'
	}, {
		name: 'patientClass'
	}, {
		name: 'dob'
	}]);
	
	// Cases grid ColumnModel
	var cm = new Ext.grid.ColumnModel([{
		header: "Date",
		dataIndex: "interactionDate",
		renderer: Ext.util.Format.dateRenderer('m/d/Y H:i'),
		width: 100
	}, {
		header: "Type",
		dataIndex: "type",
		width: 30
	}, {
		header: "Visit Number",
		dataIndex: "visitNumber",
		width: 85
	}, {
		header: "Age",
		dataIndex: "age",
		width: 30
	}, {
		header: "Gender",
		dataIndex: "gender",
		width: 50
	}, {
		header: "Patient Zipcode",
		dataIndex: "zipcode",
		width: 85
	}, {
		header: "Facility Name",
		dataIndex: "facilityName",
		width: 175
	}, {
		header: "Reason",
		dataIndex: "reason",
		width: 200
	}, {
		header: "Classification",
		dataIndex: "classification",
		width: 100
	}]);
		
	var store = new Ext.data.GroupingStore({
		proxy: new Ext.data.DWRProxy(PatientService.getPatientHistory, [facilityId, patientId, null]),
		sortInfo: { field: "interactionDate", direction: "DESC" },
		groupField: "visitNumber",
		reader: new Ext.data.ObjectReader({}, casesDetailRecordType)
	});
	
	 EpiCenter.lib.PatientHistory.superclass.constructor.call(this, {
		store: store,
		closable: true,
		cm: cm,
		loadMask: true,
		title: "Patient: " + patientId,
		frame: true,
		stripeRows: true,
		view: new Ext.grid.GroupingView({
            forceFit:true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
        })
	});
	
	this.on("render", function() {
		store.load();
	}, this, {single: true});
	
};

Ext.extend(EpiCenter.lib.PatientHistory, Ext.grid.GridPanel);

