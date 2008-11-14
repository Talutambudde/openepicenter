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
Ext.ux.PostAction = function() {
	
	return {
		
		post: function(action, config) {
			
			EpiCenter.common.setCatchUnload(false);

			/*
			 * Create a form in the DOM that we can post to.  This is the only way
			 * to initiate a file transfer in JavaScript.
			 * 
			 */
			var form = Ext.DomHelper.append(document.body, {
				tag: "form",
				method: "POST",
				action: action
			}, true);
		
			Ext.each(Object.keys(config), function(item){
				if (typeof(config[item]) != "undefined" && config[item] !== null) {
					
					var value;
					if (config[item] instanceof Date) {
						value = config[item].getTime();
					} else if (config[item] instanceof Object) {
						value = Ext.urlEncode(config[item]);
					} else {
						value = config[item];
					}
					
					console.log("item: " + item + "  value: ", value);
					
					Ext.DomHelper.append(form, {
						tag: "input",
						type: "hidden",
						name: item,
						value: value
					});
				}
			}, this);
		
			form.dom.submit();
			form.remove();
		
			EpiCenter.common.setCatchUnload(true);
			
		}
	};
}();
