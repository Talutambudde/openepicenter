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
EpiCenter.lib.map.GoogleMapsAdapter = function(domId, options) {
	
	var that = this;

	this.addEvents({
		map_loaded: true,
		overlay_loaded: true,
		overlay_loading: true,
		context_menu: true
	});

	Event.observe(window, 'unload', GUnload);

	var MAX_ZOOM_LEVEL = 14;
	var MIN_ZOOM_LEVEL = 3;
	var MIN_STATE_ZOOM_LEVEL = MIN_ZOOM_LEVEL;
	var MIN_COUNTY_ZOOM_LEVEL = 7;
	var MIN_ZIPCODE_ZOOM_LEVEL = 9;
	var map;
	var mapDiv = $(domId);
	var wmsOverlay;
	var wmsLayer;
	var scope = this;
	var acceptedParameters = {};
	var captionControl;
	
	var imageOverlay;
	
	var mapExtentsOption = options.mapExtents;
	var mapModeOption = options.mapMode || 'large';
	var featureControlEnabled = options.featureControlEnabled || false;
	var mapCaption = options.mapCaption || "";

	var onSizeChangeCenter;
	
	var featureListOverlay;
	
	var mapConfig = {
		getBaseWmsUrl: function() {
			return EpiCenter.GEOSERVER_HOST + '/wms?';
		},

		getBaseSldUrl: function() {
			var baseSldUrl =  window.location.href.gsub('/[^/]*$', '/map-style');
			if (EpiCenter.GEOSERVER_SLD_OVERRIDE) {
			//	baseSldUrl = baseSldUrl.gsub('(http(?:s)?://)[^:/]*', '#{1}' + EpiCenter.GEOSERVER_SLD_OVERRIDE);
				baseSldUrl = EpiCenter.GEOSERVER_SLD_OVERRIDE + "/map-style";
			}
			return baseSldUrl;
		}
	};


	this.renderMap = function(parameters) {
		
		Object.extend(acceptedParameters, parameters);
		if (GBrowserIsCompatible()) {
			
			map = new GMap2(mapDiv);

			setupMap();
			addOverlay();

			restrictRangeOfZoom();
			setTimeout(function() { that.fireEvent("map_loaded"); }, 10);
			//new Temp().render();
		} else {
			alert("Sorry but your browser is not compatible with google maps");
		}
	};

	this.getAcceptedParameters = function() {
		return Ext.apply({}, acceptedParameters);
	};

	this.getFeature = function() {
		return this.getAcceptedParameters().feature;
	};
	
	this.preSizeChange = function() {
		onSizeChangeCenter = map.getCenter();
	};

	this.registerSizeChange = function() {
		if (map) {
			map.checkResize();
			if (onSizeChangeCenter) {
				map.setCenter(onSizeChangeCenter);
				onSizeChangeCenter = null;
			}
		}
	};
	
	this.addImageOverlay = function(minX, minY, maxX, maxY, url) {
				
		if (imageOverlay) {
			imageOverlay.hide();
			map.removeOverlay(imageOverlay);
		}
		
		var ne = new GLatLng(maxY, maxX);
		var sw = new GLatLng(minY, minX);
		var bounds = new GLatLngBounds(sw, ne);
		
		console.log("adding overlay: ", url, bounds);
		
		imageOverlay = new GGroundOverlay(url, bounds);
		map.addOverlay(imageOverlay);
		imageOverlay.show();
		
		return imageOverlay; 
	};
	
	this.addFeatureListOverlay = function(layers, featureIds) {
		var wmsLayer = new GWMSTileLayer(map, new GCopyrightCollection(""));

		wmsLayer.baseUrl = mapConfig.getBaseWmsUrl();
		wmsLayer.opacity = 1;
		wmsLayer.layers = (layers instanceof Array) ? layers.join(",") : layers;
		wmsLayer.featureid = (featureIds instanceof Array) ? featureIds.join(",") : featureIds; 
		wmsLayer.styles = "green";
		
		this.removeFeatureListOverlay();
		
		featureListOverlay = new GTileLayerOverlay(wmsLayer);			
		map.addOverlay(featureListOverlay);
	};
	
	this.removeFeatureListOverlay = function() {
	
		if (featureListOverlay) {
			map.removeOverlay(featureListOverlay);
			featureListOverlay = null;
		}	
	};
	
	this.getBounds = function() {
		if (map) {
			var bounds = map.getBounds();
			console.log("Bounds: ", bounds);
			console.log("northeast: ", bounds.getNorthEast());
			console.log("southwest: ", bounds.getSouthWest());
			return {
				minX: bounds.getSouthWest().lng(),
				minY: bounds.getSouthWest().lat(),
				maxX: bounds.getNorthEast().lng(),
				maxY: bounds.getNorthEast().lat()
			};
		}
	};
	
	this.update = function(parameters, caption) {
		Object.extend(acceptedParameters, parameters);
		if (parameters) {
			setZoomInLimits();
		}
		
		if (imageOverlay) {
			imageOverlay.hide();
			map.removeOverlay(imageOverlay);
		}
		
		var signalLoaded = false;
		if (parameters && !wmsOverlay) {
			addOverlay();
			signalLoaded = true;
		} else if (!parameters && wmsOverlay) {
			destroyOverlay();
		} else if (wmsOverlay) {
			wmsOverlay.refresh();
			signalLoaded = true;
		}
		
		if (caption) {
			map.removeControl(captionControl);
			captionControl = new CaptionControl(caption);
			map.addControl(captionControl);
		}

		if (signalLoaded) {
			that.fireEvent("overlay_loading");
			setTimeout(function() {
					waitForOverlaysToLoad(function() {
						that.fireEvent("overlay_loaded");
					});
				}, 400);
		}
	};

	this.labelFeatures = function(shouldLabelFeatures) {
		acceptedParameters.labelFeatures = shouldLabelFeatures;
		if (wmsOverlay !== undefined) {
			wmsOverlay.refresh();
		}
	};
	
	this.changeFeature = function(feature) {
		
		if (imageOverlay) {
			imageOverlay.hide();
			map.removeOverlay(imageOverlay);
		}
		
		acceptedParameters.feature = feature;
		setZoomInLimits();
		if (wmsOverlay !== undefined) {
			wmsOverlay.refresh();
		}
	};

	this.getMapTypes = function() {
		return map.getMapTypes();
	};
	
	this.setMapType = function(type) {
		map.setMapType(type);
	};
	
	this.getCurrentMapType = function() {
		return map.getCurrentMapType();
	};
	
	this.animateMap = function(parameters) {
		Object.extend(acceptedParameters, parameters);
		(new MapAnimation()).run();
	};

	var markerRegistry = [];

	this.addMarker = function(locationPoint, options) {
		options = options || {};
		var markerLatlng = new GLatLng(locationPoint.y, locationPoint.x);
		var marker = createMarker(markerLatlng, { title: options.title, letter: options.letter, colors: options.colors });
		var infoElement = Ext.DomHelper.append(Ext.getBody(), { tag: 'div', cls: 'map-info-popup', html: options.content }, true);

		marker.bindInfoWindow(infoElement.dom);
		if (options.callback) {
			var handle = GEvent.addListener(marker, "click", function() {
				GEvent.removeListener(handle);
				options.callback(infoElement);
			});
		}
		map.addOverlay(marker);
		markerRegistry.push(marker);
	};

	this.clearMarkers = function(locationPoint, title, content) {
		markerRegistry.each(function(marker) {
			map.removeOverlay(marker);
		});
		markerRegistry.length = 0;
	};

	this.changeExtents = function(extents) {
		centerMap(extents);
	};

	function setupMap() {
		cloneMapTypes();
		centerMap();
		if (mapModeOption == 'large') {
			map.addControl(new GLargeMapControl());

		} else if (mapModeOption == 'small') {
			map.addControl(new GSmallMapControl());
		} 

		captionControl = new CaptionControl(mapCaption);
		map.addControl(captionControl);

		map.enableScrollWheelZoom();
		map.enableContinuousZoom();
		
		console.log("map setup");
		
			
		var kh = new GKeyboardHandler(map);
		GEvent.addListener(map, 'singlerightclick', function(point) {
			if (acceptedParameters.end) {
				var mapContainer = Ext.fly(map.getContainer());
				var latlng = map.fromContainerPixelToLatLng(point);
				var opts =  {
					mapX: point.x,
					mapY: point.y,
					pageX: mapContainer.getLeft() + point.x,
					pageY: mapContainer.getTop() + point.y,
					lat: latlng.lat(),
					lng: latlng.lng(),
					feature: acceptedParameters.feature
				};
				that.fireEvent("context_menu", opts);
			}
		});
	}

	function addOverlay() {
		if (acceptedParameters.end) {
			wmsLayer = createWmsLayer(map);
			wmsOverlay = new GTileLayerOverlay(wmsLayer);
						
			map.addOverlay(wmsOverlay);
		}
	}

	function destroyOverlay() {
		if (wmsOverlay) {
			map.removeOverlay(wmsOverlay);
		}
		wmsLayer = null;
		wmsOverlay = null;
	}

	function cloneMapTypes() {
		// The reason we are doing this strange cloning is that we are overriding functions
		// getMinimumResolution and getMaximumResolution on map type, and if there is more than one
		// map on a page, and they are using standard map types, thoese maps all try to modify same
		// map types. The easiest solution for this problem is to create custom map types that are
		// just clones of standard ones
		map.getMapTypes().length = 0;

		map.addMapType(cloneMapType(G_NORMAL_MAP));
		var physical = cloneMapType(G_PHYSICAL_MAP);
		map.addMapType(physical);

		setTimeout(function() { map.setMapType(physical); }, 50);
	}

	function cloneMapType(mapType) {
		return new GMapType(mapType.getTileLayers(), mapType.getProjection(), mapType.getName(), mapType);
	}
	
	var defaultBB = {
		minY: 25.3241665257384,
		minX: -130.078125,
		maxY: 51.50874245880332,
		maxX: -60.8203125
	};

	function centerMap(extents) {
		var bb = extents || mapExtentsOption || defaultBB;

		var minCorner = new GLatLng(bb.minY, bb.minX);
		var maxCorner = new GLatLng(bb.maxY, bb.maxX);

		var bounds = new GLatLngBounds(minCorner, maxCorner);

		var initZoom = map.getBoundsZoomLevel(bounds);
		initZoom = Math.max(initZoom, getMinZoomForOverlay());
		map.setCenter(bounds.getCenter(), initZoom);
	}

	var layerInstanceCount = 0;

	function createWmsLayer(map) {
		var wmsLayer = new GWMSTileLayer(map, new GCopyrightCollection(""));

		wmsLayer.baseUrl = mapConfig.getBaseWmsUrl();
		wmsLayer.opacity=1;
		addSldGenerator(wmsLayer);

		return wmsLayer;
	}

	function addSldGenerator(layer) {
		var baseSldUrl = mapConfig.getBaseSldUrl();

		layer.sldUrlGenerator = function(bbParams) {
			var params = {};
			Ext.apply(params, bbParams);
			Ext.apply(params, acceptedParameters);
			if (layer.parametersOverride) {
				Ext.apply(params, layer.parametersOverride);
			}
			
			Ext.each(Object.keys(params), function(key) {
				if (params[key] instanceof Object) {
					params[key] = Ext.urlEncode(params[key]);
				}
			}, this);
			
			console.log("map params: ", params);
				
			var sldUrl =  baseSldUrl + ";jsessionid=" + EpiCenter.common.getSessionId() + "?" + Ext.urlEncode(params);

			//debug("sldUrl = ", sldUrl);
			return sldUrl;
		};
	}

	var createMarker = function() {
		var defaultMarkerImagePath = "assets/epicenter/images/markers/COLOR/marker.png";
		var letterMarkerImagePath = "assets/epicenter/images/markers/COLOR/marker_LETTER.png";
		var defaultMarkerColors = ['orange-light', 'orange-dark'];
		var defaultMarkerIcon = new GIcon(G_DEFAULT_ICON);
		defaultMarkerIcon.image = defaultMarkerImagePath;

		return function(latlng, opts) {
			var markerPath = defaultMarkerImagePath;
			if (opts.letter) {
				markerPath= letterMarkerImagePath.gsub('LETTER', opts.letter.toLowerCase());
			}

			var colors = opts.colors || defaultMarkerColors;

			markerIcon = new GIcon(G_DEFAULT_ICON);
			markerIcon.image = markerPath.gsub('COLOR', colors[0]);

			var markerOptions = { icon: markerIcon };
			markerOptions.title = opts.title;
			var marker = new GMarker(latlng, markerOptions);

			// Switch icon on marker mouseover and mouseout
			GEvent.addListener(marker, "mouseover", function() {
				marker.setImage(marker.getIcon().image.gsub(colors[0], colors[1]));
			});
			GEvent.addListener(marker, "mouseout", function() {
				marker.setImage(marker.getIcon().image.gsub(colors[1], colors[0]));
			});

			return marker;
		};
	}();

	function restrictRangeOfZoom() {
		map.getMapTypes().each(function(mt) {
			mt.getMinimumResolution = getMinZoomForOverlay;
			mt.getMaximumResolution = function() { return MAX_ZOOM_LEVEL; };
		});
	}

	function setZoomInLimits() {
		if (map.getZoom() < getMinZoomForOverlay()) {
			map.setZoom(getMinZoomForOverlay());
		}
	}

	function waitForOverlaysToLoad(callback){
		var el = map.getPane(1);

		var sleepDelay = 200;
		var images = el.getElementsByTagName("img");

		if (images.length === 0) {
			//debug("waiting for the images to load 1");
			setTimeout(function() { waitForOverlaysToLoad(callback); }, sleepDelay);
			return;
		}

		var count = 0;

		for (var i=0;i<images.length;i++){
			var image = images[i];
			//debug("image loaded=" + image.loaded + " complete=" + image.complete + " src=" + image.src.truncate(132));
			if (!image.loaded) {
				//debug("waiting for the images to load 2");
				setTimeout(function() { waitForOverlaysToLoad(callback); }, sleepDelay);
				return;
			}
			count++;
		}

		//debug("all images finished loading");

		callback();
	}

	function MapAnimation() {
		var overlays = [];
		var interval = 2000;
		var numberOfOverlays = 7;

		this.run = function() {
			map.removeOverlay(wmsOverlay);

			var newCenter = new GLatLng(41.104190944576466, -81.5350341796875);
			map.setCenter(newCenter, 9);

			createOverlays();
			createOverlayList();
			animateOverlays();
		};

		function createOverlays() {
			var lineDate = new Date();
			lineDate.setTime(acceptedParameters.end);

			$R(0, numberOfOverlays).each(function(i) {
				lineDate.setTime(lineDate.getTime() - 24 * 60 * 60 * 1000);
				var text = formatDate(lineDate);

				var localLayer = createWmsLayer(map);
				localLayer.parametersOverride = {};
				localLayer.parametersOverride.end = lineDate.getTime();
				var overlay = new GTileLayerOverlay(localLayer);
				overlay.name = text;
				overlay.getTileLayer().name = text;
				overlays[i] = overlay;
			});
		}

		function createOverlayList() {
			var listElement = $('map-layers-list');
			if (listElement) {
				listElement.update();

				overlays.each(function(overlay) {
					var newEntry = new Insertion.Bottom(listElement, '<li>' + overlay.name + "</li>");
					var li = listElement.childElements().last();

					Event.observe(li, 'mouseover', function(event) {
						debug("adding overlay " + overlay.name);
						map.removeOverlay(wmsOverlay);
						map.addOverlay(overlay);
					});

					Event.observe(li, 'mouseout', function(event) {
						debug("removing overlay " + overlay.name);
						map.removeOverlay(overlay);
						map.addOverlay(wmsOverlay);
					});
				});
			}
		}

		function animateOverlays() {
			animateOverlayFrame(0);
		}

		var lastShowTimeMillis = (new Date()).getTime() - interval;

		function animateOverlayFrame(layerIndex) {
			if (layerIndex >= overlays.length) {
				hideOverlayFrame(overlays.length - 1);
				setTimeout(function() { map.addOverlay(wmsOverlay); }, interval);
				debug("done with animation, returning to normal mode");
				return;
			}

			var overlay = overlays[layerIndex];
			debug("adding overlay " + overlay.name);

			map.addOverlay(overlay);
			overlay.refresh();
			overlay.hide();

			setTimeout(function() {
				waitForOverlaysToLoad(function() {
					var delay = interval - ((new Date()).getTime() - lastShowTimeMillis);
					debug("loaded overlay " + overlay.name + ", have " + delay + " millis to wait before rendering");
					delay = Math.max(delay, 10);
					setTimeout(function() {
						debug("showing overlay " + overlay.name);
						hideOverlayFrame(layerIndex - 1);
						overlay.show();
						lastShowTimeMillis = (new Date()).getTime();
						setTimeout(function() { animateOverlayFrame(layerIndex + 1); }, 100);
					}, delay);
			});}, 100);
		}

		function hideOverlayFrame(layerIndex) {
			if (layerIndex >= 0) {
				var overlay = overlays[layerIndex];
				debug("hiding overlay " + overlay.name);
				overlay.hide();

				setTimeout(function() {
					debug("removing overlay " + overlay.name);
					map.removeOverlay(overlay);
				}, 50);
			}
		}
	}

	function formatDate(d) {
		return Ext.util.Format.date(d, 'm/d/Y');
	}

	function getMinZoomForOverlay() {
		var rtn = MIN_ZOOM_LEVEL;
		if (acceptedParameters.feature) {
			var feature = acceptedParameters.feature.toLowerCase();
			if (feature == 'state') {
				rtn = MIN_STATE_ZOOM_LEVEL;
			} else if (feature == 'county') {
				rtn = MIN_COUNTY_ZOOM_LEVEL;
			} else if (feature == 'zipcode') {
				rtn = MIN_ZIPCODE_ZOOM_LEVEL;
			}
		}
		return rtn;
	}

	function debug() {
		console.log.apply(this, arguments);
	}

	CaptionControl = function(caption){
		this.caption = caption;
	};
	
	Ext.extend(CaptionControl, GControl, {
		
		initialize: function(map) {
			var captionTemplate = new Ext.Template(
				'<div class="map-caption">',
				'  <div>',
				'  <p>',
				'    {caption}',
				'  </p>',
				'  </div>',
				'</div>');
			
			return captionTemplate.append(Ext.get(map.getContainer()), { caption: this.caption }, false);
		},

		// overrides method from GControl, specifies absolute placement of our control
		getDefaultPosition: function() {
			return new GControlPosition(G_ANCHOR_BOTTOM_LEFT, new GSize(0, 12));
		},

		printable: function() {
			return true;
		}
	});
};

Ext.extend(EpiCenter.lib.map.GoogleMapsAdapter, Ext.util.Observable);
