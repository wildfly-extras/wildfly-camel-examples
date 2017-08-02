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



	public final static String KEYSTORE_PATH = System.getProperty("jboss.server.config.dir") + "/application.keystore";


    @Override
    public void configure() throws Exception {

    	CxfComponent cxfConsumerComponent = new CxfComponent(getContext());
    	CxfEndpoint cxfConsumerEndpoint = new CxfEndpoint("http://localhost:8080/webservices/greeting-cdi", cxfConsumerComponent);
    	cxfConsumerEndpoint.setBeanId("cxfConsumerEndpoint");
//    	cxfFromEndpoint.setDataFormat(DataFormat.PAYLOAD);
    	cxfConsumerEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);

    	SSLContextParameters consumerSslContextParameters = this.createConsumerSSLContextParameters();
    	cxfConsumerEndpoint.setSslContextParameters(consumerSslContextParameters);

    	List<Interceptor<? extends Message>> inInterceptors = cxfConsumerEndpoint.getInInterceptors();

    	JAASLoginInterceptor jaasLoginInterceptor =  new JAASLoginInterceptor();
    	jaasLoginInterceptor.setContextName("client-cert");
    	jaasLoginInterceptor.setAllowAnonymous(false);
    	// "server" is the alias of the public key in "${JBOSS-HOME}/standalone/configuration/application.keystore"
    	List<CallbackHandlerProvider> chp = Arrays.asList(new JBossCallbackHandlerTlsCert("server"));
		jaasLoginInterceptor.setCallbackHandlerProviders(chp);
    	inInterceptors.add(jaasLoginInterceptor);



//    	SimpleAuthorizingInterceptor authorizingInterceptor = new SimpleAuthorizingInterceptor();
//    	authorizingInterceptor.setAllowAnonymousUsers(false);
//    	Map<String, String> rolesMap = new HashMap<>(1);
//    	rolesMap.put("greet", "testRole");
//		authorizingInterceptor.setMethodRolesMap(rolesMap );
////    	String roles = "adminRole testRole";
////		authorizingInterceptor.setGlobalRoles(roles);
//    	inInterceptors.add(authorizingInterceptor);



    	CxfComponent cxfProducerComponent = new CxfComponent(getContext());
    	CxfEndpoint cxfProducerEndpoint = new CxfEndpoint("https://localhost:8443/webservices/greeting-cdi", cxfProducerComponent);
    	cxfProducerEndpoint.setBeanId("cxfProducerEndpoint");
    	cxfProducerEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);




    	SSLContextParameters producerSslContextParameters = this.createProducerSSLContextParameters();
		cxfProducerEndpoint.setSslContextParameters(producerSslContextParameters);




		//FIXME: not for use in production!!!!!
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {

			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		cxfProducerEndpoint.setHostnameVerifier(hostnameVerifier);






    	from("direct:start").to(cxfProducerEndpoint);

    	from(cxfConsumerEndpoint).process(new GreetingsProcessor());

    }



    private SSLContextParameters createProducerSSLContextParameters() {

    	KeyStoreParameters ksp = new KeyStoreParameters();
    	ksp.setResource(KEYSTORE_PATH);
    	ksp.setPassword("password");

    	KeyManagersParameters kmp = new KeyManagersParameters();
    	kmp.setKeyStore(ksp);
    	kmp.setKeyPassword("password");

    	SSLContextClientParameters sslContextClientParameters  = new SSLContextClientParameters();
    	SSLContextParameters sslContextParameters = new SSLContextParameters();
    	sslContextParameters.setClientParameters(sslContextClientParameters);
    	sslContextParameters.setKeyManagers(kmp);
    	sslContextParameters.setCertAlias("server");


    	// für self-signed....
    	TrustManagersParameters tmp = new TrustManagersParameters();
    	tmp.setKeyStore(ksp);
    	sslContextParameters.setTrustManagers(tmp);



//    	SSLContext context = sslContextParameters.createSSLContext(getContext());
//    	SSLEngine engine = context.createSSLEngine();

    	return sslContextParameters;
    }


    private SSLContextParameters createConsumerSSLContextParameters() {

    	KeyStoreParameters ksp = new KeyStoreParameters();
    	ksp.setResource(KEYSTORE_PATH);
    	ksp.setPassword("password");



    	TrustManagersParameters tmp = new TrustManagersParameters();
    	tmp.setKeyStore(ksp);

    	SSLContextServerParameters sslContextServerParameters  = new SSLContextServerParameters();
    	sslContextServerParameters.setClientAuthentication(ClientAuthentication.REQUIRE.name());
    	SSLContextParameters sslContextParameters = new SSLContextParameters();
    	sslContextParameters.setServerParameters(sslContextServerParameters);
		sslContextParameters.setTrustManagers(tmp);



		// für self-signed....
    	KeyManagersParameters kmp = new KeyManagersParameters();
    	kmp.setKeyStore(ksp);
    	kmp.setKeyPassword("password");
		sslContextParameters.setKeyManagers(kmp);



//    	SSLContext context = scp.createSSLContext(getContext());
//    	SSLEngine engine = context.createSSLEngine();

    	return sslContextParameters;
    }
}
