package org.chorusbdd.chorus.websockets;

import org.chorusbdd.chorus.annotations.Handler;
import org.chorusbdd.chorus.annotations.Step;

import java.util.concurrent.atomic.AtomicBoolean;

@Handler("Test Step Publisher Handler")
public class MockHandler {

    private static final AtomicBoolean stepCalled = new AtomicBoolean();

    @Step(value = "call a test step", id = "step1")
    public void callATestStep() {
        System.out.println("Hello!");
        stepCalled.set(true);
    }

    public boolean wasStepCalled() {
        return stepCalled.get();
    }
}
