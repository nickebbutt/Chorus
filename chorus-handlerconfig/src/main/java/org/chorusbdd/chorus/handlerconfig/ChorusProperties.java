package org.chorusbdd.chorus.handlerconfig;

import org.chorusbdd.chorus.executionlistener.ExecutionListener;
import org.chorusbdd.chorus.executionlistener.ExecutionListenerAdapter;
import org.chorusbdd.chorus.handlerconfig.properties.ClassPathPropertyLoader;
import org.chorusbdd.chorus.handlerconfig.properties.FilePropertyLoader;
import org.chorusbdd.chorus.handlerconfig.properties.JdbcPropertyLoader;
import org.chorusbdd.chorus.handlerconfig.properties.VariableExpandingPropertyLoader;
import org.chorusbdd.chorus.logging.ChorusLog;
import org.chorusbdd.chorus.logging.ChorusLogFactory;
import org.chorusbdd.chorus.results.ExecutionToken;
import org.chorusbdd.chorus.results.FeatureToken;
import org.chorusbdd.chorus.util.ChorusConstants;
import org.chorusbdd.chorus.util.properties.PropertyLoader;
import org.chorusbdd.chorus.util.properties.PropertyOperations;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.chorusbdd.chorus.handlerconfig.properties.VariableExpandingPropertyLoader.expandVariables;
import static org.chorusbdd.chorus.util.properties.PropertyOperations.properties;

/**
 * Created by GA2EBBU on 03/02/2015.
 *
 * The default ConfigurationManager for chorus
 */
public class ChorusProperties implements ConfigurationManager {

    private static ChorusLog log = ChorusLogFactory.getLog(ChorusProperties.class);

    private Properties sessionProperties = new Properties();
    private Properties featureProperties = new Properties();

    private FeatureToken currentFeature;

    @Override
    public Properties getAllProperties() {
        return expandVariables(
                properties(sessionProperties).merge(properties(featureProperties)), currentFeature
        ).loadProperties();
    }

    @Override
    public Properties getFeatureProperties() {
        return expandVariables(properties(featureProperties), currentFeature).loadProperties();
    }

    @Override
    public Properties getSessionProperties() {
        return expandVariables(properties(sessionProperties), currentFeature).loadProperties();
    }

    @Override
    public void addSessionProperties(Properties properties) {
        sessionProperties = properties(sessionProperties).merge(properties(properties)).getProperties();
    }

    @Override
    public void addFeatureProperties(Properties properties) {
        featureProperties = properties(featureProperties).merge(properties(properties)).getProperties();
    }

    @Override
    public void clearFeatureProperties() {
        featureProperties = new Properties();
    }

    @Override
    public void clearSessionProperties() {
        sessionProperties = new Properties();
    }


    private void loadFeatureProperties(FeatureToken feature) {
        PropertyOperations featureProps = PropertyOperations.emptyProperties();
        featureProps = mergeLoadersForDirectory(featureProps, feature.getFeatureDir(), feature);
        featureProps = mergeLoadersForDirectory(featureProps, new File(feature.getFeatureDir(), "conf"), feature);
        featureProps = addPropertiesFromDatabase(properties(featureProps.loadProperties())); //load the feature props once then merge any db props
        this.featureProperties = featureProps.loadProperties();
    }

    private void loadSessionProperties() {
        PropertyOperations sessionProps = PropertyOperations.emptyProperties();
        sessionProps.merge(new ClassPathPropertyLoader("/chorus.properties"));
        PropertyOperations withDbProps = addPropertiesFromDatabase(properties(sessionProps.loadProperties()));  //load the session props once then merge any db props
        this.sessionProperties = withDbProps.loadProperties();
    }

    /**
     * If there are any database properties defined in sourceProperties then use them to merge extra properties from the databsase
     * @param sourceProperties
     * @return
     */
    private PropertyOperations addPropertiesFromDatabase(PropertyOperations sourceProperties) {
        PropertyOperations dbPropsOnly = sourceProperties.filterByKeyPrefix(ChorusConstants.DATABASE_CONFIGS_PROPERTY_GROUP + ".")
                                                         .removeKeyPrefix(ChorusConstants.DATABASE_CONFIGS_PROPERTY_GROUP + ".");
        dbPropsOnly = VariableExpandingPropertyLoader.expandVariables(dbPropsOnly, currentFeature);
        Map<String, Properties> dbPropsByDbName = dbPropsOnly.splitKeyAndGroup("\\.").loadPropertyGroups();
        PropertyOperations o = sourceProperties;
        for ( Map.Entry<String, Properties> m : dbPropsByDbName.entrySet()) {
            log.debug("Creating loader for database properties " + m.getKey());

            //current properties which may be from properties files or classpath take precedence over db properties so merge them on top
            o = properties(new JdbcPropertyLoader(m.getValue())).merge(o);
        }
        return o;
    }

    @Override
    public ExecutionListener getExecutionListener() {
        return new PropertySubsystemExecutionListener();
    }

    /**
     * Load and remove properties at the appropriate points in the chorus lifecyle
     */
    private class PropertySubsystemExecutionListener extends ExecutionListenerAdapter {
        public void testsStarted(ExecutionToken testExecutionToken, List<FeatureToken> features) {
            loadSessionProperties();
        }

        public void featureStarted(ExecutionToken testExecutionToken, FeatureToken feature) {
            currentFeature = feature;
            loadFeatureProperties(feature);
        }

        public void featureCompleted(ExecutionToken testExecutionToken, FeatureToken feature) {
            clearFeatureProperties();
        }
    }

    private PropertyOperations mergeLoadersForDirectory(PropertyOperations props, File dir, FeatureToken featureToken) {
        props = props.merge(getPropertyLoader(dir, "chorus.properties"));
        String featureNameBase = featureToken.getFeatureFile().getName().replace(".feature", "");
        props = props.merge(getPropertyLoader(dir, featureNameBase + ".properties"));
        props = props.merge(getPropertyLoader(dir, featureNameBase + "-" + featureToken.getConfigurationName() + ".properties"));
        return props;
    }

    private PropertyLoader getPropertyLoader(File dir, String s) {
        File propsFile = new File(dir, s);
        return propsFile.exists() && propsFile.canRead() ? new FilePropertyLoader(propsFile) : PropertyLoader.NULL_LOADER;
    }
}
