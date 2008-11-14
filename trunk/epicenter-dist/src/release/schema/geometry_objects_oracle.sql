--
-- Copyright (C) 2008 University of Pittsburgh
-- 
-- 
-- This file is part of Open EpiCenter
-- 
--     Open EpiCenter is free software: you can redistribute it and/or modify
--     it under the terms of the GNU General Public License as published by
--     the Free Software Foundation, either version 3 of the License, or
--     (at your option) any later version.
-- 
--     Open EpiCenter is distributed in the hope that it will be useful,
--     but WITHOUT ANY WARRANTY; without even the implied warranty of
--     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--     GNU General Public License for more details.
-- 
--     You should have received a copy of the GNU General Public License
--     along with Open EpiCenter.  If not, see <http://www.gnu.org/licenses/>.
-- 
-- 
--   
--
CREATE OR REPLACE VIEW geo_county
(ID, geometry, NAME, id_geo_state
UNIQUE RELY DISABLE NOVALIDATE,
CONSTRAINT pk_county
PRIMARY KEY (ID) RELY DISABLE NOVALIDATE) AS
SELECT ID, geometry, NAME, id_geo_state
FROM GEOGRAPHY
WHERE TYPE = 'C';

CREATE OR REPLACE VIEW geo_region
(ID, geometry, NAME
UNIQUE RELY DISABLE NOVALIDATE,
CONSTRAINT geo_region
PRIMARY KEY (ID) RELY DISABLE NOVALIDATE) AS
SELECT ID, geometry, NAME
FROM GEOGRAPHY
WHERE TYPE = 'R';

CREATE OR REPLACE VIEW geo_state
(ID, geometry, NAME, ABBREVIATION
UNIQUE RELY DISABLE NOVALIDATE,
CONSTRAINT pk_state
PRIMARY KEY (ID) RELY DISABLE NOVALIDATE) AS
SELECT ID, geometry, NAME, ABBREVIATION
FROM GEOGRAPHY
WHERE TYPE = 'S';

CREATE OR REPLACE VIEW geo_zipcode
(ID, geometry, NAME, po_name, id_geo_state
UNIQUE RELY DISABLE NOVALIDATE,
CONSTRAINT pk_zipcode
PRIMARY KEY (ID) RELY DISABLE NOVALIDATE) AS
SELECT ID, geometry, NAME, po_name, id_geo_state
FROM GEOGRAPHY
WHERE TYPE = 'Z';

CREATE INDEX IDX_SPAT_GEOGRAPHY_1 ON GEOGRAPHY(CENTROID)
INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS('sdo_indx_dims=2, layer_gtype=point');

CREATE INDEX IDX_SPAT_GEOGRAPHY_2 ON GEOGRAPHY(GEOMETRY)
INDEXTYPE IS MDSYS.SPATIAL_INDEX;

CREATE INDEX IDX_SPAT_FACILITY_1 ON FACILITY(GEOMETRY)
INDEXTYPE IS MDSYS.SPATIAL_INDEX;