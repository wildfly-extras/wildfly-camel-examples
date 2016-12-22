package org.wildfly.camel.examples.cxf.jaxws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.cxf.CxfComponent;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.security.JAASLoginInterceptor;
import org.apache.cxf.message.Message;
import org.wildfly.extension.camel.CamelAware;

@Startup
@CamelAware
@ApplicationScoped
@ContextName("java-dsl-context")
public class JavaDSLRouteBuilder extends RouteBuilder {


//	@Inject
//	private CamelContext camelContext;


    @Override
    public void configure() throws Exception {

    	CxfComponent cxfConsumerComponent = new CxfComponent(getContext());
    	CxfEndpoint cxfConsumerEndpoint = new CxfEndpoint("http://localhost:8080/webservices/greeting-cdi", cxfConsumerComponent);
    	cxfConsumerEndpoint.setBeanId("cxfConsumerEndpoint");
//    	cxfFromEndpoint.setDataFormat(DataFormat.PAYLOAD);
    	cxfConsumerEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);
    	List<Interceptor<? extends Message>> inInterceptors = cxfConsumerEndpoint.getInInterceptors();
    	JAASLoginInterceptor jaasLoginInterceptor =  new JAASLoginInterceptor();
    	jaasLoginInterceptor.setContextName("other");
    	inInterceptors.add(jaasLoginInterceptor);


    	CxfComponent cxfProducerComponent = new CxfComponent(getContext());
    	CxfEndpoint cxfProducerEndpoint = new CxfEndpoint("http://localhost:8080/webservices/greeting-cdi", cxfProducerComponent);
    	cxfProducerEndpoint.setBeanId("cxfProducerEndpoint");
    	cxfProducerEndpoint.setServiceClass(org.wildfly.camel.examples.cxf.jaxws.GreetingService.class);


    	cxfProducerEndpoint.setUsername("testUser");
    	cxfProducerEndpoint.setPassword("testPassword1+");

    	// without this a NullpointerException occurs during deployment
    	// TODO: open JIRA ticket for
    	// org.apache.camel.component.cxf.CxfEndpoint
    	// Lines 554 -558 should be Nullsafe
    	Map<String, Object> properties = cxfProducerEndpoint.getProperties();
    	if (properties == null) {
    		Map<String, Object> props = new HashMap<String, Object>();
    		AuthorizationPolicy authPolicy = new AuthorizationPolicy();
            authPolicy.setUserName("testUser");
            authPolicy.setPassword("testPassword1+");
//            factoryBean.getProperties().put(AuthorizationPolicy.class.getName(), authPolicy);
            props.put(AuthorizationPolicy.class.getName(), authPolicy);
    		cxfProducerEndpoint.setProperties(props);
    	}


    	from("direct:start").to(cxfProducerEndpoint);

    	from(cxfConsumerEndpoint).process(new GreetingsProcessor());

    }
}
