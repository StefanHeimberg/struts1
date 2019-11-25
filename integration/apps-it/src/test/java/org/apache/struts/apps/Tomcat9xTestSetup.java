/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts.apps;

import java.net.URL;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;

/**
 * Set up to run tests with Tomcat 9.x.  If the 'cargo.tomcat9x.home'
 * System property is not set, Tomcat 9.x will be downloaded from ibiblio.
 */
public class Tomcat9xTestSetup extends TestSetup {

    InstalledLocalContainer container;
    String version;
    String port;
    String localRepository;

    public Tomcat9xTestSetup(Test test) {
        super(test);
    }

    /**
     * Required until MSUREFIRE-119 is fixed.
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    protected void setUp() throws Exception {
        version = System.getProperty("version");
        port = System.getProperty("cargo.servlet.port");
        localRepository = System.getProperty("localRepository");

        // (1) Find the locally installed container.  This System property may
        //     be set in settings.xml, or on the command line with
        //     -Dcargo.tomcat9x.home=c:/java/apache-tomcat-9
        String tomcat9x = System.getProperty("cargo.tomcat9x.home");

        String tomcatHome;
        if (tomcat9x == null || tomcat9x.startsWith("$")) {
            System.out.println("INFO: Downloading Tomcat 9.0 from a mirror");
            Installer installer = new ZipURLInstaller(
                    new URL("http://mirror.easyname.ch/apache/tomcat/tomcat-9/v9.0.29/bin/apache-tomcat-9.0.29.zip"));
            installer.install();
            tomcatHome = installer.getHome();
        } else {
            tomcatHome = tomcat9x;
        }
        System.out.println("INFO: Tomcat home is " + tomcatHome);

        // (2) Create the Cargo Container instance wrapping our physical container
        LocalConfiguration configuration = (LocalConfiguration)
                new DefaultConfigurationFactory().createConfiguration(
                        "tomcat9x", ContainerType.INSTALLED, ConfigurationType.STANDALONE);
        container = (InstalledLocalContainer)
                new DefaultContainerFactory().createContainer(
                        "tomcat5x", ContainerType.INSTALLED, configuration);
        container.setHome(tomcatHome);

        // (3) Statically deploy WAR(s)
        configuration.addDeployable(getWAR("struts-blank"));
        configuration.addDeployable(getWAR("struts-cookbook"));
        configuration.addDeployable(getWAR("struts-examples"));
        configuration.addDeployable(getWAR("struts-faces-example1"));
        configuration.addDeployable(getWAR("struts-faces-example2"));
        configuration.addDeployable(getWAR("struts-mailreader"));
        configuration.addDeployable(getWAR("struts-scripting-mailreader"));
        configuration.addDeployable(getWAR("struts-el-example"));

        configuration.setProperty(ServletPropertySet.PORT, port);

        // (4) Start the container
        container.start();
    }

    protected void tearDown() throws Exception {
        // (6) Stop the container
        container.stop();
    }

    private WAR getWAR(String context) {
        return new WAR(localRepository + "/org/apache/struts/"
                + context + "/" + version + "/"
                + context + "-" + version + ".war");
    }

}
