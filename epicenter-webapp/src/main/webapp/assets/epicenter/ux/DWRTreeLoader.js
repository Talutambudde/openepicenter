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
/**
 * @constructor
 * @param {Object} config A config object
 * @cfg dwrCall the DWR function to call when loading the nodes
 * 
 * Thanks: http://extjs.com/forum/showthread.php?t=6217&page=3
 */

Ext.tree.DWRTreeLoader = function(config) {
  Ext.tree.DWRTreeLoader.superclass.constructor.call(this, config);
};

Ext.extend(Ext.tree.DWRTreeLoader, Ext.tree.TreeLoader, {
/**
 * Load an {@link Ext.tree.TreeNode} from the DWR service specified in the constructor.
 * This is called automatically when a node is expanded, but may be used to reload
 * a node (or append new children if the {@link #clearOnLoad} option is false.)
 * @param {Object} node node for which child elements should be retrieved
 * @param {Function} callback function that should be called before executing the DWR call
 */
  load : function(node, callback) {
    var cs, i;
    if (this.clearOnLoad) {
      while (node.firstChild) {
        node.removeChild(node.firstChild);
      }
    }
    if (node.attributes.children && node.attributes.hasChildren) { // preloaded json children
      cs = node.attributes.children;
      for (i = 0,len = cs.length; i<len; i++) {
        node.appendChild(this.createNode(cs[i]));
      }
      if (typeof callback == "function") {
        callback();
      }
    } else if (this.dwrCall) {
      this.requestData(node, callback);
    }
  },
/**
 * Performs the actual load request
 * @param {Object} node node for which child elements should be retrieved
 * @param {Function} callback function that should be called before executing the DWR call
 */
  requestData : function(node, callback) {
    var callParams = [];
    var success, error, params, key;

    if (this.fireEvent("beforeload", this, node, callback) !== false) {

      success = this.handleResponse.createDelegate(this, [node, callback], 1);
      error = this.handleFailure.createDelegate(this, [node, callback], 1);
      params = this.getParams(node);

            // node id is no longer applied as method parameter - see #getParams
      for (key in params) {
        callParams.push(params[key]);
      }

      callParams.push({callback:success, errorHandler:error});

      this.transId = true;
      this.dwrCall.apply(this, callParams);
    } else {
      // if the load is cancelled, make sure we notify
      // the node that we are done
      if (typeof callback == "function") {
        callback();
      }
    }
  },
  
  
/**
 * Creates a new tree node. Node will be an AsyncTreeNode if node has children that might be loaded later
 * @param {Object} attr attributes of this new node
 */
  createNode : function(attr) {
    if (this.baseAttrs) {
      Ext.applyIf(attr, this.baseAttrs);
    }
    if (this.applyLoader !== false) {
      attr.loader = this;
    }
    if (typeof attr.uiProvider == 'string') {
      attr.uiProvider = this.uiProviders[attr.uiProvider] || eval(attr.uiProvider);
    }

    return(attr.leaf ?
           new Ext.tree.TreeNode(attr) :
           new Ext.tree.AsyncTreeNode(attr));
  },

/**
 * Override this to add custom request parameters. Default adds the node id as first and only parameter
 */
  getParams : function(node) {
    return {id:node.id};
  },

/**
 * Handles a sucessful response.
 * @param {Object} response data that was sent back by the server that contains the child nodes
 * @param {Object} node parent node to which child nodes will be appended
 * @param {Function} callback callback that will be performed after appending the nodes
 */
  handleResponse : function(response, node, callback) {
    this.transId = false;
    this.processResponse(response, node, callback);
    this.fireEvent("load", this, node, response);
  },

/**
 * Handles load error
 * @param {Object} response data that was sent back by the server that contains the child nodes
 * @param {Object} node parent node to which child nodes will be appended
 * @param {Function} callback callback that will be performed after appending the nodes
 */
  handleFailure : function(response, node, callback) {
    this.transId = false;
    this.fireEvent("loadexception", this, node, response);
    if (typeof callback == "function") {
      callback(this, node);
    }
    throw "error during tree loading";
  },

/**
 * Process the response that server sent back via DWR.
 * @param {Object} response data that was sent back by the server that contains the child nodes
 * @param {Object} node parent node to which child nodes will be appended
 * @param {Function} callback callback that will be performed after appending the nodes
 */
  processResponse : function(response, node, callback) {
  	
    try {
      for (var i = 0; i<response.length; i++) {
      	
        var nodeData = response[i];
        var n = this.createNode(nodeData);
        
        if (n) {
          node.appendChild(n);
        }
      }
      if (typeof callback == "function") {
        callback(this, node);
      }
    } catch(e) {
      this.handleFailure(response);
    }
  }


});  


