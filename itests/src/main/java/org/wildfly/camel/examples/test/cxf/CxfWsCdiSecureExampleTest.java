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
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.camel.examples.test.common.SecurityUtils;
import org.wildfly.camel.test.common.utils.EnvironmentUtils;
import org.wildfly.camel.test.common.utils.FileUtils;
import org.wildfly.camel.test.common.utils.UserManager;
import org.wildfly.camel.test.common.utils.WildFlyCli;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@ServerSetup(CxfWsCdiSecureExampleTest.ClientCertSecurityDomainSetup.class)
public class CxfWsCdiSecureExampleTest {

    private static final Path WILDFLY_HOME = EnvironmentUtils.getWildFlyHome();

    private static final String CLIENT_CERT_KEYSTORE_PASSWORD = "123456";
    private static final String CLIENT_CRT = "client.crt";
    private static final String CLIENT_KEYSTORE = "client.keystore";
    private static final String CLIENT_TRUSTSTORE = "client.truststore";
    private static final String SERVER_CRT = "server.crt";
    private static final String SERVER_KEYSTORE = "server.keystore";
    private static final String SERVER_TRUSTSTORE = "server.truststore";
    private static final String UNTRUSTED_CRT = "untrusted.crt";
    private static final String UNTRUSTED_KEYSTORE = "untrusted.keystore";

    private static final String BASE_URI = "https://localhost:8443";
    private static final String UI_URI;
    private static final String CXF_ENDPOINT_URI;
    private static final String WS_MESSAGE_TEMPLATE = "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<Body>"
            + "<greet xmlns=\"http://jaxws.cxf.examples.camel.wildfly.org/\">"
            + "<message xmlns=\"\">%s</message>"
            + "<name xmlns=\"\">%s</name>"
            + "</greet>"
            + "</Body>"
            + "</Envelope>";

    private static final String CLIENT_ROLE = "testRole";
    private static final String CLIENT_ALIAS = "client";

