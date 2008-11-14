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
 * EpiCenter.lib.DurationField Extension Class
 *
 * @author  Steve Kondik
 * @version 0.1
 *
 * @class EpiCenter.lib.DurationField
 * @extends Ext.form.TextField
 */
EpiCenter.lib.DurationField = Ext.extend(Ext.form.TextField, {
	parseDuration: function(date, negate) {
		return EpiCenter.lib.Duration.parse(this.getValue(), date, negate);
	},

	normalizeDuration: function() {
		this.setValue(EpiCenter.lib.Duration.normalize(this.getValue()));
	},

	validateValue: function(value) {
		this.markInvalid("Invalid duration");
		var endDate = new Date();
		var startDate = this.parseDuration(endDate, true);
		return endDate != startDate;
	}
});

EpiCenter.lib.Duration = function() {
	
	function TextHelper(unit, unitConst, length, regex) {
		function time_regexp(unit) {
			return new RegExp('^\\s*(?:' +
				'(?:(1)\\s*' + unit + ')' +
				'|(?:(1\\d+|[2-9]{1}\\d*)\\s*' + unit + 's)' +
				')\\s*$');
		}

		var re = regex || time_regexp(unit);
		var len = length || 1;
		
		this.parse = function(text) {
			var rtn = null;
			var match = re.exec(text);
			if (match) {
				var valueString = match[1] || match[2];
				var value = Ext.num(parseInt(valueString, 10), 0);
				if (value !== 0) {
					rtn = value;
				}
			}
			return rtn;
		};

		this.shift = function(date, value, multiplier) {
			return date.add(unitConst, (value * len) * multiplier);
		};

		this.generate = function(value) {
			var rtn = value + ' ' + unit;
			return value == 1 ? rtn : rtn + 's';
		};
	}
	
	var textHelpers = [
		new TextHelper('year', Date.YEAR),
		new TextHelper('month', Date.MONTH),
		new TextHelper('week', Date.DAY, 7),
		new TextHelper('day', Date.DAY),
		new TextHelper('day', Date.DAY, 1, /^\s*(\d+)\s*$/i)
	];

	return {
		parse: function(durationString, date, negate) {
			
			var multiplier = negate === true ? -1 : 1;
			var rtn = date;
		
			for (var i = 0; i < textHelpers.length; i++) {
				var th = textHelpers[i];
				var value = th.parse(durationString);
				if (value) {
					rtn = th.shift(date, value, multiplier);
					break;
				}
			}
			return rtn;
		},

		normalize: function(durationString) {
			var rtn = durationString;

			for (var i = 0; i < textHelpers.length; i++) {
				var th = textHelpers[i];
				var value = th.parse(durationString);
				if (value) {
					rtn = th.generate(value);
					break;
				}
			}
			return rtn;
		}
	};

}();

Ext.reg('duration', EpiCenter.lib.DurationField);

