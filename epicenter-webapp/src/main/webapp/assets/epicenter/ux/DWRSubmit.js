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
Ext.form.Action.DWRSubmit = function(form, options) {
	Ext.form.Action.Submit.superclass.constructor.call(this, form, options);
};

Ext.extend(Ext.form.Action.DWRSubmit, Ext.form.Action, {
    type : 'DWRSubmit',

	// Process the DWR return value
    dwrCallback: function(response){
        if (typeof this.callback == "function") {
            this.callback(response);
        } 
    },
    
    run : function() {
    	
        var o = this.options;
        this.callback = o.callback;
        var callParams = [];
        if (o.params) {
        	if (o.params instanceof Array) {
	    		callParams = o.params.slice();
    		} else {
    			callParams.push(o.params);
    		}
        }
        callParams.push(this.dwrCallback.createDelegate(this, [this.callback], true));
        
        o.dwrMethod.apply(this, callParams);
    }

});

Ext.form.Action.ACTION_TYPES.DWRSubmit= Ext.form.Action.DWRSubmit;