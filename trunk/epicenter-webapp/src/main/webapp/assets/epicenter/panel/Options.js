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
EpiCenter.panel.Options = function(){

	// Password Change form
	var oldPassword = new Ext.form.TextField({
		fieldLabel: "Old Password",
		allowBlank: false,
		width: 175,
		inputType: 'password',
		name: "oldPassword"
	});
	
	var newPassword = new Ext.ux.PasswordField({
		fieldLabel: "New Password",
		allowBlank: false,
		width: 175,
		inputType: 'password',
		showCapsWarning: true,
		showStrengthMeter: true,
		name: "newPassword"
	});
	
	var confirmNewPassword = new Ext.form.TextField({
		fieldLabel: "New Password (confirm)",
		allowBlank: false,
		width: 175,
		inputType: 'password',
		name: "confirmNewPassword"
	});
	
	
	// User Info form
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
		selectOnFocus: true,
		allowBlank: true,
		editable: false,
		name: "state"
	});
	
	var zipcode = new Ext.form.NumberField({
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
	
	var faxNumber = new Ext.form.TextField({
		fieldLabel: "Fax Number",
		allowBlank: true,
		width: 175,
		name: "faxNumber"
	});
	
	
	var changePasswordForm = new Ext.form.FormPanel({
		//border: false,
		autoHeight: true,
		width: 350,
		buttonAlign: "right",
		labelAlign: "right",
		labelWidth: 100,
		frame: true,
		items: [{
			xtype: "fieldset",
			autoHeight: true,
			title: "Change Password",
			items: [oldPassword, newPassword, confirmNewPassword]
		}],
		buttons: [{
			text: "Change",
			handler: function(){
			
				if (newPassword.getValue() != confirmNewPassword.getValue()) {
					confirmNewPassword.markInvalid();
				} else 
					if (changePasswordForm.getForm().isValid()) {
					
						changePasswordForm.getEl().mask();
						changePasswordForm.getForm().doAction("DWRSubmit", {
							dwrMethod: OptionsService.changePassword,
							params: [oldPassword.getValue(), newPassword.getValue()],
							callback: function(success){
								if (success) {
									Ext.ux.Toast.msg("Change Password", "Your password has been changed.");
									changePasswordForm.getForm().reset();
								} else {
									Ext.Msg.alert("Change Password", "Password change failed.");
								}
								changePasswordForm.getEl().unmask();
							}
						});
					}
			}
		}, {
			text: "Clear",
			handler: function(){
				changePasswordForm.getForm().reset();
			}
		}]
	});
	
	var userInfoForm = new Ext.form.FormPanel({
		//border: false,
		autoHeight: true,
		width: 350,
		buttonAlign: "right",
		labelAlign: "right",
		labelWidth: 100,
		frame: true,
		items: [{
			xtype: "fieldset",
			autoHeight: true,
			title: "User Information",
			items: [emailAddress, firstName, lastName, middleInitial, title, organization, address, city, state, zipcode, phoneNumber, faxNumber]
		}],
		buttons: [{
			text: "Update",
			handler: function(){
				if (userInfoForm.getForm().isValid()) {
					EpiCenter.common.userinfo.update(userInfoForm.getForm().getValues(), function() {
						Ext.ux.Toast.msg("Status", "Your user information has been updated.");
					});
				}
			}
		}, {
			text: "Reset",
			handler: function(){
				userInfoForm.getForm().reset();
			}
		}]
	});

	userInfoForm.form.trackResetOnLoad = true;
	
	
	
	var defaultRegionStore = new Ext.data.Store({
		reader: new Ext.data.ObjectReader({}, EpiCenter.common.geographyRecordType)
	});
	
	console.log("regions: ", EpiCenter.common.userinfo.getVisibleRegions());
	
	defaultRegionStore.loadData(EpiCenter.common.userinfo.getVisibleRegions());
	
	var defaultRegion = new Ext.form.ComboBox({
		fieldLabel: 'Default Region',
		store: defaultRegionStore,
		forceSelection: true,
		valueField: 'id',
		displayField: 'name',
		mode: 'local',
		triggerAction: 'all',
		width: 175,
		selectOnFocus: true,
		allowBlank: false,
		editable: false,
		name: "DEFAULT_REGION",
		hiddenName: "DEFAULT_REGION"
	});
	
	defaultRegion.on("render", function(combo) {
		combo.setValue(EpiCenter.common.userinfo.getDefaultRegion().id);
	}, this);
	
	
	var preferencesForm = new Ext.form.FormPanel({
		autoHeight: true,
		width: 350,
		buttonAlign: "right",
		labelAlign: "right",
		labelWidth: 100,
		style: "margin-top: 40px",
		frame: true,
		items: [ {
			xtype: "fieldset",
			autoHeight: true,
			title: "Preferences",
			items: [ defaultRegion ]
		}],
		buttons: [{
			text: "Update",
			handler: function() {
				if (preferencesForm.getForm().isValid()) {
					EpiCenter.common.userinfo.updatePreferences(preferencesForm.getForm().getValues(), function() {
						Ext.ux.Toast.msg("Status", "Your preferences have been updated.");
					});
				}
			}
		}, {
			text: "Reset",
			handler: function() {
				preferencesForm.getForm().reset();
			}
		}]
	});
	
	preferencesForm.form.trackResetOnLoad = true;
	
	
	confirmNewPassword.on("change", function(){
		if (this.getValue() != newPassword.getValue()) {
			this.markInvalid();
		}
	});
	
	//	Ext.fly("change-password").boxWrap();
	//	Ext.fly("user-info").boxWrap();
	
	EpiCenter.panel.Options.superclass.constructor.call(this, {
		layout: "column",
		title: "Options",
		autoScroll: true,
		bodyStyle: "padding-top: 15px",
		items: [{
			border: false,
			columnWidth: 0.4,
			html: "<br/>"
		}, userInfoForm, {
			border: false,
			columnWidth: 0.2,
			html: "<br/>"
		}, {
			border: false,
			width: 350,
			layout: "fit",
			items: [ changePasswordForm, preferencesForm ]
		}, {
			border: false,
			columnWidth: 0.4,
			html: "<br/>"
		}]
	});
	
	userInfoForm.on("render", function(p) {
		setTimeout(function() {
			p.form.setValues(EpiCenter.common.userinfo);
		}, 100);
	});
};

Ext.extend(EpiCenter.panel.Options, Ext.Panel);

