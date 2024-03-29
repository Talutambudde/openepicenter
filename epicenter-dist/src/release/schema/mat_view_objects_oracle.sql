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

CREATE MATERIALIZED VIEW LOG ON INTERACTION WITH SEQUENCE, PRIMARY KEY, ROWID INCLUDING NEW VALUES;
CREATE MATERIALIZED VIEW LOG ON PATIENT_DETAIL WITH SEQUENCE, PRIMARY KEY, ROWID INCLUDING NEW VALUES;
CREATE MATERIALIZED VIEW LOG ON PATIENT WITH SEQUENCE, PRIMARY KEY, ROWID INCLUDING NEW VALUES;
CREATE MATERIALIZED VIEW LOG ON FACILITY WITH SEQUENCE, PRIMARY KEY, ROWID INCLUDING NEW VALUES;
CREATE MATERIALIZED VIEW LOG ON INTERACTION_CLASSIFICATION WITH SEQUENCE, PRIMARY KEY, ROWID INCLUDING NEW VALUES;
CREATE MATERIALIZED VIEW LOG ON DISCHARGE WITH SEQUENCE, PRIMARY KEY, ROWID INCLUDING NEW VALUES;
CREATE MATERIALIZED VIEW LOG ON ADMIT WITH SEQUENCE, PRIMARY KEY, ROWID INCLUDING NEW VALUES;
CREATE MATERIALIZED VIEW LOG ON REGISTRATION WITH SEQUENCE, PRIMARY KEY, ROWID INCLUDING NEW VALUES;

CREATE MATERIALIZED VIEW MV_INTERACTION_DETAIL
TABLESPACE HEALTH_CENTRAL_MAT_VIEW PCTFREE 10 INITRANS 1 MAXTRANS 255 STORAGE 
( INITIAL 64K BUFFER_POOL DEFAULT) LOGGING USING INDEX TABLESPACE HEALTH_CENTRAL_MAT_VIEW PCTFREE 10 INITRANS 2 MAXTRANS 255 STORAGE 
( INITIAL 64K BUFFER_POOL DEFAULT) REFRESH FAST ON DEMAND
AS 
select i.interaction_date, i.id_age_group, i.id as id_interaction, p.id_facility,
pd.id as id_patient_detail, pd.DOB, pd.zipcode as pat_zipcode, pd.id_gender,
i.id_patient_class, i.rowid as iROWID, p.rowid as rROWID, f.rowid as fROWID, pd.rowid as pdROWID
from interaction i, patient p, facility f, patient_detail pd
where p.id = i.id_patient
and f.id = p.id_facility
and i.ID_PATIENT_DETAIL = pd.id;

CREATE MATERIALIZED VIEW MV_INTERACTION_JOIN
TABLESPACE HEALTH_CENTRAL_MAT_VIEW PCTFREE 10 INITRANS 1 MAXTRANS 255 STORAGE 
( INITIAL 64K BUFFER_POOL DEFAULT) LOGGING USING INDEX TABLESPACE HEALTH_CENTRAL_MAT_VIEW PCTFREE 10 INITRANS 2 MAXTRANS 255 STORAGE 
( INITIAL 64K BUFFER_POOL DEFAULT) REFRESH FAST ON DEMAND
AS 
SELECT r.rowid as rROWID, id as id_interaction, '1' as interaction_type from registration r 
UNION ALL
select a.rowid as aROWID, id as id_interaction, '2' as interaction_type from admit a
UNION ALL
select d.rowid as dROWID, id as id_interaction, '3' as interaction_type from discharge d;

CREATE MATERIALIZED VIEW LOG ON MV_INTERACTION_DETAIL WITH SEQUENCE, ROWID INCLUDING NEW VALUES;
CREATE MATERIALIZED VIEW LOG ON MV_INTERACTION_JOIN WITH SEQUENCE, ROWID INCLUDING NEW VALUES;

CREATE MATERIALIZED VIEW MV_ALL_INTERACTION
TABLESPACE HEALTH_CENTRAL_MAT_VIEW PCTFREE 10 INITRANS 1 MAXTRANS 255 STORAGE 
( INITIAL 64K BUFFER_POOL DEFAULT) LOGGING USING INDEX TABLESPACE HEALTH_CENTRAL_MAT_VIEW PCTFREE 10 INITRANS 2 MAXTRANS 255 STORAGE 
( INITIAL 64K BUFFER_POOL DEFAULT) REFRESH FAST ON DEMAND
AS 
SELECT mid.interaction_date, mid.pat_zipcode, mid.id_age_group, mid.id_interaction,
mid.id_patient_class, mid.id_gender, mid.ID_FACILITY AS id_facility, mij.INTERACTION_TYPE, mid.rowid as midROWID, mij.rowid as mijROWID
FROM MV_INTERACTION_DETAIL mid, mv_interaction_join mij
WHERE mij.id_interaction = mid.id_interaction;

CREATE MATERIALIZED VIEW LOG ON MV_ALL_INTERACTION WITH SEQUENCE, ROWID INCLUDING NEW VALUES;

CREATE MATERIALIZED VIEW MV_CLASSIFICATION
TABLESPACE  HEALTH_CENTRAL_MAT_VIEW PCTFREE 10 INITRANS 1 MAXTRANS 255 STORAGE 
( INITIAL 64K BUFFER_POOL DEFAULT) LOGGING USING INDEX TABLESPACE  HEALTH_CENTRAL_MAT_VIEW PCTFREE 10 INITRANS 2 MAXTRANS 255 STORAGE 
( INITIAL 64K BUFFER_POOL DEFAULT) REFRESH FAST ON DEMAND
AS 
SELECT ai.interaction_date, ai.pat_zipcode, ic.id_classification, ai.id_age_group, ai.ID_INTERACTION,
ai.id_patient_class, ai.id_gender, ai.id_facility, ai.interaction_type, ai.ROWID AS aiROWID, ic.ROWID AS icROWID
FROM MV_ALL_INTERACTION ai, INTERACTION_CLASSIFICATION ic
WHERE ic.id_interaction = ai.id_interaction;

CREATE INDEX IDX_MV_CLASSIFICATION ON MV_CLASSIFICATION
(INTERACTION_DATE, interaction_type, PAT_ZIPCODE, ID_AGE_GROUP, ID_INTERACTION, id_patient_class, ID_GENDER, ID_FACILITY, ID_CLASSIFICATION)
LOGGING
TABLESPACE HEALTH_CENTRAL_MAT_VIEW
STORAGE    (
            BUFFER_POOL      KEEP
           )
NOPARALLEL;

CREATE INDEX IDX_MV_ALL_INTERACTION ON MV_ALL_INTERACTION
(INTERACTION_DATE, interaction_type, ID_AGE_GROUP, ID_INTERACTION, id_patient_class, ID_GENDER, ID_FACILITY, pat_zipcode)
LOGGING
TABLESPACE HEALTH_CENTRAL_MAT_VIEW
STORAGE    (
            BUFFER_POOL      KEEP
           )
NOPARALLEL;

CREATE INDEX IDX_MV_ALL_INTERACTION2 ON MV_ALL_INTERACTION
(INTERACTION_DATE, pat_zipcode)
LOGGING
TABLESPACE HEALTH_CENTRAL_MAT_VIEW
STORAGE    (
            BUFFER_POOL      KEEP
           )
NOPARALLEL;

