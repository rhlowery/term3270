package com.rhlowery.term3270;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber test suite runner.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/rhlowery/term3270")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, 
                        value = "pretty, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
public class RunCucumberTest {
}
