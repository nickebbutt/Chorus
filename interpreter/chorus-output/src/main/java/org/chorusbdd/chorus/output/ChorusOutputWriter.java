/**
 *  Copyright (C) 2000-2015 The Software Conservancy and Original Authors.
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
package org.chorusbdd.chorus.output;

import org.chorusbdd.chorus.logging.LogLevel;
import org.chorusbdd.chorus.results.*;

import java.util.List;
import java.util.Set;

/**
 * Write the output for the Chrous interpreter
 */
public interface ChorusOutputWriter {

    void printFeature(FeatureToken feature);

    void printScenario(ScenarioToken scenario);

    void printStepStart(StepToken step, int depth);

    void printStepEnd(StepToken step, int depth);

    void printStackTrace(String stackTrace);

    void printMessage(String message);

    void printResults(ResultsSummary summary, List<FeatureToken> features, Set<CataloguedStep> cataloguedSteps);

    /**
     * Print a log message (when the ChorusOutputWriter is being used for log output as well as step output)
     */
    void log(LogLevel type, Object message);

    /**
     * Print an error message (when the ChorusOutputWriter is being used for for log output as well as step output)
     */
    void logError(LogLevel type, Throwable t);

    /**
     * Called to allow cleanup when Chorus exits
     */
    void dispose();
}
