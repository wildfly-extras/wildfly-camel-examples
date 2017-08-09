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
package org.wildfly.camel.test.cxf;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.batch.Batch;
import org.jboss.as.cli.batch.BatchManager;
import org.jboss.as.cli.batch.BatchedCommand;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.domain.management.security.adduser.AddUser;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.camel.test.common.http.HttpRequest;
import org.wildfly.camel.test.common.http.HttpRequest.HttpResponse;

@RunWith(Arquillian.class)
public class CxfSecureWsCdiExampleTest {

    private static final String ENDPOINT_ADDRESS = "http://localhost:8080/example-camel-cxf-jaxws-security-cdi/cxf/";

    private static final String ADMIN_USER = "cliAdmin";
    private static final String ADMIN_PASSWORD = "cliAdminPassword1+";

    private static final String USER = "CN=localhost";
    private static final String PASSWORD = "testPassword1++";
    private static final String ROLE = "testRole";


    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/examples/example-camel-cxf-jaxws-security-cdi.war"));
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        addAppUser();
        addManagementUser();
        configureServerViaCLI();
    }



    @RunAsClient
    @Test
    public void testSayHelloCxfSoapRoute() throws Exception {

        //FIXME: very ugly, but unfortunately the CLI ":reload" has no "blocking" attribute;-(
        Thread.sleep(10000);

        HttpResponse result = HttpRequest.post(ENDPOINT_ADDRESS)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .content("message=Hello&name=Kermit")
                .timeout(30, TimeUnit.SECONDS)
                .getResponse();

            Assert.assertTrue(result.getBody().contains("Hello Kermit"));
    }

    private static void addAppUser() {
        String[] commandLine = {"-a", "-s", "-u", USER, "-p", PASSWORD, "-g", ROLE};
        AddUser.main(commandLine);
    }

    private static void addManagementUser() {
        String[] commandLine = { "-s", "-u", ADMIN_USER, "-p", ADMIN_PASSWORD};
        AddUser.main(commandLine);
    }

    private static void configureServerViaCLI() throws Exception {

        CommandContext ctx = CommandContextFactory.getInstance().newCommandContext(ADMIN_USER, ADMIN_PASSWORD.toCharArray());
        ctx.connectController();

        ModelControllerClient client = ctx.getModelControllerClient();

        try {

            // check if resources are already there
            ModelNode checkAddress = Operations.createAddress("system-property", "javax.net.ssl.trustStore");
            ModelNode checkOperation = Operations.createReadResourceOperation(checkAddress);
            ModelNode checkResult = client.execute(checkOperation);
            if (Operations.isSuccessfulOutcome(checkResult)) {
                return;
            } else {

                BatchManager batchManager = ctx.getBatchManager();
                if (!batchManager.isBatchActive()) {
                    batchManager.activateNewBatch();
                }
                Batch activeBatch = batchManager.getActiveBatch();

                BatchedCommand batchedCommand = ctx.toBatchedCommand("/system-property=javax.net.ssl.trustStore/:add(value=${jboss.home.dir}/standalone/configuration/application.keystore)");
                activeBatch.add(batchedCommand);
                batchedCommand = ctx.toBatchedCommand("/system-property=javax.net.ssl.trustStorePassword/:add(value=password)");
                activeBatch.add(batchedCommand);

                batchedCommand = ctx.toBatchedCommand("/subsystem=security/security-domain=certificate-trust-domain:add()");
                activeBatch.add(batchedCommand);
                batchedCommand = ctx.toBatchedCommand("/subsystem=security/security-domain=certificate-trust-domain/jsse=classic:add(truststore={\"password\"=>\"password\",\"url\"=>\"${jboss.home.dir}/standalone/configuration/application.keystore\"})");
                activeBatch.add(batchedCommand);

                batchedCommand = ctx.toBatchedCommand("/subsystem=security/security-domain=client-cert:add()");
                activeBatch.add(batchedCommand);
                batchedCommand = ctx.toBatchedCommand("/subsystem=security/security-domain=client-cert/authentication=classic:add(login-modules=[{\"code\"=\"CertificateRoles\",\"flag\"=\"required\",\"module-options\"=>[\"securityDomain\"=>\"certificate-trust-domain\",\"verifier\"=>\"org.jboss.security.auth.certs.AnyCertVerifier\",\"rolesProperties\"=>\"${jboss.home.dir}/standalone/configuration/application-roles.properties\"]}])");
                activeBatch.add(batchedCommand);
                batchedCommand = ctx.toBatchedCommand("/subsystem=undertow/server=default-server/https-listener=https/:write-attribute(name=verify-client,value=REQUESTED)");
                activeBatch.add(batchedCommand);


                ModelNode request = activeBatch.toRequest();
                ctx.setResolveParameterValues(false);
                ModelNode result = client.execute(request);
                batchManager.discardActiveBatch();


                if (Operations.isSuccessfulOutcome(result)) {
                    ModelNode operation = Operations.createOperation(":reload");
                    result = client.execute(operation);
                } else {
                    throw new RuntimeException("Failed to execute operation: " + request + " " + Operations.getFailureDescription(result).asString());
                }

            }
        } catch (CommandFormatException e) {
            e.printStackTrace();
            throw e;
        } finally {
            client.close();
            ctx.disconnectController();
        }

    }

}
