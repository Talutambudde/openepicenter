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
GWMSTileLayer = function(map, copyrights,  minResolution,  maxResolution) {
	
	GWMSTileLayer.superclass.constructor.call(this, copyrights, minResolution, maxResolution);
	
	var savedMap = map;

	this.getSavedMap = function() {
		return savedMap;
	};
		
	// Use PNG by default
	this.format = "image/png";
	
	this.opacity = 1.0;
	
	var WGS84_SEMI_MAJOR_AXIS = 6378137.0;

	var DEG2RAD=0.0174532922519943;
	var PI=3.14159265358979323846;

	this.dd2MercMetersLng = function(p_lng) {
		return WGS84_SEMI_MAJOR_AXIS*(p_lng*DEG2RAD);
	};

	this.dd2MercMetersLat = function(p_lat) {
		if (p_lat >= 85) p_lat=85;
		if (p_lat <= -85) p_lat=-85;
		return WGS84_SEMI_MAJOR_AXIS*Math.log(Math.tan(((p_lat*DEG2RAD)+(PI/2)) /2));
	};
	
};

Ext.extend(GWMSTileLayer, GTileLayer, {

	isPng: function() {
		return this.format == "image/png";
	},

	getOpacity: function() {
		return this.opacity;
	},

	getTileUrl: function(point, zoom) {
		var map = this.getSavedMap();
		var mapType = map.getCurrentMapType();
		var proj = mapType.getProjection();
		var tileSize = mapType.getTileSize();

		var upperLeftPix = new GPoint(point.x * tileSize, (point.y+1) * tileSize);
		var lowerRightPix = new GPoint((point.x+1) * tileSize, point.y * tileSize);

		var upperLeft = proj.fromPixelToLatLng(upperLeftPix, zoom);
		var lowerRight = proj.fromPixelToLatLng(lowerRightPix, zoom);

		var boundBox = this.dd2MercMetersLng(upperLeft.lng()) + "," + 
				this.dd2MercMetersLat(upperLeft.lat()) + "," +
				this.dd2MercMetersLng(lowerRight.lng()) + "," + 
				this.dd2MercMetersLat(lowerRight.lat());

		var lLLx = this.dd2MercMetersLng(0);
		var lLLy = this.dd2MercMetersLat(0);

		var sldParams = {
			bbox: boundBox,
			width: tileSize,
			height: tileSize
		};

		var params = {
			request: "GetMap",
			service: "WMS",
			version: "1.1.1",
			bgcolor: "0xffffff",
			transparent: "true",
			format: this.format,
			srs: "EPSG:900913",
			bbox: boundBox,
			width: tileSize,
			height: tileSize,
			layers: this.layers,
			styles: this.styles,
			featureid: this.featureid,
			reaspect: "false",
			tiled: "true",
			tilesOrigin: lLLx + "," + lLLy
		};

		if (this.sldUrl) {
			params.SLD = this.sldUrl + "?" +
				"min_lng=" + upperLeft.lng() + "&" +
				"min_lat=" + upperLeft.lat() + "&" +
				"max_lng=" + lowerRight.lng() + "&" +
				"max_lat=" + lowerRight.lat();
		} else if (this.sldUrlGenerator) {
			params.SLD = this.sldUrlGenerator({
				min_lng: upperLeft.lng(),
				min_lat: upperLeft.lat(),
				max_lng: lowerRight.lng(),
				max_lat: lowerRight.lat()
			});
		}

		for (var key in params) {
			if (params[key] == null) {
				delete params[key];
			}
		}
		
		return this.baseUrl + Ext.urlEncode(params);

	}

});

