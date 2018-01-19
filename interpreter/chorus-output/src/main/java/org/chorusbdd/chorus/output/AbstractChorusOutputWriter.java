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

import org.chorusbdd.chorus.logging.ChorusOut;
import org.chorusbdd.chorus.logging.LogLevel;
import org.chorusbdd.chorus.results.*;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * User: nick
 * Date: 12/02/14
 * Time: 19:06
 */
public abstract class AbstractChorusOutputWriter implements ChorusOutputWriter {

    public static final String OUTPUT_FORMATTER_STEP_LENGTH_CHARS = "chorusOutputFormatterStepLength";
    public static final String OUTPUT_FORMATTER_STEP_LOG_RATE = "chorusOutputFormatterStepLogRate";
    private static ScheduledExecutorService stepProgressExecutorService = Executors.newSingleThreadScheduledExecutor();

    private PrintStream printStream;
    private PrintWriter printWriter;

    private int STEP_LENGTH_CHARS;

    public AbstractChorusOutputWriter() {
        //why -12? we are aiming for 120 chars, allow for a 7 char state and a 4 char leading indent and a single space between step text and result
        STEP_LENGTH_CHARS = Integer.parseInt(System.getProperty(OUTPUT_FORMATTER_STEP_LENGTH_CHARS, "120")) - 12;
    }
    
    protected ScheduledFuture progressFuture;
    protected Object printLock = new Object();

    public void printFeature(FeatureToken feature) {
        getPrintWriter().printf("Feature: %-84s%-7s %s%n", feature.getNameWithConfiguration(), "", "");
        getPrintWriter().flush();
    }

    public void printScenario(ScenarioToken scenario) {
        getPrintWriter().printf("  Scenario: %s%n", scenario.getName());
        getPrintWriter().flush();
    }

    public abstract void printStepStart(StepToken step, int depth);
    
    public void printStepEnd(StepToken step, int depth) {
        cancelStepAnimation();
        if ( ! step.isStepMacro() ) {
            StringBuilder depthPadding = getDepthPadding(depth);
            int stepLengthChars = getStepLengthCharCount() - depthPadding.length();
            printCompletedStep(step, depthPadding, stepLengthChars);
        }
    }

    protected void printStepWithoutEndState(StepToken step, StringBuilder depthPadding, int maxStepTextChars, String terminator) {
        getPrintWriter().printf("    " + depthPadding + "%-" + maxStepTextChars + "s" + terminator, getStepText(step));
        getPrintWriter().flush();
    }

    protected void printCompletedStep(StepToken step, StringBuilder depthPadding, int stepLengthChars) {
        StringBuilder output = new StringBuilder(String.format("    " + depthPadding + "%-" + stepLengthChars + "s %-7s %s", getStepText(step), getEndState(step), getMessage(step)));
        if ( step.getErrorDetails().length() > 0) {
            output.append(" ").append(step.getErrorDetails());
        }
        output.append(System.lineSeparator());
        getPrintWriter().print(output);
        getPrintWriter().flush();
    }

    protected void startProgressTask(Runnable progress, int frameRate) {
        progressFuture = stepProgressExecutorService.scheduleWithFixedDelay(progress, frameRate, frameRate, TimeUnit.MILLISECONDS);
    }

    protected void cancelStepAnimation() {
        synchronized (printLock) {
            if ( progressFuture != null) {
                progressFuture.cancel(false);
                progressFuture = null;
            }
        }
    }
    
    protected StringBuilder getDepthPadding(int depth) {
        StringBuilder sb = new StringBuilder();
        for ( int loop=1; loop < depth; loop++ ) {
            sb.append("..");
        }
        if ( depth > 1 ) {
            sb.append(" ");
        }
        return sb;
    }

