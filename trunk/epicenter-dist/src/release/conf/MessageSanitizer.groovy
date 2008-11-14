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
package scripts;

import ca.uhn.hl7v2.util.MessageIDGenerator;

import org.slf4j.MDC

import com.hmsinc.mergence.scripting.BasePreProcessor
import com.hmsinc.mergence.scripting.PreProcessorException
import com.hmsinc.mergence.Configuration

import java.util.regex.Pattern

class MessageSanitizer extends BasePreProcessor {
		
	def hl7Version = Configuration.DEFAULT_HL7_VERSION.value()
	
	def INVALID_XML_CHARS = Pattern.compile("[^\t\r\n\u0020-\uD7FF\uE000-\uFFFD]")
	
 	def String process(String message) {
		
 		def fieldSep = "|"

 		def recSep = "^"
 		
 		def facility = ""
 		
 		def processedMessage = new StringBuilder()
 		
		def segments = message.split("\\r").toList()
		
		// Loop over each segment:
		for (segment in segments) {
			
			def processedSegment = INVALID_XML_CHARS.matcher(segment).replaceAll("")
			
			if (segment.startsWith("MSH")) {
				
				// Get the separator characters:
				fieldSep = segment.charAt(3).toString()
				recSep = segment.charAt(4).toString()

				// Split the segment into it's fields:
				def fields = segment.split("\\" + fieldSep, -1).toList()

				// Trim whitespace from all fields:
				for (field in fields) {
					field = field.trim()
				}
				
				// We can repair the message if we have the core MSH, throw an exception otherwise:
				if (fields.size() < 8) {
					throw new PreProcessorException("MSH in message contained only " + fields.size() + " fields! [" + message + "]")
				}
				
				// Facility ID MUST be set:
				if (fields.get(3) == null) {
					throw new PreProcessorException("Facility ID was not set in message! [" + message + "]")
				}
				
				facility = fields.get(3)
				
				// Put the facility into the MDC for logging:
				MDC.put("facility", "[" + fields.get(3) + "] ")
				
				// Grow the list if necessary:
				if (fields.size() < 12) {
					logger.warn("MSH in message from " + facility + " only contains " + fields.size() + " fields")
					for (i in fields.size() .. 11) {
						fields.add(null)
					}
				}
				
				// Set default HL7 version:
				if (fields.get(11) == null || fields.get(11).equals("")) {
					logger.warn("No HL7 version set in message from " + facility)
					fields.set(11, hl7Version)
				} 

				// Fix MSH-11 (processing mode):
				if (fields.get(10) == null || fields.get(10).equals("")) {
					logger.warn("No MSH-11 set in message from " + facility)
					fields.set(10, "P")
				}
				
				// Fix MSH-9 (message type):
				def msh9 = fields.get(8).split("\\" + recSep, -1);
				if (msh9.length > 1) {
					if ((msh9[0] == null || msh9[0].equals("")) && (msh9[1].equals("A04") || msh9[1].equals("A08"))) {
						logger.warn("No message type (MSH-9-1) set in message from " + facility + ", defaulting to ADT")
						msh9[0] = "ADT"
					}

					// Ignore MSH-9-3:
					if (msh9.length > 2) {
						logger.warn("Ignoring MSH-9-3 in message from " + facility)
					}

					fields.set(8, msh9[0] + recSep + msh9[1])
				} else {
					throw new PreProcessorException("Invalid message type! [" + message + "]")
				}
				
				// Fix MSH-10 (message control id):
				if (fields.get(9) == null || fields.get(9).equals("")) {
					logger.warn("No MCID set in message from " + facility)
					fields.set(9, MessageIDGenerator.getInstance().getNewID())
				}
				
				// Reassemble:
				processedSegment = fields.join(fieldSep)
				
			} else if (segment.matches("^:\\d+PID:.*")) {
				logger.warn("Fixing malformed PID segment in message from" + facility)
				def fixPid = segments.split("PID:")
				processedSegment = "PID:" + fixPid[1]
			} 
		
			processedSegment = processedSegment.trim()
			if (processedSegment != null && processedSegment.indexOf(fieldSep) == 3) {
				processedMessage.append(processedSegment)
				processedMessage.append("\r")
			}
		}
		
		return processedMessage.toString()
 	}

}
