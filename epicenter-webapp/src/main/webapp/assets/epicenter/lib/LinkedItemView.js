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
EpiCenter.lib.LinkedItemView = function(config) {
	
	var letters = "abcdefghijklmnopqrstuvwxyz0123456789";
	
	this.store = config.store;
	
	this.emptyText = "No matching data was found.";
	
	this.letterCount = 0;
	this.letterGroups = {};
	
	Ext.apply(this, config);
	
	var lastitem;

	var that = this;
	
	this.view = new Ext.DataView({
		autoWidth: true,
		store: this.store,
		tpl: this.tpl,
		singleSelect: true,
		border: false,
		loadingText: "Loading..",
		itemSelector: "div.linked-view-item",
		selectedClass: "linked-view-item-selected",
		overClass: "linked-view-item-over",
		prepareData: function(data) {
			data.dateString = data.timestamp.format("m/d/y");
			if (this.groupName) {
				
				if (data[this.groupName]) {
				
					var item = data[this.groupName];
					if (this.letterGroups[item]) {
						data.iconPath = null;
					} else {
						this.letterGroups[item] = letters.charAt(this.letterCount);
						this.letterCount++;
						data.iconPath = getLetter(this.letterGroups[item]);
					}
					
				}
			} else {
				data.iconPath = getLetter();
				lastitem = data.iconPath;
			}
			return data;
		}.createDelegate(this)
	});
	
	this.pager = new Ext.PagingToolbar({
		store: this.store,
		pageSize: 20,
		items: this.pagerButtons
	});

	var cards = new Ext.Panel({
		region: "center",
		layout: "card",
		autoScroll: true,
		border: false,
		bbar: this.pager,
		activeItem: 0,
		items: [ {
			layout: "fit",
			border: false,
			html: '<div style="text-align: center; padding: 10px; color: #888888"><span>' + this.emptyText + '</span></div>' 
		}, this.view ]
	});
	
	this.store.on("beforeload", function(store) {
		this.letterGroups = {};
		this.letterCount = 0;
	}, this);
	
	this.store.on("load", function(store) {
		if (store.getTotalCount() === 0) {
			cards.getLayout().setActiveItem(0);
		} else {			
			cards.getLayout().setActiveItem(1);
		}
	}, this);
	
	function getLetter(letter) {
		var color = that.markerColor || 'orange-light';
		var defaultMarkerImagePath= "assets/epicenter/images/markers/"+color+"/marker.png";
		var letterMarkerImagePathPrefix= "assets/epicenter/images/markers/"+color+"/marker_";
		
		if (letter) {
			return letterMarkerImagePathPrefix + letter.toLowerCase() + ".png";
		} else {
			return defaultMarkerImagePath;
		}
	}
	
	EpiCenter.lib.LinkedItemView.superclass.constructor.call(this, {
		layout: "border",
		border: false,
		items: [ {
			region: "north",
			layout: "fit",
			border: false, 
			height: 18,
			bodyStyle: "border-top: 1px solid #99bbe8; border-bottom: 1px solid #99bbe8;",
			html: "<div class='linked-view-header'><span>" + this.headerText + "</span></div>"
		}, cards ]
	});
};

Ext.extend(EpiCenter.lib.LinkedItemView, Ext.Panel, {
	
	view: new Ext.DataView({
		autoWidth: true,
		store: this.store,
		tpl: this.tpl,
		singleSelect: true,
		border: false,
		itemSelector: "div.linked-view-item",
		selectedClass: "linked-view-item-selected",
		overClass: "linked-view-item-over",
		prepareData: function(data) {
			if (this.groupName) {
				
				if (data[this.groupName]) {
				
					var item = data[this.groupName];
					if (this.letterGroups[item]) {
					//	letter = this.geolist[geographyName];
					} else {
						this.letterGroups[item] = letters.charAt(this.letterCount);
						this.letterCount++;
						data.iconPath = getLetter(this.letterGroups[item]);
					}
					
				}
			} else {
				data.iconPath = getLetter();
				lastitem = data.iconPath;
			}
			return data;
		}.createDelegate(this)
	}),
	
	refresh: function() {
		this.pager.doLoad(0);
	},
	
	getLetterForIndex: function(indexText) {
		return this.letterGroups[indexText];
	}
	
});
