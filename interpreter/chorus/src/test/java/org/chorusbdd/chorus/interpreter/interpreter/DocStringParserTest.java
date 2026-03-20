/**
 * MIT License
 *
 * Copyright (c) 2026 Chorus BDD Organisation.
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
package org.chorusbdd.chorus.interpreter.interpreter;

import org.chorusbdd.chorus.parser.FeatureFileParser;
import org.chorusbdd.chorus.pathscanner.FileReaderSupplier;
import org.chorusbdd.chorus.results.FeatureToken;
import org.chorusbdd.chorus.results.ScenarioToken;
import org.chorusbdd.chorus.results.StepToken;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests that Gherkin DocStrings are correctly parsed into the action field of a StepToken.
 *
 * A DocString block (""" ... """) appearing immediately after a step line is treated as an
 * extension of that step's action text: the DocString content is appended to the step text
 * separated by a newline, so the full action can be matched by a capture-group regex on a
 * Handler @Step method just like any other step argument.
 */
public class DocStringParserTest {

    private static final String TEST_FEATURE_FILE = "DocStringParserTest.test";

    @Test
    public void testSingleLineDocStringIsAppendedToStepAction() throws Exception {
        FeatureToken feature = parseFeature();

        ScenarioToken scenario = feature.getScenarios().get(0);
        StepToken step = scenario.getSteps().get(0);

        assertEquals("Given", step.getType());
        assertEquals("I receive the following body\nHello World", step.getAction());
    }

    @Test
    public void testMultiLineDocStringIsAppendedToStepAction() throws Exception {
        FeatureToken feature = parseFeature();

        ScenarioToken scenario = feature.getScenarios().get(1);
        StepToken step = scenario.getSteps().get(0);

        assertEquals("Given", step.getType());
        assertEquals("I receive the following body\nLine One\nLine Two\nLine Three", step.getAction());
    }

    @Test
    public void testDocStringPreservesBlankLines() throws Exception {
        FeatureToken feature = parseFeature();

        // Third scenario: DocString with a blank line inside
        ScenarioToken scenario = feature.getScenarios().get(2);
        StepToken whenStep = scenario.getSteps().get(1); // "When I receive the following body"

        assertEquals("When", whenStep.getType());
        assertEquals("I receive the following body\nFirst paragraph\n\nSecond paragraph after blank line",
                whenStep.getAction());
    }

    @Test
    public void testStepsAfterDocStringAreStillParsed() throws Exception {
        FeatureToken feature = parseFeature();

        ScenarioToken scenario = feature.getScenarios().get(2);
        assertEquals(3, scenario.getSteps().size());

        StepToken givenStep = scenario.getSteps().get(0);
        StepToken whenStep  = scenario.getSteps().get(1);
        StepToken thenStep  = scenario.getSteps().get(2);

        assertEquals("I perform setup", givenStep.getAction());
        assertTrue(whenStep.getAction().startsWith("I receive the following body\n"));
        assertEquals("I verify the result", thenStep.getAction());
    }

    /**
     * Demonstrates that the DocString action can be matched and captured by a regex pattern
     * as used in a Handler @Step annotation.
     *
     * Given a step action "I receive the following body\nLine One\nLine Two\nLine Three",
     * a @Step pattern of "I receive the following body\n((?s:.*))\" will capture the
     * DocString content as a method argument, just like any other captured group.
     */
    @Test
    public void testDocStringContentCapturedByStepRegexGroup() throws Exception {
        FeatureToken feature = parseFeature();

        ScenarioToken scenario = feature.getScenarios().get(1);
        StepToken step = scenario.getSteps().get(0);
        String action = step.getAction();

        // Pattern as it would appear in a @Step annotation on a Handler method:
        //   @Step("I receive the following body\n((?s:.*))")
        //   public void receiveBody(String body) { ... }
        //
        // (?s:.*) enables DOTALL for the capture group so '.' matches newlines,
        // allowing the entire multi-line DocString to be captured as one argument.
        Pattern stepPattern = Pattern.compile("I receive the following body\n((?s:.*))");
        Matcher matcher = stepPattern.matcher(action);

        assertTrue("Step pattern should match action containing DocString", matcher.matches());
        assertEquals("Line One\nLine Two\nLine Three", matcher.group(1));
    }

    // -----------------------------------------------------------------------

    private FeatureToken parseFeature() throws Exception {
        File f = getFileResourceWithName(TEST_FEATURE_FILE);
        FeatureFileParser parser = new FeatureFileParser();
        List<FeatureToken> features = parser.parse(new FileReaderSupplier(f));
        assertEquals("Expected exactly one feature", 1, features.size());
        return features.get(0);
    }

    private File getFileResourceWithName(String fileName) {
        URL url = getClass().getResource(fileName);
        if (url != null) {
            return new File(url.getFile());
        }
        return null;
    }
}
