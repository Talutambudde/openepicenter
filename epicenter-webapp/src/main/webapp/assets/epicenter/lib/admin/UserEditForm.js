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
EpiCenter.lib.admin.UserEditForm = function(){

	// User Form
	var userId = new Ext.form.Hidden({
		name: "id"
	});
	
	var username = new Ext.form.TextField({
		fieldLabel: "Username",
		allowBlank: false,
		width: 175,
		name: "username"
	});
	
	var password = new Ext.ux.PasswordField({
		fieldLabel: "Password",
		allowBlank: true,
		width: 175,
		inputType: 'password',
		showCapsWarning: true,
		showStrengthMeter: true,
		name: "password"
	});
	
	var confirmPassword = new Ext.form.TextField({
		fieldLabel: "Password (confirm)",
		allowBlank: true,
		width: 175,
		inputType: 'password',
		name: "confirmPassword"
	});
	
	var emailAddress = new Ext.form.TextField({
		fieldLabel: "Email Address",
		allowBlank: false,
		width: 175,
		name: "emailAddress",
		vtype: "email"
	});
	
	var firstName = new Ext.form.TextField({
		fieldLabel: "First Name",
		allowBlank: false,
		width: 175,
		name: "firstName"
	});
	
	var lastName = new Ext.form.TextField({
		fieldLabel: "Last Name",
		allowBlank: false,
		width: 175,
		name: "lastName"
	});
	
	var middleInitial = new Ext.form.TextField({
		fieldLabel: "Middle Initial",
		allowBlank: true,
		width: 175,
		name: "middleInitial"
	});
	
	var organization = new Ext.form.TextField({
		fieldLabel: "Organization",
		allowBlank: true,
		width: 175,
		name: "organization"
	});
	
	var title = new Ext.form.TextField({
		fieldLabel: "Title",
		allowBlank: true,
		width: 175,
		name: "title"
	});
	
	var address = new Ext.form.TextField({
		fieldLabel: "Address",
		allowBlank: true,
		width: 175,
		name: "address"
	});
	
	var city = new Ext.form.TextField({
		fieldLabel: "City",
		allowBlank: true,
		width: 175,
		name: "city"
	});
	
	var state = new Ext.form.ComboBox({
		fieldLabel: 'State',
		store: EpiCenter.common.statesDataStore,
		forceSelection: true,
		valueField: 'id',
		displayField: 'name',
		mode: 'local',
		triggerAction: 'all',
		width: 175,
		minListWidth: 175,
		selectOnFocus: true,
		autoListWidth: true,
		allowBlank: true,
		editable: true,
		typeAhead: true,
		name: "state",
		hiddenName: "state"
	});
	
	var zipcode = new Ext.form.TextField({
		fieldLabel: "Zipcode",
		allowBlank: true,
		width: 175,
		name: "zipcode"
	});
	
	var phoneNumber = new Ext.form.TextField({
		fieldLabel: "Phone Number",
		allowBlank: true,
		width: 175,
		name: "phoneNumber"
	});
	
	var phoneExtension = new Ext.form.TextField({
		fieldLabel: "Extension",
		allowBlank: true,
		width: 175,
		name: "phoneExtension"
	});
	
	var faxNumber = new Ext.form.TextField({
		fieldLabel: "Fax Number",
		allowBlank: true,
		width: 175,
		name: "faxNumber"
	});

	var isAdministrator = new Ext.form.Checkbox({
		fieldLabel: "Organizational Administrator",
		name: "isAdministrator",
		checked: false
	});
	
	EpiCenter.lib.admin.UserEditForm.superclass.constructor.call(this, {
		labelAlign: "right",
		buttonAlign: "right",
		labelWidth: 150,
		autoScroll: true,
		layout: "column",
		items: [ {
			layout: "form",
			autoHeight: true,
			columnWidth: 0.5,
			items: [userId, username, emailAddress, firstName, lastName, middleInitial, title, organization, address, city, state, zipcode, phoneNumber, phoneExtension, faxNumber, isAdministrator]
		}, {
			columnWidth: 0.5,
			layout: "form",
			autoHeight: true,
			items: [password, confirmPassword]
		} ]
	});
	
};

Ext.extend(EpiCenter.lib.admin.UserEditForm, Ext.form.FormPanel, {
	getValues: function() {
		var values = this.form.getValues();
		delete values.password;
		delete values.confirmPassword;
		delete values.isAdministrator;
		return values;
	}
});

