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
package org.chorusbdd.chorus.stepserver.handler;

import org.chorusbdd.chorus.annotations.ChorusResource;
import org.chorusbdd.chorus.annotations.Handler;
import org.chorusbdd.chorus.annotations.Scope;
import org.chorusbdd.chorus.annotations.Step;
import org.chorusbdd.chorus.handlerconfig.ConfigurationManager;
import org.chorusbdd.chorus.handlerconfig.HandlerConfigLoader;
import org.chorusbdd.chorus.logging.ChorusLog;
import org.chorusbdd.chorus.logging.ChorusLogFactory;
import org.chorusbdd.chorus.results.FeatureToken;
import org.chorusbdd.chorus.results.ScenarioToken;
import org.chorusbdd.chorus.stepserver.ClientConnectionException;
import org.chorusbdd.chorus.stepserver.StepServerManager;
import org.chorusbdd.chorus.util.ChorusException;
import org.chorusbdd.chorus.util.ScopeUtils;
import org.chorusbdd.chorus.util.handler.HandlerPatterns;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * This handler can be used to invoke steps on components running remotely across the network.
 * <p/>
 * The single Step method will match any regexp that ends with "(in|from) [mbean name]". 
 * 
 * In order to work, this handler must have metadata available for the mbean names that it will be connecting to. 
 * 
 * This metadata usually will be loaded from a chorus properties file see the Chorus wiki for more details of property file 
 * configuration and the various remoting properties.
 * 
 * An example configuration to connect to a component called 'MyRemoteComponent' using the jmx protocol is given below
 * This component is running on myserver.mydomain on port 18800 
 * 
 * remoting.MyRemoteComponent.connection=jmx:myserver.mydomain:18800
 *
 * It is also possible to load remoting config properties from the database. The database connection properties and the SQL used to load
 * the metadata are loaded from a properties file named in system property: -D org.chorusbdd.chorus.jmxexporter.db.properties=[file].
 */
@Handler(value = "StepServer", scope = Scope.FEATURE)
@SuppressWarnings("UnusedDeclaration")
public class StepServerHandler {

    private ChorusLog log = ChorusLogFactory.getLog(StepServerHandler.class);

    @ChorusResource("feature.dir")
    private File featureDir;

    @ChorusResource("feature.file")
    private File featureFile;

    @ChorusResource("feature.token")
    private FeatureToken featureToken;

    @ChorusResource("scenario.token")
    private ScenarioToken scenarioToken;

    @ChorusResource("subsystem.stepServerManager")
    private StepServerManager stepServerManager;

    @ChorusResource("subsystem.configurationManager")
    private ConfigurationManager configurationManager;


    @Step("StepServer start")
    public void startServer() throws Exception {
        stepServerManager.startServer(getConfig(StepServerManager.DEFAULT_SERVER_NAME));
    }

    @Step("StepServer stop")
    public void stopServer() throws Exception {
        stepServerManager.stopServer();
    }

    //A Directive which can be used to connect to one or more processes
    @Step("StepServer wait for the clients? " + HandlerPatterns.nameListPattern)
    public void waitForClientsDirective(String processNameList) throws Exception {
        waitForClients(processNameList);
    }

    @Step(".*StepServer clients? " + HandlerPatterns.nameListPattern + " (?:are|is) connected")
    public void waitForClientsToConnect(String processNameList) throws Exception {
        waitForClients(processNameList);
    }

    @Step(".*show all StepServer steps")
    public void showAllSteps() {
        stepServerManager.showAllSteps();
    }

    private void waitForClients(String processNameList) throws ClientConnectionException {
        List<String> componentNames = HandlerPatterns.getNames(processNameList);
        for ( String componentName : componentNames) {
            boolean success = stepServerManager.waitForClientConnection(componentName);
            if ( ! success) {
               throw new ChorusException("Client " + componentName + " is not connected");
            }
        }
    }

    private Properties getConfig(String configName) {
        Properties p = new HandlerConfigLoader().loadPropertiesForSubGroup(configurationManager, "stepserver", configName);
        new ScopeUtils().setScopeForContextIfNotConfigured(scenarioToken, p);
        return p;
    }

}