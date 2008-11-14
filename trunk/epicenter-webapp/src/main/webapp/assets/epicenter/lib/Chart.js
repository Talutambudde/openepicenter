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
EpiCenter.lib.Chart = function(el, config){

	this.el = Ext.get(el);
		
	Ext.apply(this, config);
	
	this.name = "x-chart-" + (this.name ? this.name : Ext.id());
			
	this.chartTemplate = new Ext.Template('<div id="{id}" class="chart">' +
	'<div class="x-box-tl" style="width: {boxwidth};"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>' +
	'<div class="x-box-ml"><div id="{id}-outer" class="x-box-mr"><div class="x-box-mc">' +
	'<div id="{id}-container" style="zoom: 1;">' +
	'<div id="{id}-header" class="chartlabel">{header}</div>' +
	'<div id="{id}-inner" class="chart-inner"></div>' +
	'</div></div></div></div>' +
	'<div class="x-box-bl" style="width: {boxwidth};"><div class="x-box-br"><div class="x-box-bc"></div></div></div>' +
	'</div>');

	EpiCenter.lib.Chart.superclass.constructor.call(this);
	
	this.addEvents({
		render: true,
		beforerender: true,
		click: true
	});
	
	this.rendering = false;
	
	this.addChartNode();

};

Ext.extend(EpiCenter.lib.Chart, Ext.util.Observable, {

	addChartNode: function(){

		var oldEl = $(this.name);
		if (oldEl !== null) {
			while (oldEl.hasChildNodes()) {
				oldEl.removeChild(oldEl.firstChild);
			}
			oldEl.remove();
		}
			
		var temp = this.chartTemplate.append(this.el, {
			id: this.name,
			height: this.height,
			width: this.width,
			header: this.header,
			footer: this.footer,
			boxwidth: this.width + 20 + "px"
		});
		
		var inner = Ext.get(this.name + "-inner");
		inner.setWidth(this.width);
				
		if (this.closable) {
			var cb = Ext.get(this.name + "-container").createChild({cls: "chart-closable"});
			cb.on("click", function() {
				Ext.get(this.name).fadeOut({remove: true});
			}, this);
		}
		
		this.titleHeight = 0;
		
		if (this.title) {
				
			var hel = Ext.get(this.name + "-header");

			var t = Ext.DomHelper.overwrite(hel, {
				tag: "span",
				html: this.title
			}, true);
					
			if (this.closable) {
				hel.setWidth(this.width-10);
				t.applyStyles("margin-right: 12px;");
			} else {
				hel.setWidth(this.width);
			}
			
			this.titleHeight = hel.getHeight();
		}
		
		inner.setHeight(this.height - this.titleHeight + 5);
		
		Ext.get(this.name + "-container").mask("Loading Chart..", "x-mask-loading");
	},
	
	getChartEl: function() {
		return Ext.get(this.name);
	},
	
	displayInfo: function(msg) {
		var container = Ext.get(this.name + "-container");
		var inner = Ext.get(this.name + "-inner");
		Ext.DomHelper.overwrite(inner, "<div class='chart-info'><p>" + msg + "<p></div>");
		container.unmask();
	},

	wrapDwrCallback: function(callback) {
		var scope = this;
		return {
			callback: callback,
			errorHandler: function(error, exception) {
				scope.displayInfo(error);
				EpiCenter.common.globalDwrErrorHandler(error, exception);
			},
			textHtmlHandler: function() {
				scope.displayInfo("Session timeout");
				EpiCenter.common.globalDwrTextHtmlHandler();
			}
		};
	},
	
	resize: function(width, height) {
	
		if (this.url && this.rendered && height != this.height && width != this.width) {
			
			this.height = height;
			this.width = width;
			
			this.load(this.url);
		}	
	},
	
	load: function(url) {
				
		if (url) {
			
			this.fireEvent("beforerender", this);
			
			if (!this.rendering) {
				
				this.rendering = true;
				
				this.url = url;
				
				if (this.rendered) {
					this.addChartNode();
				}
				this.renderered = false;
				
				var elm = Ext.get(this.name + "-inner");
				
				if (this.img === undefined) {
				
					this.img = document.createElement("img");
					
					this.img.onload = function(){
					
						if (this.clickable) {
						
							elm.on("click", function(){
								this.fireEvent("click");
							}, this);
							elm.applyStyles("cursor: pointer;");
						}
						
						if (this.resizable) {
						
							var wrap = new Ext.Resizable(this.name, {
								minWidth: 100,
								minHeight: 75,
								draggable: true,
								transparent: true,
								constrainTo: this.constrainTo ? this.constrainTo : null
							});
							wrap.on("resize", function(wrap, width, height){
								this.resize(width, height);
							}, this);
						}
						
						if (this.qtip) {
						
							Ext.QuickTips.register({
								text: this.qtip,
								target: this.name,
								trackMouse: true
							});
						}
						
						Ext.get(this.name + "-container").unmask();
						this.rendered = true;
						this.rendering = false;
						this.fireEvent("render", this);
						
					}.createDelegate(this);
					
					elm.appendChild(this.img);
				}
				
				this.img.src = url + "&" + Ext.urlEncode({
					width: Math.max(1, this.width),
					height: Math.max(1, (this.height - this.titleHeight))
				});

			}
		}

	},
	
	getTitle: function() {
		return this.title;
	}
});
