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
Ext.ux.StoreSynchronizer = function() {

	return {
		
		sync: function(stores, callback, scope, loadOnly) {
		
			var syncStores = [].concat(stores);
			var loadedCount = 0;

			function countOneAsSynced() {
				loadedCount++;
				if (loadedCount == syncStores.length) {
					if (scope) {
						callback.createDelegate(scope).call();
					} else {
						callback.call();
					}
				}
			}
			
			Ext.each(stores, function(store) {
				if (store === null) {
					throw "Store was null";
				} else 	if (loadOnly || store.loading || store.lastOptions === null) {
					store.on("load", countOneAsSynced, this, { single: true, delay: 10 });
				} else {
					countOneAsSynced();
				}
			}, this);
		}
	};
	
}();

