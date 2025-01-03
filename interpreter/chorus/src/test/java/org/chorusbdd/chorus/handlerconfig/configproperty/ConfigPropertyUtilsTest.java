/**
 * MIT License
 *
 * Copyright (c) 2025 Chorus BDD Organisation.
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

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ConfigPropertyUtilsTest {

    @Test
    public void testEnumWithPermittedCharacters() {
        Pattern actual = ConfigPropertyUtils.createValidationPatternFromEnumType(Scope.class);
        assertEquals("(?i)SCENARIO|FEATURE", actual.pattern());
    }

    @Test
    public void testEnumWithBadCharacters() {
        Pattern actual = ConfigPropertyUtils.createValidationPatternFromEnumType(BadBadEnum.class);
        assertEquals("(?i)This_Enum_Val_Cointains_Really_\\$_BAD_CHARS_£", actual.pattern());
        assertTrue(actual.matcher(BadBadEnum.This_Enum_Val_Cointains_Really_$_BAD_CHARS_£.name()).matches());
    }

    private enum BadBadEnum {
       This_Enum_Val_Cointains_Really_$_BAD_CHARS_£;
    }

}