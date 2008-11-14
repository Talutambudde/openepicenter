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
Ext.ux.ContextMenu = function(config) {
    Ext.ux.ContextMenu.superclass.constructor.call(this,config);

	this.closingDelay = 300;

	function onMouseMove(e) {
		var lastXY = e.getXY();
		menuXY = this.getEl().getXY();

		if ( lastXY
			&& (lastXY[0] < menuXY[0] || lastXY[1] < menuXY[1]
				|| lastXY[0] - menuXY[0] > this.getEl().getWidth()
				|| lastXY[1] - menuXY[1] > this.getEl().getHeight()
				) ) {
			this.hide();
		}
	}

	this.on('show', function() {
		Ext.getBody().on('mousemove', onMouseMove, this, { buffer: this.closingDelay });
	});

	this.on('hide', function() {
		Ext.getBody().un('mousemove', onMouseMove, this);
	});
};

Ext.extend(Ext.ux.ContextMenu, Ext.menu.Menu, {
	// lets override this method in order to set constrain to true
	createEl: function() {
		return new Ext.Layer({
			cls: "x-menu",
			shadow:this.shadow,
			constrain: true,
			parentEl: this.parentEl || document.body,
			zindex:15000
		});
	},

	display: function() {
		// subtracting 2 in order for mouse to be over the menu when it appears, that is
		// required to hide menu in the event user moves mouse away from it
		this.anchorPosition = [this.opts.pageX - 2, this.opts.pageY - 2];
		this.showAt(this.anchorPosition);
	},
	
	disableItems: function(items) {
		Ext.each(items, function(item) { item.disable(); });
	},
	
	enableItems: function(items) {
		Ext.each(items, function(item) { item.enable(); });
	},

	adjustForConstraints: function() {
		// forcing ext to refit the menu into constrained region
		var newXY = this.getEl().adjustForConstraints(this.anchorPosition);
		this.getEl().setXY(newXY);
	}

});