    @Override
    public void printResults(ResultsSummary s, List<FeatureToken> featuresList) {
        if (s != null) {
            
            //If there's more than one feature and at least one failed, list the name(s)
            if ( s.getFeaturesFailed() > 0 && s.getTotalFeatures() > 1 ) {
                printMessage("Features failed:");
                featuresList.stream().filter(f -> f.getEndState() == EndState.FAILED).forEachOrdered(f ->
                    printMessage("  " + f.getNameWithConfiguration())
                );
            }
            
            //only show the pending count if there were pending steps, makes the summary more legible
            if ( s.getFeaturesPending() > 0) {
                printMessage(String.format("%nFeatures  (total:%d) (passed:%d) (pending:%d) (failed:%d)",
                        s.getTotalFeatures(),
                        s.getFeaturesPassed(),
                        s.getFeaturesPending(),
                        s.getFeaturesFailed()));
            } else {
                printMessage(String.format("%nFeatures  (total:%d) (passed:%d) (failed:%d)",
                        s.getTotalFeatures(),
                        s.getFeaturesPassed(),
                        s.getFeaturesFailed()));
            }

            //only show the pending count if there were pending steps, makes the summary more legible
            if ( s.getScenariosPending() > 0 ) {
                //print scenarios summary
                printMessage(String.format("Scenarios (total:%d) (passed:%d) (pending:%d) (failed:%d)",
                        s.getTotalScenarios(),
                        s.getScenariosPassed(),
                        s.getScenariosPending(),
                        s.getScenariosFailed()));
            } else {
                //print scenarios summary
                printMessage(String.format("Scenarios (total:%d) (passed:%d) (failed:%d)",
                        s.getTotalScenarios(),
                        s.getScenariosPassed(),
                        s.getScenariosFailed()));
            }

            //print steps summary
            printMessage(String.format("Steps     (total:%d) (passed:%d) (failed:%d) (undefined:%d) (pending:%d) (skipped:%d)",
                    s.getStepsPassed() + s.getStepsFailed() + s.getStepsUndefined() + s.getStepsPending() + s.getStepsSkipped(),
                    s.getStepsPassed(),
                    s.getStepsFailed(),
                    s.getStepsUndefined(),
                    s.getStepsPending(),
                    s.getStepsSkipped()));
        }
    }

    public void printStackTrace(String stackTrace) {
        getPrintWriter().print(stackTrace);
        getPrintWriter().flush();
    }

    public void printMessage(String message) {
        getPrintWriter().printf("%s%n", message);
        getPrintWriter().flush();
    }

    public void log(LogLevel level, Object message) {
        if ( level == LogLevel.ERROR ) {
            logErr(level, message);
        } else {
            logOut(level, message);
        }
    }

    public void logError(LogLevel level, Throwable t) {
        if ( level == LogLevel.ERROR ) {
            t.printStackTrace(ChorusOut.err);
        } else {
            t.printStackTrace(getPrintWriter());
            getPrintWriter().flush();
        }
    }

    protected void logOut(LogLevel type, Object message) {
        //Use 'Chorus' instead of class name for logging, since we are testing the log output up to info level
        //and don't want refactoring the code to break tests if log statements move class
        getPrintWriter().println(String.format("%s --> %-7s - %s", "Chorus", type, message));
        getPrintWriter().flush();
    }

    protected void logErr(LogLevel type, Object message) {
        //Use 'Chorus' instead of class name for logging, since we are testing the log output up to info level
        //and don't want refactoring the code to break tests if log statements move class
        getPrintWriter().println(String.format("%s --> %-7s - %s", "Chorus", type, message));
        getPrintWriter().flush();
    }

    protected int getStepLengthCharCount() {
        return STEP_LENGTH_CHARS;
    }

    protected abstract class StepProgressRunnable implements Runnable {
        private StringBuilder depthPadding;
        private int maxStepTextChars;
        private StepToken step;
        private int frameCount = 0;

        public StepProgressRunnable(StringBuilder depthPadding, int maxStepTextChars, StepToken step) {
            this.depthPadding = depthPadding;
            this.maxStepTextChars = maxStepTextChars;
            this.step = step;
        }

        public void run() {
            synchronized (printLock) {
                String terminator;
                if ( ! progressFuture.isCancelled()) {
                    frameCount++;
                    terminator = getTerminator(frameCount);
                    printStepWithoutEndState(step, depthPadding, maxStepTextChars, terminator);
                }
            }
        }

        protected abstract String getTerminator(int frameCount);
    }


    /**
     * May be overridden (e.g. to add control codes for colouring terminal output)
     */
    protected String getStepText(StepToken step) {
        return step.toString();
    }

    /**
     * May be overridden (e.g. to add control codes for colouring terminal output)
     */
    protected String getMessage(StepToken step) {
        return step.getMessage();
    }

    /**
     * May be overridden (e.g. to add control codes for colouring terminal output)
     */
    protected String getEndState(StepToken step) {
        return step.getEndState().toString();
    }
    
    
    /**
     * This is an extension point to change Chorus output
     *
     * The user can provider their own OutputWriter which extends the default and
     * overrides getPrintWriter() to return a writer configured for a different output stream
     *
     * n.b. this method will be called frequently so it is expected that the PrintWriter returned
     * will generally be cached and reused by the implementation, but in some circumstances it is
     * useful to be able to change the PrintWriter during the testing process so the details are
     * left to the implementation
     *
     * @return a PrintWriter to use for all logging
     */
    protected PrintWriter getPrintWriter() {
        if ( printWriter == null || printStream != ChorusOut.out) {
            printWriter = new PrintWriter(ChorusOut.out);
            printStream = ChorusOut.out;
        }
        return printWriter;
    }

    public void dispose() {
        //Nothing to do since don't need to close system out
    }

}
