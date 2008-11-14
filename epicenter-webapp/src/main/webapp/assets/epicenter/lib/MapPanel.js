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
EpiCenter.lib.MapPanel = Ext.extend(Ext.Panel, {
	
	layout: "fit",
	
	initComponent: function() {
			
		this.earlyMarkers = [];
		
		this.addEvents({
			map_loading: true,
			map_loaded: true,
			map_updated: true,
			overlay_loaded: true,
			overlay_loading: true
		});
		
		this.tbar = new EpiCenter.lib.map.MapToolbarControl(this);
		
		// Initialize plugins here because we don't want them in the prototype
		this.plugins = [
			new EpiCenter.lib.map.MapMessageOverlay(),
			new EpiCenter.lib.map.MapContextMenu()
		];
				
		EpiCenter.lib.MapPanel.superclass.initComponent.call(this);
				
	},
	
	onResize: function(panel, h, w) {
		
		if (this.mapAdapter) {
			this.mapAdapter.preSizeChange();
						
			// timeout required in order to allow IE to recalculate width, without it map will see same
			// old width and new height; of course, FF does not need this kludge
			setTimeout(function(){
				if (this.mapAdapter) {
										
					this.mapContainerEl.setHeight(h - 27);
					this.mapAdapter.registerSizeChange();
				}
			}.createDelegate(this), 50);
		}
	},

	addMarker: function(locationPoint, options) {
		if (this.mapAdapter) {
			this.mapAdapter.addMarker(locationPoint, options);
		} else {
			this.earlyMarkers.push([locationPoint, options]);
		}

	},

	clearMarkers: function() {
		if (this.mapAdapter) {
			this.mapAdapter.clearMarkers();
		}
		this.earlyMarkers.length = 0;
	},

	updateMap: function(parameters, caption) {
		if (this.mapAdapter) {
			this.mapAdapter.update(parameters, caption);
			this.fireEvent("map_updated", this, parameters, this.mapAdapter.getFeature());
		}
	},

	changeMapExtents: function(extents) {
		if (this.mapAdapter) {
			this.mapAdapter.changeExtents(extents); 
		} else {
			this.earlyExtents = extents;
		}
	},

	getParameters: function() {
		if (this.mapAdapter) {
			return this.mapAdapter.getAcceptedParameters();
		}
	},
	
	hideMap: function() {
		this.mapEl.hide();
	},

	showMap: function() {
		this.mapEl.show();
	},

	changeMapExtents: function(extents) {
		this.mapAdapter.changeExtents(extents); 
	},

	onRender: function(ct, position) {
					
		var mapContainerId = Ext.id();
						
		this.mapContainerEl = Ext.DomHelper.append(Ext.getBody(), {
			tag: 'div', cls: 'map-main', children: [
				{ tag: 'div', id: mapContainerId, cls: 'map-container' }
			]
		}, true);
		
		this.mapEl = Ext.get(mapContainerId);
				
		this.mapEl.setVisibilityMode(Element.VISIBILITY);
		this.mapEl.hide();
				
		this.mapContainerEl.setStyle("width", "100%");
		this.mapContainerEl.setStyle("height", "100%");
		
		this.mapEl.setStyle("width", "100%");
		this.mapEl.setStyle("height", "100%");
		
		this.contentEl = this.mapContainerEl;
		
		EpiCenter.lib.MapPanel.superclass.onRender.call(this, ct, position);
			
		this.fireEvent("map_loading", this);
		
		this.mapExtents = this.mapExtents || EpiCenter.common.userinfo.visibleRegionEnvelope;
		
		this.mapAdapter = new EpiCenter.lib.map.GoogleMapsAdapter(this.mapEl.id, this);
						
		this.mapAdapter.on("map_loaded", function() {
			
			// Setup any "early" data
			Ext.each(this.earlyMarkers, function(ma) {
				this.addMarker(ma[0], ma[1]);
			}, this);
			this.earlyMarkers.length = 0;

			if (this.earlyExtents) {
				this.mapAdapter.changeExtents(this.earlyExtents);
				this.earlyExtents = null;
			}
			
 			this.showMap();
			this.fireEvent("map_loaded", this);
			
		}, this, { single: true });
		
		this.mapAdapter.on("overlay_loaded", function() {
			console.log("overlays loaded.");
			this.fireEvent("overlay_loaded", this);
		}, this);
		
		this.mapAdapter.on("overlay_loading", function() {
		 	console.log("loading overlays..");
			this.fireEvent("overlay_loading", this);
		 }, this);

				 
		this.mapAdapter.renderMap();
				
	}

});

