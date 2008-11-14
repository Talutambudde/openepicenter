/**
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
package com.hmsinc.epicenter.webapp.util;

import org.directwebremoting.ConversionException;
import org.directwebremoting.convert.BaseV20Converter;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.NonNestedOutboundVariable;
import org.directwebremoting.extend.OutboundContext;
import org.directwebremoting.extend.OutboundVariable;
import org.directwebremoting.extend.ProtocolConstants;
import org.joda.time.DateTime;

/**
 * DWR converter for Joda DateTime objects.
 * 
 * @author shade
 * @version $Id: DateTimeConverter.java 1805 2008-07-03 13:55:39Z steve.kondik $
 */
public class DateTimeConverter extends BaseV20Converter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.directwebremoting.extend.Converter#convertInbound(java.lang.Class,
	 *      org.directwebremoting.extend.InboundVariable)
	 */
	public Object convertInbound(Class<?> paramType, InboundVariable data) throws ConversionException {

		DateTime ret = null;

		final String value = data.getValue();
		if (!value.trim().equals(ProtocolConstants.INBOUND_NULL)) {

			long millis = 0;
			if (value.length() > 0) {
				millis = Long.parseLong(value);
			}

			if (DateTime.class.equals(paramType)) {
				ret = new DateTime(millis);
			} else {
				throw new ConversionException(paramType);
			}
		}

		return ret;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.directwebremoting.extend.Converter#convertOutbound(java.lang.Object,
	 *      org.directwebremoting.extend.OutboundContext)
	 */
	public OutboundVariable convertOutbound(Object data, OutboundContext outctx) throws ConversionException {

		final long millis;
		if (data instanceof DateTime) {
			millis = ((DateTime) data).getMillis();
		} else {
			throw new ConversionException(data.getClass());
		}

		return new NonNestedOutboundVariable(new StringBuilder("new Date(").append(millis).append(")").toString());
	}

}
