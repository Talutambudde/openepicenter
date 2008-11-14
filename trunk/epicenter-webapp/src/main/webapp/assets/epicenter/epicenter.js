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
(function() {
	
	/*
	 * This is a sequential JavaScript loader.
	 *
	 * While all browsers will load scripts asynchronously, IE and Safari will execute scripts
	 * in the order that the load completes, rather than the order they were referenced (Firefox).
	 * 
	 * (Insert wrist-slashing comment here.)
	 * 
	 * The Maven build of EpiCenter will overwrite this file with a compressed version containing
	 * all the scripts listed here for packaging into the WAR.
	 *
	 */  
	var a = "assets/epicenter/";

	var jsfiles = [ 
		
		"assets/google-maps/wms-gs-hms.js",
		
		a + "ux/DWRProxy.js",
		a + "ux/DWRSubmit.js",
		a + "ux/DWRTreeLoader.js",
		a + "ux/ExtFixes.js",
		a + "ux/NumberFormat.js",
		a + "ux/PasswordField.js",
		a + "ux/DDView.js",
		a + "ux/Multiselect.js",
		a + "ux/RowFitLayout.js",
		a + "ux/StoreSynchronizer.js",
		a + "ux/PostAction.js",
		a + "ux/ContextMenu.js",
		a + "ux/RowExpander.js",
		a + "ux/RowActions.js",
		a + "ux/Toast.js",
		a + "ux/PseudoCombobox.js",
		
		a + "core/Namespace.js",
		a + "core/Userinfo.js",
		a + "core/Common.js",
		
		a + "lib/map/GoogleMapsAdapter.js",
		
		a + "lib/map/MapMessageOverlay.js",
		a + "lib/map/MapContextMenu.js",
		a + "lib/map/MapToolbarControl.js",
				
		a + "lib/MapPanel.js",
		a + "lib/DataTypePanel.js",
		a + "lib/Chart.js",
		a + "lib/Cases.js",
		a + "lib/DurationField.js",
		a + "lib/GeographySelector.js",
		a + "lib/LinkedItemView.js",
		a + "lib/WhenWherePanel.js",
		a + "lib/InvestigationDetails.js",
		a + "lib/AnomalyDetails.js",
		a + "lib/AnalyzerDetails.js",
		a + "lib/ResolutionDetails.js",
		a + "lib/ChartDetails.js",
		a + "lib/PatientHistory.js",
		
		a + "lib/admin/OrganizationEditForm.js",
		a + "lib/admin/UserEditForm.js",
		a + "lib/admin/AuthorizedRegionEditor.js",
		a + "lib/admin/SponsorshipEditor.js",
		
		a + "panel/Summary.js",
		a + "panel/Charts.js",
		a + "panel/Maps.js",
		a + "panel/Forecasting.js",
		a + "panel/Anomalies.js",
		a + "panel/Investigations.js",
		a + "panel/Options.js",
		a + "panel/Admin.js",
		
		a + "core/Viewport.js"
	];

	function include(src, callback) {
		var head = document.getElementsByTagName("head")[0];
		var s = document.createElement("script");
        s.src = src;
        s.type = "text/javascript";
        if (callback) {
    	    s.onload = callback;
    	    s.onreadystatechange = function() {
    	    	if (this.readyState == 'loaded' || this.readyState == 'complete') {
    	    		callback();
    	    	}
    	    };
        }
		head.appendChild(s);
	}
	
	var j = 0;
	
	function finishedLoad() {
		j++;
		if (j < jsfiles.length) {
			include(jsfiles[j], finishedLoad);
		}
	}
	
	include(jsfiles[j], finishedLoad);
    
})();

	
