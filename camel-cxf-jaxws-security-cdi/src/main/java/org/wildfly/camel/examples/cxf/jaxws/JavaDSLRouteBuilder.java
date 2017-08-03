package org.wildfly.camel.examples.cxf.jaxws;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.cxf.CxfComponent;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.util.jsse.ClientAuthentication;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextClientParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.SSLContextServerParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.security.JAASLoginInterceptor;
import org.apache.cxf.interceptor.security.callback.CallbackHandlerProvider;
import org.apache.cxf.message.Message;
import org.wildfly.extension.camel.CamelAware;

@Startup
@CamelAware
@ApplicationScoped
@ContextName("java-dsl-context")
public class JavaDSLRouteBuilder extends RouteBuilder {

	private static final String CXF_PRODUCER_ENDPOINT_ADDRESS = "https://localhost:8443/webservices/greeting-cdi";

	// actually the same as producer address, but here "https" doesn't work. Is this a bug?
	private static final String CXF_CONSUMER_ENDPOINT_ADDRESS = "http://localhost:8080/webservices/greeting-cdi";


	private static final String WILDFLY_SECURITY_DOMAIN_NAME = "client-cert";


	private final static String KEYSTORE_PATH = System.getProperty("jboss.server.config.dir") + "/application.keystore";
	private static final String KEYSTORE_PASSWORD = "password";
	// "server" is the alias of the key in "${JBOSS-HOME}/standalone/configuration/application.keystore"
	private final static String TRUSTSTORE_ALIAS_NAME = "server";


	@Override
	public void configure() throws Exception {

		CxfEndpoint cxfConsumerEndpoint = createCxfConsumerEndpoint();

		CxfEndpoint cxfProducerEndpoint = createCxfProducerEndpoint();

		from("direct:start").to(cxfProducerEndpoint);

		from(cxfConsumerEndpoint).process(new GreetingsProcessor());

	}

	private CxfEndpoint createCxfProducerEndpoint() {
		CxfComponent cxfProducerComponent = new CxfComponent(getContext());
		CxfEndpoint cxfProducerEndpoint = new CxfEndpoint(CXF_PRODUCER_ENDPOINT_ADDRESS, cxfProducerComponent);
		cxfProducerEndpoint.setBeanId("cxfProducerEndpoint");
		cxfProducerEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);

		SSLContextParameters producerSslContextParameters = this.createProducerSSLContextParameters();
		cxfProducerEndpoint.setSslContextParameters(producerSslContextParameters);

		// FIXME: not for use in production!!!!!
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {

			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		cxfProducerEndpoint.setHostnameVerifier(hostnameVerifier);

		return cxfProducerEndpoint;
	}

	private SSLContextParameters createProducerSSLContextParameters() {

		KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource(KEYSTORE_PATH);
		ksp.setPassword(KEYSTORE_PASSWORD);

		KeyManagersParameters kmp = new KeyManagersParameters();
		kmp.setKeyStore(ksp);
		kmp.setKeyPassword(KEYSTORE_PASSWORD);

		SSLContextClientParameters sslContextClientParameters = new SSLContextClientParameters();
		SSLContextParameters sslContextParameters = new SSLContextParameters();
		sslContextParameters.setClientParameters(sslContextClientParameters);
		sslContextParameters.setKeyManagers(kmp);
		sslContextParameters.setCertAlias(TRUSTSTORE_ALIAS_NAME);

		// so that the client trusts the self-signed server certificate
		TrustManagersParameters tmp = new TrustManagersParameters();
		tmp.setKeyStore(ksp);
		sslContextParameters.setTrustManagers(tmp);

		return sslContextParameters;
	}

	private CxfEndpoint createCxfConsumerEndpoint() {
		CxfComponent cxfConsumerComponent = new CxfComponent(getContext());
		CxfEndpoint cxfConsumerEndpoint = new CxfEndpoint(CXF_CONSUMER_ENDPOINT_ADDRESS, cxfConsumerComponent);
		cxfConsumerEndpoint.setBeanId("cxfConsumerEndpoint");
		cxfConsumerEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);

		SSLContextParameters consumerSslContextParameters = this.createConsumerSSLContextParameters();
		cxfConsumerEndpoint.setSslContextParameters(consumerSslContextParameters);

		List<Interceptor<? extends Message>> inInterceptors = cxfConsumerEndpoint.getInInterceptors();

		// Authentication
		JAASLoginInterceptor jaasLoginInterceptor = new JAASLoginInterceptor();
		jaasLoginInterceptor.setContextName(WILDFLY_SECURITY_DOMAIN_NAME);
		jaasLoginInterceptor.setAllowAnonymous(false);
		List<CallbackHandlerProvider> chp = Arrays.asList(new JBossCallbackHandlerTlsCert(TRUSTSTORE_ALIAS_NAME));
		jaasLoginInterceptor.setCallbackHandlerProviders(chp);
		inInterceptors.add(jaasLoginInterceptor);

		// Authorization
		// SimpleAuthorizingInterceptor authorizingInterceptor = new SimpleAuthorizingInterceptor();
		// authorizingInterceptor.setAllowAnonymousUsers(false);
		// Map<String, String> rolesMap = new HashMap<>(1);
		// rolesMap.put("greet", "testRole");
		// authorizingInterceptor.setMethodRolesMap(rolesMap );
		//// String roles = "adminRole testRole";
		//// authorizingInterceptor.setGlobalRoles(roles);
		// inInterceptors.add(authorizingInterceptor);
		return cxfConsumerEndpoint;
	}

	private SSLContextParameters createConsumerSSLContextParameters() {

		KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource(KEYSTORE_PATH);
		ksp.setPassword(KEYSTORE_PASSWORD);

		TrustManagersParameters tmp = new TrustManagersParameters();
		tmp.setKeyStore(ksp);

		SSLContextServerParameters sslContextServerParameters = new SSLContextServerParameters();
		sslContextServerParameters.setClientAuthentication(ClientAuthentication.REQUIRE.name());
		SSLContextParameters sslContextParameters = new SSLContextParameters();
		sslContextParameters.setServerParameters(sslContextServerParameters);
		sslContextParameters.setTrustManagers(tmp);

		KeyManagersParameters kmp = new KeyManagersParameters();
		kmp.setKeyStore(ksp);
		kmp.setKeyPassword(KEYSTORE_PASSWORD);
		sslContextParameters.setKeyManagers(kmp);

		return sslContextParameters;
	}
}
