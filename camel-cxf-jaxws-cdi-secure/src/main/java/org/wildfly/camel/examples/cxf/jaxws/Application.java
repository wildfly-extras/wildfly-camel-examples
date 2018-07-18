/*
 * #%L
 * Wildfly Camel :: Example :: Camel CXF JAX-WS CDI Secure
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
package org.wildfly.camel.examples.cxf.jaxws;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.cxf.CxfComponent;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextClientParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;

@Named("cxf_cdi_security_app")
public class Application {

    private static final String CLIENT_CERT_KEYSTORE_PASSWORD = "123456";
    private static final String CLIENT_CERT_KEYSTORE_PATH = System.getProperty("jboss.server.config.dir")
            + "/client.keystore";
    private static final String CLIENT_CERT_TRUSTSTORE_PATH = System.getProperty("jboss.server.config.dir")
            + "/client.truststore";
    private static final String CXF_ENDPOINT_URI = "https://localhost:8443/webservices/greeting-secure-cdi";

    @Inject
    @ContextName("cxfws-secure-cdi-camel-context")
    CamelContext camelContext;

    @Named("cxfConsumerEndpoint")
    @Produces
    public CxfEndpoint createCxfConsumerEndpoint() {
        CxfComponent cxfConsumerComponent = new CxfComponent(this.camelContext);
        CxfEndpoint cxfConsumerEndpoint = new CxfEndpoint(CXF_ENDPOINT_URI, cxfConsumerComponent);
        cxfConsumerEndpoint.setBeanId("cxfConsumerEndpoint");
        cxfConsumerEndpoint.setServiceClass(GreetingService.class);

        return cxfConsumerEndpoint;
    }

    @Named("cxfProducerEndpoint")
    @Produces
    public CxfEndpoint createCxfProducerEndpoint() {
        CxfComponent cxfProducerComponent = new CxfComponent(this.camelContext);
        CxfEndpoint cxfProducerEndpoint = new CxfEndpoint(CXF_ENDPOINT_URI, cxfProducerComponent);
        cxfProducerEndpoint.setBeanId("cxfProducerEndpoint");
        cxfProducerEndpoint.setServiceClass(GreetingService.class);

        SSLContextParameters producerSslContextParameters = this.createProducerSSLContextParameters();
        cxfProducerEndpoint.setSslContextParameters(producerSslContextParameters);

        // Not for use in production
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        cxfProducerEndpoint.setHostnameVerifier(hostnameVerifier);

        return cxfProducerEndpoint;
    }

    private SSLContextParameters createProducerSSLContextParameters() {
        final KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(CLIENT_CERT_KEYSTORE_PATH);
        ksp.setPassword(CLIENT_CERT_KEYSTORE_PASSWORD);

        final KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyStore(ksp);
        kmp.setKeyPassword(CLIENT_CERT_KEYSTORE_PASSWORD);

        final SSLContextClientParameters sslContextClientParameters = new SSLContextClientParameters();
        final SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setClientParameters(sslContextClientParameters);
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setCertAlias("client");

        // so that the client trusts the self-signed server certificate
        final KeyStoreParameters trustStoreParams = new KeyStoreParameters();
        trustStoreParams.setResource(CLIENT_CERT_TRUSTSTORE_PATH);
        trustStoreParams.setPassword(CLIENT_CERT_KEYSTORE_PASSWORD);
        final TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(trustStoreParams);
        sslContextParameters.setTrustManagers(tmp);

        return sslContextParameters;
    }

    @Named("greetingsProcessor")
    @Produces
    public Processor produceGreetingsProcessor() {
        return new GreetingsProcessor();
    }

}
