#!/bin/sh
#
# Copyright (C) 2008 University of Pittsburgh
# 
# 
# This file is part of Open EpiCenter
# 
#     Open EpiCenter is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
# 
#     Open EpiCenter is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
# 
#     You should have received a copy of the GNU General Public License
#     along with Open EpiCenter.  If not, see <http://www.gnu.org/licenses/>.
# 
# 
#   
#

SURVEILLANCE_HOME=.
JAVA_OPTS="-Xms128m -Xmx256m -Dcom.sun.management.jmxremote"
if [ "$RUN_ONCE" = "true" ]
then
	JAVA_OPTS="$JAVA_OPTS -DrunOnce=true"
else
	JAVA_OPTS="$JAVA_OPTS -DrunOnce=false"
fi

java $JAVA_OPTS -cp $SURVEILLANCE_HOME/lib/classworlds-1.1.jar \
	-Dclassworlds.conf=$SURVEILLANCE_HOME/conf/surveillance.conf \
	-Dsurveillance.home=$SURVEILLANCE_HOME \
	org.codehaus.classworlds.Launcher $*

