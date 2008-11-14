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
EpiCenter.lib.map.MapMessageOverlay = function(mapContainerId) {

	var mmoOpacity = 0.7;
	var legendShiftAmount = 78;

	var loadingElement;
	var legendElement;
	var legendWindowElement;

	var maximizedPositioning;
	var minimizedPositioning;
	
	var rendered = false;
	
	var legendTemplate = new Ext.Template(
		'<div class="legend-window">',
		'  <div class="legend map-legend" style="display:none">',
		'    <div class="legend-padding">',
		'      <p>Percentile</p>',
		'      <dl>',
		'      </dl>',
		'    </div>',
		'  </div>',
		'</div>');

	var legendRowTemplate = new Ext.Template(
		'      <dt>',
		'        <div class="map-legend-box" style="background-color: {color}"></div>',
		'      </dt>',
		'      <dd>',
		'        {label}',
		'      </dd>');

	var labeledColors = new Hash({
		'#a63603': '(99% , 100%]',
		'#e6550d': '(95% , 99%]',
		'#fd8d3c': '(87% , 95%]',
		'#fdd0a2': '(68% , 87%]',
		'#feedde': '(0% , 68%]'
	});

	this.init = function(mapPanel) {
				
		mapPanel.on("map_loading", function() {
			
			if (!rendered) {
				
				console.log("initializing overlay");
												
				var container = mapPanel.mapEl.dom;
				var parent = container.parentNode;
				
				var wrapper = document.createElement('div');
				wrapper.style.cssText = container.style.cssText;
				parent.insertBefore(wrapper, container);
				parent.removeChild(container);
				wrapper.appendChild(container);
				container.className = 'map-message-overlay-container';
				
				loadingElement = Ext.DomHelper.append(wrapper, {
					tag: 'div',
					cls: 'map-loading',
					style: 'display: none;',
					html: 'Loading...'
				}, true);
				
				
				legendWindowElement = legendTemplate.append(wrapper, null, true);
				// will need legendElement later...
				legendElement = legendWindowElement.select('div.map-legend').first();
				var tableElement = legendElement.select('dl').first();
				
				labeledColors.each(function(pair){
					legendRowTemplate.append(tableElement, {
						color: pair.key,
						label: pair.value
					}, true);
				});

				rendered = true;
			}
			
			this.showLoadingMessage();	
				
		}, this, { single: true });
		
		mapPanel.on("overlay_loaded", function() { 
			this.initializeLegend(); 
		}, this, { single: true });

		mapPanel.on("map_loaded", function() {
			console.log("map_loaded");
			this.hideLoadingMessage();
		}, this);
			
	};
	
	var legendMinimized = false;
	var movingLegend = false;
	var mouseHover = false;


	function minimizeLegend() {
		mouseHover = false;
		if (!(legendMinimized || movingLegend)) {
			moveLegend('right', function() {
				legendMinimized = true;
				if (mouseHover) {
					maximizeLegend();
				}
			});
			//legendElement.setPositioning(minimizedPositioning);
		}
	}

	function maximizeLegend() {
		mouseHover = true;
		if (legendMinimized && !movingLegend) {
			moveLegend('left', function() {
				legendMinimized = false;
				if (!mouseHover) {
					minimizeLegend();
				}
			});
			//legendElement.setPositioning(maximizedPositioning);
		}
	}

	function moveLegend(direction, callback) {
		movingLegend = true;

		function inner_callback() {
			movingLegend = false;
			callback();
		}
		legendElement.move(direction, legendShiftAmount, { callback: inner_callback });
	}

	this.initializeLegend = function() {
		//maximizedPositioning = legendElement.getPositioning();
		legendWindowElement.move('up', 190, false);
		legendWindowElement.move('left', 1, false);

		legendElement.show(false);
		setTimeout(function() {
			legendElement.hover(maximizeLegend, minimizeLegend);
			minimizeLegend();
		}, 1000);
	};

	this.hideLoadingMessage = function() {
		loadingElement.fadeOut({
			remove: false
		});
	};

	this.showLoadingMessage = function() {
		loadingElement.fadeIn({ endOpacity: mmoOpacity });
	};
};
