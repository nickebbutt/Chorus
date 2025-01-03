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
package org.chorusbdd.chorus.selftest.processhandler.processwithlog4jconfig;

import org.chorusbdd.chorus.annotations.ChorusResource;
import org.chorusbdd.chorus.annotations.Handler;
import org.chorusbdd.chorus.annotations.Step;
import org.chorusbdd.chorus.util.assertion.ChorusAssert;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 14/06/12
 * Time: 09:21
 */
@Handler("Process With Log4j")
public class ProcessWithLog4jHandler extends ChorusAssert {

    @ChorusResource("feature.dir")
    File featureDir;

    @Step("log4j has written to the (.*) file")
    public void checkLogFile(String name) {
        File f = new File(featureDir.getAbsolutePath() + File.separatorChar + "logs" + File.separatorChar + name );
        ChorusAssert.assertTrue("Log file does not exist", f.exists());
    }

    @Step("I can delete the log file (.*)")
    public void deleteLogFile(String name) {
        File f = new File(featureDir.getAbsolutePath() + File.separatorChar + "logs" + File.separatorChar + name );
        ChorusAssert.assertTrue("Log file does not exist", f.exists());
        assertTrue("Could not delete log", f.delete());
    }

}
