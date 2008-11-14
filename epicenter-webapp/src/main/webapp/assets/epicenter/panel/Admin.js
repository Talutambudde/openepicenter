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
EpiCenter.panel.Admin = function(){

	var userEditForm = new EpiCenter.lib.admin.UserEditForm();
	
	var orgEditForm = new EpiCenter.lib.admin.OrganizationEditForm();
	
	var authorizedRegionEditor = new EpiCenter.lib.admin.AuthorizedRegionEditor();
	
	var sponsorshipEditor = new EpiCenter.lib.admin.SponsorshipEditor();
		
	var orgPanel = new Ext.Panel({
		layout: "border",
		title: "Edit Organization",
		autoScroll: true,
		border: false,
		frame: true,
		items: [ {
			region: "center",
			autoScroll: true,
			layout: "column",
			border: false,
			items: [ {
				layout: "form",
				columnWidth: 0.5,
				items: [ orgEditForm ]
			}, {
				layout: "fit",
				columnWidth: 0.5,
				items: [ authorizedRegionEditor ]
			} ]
		}, {
			region: "south",
			height: 280,
			items: [ sponsorshipEditor ]
		} ],
		buttons: [{
			text: "Save",
			handler: function(){
				if (orgEditForm.getForm().isValid()) {
					
					if (!orgEditForm.orgId.getValue()) {

						AdminService.isOrganizationAvailable(orgEditForm.form.findField("name").getValue(), {
							async: false,
							callback: function(result) {
								if (!result) {
									orgEditForm.orgId.markInvalid("Organization name is already in use.");
								}
							}
						});
					}
					
					if (orgEditForm.getForm().isValid()) {
					
						var values = orgEditForm.getForm().getValues();
						var ars = [];
						authorizedRegionEditor.store.each(function(m) {
							ars.push({
								grantedById: m.get("grantedById"),
								geographyId: m.get("geographyId"),
								type: m.get("type")
							});
						});
						
						AdminService.saveOrganization({
							id: values.id,
							name: values.name,
							description: values.description
						}, values.workflow, values.authoritativeRegion, sponsorshipEditor.getSelectedValues(), ars, function(newOrg){
							
							authorizedRegionEditor.store.commitChanges();
							orgEditForm.load(newOrg);
							
							Ext.ux.Toast.msg("Status", "Organization information has been saved.");
							
							dwr.engine.beginBatch();
							orgTree.getRootNode().reload();
							authorizedRegionEditor.containerStore.load();
							dwr.engine.endBatch();
							
						});
					}
				}
			}
		}, {
			text: "Reset",
			handler: function(){
				orgEditForm.getForm().reset();
				authorizedRegionEditor.reset();
			}
		}]
	});

	var userPanel = new Ext.Panel({
		autoScroll: true,
		border: false,
		title: "Edit User",
		layout: "fit",
		frame: true,
		items: [userEditForm],
		buttons: [{
			text: "Save",
			handler: function(){
				if (userEditForm.form.isValid()) {
					
					var password = userEditForm.form.findField("password");
					var confirmPassword = userEditForm.form.findField("confirmPassword");
					
					if (password.getValue() || confirmPassword.getValue()) {
						if (!checkPasswordsMatch(password.getValue(), confirmPassword.getValue())) {
							password.markInvalid("Passwords do not match");
							confirmPassword.markInvalid("Passwords do not match");
							return;
						}
						
					}
					var selected = orgTree.getSelectionModel().getSelectedNode();
					var org = selected.id.startsWith("org-") ? selected : selected.parentNode;
					var orgId = org.id.split("-")[1];
					AdminService.saveUser(userEditForm.getValues(), orgId, password.getValue(), userEditForm.form.findField("isAdministrator").getValue(), function(){
						Ext.ux.Toast.msg("Status", "User information has been saved.");
						org.reload(function() {
							orgTree.getNodeById(selected.id).select();
						}.createDelegate(this));
					}.createDelegate(this));
				}
			}
		}, {
			text: "Reset",
			handler: function(){
				userEditForm.getForm().reset();
			}
		}]
	});
	
	var displaySelectedNode = function(node, event){
		
		var id;
		
		if (node.id.startsWith("org-")) {
		
			// Load and display the Organization
			id = node.id.split("-")[1];
			AdminService.getOrganization(id, function(organization){
				cards.getLayout().setActiveItem(1);
				orgPanel.setTitle("Edit Organization: " + organization.name);
				orgEditForm.getForm().reset();
				
				dwr.engine.beginBatch();
				orgEditForm.load(organization);
				authorizedRegionEditor.load(organization);
				dwr.engine.endBatch();

				sponsorshipEditor.load(getSponsorList(id), organization);
			});
			
		} else if (node.id.startsWith("user-")) {
			
			// Load and display the User
			id = node.id.split("-")[1];
			AdminService.getUser(id, function(user){
				cards.getLayout().setActiveItem(2);
				userPanel.setTitle("Edit User: " + user.username);
				userEditForm.getForm().reset();
				userEditForm.getForm().setValues(user);
				Ext.each(user.roles, function(role) {
					if (role.authority == "ROLE_ORG_ADMIN") {
						userEditForm.getForm().findField("isAdministrator").setValue(true);
					}
				}, this);
			});
			
		} else {
			cards.getLayout().setActiveItem(0);
		}
	};
		
	var newOrgButton = new Ext.Button({
		tooltip: "Create a new organization",
		iconCls: "icon-organization",
		handler: function(){
			cards.getLayout().setActiveItem(1);
			orgPanel.setTitle("Create New Organization");
			orgEditForm.getForm().reset();
			authorizedRegionEditor.store.removeAll();
			sponsorshipEditor.load(getSponsorList());
		}
	});
	
	var newUserButton = new Ext.Button({
		tooltip: "Create a new user",
		iconCls: "icon-user",
		handler: function(){
			cards.getLayout().setActiveItem(2);
			userEditForm.getForm().reset();
			userPanel.setTitle("Create New User (Organization: " + orgTree.getSelectionModel().getSelectedNode().text + ")");
		},
		disabled: true
	});
	
	var deleteButton = new Ext.Button({
		tooltip: "Delete this entry",
		iconCls: "icon-trash",
		handler: function(){
			var node = orgTree.getSelectionModel().getSelectedNode();
			if (node.id.startsWith("org-")) {
				var orgId = node.id.split("-")[1];
				Ext.MessageBox.show({
					title: 'Delete Organization',
					msg: 'Are you sure you would like to delete "' + node.text + '" and all associated users?',
					buttons: Ext.MessageBox.OKCANCEL,
					icon: Ext.MessageBox.QUESTION,
					animEl: deleteButton.getEl(),
					fn: function(btn){
						if (btn == 'ok') {
							AdminService.deleteOrganization(orgId, true, function(){
								orgTree.getRootNode().reload();
								cards.getLayout().setActiveItem(0);
								Ext.ux.Toast.msg("Status", "Organization has been deleted.");
							});
						}
					}
				});
			} else 
				if (node.id.startsWith("user-")) {
					var userId = node.id.split("-")[1];
					Ext.MessageBox.show({
						title: "Delete User",
						msg: "Are you sure you would like to delete \"" + node.text + "\"?",
						buttons: Ext.MessageBox.OKCANCEL,
						icon: Ext.MessageBox.QUESTION,
						animEl: deleteButton.getEl(),
						fn: function(btn){
							if (btn == 'ok') {
								AdminService.deleteUser(userId, function(){
									node.parentNode.reload();
									cards.getLayout().setActiveItem(0);
									Ext.ux.Toast.msg("Status", "User has been deleted.");
								});
							}
						}
					});
				}
		}
	});
	
	function getSponsorList(id) {
		var sponsors = [];
		authorizedRegionEditor.containerStore.each(function(record) {
			if (id === undefined || record.get("id") != id) {
				sponsors.push(record.data);
			}
		}, this);
		return sponsors;
	}
	
	function checkPasswordsMatch(pw, confirmPw) {
		var ret = true;
		if (pw !== null && confirmPw !== null) {
			if (pw !== confirmPw) {
				ret = false;
			}
		} else {
			ret = false;
		}
		return ret;
	}
	
	// The Organizations tree
	var orgTree = new Ext.tree.TreePanel({
		title: "Organizations",
		animate: true,
		containerScroll: true,
		autoScroll: true,
		layout: "fit",
		loader: new Ext.tree.DWRTreeLoader({
			dwrCall: AdminService.getOrganizationTree
		}),
		root: new Ext.tree.AsyncTreeNode({
			text: "Organizations",
			draggable: false,
			id: "root"
		}),
		tbar: [newOrgButton, "-", newUserButton, "->", deleteButton]
	});
	
	new Ext.tree.TreeSorter(orgTree);
	
	orgTree.on("click", displaySelectedNode);
	orgTree.on("beforeclick", function(node){
		if (node.id.startsWith("org-")) {
			// Enable create user when org is selected
			newUserButton.enable();
		} else {
			newUserButton.disable();
		}
	});
	
	// Not implemented yet.
	var providerTree = new Ext.tree.TreePanel({
		title: "Providers",
		animate: true,
		containerScroll: true,
		autoScroll: true,
		layout: "fit"
	});
	
	
	// Card panels
	var cards = new Ext.Panel({
		layout: "card",
		region: "center",
		activeItem: 0,
		items: [{
			layout: "fit",
			border: false,
			autoScroll: true,
			html: "<div style='text-align: center; padding-top: 20px;'><span>Please select an item from the tree..</span></div>"
		}, orgPanel, userPanel]
	});
	
	EpiCenter.panel.Admin.superclass.constructor.call(this, {
		title: "Administration",
		layout: "border",
		items: [new Ext.TabPanel({
			region: "west",
			activeTab: 0,
			width: 260,
			minSize: 260,
			maxSize: 260,
			collapsible: true,
			collapseMode: "mini",
			split: true,
			tabPosition: "bottom",
			items: [orgTree, providerTree]
		}), cards]
	});
	
	this.on("render", function() {
		dwr.engine.beginBatch();
		orgEditForm.workflowDataStore.load();
		authorizedRegionEditor.containerStore.load();
		authorizedRegionEditor.typeStore.load();
		dwr.engine.endBatch();
	}, this, {single: true});
	
};
Ext.extend(EpiCenter.panel.Admin, Ext.Panel);

