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
/*
 * DWR integration for Ext.data.Store.
 *
 * http://extjs.com/forum/showthread.php?t=5586
 */

Ext.data.DWRProxy = function(dwrCall, args, paging) {
	Ext.data.DWRProxy.superclass.constructor.call(this);
	this.dwrCall = dwrCall;
	this.args = args;
	this.paging = (paging === undefined ? false : paging);
};

Ext.extend(Ext.data.DWRProxy, Ext.data.DataProxy, {
	
	dwrCallback : function(data, reader, callback, scope, arg) {
		
		var result;
		try {
			result = reader.readRecords(data);
			this.fireEvent("load", this, arg, result);
		} catch(e) {
			this.fireEvent("loadexception", this, arg, null, e);
			callback.call(scope, null, arg, false);
			scope.loading = false;
			return;
		}
		callback.call(scope, result, arg, true);
		scope.loading = false;
	},

	load : function(params, reader, callback, scope, arg) {
		
		if (this.fireEvent("beforeload", this, params) !== false) {
			
			var delegate = this.dwrCallback.createDelegate(this, [reader, callback, scope, arg], 1);
		
			var callParams = [];
				
			if (arg.arg) {
				callParams = arg.arg.slice();
			} else	if (this.args) {
				if (this.args instanceof Array) {
					callParams = this.args.slice();
				} else {
					callParams.push(this.args);
				}
			}

			if (params.query) {
				callParams.push(params.query);
			}
			
			if (this.paging) {
				callParams.push(params.start);
				callParams.push(params.limit);
			}
		
			callParams.push(delegate);
			
			this.dwrCall.apply(this, callParams);

			scope.loading = true;
			
		} else {
			callback.call(scope || this, null, arg, false);
		}
	}

});


/*
 * An Ext.data.DataReader that can parse objects, with some extra features
 * like "attributes" (which is a map of metadata about the records returned).
 */
Ext.data.ObjectReader = Ext.extend(Ext.data.JsonReader, {
	readRecords : function(o) {
		var ret = Ext.data.ObjectReader.superclass.readRecords.call(this, o);
		if (o.attributes !== undefined) {
			ret.attributes = o.attributes;
		}
		return ret;
	}
});

/*
 * Override the loadRecords prototype to process attributes.
 */
(function() {
	var originalMethod = Ext.data.Store.prototype.loadRecords;

	Ext.override(Ext.data.Store, {
		loadRecords : function(o, options, success){ 
			this.attributes = o.attributes;
			originalMethod.apply(this, [o, options, success]);
		}
	});
})();
