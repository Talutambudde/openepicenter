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
EpiCenter.lib.AnalyzerDetails = function(config) {
	
	this.getValues = function() {
		return this.form.getForm().getValues(false);
	};
	
	var form = new Ext.form.FormPanel({
		border: false,
		labelAlign: "right",
		labelWidth: 250,
		autoScroll: true
	});
	
	this.form = form;
	
	var fieldsets = [];
	var names = [];
		
	Ext.apply(this, config);
	
	EpiCenter.lib.AnalyzerDetails.superclass.constructor.call(this, {
		layout: "fit",
		defaults: {
			bodyStyle: "padding: 10px 10px 0px 15px;"
		},
		height: 300,
		width: 500,
		collapsible: true,
		titleCollapse: true,
		constrain: true,
		buttons: [ {
			text: "Reset",
			scope: this,
			handler: function() {
				this.form.getForm().setValues(this.defaultValues);
			}
		} ]
	});
	
	function fixName(item) {
		// Fix the class name to something human readable.
		return item.match(/\.(\w+)@/)[1].replace(/([A-Z])/g, function(match) { return " " + match; }).strip();
	}
	
	function updateEffectiveTrainingWindowValues(name, field, values) {
		
		AnalysisService.getEffectiveTrainingPeriods(name, values, function(result) {
			
			var idx = 0;
			Ext.each(fieldsets, function(fieldset) {
				fieldset.setTitle(names[idx] + " (Window: " + result[idx] + ")");
				
				// If the parameters result in a psychotic training window, mark the field as invalid.
				if (result[idx] < 0 || result[idx] > 1095) {
					field.markInvalid("Invalid parameters.");
				}
				
				idx++;
				
			}, this);
			
		}.createDelegate(this));
	}
	
	function updateFormValues() {
		
		this.setTitle("Analyzer Configuration: " + this.algorithm);
		this.body.mask("Loading Parameters..", "x-mask-loading");
		
		fieldsets = [];
		names = [];
		
		AnalysisService.getAnalyzerDetails(this.algorithm, function(details) {
						
			this.metadata = details.metadata.algorithms;

			var firstField;
			
			Ext.each(Object.keys(this.metadata), function(algorithm) {
				
				var fieldset = new Ext.form.FieldSet({
					title: fixName(algorithm),
					autoHeight: true
				});
				
				Ext.each(this.metadata[algorithm], function(parameter) {
					
					if (parameter.description) {
											
						if (parameter.typeAsString == "double" || parameter.typeAsString == "integer") {
							var item = new Ext.form.NumberField({
								allowDecimals: (parameter.typeAsString == "double"),
								decimalPrecision: 20,
								name: parameter.name,
								value: parameter.defaultValue,
								fieldLabel: parameter.description,
								allowBlank: false
							});
							
							item.on("change", function(field) {
								updateEffectiveTrainingWindowValues(this.algorithm, field, this.getValues());
							}, this);
							
							fieldset.add(item);
												
							if (firstField === null) {
								firstField = item;
							}
						}
						
					}
				}, this);
				
				this.form.add(fieldset);
				fieldsets.push(fieldset);
				names.push(fieldset.title);
				
			}, this);
			
			this.add(this.form);
			this.doLayout();
			this.defaultValues = this.getValues();
			
			updateEffectiveTrainingWindowValues(this.algorithm, this.getValues());
			
			this.body.unmask();

			if (firstField) {
				setTimeout(function() {
					firstField.focus();
				}.createDelegate(this), 10);
			}
			
		}.createDelegate(this));
	}
	
	this.on("render", updateFormValues);
	
	// Stop mouse wheel events from going thru the window
	this.on("render", function() {
		this.body.on("mousewheel", function(e) {
			e.stopPropagation();
		}, this);
	}, this);

};

Ext.extend(EpiCenter.lib.AnalyzerDetails, Ext.Window);
