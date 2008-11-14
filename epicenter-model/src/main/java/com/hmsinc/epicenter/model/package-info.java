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
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters(value = {
  @javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value = com.hmsinc.ts4j.jaxb.DateTimeAdapter.class, type = org.joda.time.DateTime.class)
})
@org.hibernate.annotations.TypeDefs({
  @org.hibernate.annotations.TypeDef(name = "joda", typeClass = org.joda.time.contrib.hibernate.PersistentDateTime.class),
  @org.hibernate.annotations.TypeDef(name = "geometry", typeClass = com.hmsinc.hibernate.spatial.GeometryType.class)
})
package com.hmsinc.epicenter.model;
