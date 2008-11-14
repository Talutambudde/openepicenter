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
EpiCenter.Userinfo = function(){

	this.addEvents({
		load: true,
		update: true
	});
};

Ext.extend(EpiCenter.Userinfo, Ext.util.Observable, {

	load: function(){
		OptionsService.getUserInfo(function(info){
			Ext.apply(this, info);		
			this.fireEvent("load", this);
		}.createDelegate(this));
	},
	
	update: function(newInfo, callback){
		var cinfo = Ext.ux.clone(newInfo);
		delete cinfo.preferences;
		
		OptionsService.updateUserInfo(cinfo, function(info){
			Ext.apply(this, info);
			if (callback !== undefined) {
				callback.call(this);
			}
			this.fireEvent("update", this);
			this.fireEvent("load", this);
		}.createDelegate(this));
	},
	
	updatePreferences: function(prefs, callback) {
		OptionsService.updatePreferences(prefs, function(info) {
			Ext.apply(this, info);
			if (callback !== undefined) {
				callback.call(this);
				this.fireEvent("update", this);
			}
		}.createDelegate(this));
	},
	
	getVisibleRegions: function() {
		
		var regions = [];
		
		Ext.each(this.organizations, function(organization){
			if (organization.authoritativeRegion) {
				regions.push({
					id: organization.authoritativeRegion.id,
					name: organization.authoritativeRegion.displayName,
					visibility: "FULL"
				});
			}
		}, this);
		
		Ext.each(this.organizations, function(organization) {
			if (organization.authorizedRegions !== undefined) {
				Ext.each(organization.authorizedRegions, function(authorizedRegion) {
					regions.push({
						id: authorizedRegion.geography.id,
						name: authorizedRegion.geography.displayName,
						visibility: authorizedRegion.type
					});
				}, this);
			}
		}, this);	
		
		return regions;
	},
	
	getPreference: function(pref) {
		if (this.preferences !== undefined && this.preferences[pref] !== undefined) {
			return this.preferences[pref];
		}
	},
	
	getDefaultRegion: function() {
		
		var dr = this.getPreference("DEFAULT_REGION");
		
		var ret = this.getVisibleRegions()[0];
		
		if (dr !== undefined) {
			var pref = this.getVisibleRegions().find(function(f){
				f.id == dr;
			});
			if (pref !== undefined) {
				ret = pref;
			}
		} 
		
		return ret;
	}
});
