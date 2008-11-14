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
 * Adds the "setActiveItem" method to AccordionLayout, which is missing
 * from Ext 2.0 for some unknown reason.
 * 
 * http://extjs.com/forum/showthread.php?t=17073
 */
Ext.override(Ext.layout.Accordion, {
	
	setActiveItem : function(item){
        item = this.container.getComponent(item);
        if(this.activeItem != item){
            this.activeItem = item;
        }
    },

	getActiveItem: function() {
		return this.activeItem;
	},
	
	renderItem : function(c){
        if(this.animate === false){
            c.animCollapse = false;
        }
        c.collapsible = true;
        if(this.autoWidth){
            c.autoWidth = true;
        }
        if(this.titleCollapse){
            c.titleCollapse = true;
        }
        if(this.hideCollapseTool){
            c.hideCollapseTool = true;
        }
        if(this.collapseFirst !== undefined){
            c.collapseFirst = this.collapseFirst;
        }
        if(!this.activeItem && !c.collapsed){
            this.activeItem = c;
        }else if(this.activeItem !== c){
            c.collapsed = true;
        }
        Ext.layout.Accordion.superclass.renderItem.apply(this, arguments);
        c.header.addClass('x-accordion-hd');
        c.on('beforeexpand', this.beforeExpand, this);
    }
});

/*
 * Fixes the issue with ColumnLayout not expanding to fill
 * the full height of it's container.
 * 
 * http://extjs.com/forum/showthread.php?t=17130
 */
Ext.override(Ext.layout.ColumnLayout, {
    onLayout : function(ct, target){
        var cs = ct.items.items, len = cs.length, c, i;

        if(!this.innerCt){
            target.addClass('x-column-layout-ct');

            // the innerCt prevents wrapping and shuffling while
            // the container is resizing
            this.innerCt = target.createChild({cls:'x-column-inner'});

            this.renderAll(ct, this.innerCt);

            this.innerCt.createChild({cls:'x-clear'});

        }

        var size = target.getViewSize();

        if(size.width < 1 && size.height < 1){ 
            return;
        }

        var w = size.width - target.getPadding('lr') - this.scrollOffset,
            h = size.height - target.getPadding('tb'),
            pw = w;

        this.innerCt.setWidth(w);
        
        for(i = 0; i < len; i++){
            c = cs[i];
            if(!c.columnWidth){
                pw -= (c.getSize().width + c.getEl().getMargins('lr'));
            }
        }

        pw = pw < 0 ? 0 : pw;
        for(i = 0; i < len; i++){
            c = cs[i];
            if(c.columnWidth){
                c.setWidth(Math.floor(c.columnWidth*pw) - c.getEl().getMargins('lr'));
            }
            if (c.fill) {
	            c.setHeight(size.height);
            }
        }
      
    }
});

/*
 * Adds an "autoListWidth" option to Ext.form.ComboBox.
 * 
 * http://extjs.com/forum/showthread.php?t=17938
 */
Ext.override(Ext.form.ComboBox,{
        onLoad : function(){
        if(!this.hasFocus){
            return;
        }
                
        if(this.store.getCount() > 0){
                    if (this.autoListWidth){
                        if(!this.metrics){
                this.metrics = Ext.util.TextMetrics.createInstance(this.el);
                }
                        this.store.each(function(record){
                    var v = record.get(this.displayField) + " ";
                    var w = Math.min(this.growMax, Math.max(this.metrics.getWidth(v) + /* add extra padding */ 20, this.growMin));
                            if (w > this.innerList.getWidth()) {
                                this.innerList.setWidth(w);
                                this.list.setWidth(w);
                            }
                        },this);
                    }
            this.expand();
            this.restrictHeight();
            if(this.lastQuery == this.allQuery){
                if(this.editable){
                    this.el.dom.select();
                }
                if(!this.selectByValue(this.value, true)){
                    this.select(0, true);
                }
            }else{
                this.selectNext();
                if(this.typeAhead && this.lastKey != Ext.EventObject.BACKSPACE && this.lastKey != Ext.EventObject.DELETE){
                    this.taTask.delay(this.typeAheadDelay);
                }
            }
        }else{
            this.onEmptyResults();
        }
            }
});

