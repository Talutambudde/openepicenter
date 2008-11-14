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
EpiCenter.lib.Cases = function(config, parameters){

	this.parameters = parameters;

	Ext.apply(this, config);
		
	var noHistoryAvailiable = new Ext.Template("<div style='padding: 5px 0px 5px 10px; font-style: italic;'><span>Patient history is not available for this record.</span></div>");
	
	var historyTemplate = new Ext.Template("<div style='padding: 5px 10px 0px 10px; background-color: #eeeeee; border: 1px dashed #15428B;'>",
		"<div style='padding-left: 15px;'><table width='100%'><tr>",
		"<td width='25%'><b>Visit Number:</b> {visitNumber}</td>",
		"<td width='25%'><b>Patient Id:</b> {patientId}</td>",
		"<td width='25%'><b>Date of Birth:</b> {dob:date('m/d/Y')}</tr>",
		"<td width='25%'><div id='{buttonId}'></div></td>",
		"</tr></table></div></div>");
		
	var historyRecordTemplate = new Ext.Template("<div>",
		"<div style='float: left; width: 15px; height: 40px;'><span><b>{index}.</b></div>",
		"<div style='zoom: 1;'><table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>",
		"<td width='25%'><b>Type:</b> {type}</td>",
		"<td width='25%'><b>Time:</b> {interactionDate:date('m/d/Y h:i:s A (T)')}</td>",
		"<td width='25%'><b>Patient Class:</b> {patientClass}</td>",
		"<td width='25%'><b>Age Group:</b> {ageGroup}</td>",
		"</td></tr><tr>",
		"<td colspan='2'><b>Classifications:</b> {classification}</td>",
		"<td colspan='2'><b>Diagnostic Codes:</b> {icd9Codes}</td>",
		"</tr></table></div>");
		
	// Row expander
	var expander = new Ext.ux.grid.RowExpander();
	
	expander.on("expand", function(expander, record, body, rowIndex) {
		
		if (record.get("patientId") && record.get("facilityId") && record.get("visitNumber")) {
			PatientService.getPatientHistory(record.get("facilityId"), record.get("patientId"), record.get("visitNumber"), function(result) {
				if (result !== null && result.length > 0) {
					
					var idx = 1;
					
					var buttonId = Ext.id();
										
					var history = historyTemplate.overwrite(body, Ext.apply(result[0], {
						buttonId: buttonId
					}));
					
					var button = new Ext.Button({
						applyTo: buttonId,
						text: "View Full History",
						handler: function() {
							var patientHistory = new EpiCenter.lib.PatientHistory({}, record.get("facilityId"), record.get("patientId"));
							this.ownerCt.add(patientHistory).show();
						},
						scope: this
					});
					
					Ext.each(result, function(r){
						historyRecordTemplate.append(history, Ext.apply(r, {
							index: idx
						}));
						idx++;
					}, this);
					
				} else {
					noHistoryAvailiable.overwrite(body);
				}
			}.createDelegate(this));
		} else {
			noHistoryAvailiable.overwrite(body);
		}
	}, this);
	
	// Record type for Cases
	var casesRecordType = Ext.data.Record.create([{
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
	}]);
	
	// Cases grid ColumnModel
	this.cm = new Ext.grid.ColumnModel([expander, {
		header: "Date",
		dataIndex: "interactionDate",
		renderer: Ext.util.Format.dateRenderer('m/d/Y H:i'),
		width: 100
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
		header: "Visit Number",
		dataIndex: "visitNumber",
		width: 85
	}, {
		header: "Reason",
		dataIndex: "reason",
		width: 200
	}, {
		header: "Classification",
		dataIndex: "classification",
		width: 100
	}]);
		
	this.store = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(PatientService.getCases, this.parameters, true),
		sortInfo: { field: "interactionDate", direction: "DESC" },
		reader: new Ext.data.ObjectReader({
			totalProperty: "totalItems",
			root: "items"
		}, casesRecordType)
	});
	
	function downloadCases(){
		Ext.ux.PostAction.post("download/cases", this.parameters);
	}

	// Add the pager
	this.bbar = new Ext.PagingToolbar({
		store: this.store,
		pageSize: 100,
		displayInfo: true,
		items: ["-", {
			text: "Download Cases as CSV",
			iconCls: "icon-save",
			tooltip: "Download cases as a comma-separated-values file.",
			handler: downloadCases.createDelegate(this, [this.store], true)
		}, {
			text: "Expand all rows",
			iconCls: "icon-find",
			tooltip: "View details for all records",
			handler: function() {
								
				dwr.engine.beginBatch();
				var count = 0;
				
				// Iterating on NodeList is broken in Ext 2.1.
				Ext.each($A(expander.grid.getView().getRows()), function(row) {		
					expander.expandRow(row);
					count++;
					if (count % 20 === 0) {
						dwr.engine.endBatch();
						dwr.engine.beginBatch();
					}
				}, this);
				dwr.engine.endBatch();
			},
			scope: this
		}]
	});
		
	EpiCenter.lib.Cases.superclass.constructor.call(this, {
		loadMask: true,
		autoExpandColumn: 6,
		title: "Cases",
		stripeRows: true,
		plugins: expander,
		frame:true,
		viewConfig: {
			forceFit: true
		}
	});

	this.on("render", function() {
		this.store.load({params:{start:0,limit:100}});
	}, this, {single: true});
};

Ext.extend(EpiCenter.lib.Cases, Ext.grid.GridPanel);

