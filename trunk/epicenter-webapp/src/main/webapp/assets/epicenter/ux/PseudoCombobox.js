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
Ext.ux.PseudoCombobox = function(config, params) {
	
	var scope = this;

	this.store = params.store;
	var templateString = new Ext.XTemplate("<tpl for='.'>", params.templateString, "</tpl>");
	var callback = params.callback;
	var callbackScope = params.scope;
	
    var dropdownLayer = new Ext.Layer({
		constrain: true,
		shadow: true
	});

	dropdownLayer.setWidth(500);

	dropdownLayer.swallowEvent('mousewheel');

	var panelEl = dropdownLayer.createChild({});

    var rowTemplate = new Ext.XTemplate(
		'<div class="x-menu">',
			'<ul class="x-menu-list">',
				'<tpl for=".">',
					'<li class="x-menu-list-item"><a class="x-menu-item" style="margin-left: 26px; padding-top: 0px;">{inner}</a></li>',
				'</tpl>',
			'</ul>',
        '</div>');

    var dataView = new Ext.DataView({
		autoHeight: true,
		loadingText: "Loading..",
		style: 'overflow: hidden',
		border: false,
		tpl: rowTemplate,
		store: this.store,
		overClass: 'x-menu-item-active',
		itemSelector: '.x-menu-list-item',
		prepareData: function(data) {
			data.inner = Ext.util.Format.ellipsis(templateString.applyTemplate(data), 85);
			return data;
		}
	});

	dataView.on('click', function(view, index) {
		var rowId = this.store.getAt(index).get("id");
		callback.call(callbackScope, rowId);
		innerHide();
	}, this);

    var pager = new Ext.PagingToolbar({
		store: this.store,
		pageSize: 10,
		displayInfo: true,
		displayMsg: 'Items {0} - {1} of {2}',
		emptyMsg: "No items to display"
	});

	this.display = function(alignmentTarget) {
		if (this.store.getCount() > 0) {
			dropdownLayer.alignTo(alignmentTarget, 'tl-bl?');
			dropdownLayer.show();
			freezeHeight();
			Ext.getDoc().on('mousedown', onMouseDown);
		}
	};

	function innerHide() {
		Ext.getDoc().un('mousedown', onMouseDown);
		dropdownLayer.hide();
	}

	function freezeHeight() {
		scope.setHeight(scope.getSize().height);
	}

	this.refresh = function() {
		pager.doLoad(0);
	};

    Ext.ux.PseudoCombobox.superclass.constructor.call(this, {
		applyTo: panelEl,
        height: 'auto',
        autoScroll:false,
        items: dataView,
        bbar: pager
    });

   function onMouseDown(e){
       if(!e.within(dropdownLayer)){
           innerHide();
       }
   }
};

Ext.extend(Ext.ux.PseudoCombobox, Ext.Panel, {
});

