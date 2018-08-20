/**
 * MIT License
 *
 * Copyright (c) 2018 Chorus BDD Organisation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.chorusbdd.chorus.handlerconfig.configproperty;

import org.chorusbdd.chorus.annotations.Scope;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConfigBuilderTest {
    
    private ConfigBuilder configBuilder = new ConfigBuilder();
    
    @Test
    public void testICanBuildABeanProvidingASimpleStringProperty() {
        Properties p = new Properties();
        p.setProperty("stringProperty", "My Provided Value");
        ConfigClassWithSimpleProperty c = configBuilder.buildConfig(ConfigClassWithSimpleProperty.class, p);
        assertEquals("My Provided Value", c.getStringProperty());
    }

    @Test
    public void testADefaultValueIsUsedIfIDoNotProvideAValue() {
        Properties p = new Properties();
        ConfigClassWithSimpleProperty c = configBuilder.buildConfig(ConfigClassWithSimpleProperty.class, p);
        assertEquals("My Default Value", c.getStringProperty());
    }
    
    @Test
    public void testValidationRulesAreAppliedToMyProvidedValue() {
        Properties p = new Properties();
        p.setProperty("stringProperty", "This value will not validate");
        try {
            configBuilder.buildConfig(ConfigClassWithSimpleProperty.class, p);
            fail("Should not validate");
        } catch (Exception e) {
            assertEquals("Property value 'This value will not validate' does not match pattern 'My.*'", e.getMessage());
        }
    }

    @Test
    public void testAMandatoryPropertyMustBeProvidedIfADefaultDoesNotExist() {
        Properties p = new Properties();
        try {
            configBuilder.buildConfig(ConfigClassPropertyWithNoDefault.class, p);
            fail("Should not validate");
        } catch (Exception e) {
            assertEquals("Property stringProperty is mandatory but no value was provided", e.getMessage());
        }
    }

    @Test
    public void testConfigProperiesWithConversions() {
        Properties p = new Properties();
        p.setProperty("intProperty", "123");
        p.setProperty("floatProperty", "234.5");
        p.setProperty("longProperty", "345");
        p.setProperty("doubleProperty", "456.7");
        p.setProperty("booleanProperty", "true");
        
        ConfigClassPropertyWithConversions c = configBuilder.buildConfig(ConfigClassPropertyWithConversions.class, p);
        assertEquals( 123, c.intProperty);
        assertEquals( 234.5f, c.floatProperty, 0);
        assertEquals( 345, c.longProperty);
        assertEquals(456.7d, c.doubleProperty, 0);
        assertEquals( true, c.booleanProperty);
    }

    @Test
    public void testConfigProperiesWithPrimitiveSetters() {
        Properties p = new Properties();
        p.setProperty("intProperty", "123");
        p.setProperty("floatProperty", "234.5");
        p.setProperty("longProperty", "345");
        p.setProperty("doubleProperty", "456.7");
        p.setProperty("booleanProperty", "true");

        ConfigClassPropertyWithPrimitivesSetters c = configBuilder.buildConfig(ConfigClassPropertyWithPrimitivesSetters.class, p);
        assertEquals( 123, c.intProperty);
        assertEquals( 234.5f, c.floatProperty, 0);
        assertEquals( 345, c.longProperty);
        assertEquals(456.7d, c.doubleProperty, 0);
        assertEquals( true, c.booleanProperty);
    }

    @Test
    public void testConfigPropertiesWithEnumField() {
        Properties p = new Properties();
        p.setProperty("enumField", "feature");
        p.setProperty("enumFieldCaseInsensitive", "ScEnArIo");


        ConfigClassWithEnumTypes c = configBuilder.buildConfig(ConfigClassWithEnumTypes.class, p);
        assertEquals(Scope.FEATURE, c.scope);
        assertEquals(Scope.SCENARIO, c.scopeCaseInsensitive);
    }

    @Test
    public void testAnEnumValueWhichCantBeMappedThrowsException() {
        Properties p = new Properties();
        p.setProperty("enumField", "rgioergergerg");

        try {
            ConfigClassWithEnumTypes c = configBuilder.buildConfig(ConfigClassWithEnumTypes.class, p);
            fail("Should throw exception");
        } catch (Exception e) {
            assertEquals("Could not convert property value rgioergergerg to an instance of Enum class org.chorusbdd.chorus.annotations.Scope", e.getMessage());
        }
    }

    @Test
    public void testUnvalidatedConfigSettersCanAcceptEmptyStringAsValidPropertyValue() {
        Properties p = new Properties();
        p.setProperty("stringProperty", "");

        ConfigClassWithUnvalidatedStringProperty c = configBuilder.buildConfig(ConfigClassWithUnvalidatedStringProperty.class, p);
        assertEquals("", c.stringProperty);
    }


    static class ConfigClassWithUnvalidatedStringProperty {

        private String stringProperty;

        @ConfigProperty(
            name = "stringProperty",
            description = "Simple String Property")
        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public String getStringProperty() {
            return stringProperty;
        }
    }


    static class ConfigClassWithSimpleProperty {

        private String stringProperty;

        @ConfigProperty(
            name = "stringProperty",
            description = "Simple String Property",
            defaultValue = "My Default Value",
            validationPattern = "My.*")
        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public String getStringProperty() {
            return stringProperty;
        }
    }

    static class ConfigClassPropertyWithNoDefault {

        private String stringProperty;

        @ConfigProperty(
            name = "stringProperty",
            description = "Simple String Property"
        )
        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public String getStringProperty() {
            return stringProperty;
        }
    }

    static class ConfigClassPropertyWithConversions {

        private int intProperty;
        private long longProperty;
        private float floatProperty;
        private double doubleProperty;
        private boolean booleanProperty;

        @ConfigProperty(
            name = "intProperty",
            description = "intProperty"
        )
        public void setIntProperty(Integer intProperty) {
            this.intProperty = intProperty;
        }

        @ConfigProperty(
            name = "floatProperty",
            description = "floatProperty"
        )
        public void setFloatProperty(Float floatProperty) {
            this.floatProperty = floatProperty;
        }

        @ConfigProperty(
            name = "longProperty",
            description = "longProperty"
        )
        public void setLongProperty(Long longProperty) {
            this.longProperty = longProperty;
        }

        @ConfigProperty(
            name = "doubleProperty",
            description = "doubleProperty"
        )
        public void setDoubleProperty(Double doubleProperty) {
            this.doubleProperty = doubleProperty;
        }

        @ConfigProperty(
            name = "booleanProperty",
            description = "booleanProperty"
        )
        public void setBooleanProperty(Boolean booleanProperty) {
            this.booleanProperty = booleanProperty;
        }
    }

    static class ConfigClassPropertyWithPrimitivesSetters {

        private int intProperty;
        private long longProperty;
        private float floatProperty;
        private double doubleProperty;
        private boolean booleanProperty;

        @ConfigProperty(
            name = "intProperty",
            description = "intProperty"
        )
        public void setIntProperty(int intProperty) {
            this.intProperty = intProperty;
        }

        @ConfigProperty(
            name = "floatProperty",
            description = "floatProperty"
        )
        public void setFloatProperty(float floatProperty) {
            this.floatProperty = floatProperty;
        }

        @ConfigProperty(
            name = "longProperty",
            description = "longProperty"
        )
        public void setLongProperty(long longProperty) {
            this.longProperty = longProperty;
        }

        @ConfigProperty(
            name = "doubleProperty",
            description = "doubleProperty"
        )
        public void setDoubleProperty(double doubleProperty) {
            this.doubleProperty = doubleProperty;
        }

        @ConfigProperty(
            name = "booleanProperty",
            description = "booleanProperty"
        )
        public void setBooleanProperty(boolean booleanProperty) {
            this.booleanProperty = booleanProperty;
        }
    }


    public static class ConfigClassWithEnumTypes {

        private Scope scope;
        private Scope scopeCaseInsensitive;


        @ConfigProperty(
            name = "enumField",
            description = "Enum value"
        )
        public void setEnumField(Scope scope) {
            this.scope = scope;
        }

        @ConfigProperty(
            name = "enumFieldCaseInsensitive",
            description = "Enum value"
        )
        public void setEnumFieldCaseInsensitive(Scope scope) {
            this.scopeCaseInsensitive = scope;
        }
    }
}
