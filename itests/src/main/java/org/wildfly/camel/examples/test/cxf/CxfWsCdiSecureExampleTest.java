/*
 * #%L
 * Wildfly Camel :: Testsuite
 * %%
 * Copyright (C) 2013 - 2017 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wildfly.camel.examples.test.cxf;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.security.Constants;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.camel.examples.test.common.ServerReload;
import org.wildfly.camel.examples.test.common.UserManager;
import org.wildfly.camel.test.common.http.HttpRequest;
import org.wildfly.camel.test.common.http.HttpRequest.HttpResponse;
import org.wildfly.camel.test.common.utils.DMRUtils;

@RunAsClient
@RunWith(Arquillian.class)
@ServerSetup(CxfWsCdiSecureExampleTest.ServerSecuritySetup.class)
public class CxfWsCdiSecureExampleTest {

    private static final String HTTPS_HOST = "https://localhost:8443";
    private static final String ENDPOINT_ADDRESS = "http://localhost:8080/example-camel-cxf-jaxws-cdi-secure/cxf/";

    static class ServerSecuritySetup implements ServerSetupTask {

        private static final String APPLICATION_USER = "CN=localhost";
        private static final String APPLICATION_PASSWORD = "testPassword1+";
        private static final String APPLICATION_ROLE = "testRole";

        private static final String TRUSTSTORE_PASSWORD = "password";
        private static final String TRUSTSTORE_PATH = "${jboss.home.dir}/standalone/configuration/application.keystore";

        private static final String ADDRESS_SYSTEM_PROPERTY_TRUST_STORE_PASSWORD = "system-property=javax.net.ssl.trustStorePassword";
        private static final String ADDRESS_SYSTEM_PROPERTY_TRUST_STORE = "system-property=javax.net.ssl.trustStore";

        private static final String ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CERTIFICATE_TRUST_DOMAIN = "subsystem=security/security-domain=certificate-trust-domain";
        private static final String ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CERTIFICATE_TRUST_DOMAIN_JSSE_CLASSIC = ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CERTIFICATE_TRUST_DOMAIN + "/jsse=classic";
        private static final String ADDRESS_ATTRIBUTE_TRUSTSTORE = "truststore";

        private static final String ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CLIENT_CERT = "subsystem=security/security-domain=client-cert";
        private static final String ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CLIENT_CERT_AUTH_CLASSIC = ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CLIENT_CERT + "/authentication=classic";

        private static final String ADDRESS_SUBSYSTEM_UNDERTOW_HTTPS_LISTENER = "subsystem=undertow/server=default-server/https-listener=https";


        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            // Make WildFly generate a keystore
            HttpRequest.post(HTTPS_HOST).getResponse();

            UserManager.addApplicationUser(APPLICATION_USER, APPLICATION_PASSWORD);
            UserManager.addRoleToApplicationUser(APPLICATION_USER, APPLICATION_ROLE);

            ModelNode[] steps = new ModelNode[8];
            steps[0] = DMRUtils.createOpNode(ADDRESS_SYSTEM_PROPERTY_TRUST_STORE, ModelDescriptionConstants.ADD);
            steps[0].get(ModelDescriptionConstants.VALUE).set(TRUSTSTORE_PATH);

            steps[1] = DMRUtils.createOpNode(ADDRESS_SYSTEM_PROPERTY_TRUST_STORE_PASSWORD, ModelDescriptionConstants.ADD);
            steps[1].get(ModelDescriptionConstants.VALUE).set(TRUSTSTORE_PASSWORD);

            steps[2] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CERTIFICATE_TRUST_DOMAIN, ModelDescriptionConstants.ADD);
            steps[3] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CERTIFICATE_TRUST_DOMAIN_JSSE_CLASSIC, ModelDescriptionConstants.ADD);

            steps[4] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CERTIFICATE_TRUST_DOMAIN_JSSE_CLASSIC, ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION);
            steps[4].get(ModelDescriptionConstants.NAME).set(ADDRESS_ATTRIBUTE_TRUSTSTORE);
            steps[4].get(ModelDescriptionConstants.VALUE).get(ModelDescriptionConstants.PASSWORD).set(TRUSTSTORE_PASSWORD);
            steps[4].get(ModelDescriptionConstants.VALUE).get(ModelDescriptionConstants.URL).set(TRUSTSTORE_PATH);

            steps[5] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CLIENT_CERT, ModelDescriptionConstants.ADD);
            steps[6] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CLIENT_CERT_AUTH_CLASSIC, ModelDescriptionConstants.ADD);
            steps[6].get(Constants.LOGIN_MODULES).get(0).get(Constants.CODE).set("CertificateRoles");
            steps[6].get(Constants.LOGIN_MODULES).get(0).get(Constants.FLAG).set("required");
            steps[6].get(Constants.LOGIN_MODULES).get(0).get(Constants.MODULE_OPTIONS, "securityDomain").set("certificate-trust-domain");
            steps[6].get(Constants.LOGIN_MODULES).get(0).get(Constants.MODULE_OPTIONS, "verifier").set("org.jboss.security.auth.certs.AnyCertVerifier");
            steps[6].get(Constants.LOGIN_MODULES).get(0).get(Constants.MODULE_OPTIONS, "rolesProperties").set("${jboss.home.dir}/standalone/configuration/application-roles.properties");

            steps[7] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_UNDERTOW_HTTPS_LISTENER, ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION);
            steps[7].get(ModelDescriptionConstants.NAME).set("verify-client");
            steps[7].get(ModelDescriptionConstants.VALUE).set("REQUESTED");

            ModelNode compositeNode = DMRUtils.createCompositeNode(steps);
            managementClient.getControllerClient().execute(compositeNode);

            reload(managementClient);
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            ModelNode[] steps = new ModelNode[5];
            steps[0] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_UNDERTOW_HTTPS_LISTENER, ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION);
            steps[0].get(ModelDescriptionConstants.NAME).set("verify-client");

            steps[1] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CLIENT_CERT, ModelDescriptionConstants.REMOVE);

            steps[2] = DMRUtils.createOpNode(ADDRESS_SUBSYSTEM_SECURITY_SECURITY_DOMAIN_CERTIFICATE_TRUST_DOMAIN, ModelDescriptionConstants.REMOVE);

            steps[3] = DMRUtils.createOpNode(ADDRESS_SYSTEM_PROPERTY_TRUST_STORE, ModelDescriptionConstants.REMOVE);
            steps[4] = DMRUtils.createOpNode(ADDRESS_SYSTEM_PROPERTY_TRUST_STORE_PASSWORD, ModelDescriptionConstants.REMOVE);

            ModelNode compositeNode = DMRUtils.createCompositeNode(steps);
            managementClient.getControllerClient().execute(compositeNode);

            UserManager.removeApplicationUser(APPLICATION_USER);
            UserManager.revokeRoleFromApplicationUser(APPLICATION_USER, APPLICATION_ROLE);
        }

        public void reload(final ManagementClient managementClient) throws Exception {
            ServerReload.executeReloadAndWaitForCompletion(managementClient.getControllerClient(), 60000);
        }
    }

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/examples/example-camel-cxf-jaxws-cdi-secure.war"));
    }

    @Test
    public void testSecureCxfSoapRoute() throws Exception {
        HttpResponse result = HttpRequest.post(ENDPOINT_ADDRESS)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .content("message=Hello&name=Kermit")
            .getResponse();

        Assert.assertTrue(result.getBody().contains("Hello Kermit"));
    }
}
