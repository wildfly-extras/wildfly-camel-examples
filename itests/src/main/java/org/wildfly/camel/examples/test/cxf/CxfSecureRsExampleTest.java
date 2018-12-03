/*
 * #%L
 * Wildfly Camel :: Testsuite
 * %%
 * Copyright (C) 2013 - 2014 RedHat
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
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import org.wildfly.camel.test.common.http.HttpRequest;
import org.wildfly.camel.test.common.http.HttpRequest.HttpResponse;
import org.wildfly.camel.test.common.utils.FileUtils;
import org.wildfly.camel.test.common.utils.UserManager;
import org.wildfly.camel.test.common.utils.WildFlyCli;

@RunAsClient
@RunWith(Arquillian.class)
@ServerSetup({ CxfSecureRsExampleTest.BasicSecurityDomainSetup.class })
public class CxfSecureRsExampleTest {

    private static final String CXF_ENDPOINT_URI = "http://localhost:8080/rest/greet/hello";
    private static final String UI_URI = "http://localhost:8080/example-camel-cxf-jaxrs-secure/cxf";
    private static final String BAD_USER = "badRsUser";
    private static final String BAD_USER_PASSWORD = "badUserPassword1+";
    private static final String GOOD_USER = "testRsUser";
    private static final String GOOD_USER_PASSWORD = "testRsPassword1+";
    private static final String GOOD_USER_ROLE = "testRsRole";

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/examples/example-camel-cxf-jaxrs-secure.war"));
    }

    private static void assertGreet(String uri, String user, String password, int responseCode,
            String responseBody) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, CertificateException, IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(uri + "/Joe");
            request.setHeader("Content-Type", "application/json");
            if (user != null) {
                String auth = user + ":" + password;
                String authHeader = "Basic "
                        + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
                request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            }
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                final int actualCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(responseCode, actualCode);
                if (actualCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    Assert.assertTrue(body.startsWith(responseBody));
                }
            }
        }
    }

    @Test
    public void greetAnonymous() throws Exception {
        assertGreet(CXF_ENDPOINT_URI, null, null, 401, null);
    }

    @Test
    public void greetBasicBadUser() throws Exception {
        /* the user can authenticate, but does not have the required role assigned */
        assertGreet(CXF_ENDPOINT_URI,
                BAD_USER, BAD_USER_PASSWORD, 403,
                null);
    }

    @Test
    public void greetBasicGoodUser() throws Exception {
        assertGreet(CXF_ENDPOINT_URI,
                GOOD_USER, GOOD_USER_PASSWORD, 200,
                "Hello Joe");
    }

    @Test
    public void ui() throws Exception {
        HttpResponse result = HttpRequest.post(UI_URI)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .content("name=Kermit")
            .getResponse();

        Assert.assertEquals(200, result.getStatusCode());
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        Assert.assertTrue(result.getBody().contains("Hello Kermit from " + hostAddress));
    }

    static class BasicSecurityDomainSetup implements ServerSetupTask {

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            try (UserManager um = UserManager.forStandaloneApplicationRealm()) {
                um
                        .addUser(GOOD_USER, GOOD_USER_PASSWORD)
                        .addRole(GOOD_USER, GOOD_USER_ROLE)
                        .addUser(BAD_USER, BAD_USER_PASSWORD)
                ;
            }
            final URL cliUrl = this.getClass().getClassLoader().getResource("configure-basic-security-rs.cli");
            final Path cliTmpPath = Files.createTempFile(WildFlyCli.class.getSimpleName(), ".cli");
            FileUtils.copy(cliUrl, cliTmpPath);
            new WildFlyCli().run(cliTmpPath).assertSuccess();
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            try (UserManager um = UserManager.forStandaloneApplicationRealm()) {
                um
                        .removeUser(GOOD_USER)
                        .removeRole(GOOD_USER, GOOD_USER_ROLE)
                        .removeUser(BAD_USER)
                ;
            }
            final URL cliUrl = this.getClass().getClassLoader().getResource("remove-basic-security-rs.cli");
            final Path cliTmpPath = Files.createTempFile(WildFlyCli.class.getSimpleName(), ".cli");
            FileUtils.copy(cliUrl, cliTmpPath);
            new WildFlyCli().run(cliTmpPath).assertSuccess();
        }
    }
}