/*
 * Clone function
 * http://extjs.com/forum/showthread.php?t=26644
 */
Ext.ux.clone = function(o) {
    if(!o || 'object' !== typeof o) {
        return o;
    }
    var c = 'function' === typeof o.pop ? [] : {};
    var p, v;
    for(p in o) {
        if(o.hasOwnProperty(p)) {
            v = o[p];
            if(v && 'object' === typeof v) {
                c[p] = Ext.ux.clone(v);
            }
            else {
                c[p] = v;
            }
        }
    }
    return c;
};

/*
 * Nicer remote ComboBox.
 * 
 * http://extjs.com/forum/showthread.php?t=36743
 */
Ext.override(Ext.form.ComboBox, {
  onLoad: Ext.form.ComboBox.prototype.onLoad.createInterceptor(function() {
    if (!this.hasFocus && (this.mode == "remote")) {
      var s = this.store, cnt = s.getCount();
      var rv = this._searchStr || this.getRawValue();
      delete this._searchStr;

      if (!cnt) return; // continue with ExtJs default onLoad handler

      var r = null;
      if (cnt == 1) {
        r = s.getAt(0);
      } else if(rv) { // try to find best fitting result
        r = this.findRecord(this.displayField || this.valueField, rv);
      }

      if (r) {
        this.setValue(r.get(this.valueField || this.displayField));
        this.collapse();
        this.fireEvent("select", this, r, s.indexOf(r));
        return false; // don't execute default handler
      }
    }
  }),

  beforeBlur: function() {
    while (this.mode == "remote") {

      var v = this.getValue(), rv = this.getRawValue();
      if (!rv) { // cleared value
        if (v) this.setValue(""); // clear old value
        break;
      }

      var r, s = this.store, cnt = s.getCount();
      if (cnt == 1) {
        r = s.getAt(0);
        if (rv == r.get(this.displayField || this.valueField)) break; // all set
      } else if (cnt) { // more records in store
        r = this.findRecord(this.displayField || this.valueField, rv);
        if (r) { // found record matching query
          if (v != r.get(this.valueField || this.displayField)) this.setValue(v);
          break;
        }
      }

      // this.doQuery(rv); - does expand(), copied 2 lines below from there
      s.baseParams[this.queryParam] = rv;
      this.store.load({params: this.getParams(rv)});
      this._searchStr = rv;
      break;
    }
    Ext.form.ComboBox.superclass.beforeBlur.apply(this, arguments);
  }
}); 

/*
 * Extension of ComboBox to fix width issues when shown in a toolbar.
 * These are really dirty, nasty hacks.
 */
Ext.ux.ToolbarComboBox = Ext.extend(Ext.form.ComboBox, {

	onRender: function(ct, position) {
		Ext.ux.ToolbarComboBox.superclass.onRender.call(this, ct, position);
		
		this.wrap.setWidth = this.wrap.setWidth.createInterceptor(function(width) {
			if (width && width * 1 > 0) {
				return true;
			}
			else {
				return false;
			}
		});
	
	},

	onResize: function(w, h) {
		Ext.ux.ToolbarComboBox.superclass.onResize.call(this, w, h);
		var realWidth = this.trigger.getWidth() == 0 ? (w - 17) : w - this.trigger.getWidth();
		if (typeof w == 'number') {
			this.el.setWidth(this.adjustWidth('input', realWidth));
		}
	}
});

/*
 * Adds a "qtipText" property to all Ext.form.Fields which will
 * show a quicktip on hover.
 * 
 * http://extjs.com/forum/showthread.php?t=11537
 */
/*
Ext.override(Ext.form.Field, {

    findLabel: function(){
    
        var wrapDiv = this.getEl().up('div.x-form-item');
        if (wrapDiv) {
            return wrapDiv.child('label');
        }
    },
    
    afterRender: function(){
        if (this.qtipText) {
            Ext.QuickTips.register({
                target: this.getEl(),
                title: '',
                text: this.qtipText,
                enabled: true
            });
            var label = this.findLabel();
            if (label) {
                Ext.QuickTips.register({
                    target: label,
                    title: '',
                    text: this.qtipText,
                    enabled: true
                });
            }
        }
        Ext.form.Field.superclass.afterRender.call(this);
        this.initEvents();
    }
});
*/