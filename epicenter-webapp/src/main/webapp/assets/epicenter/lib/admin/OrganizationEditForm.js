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
EpiCenter.lib.admin.OrganizationEditForm = function(){

	// Workflow dataStore
	this.workflowDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(WorkflowService.getWorkflows),
		reader: new Ext.data.ObjectReader({}, EpiCenter.common.simpleRecordType)
	});
	
	// Organization Form
	this.orgId = new Ext.form.Hidden({
		name: "id"
	});
	
	this.orgName = new Ext.form.TextField({
		fieldLabel: "Organization Name",
		allowBlank: false,
		width: 175,
		name: "name"
	});
	
	this.orgDescription = new Ext.form.TextArea({
		fieldLabel: "Description",
		allowBlank: true,
		width: 175,
		name: "description"
	});
	
	this.orgWorkflow = new Ext.form.ComboBox({
		fieldLabel: 'Workflow',
		store: this.workflowDataStore,
		forceSelection: true,
		valueField: 'id',
		displayField: 'value',
		mode: 'local',
		triggerAction: 'all',
		width: 175,
		minListWidth: 175,
		selectOnFocus: true,
		allowBlank: false,
		editable: false,
		name: "workflow",
		hiddenName: "workflow",
		autoListWidth: true
	});
	
	this.orgAuthGeography = new EpiCenter.lib.GeographySelector({
		fieldLabel: "Authoritative Region",
		width: 175,
		minListWidth: 175,
		name: "authoritativeRegion",
		hiddenName: "authoritativeRegion",
		allowBlank: true
	});
	
	EpiCenter.lib.admin.OrganizationEditForm.superclass.constructor.call(this, {
		labelAlign: "right",
		buttonAlign: "right",
		labelWidth: 150,
		autoScroll: true,
		items: [this.orgId, this.orgName, this.orgDescription, this.orgWorkflow, this.orgAuthGeography]
	});

};

Ext.extend(EpiCenter.lib.admin.OrganizationEditForm, Ext.form.FormPanel, {
	load: function(organization) {
		if (organization) {
			this.orgId.setValue(organization.id);
			this.orgName.setValue(organization.name);
			this.orgDescription.setValue(organization.description);
			this.orgWorkflow.setValue(organization.workflow.id);
			
			var a = organization.authoritativeRegion;
			if (a) {
				this.orgAuthGeography.store.loadData([{ id: a.id, name: a.displayName }]);
				this.orgAuthGeography.setValue(a.id);
			}
		}
	}
});

