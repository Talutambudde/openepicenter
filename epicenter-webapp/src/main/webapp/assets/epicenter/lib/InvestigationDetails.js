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
EpiCenter.lib.InvestigationDetails = function(config) {
	
	var currentInvestigation;
	
	var currentLetter;
	
	/*
	 * Constants and element locations
	 */
	var letters = "abcedfghijklmnopqrstuvwxyz0123456789";
	
	var chartContainer = Ext.id();
	
	var detailsContainer = Ext.id();
	
	var commentsContainer = Ext.id();
	
	var changeAssignmentId = detailsContainer + "-assignment";
	
	var associateEventsId = detailsContainer + "-associate";
	
	var addCommentsId = detailsContainer + "-comment";
	
	var otherActionsId = detailsContainer + "-actions";
	
	var detailsTemplate = new Ext.XTemplate('<tpl for=".">',
		'<div class="anomaly-details-inner">',
		'<p>This investigation began at {startDateString}',
		'<tpl for="createdBy"> and was initiated by {value}</tpl> ',
		'<tpl for="organization">({name}). </tpl>',
		'Currently, {assignedToString}</p>',
		'<p>A total of {anomalyCount} anomalies have been associated with the investigation.</p> ',
		'<tpl for="state">The current state of this investigation is {name}. </tpl>',
		'The <a id="' + changeAssignmentId + '">investigator</a> can ',
		'<a id="' + associateEventsId + '">associate anomalies</a>, ',
		'<a id="' + addCommentsId + '">add comments</a>, or ',
		'<a id="' + otherActionsId + '">update the state</a> of the investigation.<br/>',
		'</div></tpl>');

	var anomaliesTemplate = new Ext.XTemplate(
		'<tpl for=".">',
		'  <div class="{itemclass}"/>',
		'    <div></div>',
		'    <p>',
		'      {timestampString}: <strong>{category} in {geographyName}</strong> by {algorithmName}.<br>',
		'      The current count of {currentValue} visits {[values.inactive ? "no longer" : ""]} exceeds the detection threshold ({actualThreshold}).',
		'    </p>',
		'  </div>',
		'</tpl>');
	
	var activitiesTemplate = new Ext.XTemplate('<tpl for=".">',
		'<div class="activity-item"><span><span style="font-weight: bold">{timestamp:date("m/d/Y g:i a")}:</span> {log}</span>',
		'</div></tpl>');
	
	var investigationInfoPopupTemplate = new Ext.Template(
		'<p>',
		'  {description} - {investigator} ({org})',
		'</p>');
		
    var recentAnomaliesTemplateString =
		EpiCenter.common.templateSources.anomalyTitle;

		
	// Activity record type
	var activityRecordType = Ext.data.Record.create([{
		name: 'id'
	}, {
		name: 'timestamp'
	}, {
		name: 'name'
	}, {
		name: 'username'
	}, {
		name: 'email'
	}, {
		name: 'log'
	}]);
	
	
	var activitiesDataStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(WorkflowService.getInvestigationActivities),
		reader: new Ext.data.ObjectReader({
			root: "items",
			totalProperty: "totalItems"
		}, activityRecordType)	
	});
	
	var activitiesView = new Ext.DataView({
		tpl: activitiesTemplate,
		autoHeight: true,
		overClass: 'activity-item-over',
		itemSelector: 'div.activity-item',
		store: activitiesDataStore
	});
	
	var detailsPanel = new Ext.Panel({
		fill: true,
		columnWidth: 0.5,
		autoScroll: true,
		html: '<div id="' + detailsContainer + '" class="anomaly-details"></div>',
		title: " "
	});
	
	var detailsMap = new EpiCenter.lib.MapPanel({
		mapMode: 'small',
		visible: false,
		mapCaption: "Anomalies Associated with the Investigation",
		frame: false,
		border: false
	});

	var anomaliesDataStore = new Ext.data.Store({
		reader: new Ext.data.ObjectReader({}, EpiCenter.common.eventRecordType)
	});
	
	var anomaliesView = new Ext.DataView({
		tpl: anomaliesTemplate,
		autoHeight: true,
		overClass: 'anomaly-item-over',
		itemSelector: 'div.anomaly-item',
		store: anomaliesDataStore,
		emptyText: '<p class="no-data-message">No events are assigned to this investigation.</p>',
		prepareData: function(data) {
			data.timestampString = data.timestamp.format("m/d/Y");
			data.currentValue = currentInvestigation.currentValues[data.id];
			data.inactive = data.currentValue < data.actualThreshold;
			
			var cls = [ "anomaly-item "];
			
			if (data.geography.visibility === "FULL" || data.geography.visibility === "AGGREGATE_ONLY") {
				cls.push("anomaly-item-selectable");
			} else {
				cls.push("anomaly-item-unselectable");
			}
			
			if (data.inactive) {
				cls.push("anomaly-item-inactive");
			}
			
			data.itemclass = cls.join(" ");
			
			return data;
		}
	});
	
	var commentEditor = new Ext.form.HtmlEditor({
		hideLabel: true,
		width: 538,
		height: 185
	});
	
	var commentEditorWindow = new Ext.Window({
		title: "Add Comment",
		layout: "form",
		width: 550,
		height: 250,
		modal: true,
		border: false,
		closeAction: "hide",
		defaults: {
			bodyStyle: "padding: 15px;"
		},
		constrain: true,
		items: [ commentEditor ],
		buttons: [ {
			text: "Add Comment",
			handler: function() {
				if (commentEditor.getValue()) {
					WorkflowService.addCommentToInvestigation(currentInvestigation.id, commentEditor.getValue(), function() {
						commentEditorWindow.hide();
						activitiesDataStore.load({ arg: [ currentInvestigation.id ] });
					});
				}
			}
		}, {
			text: "Cancel",
			handler: function() {
				commentEditorWindow.hide();
			}
		} ]
	});

	var recentAnomaliesStore = new Ext.data.Store({
		proxy: new Ext.data.DWRProxy(EventService.getRecentEvents, null, true),
		reader: new Ext.data.ObjectReader({
			root: "items",
			totalProperty: "totalItems"
		}, EpiCenter.common.eventRecordType)
	});
	
	var dropdownPanel = new Ext.ux.PseudoCombobox({}, {
		store: recentAnomaliesStore,
		templateString: recentAnomaliesTemplateString,
		callback: function(anomalyId) {
			WorkflowService.addEventToInvestigation(currentInvestigation.id, anomalyId, function() {
				this.showDetails(currentInvestigation.id, currentLetter);
			}.createDelegate(this));
		},
		scope: this
	});
	
	var associateMenu;
	
	this.showDetails = function(investigationId, letter) {
			
		this.el.mask("Loading Investigation..", "x-mask-loading");

		WorkflowService.getInvestigationDetail(investigationId, function(investigation) {
			
			dwr.engine.beginBatch();
			
			currentInvestigation = investigation;
			currentLetter = letter;
			
			// Generate the map
			renderMarkers(investigation, letter);
		//	updateLocalityOverlay(investigation);
			
			detailsMap.showMap();
						
			// Show the details text, sets up event handlers, and loads activities.
			renderDetails(investigation);
			
			dwr.engine.endBatch();
			
			this.el.unmask();
			
		}.createDelegate(this));

	};
		
	this.refreshDetails = function(investigation) {
		renderDetails(investigation);
	};

	function isAnomalyInactive(anomaly) {
		var actualValue = currentInvestigation.currentValues[anomaly.id];
		return actualValue < anomaly.actualThreshold;
	}
	
	function renderMarkers(investigation, letter) {
		detailsMap.clearMarkers();
			
		detailsMap.changeMapExtents(investigation.envelope);
		var mrendered = {};
		
		Ext.each(investigation.anomalies, function(anomaly) {
			var inactive = isAnomalyInactive(anomaly);
			var colors = inactive ? ['amber-light', 'amber-dark'] : ['orange-light', 'orange-dark'];
			var geographyName = anomaly.geography.name;
			var existingMarker = mrendered[geographyName];
			if (existingMarker) {
				existingMarker.content = existingMarker.content + formatInfoWindowItem(anomaly);
				if (existingMarker.inactive && !inactive) {
					existingMarker.inactive = false;
					existingMarker.colors = colors;
				}	
			} else {
				mrendered[geographyName] = {
					title: geographyName,
					content: formatInfoWindowItem(anomaly),
					locationPoint: anomaly.locationPoint,
					colors: colors,
					inactive: inactive
				};
			}
							
		}, this);
		
		Ext.each(Object.keys(mrendered), function(key) {
			var item = mrendered[key];
			detailsMap.addMarker(item.locationPoint, item);
			console.log("marker: ", item.locationPoint);
		}, this);

		detailsMap.addMarker(investigation.organizationPoint, {
			letter: letter,
			title: investigation.organizationName,
			content: formatInvestigationInfoWindowItem(investigation),
			colors: ['blue-light', 'blue-dark']
		});

	}
	
	function formatInfoWindowItem(anomaly) {
		return EpiCenter.common.templates.anomalyTitle.applyTemplate(anomaly);
	}

	
		
	function formatInvestigationInfoWindowItem(investigation) {
		return investigationInfoPopupTemplate.apply({
			description: investigation.description,
			investigator: investigation.assignedTo ? investigation.assignedTo.value : investigation.createdBy.value,
			org: investigation.organizationName
		});
	}
	
	function renderDetails(investigation) {
		
		investigation.dateString = investigation.timestamp.format("m/d/Y");
		investigation.startDateString = investigation.timestamp.format("g:i a T");
		investigation.assignedToString = investigation.assignedTo ? "the investigation is being led by " + investigation.assignedTo.value : "this investigation has no leader.";
		investigation.anomalyCount = investigation.anomalies.length;
		
		detailsTemplate.overwrite(Ext.get(detailsContainer), investigation);
		detailsPanel.setTitle(investigation.dateString + " - " + investigation.description + " (" + investigation.id + ")");
		
		activitiesDataStore.load({ arg: [ investigation.id ] });
		
		// Create the assignTo menu..
		createAssignToMenu(investigation);
						
		// Create the otherActions menu..
		createOtherActionsMenu(investigation);
		
		// Create the "associate anomalies" menu.
		createAssociateEventsMenu(investigation);
		
		// Hook up the HtmlEditor window for comments..
		var addCommentsEl = Ext.get(addCommentsId);
		addCommentsEl.on("click", function(e) {
			commentEditor.setValue("");
			commentEditorWindow.investigationId = investigation.id;
			commentEditorWindow.show(addCommentsEl);
			// this is ridiculous, but if we do not set loooong timeout here, text editor will not
			// get its keyboard focus
			setTimeout(function() {
				commentEditor.focus();
			}, 1000);
		});
		
		loadAnomaliesList(investigation);
		
	}
	
	function loadAnomaliesList(investigation) {
		anomaliesDataStore.loadData(investigation.anomalies);
	}
	
	function createAssignToMenu(investigation) {
		
		var assignToMenu = new Ext.menu.Menu({
			id: "assignToMenu"
		});
			
		Ext.each(investigation.users, function(user) {
				
			assignToMenu.add({
				text: user.value,
				key: user.id,
				checked: investigation.assignedTo.id === user.id,
				group: "assigned"
			});
		});
		
		var changeAssignmentEl = Ext.get(changeAssignmentId);
		changeAssignmentEl.on("click", function(e) {
			assignToMenu.show(changeAssignmentEl);
		});
			
		assignToMenu.on("itemclick", function(item) {
			if (investigation.assignedTo.id !== item.key) {
				WorkflowService.updateInvestigationAssignment(investigation.id, item.key, function(user){
					investigation.assignedTo = user;
					renderDetails(investigation);
				});
			}
		});
			
		return assignToMenu;
	}
	
	function createEventContextMenu() {
		
		var menu = new Ext.ux.ContextMenu({
			id: "eventContextMenu",
			items: [ {
				text: "Remove this event",
				handler: function() {
					Ext.MessageBox.confirm("Remove Event", "Are you sure you want to remove this anomaly from the investigation?", function(btn) {
						if (btn === "yes") {
							var rec = anomaliesDataStore.getAt(menu.opts.itemIndex);
							if (rec) {
								WorkflowService.removeEventFromInvestigation(currentInvestigation.id, rec.get("id"), function(){
									
									// Just remove it from the anomalies array, no need for another ajax request.
									var pos = 0;
									Ext.each(currentInvestigation.anomalies, function(anomaly) {
										if (anomaly.id == rec.get("id")) {
											currentInvestigation.anomalies.removeItem(pos);
											return false;
										}
										pos++;
									}, this);
																		
									renderDetails(currentInvestigation);
									
								}.createDelegate(this));
							}
						}
					})
				},
				scope: this
			}]
		});
		
		anomaliesView.on("contextmenu", function(view, index, node, e) {
			
			console.log("x, y: ", e.getPageX(), e.getPageY());
			menu.opts = {
				pageX: e.getPageX(),
				pageY: e.getPageY(),
				itemIndex: index
			};
			menu.display();
			
			e.stopPropagation();
			
		}, this);
	}	
			
	function getRecentAnomaliesProxyArgs() {
		var whenWherePanel = EpiCenter.core.Viewport.panels.investigationPanel.whenWherePanel;
		var endDate = new Date();
		var startDate = endDate.add(Date.MONTH, -6);
		var geography = whenWherePanel.getGeography();
		return [startDate, endDate, geography ];
	}
	
	function createAssociateEventsMenu(investigation) {
		recentAnomaliesStore.proxy.args = getRecentAnomaliesProxyArgs();
		dropdownPanel.refresh();
		
		var associateEl = Ext.get(associateEventsId);
		associateEl.on("click", function(e) {
			dropdownPanel.display(associateEl);
		});

		return;
	}
	
	function updateLocalityOverlay(investigation) {
		detailsMap.addFeatureListOverlay(investigation.localityLayers, investigation.localities.pluck("id"));
	}
	
	function createOtherActionsMenu(investigation) {
		
		var otherActionsMenu = new Ext.menu.Menu({
			id: "otherActionsMenu"
		});
		
		Ext.each(investigation.state.transitions, function(transition) {
			
			otherActionsMenu.add({
				text: transition.action,
				key: transition.id,
				toState: transition.toState
			});
		});
		
		var otherActionsEl = Ext.get(otherActionsId);
		otherActionsEl.on("click", function(e) {
			otherActionsMenu.show(otherActionsEl);
		});
			
		otherActionsMenu.on("itemclick", function(item) {
			if (item.toState.stateType == "TERMINAL") {
				resolutionDetails = new EpiCenter.lib.ResolutionDetails({
					toState: item.toState,
					investigation: investigation,
					submitCallback: function(values) {
						WorkflowService.takeActionOnInvestigationWithDispositions(investigation.id, item.key, values, function(state) {
							investigation.state = state;
							renderDetails(investigation);
						});
					}
				});
				resolutionDetails.show();
			} else {
				WorkflowService.takeActionOnInvestigation(investigation.id, item.key, function(state) {
					investigation.state = state;
					renderDetails(investigation);
				});
			}
		});
			
		return otherActionsMenu;
	}
	
	Ext.apply(this, config);
	
	EpiCenter.lib.InvestigationDetails.superclass.constructor.call(this, {
		layout: "anchor",
		border: false,
		cls: 'details-panel',
		items: [ {
			anchor: '100% 50%',
			layout: "column",
			border: false,
		 	items: [ detailsPanel, {
				fill: true,
				layout: "fit",
				columnWidth: 0.5,
				items: detailsMap
			} ]
		}, {
			anchor: '100% 50%',
			layout: "column",
			border: false,
			items: [ {
				fill: true,
				columnWidth: 0.5,
		 		autoScroll: true,
		 		items: activitiesView
			}, {
				fill: true,
				columnWidth: 0.5,
		 		autoScroll: true,
		 		items: anomaliesView
			} ]
		} ]
	});

	/*
	* Switches to the anomaly details view when an item is selected.
	*/
	anomaliesView.on("click", function(view, index) {
		var anomaly = anomaliesDataStore.getAt(index);
		EpiCenter.core.Viewport.activateEvent(anomaly.get('id'), anomaly.get('timestamp'), anomaly.get('geography').name, anomaly.get('geography').id);
	}, this);
	
	var contextMenu = createEventContextMenu();
	
};

Ext.extend(EpiCenter.lib.InvestigationDetails, Ext.Panel);
