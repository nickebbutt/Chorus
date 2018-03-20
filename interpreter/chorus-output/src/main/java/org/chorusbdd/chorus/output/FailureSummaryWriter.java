package org.chorusbdd.chorus.output;

import org.chorusbdd.chorus.results.*;
import org.chorusbdd.chorus.util.ChorusConstants;

import java.util.List;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * Created by nickebbutt on 18/03/2018.
 */
public class FailureSummaryWriter {

    static final boolean suppressFailureSummary = Boolean.getBoolean(ChorusConstants.SUPPRESS_FAILURE_SUMMARY_PROPERTY);

    void printFailureSummary(List<FeatureToken> featuresList, Consumer<String> messageWriter) {
        if ( ! suppressFailureSummary && featuresList.size() > 1) {
            if (featuresList.stream().filter(f -> f.getEndState() == EndState.FAILED).count() > 0) {
                messageWriter.accept("Failure Summary:");
                printFailures(featuresList, messageWriter);
            }
        }
    }

    private void printFailures(List<FeatureToken> featuresList, Consumer<String> messageWriter) {

        featuresList.stream().filter(f-> f.getEndState() == EndState.FAILED).forEachOrdered(f -> {
            f.accept(new TokenVisitorAdapter() {

                final String INDENT = "  ";
                private StringBuilder stepIndent = new StringBuilder(INDENT + INDENT);

                @Override
                public void startVisit(FeatureToken featureToken) {
                    messageWriter.accept("");
                    messageWriter.accept(INDENT + featureToken.getNameWithConfiguration());
                }

                @Override
                public void startVisit(ScenarioToken scenarioToken) {
                    if ( scenarioToken.getEndState() == EndState.FAILED && ! scenarioToken.wasSkipped()) {
                        messageWriter.accept(INDENT + INDENT + scenarioToken.getName());
                    }
                }

                @Override
                public void startVisit(StepToken stepToken) {
                    stepIndent.append(INDENT);
                    String stepText = format("%s %s", stepToken.getType(), stepToken.getAction());
                    boolean consideredFailed = isConsideredFailed(stepToken);

                    if ( consideredFailed ) {
                        String bracketedMessage = stepToken.getMessage().length() > 0 ? "(" + stepToken.getMessage() + ")" : "";
                        String textToWrite = stepToken.isStepMacro() ?
                                format("%s %s >", stepIndent, stepText):
                                format("%s %s - %s %s", stepIndent, stepText, stepToken.getEndState(), bracketedMessage);
                        messageWriter.accept(textToWrite);
                    }
                }

                private boolean isConsideredFailed(StepToken stepToken) {
                    return stepToken.inOneOf(StepEndState.FAILED, StepEndState.TIMEOUT , StepEndState.UNDEFINED);
                }

                @Override
                public void endVisit(StepToken stepToken) {
                    stepIndent.setLength(stepIndent.length() - INDENT.length());
                }
            });
        });
    }
}