    static {
        UI_URI = BASE_URI.replace("https:", "http:").replace("8443", "8080") + "/example-camel-cxf-jaxws-cdi-secure/cxf/";
        CXF_ENDPOINT_URI = BASE_URI + "/webservices/greeting-secure-cdi";
    }

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class,
                new File("target/examples/example-camel-cxf-jaxws-cdi-secure.war"));
    }

    @Test
    public void ui() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(UI_URI);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setEntity(new StringEntity("message=Hello&name=Kermit", StandardCharsets.UTF_8));
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                Assert.assertEquals(200, response.getStatusLine().getStatusCode());

                HttpEntity entity = response.getEntity();
                String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                Assert.assertTrue(body.contains("Hello Kermit"));
            }

        }
    }

    @Test
    public void greetAnonymous() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(createUntrustedClientCertSocketFactory(WILDFLY_HOME)).build()) {
            HttpPost request = new HttpPost(CXF_ENDPOINT_URI);
            request.setHeader("Content-Type", "text/xml");
            request.setHeader("soapaction", "\"urn:greet\"");

            request.setEntity(
                    new StringEntity(String.format(WS_MESSAGE_TEMPLATE, "Hi", "Joe"), StandardCharsets.UTF_8));
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                Assert.assertEquals(403, response.getStatusLine().getStatusCode());
            }
        }
    }

    @Test
    public void greetClientCert() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(createTrustedClientCertSocketFactory(WILDFLY_HOME)).build()) {
            HttpPost request = new HttpPost(CXF_ENDPOINT_URI);
            request.setHeader("Content-Type", "text/xml");
            request.setHeader("soapaction", "\"urn:greet\"");

            request.setEntity(
                    new StringEntity(String.format(WS_MESSAGE_TEMPLATE, "Hi", "Joe"), StandardCharsets.UTF_8));
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                Assert.assertEquals(200, response.getStatusLine().getStatusCode());

                HttpEntity entity = response.getEntity();
                String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                Assert.assertTrue(body.contains("Hi Joe"));
            }
        }
    }

    private static SSLConnectionSocketFactory createTrustedClientCertSocketFactory(final Path wildflyHome)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
            IOException, UnrecoverableKeyException {
        final Path truststoreFile = resolveConfigFile(CLIENT_TRUSTSTORE);
        final Path keystoreFile = resolveConfigFile(CLIENT_KEYSTORE);
        return SecurityUtils.createSocketFactory(truststoreFile, keystoreFile, CLIENT_CERT_KEYSTORE_PASSWORD);
    }

    private static Path resolveConfigFile(String fileName) {
        return WILDFLY_HOME.resolve("standalone/configuration/"+ fileName);
    }

    private static SSLConnectionSocketFactory createUntrustedClientCertSocketFactory(final Path wildflyHome)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
            IOException, UnrecoverableKeyException {
        final Path truststoreFile = resolveConfigFile(CLIENT_TRUSTSTORE);
        final Path keystoreFile = resolveConfigFile(UNTRUSTED_KEYSTORE);
        return SecurityUtils.createSocketFactory(truststoreFile, keystoreFile, CLIENT_CERT_KEYSTORE_PASSWORD);
    }

    /**
     * Creates an Undertow {@code application-security-domain} called {@value #SECURITY_DOMAIN} and links it with the
     * default Elytron {@code ApplicationDomain} for authorization. Also adds some roles to
     * {@code application-roles.properties}.
     */
    static class ClientCertSecurityDomainSetup implements ServerSetupTask {

        /**
         * Copies server and clients keystores and truststores from this package to the given
         * {@code $wildflyHome/standalone/configuration}. Server truststore has accepted certificate from client keystore
         * and vice-versa
         *
         * @param wildflyHome
         * @throws java.io.IOException copying of keystores fails
         * @throws IllegalArgumentException workingFolder is null or it's not a directory
         */
        private static void copyKeyMaterial(final Path wildflyHome) throws IOException, IllegalArgumentException {
            final Path targetDirectory = wildflyHome.resolve("standalone/configuration");
            if (targetDirectory == null || !Files.isDirectory(targetDirectory)) {
                throw new IllegalArgumentException("Provide an existing folder as the method parameter.");
            }
            copy(SERVER_KEYSTORE, targetDirectory);
            copy(SERVER_TRUSTSTORE, targetDirectory);
            copy(SERVER_CRT, targetDirectory);
            copy(CLIENT_KEYSTORE, targetDirectory);
            copy(CLIENT_TRUSTSTORE, targetDirectory);
            copy(CLIENT_CRT, targetDirectory);
            copy(UNTRUSTED_KEYSTORE, targetDirectory);
            copy(UNTRUSTED_CRT, targetDirectory);
        }

        private static void copy(String fileName, Path targetDirectory) throws IOException {
            FileUtils.copy(CxfWsCdiSecureExampleTest.class.getClassLoader().getResource("keys/" + fileName),
                    targetDirectory.resolve(fileName));
        }

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            copyKeyMaterial(WILDFLY_HOME);
            try (UserManager um = UserManager.forStandaloneApplicationRealm()) {
                um.addRole(CLIENT_ALIAS, CLIENT_ROLE);
            }
            final URL cliUrl = this.getClass().getClassLoader().getResource("configure-tls-security.cli");
            final Path cliTmpPath = Files.createTempFile(WildFlyCli.class.getSimpleName(), ".cli");
            FileUtils.copy(cliUrl, cliTmpPath);
            new WildFlyCli().run(cliTmpPath).assertSuccess();
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            try (UserManager um = UserManager.forStandaloneApplicationRealm()) {
                um.removeRole(CLIENT_ALIAS, CLIENT_ROLE);
            }
            final URL cliUrl = this.getClass().getClassLoader().getResource("remove-tls-security.cli");
            final Path cliTmpPath = Files.createTempFile(WildFlyCli.class.getSimpleName(), ".cli");
            FileUtils.copy(cliUrl, cliTmpPath);
            new WildFlyCli().run(cliTmpPath).assertSuccess();
        }
    }
}
