/**
 *  Copyright (C) 2000-2012 The Software Conservancy and Original Authors.
 *  All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to
 *  deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 *  sell copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 *
 *  Nothing in this notice shall be deemed to grant any rights to trademarks,
 *  copyrights, patents, trade secrets or any other intellectual property of the
 *  licensor or any contributor except as expressly stated herein. No patent
 *  license is granted separate from the Software, for code that you delete from
 *  the Software, or for combinations of the Software with other software or
 *  hardware.
 */
package org.chorusbdd.chorus.util.config;

import org.chorusbdd.chorus.util.ChorusOut;
import org.chorusbdd.chorus.util.DeepCopy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 12/06/12
 * Time: 09:33
 *
 * Reads and validates configuration specified either as system properties, command line switches or defaults
 *
 * Takes a collection of ConfigurationProperty which define the options and possible values - in the case of
 * Chorus itself these settings are defined in ChorusConfigurationProperty enum
 *
 * Configuration may be provided as switches/arguments to the process, or alternatively as System properties
 * Failing this, any defaults will apply
 *
 * The available parameters are provided as a List of ConfigurationProperty
 * Chorus has an enumeration ChorusConfigProperty which provides the config options for chorus
 */
public class ConfigReader implements DeepCopy<ConfigReader> {

    private List<ConfigurationProperty> properties;
    private String[] args;
    private Map<ConfigurationProperty, List<String>> propertyMap = new HashMap<ConfigurationProperty, List<String>>();

    //ordered list of property sources
    private PropertySource[] propertySources;

    /**
     * Create a configuration using System Properties and defaults only
     */
    public ConfigReader(List<ConfigurationProperty> properties) {
        this(properties, new String[0]);
    }

    /**
     * Create a configuration using process arguments, System Properties and defaults
     */
    public ConfigReader(List<ConfigurationProperty> properties, String[] args) {
        this.properties = properties;
        this.args = args;

        propertySources = new PropertySource[] {
            new CommandLineParser(properties),
            new SystemPropertyParser(properties),
            new DefaultsPropertySource(properties)
        };
    }

    public ConfigReader readConfiguration() throws InterpreterPropertyException {
        for ( PropertySource s : propertySources) {
            s.parseProperties(propertyMap, args);
        }

        validateProperties(propertyMap);
        return this;
    }

    public void setProperty(ConfigurationProperty property, List<String> values) {
        propertyMap.put(property, values);
    }

    public List<String> getValues(ConfigurationProperty property) {
        return propertyMap.get(property);
    }

    /**
     * @return The value for a property which should always a single value
     */
    public String getSingleValue(ConfigurationProperty property) {
        List<String> values = propertyMap.get(property);
        if ( values == null || values.size() != 1) {
            throw new RuntimeException("Property " + property + " did not have a single value, instead had the values " + values);
        }
        return values.get(0);
    }

    public boolean isSet(ConfigurationProperty property) {
        return propertyMap.containsKey(property);
    }

    /**
     * for boolean properties, is the property set true
     */
    public boolean isTrue(ConfigurationProperty property) {
        return isSet(property) && propertyMap.get(property).size() == 1
            && "true".equalsIgnoreCase(propertyMap.get(property).get(0));
    }

    private void validateProperties(Map<ConfigurationProperty, List<String>> results) throws InterpreterPropertyException {
        for ( ConfigurationProperty p : properties) {
            checkIfMandatory(results, p);

            if ( results.containsKey(p)) {
                List<String> values = results.get(p);
                checkValueCount(p, values);
                checkValues(p, values);
            }
        }

    }

    private void checkValues(ConfigurationProperty p, List<String> values) throws InterpreterPropertyException {
        Pattern pattern = Pattern.compile(p.getValidatingExpression());
        for (String value : values) {
            Matcher m = pattern.matcher(value);
            if ( ! m.matches()) {
                throw new InterpreterPropertyException(
                    "Could not parse the value for interpreter property " + p +
                    " expected to be in the form " + p.getExample()
                );
            }
        }
    }

    private void checkValueCount(ConfigurationProperty p, List<String> values) throws InterpreterPropertyException {
        if ( values.size() < p.getMinValueCount()) {
            throw new InterpreterPropertyException("At least " + p.getMinValueCount() + " value(s) must be supplied for the property " + p);
        } else if ( values.size() > p.getMaxValueCount()) {
            throw new InterpreterPropertyException("At most " + p.getMaxValueCount() + " value(s) must be supplied for the property " + p);
        }
    }

    private void checkIfMandatory(Map<ConfigurationProperty, List<String>> results, ConfigurationProperty p) throws InterpreterPropertyException {
        if ( p.isMandatory() && ! results.containsKey(p)) {
            throw new InterpreterPropertyException(
                "Mandatory property " + p + " was not set. " +
                "You can set this property with the -" + p.getSwitchName() + " switch, " +
                "the -" + p.getSwitchShortName() + " switch or the " +
                p.getSystemProperty() + " system property"
            );
        }
    }

    public static void logHelp() {
        ChorusOut.err.println("Usage: Main -f [feature_dirs | feature_files] -h [handler base packages] [-name Test Suite Name] [-t tag_expression] [-jmxListener host:port] [-showErrors] [-dryrun] [-showsummary] ");
    }

    public ConfigReader deepCopy() {
        List<ConfigurationProperty> l = new ArrayList<ConfigurationProperty>(properties);
        ConfigReader c = new ConfigReader(l);

        c.args = args;
        c.propertyMap = new HashMap<ConfigurationProperty, List<String>>();
        for ( Map.Entry<ConfigurationProperty, List<String>> e : propertyMap.entrySet()) {
            List<String> cloneList = new ArrayList<String>();
            cloneList.addAll(e.getValue());
            c.propertyMap.put(e.getKey(), cloneList);
        }
        return c;
    }

}