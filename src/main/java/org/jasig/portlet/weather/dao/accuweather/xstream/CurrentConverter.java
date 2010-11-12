/* Copyright 2008 The JA-SIG Collaborative. All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portlet.weather.dao.accuweather.xstream;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jasig.portlet.weather.dao.accuweather.constants.Constants;
import org.jasig.portlet.weather.domain.Current;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter class to convert current conditions XML into a current object. Data
 * that is not part of the current object but falls within the current
 * conditions XML is put inside the context object to be retrieved by the parent
 * class.
 * 
 * @author Dustin Schultz
 * @version $Id$
 */
public class CurrentConverter implements Converter {
	
	private static final Logger logger = Logger.getLogger(CurrentConverter.class);

	public void marshal(Object arg0, HierarchicalStreamWriter arg1,
			MarshallingContext arg2) {
		// Don't need marshalling
	}
	
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		Current current = new Current();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (Constants.URL_TAG.equals(reader.getNodeName())) {
				//put this in context because it is not part of the current object
				context.put("moreInformationLink", reader.getValue());
			} else if (Constants.OBS_TIME_TAG.equals(reader.getNodeName())) {
				String obsTime = reader.getValue();
				Date observationTime = null;
				for (String pattern : Constants.dateFormatterPatterns) {
					//if we already successfully converted the sunsetTime, don't try again
					if (observationTime != null) { continue; }
					DateFormat formatter = new SimpleDateFormat(pattern);
					try {
						observationTime = formatter.parse(obsTime.trim());
					} catch (ParseException pe) {
						if (logger.isEnabledFor(Level.WARN)) {
							logger.warn("Unable to parse observation time " + obsTime);
						}
					}
				}
				//put this in context because it is not part of the current object
				context.put("observationTime", observationTime);
			} else if (Constants.CURR_PRESSURE_TAG.equals(reader.getNodeName())) {
				Double pressure = null;
				if (!Constants.PRESSURE_UNKNOWN.equals(reader.getValue())) {
					pressure = Double.valueOf(reader.getValue());
				}
				current.setPressure(pressure);
			} else if (Constants.CURR_TEMP_TAG.equals(reader.getNodeName())) {
				current.setTemperature(Integer.valueOf(reader.getValue()));
			} else if (Constants.CURR_HUMIDITY_TAG.equals(reader.getNodeName())) {
				String humidity = reader.getValue();
				current.setHumidity(Double.valueOf(humidity.substring(0, humidity.indexOf('%'))));
			} else if (Constants.CURR_COND_TAG.equals(reader.getNodeName())) {
				current.setCondition(reader.getValue());
			} else if (Constants.CURR_ICON_TAG.equals(reader.getNodeName())) {
				current.setImgName(reader.getValue());
			} else if (Constants.CURR_WIND_SPEED_TAG.equals(reader.getNodeName())) {
				current.setWindSpeed(Double.valueOf(reader.getValue()));
			} else if (Constants.CURR_WIND_DIR_TAG.equals(reader.getNodeName())) {
				current.setWindDirection(reader.getValue());
			}
			reader.moveUp();
		}
		return current;
	}

	@SuppressWarnings("unchecked")
	public boolean canConvert(Class clazz) {
		return clazz.equals(Current.class);
	}

}