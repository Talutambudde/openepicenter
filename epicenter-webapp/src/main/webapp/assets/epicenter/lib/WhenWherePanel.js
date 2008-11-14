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
EpiCenter.lib.WhenWherePanel = function(config) {
	
	this.optionsEl = Ext.id();
	
	this.addEvents({
		accept: true,
		acceptInvisible: true,
		optionsclick: true
	});

	var panelTotalWidth = 250;
	var panelWidth = panelTotalWidth - 20;
	var endDateFieldWidth = 102;
	var submitButtonWidth = 62;
	
	Ext.apply(this, config);
	
	
	// Date Fields
	this.endDateField = new Ext.form.DateField({
		allowBlank: false,
		value: new Date(),
		maxValue: new Date(),
		format: 'm-d-Y',
		width: endDateFieldWidth,
		qtipText: "The date to focus on."
	});
	
	this.historyField = new EpiCenter.lib.DurationField({
		allowBlank: false,
		value: "7 days",
		width: 120,
		qtipText: "How far back into history to go.  Any text value such as \"7 days\", \"1 year\", or \"2 weeks 3 days\" is valid."
	});
	
	this.buttonTooltip = "Accept the current values";
	
	// Geography ComboBox with autocomplete	
	this.geographyField = new EpiCenter.lib.GeographySelector({
		name: "geography",
		allowBlank: (this.allowBlankGeography !== undefined ? this.allowBlankGeography : false),
		width: panelWidth,
		minListWidth: panelWidth,
		emptyText: "All Visible Regions",
		qtipText: "The geographical area display data for.  Any freetext value is acceptable (including state abbreviations) and suggestions will be shown as you type."
	});
	
	var scope = this;
		
	var acceptButton = new Ext.Button({
		text: "Submit",
		minWidth: submitButtonWidth,
		tooltip: this.buttonTooltip,
		handler: function() {
			if (scope.getForm().isValid()) {
				scope.historyField.normalizeDuration();
				var geographyId = scope.geographyField.getValue();
				if (geographyId) {
					var geographyIndex = scope.geographyField.store.find("id", geographyId);
					var geographyRecord = scope.geographyField.store.getAt(geographyIndex);
					var visibility = geographyRecord.get("visibility");
					if (visibility == "LIMITED") {
						scope.getForm().markInvalid({
							"geography": "The selected geography is not accessible"
						});
						scope.fireEvent("acceptInvisible", scope);
					}
					else {
						scope.fireEvent("accept", scope);
					}
				}
				else {
					scope.fireEvent("accept", scope);
				}
			}
		}
	});
	
	EpiCenter.lib.WhenWherePanel.superclass.constructor.call(this, {
		hideLabels: true,
		buttonAlign: "right",
		height: this.height ? this.height : 88,
		width: panelTotalWidth,
		bodyStyle: "padding: 6px 10px 6px 15px;",
		defaults: { hideLabels: true, width: panelWidth },
		items: [ this.geographyField, {
			layout: "column",
			border: false,
			items: [ {
				border: false,
				width: panelWidth - endDateFieldWidth,
				items: [ this.historyField ]
			}, {
				border: false,
				width: endDateFieldWidth,
				items: [ this.endDateField ]
			} ]
		}, {
			layout: "column",
			bodyStyle: "padding-top: 4px;",
			border: false,
			items: [ {
				border: false,
				bodyStyle: "padding-top: 2px;",
				width: panelWidth - submitButtonWidth,
				autoHeight: true,
				html: { id: this.optionsEl, cls: "when-where-advanced", html: this.linkText ? "<a>" + this.linkText + "</a>": "" },
				listeners: {
					"render": {
						fn: function(cmp) {
							cmp.body.on("click", function(e) {
								this.fireEvent("optionsclick", cmp, e);
							}, this);
						},
						scope: this
					}
				}
			}, {
				border: false,
				width: submitButtonWidth,
				items: [ acceptButton ]
			} ]
		} ]
	});
	
};

Ext.extend(EpiCenter.lib.WhenWherePanel, Ext.form.FormPanel, {
	
	setLinkText: function(text) {
		Ext.DomHelper.overwrite(Ext.get(this.optionsEl), text);
	},
	
	getStore: function() {
		return this.geographyField.store;
	},
	
	getStartDate: function() {
		return this.historyField.parseDuration(this.getEndDate(), true);
	},
	
	getEndDate: function() {
		var today = new Date();
		if (this.endDateField.getValue() && today.format('Y-m-d') == this.endDateField.getValue().format('Y-m-d')) {
			return today;
		} else {
			return this.endDateField.getValue();
		}
	},
	
	getGeography: function() {
		return this.geographyField.getValue();
	},
	
	getVisibilityForSelectedGeography: function() {
		var record = this.geographyField.store.getAt(this.geographyField.store.find("id", this.geographyField.getValue()));
		if (record != null) {
			return record.get("visibility");
		}
	},
	
	getValues: function() {
		return {
			start: this.getStartDate(),
			end: this.getEndDate(),
			geography: this.getGeography()
		};
	},
	
	getOptionsEl: function() {
		return this.optionsEl;
	},
	
	setValues: function(date, historyString, geography, geographyId) {
		this.endDateField.setValue(date);
		this.historyField.setValue(historyString);
		if (geographyId !== undefined) {
			this.geographyField.store.loadData([{ id: geographyId, name: geography, visibility: "FULL" }]);	
			this.geographyField.setValue(geographyId);
		} else {
			// todo this is revolting, there must be a better way, but lets have it fixed this way for
			// this very minute
			this.geographyField.load(geography.sub(' County', ''));
		}

	},
	
	setValuesFromParameters: function(params) {
		var analysisDays = Math.ceil((params.end - params.start) / (1000*60*60*24));
		var history = analysisDays + ' days';
		this.setValues(new Date(params.end), history, params.geographyName, params.geography);
	}
});

